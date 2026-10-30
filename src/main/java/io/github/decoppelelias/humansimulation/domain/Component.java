package io.github.decoppelelias.humansimulation.domain;

import java.util.List;
import java.util.Map;
import java.util.Optional;

interface Component {
    Class<? extends Component> key();

    Record ownView();

    /** Empty when creatures in range cannot see it. */
    Optional<Record> seenView();

    /** What creatures in range see it as; empty when its seen view is. */
    List<Feature> features();

    /** The values a person watching sees, by name, per 0007. */
    Map<String, Double> shown();

    /** One day passes for this part alone. Returns the cause its carrier ends with, or nothing. */
    default Optional<String> tick() {
        return Optional.empty();
    }
}
