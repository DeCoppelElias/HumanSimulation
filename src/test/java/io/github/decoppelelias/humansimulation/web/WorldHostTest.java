package io.github.decoppelelias.humansimulation.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.decoppelelias.humansimulation.domain.Rabbit;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import org.junit.jupiter.api.Test;

class WorldHostTest {
    private static WorldHost host() {
        return new WorldHost("w", 5, 5, 1, List.of(Rabbit.species()));
    }

    @Test
    void oneStepAtATime() throws Exception {
        WorldHost host = host();
        CountDownLatch release = new CountDownLatch(1);
        CompletableFuture<Integer> blocker = CompletableFuture.supplyAsync(() -> host.call(() -> {
            release.await();
            return 0;
        }));
        CompletableFuture<Integer> first =
                CompletableFuture.supplyAsync(() -> host.step(1).census().day());
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
    }

    @Test
    void stepTakesOneToTenThousandDays() {
        WorldHost host = host();
        assertThatThrownBy(() -> host.step(0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> host.step(10_001)).isInstanceOf(IllegalArgumentException.class);
        host.close();
    }
}
