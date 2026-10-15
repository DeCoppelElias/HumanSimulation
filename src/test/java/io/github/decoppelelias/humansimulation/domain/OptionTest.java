package io.github.decoppelelias.humansimulation.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class OptionTest {
    @Test
    void eachOptionReturnsTheIntentOfItsName() {
        assertThat(new Option.Move(TestSpecies.EAST, 2, 2, 0).intent()).isEqualTo(new Intent.Move(TestSpecies.EAST, 2));
        assertThat(new Option.Idle().intent()).isEqualTo(new Intent.Idle());
        assertThat(new Option.Breed().intent()).isEqualTo(new Intent.Breed());
    }
}
