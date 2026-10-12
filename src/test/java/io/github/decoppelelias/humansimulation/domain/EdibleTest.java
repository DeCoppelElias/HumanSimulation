package io.github.decoppelelias.humansimulation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class EdibleTest {
    @Test
    void showsItsAmountToEveryone() {
        Edible grass = new Edible(15);
        assertThat(grass.ownView()).isEqualTo(new Edible.Amount(15));
        assertThat(grass.seenView()).contains(new Edible.Amount(15));
        assertThat(grass.key()).isEqualTo(Edible.class);
    }

    @Test
    void anAmountIsPositive() {
        assertThatThrownBy(() -> new Edible(0)).isInstanceOf(IllegalArgumentException.class);
    }
}
