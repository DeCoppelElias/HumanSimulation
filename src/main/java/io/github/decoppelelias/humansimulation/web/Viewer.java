package io.github.decoppelelias.humansimulation.web;

import io.javalin.http.sse.SseClient;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * One open event stream. The world's thread only overwrites the slot and this viewer's own thread writes to the
 * network, so a slow viewer skips days instead of slowing the world.
 */
final class Viewer {
    private final SseClient client;
    private final Duration keepAlive;
    private final AtomicReference<Optional<StreamEvent>> slot = new AtomicReference<>(Optional.empty());
    private final AtomicBoolean censusReset = new AtomicBoolean();
    private final Semaphore wake = new Semaphore(0);

    Viewer(SseClient client, Duration keepAlive) {
        this.client = client;
        this.keepAlive = keepAlive;
    }

    void offer(StreamEvent event) {
        slot.set(Optional.of(event));
        wake.release();
    }

    void censusReset() {
        censusReset.set(true);
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
                if (censusReset.getAndSet(false)) {
                    client.sendEvent("censusReset", Map.of());
                }
                slot.getAndSet(Optional.empty()).ifPresent(this::send);
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
