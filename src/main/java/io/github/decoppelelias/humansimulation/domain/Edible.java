package io.github.decoppelelias.humansimulation.domain;

/** Food worth an amount, per 0016. Each kind of food is a component of its own, with its own key. */
interface Edible extends Component {
    record Amount(double amount) {}

    double amount();
}
