package io.github.decoppelelias.humansimulation.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class MetabolismTest {
    private static Settings settings(int interval, int charge, int reserve) {
        return new Settings(List.of(
                new Setting.Scalar(Metabolism.EATING_INTERVAL, "Days between charges.", 1, 30, 1, interval),
                new Setting.Scalar(Metabolism.EATING_CHARGE, "Paid at each charge.", 1, 100, 1, charge),
                new Setting.Scalar(Metabolism.STARTING_RESERVE, "Reserve at spawn.", 0, 100, 1, reserve)));
    }

    @Test
    void startsFromTheStartingReserve() {
        assertThat(new Metabolism(settings(1, 1, 15)).ownView()).isEqualTo(new Metabolism.Own(15, 15));
    }

    @Test
    void chargesOnceItsOwnCountReachesTheInterval() {
        Metabolism metabolism = new Metabolism(settings(3, 2, 10));
        metabolism.tick();
        metabolism.tick();
        assertThat(metabolism.ownView()).isEqualTo(new Metabolism.Own(10, 15));
        metabolism.tick();
        assertThat(metabolism.ownView()).isEqualTo(new Metabolism.Own(8, 14));
    }

    @Test
    void survivesAtZeroAndNotBelow() {
        Metabolism metabolism = new Metabolism(settings(1, 1, 1));
        assertThat(metabolism.tick()).isTrue();
        assertThat(metabolism.ownView()).isEqualTo(new Metabolism.Own(0, 0));
        assertThat(metabolism.tick()).isFalse();
    }

    @Test
    void eatingCreditsTheReserve() {
        Metabolism metabolism = new Metabolism(settings(1, 1, 0));
        metabolism.credit(15);
        assertThat(metabolism.ownView()).isEqualTo(new Metabolism.Own(15, 15));
    }

    @Test
    void othersSeeNothing() {
        assertThat(new Metabolism(settings(1, 1, 15)).seenView()).isEmpty();
    }
}
