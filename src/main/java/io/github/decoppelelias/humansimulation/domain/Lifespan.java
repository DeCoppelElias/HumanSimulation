package io.github.decoppelelias.humansimulation.domain;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.TreeMap;

/** Counts its carrier's days, and ends it when they reach the species' lifespan, read live, where the species sets one. */
final class Lifespan implements Component {
    static final String LIFESPAN = "lifespan";
    static final String AGED = "aged";
    static final String AGE = "age";
    static final String DAYS_TO_LIVE = "daysToLive";

    /** Days lived, and the ticks it still survives where the species sets a lifespan. */
    record Own(int age, OptionalInt daysLeft) {}

    private final SettingValues settings;
    private int age;

    Lifespan(SettingValues settings) {
        this.settings = settings;
    }

    static Lifespan of(Spawn spawn) {
        return new Lifespan(spawn.settings());
    }

    @Override
    public Class<? extends Component> key() {
        return Lifespan.class;
    }

    @Override
    public Record ownView() {
        return new Own(age, daysLeft());
    }

    @Override
    public Optional<Record> seenView() {
        return Optional.empty();
    }

    @Override
    public List<Feature> features() {
        return List.of();
    }

    @Override
    public Map<String, Double> shown() {
        Map<String, Double> shown = new TreeMap<>();
        shown.put(AGE, (double) age);
        daysLeft().ifPresent(days -> shown.put(DAYS_TO_LIVE, (double) days));
        return shown;
    }

    @Override
    public Optional<String> tick() {
        age++;
        return settings.declares(LIFESPAN) && age >= settings.whole(LIFESPAN) ? Optional.of(AGED) : Optional.empty();
    }

    private OptionalInt daysLeft() {
        return settings.declares(LIFESPAN)
                ? OptionalInt.of(Math.max(0, settings.whole(LIFESPAN) - age - 1))
                : OptionalInt.empty();
    }
}
