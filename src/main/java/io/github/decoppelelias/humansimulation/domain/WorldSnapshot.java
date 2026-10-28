package io.github.decoppelelias.humansimulation.domain;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/** The whole world on one day, with each owner's current setting values. */
public record WorldSnapshot(
        long seed,
        int day,
        int width,
        int height,
        List<String> species,
        List<TileView> tiles,
        Map<String, Map<String, Double>> settings) {
    public WorldSnapshot {
        species = List.copyOf(species);
        tiles = List.copyOf(tiles);
        SortedMap<String, Map<String, Double>> byOwner = new TreeMap<>();
        settings.forEach(
                (owner, values) -> byOwner.put(owner, Collections.unmodifiableSortedMap(new TreeMap<>(values))));
        settings = Collections.unmodifiableSortedMap(byOwner);
    }

    public record TileView(GridPosition at, Map<String, Double> fields, List<EntityView> entities) {
        public TileView {
            fields = Collections.unmodifiableSortedMap(new TreeMap<>(fields));
            entities = List.copyOf(entities);
        }
    }

    public record EntityView(
            int id,
            String species,
            String spriteKey,
            boolean creature,
            Map<String, Double> info,
            Map<String, Double> genes) {
        public EntityView {
            info = Collections.unmodifiableSortedMap(new TreeMap<>(info));
            genes = Collections.unmodifiableSortedMap(new TreeMap<>(genes));
        }
    }
}
