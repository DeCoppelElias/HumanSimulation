package io.github.decoppelelias.humansimulation.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class GrassTest {
    @Test
    void carriesAPlantForItsNourishment() {
        Species grass = Grass.species();
        SettingValues values = new SettingValues(grass.settings());
        assertThat(values.number("nourishment")).isEqualTo(15);
        Component part =
                grass.parts().getFirst().apply(new Spawn(grass.baseline(), grass, values, 0, Optional.empty()));
        assertThat(part).isInstanceOf(Plant.class);
        Plant plant = (Plant) part;
        assertThat(plant.amount()).isEqualTo(15);
        values.set("nourishment", 20);
        assertThat(plant.amount()).isEqualTo(20);
    }

    @Test
    void declaresATwelveDayLifespan() {
        Species grass = Grass.species();
        SettingValues values = new SettingValues(grass.settings());
        assertThat(values.whole("lifespan")).isEqualTo(12);
        assertThat(grass.parts()).hasSize(2);
        assertThat(grass.parts().get(1).apply(new Spawn(grass.baseline(), grass, values, 0, Optional.empty())))
                .isInstanceOf(Lifespan.class);
    }

    @Test
    void hasNoBrainAndNoGenes() {
        assertThat(Grass.species().brain()).isEmpty();
        assertThat(Grass.species().geneLayout()).isEmpty();
        assertThat(Grass.species().name()).isEqualTo("grass");
    }
}
