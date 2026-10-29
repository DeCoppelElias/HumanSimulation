package io.github.decoppelelias.humansimulation.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;

/** The recipe every animal shares, so a wolf differs from a rabbit only where it is a predator. */
final class Animal {
    private Animal() {}

    /** Every pull gene not named in pulls starts at 0. */
    static Species species(
            String name,
            Map<String, Double> pulls,
            Set<Class<? extends Edible>> diet,
            List<Function<Spawn, Component>> carried,
            List<Setting> ownSettings) {
        List<GeneSpec> layout = new ArrayList<>();
        layout.add(new GeneSpec.Scalar(CircularSense.VIEW_RANGE, 1, 10, 1.0));
        layout.addAll(ScoringBrain.pullLayout());
        layout.add(new GeneSpec.Scalar(ScoringBrain.WHIM, 0.05, 5, 0.1));
        layout.add(new GeneSpec.Scalar(ScoringBrain.BREED_PULL, 0, 5, 0.2));
        layout.add(new GeneSpec.Scalar(World.SPEED, 0, 1, 0.05));
        Map<String, Double> baseline = new TreeMap<>();
        layout.forEach(gene -> baseline.put(gene.key(), 0.0));
        baseline.put(CircularSense.VIEW_RANGE, 3.0);
        baseline.put(ScoringBrain.WHIM, 0.5);
        baseline.put(ScoringBrain.BREED_PULL, 1.0);
        baseline.put(World.SPEED, 0.5);
        baseline.putAll(pulls);
        List<Setting> settings = new ArrayList<>(shared(name));
        settings.addAll(ownSettings);
        List<Function<Spawn, Component>> parts =
                new ArrayList<>(List.of(Metabolism::of, spawn -> new Fertility(spawn.day()), spawn -> new Eater(diet)));
        parts.addAll(carried);
        return new Species(
                name,
                name,
                layout,
                new Genome(layout, baseline),
                new Settings(settings),
                Optional.of(ScoringBrain::of),
                parts);
    }

    private static List<Setting> shared(String name) {
        String one = "a " + name;
        return List.of(
                new Setting.Scalar(
                        Metabolism.EATING_INTERVAL,
                        "Days between two charges against " + one + "'s reserve.",
                        1,
                        30,
                        1,
                        1),
                new Setting.Scalar(Metabolism.EATING_CHARGE, "Reserve " + one + " pays at each charge.", 1, 100, 1, 1),
                new Setting.Scalar(
                        Metabolism.SPEED_CHARGE,
                        "Reserve " + one + " pays per unit of speed at each charge.",
                        0,
                        2,
                        0.01,
                        0.1),
                new Setting.Scalar(
                        Metabolism.VIEW_CHARGE,
                        "Reserve " + one + " pays per tile of view at each charge.",
                        0,
                        1,
                        0.01,
                        0.03),
                new Setting.Scalar(
                        Metabolism.STARTING_RESERVE,
                        "Reserve a spawned " + name + " starts with; one pays for a day of eating.",
                        0,
                        100,
                        1,
                        15),
                new Setting.Scalar(MoveRule.STRIDE, "Farthest " + one + " can walk in one day, in tiles.", 1, 5, 1, 3),
                new Setting.Scalar(
                        BreedRule.BREED_INTERVAL,
                        "Days " + one + " waits after its birth or last breed before it can breed.",
                        1,
                        100,
                        1,
                        15),
                new Setting.Scalar(BreedRule.CHILD_RESERVE, "Reserve a parent gives its child.", 0, 100, 1, 15),
                new Setting.Scalar(
                        BreedRule.BREEDING_LOSS, "Reserve lost to each birth on top of the child's.", 0, 200, 1, 30),
                new Setting.Scalar(
                        BreedRule.PARENT_MARGIN, "Reserve a parent must still hold after breeding.", 0, 100, 1, 5));
    }
}
