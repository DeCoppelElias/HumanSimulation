package io.github.decoppelelias.humansimulation.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
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

    private record Seen(int dx, int dy, List<Feature> features) {}

    private static Seen grass(int dx, int dy) {
        return new Seen(dx, dy, List.of(Features.PLANT));
    }

    private static Seen rabbit(int dx, int dy) {
        return new Seen(dx, dy, List.of(Features.MEAT, Features.TAKES_PLANT));
    }

    private static Seen meat(int dx, int dy) {
        return new Seen(dx, dy, List.of(Features.MEAT));
    }

    private static Perception perception(int daysLeft, Seen... seen) {
        List<Perception.TileView> tiles = new ArrayList<>();
        int id = 1;
        for (Seen one : seen) {
            ViewParts parts = new ViewParts(List.of(), List.of(), one.features());
            tiles.add(new Perception.TileView(
                    one.dx(), one.dy(), Map.of(), List.of(new Perception.EntityView(id++, "seen", parts))));
        }
        ViewParts self =
                new ViewParts(List.of(Metabolism.class), List.of(new Metabolism.Own(daysLeft, daysLeft)), List.of());
        return new Perception(new Perception.SelfView(0, self), tiles, Grid::distance);
    }

    /** A brain whose pulls are zero except those named. */
    private static ScoringBrain brain(Map<Feature, ScoringBrain.Pull> named, double whim, double breedPull) {
        Map<Feature, ScoringBrain.Pull> pulls = new LinkedHashMap<>();
        for (Feature feature : Features.ALL) {
            pulls.put(feature, named.getOrDefault(feature, new ScoringBrain.Pull(0, 0, 0)));
        }
        return new ScoringBrain(pulls, whim, breedPull);
    }

    /** Pulled toward the nearest plant, with hunger, and by closeness to everything carrying meat. */
    private static ScoringBrain grazer(double near, double hungry, double crowd, double whim, double breedPull) {
        return brain(
                Map.of(
                        Features.PLANT, new ScoringBrain.Pull(near, hungry, 0),
                        Features.MEAT, new ScoringBrain.Pull(0, 0, crowd)),
                whim,
                breedPull);
    }

    private static ScoringBrain grazer(double near, double hungry, double crowd, double whim) {
        return grazer(near, hungry, crowd, whim, 0);
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
        Map<Intent, Integer> counts = picks(grazer(1, 2, 0, 0.5, 1), perception(15), idleOrBreed, 4000);
        double breeding = counts.getOrDefault(new Intent.Breed(), 0) / 4000.0;
        assertThat(breeding).isBetween(0.85, 0.91);
    }

    @Test
    void farFromGrassItWalksRatherThanBreeds() {
        assertThat(picks(grazer(1, 0, 0, 0.05, 1), perception(50, grass(6, 0)), openGround(true), 200))
                .containsOnlyKeys(new Intent.Move(EAST, 3));
    }

    @Test
    void onGrassWithNothingToGainByWalkingItBreeds() {
        assertThat(picks(grazer(1, 0, 0, 0.05, 1), perception(50, grass(0, 0)), openGround(true), 200))
                .containsOnlyKeys(new Intent.Breed());
    }

    @Test
    void landsOnGrassWithinReach() {
        assertThat(picks(grazer(1, 0, 0, 0.05), perception(15, grass(2, 0)), 200))
                .containsOnlyKeys(new Intent.Move(EAST, 2));
    }

    @Test
    void closesOnGrassBeyondReach() {
        assertThat(picks(grazer(1, 0, 0, 0.05), perception(15, grass(5, 0)), 200))
                .containsOnlyKeys(new Intent.Move(EAST, 3));
    }

    @Test
    void aFeatureWithNoPullDoesNotPull() {
        ScoringBrain plantOnly = brain(Map.of(Features.PLANT, new ScoringBrain.Pull(1, 0, 0)), 0.05, 0);
        assertThat(picks(plantOnly, perception(15, meat(2, 0)), 200).getOrDefault(new Intent.Move(EAST, 2), 0))
                .isLessThan(50);
    }

    @Test
    void aPullOnMeatWalksToMeat() {
        ScoringBrain hunter = brain(Map.of(Features.MEAT, new ScoringBrain.Pull(1, 0, 0)), 0.05, 0);
        assertThat(picks(hunter, perception(15, meat(2, 0), grass(0, 2)), 200))
                .containsOnlyKeys(new Intent.Move(EAST, 2));
    }

    @Test
    void aNegativeGrassPullWalksAwayFromGrass() {
        assertThat(picks(grazer(-1, 0, 0, 0.05), perception(15, grass(1, 0)), 200))
                .containsOnlyKeys(new Intent.Move(WEST, 3));
    }

    @Test
    void aNegativeCrowdPullPrefersTheLandingFartherFromRabbits() {
        assertThat(picks(grazer(1, 0, -2, 0.005), perception(15, rabbit(0, -1)), 200))
                .containsOnlyKeys(new Intent.Move(SOUTH, 3));
    }

    @Test
    void aPositiveCrowdPullPrefersTheLandingNearerRabbits() {
        assertThat(picks(grazer(1, 0, 2, 0.05), perception(15, rabbit(0, -1)), 200))
                .containsOnlyKeys(new Intent.Move(NORTH, 1));
    }

    /** Rejects a summed shape that counts a tile once, or a nearest shape that adds up every entity. */
    @Test
    void theSummedPullCountsEveryoneShowingTheFeature() {
        ScoringBrain crowd = brain(Map.of(Features.MEAT, new ScoringBrain.Pull(0, 0, -1)), 0.5, 0);
        ScoringBrain nearest = brain(Map.of(Features.MEAT, new ScoringBrain.Pull(1, 0, 0)), 0.5, 0);
        Options idle = new Options(DIRECTIONS, List.of(new Option.Idle()));
        Perception one = perception(15, meat(0, -1));
        Perception two = perception(15, meat(0, -1), meat(0, -1));
        assertThat(crowd.scores(two, idle)[0]).isEqualTo(2 * crowd.scores(one, idle)[0]);
        assertThat(nearest.scores(two, idle)[0]).isEqualTo(nearest.scores(one, idle)[0]);
    }

    @Test
    void hungerSharpensThePullOfGrass() {
        ScoringBrain loafer = grazer(0, 2, 0, 0.5);
        Intent ontoGrass = new Intent.Move(EAST, 2);
        double fed = picks(loafer, perception(14, grass(2, 0)), 2000).getOrDefault(ontoGrass, 0) / 2000.0;
        double starving = picks(loafer, perception(0, grass(2, 0)), 2000).getOrDefault(ontoGrass, 0) / 2000.0;
        assertThat(fed).isBetween(0.08, 0.25);
        assertThat(starving).isGreaterThan(0.9);
    }

    @Test
    void withNothingInViewEveryLandingIsEquallyLikely() {
        Map<Intent, Integer> counts = picks(grazer(1, 2, -0.2, 0.5), perception(15), 13_000);
        assertThat(counts).hasSize(13);
        assertThat(counts.values()).allSatisfy(count -> assertThat(count).isBetween(850, 1150));
    }

    @Test
    void equalLandingsSplit() {
        Map<Intent, Integer> counts = picks(grazer(1, 0, 0, 0.05), perception(15, grass(2, 0), grass(-2, 0)), 2000);
        assertThat(counts).containsOnlyKeys(new Intent.Move(EAST, 2), new Intent.Move(WEST, 2));
        assertThat(counts.get(new Intent.Move(EAST, 2))).isBetween(850, 1150);
    }

    @Test
    void aSeedReplaysThePick() {
        ScoringBrain brain = grazer(1, 2, -0.2, 0.5);
        Perception seen = perception(5, grass(2, 1), rabbit(1, -2));
        for (long seed = 0; seed < 50; seed++) {
            assertThat(brain.decide(seen, openGround(true), seeded(seed)))
                    .isEqualTo(brain.decide(seen, openGround(true), seeded(seed)));
        }
    }

    /**
     * The brain before features, written out with its terms in their old order, so the equality is exact. Rejects a
     * crowd pull on the nearest shape, hunger on the summed one, or a baseline that moved.
     */
    @Test
    void atTheRabbitsBaselinesItScoresAsTheBrainBeforeFeatures() {
        ScoringBrain baseline = ScoringBrain.from(Rabbit.species().baseline());
        Perception seen = perception(15, grass(2, 0), rabbit(0, 1));
        double weight = 1 + 2 * (1.0 / (1 + 15));
        List<Option> offered = openGround(false).offered();
        double[] expected = new double[offered.size()];
        for (int i = 0; i < offered.size(); i++) {
            int dx = offered.get(i) instanceof Option.Move move ? move.dx() : 0;
            int dy = offered.get(i) instanceof Option.Move move ? move.dy() : 0;
            double score = 0;
            score -= weight * Math.hypot(2 - dx, 0 - dy);
            score += -0.2 / (1 + Math.hypot(0 - dx, 1 - dy));
            expected[i] = score;
        }
        assertThat(baseline.scores(seen, openGround(false))).containsExactly(expected);
    }
}
