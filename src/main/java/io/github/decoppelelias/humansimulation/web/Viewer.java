package io.github.decoppelelias.humansimulation.web;

import io.javalin.http.sse.SseClient;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * One open event stream. The world's thread only overwrites the slot and this viewer's own thread writes to the
 * network, so a slow viewer skips days instead of slowing the world.
 */
final class Viewer {
    /** Where a viewer writes. Javalin's stream in the server, a recorder in a test. */
    interface Sink {
        void sendEvent(String name, Object data);

        void sendComment(String comment);

        boolean terminated();

        void close();

        static Sink of(SseClient client) {
            return new Sink() {
                @Override
                public void sendEvent(String name, Object data) {
                    client.sendEvent(name, data);
                }

                @Override
                public void sendComment(String comment) {
                    client.sendComment(comment);
                }

                @Override
                public boolean terminated() {
                    return client.terminated();
                }

                @Override
                public void close() {
                    client.close();
                }
            };
        }
    }

    /** What is waiting to be sent, swapped as one value so a reset and the event after it never come apart. */
    private record Pending(boolean censusReset, Optional<WorldHost.PlayState> playState, Optional<StreamEvent> event) {}

    private static final Pending NOTHING = new Pending(false, Optional.empty(), Optional.empty());

    private final Sink sink;
    private final Duration keepAlive;
    private final AtomicReference<Pending> slot = new AtomicReference<>(NOTHING);
    private final Semaphore wake = new Semaphore(0);

    Viewer(Sink sink, Duration keepAlive) {
        this.sink = sink;
        this.keepAlive = keepAlive;
    }

    void offer(StreamEvent event) {
        slot.updateAndGet(pending -> new Pending(pending.censusReset(), pending.playState(), Optional.of(event)));
        wake.release();
    }

    /** Held apart from the latest day, so a burst of days never overwrites it. */
    void playState(WorldHost.PlayState state) {
        slot.updateAndGet(pending -> new Pending(pending.censusReset(), Optional.of(state), pending.event()));
        wake.release();
    }

    /** Drops any event still waiting, since it belongs to the history the reset cleared. */
    void censusReset() {
        slot.updateAndGet(pending -> new Pending(true, pending.playState(), Optional.empty()));
        wake.release();
    }

    void close() {
        sink.close();
        wake.release();
    }

    void run() {
        try {
            while (!sink.terminated()) {
                boolean woken = wake.tryAcquire(keepAlive.toMillis(), TimeUnit.MILLISECONDS);
                wake.drainPermits();
                if (!woken) {
                    sink.sendComment("keepalive");
                    continue;
                }
                Pending pending = slot.getAndSet(NOTHING);
                if (pending.censusReset()) {
                    sink.sendEvent("censusReset", Map.of());
                }
                pending.playState().ifPresent(state -> sink.sendEvent("playState", state));
                pending.event().ifPresent(this::send);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void send(StreamEvent event) {
        switch (event) {
            case StreamEvent.Day day -> sink.sendEvent("day", day);
            case StreamEvent.Snapshot snapshot -> sink.sendEvent("snapshot", snapshot);
        }
    }
}
