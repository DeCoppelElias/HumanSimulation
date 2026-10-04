package io.github.decoppelelias.humansimulation.domain;

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
}
