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
    void scalarFindsADeclaration() {
        assertThat(SETTINGS.scalar("stride").defaultValue()).isEqualTo(3);
        assertThatThrownBy(() -> SETTINGS.scalar("speed"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("speed");
    }

    @Test
    void declaresOnlyItsOwnKeys() {
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
