package io.github.decoppelelias.humansimulation.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;
import org.junit.jupiter.api.Test;

class ScoringBrainTest {
    private static final List<Direction> DIRECTIONS = new Grid(1, 1).directions();
    private static final Direction NORTH = DIRECTIONS.getFirst();
    private static final Direction EAST = DIRECTIONS.get(1);
    private static final Direction SOUTH = DIRECTIONS.get(2);
    private static final Direction WEST = DIRECTIONS.get(3);

    /** Idling and every move within a stride of 3, with no edge in the way, and breeding when asked for. */
    private static Options openGround(boolean breed) {
        List<Option> offered = new ArrayList<>();
        offered.add(new Option.Idle());
        for (Direction direction : DIRECTIONS) {
            for (int distance = 1; distance <= 3; distance++) {
                offered.add(new Option.Move(direction, distance, direction.dx() * distance, direction.dy() * distance));
            }
        }
        if (breed) {
            offered.add(new Option.Breed());
        }
        return new Options(DIRECTIONS, offered);
    }

    private record Seen(int dx, int dy, String species, List<Class<? extends Component>> food) {}

    private static Seen grass(int dx, int dy) {
        return new Seen(dx, dy, "grass", List.of(Plant.class));
    }

    private static Seen bone(int dx, int dy) {
        return new Seen(dx, dy, "bone", List.of(TestSpecies.Bone.class));
    }

    private static Seen rabbit(int dx, int dy) {
        return new Seen(dx, dy, "rabbit", List.of());
    }

    private static Perception perception(int daysLeft, Seen... seen) {
        List<Perception.TileView> tiles = new ArrayList<>();
        int id = 1;
        for (Seen one : seen) {
            List<Record> views = one.food().stream()
                    .<Record>map(food -> new Edible.Amount(15))
                    .toList();
            ViewParts parts = new ViewParts(one.food(), views, List.of());
            tiles.add(new Perception.TileView(
                    one.dx(), one.dy(), Map.of(), List.of(new Perception.EntityView(id++, one.species(), parts))));
        }
        ViewParts self =
                new ViewParts(List.of(Metabolism.class), List.of(new Metabolism.Own(daysLeft, daysLeft)), List.of());
        return new Perception(new Perception.SelfView(0, self), tiles, Grid::distance);
    }

    private static ScoringBrain brain(double grassPull, double hungerDrive, double rabbitPull, double whim) {
        return brain(grassPull, hungerDrive, rabbitPull, whim, 0);
    }

    private static ScoringBrain brain(
            double grassPull, double hungerDrive, double rabbitPull, double whim, double breedPull) {
        return new ScoringBrain("rabbit", grassPull, hungerDrive, rabbitPull, whim, breedPull);
    }

    private static RandomGenerator seeded(long seed) {
        return RandomGeneratorFactory.of("L64X128MixRandom").create(seed);
    }

    private static Map<Intent, Integer> picks(ScoringBrain brain, Perception perception, int draws) {
        return picks(brain, perception, openGround(false), draws);
    }

    private static Map<Intent, Integer> picks(ScoringBrain brain, Perception perception, Options options, int draws) {
        Map<Intent, Integer> counts = new HashMap<>();
        RandomGenerator random = seeded(1);
        for (int i = 0; i < draws; i++) {
            counts.merge(brain.decide(perception, options, random), 1, Integer::sum);
        }
        return counts;
    }

    @Test
    void breedingScoresAsIdlingPlusBreedPull() {
        Options idleOrBreed = new Options(DIRECTIONS, List.of(new Option.Idle(), new Option.Breed()));
        Map<Intent, Integer> counts = picks(brain(1, 2, 0, 0.5, 1), perception(15), idleOrBreed, 4000);
        double breeding = counts.getOrDefault(new Intent.Breed(), 0) / 4000.0;
        assertThat(breeding).isBetween(0.85, 0.91);
    }

