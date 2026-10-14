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
        Genome baseline = new Genome(layout, Map.of("viewRange", 1.0));
        Brain brain = (perception, options, random) -> intent;
        return new Species("walker", "walker", layout, baseline, Settings.NONE, Optional.of(spawn -> brain), List.of());
    }

    static Settings metabolism(int interval, int startingReserve) {
        return new Settings(List.of(
                new Setting.Scalar(Metabolism.EATING_INTERVAL, "Days between charges.", 1, 30, 1, interval),
                new Setting.Scalar(Metabolism.EATING_CHARGE, "Paid at each charge.", 1, 100, 1, 1),
                new Setting.Scalar(Metabolism.STARTING_RESERVE, "Reserve at spawn.", 0, 100, 1, startingReserve)));
    }

    /** A species named "eater" that idles, charged one on every interval. */
    static Species eater(int interval, int startingReserve) {
        List<GeneSpec> layout = List.of(new GeneSpec.Scalar("viewRange", 1, 10, 1.0));
        Brain brain = (perception, options, random) -> new Intent.Idle();
        return new Species(
                "eater",
                "eater",
                layout,
                new Genome(layout, Map.of("viewRange", 1.0)),
                metabolism(interval, startingReserve),
                Optional.of(spawn -> brain),
                List.of(spawn -> new Metabolism(spawn.species().settings())));
    }

    /** A species named "food", worth five, with no brain. */
    static Species food() {
        return new Species(
                "food",
                "food",
                List.of(),
                new Genome(List.of(), Map.of()),
                Settings.NONE,
                Optional.empty(),
                List.of(spawn -> new Edible(5)));
    }

    /** A species named "recorder", seeing one tile and walking up to three, whose brain idles and keeps its inputs. */
    static Species recorder(List<Perception> perceptions, List<Options> options) {
        List<GeneSpec> layout = List.of(new GeneSpec.Scalar("viewRange", 1, 10, 1.0));
        Genome baseline = new Genome(layout, Map.of("viewRange", 1.0));
        Brain brain = (perception, offered, random) -> {
            perceptions.add(perception);
            options.add(offered);
            return new Intent.Idle();
        };
        Settings settings = new Settings(List.of(new Setting.Scalar(World.STRIDE, "Farthest walk.", 1, 5, 1, 3)));
        return new Species("recorder", "recorder", layout, baseline, settings, Optional.of(spawn -> brain), List.of());
    }
}
