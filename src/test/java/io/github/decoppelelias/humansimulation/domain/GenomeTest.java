package io.github.decoppelelias.humansimulation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;
import org.junit.jupiter.api.Test;

class GenomeTest {
    private static final List<GeneSpec> LAYOUT = List.of(new GeneSpec.Scalar("viewRange", 1, 10, 1.0));

    @Test
    void readsTheValuesItWasBuiltWith() {
        Genome genome = new Genome(LAYOUT, Map.of("viewRange", 3.0));
        assertThat(genome.scalar("viewRange")).isEqualTo(3.0);
    }

    @Test
    void rejectsAGeneWithNoValue() {
        assertThatThrownBy(() -> new Genome(LAYOUT, Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("viewRange");
    }

    @Test
    void rejectsAValueTheLayoutDoesNotDeclare() {
        assertThatThrownBy(() -> new Genome(LAYOUT, Map.of("viewRange", 3.0, "speed", 1.0)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("speed");
    }

    @Test
    void rejectsAScalarOutsideItsBounds() {
        assertThatThrownBy(() -> new Genome(LAYOUT, Map.of("viewRange", 11.0)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("viewRange");
    }

    @Test
    void readingAnUndeclaredGeneThrows() {
        Genome genome = new Genome(LAYOUT, Map.of("viewRange", 3.0));
        assertThatThrownBy(() -> genome.scalar("speed")).isInstanceOf(IllegalArgumentException.class);
    }

    private static final List<GeneSpec> TWO =
            List.of(new GeneSpec.Scalar("viewRange", 1, 10, 1.0), new GeneSpec.Scalar("whim", 0.05, 5, 0.1));

    private static RandomGenerator seeded(long seed) {
        return RandomGeneratorFactory.of("L64X128MixRandom").create(seed);
    }

    @Test
    void aMutationMovesEachGeneByAtMostItsSize() {
        Genome parent = new Genome(TWO, Map.of("viewRange", 5.0, "whim", 1.0));
        RandomGenerator random = seeded(1);
        List<Double> views = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            Genome child = parent.mutated(random);
            assertThat(child.scalar("viewRange")).isBetween(4.0, 6.0);
            assertThat(child.scalar("whim")).isBetween(0.9, 1.1);
            views.add(child.scalar("viewRange"));
        }
        assertThat(views).doesNotHaveDuplicates();
    }

    @Test
    void offsetsAreUniformAcrossTheMutationSize() {
        Genome parent = new Genome(TWO, Map.of("viewRange", 5.0, "whim", 1.0));
        RandomGenerator random = seeded(2);
        int above = 0;
        int outerQuarter = 0;
        for (int i = 0; i < 4000; i++) {
            double offset = parent.mutated(random).scalar("viewRange") - 5.0;
            above += offset > 0 ? 1 : 0;
            outerQuarter += Math.abs(offset) > 0.75 ? 1 : 0;
        }
        assertThat(above).isBetween(1850, 2150);
        assertThat(outerQuarter).isBetween(850, 1150);
    }

    @Test
    void aViewRangeNeverDropsBelowOne() {
        Genome parent = new Genome(TWO, Map.of("viewRange", 1.0, "whim", 0.05));
        RandomGenerator random = seeded(3);
        for (int i = 0; i < 1000; i++) {
            Genome child = parent.mutated(random);
            assertThat(child.scalar("viewRange")).isBetween(1.0, 2.0);
            assertThat(child.scalar("whim")).isGreaterThanOrEqualTo(0.05);
        }
    }

    @Test
    void aSeedReplaysAMutationAndASizeOfZeroStaysPut() {
        Genome parent = new Genome(TWO, Map.of("viewRange", 5.0, "whim", 1.0));
        assertThat(parent.mutated(seeded(4))).isEqualTo(parent.mutated(seeded(4)));
        List<GeneSpec> fixed = List.of(new GeneSpec.Scalar("viewRange", 1, 10, 0));
        assertThat(new Genome(fixed, Map.of("viewRange", 5.0))
                        .mutated(seeded(5))
                        .scalar("viewRange"))
                .isEqualTo(5.0);
    }

    @Test
    void declaresTheGenesOfItsLayout() {
        Genome genome = new Genome(LAYOUT, Map.of("viewRange", 3.0));
        assertThat(genome.declares("viewRange")).isTrue();
        assertThat(genome.declares("speed")).isFalse();
    }

    @Test
    void geneSpecsRejectBoundsThatAreNotARange() {
        assertThatThrownBy(() -> new GeneSpec.Scalar("x", 2, 1, 0.1)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new GeneSpec.Scalar("x", 1, 2, -0.1)).isInstanceOf(IllegalArgumentException.class);
    }
}
