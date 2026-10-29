package io.github.decoppelelias.humansimulation.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

class FeaturesTest {
    @Test
    void everyFeatureASpeciesShowsIsInTheCatalog() {
        for (Species species : List.of(Grass.species(), Rabbit.species())) {
            Spawn spawn =
                    new Spawn(species.baseline(), species, new SettingValues(species.settings()), 0, Optional.empty());
            for (Function<Spawn, Component> part : species.parts()) {
                assertThat(Features.ALL)
                        .as(species.name())
                        .containsAll(part.apply(spawn).features());
            }
        }
    }

    @Test
    void namesEachFeatureOnce() {
        assertThat(Features.ALL).doesNotHaveDuplicates();
    }
}
