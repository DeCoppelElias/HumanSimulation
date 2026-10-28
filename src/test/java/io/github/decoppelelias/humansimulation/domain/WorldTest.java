package io.github.decoppelelias.humansimulation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.assertj.core.groups.Tuple.tuple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.TreeMap;
import java.util.random.RandomGeneratorFactory;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

class WorldTest {
    private static final List<Species> RABBITS = List.of(Rabbit.species());

    static List<GridPosition> occupied(WorldSnapshot snapshot) {
        return snapshot.tiles().stream()
                .flatMap(tile -> tile.entities().stream().map(entity -> tile.at()))
                .toList();
    }

    @Test
    void aNewWorldIsEmptyOnDayZero() {
        WorldSnapshot snapshot = new World(4, 3, 42, RABBITS).snapshot();
        assertThat(snapshot.seed()).isEqualTo(42);
        assertThat(snapshot.day()).isZero();
        assertThat(snapshot.width()).isEqualTo(4);
        assertThat(snapshot.height()).isEqualTo(3);
        assertThat(snapshot.tiles()).hasSize(12);
        assertThat(occupied(snapshot)).isEmpty();
    }

    @Test
    void aWorldNeedsAtLeastOneTile() {
        assertThatThrownBy(() -> new World(0, 3, 42, RABBITS)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void twoSpeciesCannotShareAName() {
        assertThatThrownBy(() -> new World(3, 3, 42, List.of(Rabbit.species(), Rabbit.species())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("rabbit");
    }

    @Test
    void aSpawnWaitsForPendingCommandsToApply() {
        World world = new World(5, 5, 1, RABBITS);
        world.submit(new Command.Spawn("rabbit", 3));
        assertThat(occupied(world.snapshot())).isEmpty();
        assertThat(occupied(world.applyPending())).hasSize(3);
    }

    @Test
    void applyPendingShowsCommandsAndKeepsTheDay() {
        World world = new World(5, 5, 1, RABBITS);
        world.submit(new Command.Spawn("rabbit", 2));
        WorldSnapshot snapshot = world.applyPending();
        assertThat(snapshot.day()).isZero();
        assertThat(occupied(snapshot)).hasSize(2);
    }

    @Test
    void spawnAtPlacesOneOnThatTile() {
        World world = new World(5, 5, 1, RABBITS);
        world.submit(new Command.SpawnAt("rabbit", new GridPosition(3, 4)));
        assertThat(occupied(world.applyPending())).containsExactly(new GridPosition(3, 4));
    }

    @Test
    void aSpawnedCountMayShareTiles() {
        World world = new World(1, 1, 1, RABBITS);
        world.submit(new Command.Spawn("rabbit", 3));
        WorldSnapshot snapshot = world.applyPending();
        assertThat(snapshot.tiles().getFirst().entities())
                .extracting(WorldSnapshot.EntityView::id)
                .containsExactly(0, 1, 2);
    }

    @Test
    void aRejectedCommandChangesNothing() {
        World world = new World(5, 5, 1, RABBITS);
        world.submit(new Command.Spawn("rabbit", 2));
        WorldSnapshot before = world.applyPending();
        List<LoggedCommand> logBefore = world.log();

        assertThatThrownBy(() -> world.submit(new Command.Spawn("wolf", 1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("wolf");
        assertThatThrownBy(() -> world.submit(new Command.SpawnAt("rabbit", new GridPosition(5, 0))))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Command.Spawn("rabbit", 0)).isInstanceOf(IllegalArgumentException.class);

        assertThat(world.applyPending()).isEqualTo(before);
        assertThat(world.log()).isEqualTo(logBefore);
    }

    @Test
    void theLogRecordsEachCommandWithTheDayCounterWhenItDrained() {
        World world = new World(5, 5, 1, RABBITS);
        Command spawn = new Command.Spawn("rabbit", 2);
        world.submit(spawn);
        world.applyPending();
        assertThat(world.log()).containsExactly(new LoggedCommand(0, spawn));
    }

    @Test
    void advancingCompletesADay() {
        World world = new World(5, 5, 1, RABBITS);
        world.submit(new Command.Spawn("rabbit", 3));
        DayReport report = world.advance();
        assertThat(report.snapshot().day()).isEqualTo(1);
        assertThat(report.census().day()).isEqualTo(1);
        assertThat(report.census().population()).isEqualTo(Map.of("rabbit", 3));
    }

    @Test
    void theCensusAveragesEachGenePerSpecies() {
        World world = new World(5, 5, 1, List.of(Rabbit.species(), TestSpecies.food(), TestSpecies.eater(1, 1)));
        world.submit(new Command.Spawn("rabbit", 3));
        world.submit(new Command.SpawnAt("food", new GridPosition(4, 4)));
        world.submit(new Command.SpawnAt("eater", new GridPosition(4, 0)));
        CensusRow row = world.advance().census();
        assertThat(row.geneMeans()).containsOnlyKeys("eater", "rabbit");
        assertThat(row.geneMeans().get("rabbit")).hasSize(7);
        for (GeneSpec spec : Rabbit.species().geneLayout()) {
            double sum = 0;
            for (int id = 0; id < 3; id++) {
                sum += world.entity(id).orElseThrow().genome().scalar(spec.key());
            }
            assertThat(row.geneMeans().get("rabbit").get(spec.key())).isCloseTo(sum / 3, within(1e-12));
        }
        assertThat(world.entity(0).orElseThrow().genome().scalar("grassPull"))
                .isNotEqualTo(world.entity(1).orElseThrow().genome().scalar("grassPull"));

        CensusRow afterTheEaterStarves = world.advance().census();
        assertThat(afterTheEaterStarves.population()).containsEntry("eater", 0);
        assertThat(afterTheEaterStarves.geneMeans()).containsOnlyKeys("rabbit");
    }

    @Test
    void theCensusListsEverySpeciesTheWorldKnows() {
        World world = new World(5, 5, 1, List.of(Rabbit.species(), TestSpecies.walker(new Intent.Idle())));
        world.submit(new Command.Spawn("rabbit", 2));
        assertThat(world.advance().census().population())
                .containsExactly(Map.entry("rabbit", 2), Map.entry("walker", 0));
    }

    @Test
    void theCensusHasARowForEveryDay() {
        World world = new World(5, 5, 1, RABBITS);
        world.submit(new Command.Spawn("rabbit", 3));
        List<Integer> days = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            days.add(world.advance().census().day());
        }
        assertThat(days).isEqualTo(IntStream.rangeClosed(1, 50).boxed().toList());
    }

    @Test
    void aMoveStopsAtTheEdge() {
        World world = new World(5, 5, 1, List.of(TestSpecies.walker(new Intent.Move(TestSpecies.NORTH, 3))));
        world.submit(new Command.SpawnAt("walker", new GridPosition(2, 1)));
        assertThat(occupied(world.advance().snapshot())).containsExactly(new GridPosition(2, 0));
    }

    @Test
    void aClearMoveWalksItsWholeDistance() {
        World world = new World(5, 5, 1, List.of(TestSpecies.walker(new Intent.Move(TestSpecies.EAST, 2))));
        world.submit(new Command.SpawnAt("walker", new GridPosition(0, 0)));
        assertThat(occupied(world.advance().snapshot())).containsExactly(new GridPosition(2, 0));
    }

    @Test
    void rabbitsWander() {
        World world = new World(20, 20, 1, RABBITS);
        world.submit(new Command.Spawn("rabbit", 10));
        List<GridPosition> start = occupied(world.applyPending());
        assertThat(occupied(world.advance().snapshot())).isNotEqualTo(start);
    }

    @Test
    void aResetLeavesTheWorldAsConstructionWould() {
        World world = new World(10, 10, 1, RABBITS);
        world.submit(new Command.Spawn("rabbit", 5));
        for (int i = 0; i < 3; i++) {
            world.advance();
        }
        world.submit(new Command.Reset(7));
        World fresh = new World(10, 10, 7, RABBITS);

        assertThat(world.applyPending()).isEqualTo(fresh.snapshot());
        assertThat(world.log()).containsExactly(new LoggedCommand(0, new Command.Reset(7)));

        world.submit(new Command.Spawn("rabbit", 5));
        fresh.submit(new Command.Spawn("rabbit", 5));
        for (int i = 0; i < 10; i++) {
            assertThat(world.advance()).isEqualTo(fresh.advance());
        }
    }

    @Test
    void spawnedMembersVaryAroundTheBaseline() {
        World world = new World(5, 5, 1, RABBITS);
        world.submit(new Command.Spawn("rabbit", 20));
        world.applyPending();
        List<Double> views = new ArrayList<>();
        for (int id = 0; id < 20; id++) {
            Genome genome = world.entity(id).orElseThrow().genome();
            assertThat(genome.scalar("viewRange")).isBetween(2.0, 4.0);
            assertThat(genome.scalar("grassPull")).isBetween(0.8, 1.2);
            views.add(genome.scalar("viewRange"));
        }
        assertThat(views).doesNotHaveDuplicates();
        world.submit(new Command.SpawnAt("rabbit", new GridPosition(0, 0)));
        world.submit(new Command.SpawnAt("rabbit", new GridPosition(0, 0)));
        world.applyPending();
        assertThat(world.entity(20).orElseThrow().genome())
                .isNotEqualTo(world.entity(21).orElseThrow().genome());
    }

    private static double reserveOf(World world, int id) {
        return world.entity(id)
                .orElseThrow()
                .part(Metabolism.class)
                .orElseThrow()
                .reserve();
    }

    @Test
    void breedingIsOfferedOnlyOnceTheWindowOpens() {
        List<Options> seen = new ArrayList<>();
        World world = new World(3, 3, 1, List.of(TestSpecies.breeder(seen)));
        world.submit(new Command.SpawnAt("breeder", new GridPosition(1, 1)));
        for (int i = 0; i < 15; i++) {
            world.advance();
        }
        assertThat(seen)
                .hasSize(15)
                .allSatisfy(options -> assertThat(options.offered()).doesNotContain(new Option.Breed()));
        world.advance();
        List<Option> offered = seen.get(15).offered();
        assertThat(offered).hasSize(6).startsWith(new Option.Idle()).endsWith(new Option.Breed());
    }

    @Test
    void breedingSpawnsAChildOnItsParentsTile() {
        World world = new World(3, 3, 1, List.of(TestSpecies.breeder(new ArrayList<>())));
        world.submit(new Command.SpawnAt("breeder", new GridPosition(1, 1)));
        DayReport report = world.advance();
        for (int i = 1; i < 16; i++) {
            report = world.advance();
        }
        assertThat(report.census().population()).containsEntry("breeder", 2);
        assertThat(report.snapshot().tiles().get(4).entities())
                .extracting(WorldSnapshot.EntityView::id)
                .containsExactly(0, 1);
        assertThat(reserveOf(world, 0)).isEqualTo(39);
        assertThat(reserveOf(world, 1)).isEqualTo(14);
    }

    @Test
    void aChildFirstActsTheDayAfterItsBirth() {
        List<Options> seen = new ArrayList<>();
        World world = new World(3, 3, 1, List.of(TestSpecies.breeder(seen)));
        world.submit(new Command.SpawnAt("breeder", new GridPosition(1, 1)));
        for (int i = 0; i < 16; i++) {
            world.advance();
        }
        assertThat(seen).hasSize(16);
        world.advance();
        assertThat(seen).hasSize(18);
    }

    @Test
    void aChildCarriesItsParentsGenomeMutated() {
        World world = new World(5, 5, 1, List.of(TestSpecies.breeder(new ArrayList<>())));
        for (int x = 0; x < 5; x++) {
            for (int y = 0; y < 5; y++) {
                world.submit(new Command.SpawnAt("breeder", new GridPosition(x, y)));
            }
        }
        WorldSnapshot snapshot = world.applyPending();
        for (int i = 0; i < 16; i++) {
            snapshot = world.advance().snapshot();
        }
        int differ = 0;
        for (WorldSnapshot.TileView tile : snapshot.tiles()) {
            assertThat(tile.entities()).hasSize(2);
            double parent = world.entity(tile.entities().get(0).id())
                    .orElseThrow()
                    .genome()
                    .scalar("viewRange");
            double child = world.entity(tile.entities().get(1).id())
                    .orElseThrow()
                    .genome()
                    .scalar("viewRange");
            assertThat(child).isBetween(Math.max(1, parent - 4), Math.min(10, parent + 4));
            differ += child != parent ? 1 : 0;
        }
        assertThat(differ).isGreaterThan(5);
    }

    @Test
    void mutationIsOnByDefault() {
        World world = new World(5, 5, 1, RABBITS);
        assertThat(world.snapshot().settings().get("world")).containsEntry("mutation", 1.0);
    }

    @Test
    void withMutationOffASpawnIsTheBaseline() {
        World world = new World(10, 10, 1, RABBITS);
        world.submit(new Command.SetSetting("world", "mutation", 0));
        world.submit(new Command.Spawn("rabbit", 30));
        for (int x = 0; x < 10; x++) {
            world.submit(new Command.SpawnAt("rabbit", new GridPosition(x, 0)));
        }
        WorldSnapshot snapshot = world.applyPending();
        Genome baseline = Rabbit.species().baseline();
        int seen = 0;
        for (WorldSnapshot.TileView tile : snapshot.tiles()) {
            for (WorldSnapshot.EntityView view : tile.entities()) {
                assertThat(world.entity(view.id()).orElseThrow().genome()).isEqualTo(baseline);
                seen++;
            }
        }
        assertThat(seen).isEqualTo(40);
    }

    @Test
    void aSpawnsGeneOverridesReplaceThoseGenesOfTheBaseline() {
        World world = new World(10, 10, 1, RABBITS);
        world.submit(new Command.SetSetting("world", "mutation", 0));
        world.submit(new Command.Spawn("rabbit", 5, Map.of("grassPull", 0.2, "whim", 2.0)));
        Map<String, Double> expected = new TreeMap<>(Rabbit.species().baseline().scalars());
        expected.put("grassPull", 0.2);
        expected.put("whim", 2.0);
        List<Genome> spawned = genomes(world, world.applyPending());
        assertThat(spawned)
                .hasSize(5)
                .allSatisfy(genome -> assertThat(genome.scalars()).isEqualTo(expected));
    }

    @Test
    void mutationAppliesAfterTheOverrides() {
        World world = new World(10, 10, 1, RABBITS);
        world.submit(new Command.Spawn("rabbit", 20, Map.of("grassPull", 0.2)));
        List<Genome> spawned = genomes(world, world.applyPending());
        assertThat(spawned)
                .extracting(genome -> genome.scalar("grassPull"))
                .allSatisfy(value -> assertThat(value).isBetween(0.0, 0.4))
                .anySatisfy(value -> assertThat(value).isNotEqualTo(0.2));
    }

    @Test
    void anOverrideOfAnUndeclaredGeneOrOutOfBoundsChangesNothing() {
        World world = new World(10, 10, 1, RABBITS);
        assertThatThrownBy(() -> world.submit(new Command.Spawn("rabbit", 3, Map.of("wings", 1.0))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("wings");
        assertThatThrownBy(() -> world.submit(new Command.Spawn("rabbit", 3, Map.of("grassPull", 9.0))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("grassPull");
        assertThat(occupied(world.applyPending())).isEmpty();
        assertThat(world.log()).isEmpty();
    }

    @Test
    void anOverrideWithNoValueIsRefusedByName() {
        Map<String, Double> genes = new HashMap<>();
        genes.put("grassPull", null);
        assertThatThrownBy(() -> new Command.Spawn("rabbit", 1, genes))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("grassPull");
    }

    @Test
    void theLogCarriesTheOverrides() {
        World world = new World(10, 10, 1, RABBITS);
        world.submit(new Command.Spawn("rabbit", 2, Map.of("grassPull", 0.2)));
        world.applyPending();
        assertThat(world.log())
                .extracting(LoggedCommand::command)
                .containsExactly(new Command.Spawn("rabbit", 2, Map.of("grassPull", 0.2)));
    }

    private static List<Genome> genomes(World world, WorldSnapshot snapshot) {
        List<Genome> found = new ArrayList<>();
        for (WorldSnapshot.TileView tile : snapshot.tiles()) {
            for (WorldSnapshot.EntityView view : tile.entities()) {
                found.add(world.entity(view.id()).orElseThrow().genome());
            }
        }
        return found;
    }

    @Test
    void withMutationOffAChildCopiesItsParent() {
        World world = new World(5, 5, 1, List.of(TestSpecies.breeder(new ArrayList<>())));
        for (int x = 0; x < 5; x++) {
            for (int y = 0; y < 5; y++) {
                world.submit(new Command.SpawnAt("breeder", new GridPosition(x, y)));
            }
        }
        world.applyPending();
        world.submit(new Command.SetSetting("world", "mutation", 0));
        WorldSnapshot snapshot = world.applyPending();
        for (int i = 0; i < 16; i++) {
            snapshot = world.advance().snapshot();
        }
        Genome baseline = TestSpecies.breeder(new ArrayList<>()).baseline();
        int parentsOffBaseline = 0;
        for (WorldSnapshot.TileView tile : snapshot.tiles()) {
            assertThat(tile.entities()).hasSize(2);
            Genome parent =
                    world.entity(tile.entities().get(0).id()).orElseThrow().genome();
            Genome child =
                    world.entity(tile.entities().get(1).id()).orElseThrow().genome();
            assertThat(child).isEqualTo(parent);
            parentsOffBaseline += parent.equals(baseline) ? 0 : 1;
        }
        assertThat(parentsOffBaseline).isGreaterThan(5);
    }

    @Test
    void rabbitsBreedOnTheirOwn() {
        World world = new World(20, 20, 3, List.of(Grass.species(), Rabbit.species()), List.of(new FoodDrop("grass")));
        world.submit(new Command.Spawn("rabbit", 10));
        world.submit(new Command.Spawn("grass", 100));
        int most = 0;
        for (int day = 0; day < 60; day++) {
            most = Math.max(most, world.advance().census().population().get("rabbit"));
        }
        assertThat(most).isGreaterThan(10);
    }

    @Test
    void anIllegalBreedChangesNothing() {
        World walkers = new World(3, 3, 1, List.of(TestSpecies.walker(new Intent.Breed())));
        walkers.submit(new Command.SpawnAt("walker", new GridPosition(1, 1)));
        assertThat(walkers.advance().census().population()).containsEntry("walker", 1);

        World eager = new World(3, 3, 1, List.of(TestSpecies.eagerBreeder()));
        eager.submit(new Command.SpawnAt("breeder", new GridPosition(1, 1)));
        assertThat(eager.advance().census().population()).containsEntry("breeder", 1);
        assertThat(reserveOf(eager, 0)).isEqualTo(99);
        assertThat(eager.entity(0)
                        .orElseThrow()
                        .part(Fertility.class)
                        .orElseThrow()
                        .ownView())
                .isEqualTo(new Fertility.Since(0));
    }

    @Test
    void anEditReachesItsOwnWorldOnly() {
        World edited = new World(3, 3, 1, RABBITS);
        World other = new World(3, 3, 1, RABBITS);
        edited.submit(new Command.Spawn("rabbit", 1));
        other.submit(new Command.Spawn("rabbit", 1));
        edited.advance();
        other.advance();
        edited.submit(new Command.SetSetting("rabbit", "eatingCharge", 20));
        assertThat(edited.advance().census().population()).containsEntry("rabbit", 0);
        assertThat(other.advance().census().population()).containsEntry("rabbit", 1);
    }

    @Test
    void theFoodDropReadsItsWorldsValues() {
        World world = new World(10, 10, 1, List.of(TestSpecies.food()), List.of(new FoodDrop("food")));
        world.submit(new Command.SetSetting("world", "foodDropInterval", 2));
        world.submit(new Command.SetSetting("world", "foodPerDrop", 3));
        world.advance();
        assertThat(world.advance().census().population()).containsEntry("food", 3);
    }

    @Test
    void declarationsListTheWorldThenEachSpeciesByName() {
        World world = new World(3, 3, 1, List.of(Rabbit.species(), Grass.species()), List.of(new FoodDrop("grass")));
        assertThat(world.declarations())
                .extracting(SettingDeclaration::owner)
                .startsWith("world", "world", "world", "grass", "grass", "rabbit");
    }

    @Test
    void aBadSettingCommandChangesNothing() {
        World world = new World(3, 3, 1, RABBITS);
        WorldSnapshot before = world.applyPending();
        assertThatThrownBy(() -> world.submit(new Command.SetSetting("rabbit", "stride", 9)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("stride");
        assertThatThrownBy(() -> world.submit(new Command.SetSetting("wolf", "stride", 2)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("wolf");
        assertThatThrownBy(() -> world.submit(new Command.RestoreDefaults(Optional.of("rabbit"), Optional.of("wings"))))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Command.RestoreDefaults(Optional.empty(), Optional.of("stride")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("stride");
        assertThat(world.applyPending()).isEqualTo(before);
        assertThat(world.log()).isEmpty();
    }

    @Test
    void theSnapshotCarriesTheCurrentValues() {
        World world = new World(3, 3, 1, List.of(Grass.species(), Rabbit.species()), List.of(new FoodDrop("grass")));
        world.submit(new Command.SetSetting("rabbit", "stride", 2));
        WorldSnapshot snapshot = world.applyPending();
        assertThat(snapshot.settings()).containsOnlyKeys("grass", "rabbit", "world");
        assertThat(snapshot.settings().get("rabbit")).containsEntry("stride", 2.0);
        assertThat(snapshot.settings().get("world")).containsEntry("grassPerDrop", 7.0);
    }

    @Test
    void restoreDefaultsPutsTheDeclaredValuesBack() {
        World world = new World(3, 3, 1, RABBITS);
        world.submit(new Command.SetSetting("rabbit", "stride", 2));
        world.submit(new Command.SetSetting("rabbit", "eatingCharge", 2));
        world.submit(new Command.RestoreDefaults(Optional.of("rabbit"), Optional.of("stride")));
        assertThat(world.applyPending().settings().get("rabbit"))
                .containsEntry("stride", 3.0)
                .containsEntry("eatingCharge", 2.0);
        world.submit(new Command.RestoreDefaults(Optional.empty(), Optional.empty()));
        assertThat(world.applyPending().settings().get("rabbit")).containsEntry("eatingCharge", 1.0);
    }

    @Test
    void aResetKeepsEditedSettingsAndLogsThem() {
        World world = new World(3, 3, 1, List.of(Grass.species(), Rabbit.species()), List.of(new FoodDrop("grass")));
        world.submit(new Command.SetSetting("rabbit", "stride", 2));
        world.submit(new Command.SetSetting("world", "grassPerDrop", 3));
        world.submit(new Command.Reset(7));
        assertThat(world.applyPending().settings().get("rabbit")).containsEntry("stride", 2.0);
        assertThat(world.log())
                .containsExactly(
                        new LoggedCommand(0, new Command.Reset(7)),
                        new LoggedCommand(0, new Command.SetSetting("world", "grassPerDrop", 3)),
                        new LoggedCommand(0, new Command.SetSetting("rabbit", "stride", 2)));
    }

    @Test
    void eachBrainIsHandedWhatItsSenseSeesAndTheGridsDirections() {
        List<Perception> perceptions = new ArrayList<>();
        List<Options> options = new ArrayList<>();
        World world = new World(5, 5, 1, List.of(TestSpecies.recorder(perceptions, options)));
        world.submit(new Command.SpawnAt("recorder", new GridPosition(2, 2)));
        world.submit(new Command.SpawnAt("recorder", new GridPosition(3, 2)));
        world.advance();

        assertThat(perceptions).extracting(perception -> perception.self().id()).containsExactly(0, 1);
        Perception first = perceptions.getFirst();
        assertThat(first.tiles()).hasSize(5);
        assertThat(first.tiles())
                .filteredOn(tile -> tile.dx() == 1 && tile.dy() == 0)
                .singleElement()
                .satisfies(tile -> assertThat(tile.entities())
                        .extracting(Perception.EntityView::id)
                        .containsExactly(1));
        assertThat(options)
                .allSatisfy(offered -> assertThat(offered.directions())
                        .extracting(Direction::name)
                        .containsExactly("north", "east", "south", "west"));
    }

    @Test
    void eachBrainIsOfferedIdlingThenItsMoves() {
        List<Perception> perceptions = new ArrayList<>();
        List<Options> options = new ArrayList<>();
        World world = new World(5, 5, 1, List.of(TestSpecies.recorder(perceptions, options)));
        world.submit(new Command.SpawnAt("recorder", new GridPosition(0, 0)));
        world.advance();

        List<Option> offered = options.getFirst().offered();
        assertThat(offered.getFirst()).isEqualTo(new Option.Idle());
        assertThat(offered).hasSize(7);
        assertThat(offered.get(3)).isEqualTo(new Option.Move(TestSpecies.EAST, 3, 3, 0));
    }

    @Test
    void theDaysOrderIsAPermutationShuffledFromIdOrder() {
        List<Decision> byId = IntStream.range(0, 10)
                .mapToObj(id -> new Decision(id, new Intent.Idle()))
                .toList();
        int stillInIdOrder = 0;
        for (long seed = 0; seed < 20; seed++) {
            List<Decision> order = World.dayOrder(
                    byId,
                    id -> OptionalDouble.empty(),
                    RandomGeneratorFactory.of("L64X128MixRandom").create(seed));
            assertThat(order).containsExactlyInAnyOrderElementsOf(byId);
            assertThat(order)
                    .isEqualTo(World.dayOrder(
                            byId,
                            id -> OptionalDouble.empty(),
                            RandomGeneratorFactory.of("L64X128MixRandom").create(seed)));
            if (order.equals(byId)) {
                stillInIdOrder++;
            }
        }
        assertThat(stillInIdOrder).isZero();
    }

    private static List<Decision> idle(int count) {
        return IntStream.range(0, count)
                .mapToObj(id -> new Decision(id, new Intent.Idle()))
                .toList();
    }

    @Test
    void theFasterActFirst() {
        List<Decision> order = World.dayOrder(
                idle(6),
                id -> OptionalDouble.of(id * 0.1),
                RandomGeneratorFactory.of("L64X128MixRandom").create(1));
        assertThat(order).extracting(Decision::entityId).containsExactly(5, 4, 3, 2, 1, 0);
    }

    @Test
    void equalSpeedsAreDrawnAtRandom() {
        int stillInIdOrder = 0;
        for (long seed = 0; seed < 20; seed++) {
            List<Decision> order = World.dayOrder(
                    idle(10),
                    id -> OptionalDouble.of(0.5),
                    RandomGeneratorFactory.of("L64X128MixRandom").create(seed));
            stillInIdOrder += order.equals(idle(10)) ? 1 : 0;
        }
        assertThat(stillInIdOrder).isZero();
    }

    @Test
    void withoutASpeedGeneActsLast() {
        List<Decision> order = World.dayOrder(
                idle(4),
                id -> id >= 2 ? OptionalDouble.of(id - 2) : OptionalDouble.empty(),
                RandomGeneratorFactory.of("L64X128MixRandom").create(3));
        assertThat(order).extracting(Decision::entityId).startsWith(3, 2);
        assertThat(order.subList(2, 4)).extracting(Decision::entityId).containsExactlyInAnyOrder(0, 1);
    }

    @Test
    void removingAnUnknownEntityChangesNothing() {
        World world = new World(5, 5, 1, RABBITS);
        world.submit(new Command.Spawn("rabbit", 2));
        WorldSnapshot before = world.applyPending();
        assertThatThrownBy(() -> world.remove(7, World.EATEN))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("7");
        assertThat(world.snapshot()).isEqualTo(before);
    }

    /** Ends its carrier on its first tick, naming its cause, and records that it was ticked. */
    private record Ends(String cause, List<String> ticked) implements Component {
        @Override
        public Class<? extends Component> key() {
            return Ends.class;
        }

        @Override
        public Record ownView() {
            return this;
        }

        @Override
        public Optional<Record> seenView() {
            return Optional.empty();
        }

        @Override
        public Optional<String> tick() {
            ticked.add(cause);
            return Optional.of(cause);
        }
    }

    @Test
    void theSnapshotCarriesEachEntitysOwnGenes() {
        World world = new World(5, 5, 1, List.of(Grass.species(), Rabbit.species()));
        world.submit(new Command.SetSetting("world", "mutation", 0));
        world.submit(new Command.Spawn("rabbit", 1, Map.of("grassPull", 0.2)));
        world.submit(new Command.Spawn("grass", 1));
        List<WorldSnapshot.EntityView> views = new ArrayList<>();
        world.applyPending().tiles().forEach(tile -> views.addAll(tile.entities()));
        WorldSnapshot.EntityView rabbit = views.stream()
                .filter(view -> view.species().equals("rabbit"))
                .findFirst()
                .orElseThrow();
        WorldSnapshot.EntityView grass = views.stream()
                .filter(view -> view.species().equals("grass"))
                .findFirst()
                .orElseThrow();
        assertThat(rabbit.genes())
                .isEqualTo(Rabbit.species()
                        .baseline()
                        .with(Map.of("grassPull", 0.2))
                        .scalars())
                .containsEntry("grassPull", 0.2);
        assertThat(grass.genes()).isEmpty();
    }

    /** The eater at 0,0 pays its 1 on day 1 and starves on day 2; the one at 1,1 eats and lives on. */
    @Test
    void theCensusCountsTheDaysDeathsByCause() {
        World world = new World(3, 3, 1, List.of(TestSpecies.eater(1, 1), TestSpecies.food()));
        world.submit(new Command.SpawnAt("eater", new GridPosition(0, 0)));
        world.submit(new Command.SpawnAt("eater", new GridPosition(1, 1)));
        world.submit(new Command.SpawnAt("food", new GridPosition(1, 1)));
        assertThat(world.advance().census().deaths()).isEqualTo(Map.of("food", Map.of("eaten", 1)));
        assertThat(world.advance().census().deaths()).isEqualTo(Map.of("eater", Map.of("starved", 1)));
        assertThat(world.advance().census().deaths()).isEmpty();
    }

    /** A grass spawned on day 0 is there for twelve feeds and withers in the twelfth Tick. */
    @Test
    void grassLeftUneatenForItsLifespanWithers() {
        World world = new World(3, 3, 1, List.of(Grass.species()));
        world.submit(new Command.SpawnAt("grass", new GridPosition(1, 1)));
        for (int day = 1; day < 12; day++) {
            assertThat(world.advance().census().population()).containsEntry("grass", 1);
        }
        CensusRow twelfth = world.advance().census();
        assertThat(twelfth.population()).containsEntry("grass", 0);
        assertThat(twelfth.deaths()).isEqualTo(Map.of("grass", Map.of("aged", 1)));
    }

    /** The eater starts with 0, eats 15 on the grass's last day and pays 1, so feeding comes before the Tick. */
    @Test
    void grassEatenOnItsLastDayFeedsAndDoesNotWither() {
        World world = new World(3, 3, 1, List.of(TestSpecies.eater(1, 0), Grass.species()));
        world.submit(new Command.SpawnAt("grass", new GridPosition(1, 1)));
        for (int day = 1; day < 12; day++) {
            world.advance();
        }
        world.submit(new Command.SpawnAt("eater", new GridPosition(1, 1)));
        CensusRow twelfth = world.advance().census();
        assertThat(twelfth.deaths()).isEqualTo(Map.of("grass", Map.of("eaten", 1)));
        assertThat(world.entity(1).flatMap(eater -> eater.part(Metabolism.class)))
                .map(Metabolism::reserve)
                .contains(14.0);
    }

    @Test
    void onlyTheFirstPartToNameACauseEndsItsCarrier() {
        List<String> ticked = new ArrayList<>();
        Species doomed = new Species(
                "doomed",
                "doomed",
                List.of(),
                new Genome(List.of(), Map.of()),
                Settings.NONE,
                Optional.empty(),
                List.of(spawn -> new Ends("first", ticked), spawn -> new Ends("second", ticked)));
        World world = new World(3, 3, 1, List.of(doomed));
        world.submit(new Command.Spawn("doomed", 1));
        CensusRow row = world.advance().census();
        assertThat(row.population()).containsEntry("doomed", 0);
        assertThat(row.deaths()).isEqualTo(Map.of("doomed", Map.of("first", 1)));
        assertThat(ticked).containsExactly("first");
    }

    @Test
    void aCreatureThatNeverEatsStarves() {
        World world = new World(3, 3, 1, List.of(TestSpecies.eater(1, 2)));
        world.submit(new Command.Spawn("eater", 1));
        assertThat(world.advance().census().population()).containsEntry("eater", 1);
        assertThat(world.advance().census().population()).containsEntry("eater", 1);
        assertThat(world.advance().census().population()).containsEntry("eater", 0);
    }

    @Test
    void theChargeFallsDueOnEachCreaturesOwnClock() {
        World world = new World(3, 3, 1, List.of(TestSpecies.eater(3, 0)));
        world.submit(new Command.Spawn("eater", 1));
        world.advance();
        world.submit(new Command.Spawn("eater", 1));
        world.advance();
        assertThat(world.advance().census().population()).containsEntry("eater", 1);
        assertThat(world.advance().census().population()).containsEntry("eater", 0);
    }

    @Test
    void feedingTakesEveryFoodTheEaterTakes() {
        World world = new World(3, 3, 1, List.of(TestSpecies.eater(1, 0), TestSpecies.food()));
        world.submit(new Command.SpawnAt("eater", new GridPosition(1, 1)));
        world.submit(new Command.SpawnAt("food", new GridPosition(1, 1)));
        world.submit(new Command.SpawnAt("food", new GridPosition(1, 1)));
        DayReport first = world.advance();
        assertThat(first.census().population()).containsEntry("food", 0).containsEntry("eater", 1);
        for (int day = 2; day <= 10; day++) {
            assertThat(world.advance().census().population()).containsEntry("eater", 1);
        }
        assertThat(world.advance().census().population()).containsEntry("eater", 0);
    }

    @Test
    void aLaterFeederFindsNothing() {
        World world = new World(3, 3, 1, List.of(TestSpecies.eater(1, 0), TestSpecies.food()));
        world.submit(new Command.SpawnAt("eater", new GridPosition(1, 1)));
        world.submit(new Command.SpawnAt("eater", new GridPosition(1, 1)));
        world.submit(new Command.SpawnAt("food", new GridPosition(1, 1)));
        assertThat(world.advance().census().population())
                .containsEntry("eater", 1)
                .containsEntry("food", 0);
    }

    @Test
    void theFirstInTheDaysOrderEats() {
        World world = new World(3, 3, 1, List.of(TestSpecies.eater(1, 0), TestSpecies.food()));
        world.submit(new Command.SpawnAt("eater", new GridPosition(1, 1)));
        world.submit(new Command.SpawnAt("eater", new GridPosition(1, 1)));
        world.submit(new Command.SpawnAt("food", new GridPosition(1, 1)));
        world.applyPending();
        world.feed(List.of(new Decision(1, new Intent.Idle()), new Decision(0, new Intent.Idle())));
        assertThat(world.advance().snapshot().tiles().get(4).entities())
                .extracting(WorldSnapshot.EntityView::id)
                .containsExactly(1);
    }

    @Test
    void anEaterTakesOnlyTheFoodsItNames() {
        World world = new World(3, 3, 1, List.of(TestSpecies.eater(1, 0), TestSpecies.food(), TestSpecies.bone()));
        world.submit(new Command.SpawnAt("eater", new GridPosition(1, 1)));
        world.submit(new Command.SpawnAt("food", new GridPosition(1, 1)));
        world.submit(new Command.SpawnAt("bone", new GridPosition(1, 1)));
        assertThat(world.advance().census().population())
                .containsEntry("eater", 1)
                .containsEntry("food", 0)
                .containsEntry("bone", 1);
    }

    @Test
    void grassAnEaterRefusesIsLeftForOneThatTakesIt() {
        World world = new World(
                3,
                3,
                1,
                List.of(
                        TestSpecies.eaterTaking("picky", Set.of(TestSpecies.Bone.class)),
                        TestSpecies.eaterTaking("grazer", Set.of(Plant.class)),
                        TestSpecies.food()));
        world.submit(new Command.SpawnAt("picky", new GridPosition(1, 1)));
        world.submit(new Command.SpawnAt("grazer", new GridPosition(1, 1)));
        world.submit(new Command.SpawnAt("food", new GridPosition(1, 1)));
        world.applyPending();
        world.feed(List.of(new Decision(0, new Intent.Idle()), new Decision(1, new Intent.Idle())));
        assertThat(world.advance().census().population())
                .containsEntry("picky", 0)
                .containsEntry("grazer", 1)
                .containsEntry("food", 0);
    }

    @Test
    void aCreatureWithNoEaterEatsNothing() {
        World world = new World(3, 3, 1, List.of(TestSpecies.unfed(), TestSpecies.food()));
        world.submit(new Command.SpawnAt("unfed", new GridPosition(1, 1)));
        world.submit(new Command.SpawnAt("food", new GridPosition(1, 1)));
        assertThat(world.advance().census().population())
                .containsEntry("unfed", 0)
                .containsEntry("food", 1);
    }

    @Test
    void foodDropsOnTheDaysCompletingItsInterval() {
        World world = new World(20, 20, 1, List.of(TestSpecies.food()), List.of(new FoodDrop("food")));
        for (int day = 1; day < 7; day++) {
            assertThat(world.advance().census().population()).containsEntry("food", 0);
        }
        assertThat(world.advance().census().population()).containsEntry("food", 7);
        for (int day = 8; day < 14; day++) {
            world.advance();
        }
        assertThat(world.advance().census().population()).containsEntry("food", 14);
    }

    @Test
    void aWorldWithoutProcessesDropsNothing() {
        World world = new World(5, 5, 1, List.of(TestSpecies.food()));
        for (int day = 0; day < 14; day++) {
            assertThat(world.advance().census().population()).containsEntry("food", 0);
        }
    }

    @Test
    void aDropOfAnUnknownSpeciesIsRejectedOnBuilding() {
        assertThatThrownBy(() -> new World(5, 5, 1, RABBITS, List.of(new FoodDrop("grass"))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("grass");
    }

    @Test
    void theSnapshotMarksCreaturesAndNamesItsSpecies() {
        World world = new World(3, 3, 1, List.of(Rabbit.species(), Grass.species()));
        world.submit(new Command.SpawnAt("grass", new GridPosition(1, 1)));
        world.submit(new Command.SpawnAt("rabbit", new GridPosition(1, 1)));
        WorldSnapshot snapshot = world.applyPending();
        assertThat(snapshot.species()).containsExactly("grass", "rabbit");
        assertThat(snapshot.tiles().get(4).entities())
                .extracting(WorldSnapshot.EntityView::species, WorldSnapshot.EntityView::creature)
                .containsExactly(tuple("grass", false), tuple("rabbit", true));
    }
}
