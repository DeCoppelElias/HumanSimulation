package io.github.decoppelelias.humansimulation.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class GrassTest {
    @Test
    void isEdibleForItsNourishment() {
        Species grass = Grass.species();
        assertThat(grass.settings().number("nourishment")).isEqualTo(15);
        assertThat(grass.parts())
                .singleElement()
                .satisfies(part -> assertThat(part.apply(new Spawn(grass.baseline(), grass)))
                        .isEqualTo(new Edible(15)));
    }

    @Test
    void hasNoBrainAndNoGenes() {
        assertThat(Grass.species().brain()).isEmpty();
        assertThat(Grass.species().geneLayout()).isEmpty();
        assertThat(Grass.species().name()).isEqualTo("grass");
    }
}
