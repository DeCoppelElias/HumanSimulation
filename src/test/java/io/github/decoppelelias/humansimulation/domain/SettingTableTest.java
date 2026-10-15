package io.github.decoppelelias.humansimulation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class SettingTableTest {
    private static SettingTable table() {
        return new SettingTable(List.of(Grass.species(), Rabbit.species()), List.of(new FoodDrop("grass")));
    }

    @Test
    void groupsTheWorldFirstThenEachSpecies() {
        SettingTable table = table();
        assertThat(table.of("world").whole("grassPerDrop")).isEqualTo(7);
        assertThat(table.of("rabbit").whole("stride")).isEqualTo(3);
        assertThat(table.declarations())
                .extracting(SettingDeclaration::owner)
                .startsWith("world", "world", "grass", "rabbit");
        assertThat(table.declarations().getFirst())
                .isEqualTo(new SettingDeclaration(
                        "world", "grassPerDrop", "How many grass land in each drop.", 1, 100, 1, 7));
    }

    @Test
    void aSpeciesNamedWorldIsRefused() {
        Species world = new Species(
                "world",
                "world",
                List.of(),
                new Genome(List.of(), Map.of()),
                Settings.NONE,
                Optional.empty(),
                List.of());
        assertThatThrownBy(() -> new SettingTable(List.of(world), List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("world");
    }

    @Test
    void twoProcessesDeclaringOneKeyAreRefused() {
        assertThatThrownBy(() -> new SettingTable(
                        List.of(Grass.species()), List.of(new FoodDrop("grass"), new FoodDrop("grass"))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("grassPerDrop");
    }

    @Test
    void anUnknownOwnerOrKeyIsRefused() {
        SettingTable table = table();
        assertThatThrownBy(() -> table.check("wolf", "stride", 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("wolf");
        assertThatThrownBy(() -> table.check("rabbit", "wings", 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("wings");
        assertThatThrownBy(() -> table.checkScope(Optional.of("rabbit"), Optional.of("wings")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> table.checkScope(Optional.of("wolf"), Optional.empty()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void restoringCoversEverythingAGroupOrOneSetting() {
        SettingTable table = table();
        table.set("rabbit", "stride", 1);
        table.set("rabbit", "eatingCharge", 2);
        table.set("world", "grassPerDrop", 3);

        table.restore(Optional.of("rabbit"), Optional.of("stride"));
        assertThat(table.of("rabbit").whole("stride")).isEqualTo(3);
        assertThat(table.of("rabbit").whole("eatingCharge")).isEqualTo(2);

        table.restore(Optional.of("rabbit"), Optional.empty());
        assertThat(table.of("rabbit").whole("eatingCharge")).isEqualTo(1);
        assertThat(table.of("world").whole("grassPerDrop")).isEqualTo(3);

        table.restore(Optional.empty(), Optional.empty());
        assertThat(table.of("world").whole("grassPerDrop")).isEqualTo(7);
    }

    @Test
    void currentHoldsEveryValueByOwner() {
        SettingTable table = table();
        table.set("grass", "nourishment", 20);
        assertThat(table.current()).containsOnlyKeys("grass", "rabbit", "world");
        assertThat(table.current().get("grass")).containsExactly(Map.entry("nourishment", 20.0));
        assertThat(table.declarations())
                .extracting(SettingDeclaration::owner, SettingDeclaration::key)
                .contains(tuple("grass", "nourishment"), tuple("rabbit", "stride"));
    }
}
