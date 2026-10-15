package io.github.decoppelelias.humansimulation.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class GrassTest {
    @Test
    void isEdibleForItsNourishment() {
        Species grass = Grass.species();
        SettingValues values = new SettingValues(grass.settings());
        assertThat(values.number("nourishment")).isEqualTo(15);
        assertThat(grass.parts()).singleElement().satisfies(part -> {
            Edible edible = (Edible) part.apply(new Spawn(grass.baseline(), grass, values));
            assertThat(edible.amount()).isEqualTo(15);
            values.set("nourishment", 20);
            assertThat(edible.amount()).isEqualTo(20);
        });
    }

    @Test
    void hasNoBrainAndNoGenes() {
        assertThat(Grass.species().brain()).isEmpty();
        assertThat(Grass.species().geneLayout()).isEmpty();
        assertThat(Grass.species().name()).isEqualTo("grass");
    }
}
