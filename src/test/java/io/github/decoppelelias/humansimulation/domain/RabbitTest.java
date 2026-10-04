package io.github.decoppelelias.humansimulation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class RabbitTest {
    @Test
    void declaresItsGenesWithBoundsAndMutationSizes() {
        assertThat(Rabbit.species().geneLayout())
                .containsExactly(
                        new GeneSpec.Scalar("viewRange", 1, 10, 1.0), new GeneSpec.Simplex("steps", 1, 3, 0.2));
    }

    @Test
    void startsFromItsBaselineGenome() {
        Genome baseline = Rabbit.species().baseline();
        assertThat(baseline.scalar("viewRange")).isEqualTo(3.0);
        assertThat(baseline.simplex("steps")).containsExactly(0.6, 0.3, 0.1);
    }

    @Test
    void isBornWithARandomBrain() {
        Species rabbit = Rabbit.species();
        assertThat(rabbit.name()).isEqualTo("rabbit");
        assertThat(rabbit.brain())
                .hasValueSatisfying(factory -> assertThat(factory.apply(new Spawn(rabbit.baseline(), rabbit)))
                        .isInstanceOf(RandomBrain.class));
    }

    @Test
    void aSpeciesRejectsABaselineOfAnotherLayout() {
        List<GeneSpec> layout = List.of(new GeneSpec.Scalar("viewRange", 1, 10, 1.0));
        Genome other =
                new Genome(List.of(new GeneSpec.Scalar("viewRange", 1, 5, 1.0)), Map.of("viewRange", 3.0), Map.of());
        assertThatThrownBy(() -> new Species("odd", "odd", layout, other, Optional.empty(), List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("odd");
    }
}
