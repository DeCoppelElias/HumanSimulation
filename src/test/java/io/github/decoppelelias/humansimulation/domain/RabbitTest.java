package io.github.decoppelelias.humansimulation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

class RabbitTest {
    @Test
    void declaresItsGenesWithBoundsAndMutationSizes() {
        assertThat(Rabbit.species().geneLayout())
                .containsExactly(
                        new GeneSpec.Scalar("viewRange", 1, 10, 1.0),
                        new GeneSpec.Scalar("grassPull", -5, 5, 0.2),
                        new GeneSpec.Scalar("hungerDrive", -10, 10, 0.4),
                        new GeneSpec.Scalar("rabbitPull", -2, 2, 0.1),
                        new GeneSpec.Scalar("whim", 0.05, 5, 0.1),
                        new GeneSpec.Scalar("breedPull", 0, 5, 0.2),
                        new GeneSpec.Scalar("speed", 0, 1, 0.05));
    }

    @Test
    void startsFromItsBaselineGenome() {
        assertThat(Rabbit.species().baseline().scalars())
                .containsExactly(
                        Map.entry("breedPull", 1.0),
                        Map.entry("grassPull", 1.0),
                        Map.entry("hungerDrive", 2.0),
                        Map.entry("rabbitPull", -0.2),
                        Map.entry("speed", 0.5),
                        Map.entry("viewRange", 3.0),
                        Map.entry("whim", 0.5));
    }

    @Test
    void declaresItsSettings() {
        SettingValues settings = new SettingValues(Rabbit.species().settings());
        assertThat(settings.whole("eatingInterval")).isEqualTo(1);
        assertThat(settings.number("eatingCharge")).isEqualTo(1);
        assertThat(settings.number("startingReserve")).isEqualTo(15);
        assertThat(settings.whole("stride")).isEqualTo(3);
        assertThat(settings.declared().scalar("speedCharge"))
                .extracting(
                        Setting.Scalar::min, Setting.Scalar::max, Setting.Scalar::step, Setting.Scalar::defaultValue)
                .containsExactly(0.0, 2.0, 0.01, 0.1);
        assertThat(settings.declared().scalar("viewCharge"))
                .extracting(
                        Setting.Scalar::min, Setting.Scalar::max, Setting.Scalar::step, Setting.Scalar::defaultValue)
                .containsExactly(0.0, 1.0, 0.01, 0.03);
        assertThat(settings.whole("breedInterval")).isEqualTo(15);
        assertThat(settings.number("childReserve")).isEqualTo(15);
        assertThat(settings.number("breedingLoss")).isEqualTo(30);
        assertThat(settings.number("parentMargin")).isEqualTo(5);
        assertThat(settings.declared().scalar("breedInterval"))
                .extracting(Setting.Scalar::min, Setting.Scalar::max)
                .containsExactly(1.0, 100.0);
        assertThat(settings.declared().scalar("breedingLoss"))
                .extracting(Setting.Scalar::min, Setting.Scalar::max)
                .containsExactly(0.0, 200.0);
    }

    @Test
    void isBornWithAMetabolismAFertilityAnEaterAndTheScoringBrain() {
        Species rabbit = Rabbit.species();
        Spawn spawn = new Spawn(rabbit.baseline(), rabbit, new SettingValues(rabbit.settings()), 4, Optional.empty());
        assertThat(rabbit.brain())
                .hasValueSatisfying(factory -> assertThat(factory.apply(spawn)).isInstanceOf(ScoringBrain.class));
        assertThat(rabbit.parts()).hasSize(3);
        assertThat(rabbit.parts().get(0).apply(spawn)).isInstanceOf(Metabolism.class);
        assertThat(rabbit.parts().get(1).apply(spawn).ownView()).isEqualTo(new Fertility.Since(4));
        assertThat(rabbit.parts().get(2).apply(spawn).ownView()).isEqualTo(new Eater.Takes(Set.of(Plant.class)));
    }

    @Test
    void aZeroBreedingIntervalIsRejected() {
        World world = new World(3, 3, 1, List.of(Rabbit.species()));
        assertThatThrownBy(() -> world.submit(new Command.SetSetting("rabbit", "breedInterval", 0)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("breedInterval")
                .hasMessageContaining("outside");
    }

    @Test
    void aSpeciesRejectsABaselineOfAnotherLayout() {
        List<GeneSpec> layout = List.of(new GeneSpec.Scalar("viewRange", 1, 10, 1.0));
        Genome other = new Genome(List.of(new GeneSpec.Scalar("viewRange", 1, 5, 1.0)), Map.of("viewRange", 3.0));
        assertThatThrownBy(() -> new Species("odd", "odd", layout, other, Settings.NONE, Optional.empty(), List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("odd");
    }
}
