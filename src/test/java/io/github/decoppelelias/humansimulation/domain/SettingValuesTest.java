package io.github.decoppelelias.humansimulation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SettingValuesTest {
    private static SettingValues values() {
        return new SettingValues(new Settings(List.of(
                new Setting.Scalar("stride", "Farthest walk in a day.", 1, 5, 1, 3),
                new Setting.Scalar("share", "A fraction.", 0, 1, 0.05, 0.25))));
    }

    @Test
    void startsAtTheDefaults() {
        SettingValues values = values();
        assertThat(values.whole("stride")).isEqualTo(3);
        assertThat(values.number("share")).isEqualTo(0.25);
        assertThat(values.current()).containsExactly(Map.entry("share", 0.25), Map.entry("stride", 3.0));
    }

    @Test
    void anEditIsReadLive() {
        SettingValues values = values();
        values.set("stride", 5);
        assertThat(values.whole("stride")).isEqualTo(5);
    }

    @Test
    void aValueOffItsBoundsOrStepIsRefusedAndKeepsTheOldOne() {
        SettingValues values = values();
        assertThatThrownBy(() -> values.set("stride", 6))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("stride");
        assertThatThrownBy(() -> values.set("share", 0.33)).isInstanceOf(IllegalArgumentException.class);
        assertThat(values.whole("stride")).isEqualTo(3);
        assertThat(values.number("share")).isEqualTo(0.25);
    }

    @Test
    void restoringPutsTheDefaultBack() {
        SettingValues values = values();
        values.set("stride", 1);
        values.set("share", 0.5);
        values.restore("stride");
        assertThat(values.whole("stride")).isEqualTo(3);
        assertThat(values.number("share")).isEqualTo(0.5);
        values.restoreAll();
        assertThat(values.number("share")).isEqualTo(0.25);
    }

    @Test
    void aFractionalSettingIsNotWhole() {
        assertThatThrownBy(() -> values().whole("share"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("share");
    }

    @Test
    void anUnknownKeyThrows() {
        assertThatThrownBy(() -> values().number("speed"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("speed");
        assertThat(values().declares("speed")).isFalse();
        assertThat(values().declares("stride")).isTrue();
    }
}
