package io.github.decoppelelias.humansimulation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;
import org.junit.jupiter.api.Test;

class RandomBrainTest {
    private static final Options FOUR_WAYS = new Options(new Grid(3, 3).directions());
    private static final Perception NOTHING =
            new Perception(new Perception.SelfView(0, ViewParts.own(List.of())), List.of());
    private static final int DRAWS = 10_000;

    private static RandomGenerator seeded() {
        return RandomGeneratorFactory.of("L64X128MixRandom").create(1);
    }

    @Test
    void idlesOneTimeInFive() {
        RandomBrain brain = new RandomBrain(List.of(0.6, 0.3, 0.1));
        RandomGenerator random = seeded();
        int idle = 0;
        for (int i = 0; i < DRAWS; i++) {
            if (brain.decide(NOTHING, FOUR_WAYS, random) instanceof Intent.Idle) {
                idle++;
            }
        }
        assertThat(idle).isBetween(1_850, 2_150);
    }

    @Test
    void picksEveryDirectionItIsOfferedEquallyOften() {
        RandomBrain brain = new RandomBrain(List.of(1.0));
        RandomGenerator random = seeded();
        Map<String, Integer> moves = new TreeMap<>();
        for (int i = 0; i < DRAWS; i++) {
            if (brain.decide(NOTHING, FOUR_WAYS, random) instanceof Intent.Move move) {
                moves.merge(move.direction().name(), 1, Integer::sum);
            }
        }
        assertThat(moves).containsOnlyKeys("north", "east", "south", "west");
        assertThat(moves.values()).allSatisfy(count -> assertThat(count).isBetween(1_850, 2_150));
    }

    @Test
    void neverWalksOffTheStepDistribution() {
        RandomGenerator random = seeded();
        for (List<Double> steps : List.of(List.of(1.0), List.of(0.5, 0.5), List.of(0.6, 0.3, 0.1))) {
            RandomBrain brain = new RandomBrain(steps);
            for (int i = 0; i < DRAWS; i++) {
                if (brain.decide(NOTHING, FOUR_WAYS, random) instanceof Intent.Move move) {
                    assertThat(move.distance()).isBetween(1, steps.size());
                }
            }
        }
    }

    @Test
    void drawsDistancesInProportionToTheStepDistribution() {
        RandomBrain brain = new RandomBrain(List.of(0.6, 0.3, 0.1));
        RandomGenerator random = seeded();
        int[] byDistance = new int[4];
        int moves = 0;
        for (int i = 0; i < DRAWS; i++) {
            if (brain.decide(NOTHING, FOUR_WAYS, random) instanceof Intent.Move move) {
                byDistance[move.distance()]++;
                moves++;
            }
        }
        assertThat(byDistance[1] / (double) moves).isBetween(0.57, 0.63);
        assertThat(byDistance[2] / (double) moves).isBetween(0.27, 0.33);
        assertThat(byDistance[3] / (double) moves).isBetween(0.08, 0.12);
    }

    @Test
    void idlesWhenOfferedNoDirections() {
        Intent intent = new RandomBrain(List.of(1.0)).decide(NOTHING, new Options(List.of()), seeded());
        assertThat(intent).isEqualTo(new Intent.Idle());
    }

    @Test
    void aMoveCoversAtLeastOneTile() {
        Direction north = new Grid(3, 3).directions().getFirst();
        assertThatThrownBy(() -> new Intent.Move(north, 0)).isInstanceOf(IllegalArgumentException.class);
    }
}
