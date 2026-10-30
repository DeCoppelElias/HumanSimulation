package io.github.decoppelelias.humansimulation.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import org.junit.jupiter.api.Test;

class LifespanTest {
    private static SettingValues lasting(int days) {
        return new SettingValues(
                new Settings(List.of(new Setting.Scalar(Lifespan.LIFESPAN, "Days it lasts.", 1, 100, 1, days))));
    }

    @Test
    void endsItsCarrierOnTheTickItsAgeReachesTheLifespan() {
        Lifespan lifespan = new Lifespan(lasting(3));
        assertThat(lifespan.ownView()).isEqualTo(new Lifespan.Own(0, OptionalInt.of(2)));
        assertThat(lifespan.tick()).isEmpty();
        assertThat(lifespan.tick()).isEmpty();
        assertThat(lifespan.ownView()).isEqualTo(new Lifespan.Own(2, OptionalInt.of(0)));
        assertThat(lifespan.tick()).contains(Lifespan.AGED);
    }

    @Test
    void withNoLengthItCountsAndNeverEnds() {
        Lifespan lifespan = new Lifespan(new SettingValues(new Settings(List.of())));
        for (int day = 0; day < 100; day++) {
            assertThat(lifespan.tick()).isEmpty();
        }
        assertThat(lifespan.ownView()).isEqualTo(new Lifespan.Own(100, OptionalInt.empty()));
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
        assertThat(new Lifespan(lasting(3)).features()).isEmpty();
    }

    @Test
    void showsItsAgeAndDaysToLiveOnlyWithALength() {
        Lifespan grass = new Lifespan(lasting(5));
        grass.tick();
        assertThat(grass.shown()).isEqualTo(Map.of("age", 1.0, "daysToLive", 3.0));
        Lifespan rabbit = new Lifespan(new SettingValues(new Settings(List.of())));
        rabbit.tick();
        assertThat(rabbit.shown()).isEqualTo(Map.of("age", 1.0));
    }
}
