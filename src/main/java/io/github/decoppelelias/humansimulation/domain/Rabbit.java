package io.github.decoppelelias.humansimulation.domain;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class Rabbit {
    private Rabbit() {}

    public static Species species() {
        List<GeneSpec> layout =
                List.of(new GeneSpec.Scalar("viewRange", 1, 10, 1.0), new GeneSpec.Simplex("steps", 1, 3, 0.2));
        Genome baseline = new Genome(layout, Map.of("viewRange", 3.0), Map.of("steps", List.of(0.6, 0.3, 0.1)));
        return new Species(
                "rabbit",
                "rabbit",
                layout,
                baseline,
                Optional.of(spawn -> new RandomBrain(spawn.genome().simplex("steps"))),
                List.of());
    }
}
