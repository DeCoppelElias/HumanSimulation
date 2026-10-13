package io.github.decoppelelias.humansimulation.domain;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public record WorldSnapshot(long seed, int day, int width, int height, List<String> species, List<TileView> tiles) {
    public WorldSnapshot {
        species = List.copyOf(species);
        tiles = List.copyOf(tiles);
    }

    public record TileView(GridPosition at, Map<String, Double> fields, List<EntityView> entities) {
        public TileView {
            fields = Collections.unmodifiableSortedMap(new TreeMap<>(fields));
            entities = List.copyOf(entities);
        }
    }

    public record EntityView(int id, String species, String spriteKey, boolean creature, Map<String, Double> info) {
        public EntityView {
            info = Collections.unmodifiableSortedMap(new TreeMap<>(info));
        }
    }
}
