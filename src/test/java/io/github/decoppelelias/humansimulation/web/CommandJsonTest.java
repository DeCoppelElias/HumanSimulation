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
        return new CommandJson.Body(type, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
    }

    @Test
    void readsASpawn() {
        CommandJson.Body spawn = new CommandJson.Body(
                "spawn", Optional.of("rabbit"), Optional.of(3), Optional.empty(), Optional.empty());
        assertThat(CommandJson.toCommand(spawn, () -> 0L)).isEqualTo(new Command.Spawn("rabbit", 3));
    }

    @Test
    void readsASpawnAt() {
        CommandJson.Body spawnAt = new CommandJson.Body(
                "spawnAt",
                Optional.of("rabbit"),
                Optional.empty(),
                Optional.of(new GridPosition(3, 4)),
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
}
