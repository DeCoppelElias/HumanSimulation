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
            for (Function<Spawn, Component> recipe : species.parts()) {
                Component part = recipe.apply(spawn);
                assertThat(Features.ALL).as(species.name()).containsAll(part.features());
                if (part.seenView().isEmpty()) {
                    assertThat(part.features())
                            .as(species.name() + " shows nothing")
                            .isEmpty();
                }
            }
        }
    }

    @Test
    void namesEachFeatureOnce() {
        assertThat(Features.ALL).doesNotHaveDuplicates();
    }
}
