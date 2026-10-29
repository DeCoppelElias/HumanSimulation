package io.github.decoppelelias.humansimulation.domain;

import java.util.List;
import java.util.Optional;

/** Counts its carrier's days and ends it when they reach the species' lifespan, read live. */
final class Lifespan implements Component {
    static final String LIFESPAN = "lifespan";
    static final String AGED = "aged";

    /** Days lived, and the ticks it still survives. */
    record Own(int age, int daysLeft) {}

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
        return new Own(age, Math.max(0, settings.whole(LIFESPAN) - age - 1));
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
    public Optional<String> tick() {
        age++;
        return age >= settings.whole(LIFESPAN) ? Optional.of(AGED) : Optional.empty();
    }
}
