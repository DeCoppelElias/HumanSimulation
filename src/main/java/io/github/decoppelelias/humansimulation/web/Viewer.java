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
    /** What is waiting to be sent, swapped as one value so a reset and the event after it never come apart. */
    private record Pending(boolean censusReset, Optional<StreamEvent> event) {}

    private static final Pending NOTHING = new Pending(false, Optional.empty());

    private final SseClient client;
    private final Duration keepAlive;
    private final AtomicReference<Pending> slot = new AtomicReference<>(NOTHING);
    private final Semaphore wake = new Semaphore(0);

    Viewer(SseClient client, Duration keepAlive) {
        this.client = client;
        this.keepAlive = keepAlive;
    }

    void offer(StreamEvent event) {
        slot.updateAndGet(pending -> new Pending(pending.censusReset(), Optional.of(event)));
        wake.release();
    }

    /** Drops any event still waiting, since it belongs to the history the reset cleared. */
    void censusReset() {
        slot.set(new Pending(true, Optional.empty()));
        wake.release();
    }

    void close() {
        client.close();
        wake.release();
    }

    void run() {
        try {
            while (!client.terminated()) {
                boolean woken = wake.tryAcquire(keepAlive.toMillis(), TimeUnit.MILLISECONDS);
                wake.drainPermits();
                if (!woken) {
                    client.sendComment("keepalive");
                    continue;
                }
                Pending pending = slot.getAndSet(NOTHING);
                if (pending.censusReset()) {
                    client.sendEvent("censusReset", Map.of());
                }
                pending.event().ifPresent(this::send);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void send(StreamEvent event) {
        switch (event) {
            case StreamEvent.Day day -> client.sendEvent("day", day);
            case StreamEvent.Snapshot snapshot -> client.sendEvent("snapshot", snapshot);
        }
    }
}
