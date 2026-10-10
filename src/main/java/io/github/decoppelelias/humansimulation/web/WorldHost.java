package io.github.decoppelelias.humansimulation.web;

import io.github.decoppelelias.humansimulation.domain.CensusRow;
import io.github.decoppelelias.humansimulation.domain.Command;
import io.github.decoppelelias.humansimulation.domain.DayReport;
import io.github.decoppelelias.humansimulation.domain.Species;
import io.github.decoppelelias.humansimulation.domain.World;
import io.github.decoppelelias.humansimulation.domain.WorldSnapshot;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** One world and the thread that owns it. Everything that touches the world runs on that thread. */
final class WorldHost {
    private static final Logger LOG = LoggerFactory.getLogger(WorldHost.class);

    static final double MIN_RATE = 0.5;
    static final double MAX_RATE = 10;
    static final double DEFAULT_RATE = 2;
    static final int MAX_STEP_DAYS = 10_000;

    record Summary(
            String id, int width, int height, long seed, int day, boolean playing, Map<String, Integer> population) {}

    record PlayState(boolean playing, double daysPerSecond) {}

    record LogView(int width, int height, long seed, List<Map<String, Object>> commands) {}

    private final String id;
    private final World world;
    private final ScheduledExecutorService thread;
    private final List<CensusRow> history = new ArrayList<>();
    private final Set<Viewer> viewers = new CopyOnWriteArraySet<>();
    private final AtomicBoolean stepping = new AtomicBoolean();
    private volatile WorldSnapshot latest;
    private volatile boolean playing;
    private volatile double daysPerSecond = DEFAULT_RATE;
    private final AtomicBoolean closing = new AtomicBoolean();
    private Optional<ScheduledFuture<?>> timer = Optional.empty();
    private boolean resetPending;
    private boolean closed;

    WorldHost(String id, int width, int height, long seed, List<Species> species) {
        this.id = id;
        this.world = new World(width, height, seed, species);
        this.thread = Executors.newSingleThreadScheduledExecutor(
                Thread.ofPlatform().name("world-" + id).daemon().factory());
        this.latest = world.snapshot();
    }

    String id() {
        return id;
    }

    WorldSnapshot snapshot() {
        return latest;
    }

    Summary summary() {
        WorldSnapshot snapshot = latest;
        Map<String, Integer> population = new TreeMap<>();
        snapshot.tiles()
                .forEach(
                        tile -> tile.entities().forEach(entity -> population.merge(entity.species(), 1, Integer::sum)));
        return new Summary(
                id, snapshot.width(), snapshot.height(), snapshot.seed(), snapshot.day(), playing, population);
    }

    /** Empty while playing, when the command lands the next day. */
    Optional<WorldSnapshot> submit(Command command) {
        return call(() -> {
            world.submit(command);
            if (command instanceof Command.Reset) {
                resetPending = true;
            }
            if (playing) {
                return Optional.empty();
            }
            WorldSnapshot snapshot = world.applyPending();
            clearHistoryIfReset();
            publish(new StreamEvent.Snapshot(snapshot));
            return Optional.of(snapshot);
        });
    }

    DayReport step(int days) {
        if (days < 1 || days > MAX_STEP_DAYS) {
            throw new IllegalArgumentException("step between 1 and " + MAX_STEP_DAYS + " days, got " + days);
        }
        if (!stepping.compareAndSet(false, true)) {
            throw new ApiException(409, "this world is already stepping");
        }
        try {
            return call(() -> {
                if (playing) {
                    throw new ApiException(409, "pause the world before stepping it");
                }
                DayReport report = advance();
                for (int i = 1; i < days; i++) {
                    report = advance();
                }
                return report;
            });
        } finally {
            stepping.set(false);
        }
    }

    boolean stepping() {
        return stepping.get();
    }

    PlayState play() {
        return call(() -> {
            if (!playing) {
                playing = true;
                schedule();
            }
            return state();
        });
    }

    PlayState pause() {
        return call(() -> {
            playing = false;
            cancelTimer();
            return state();
        });
    }

    PlayState rate(double newRate) {
        if (!(newRate >= MIN_RATE && newRate <= MAX_RATE)) {
            throw new IllegalArgumentException(
                    "the rate is between " + MIN_RATE + " and " + MAX_RATE + " days a second, got " + newRate);
        }
        return call(() -> {
            daysPerSecond = newRate;
            if (playing) {
                cancelTimer();
                schedule();
            }
            return state();
        });
    }

    List<CensusRow> census(int from) {
        return call(() -> history.stream().filter(row -> row.day() >= from).toList());
    }

    void resetCensus() {
        call(() -> {
            history.clear();
            viewers.forEach(Viewer::censusReset);
            return 0;
        });
    }

    LogView log() {
        return call(() -> new LogView(
                latest.width(),
                latest.height(),
                latest.seed(),
                world.log().stream()
                        .<Map<String, Object>>map(logged -> {
                            Map<String, Object> entry = new LinkedHashMap<>();
                            entry.put("day", logged.day());
                            entry.put("command", CommandJson.toJson(logged.command()));
                            return entry;
                        })
                        .toList()));
    }

    void attach(Viewer viewer) {
        call(() -> {
            viewers.add(viewer);
            viewer.offer(new StreamEvent.Snapshot(latest));
            return 0;
        });
    }

    void detach(Viewer viewer) {
        viewers.remove(viewer);
    }

    void close() {
        if (!closing.compareAndSet(false, true)) {
            return;
        }
        call(() -> {
            playing = false;
            cancelTimer();
            viewers.forEach(Viewer::close);
            closed = true;
            return 0;
        });
        // Tasks already queued still run, and find the world closed instead of waiting forever.
        thread.shutdown();
    }

    <T> T call(Callable<T> task) {
        Future<T> result;
        try {
            result = thread.submit(() -> {
                if (closed) {
                    throw gone();
                }
                return task.call();
            });
        } catch (RejectedExecutionException e) {
            throw gone();
        }
        try {
            return result.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof RuntimeException runtime) {
                throw runtime;
            }
            throw new IllegalStateException(e.getCause());
        }
    }

    private ApiException gone() {
        return new ApiException(404, "no world " + id);
    }

    private void schedule() {
        long period = Math.round(1_000_000_000 / daysPerSecond);
        timer = Optional.of(thread.scheduleAtFixedRate(this::tick, period, period, TimeUnit.NANOSECONDS));
    }

    /** A timer task that throws is silently never run again, so a failed day pauses the world instead. */
    private void tick() {
        try {
            advance();
        } catch (RuntimeException e) {
            LOG.error("World {} stopped playing after a day failed", id, e);
            playing = false;
            cancelTimer();
        }
    }

    private void cancelTimer() {
        timer.ifPresent(running -> running.cancel(false));
        timer = Optional.empty();
    }

    private DayReport advance() {
        DayReport report = world.advance();
        clearHistoryIfReset();
        history.add(report.census());
        publish(new StreamEvent.Day(report.snapshot(), report.census()));
        return report;
    }

    private void clearHistoryIfReset() {
        if (resetPending) {
            resetPending = false;
            history.clear();
            viewers.forEach(Viewer::censusReset);
        }
    }

    private void publish(StreamEvent event) {
        latest = event.snapshot();
        viewers.forEach(viewer -> viewer.offer(event));
    }

    private PlayState state() {
        return new PlayState(playing, daysPerSecond);
    }
}
