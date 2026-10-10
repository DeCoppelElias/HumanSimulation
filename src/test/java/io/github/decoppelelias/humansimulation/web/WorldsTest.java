package io.github.decoppelelias.humansimulation.web;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.decoppelelias.humansimulation.domain.Rabbit;
import java.util.List;
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
}
