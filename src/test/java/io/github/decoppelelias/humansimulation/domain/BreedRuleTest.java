package io.github.decoppelelias.humansimulation.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class BreedRuleTest {
    private static final Species KIND = TestSpecies.eater(1, 0);

    private static SettingValues settings() {
        List<Setting> declared = new ArrayList<>(TestSpecies.metabolism(1, 0).declared());
        declared.addAll(TestSpecies.breeding().declared());
        return new SettingValues(new Settings(declared));
    }

    private static Entity parent(SettingValues settings, double reserve, int born, boolean fertile) {
        List<Component> parts = new ArrayList<>();
        parts.add(new Metabolism(settings, new Genome(List.of(), Map.of()), reserve));
        if (fertile) {
            parts.add(new Fertility(born));
        }
        return new Entity(0, KIND, KIND.baseline(), new GridPosition(0, 0), Optional.empty(), parts);
    }

    private static double reserve(Entity entity) {
        return entity.part(Metabolism.class).orElseThrow().reserve();
    }

    @Test
    void offeredOnceTheIntervalHasPassed() {
        SettingValues settings = settings();
        assertThat(BreedRule.options(parent(settings, 60, 0, true), settings, 14))
                .isEmpty();
        assertThat(BreedRule.options(parent(settings, 60, 0, true), settings, 15))
                .contains(new Option.Breed());
        assertThat(BreedRule.options(parent(settings, 60, 0, true), settings, 90))
                .contains(new Option.Breed());
    }

    @Test
    void offeredOnlyWhenTheReserveCoversChildLossAndMargin() {
        SettingValues settings = settings();
        assertThat(BreedRule.options(parent(settings, 49, 0, true), settings, 15))
                .isEmpty();
        assertThat(BreedRule.options(parent(settings, 50, 0, true), settings, 15))
                .contains(new Option.Breed());
        settings.set(BreedRule.PARENT_MARGIN, 0);
        assertThat(BreedRule.options(parent(settings, 45, 0, true), settings, 15))
                .contains(new Option.Breed());
    }

    @Test
    void breedingPaysTheChildAndTheLossAndRestartsTheWindow() {
        SettingValues settings = settings();
        Entity parent = parent(settings, 60, 0, true);
        assertThat(BreedRule.resolve(parent, settings, 15)).contains(15.0);
        assertThat(reserve(parent)).isEqualTo(15);
        assertThat(parent.part(Fertility.class).orElseThrow().ownView()).isEqualTo(new Fertility.Since(15));
        assertThat(BreedRule.options(parent, settings, 16)).isEmpty();
    }

    @Test
    void aBreedThatIsNoLongerLegalPaysNothing() {
        SettingValues settings = settings();
        Entity parent = parent(settings, 40, 0, true);
        assertThat(BreedRule.resolve(parent, settings, 15)).isEmpty();
        assertThat(reserve(parent)).isEqualTo(40);
        assertThat(parent.part(Fertility.class).orElseThrow().ownView()).isEqualTo(new Fertility.Since(0));
    }

    @Test
    void aCreatureWithoutFertilityIsNeverOffered() {
        SettingValues settings = settings();
        assertThat(BreedRule.options(parent(settings, 100, 0, false), settings, 50))
                .isEmpty();
        assertThat(BreedRule.resolve(parent(settings, 100, 0, false), settings, 50))
                .isEmpty();
    }
}
