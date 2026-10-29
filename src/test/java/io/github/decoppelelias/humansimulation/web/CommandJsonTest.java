package io.github.decoppelelias.humansimulation.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.decoppelelias.humansimulation.domain.Command;
import io.github.decoppelelias.humansimulation.domain.GridPosition;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class CommandJsonTest {
    private static CommandJson.Body body(String type) {
        return setting(type, Optional.empty(), Optional.empty(), Optional.empty());
    }

    private static CommandJson.Body setting(
            String type, Optional<String> owner, Optional<String> key, Optional<Double> value) {
        return new CommandJson.Body(
                Optional.of(type),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                owner,
                key,
                value,
                Optional.empty());
    }

    @Test
    void readsASpawn() {
        CommandJson.Body spawn = new CommandJson.Body(
                Optional.of("spawn"),
                Optional.of("rabbit"),
                Optional.of(3),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty());
        assertThat(CommandJson.toCommand(spawn, () -> 0L)).isEqualTo(new Command.Spawn("rabbit", 3));
    }

    @Test
    void aSpawnCarriesItsGeneOverridesBothWays() {
        CommandJson.Body spawn = new CommandJson.Body(
                Optional.of("spawn"),
                Optional.of("rabbit"),
                Optional.of(3),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of(Map.of("near.Plant", 0.2)));
        Command read = CommandJson.toCommand(spawn, () -> 0L);
        assertThat(read).isEqualTo(new Command.Spawn("rabbit", 3, Map.of("near.Plant", 0.2)));
        assertThat(CommandJson.toJson(read)).containsEntry("genes", Map.of("near.Plant", 0.2));
        assertThat(CommandJson.toJson(new Command.Spawn("rabbit", 3))).doesNotContainKey("genes");
    }

    @Test
    void readsASetSetting() {
        assertThat(CommandJson.toCommand(
                        setting("setSetting", Optional.of("rabbit"), Optional.of("stride"), Optional.of(2.0)),
                        () -> 0L))
                .isEqualTo(new Command.SetSetting("rabbit", "stride", 2));
    }

    @Test
    void readsARestoreOfEverythingAGroupOrOneSetting() {
        assertThat(CommandJson.toCommand(body("restoreDefaults"), () -> 0L))
                .isEqualTo(new Command.RestoreDefaults(Optional.empty(), Optional.empty()));
        assertThat(CommandJson.toCommand(
                        setting("restoreDefaults", Optional.of("rabbit"), Optional.empty(), Optional.empty()),
                        () -> 0L))
                .isEqualTo(new Command.RestoreDefaults(Optional.of("rabbit"), Optional.empty()));
        assertThat(CommandJson.toCommand(
                        setting("restoreDefaults", Optional.of("rabbit"), Optional.of("stride"), Optional.empty()),
                        () -> 0L))
                .isEqualTo(new Command.RestoreDefaults(Optional.of("rabbit"), Optional.of("stride")));
    }

    @Test
    void aSetSettingNeedsAnOwnerAKeyAndAValue() {
        assertThatThrownBy(() -> CommandJson.toCommand(
                        setting("setSetting", Optional.of("rabbit"), Optional.of("stride"), Optional.empty()),
                        () -> 0L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("value");
        assertThatThrownBy(() -> CommandJson.toCommand(
                        setting("setSetting", Optional.empty(), Optional.of("stride"), Optional.of(2.0)), () -> 0L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("owner");
        assertThatThrownBy(() -> CommandJson.toCommand(
                        setting("setSetting", Optional.of("rabbit"), Optional.empty(), Optional.of(2.0)), () -> 0L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("key");
    }

    @Test
    void readsASpawnAt() {
        CommandJson.Body spawnAt = new CommandJson.Body(
                Optional.of("spawnAt"),
                Optional.of("rabbit"),
                Optional.empty(),
                Optional.of(new GridPosition(3, 4)),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty());
        assertThat(CommandJson.toCommand(spawnAt, () -> 0L))
                .isEqualTo(new Command.SpawnAt("rabbit", new GridPosition(3, 4)));
    }

    @Test
    void aResetWithoutASeedDrawsOne() {
        assertThat(CommandJson.toCommand(body("reset"), () -> 99L)).isEqualTo(new Command.Reset(99));
    }

    @Test
    void rejectsAMissingFieldOrAnUnknownType() {
        assertThatThrownBy(() -> CommandJson.toCommand(body("spawn"), () -> 0L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("species");
        assertThatThrownBy(() -> CommandJson.toCommand(body("fly"), () -> 0L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("fly");
    }

    @Test
    void writesEachCommandWithItsTypeFirst() {
        assertThat(CommandJson.toJson(new Command.Spawn("rabbit", 3)))
                .containsExactly(Map.entry("type", "spawn"), Map.entry("species", "rabbit"), Map.entry("count", 3));
        assertThat(CommandJson.toJson(new Command.SpawnAt("rabbit", new GridPosition(3, 4))))
                .containsExactly(
                        Map.entry("type", "spawnAt"),
                        Map.entry("species", "rabbit"),
                        Map.entry("at", new GridPosition(3, 4)));
        assertThat(CommandJson.toJson(new Command.Reset(7)))
                .containsExactly(Map.entry("type", "reset"), Map.entry("seed", 7L));
    }

    @Test
    void writesTheSettingCommandsWithOnlyTheFieldsTheyHave() {
        assertThat(CommandJson.toJson(new Command.SetSetting("rabbit", "stride", 2)))
                .containsExactly(
                        Map.entry("type", "setSetting"),
                        Map.entry("owner", "rabbit"),
                        Map.entry("key", "stride"),
                        Map.entry("value", 2.0));
        assertThat(CommandJson.toJson(new Command.RestoreDefaults(Optional.of("rabbit"), Optional.empty())))
                .containsExactly(Map.entry("type", "restoreDefaults"), Map.entry("owner", "rabbit"));
        assertThat(CommandJson.toJson(new Command.RestoreDefaults(Optional.of("rabbit"), Optional.of("stride"))))
                .containsExactly(
                        Map.entry("type", "restoreDefaults"), Map.entry("owner", "rabbit"), Map.entry("key", "stride"));
        assertThat(CommandJson.toJson(new Command.RestoreDefaults(Optional.empty(), Optional.empty())))
                .containsExactly(Map.entry("type", "restoreDefaults"));
    }
}
