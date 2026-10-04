package io.github.decoppelelias.humansimulation.domain;

import java.util.List;
import java.util.Map;
import java.util.Optional;

final class TestSpecies {
    static final Direction NORTH = new Direction("north", 0, -1);
    static final Direction EAST = new Direction("east", 1, 0);

    private TestSpecies() {}

    /** A species named "walker", seeing one tile, whose brain always returns the same intent. */
    static Species walker(Intent intent) {
        List<GeneSpec> layout = List.of(new GeneSpec.Scalar("viewRange", 1, 10, 1.0));
        Genome baseline = new Genome(layout, Map.of("viewRange", 1.0), Map.of());
        Brain brain = (perception, options, random) -> intent;
        return new Species("walker", "walker", layout, baseline, Optional.of(spawn -> brain), List.of());
    }
}
