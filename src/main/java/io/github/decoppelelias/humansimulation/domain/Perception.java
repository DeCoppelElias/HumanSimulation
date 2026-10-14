package io.github.decoppelelias.humansimulation.domain;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

record Perception(SelfView self, List<TileView> tiles, Metric metric) {
    /** The grid's distance, so a brain never measures one itself. */
    interface Metric {
        double distance(int dx, int dy);
    }

    Perception {
        tiles = List.copyOf(tiles);
    }

    double distance(int dx, int dy) {
        return metric.distance(dx, dy);
    }

    record SelfView(int id, Parts parts) {}

    record TileView(int dx, int dy, Map<String, Double> fields, List<EntityView> entities) {
        TileView {
            fields = Collections.unmodifiableSortedMap(new TreeMap<>(fields));
            entities = List.copyOf(entities);
        }
    }

    record EntityView(int id, String species, Parts parts) {}
}