    @Test
    void farFromGrassItWalksRatherThanBreeds() {
        assertThat(picks(brain(1, 0, 0, 0.05, 1), perception(50, grass(6, 0)), openGround(true), 200))
                .containsOnlyKeys(new Intent.Move(EAST, 3));
    }

    @Test
    void onGrassWithNothingToGainByWalkingItBreeds() {
        assertThat(picks(brain(1, 0, 0, 0.05, 1), perception(50, grass(0, 0)), openGround(true), 200))
                .containsOnlyKeys(new Intent.Breed());
    }

    @Test
    void landsOnGrassWithinReach() {
        assertThat(picks(brain(1, 0, 0, 0.05), perception(15, grass(2, 0)), 200))
                .containsOnlyKeys(new Intent.Move(EAST, 2));
    }

    @Test
    void closesOnGrassBeyondReach() {
        assertThat(picks(brain(1, 0, 0, 0.05), perception(15, grass(5, 0)), 200))
                .containsOnlyKeys(new Intent.Move(EAST, 3));
    }

    @Test
    void aSeenFoodThatIsNotAPlantDoesNotPull() {
        assertThat(picks(brain(1, 0, 0, 0.05), perception(15, bone(2, 0)), 200)
                        .getOrDefault(new Intent.Move(EAST, 2), 0))
                .isLessThan(50);
    }

    @Test
    void aNegativeGrassPullWalksAwayFromGrass() {
        assertThat(picks(brain(-1, 0, 0, 0.05), perception(15, grass(1, 0)), 200))
                .containsOnlyKeys(new Intent.Move(WEST, 3));
    }

    @Test
    void aNegativeRabbitPullPrefersTheLandingFartherFromRabbits() {
        assertThat(picks(brain(1, 0, -2, 0.005), perception(15, rabbit(0, -1)), 200))
                .containsOnlyKeys(new Intent.Move(SOUTH, 3));
    }

    @Test
    void aPositiveRabbitPullPrefersTheLandingNearerRabbits() {
        assertThat(picks(brain(1, 0, 2, 0.05), perception(15, rabbit(0, -1)), 200))
                .containsOnlyKeys(new Intent.Move(NORTH, 1));
    }

    @Test
    void hungerSharpensThePullOfGrass() {
        ScoringBrain loafer = brain(0, 2, 0, 0.5);
        Intent ontoGrass = new Intent.Move(EAST, 2);
        double fed = picks(loafer, perception(14, grass(2, 0)), 2000).getOrDefault(ontoGrass, 0) / 2000.0;
        double starving = picks(loafer, perception(0, grass(2, 0)), 2000).getOrDefault(ontoGrass, 0) / 2000.0;
        assertThat(fed).isBetween(0.08, 0.25);
        assertThat(starving).isGreaterThan(0.9);
    }

    @Test
    void withNothingInViewEveryLandingIsEquallyLikely() {
        Map<Intent, Integer> counts = picks(brain(1, 2, -0.2, 0.5), perception(15), 13_000);
        assertThat(counts).hasSize(13);
        assertThat(counts.values()).allSatisfy(count -> assertThat(count).isBetween(850, 1150));
    }

    @Test
    void equalLandingsSplit() {
        Map<Intent, Integer> counts = picks(brain(1, 0, 0, 0.05), perception(15, grass(2, 0), grass(-2, 0)), 2000);
        assertThat(counts).containsOnlyKeys(new Intent.Move(EAST, 2), new Intent.Move(WEST, 2));
        assertThat(counts.get(new Intent.Move(EAST, 2))).isBetween(850, 1150);
    }

    @Test
    void aSeedReplaysThePick() {
        ScoringBrain brain = brain(1, 2, -0.2, 0.5);
        Perception seen = perception(5, grass(2, 1), rabbit(1, -2));
        for (long seed = 0; seed < 50; seed++) {
            assertThat(brain.decide(seen, openGround(true), seeded(seed)))
                    .isEqualTo(brain.decide(seen, openGround(true), seeded(seed)));
        }
    }
}
