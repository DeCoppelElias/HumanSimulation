package io.github.decoppelelias.humansimulation.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MeatTest {
    @Test
    void isFoodWorthItsAmountReadLiveAndShowsItself() {
        double[] amount = {30};
        Meat meat = new Meat(() -> amount[0]);
        amount[0] = 20;
        assertThat(meat.key()).isEqualTo(Meat.class);
        assertThat(meat.amount()).isEqualTo(20);
        assertThat(meat.seenView()).contains(new Edible.Amount(20));
        assertThat(meat.features()).containsExactly(Features.MEAT);
    }
}
