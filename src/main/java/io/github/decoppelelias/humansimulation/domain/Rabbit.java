package io.github.decoppelelias.humansimulation.domain;

import java.util.List;
import java.util.Map;
import java.util.Set;

public final class Rabbit {
    private Rabbit() {}

    public static Species species() {
        return Animal.species(
                "rabbit",
                Map.of(
                        ScoringBrain.near(Features.PLANT), 1.0,
                        ScoringBrain.hungryNear(Features.PLANT), 2.0,
                        ScoringBrain.sum(Features.MEAT), -0.2),
                Set.of(Plant.class),
                List.of(),
                List.of());
    }
}
