package io.github.decoppelelias.humansimulation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class GenomeTest {
    private static final List<GeneSpec> LAYOUT =
            List.of(new GeneSpec.Scalar("viewRange", 1, 10, 1.0), new GeneSpec.Simplex("steps", 1, 3, 0.2));

    private static Genome genome(Map<String, Double> scalars, Map<String, List<Double>> simplexes) {
        return new Genome(LAYOUT, scalars, simplexes);
    }

    @Test
    void readsTheValuesItWasBuiltWith() {
        Genome genome = genome(Map.of("viewRange", 3.0), Map.of("steps", List.of(0.6, 0.3, 0.1)));
        assertThat(genome.scalar("viewRange")).isEqualTo(3.0);
        assertThat(genome.simplex("steps")).containsExactly(0.6, 0.3, 0.1);
    }

    @Test
    void rejectsAGeneWithNoValue() {
        assertThatThrownBy(() -> genome(Map.of(), Map.of("steps", List.of(1.0))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("viewRange");
    }

    @Test
    void rejectsAValueTheLayoutDoesNotDeclare() {
        assertThatThrownBy(() -> genome(Map.of("viewRange", 3.0, "speed", 1.0), Map.of("steps", List.of(1.0))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("speed");
    }

    @Test
    void rejectsAValueOfTheWrongShape() {
        assertThatThrownBy(() -> genome(Map.of("viewRange", 3.0, "steps", 1.0), Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("steps");
    }

    @Test
    void rejectsAScalarOutsideItsBounds() {
        assertThatThrownBy(() -> genome(Map.of("viewRange", 11.0), Map.of("steps", List.of(1.0))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("viewRange");
    }

    @Test
    void rejectsASimplexThatDoesNotSumToOne() {
        assertThatThrownBy(() -> genome(Map.of("viewRange", 3.0), Map.of("steps", List.of(0.5, 0.4))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("steps");
    }

    @Test
    void rejectsASimplexLongerThanItsBound() {
        assertThatThrownBy(() -> genome(Map.of("viewRange", 3.0), Map.of("steps", List.of(0.25, 0.25, 0.25, 0.25))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("steps");
    }

    @Test
    void rejectsANegativeWeight() {
        assertThatThrownBy(() -> genome(Map.of("viewRange", 3.0), Map.of("steps", List.of(1.5, -0.5))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("steps");
    }

    @Test
    void readingAnUndeclaredGeneThrows() {
        Genome genome = genome(Map.of("viewRange", 3.0), Map.of("steps", List.of(1.0)));
        assertThatThrownBy(() -> genome.scalar("speed")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> genome.simplex("speed")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void geneSpecsRejectBoundsThatAreNotARange() {
        assertThatThrownBy(() -> new GeneSpec.Scalar("x", 2, 1, 0.1)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new GeneSpec.Simplex("x", 0, 3, 0.1)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new GeneSpec.Simplex("x", 3, 2, 0.1)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new GeneSpec.Scalar("x", 1, 2, -0.1)).isInstanceOf(IllegalArgumentException.class);
    }
}
