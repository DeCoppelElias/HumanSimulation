package io.github.decoppelelias.humansimulation.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PlantTest {
    @Test
    void showsItsAmountToEveryoneUnderItsOwnKey() {
        Plant grass = new Plant(() -> 15);
        assertThat(grass).isInstanceOf(Edible.class);
        assertThat(grass.ownView()).isEqualTo(new Edible.Amount(15));
        assertThat(grass.seenView()).contains(new Edible.Amount(15));
        assertThat(grass.key()).isEqualTo(Plant.class);
    }

    @Test
    void showsItsFeature() {
        assertThat(new Plant(() -> 15).features()).containsExactly(Features.PLANT);
    }

    @Test
    void readsItsAmountLive() {
        SettingValues values = new SettingValues(new Settings(
                List.of(new Setting.Scalar(Edible.NOURISHMENT, "Reserve from one grass.", 1, 100, 1, 15))));
        Plant grass = new Plant(() -> values.number(Edible.NOURISHMENT));
        values.set(Edible.NOURISHMENT, 20);
        assertThat(grass.amount()).isEqualTo(20);
    }

    @Test
    void showsItsAmountToAPerson() {
        assertThat(new Plant(() -> 15).shown()).isEqualTo(Map.of("amount", 15.0));
    }
}
