package io.github.decoppelelias.humansimulation.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class EdibleTest {
    @Test
    void showsItsAmountToEveryone() {
        Edible grass = new Edible(() -> 15);
        assertThat(grass.ownView()).isEqualTo(new Edible.Amount(15));
        assertThat(grass.seenView()).contains(new Edible.Amount(15));
        assertThat(grass.key()).isEqualTo(Edible.class);
    }

    @Test
    void readsItsAmountLive() {
        SettingValues values = new SettingValues(
                new Settings(List.of(new Setting.Scalar(Grass.NOURISHMENT, "Reserve from one grass.", 1, 100, 1, 15))));
        Edible grass = new Edible(() -> values.number(Grass.NOURISHMENT));
        values.set(Grass.NOURISHMENT, 20);
        assertThat(grass.amount()).isEqualTo(20);
    }
}
