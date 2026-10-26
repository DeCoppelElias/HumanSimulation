package io.github.decoppelelias.humansimulation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class MetabolismTest {
    private static final Genome NO_GENES = new Genome(List.of(), Map.of());

    private static Settings settings(int interval, int charge, int reserve) {
        return new Settings(List.of(
                new Setting.Scalar(Metabolism.EATING_INTERVAL, "Days between charges.", 1, 30, 1, interval),
                new Setting.Scalar(Metabolism.EATING_CHARGE, "Paid at each charge.", 1, 100, 1, charge),
                new Setting.Scalar(Metabolism.STARTING_RESERVE, "Reserve at spawn.", 0, 100, 1, reserve)));
    }

    private static SettingValues values(int interval, int charge, int reserve) {
        return new SettingValues(settings(interval, charge, reserve));
    }

    private static Metabolism metabolism(int interval, int charge, double reserve) {
        return new Metabolism(values(interval, charge, 0), NO_GENES, reserve);
    }

    private static SettingValues withCharges() {
        List<Setting> declared = new ArrayList<>(settings(1, 1, 0).declared());
        declared.add(new Setting.Scalar(Metabolism.SPEED_CHARGE, "Per unit of speed.", 0, 2, 0.01, 0.1));
        declared.add(new Setting.Scalar(Metabolism.VIEW_CHARGE, "Per tile of view.", 0, 1, 0.01, 0.03));
        return new SettingValues(new Settings(declared));
    }

    private static Genome genes(double speed, double viewRange) {
        List<GeneSpec> layout =
                List.of(new GeneSpec.Scalar("viewRange", 1, 10, 1.0), new GeneSpec.Scalar("speed", 0, 1, 0.05));
        return new Genome(layout, Map.of("viewRange", viewRange, "speed", speed));
    }

    @Test
    void readsItsValuesLive() {
        SettingValues values = values(1, 1, 0);
        Metabolism metabolism = new Metabolism(values, NO_GENES, 10);
        values.set(Metabolism.EATING_CHARGE, 3);
        metabolism.tick();
        assertThat(metabolism.ownView()).isEqualTo(new Metabolism.Own(7, 2));
    }

    @Test
    void aSpawnedMemberStartsFromTheStartingReserve() {
        Species eater = TestSpecies.eater(1, 15);
        Metabolism metabolism = Metabolism.of(
                new Spawn(eater.baseline(), eater, new SettingValues(eater.settings()), 0, Optional.empty()));
        assertThat(metabolism.ownView()).isEqualTo(new Metabolism.Own(15, 15));
        Species rabbit = Rabbit.species();
        Metabolism charged = Metabolism.of(
                new Spawn(rabbit.baseline(), rabbit, new SettingValues(rabbit.settings()), 0, Optional.empty()));
        assertThat(charged.ownView()).isEqualTo(new Metabolism.Own(15, 13));
    }

    @Test
    void aChildStartsFromWhatItsParentPaid() {
        Species eater = TestSpecies.eater(1, 15);
        Metabolism metabolism = Metabolism.of(
                new Spawn(eater.baseline(), eater, new SettingValues(eater.settings()), 3, Optional.of(14.0)));
        assertThat(metabolism.ownView()).isEqualTo(new Metabolism.Own(14, 14));
    }

    @Test
    void chargesOnceItsOwnCountReachesTheInterval() {
        Metabolism metabolism = metabolism(3, 2, 10);
        metabolism.tick();
        metabolism.tick();
        assertThat(metabolism.ownView()).isEqualTo(new Metabolism.Own(10, 15));
        metabolism.tick();
        assertThat(metabolism.ownView()).isEqualTo(new Metabolism.Own(8, 14));
    }

    @Test
    void chargesForSpeedAndTheTilesInView() {
        Metabolism metabolism = new Metabolism(withCharges(), genes(0.5, 3.4), 10);
        metabolism.tick();
        assertThat(((Metabolism.Own) metabolism.ownView()).reserve()).isCloseTo(8.86, within(1e-9));
    }

    @Test
    void daysLeftReadsTheWholeCharge() {
        assertThat(new Metabolism(withCharges(), genes(0.5, 3), 10).ownView()).isEqualTo(new Metabolism.Own(10, 8));
    }

    @Test
    void survivesAtZeroAndStarvesBelow() {
        Metabolism metabolism = metabolism(1, 1, 1);
        assertThat(metabolism.tick()).isEmpty();
        assertThat(metabolism.ownView()).isEqualTo(new Metabolism.Own(0, 0));
        assertThat(metabolism.tick()).contains(Metabolism.STARVED);
    }

    @Test
    void eatingCreditsTheReserve() {
        Metabolism metabolism = metabolism(1, 1, 0);
        metabolism.credit(15);
        assertThat(metabolism.ownView()).isEqualTo(new Metabolism.Own(15, 15));
    }

    @Test
    void spendingTakesFromTheReserve() {
        Metabolism metabolism = metabolism(1, 1, 60);
        metabolism.spend(45);
        assertThat(metabolism.reserve()).isEqualTo(15);
    }

    @Test
    void othersSeeNothing() {
        assertThat(metabolism(1, 1, 15).seenView()).isEmpty();
    }
}
