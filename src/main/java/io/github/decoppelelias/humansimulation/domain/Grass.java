package io.github.decoppelelias.humansimulation.domain;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class Grass {
    static final String NOURISHMENT = "nourishment";

    private Grass() {}

    public static Species species() {
        Settings settings = new Settings(List.of(
                new Setting.Scalar(NOURISHMENT, "Reserve an eater gains from one grass, in days.", 1, 100, 1, 15)));
        return new Species(
                "grass",
                "grass",
                List.of(),
                new Genome(List.of(), Map.of()),
                settings,
                Optional.empty(),
                List.of(spawn -> new Edible(spawn.species().settings().number(NOURISHMENT))));
    }
}
