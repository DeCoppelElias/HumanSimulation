package io.github.decoppelelias.humansimulation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;

class SettingsTest {
    private static final Settings SETTINGS = new Settings(List.of(
            new Setting.Scalar("stride", "Farthest walk in a day.", 1, 5, 1, 3),
            new Setting.Scalar("share", "A fraction.", 0, 1, 0.05, 0.25)));

    @Test
    void readsDefaults() {
        assertThat(SETTINGS.whole("stride")).isEqualTo(3);
        assertThat(SETTINGS.number("share")).isEqualTo(0.25);
    }

    @Test
    void aFractionalSettingIsNotWhole() {
        assertThatThrownBy(() -> SETTINGS.whole("share"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("share");
    }

    @Test
    void anUnknownKeyThrows() {
        assertThatThrownBy(() -> SETTINGS.number("speed"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("speed");
        assertThat(SETTINGS.declares("speed")).isFalse();
        assertThat(SETTINGS.declares("stride")).isTrue();
    }

    @Test
    void twoSettingsCannotShareAKey() {
        Setting.Scalar one = new Setting.Scalar("stride", "One.", 1, 5, 1, 3);
        assertThatThrownBy(() -> new Settings(List.of(one, one)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("stride");
    }
}
