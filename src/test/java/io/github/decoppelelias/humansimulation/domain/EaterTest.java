package io.github.decoppelelias.humansimulation.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class EaterTest {
    @Test
    void showsItsFoodsToItsCarrierOnly() {
        Eater grazer = new Eater(Set.of(Plant.class));
        assertThat(grazer.key()).isEqualTo(Eater.class);
        assertThat(grazer.ownView()).isEqualTo(new Eater.Takes(Set.of(Plant.class)));
        assertThat(grazer.seenView()).isEmpty();
    }

    @Test
    void findsOnlyTheFoodsItTakes() {
        Plant plant = new Plant(() -> 15);
        Eater grazer = new Eater(Set.of(Plant.class));
        assertThat(grazer.foodIn(List.of(new TestSpecies.Bone(), plant))).containsSame(plant);
        assertThat(grazer.foodIn(List.of(new TestSpecies.Bone()))).isEmpty();
        assertThat(new Eater(Set.of()).foodIn(List.of(plant))).isEmpty();
    }
}
