package io.github.decoppelelias.humansimulation.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class FoodDropTest {
    private static final FoodDrop DROP = new FoodDrop("grass");

    @Test
    void namesItsSettingsFromItsSpecies() {
        SettingValues values = new SettingValues(DROP.settings());
        assertThat(values.whole("grassPerDrop")).isEqualTo(7);
        assertThat(values.whole("grassDropInterval")).isEqualTo(7);
    }

    @Test
    void dropsOnTheDaysCompletingAMultipleOfItsInterval() {
        SettingValues values = new SettingValues(DROP.settings());
        assertThat(DROP.dropsOn(1, values)).isZero();
        assertThat(DROP.dropsOn(6, values)).isZero();
        assertThat(DROP.dropsOn(7, values)).isEqualTo(7);
        assertThat(DROP.dropsOn(14, values)).isEqualTo(7);
    }

    @Test
    void readsItsValuesLive() {
        SettingValues values = new SettingValues(DROP.settings());
        values.set("grassDropInterval", 2);
        values.set("grassPerDrop", 3);
        assertThat(DROP.dropsOn(2, values)).isEqualTo(3);
    }
}
