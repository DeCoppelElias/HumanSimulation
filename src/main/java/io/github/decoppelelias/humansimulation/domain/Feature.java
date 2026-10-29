package io.github.decoppelelias.humansimulation.domain;

/** A fact a component shows others, per 0013. Built from a class, so a misspelt feature does not compile. */
record Feature(String name) {
    Feature {
        if (name.isBlank()) {
            throw new IllegalArgumentException("a feature needs a name");
        }
    }

    static Feature of(Class<? extends Component> kind) {
        return new Feature(kind.getSimpleName());
    }

    static Feature takes(Class<? extends Edible> food) {
        return new Feature("takes." + food.getSimpleName());
    }
}
