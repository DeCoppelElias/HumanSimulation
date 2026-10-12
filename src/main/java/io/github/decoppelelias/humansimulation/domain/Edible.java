package io.github.decoppelelias.humansimulation.domain;

import java.util.Optional;

record Edible(double amount) implements Component {
    record Amount(double amount) {}

    Edible {
        if (!(amount > 0)) {
            throw new IllegalArgumentException("an edible is worth more than nothing, got " + amount);
        }
    }

    @Override
    public Class<? extends Component> key() {
        return Edible.class;
    }

    @Override
    public Record ownView() {
        return new Amount(amount);
    }

    @Override
    public Optional<Record> seenView() {
        return Optional.of(new Amount(amount));
    }
}
