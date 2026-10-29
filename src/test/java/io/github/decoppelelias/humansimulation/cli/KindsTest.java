package io.github.decoppelelias.humansimulation.cli;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.decoppelelias.humansimulation.domain.Species;
import org.junit.jupiter.api.Test;

class KindsTest {
    @Test
    void runAndServeKnowGrassRabbitsAndWolves() {
        assertThat(Kinds.all()).extracting(Species::name).containsExactly("grass", "rabbit", "wolf");
    }
}
