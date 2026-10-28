package io.github.decoppelelias.humansimulation.domain;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/** The foods its carrier takes, fixed in its species recipe, per 0016. Others see nothing of it. */
final class Eater implements Component {
    record Takes(Set<Class<? extends Edible>> foods) {}

    private final Set<Class<? extends Edible>> foods;

    Eater(Set<Class<? extends Edible>> foods) {
        this.foods = Set.copyOf(foods);
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
        return Optional.empty();
    }
}
