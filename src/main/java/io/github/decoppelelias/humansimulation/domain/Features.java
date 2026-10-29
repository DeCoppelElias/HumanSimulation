package io.github.decoppelelias.humansimulation.domain;

import java.util.List;

/** Every feature a brain carries genes for, in gene layout order, per 0013. */
final class Features {
    static final Feature PLANT = Feature.of(Plant.class);
    static final Feature MEAT = Feature.of(Meat.class);
    static final Feature TAKES_PLANT = Feature.takes(Plant.class);
    static final Feature TAKES_MEAT = Feature.takes(Meat.class);
    static final List<Feature> ALL = List.of(PLANT, MEAT, TAKES_PLANT, TAKES_MEAT);

    private Features() {}
}
