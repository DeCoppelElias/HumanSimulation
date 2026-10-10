package io.github.decoppelelias.humansimulation.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

class IdleWatchTest {
    private final AtomicLong now = new AtomicLong();
    private final AtomicInteger fired = new AtomicInteger();
    private final IdleWatch watch = new IdleWatch(Duration.ofMinutes(10), now::get, fired::incrementAndGet);

    private void minutes(long minutes) {
        now.addAndGet(Duration.ofMinutes(minutes).toNanos());
    }

    @Test
    void firesOnceTheLimitPassesWithNoActivity() {
        minutes(9);
        watch.check();
        assertThat(fired).hasValue(0);
        minutes(1);
        watch.check();
        assertThat(fired).hasValue(1);
    }

    @Test
    void aRequestStartsTheWaitAgain() {
        minutes(9);
        watch.touch();
        minutes(9);
        watch.check();
        assertThat(fired).hasValue(0);
    }

    @Test
    void anOpenStreamIsActivity() {
        watch.streamOpened();
        minutes(60);
        watch.check();
        assertThat(fired).hasValue(0);
        watch.streamClosed();
        minutes(10);
        watch.check();
        assertThat(fired).hasValue(1);
    }

    @Test
    void firesOncePerQuietPeriod() {
        minutes(10);
        watch.check();
        watch.check();
        assertThat(fired).hasValue(1);
    }

    @Test
    void aZeroLimitNeverFires() {
        IdleWatch never = new IdleWatch(Duration.ZERO, now::get, fired::incrementAndGet);
        minutes(100_000);
        never.check();
        assertThat(fired).hasValue(0);
    }
}
