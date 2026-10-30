package io.github.decoppelelias.humansimulation.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class FertilityTest {
    @Test
    void theWindowOpensOnceTheIntervalHasPassedAndStaysOpen() {
        Fertility fertility = new Fertility(3);
        assertThat(fertility.open(17, 15)).isFalse();
        assertThat(fertility.open(18, 15)).isTrue();
        assertThat(fertility.open(60, 15)).isTrue();
    }

    @Test
    void breedingRestartsTheWindow() {
        Fertility fertility = new Fertility(0);
        fertility.bred(20);
        assertThat(fertility.open(34, 15)).isFalse();
        assertThat(fertility.open(35, 15)).isTrue();
    }

    @Test
    void showsItsDayToItsCarrierOnly() {
        assertThat(new Fertility(4).ownView()).isEqualTo(new Fertility.Since(4));
        assertThat(new Fertility(4).seenView()).isEmpty();
        assertThat(new Fertility(4).features()).isEmpty();
    }

    @Test
    void showsAPersonNothing() {
        assertThat(new Fertility(4).shown()).isEmpty();
    }
}
