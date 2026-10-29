package io.github.decoppelelias.humansimulation.domain;

import java.util.List;
import java.util.Optional;

final class Metabolism implements Component {
    static final String EATING_INTERVAL = "eatingInterval";
    static final String EATING_CHARGE = "eatingCharge";
    static final String STARTING_RESERVE = "startingReserve";
    static final String SPEED_CHARGE = "speedCharge";
    static final String VIEW_CHARGE = "viewCharge";
    static final String STARVED = "starved";

    /** The reserve, and the days left at the live charge and interval, genes included. */
    record Own(double reserve, int daysLeft) {}

    private final SettingValues settings;
    private final Genome genome;
    private double reserve;
    private int daysSinceCharge;

    Metabolism(SettingValues settings, Genome genome, double reserve) {
        this.settings = settings;
        this.genome = genome;
        this.reserve = reserve;
    }

    /** A child starts from what its parent paid, a spawned member from the starting reserve. */
    static Metabolism of(Spawn spawn) {
        return new Metabolism(
                spawn.settings(),
                spawn.genome(),
                spawn.reserve().orElseGet(() -> spawn.settings().number(STARTING_RESERVE)));
    }

    @Override
    public Class<? extends Component> key() {
        return Metabolism.class;
    }

    @Override
    public Record ownView() {
        return new Own(reserve, daysLeft());
    }

    @Override
    public Optional<Record> seenView() {
        return Optional.empty();
    }

    @Override
    public List<Feature> features() {
        return List.of();
    }

    void credit(double amount) {
        reserve += amount;
    }

    double reserve() {
        return reserve;
    }

    /** Takes from the reserve. Whoever spends checks first that it may. */
    void spend(double amount) {
        reserve -= amount;
    }

    @Override
    public Optional<String> tick() {
        daysSinceCharge++;
        if (daysSinceCharge >= settings.whole(EATING_INTERVAL)) {
            daysSinceCharge = 0;
            reserve -= charge();
        }
        return reserve >= 0 ? Optional.empty() : Optional.of(STARVED);
    }

    /** Eating, plus speed and the tiles in view where the species declares both the gene and its charge. */
    private double charge() {
        double charge = settings.number(EATING_CHARGE);
        if (genome.declares(World.SPEED) && settings.declares(SPEED_CHARGE)) {
            charge += genome.scalar(World.SPEED) * settings.number(SPEED_CHARGE);
        }
        if (genome.declares(CircularSense.VIEW_RANGE) && settings.declares(VIEW_CHARGE)) {
            charge += Math.round(genome.scalar(CircularSense.VIEW_RANGE)) * settings.number(VIEW_CHARGE);
        }
        return charge;
    }

    /** The carrier survives every charge its reserve covers and dies at the next. */
    private int daysLeft() {
        int interval = settings.whole(EATING_INTERVAL);
        long charges = (long) Math.floor(reserve / charge());
        long days = charges * interval + Math.max(1, interval - daysSinceCharge) - 1;
        return (int) Math.min(Integer.MAX_VALUE, Math.max(0, days));
    }
}
