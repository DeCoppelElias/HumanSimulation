package io.github.decoppelelias.humansimulation.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class IntentTest {
    @Test
    void aMoveCoversAtLeastOneTile() {
        Direction north = new Grid(3, 3).directions().getFirst();
        assertThatThrownBy(() -> new Intent.Move(north, 0)).isInstanceOf(IllegalArgumentException.class);
    }
}
