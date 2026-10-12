package io.github.decoppelelias.humansimulation.domain;

import java.util.Optional;

final class Metabolism implements Component {
    static final String EATING_INTERVAL = "eatingInterval";
    static final String EATING_CHARGE = "eatingCharge";
    static final String STARTING_RESERVE = "startingReserve";

    /** The reserve counts days at the default charge; days left reads the live charge and interval. */
    record Own(double reserve, int daysLeft) {}

    private final Settings settings;
    private double reserve;
    private int daysSinceCharge;

    Metabolism(Settings settings) {
        this.settings = settings;
        this.reserve = settings.number(STARTING_RESERVE);
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

    void credit(double amount) {
        reserve += amount;
    }

    /** One day passes. Returns whether the carrier is still alive. */
    boolean tick() {
        daysSinceCharge++;
        if (daysSinceCharge >= settings.whole(EATING_INTERVAL)) {
            daysSinceCharge = 0;
            reserve -= settings.number(EATING_CHARGE);
        }
        return reserve >= 0;
    }

    /** The carrier survives every charge its reserve covers and dies at the next. */
    private int daysLeft() {
        int interval = settings.whole(EATING_INTERVAL);
        long charges = (long) Math.floor(reserve / settings.number(EATING_CHARGE));
        long days = charges * interval + Math.max(1, interval - daysSinceCharge) - 1;
        return (int) Math.min(Integer.MAX_VALUE, Math.max(0, days));
    }
}
