package io.github.decoppelelias.humansimulation.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class LifespanTest {
    private static SettingValues lasting(int days) {
        return new SettingValues(
                new Settings(List.of(new Setting.Scalar(Lifespan.LIFESPAN, "Days it lasts.", 1, 100, 1, days))));
    }

    @Test
    void endsItsCarrierOnTheTickItsAgeReachesTheLifespan() {
        Lifespan lifespan = new Lifespan(lasting(3));
        assertThat(lifespan.ownView()).isEqualTo(new Lifespan.Own(0, 2));
        assertThat(lifespan.tick()).isEmpty();
        assertThat(lifespan.tick()).isEmpty();
        assertThat(lifespan.ownView()).isEqualTo(new Lifespan.Own(2, 0));
        assertThat(lifespan.tick()).contains(Lifespan.AGED);
    }

    @Test
    void readsItsLengthLive() {
        SettingValues values = lasting(10);
        Lifespan lifespan = new Lifespan(values);
        lifespan.tick();
        lifespan.tick();
        values.set(Lifespan.LIFESPAN, 3);
        assertThat(lifespan.tick()).contains(Lifespan.AGED);
    }

    @Test
    void othersSeeNothing() {
        assertThat(new Lifespan(lasting(3)).seenView()).isEmpty();
    }
}
