package io.github.decoppelelias.humansimulation.domain;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class Grass {
    static final String NOURISHMENT = "nourishment";

    private Grass() {}

    public static Species species() {
        Settings settings = new Settings(List.of(
                new Setting.Scalar(NOURISHMENT, "Reserve an eater gains from one grass.", 1, 100, 1, 15),
                new Setting.Scalar(Lifespan.LIFESPAN, "Days a grass lies uneaten before it withers.", 1, 100, 1, 12)));
        return new Species(
                "grass",
                "grass",
                List.of(),
                new Genome(List.of(), Map.of()),
                settings,
                Optional.empty(),
                List.of(spawn -> new Edible(() -> spawn.settings().number(NOURISHMENT)), Lifespan::of));
    }
}
