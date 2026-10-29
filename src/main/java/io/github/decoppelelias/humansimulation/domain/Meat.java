package io.github.decoppelelias.humansimulation.domain;

import java.util.List;
import java.util.Optional;
import java.util.function.DoubleSupplier;

/** The food kind a rabbit carries, worth an amount read live, per 0016. */
final class Meat implements Edible {
    private final DoubleSupplier amount;

    Meat(DoubleSupplier amount) {
        this.amount = amount;
    }

    @Override
    public double amount() {
        return amount.getAsDouble();
    }

    @Override
    public Class<? extends Component> key() {
        return Meat.class;
    }

    @Override
    public Record ownView() {
        return new Amount(amount());
    }

    @Override
    public Optional<Record> seenView() {
        return Optional.of(new Amount(amount()));
    }

    @Override
    public List<Feature> features() {
        return List.of(Features.MEAT);
    }
}
