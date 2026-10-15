package io.github.decoppelelias.humansimulation.domain;

import java.util.Optional;

public sealed interface Command {
    /** Spawns a count of a species, each on a tile drawn from the whole grid. */
    record Spawn(String species, int count) implements Command {
        public Spawn {
            if (count < 1) {
                throw new IllegalArgumentException("spawn at least one " + species + ", got " + count);
            }
        }
    }

    record SpawnAt(String species, GridPosition at) implements Command {}

    record Reset(long seed) implements Command {}

    /** Sets one declared setting in this world, checked against its bounds and step. */
    record SetSetting(String owner, String key, double value) implements Command {}

    /** Puts declared defaults back: all of them with no owner, one group with an owner, one setting with both. */
    record RestoreDefaults(Optional<String> owner, Optional<String> key) implements Command {
        public RestoreDefaults {
            if (owner.isEmpty() && key.isPresent()) {
                throw new IllegalArgumentException("restoring " + key.orElseThrow() + " needs the owner it belongs to");
            }
        }
    }
}
