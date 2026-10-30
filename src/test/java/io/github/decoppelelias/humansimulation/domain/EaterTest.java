package io.github.decoppelelias.humansimulation.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class EaterTest {
    @Test
    void showsWhatItTakesToEveryone() {
        Eater grazer = new Eater(Set.of(Plant.class));
        assertThat(grazer.key()).isEqualTo(Eater.class);
        assertThat(grazer.ownView()).isEqualTo(new Eater.Takes(List.of(Plant.class)));
        assertThat(grazer.seenView()).contains(new Eater.Takes(List.of(Plant.class)));
    }

    /** Given in reverse name order, so keeping the order it was handed fails every run rather than by luck. */
    @Test
    void showsItsFoodsAndOneFeaturePerFoodInNameOrder() {
        Eater eater = new Eater(new LinkedHashSet<>(List.of(Plant.class, TestSpecies.Bone.class)));
        assertThat(eater.ownView()).isEqualTo(new Eater.Takes(List.of(TestSpecies.Bone.class, Plant.class)));
        assertThat(eater.features()).containsExactly(Feature.takes(TestSpecies.Bone.class), Features.TAKES_PLANT);
    }

    @Test
    void findsOnlyTheFoodsItTakes() {
        Plant plant = new Plant(() -> 15);
        Eater grazer = new Eater(Set.of(Plant.class));
        assertThat(grazer.foodIn(List.of(new TestSpecies.Bone(), plant))).containsSame(plant);
        assertThat(grazer.foodIn(List.of(new TestSpecies.Bone()))).isEmpty();
        assertThat(new Eater(Set.of()).foodIn(List.of(plant))).isEmpty();
    }

    @Test
    void showsAPersonNothing() {
        assertThat(new Eater(Set.of(Plant.class)).shown()).isEmpty();
    }
}
