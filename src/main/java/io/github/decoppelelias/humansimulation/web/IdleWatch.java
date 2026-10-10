package io.github.decoppelelias.humansimulation.web;

import java.time.Duration;
import java.util.function.LongSupplier;

public final class IdleWatch {
    public IdleWatch(Duration limit, LongSupplier nanos, Runnable onIdle) {}

    public void touch() {}

    void streamOpened() {}

    void streamClosed() {}
}
