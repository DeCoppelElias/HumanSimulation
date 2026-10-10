package io.github.decoppelelias.humansimulation.web;

import io.github.decoppelelias.humansimulation.domain.Species;
import io.github.decoppelelias.humansimulation.domain.World;
import io.github.decoppelelias.humansimulation.domain.WorldSnapshot;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/** One world and the thread that owns it. Everything that touches the world runs on that thread. */
final class WorldHost {
    record Summary(
            String id, int width, int height, long seed, int day, boolean playing, Map<String, Integer> population) {}

    private final String id;
    private final World world;
    private final ScheduledExecutorService thread;
    private volatile WorldSnapshot latest;
    private volatile boolean playing;

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

    void close() {
        thread.shutdownNow();
    }

    <T> T call(Callable<T> task) {
        try {
            return thread.submit(task).get();
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
}
