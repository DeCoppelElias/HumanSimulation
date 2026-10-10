package io.github.decoppelelias.humansimulation.web;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.decoppelelias.humansimulation.domain.Rabbit;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class WorldsTest {
    @Test
    void pauseAllStopsEveryPlayingWorldAndKeepsThem() {
        Worlds worlds = new Worlds(3, List.of(Rabbit.species()));
        WorldHost first = worlds.create(5, 5, 1);
        WorldHost second = worlds.create(5, 5, 2);
        first.play();
        second.play();

        worlds.pauseAll();

        assertThat(worlds.all()).containsExactly(first, second);
        assertThat(worlds.all()).extracting(host -> host.summary().playing()).containsOnly(false);
        worlds.closeAll();
    }

    @Test
    void pauseAllPassesOverAWorldClosedUnderIt() {
        Worlds worlds = new Worlds(3, List.of(Rabbit.species()));
        WorldHost closed = worlds.create(5, 5, 1);
        WorldHost open = worlds.create(5, 5, 2);
        open.play();
        closed.close();

        worlds.pauseAll();

        assertThat(open.summary().playing()).isFalse();
        worlds.closeAll();
    }

    @Test
    void deletingABusyWorldDoesNotHoldUpTheOthers() throws Exception {
        Worlds worlds = new Worlds(3, List.of(Rabbit.species()));
        WorldHost busy = worlds.create(5, 5, 1);
        WorldHost other = worlds.create(5, 5, 2);
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        ExecutorService threads = Executors.newVirtualThreadPerTaskExecutor();
        CompletableFuture<Integer> work = CompletableFuture.supplyAsync(
                () -> busy.call(() -> {
                    started.countDown();
                    release.await();
                    return 0;
                }),
                threads);
        started.await();
        CompletableFuture<Void> deleting = CompletableFuture.runAsync(() -> worlds.delete(busy.id()), threads);
        CompletableFuture.runAsync(
                        () -> {
                            while (worlds.all().contains(busy)) {
                                Thread.onSpinWait();
                            }
                        },
                        threads)
                .get(2, TimeUnit.SECONDS);

        assertThat(CompletableFuture.supplyAsync(() -> worlds.get(other.id()), threads)
                        .get(2, TimeUnit.SECONDS))
                .isSameAs(other);

        release.countDown();
        work.get(2, TimeUnit.SECONDS);
        deleting.get(2, TimeUnit.SECONDS);
        worlds.closeAll();
        threads.close();
    }
}
