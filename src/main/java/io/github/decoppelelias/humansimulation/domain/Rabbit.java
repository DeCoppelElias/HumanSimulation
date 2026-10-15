package io.github.decoppelelias.humansimulation.domain;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class Rabbit {
    private Rabbit() {}

    public static Species species() {
        List<GeneSpec> layout = List.of(
                new GeneSpec.Scalar("viewRange", 1, 10, 1.0),
                new GeneSpec.Scalar("grassPull", 0, 5, 0.2),
                new GeneSpec.Scalar("hungerDrive", 0, 10, 0.4),
                new GeneSpec.Scalar("rabbitPull", -2, 2, 0.1),
                new GeneSpec.Scalar("whim", 0.05, 5, 0.1),
                new GeneSpec.Scalar("breedPull", 0, 5, 0.2),
                new GeneSpec.Scalar(World.SPEED, 0, 1, 0.05));
        Genome baseline = new Genome(
                layout,
                Map.of(
                        "viewRange",
                        3.0,
                        "grassPull",
                        1.0,
                        "hungerDrive",
                        2.0,
                        "rabbitPull",
                        -0.2,
                        "whim",
                        0.5,
                        "breedPull",
                        1.0,
                        World.SPEED,
                        0.5));
        Settings settings = new Settings(List.of(
                new Setting.Scalar(
                        Metabolism.EATING_INTERVAL,
                        "Days between two charges against a rabbit's reserve.",
                        1,
                        30,
                        1,
                        1),
                new Setting.Scalar(Metabolism.EATING_CHARGE, "Reserve a rabbit pays at each charge.", 1, 100, 1, 1),
                new Setting.Scalar(
                        Metabolism.SPEED_CHARGE,
                        "Reserve a rabbit pays per unit of speed at each charge.",
                        0,
                        2,
                        0.01,
                        0.1),
                new Setting.Scalar(
                        Metabolism.VIEW_CHARGE,
                        "Reserve a rabbit pays per tile of view at each charge.",
                        0,
                        1,
                        0.01,
                        0.03),
                new Setting.Scalar(
                        Metabolism.STARTING_RESERVE, "Reserve a spawned rabbit starts with, in days.", 0, 100, 1, 15),
                new Setting.Scalar(MoveRule.STRIDE, "Farthest a rabbit can walk in one day, in tiles.", 1, 5, 1, 3),
                new Setting.Scalar(
                        BreedRule.BREED_INTERVAL,
                        "Days a rabbit waits after its birth or last breed before it can breed.",
                        1,
                        100,
                        1,
                        15),
                new Setting.Scalar(
                        BreedRule.CHILD_RESERVE, "Reserve a parent gives its child, in days.", 0, 100, 1, 15),
                new Setting.Scalar(
                        BreedRule.BREEDING_LOSS, "Reserve lost to each birth on top of the child's.", 0, 200, 1, 30),
                new Setting.Scalar(
                        BreedRule.PARENT_MARGIN, "Reserve a parent must still hold after breeding.", 0, 100, 1, 5)));
        return new Species(
                "rabbit",
                "rabbit",
                layout,
                baseline,
                settings,
                Optional.of(ScoringBrain::of),
                List.of(Metabolism::of, spawn -> new Fertility(spawn.day())));
    }
}
