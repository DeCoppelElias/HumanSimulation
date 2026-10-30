package io.github.decoppelelias.humansimulation.domain;

import java.util.Map;

/** Food worth an amount, per 0016. Each kind of food is a component of its own, with its own key. */
interface Edible extends Component {
    /** The setting a food's species declares for what one is worth. */
    String NOURISHMENT = "nourishment";

    String AMOUNT = "amount";

    record Amount(double amount) {}

    double amount();

    @Override
    default Map<String, Double> shown() {
        return Map.of(AMOUNT, amount());
    }
}
