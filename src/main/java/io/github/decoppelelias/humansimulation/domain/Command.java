package io.github.decoppelelias.humansimulation.domain;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

public sealed interface Command {
    /**
     * Spawns a count of a species, each on a tile drawn from the whole grid, starting from its baseline with the
     * given genes in place.
     */
    record Spawn(String species, int count, Map<String, Double> genes) implements Command {
        public Spawn {
            if (count < 1) {
                throw new IllegalArgumentException("spawn at least one " + species + ", got " + count);
            }
            genes.forEach((gene, value) -> {
                if (value == null) {
                    throw new IllegalArgumentException("gene " + gene + " needs a value");
                }
            });
            genes = Collections.unmodifiableSortedMap(new TreeMap<>(genes));
        }

        public Spawn(String species, int count) {
            this(species, count, Map.of());
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
