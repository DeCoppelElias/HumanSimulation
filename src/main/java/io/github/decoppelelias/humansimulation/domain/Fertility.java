package io.github.decoppelelias.humansimulation.domain;

import java.util.List;
import java.util.Optional;

/** The breeding window: the day of its carrier's birth or last breed, counted in completed days. */
final class Fertility implements Component {
    record Since(int day) {}

    private int since;

    Fertility(int since) {
        this.since = since;
    }

    @Override
    public Class<? extends Component> key() {
        return Fertility.class;
    }

    @Override
    public Record ownView() {
        return new Since(since);
    }

    @Override
    public Optional<Record> seenView() {
        return Optional.empty();
    }

    @Override
    public List<Feature> features() {
        return List.of();
    }

    boolean open(int today, int interval) {
        return today - since >= interval;
    }

    void bred(int today) {
        since = today;
    }
}
