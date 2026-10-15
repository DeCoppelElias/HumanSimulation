package io.github.decoppelelias.humansimulation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class SettingTest {
    private static Setting.Scalar interval() {
        return new Setting.Scalar("eatingInterval", "Days between two charges.", 1, 30, 1, 1);
    }

    @Test
    void aDefaultOffItsBoundsOrStepIsRejected() {
        assertThatThrownBy(() -> new Setting.Scalar("x", "An x.", 1, 30, 1, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("x");
        assertThatThrownBy(() -> new Setting.Scalar("x", "An x.", 1, 30, 1, 2.5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("step");
    }

    @Test
    void aDeclarationNeedsAKeyADescriptionARangeAndAPositiveStep() {
        assertThatThrownBy(() -> new Setting.Scalar(" ", "An x.", 1, 2, 1, 1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Setting.Scalar("x", " ", 1, 2, 1, 1)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Setting.Scalar("x", "An x.", 2, 1, 1, 1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Setting.Scalar("x", "An x.", 1, 2, 0, 1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aMaxOffItsStepOrTooManyStepsAreRejected() {
        assertThatThrownBy(() -> new Setting.Scalar("x", "An x.", 0, 1.05, 0.1, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("step");
        assertThatThrownBy(() -> new Setting.Scalar("x", "An x.", 0, 1e7, 1, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("steps");
    }

    @Test
    void valuesAndTheDefaultSitExactlyOnTheStepFromAFractionalMin() {
        assertThat(new Setting.Scalar("x", "An x.", 0, 1, 0.1, 0.1 + 0.2).defaultValue())
                .isEqualTo(0.3);
        Setting.Scalar fromAFraction = new Setting.Scalar("y", "A y.", 0.05, 1.05, 0.1, 0.35);
        assertThat(fromAFraction.onStep(0.65 + 1e-12)).isEqualTo(0.65);
    }

    @Test
    void aZeroEatingIntervalIsRejected() {
        assertThatThrownBy(() -> interval().check(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("eatingInterval");
        assertThatCode(() -> interval().check(15)).doesNotThrowAnyException();
    }
}
