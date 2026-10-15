package io.github.decoppelelias.humansimulation.domain;

import java.util.Optional;
import java.util.function.DoubleSupplier;

/** Food worth an amount read live, so an edit reaches food already lying on the ground. */
final class Edible implements Component {
    record Amount(double amount) {}

    private final DoubleSupplier amount;

    Edible(DoubleSupplier amount) {
        this.amount = amount;
    }

    double amount() {
        return amount.getAsDouble();
    }

    @Override
    public Class<? extends Component> key() {
        return Edible.class;
    }

    @Override
    public Record ownView() {
        return new Amount(amount());
    }

    @Override
    public Optional<Record> seenView() {
        return Optional.of(new Amount(amount()));
    }
}
