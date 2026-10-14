package io.github.decoppelelias.humansimulation.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class FoodDropTest {
    @Test
    void namesItsSettingsFromItsSpecies() {
        FoodDrop drop = new FoodDrop("grass");
        assertThat(drop.settings().whole("grassPerDrop")).isEqualTo(7);
        assertThat(drop.settings().whole("grassDropInterval")).isEqualTo(7);
    }

    @Test
    void dropsOnTheDaysCompletingAMultipleOfItsInterval() {
        FoodDrop drop = new FoodDrop("grass");
        assertThat(drop.dropsOn(1)).isZero();
        assertThat(drop.dropsOn(6)).isZero();
        assertThat(drop.dropsOn(7)).isEqualTo(7);
        assertThat(drop.dropsOn(14)).isEqualTo(7);
    }
}
