package io.github.decoppelelias.humansimulation.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.decoppelelias.humansimulation.domain.Rabbit;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

class WorldHostTest {
    private static WorldHost host() {
        return new WorldHost("w", 5, 5, 1, List.of(Rabbit.species()));
    }

    @Test
    @Timeout(10)
    void oneStepAtATime() throws Exception {
        WorldHost host = host();
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        ExecutorService threads = Executors.newVirtualThreadPerTaskExecutor();
        CompletableFuture<Integer> blocker = CompletableFuture.supplyAsync(
                () -> host.call(() -> {
                    started.countDown();
                    release.await();
                    return 0;
                }),
                threads);
        started.await();
        CompletableFuture<Integer> first =
                CompletableFuture.supplyAsync(() -> host.step(1).census().day(), threads);
        while (!host.stepping()) {
            Thread.onSpinWait();
        }
        assertThatThrownBy(() -> host.step(1))
                .isInstanceOfSatisfying(
                        ApiException.class, e -> assertThat(e.status()).isEqualTo(409));
        release.countDown();
        assertThat(blocker.get()).isZero();
        assertThat(first.get()).isEqualTo(1);
        host.close();
        threads.close();
    }

    @Test
    void aClosedWorldAnswers404() {
        WorldHost host = host();
        host.close();
        assertThatThrownBy(() -> host.step(1))
                .isInstanceOfSatisfying(
                        ApiException.class, e -> assertThat(e.status()).isEqualTo(404));
        assertThatThrownBy(() -> host.census(1))
                .isInstanceOfSatisfying(
                        ApiException.class, e -> assertThat(e.status()).isEqualTo(404));
    }

    @Test
    void stepTakesOneToTenThousandDays() {
        WorldHost host = host();
        assertThatThrownBy(() -> host.step(0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> host.step(10_001)).isInstanceOf(IllegalArgumentException.class);
        host.close();
    }
}
