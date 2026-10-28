package io.github.decoppelelias.humansimulation.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

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

    /** Breeding every 15 days, with a child's reserve of 15, a loss of 30 and a margin of 5. */
    static Settings breeding() {
        return new Settings(List.of(
                new Setting.Scalar(BreedRule.BREED_INTERVAL, "Days between breeds.", 1, 100, 1, 15),
                new Setting.Scalar(BreedRule.CHILD_RESERVE, "Reserve a child starts with.", 0, 100, 1, 15),
                new Setting.Scalar(BreedRule.BREEDING_LOSS, "Reserve lost to a birth.", 0, 200, 1, 30),
                new Setting.Scalar(BreedRule.PARENT_MARGIN, "Reserve a parent keeps.", 0, 100, 1, 5)));
    }

    /** A species named "eater" that idles and takes plants, charged one on every interval. */
    static Species eater(int interval, int startingReserve) {
        return eater("eater", interval, startingReserve, List.of(spawn -> new Eater(Set.of(Plant.class))));
    }

    /** An idling eater named for the foods it takes, starting with nothing and charged one a day. */
    static Species eaterTaking(String name, Set<Class<? extends Edible>> foods) {
        return eater(name, 1, 0, List.of(spawn -> new Eater(foods)));
    }

    /** An idling species named "unfed" with a metabolism and no eater, starting with nothing. */
    static Species unfed() {
        return eater("unfed", 1, 0, List.of());
    }

    private static Species eater(
            String name, int interval, int startingReserve, List<Function<Spawn, Component>> diet) {
        List<GeneSpec> layout = List.of(new GeneSpec.Scalar("viewRange", 1, 10, 1.0));
        Brain brain = (perception, options, random) -> new Intent.Idle();
        List<Function<Spawn, Component>> parts = new ArrayList<>();
        parts.add(Metabolism::of);
        parts.addAll(diet);
        return new Species(
                name,
                name,
                layout,
                new Genome(layout, Map.of("viewRange", 1.0)),
                metabolism(interval, startingReserve),
                Optional.of(spawn -> brain),
                parts);
    }

    /** A species named "food", a plant worth five, with no brain. */
    static Species food() {
        return new Species(
                "food",
                "food",
                List.of(),
                new Genome(List.of(), Map.of()),
                Settings.NONE,
                Optional.empty(),
                List.of(spawn -> new Plant(() -> 5)));
    }

    /** A food kind no production species carries, so a test can tell one kind from another. */
    static final class Bone implements Edible {
        @Override
        public double amount() {
            return 5;
        }

        @Override
        public Class<? extends Component> key() {
            return Bone.class;
        }

        @Override
        public Record ownView() {
            return new Amount(5);
        }

        @Override
        public Optional<Record> seenView() {
            return Optional.of(new Amount(5));
        }
    }

    /** A species named "bone", a bone worth five, with no brain. */
    static Species bone() {
        return new Species(
                "bone",
                "bone",
                List.of(),
                new Genome(List.of(), Map.of()),
                Settings.NONE,
                Optional.empty(),
                List.of(spawn -> new Bone()));
    }

    /**
     * A species named "breeder" that breeds whenever offered, idles otherwise and keeps the options it saw. It starts
     * with 100, walks one tile, and its view range spreads widely, so a child shows whose genome it came from.
     */
    static Species breeder(List<Options> seen) {
        return breeder((perception, options, random) -> {
            seen.add(options);
            return options.offered().contains(new Option.Breed()) ? new Intent.Breed() : new Intent.Idle();
        });
    }

    /** A breeder whose brain asks to breed every day, offered or not. */
    static Species eagerBreeder() {
        return breeder((perception, options, random) -> new Intent.Breed());
    }

    private static Species breeder(Brain brain) {
        List<GeneSpec> layout = List.of(new GeneSpec.Scalar("viewRange", 1, 10, 4.0));
        List<Setting> declared = new ArrayList<>(metabolism(1, 100).declared());
        declared.addAll(breeding().declared());
        declared.add(new Setting.Scalar(MoveRule.STRIDE, "Farthest walk.", 1, 5, 1, 1));
        return new Species(
                "breeder",
                "breeder",
                layout,
                new Genome(layout, Map.of("viewRange", 5.0)),
                new Settings(declared),
                Optional.of(spawn -> brain),
                List.of(Metabolism::of, spawn -> new Fertility(spawn.day())));
    }

    /**
     * A species named "recorder", seeing one tile and walking up to three, whose brain idles and keeps its inputs. Its
     * sight does not mutate, so every member sees the same tiles.
     */
    static Species recorder(List<Perception> perceptions, List<Options> options) {
        List<GeneSpec> layout = List.of(new GeneSpec.Scalar("viewRange", 1, 10, 0));
        Genome baseline = new Genome(layout, Map.of("viewRange", 1.0));
        Brain brain = (perception, offered, random) -> {
            perceptions.add(perception);
            options.add(offered);
            return new Intent.Idle();
        };
        Settings settings = new Settings(List.of(new Setting.Scalar(MoveRule.STRIDE, "Farthest walk.", 1, 5, 1, 3)));
        return new Species("recorder", "recorder", layout, baseline, settings, Optional.of(spawn -> brain), List.of());
    }
}
