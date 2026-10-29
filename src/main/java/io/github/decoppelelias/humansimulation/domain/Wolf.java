package io.github.decoppelelias.humansimulation.domain;

import java.util.List;
import java.util.Map;
import java.util.Set;

public final class Wolf {
    private Wolf() {}

    public static Species species() {
        return Animal.species(
                "wolf",
                Map.of(
                        ScoringBrain.near(Features.MEAT), 1.0,
                        ScoringBrain.hungryNear(Features.MEAT), 2.0,
                        ScoringBrain.sum(Features.TAKES_MEAT), -0.2),
                Set.of(Meat.class),
                List.of(),
                List.of());
    }
}
