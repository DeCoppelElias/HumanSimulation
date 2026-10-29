package io.github.decoppelelias.humansimulation.domain;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/** The foods its carrier takes, fixed in its species recipe and shown to others, per 0016. */
final class Eater implements Component {
    /** Sorted by simple name, so a set's order cannot reach a replay. */
    record Takes(List<Class<? extends Edible>> foods) {}

    private final List<Class<? extends Edible>> foods;

    Eater(Set<Class<? extends Edible>> foods) {
        this.foods = foods.stream()
                .sorted(Comparator.comparing(Class::getSimpleName))
                .toList();
    }

    /** The first of these parts that is a food this eater takes. */
    Optional<Edible> foodIn(List<Component> parts) {
        return parts.stream()
                .filter(Edible.class::isInstance)
                .map(Edible.class::cast)
                .filter(food -> foods.contains(food.key()))
                .findFirst();
    }

    @Override
    public Class<? extends Component> key() {
        return Eater.class;
    }

    @Override
    public Record ownView() {
        return new Takes(foods);
    }

    @Override
    public Optional<Record> seenView() {
        return Optional.of(new Takes(foods));
    }

    @Override
    public List<Feature> features() {
        return foods.stream().map(Feature::takes).toList();
    }
}
