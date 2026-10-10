package io.github.decoppelelias.humansimulation.web;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;

/** Calls back once the server has had no request and no open stream for a whole limit. A zero limit never does. */
public final class IdleWatch {
    private final Duration limit;
    private final LongSupplier nanos;
    private final Runnable onIdle;
    private final AtomicLong lastActivity;
    private final AtomicInteger openStreams = new AtomicInteger();

    public IdleWatch(Duration limit, LongSupplier nanos, Runnable onIdle) {
        this.limit = limit;
        this.nanos = nanos;
        this.onIdle = onIdle;
        this.lastActivity = new AtomicLong(nanos.getAsLong());
    }

    public void touch() {
        lastActivity.set(nanos.getAsLong());
    }

    void streamOpened() {
        openStreams.incrementAndGet();
        touch();
    }

    void streamClosed() {
        openStreams.decrementAndGet();
        touch();
    }

    public void check() {
        if (limit.isZero() || openStreams.get() > 0) {
            return;
        }
        if (nanos.getAsLong() - lastActivity.get() >= limit.toNanos()) {
            touch();
            onIdle.run();
        }
    }
}
