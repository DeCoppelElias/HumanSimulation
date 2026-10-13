package io.github.decoppelelias.humansimulation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;
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

    @Test
    void geneSpecsRejectBoundsThatAreNotARange() {
        assertThatThrownBy(() -> new GeneSpec.Scalar("x", 2, 1, 0.1)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new GeneSpec.Scalar("x", 1, 2, -0.1)).isInstanceOf(IllegalArgumentException.class);
    }
}
