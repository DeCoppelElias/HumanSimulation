package io.github.decoppelelias.humansimulation.domain;

import java.util.List;
import java.util.Optional;
import java.util.function.DoubleSupplier;

/** The food kind grass carries, worth an amount read live, so an edit reaches plants already on the ground. */
final class Plant implements Edible {
    private final DoubleSupplier amount;

    Plant(DoubleSupplier amount) {
        this.amount = amount;
    }

    @Override
    public double amount() {
        return amount.getAsDouble();
    }

    @Override
    public Class<? extends Component> key() {
        return Plant.class;
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
        return List.of(Features.PLANT);
    }
}
