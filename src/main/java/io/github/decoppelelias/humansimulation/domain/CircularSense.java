package io.github.decoppelelias.humansimulation.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** The sense a creature has when its species declares no other: everything within its view range. */
final class CircularSense {
    private static final String VIEW_RANGE = "viewRange";

    private CircularSense() {}

    static Perception perceive(Entity self, Grid grid, Map<Integer, Entity> entities) {
        int range = (int) Math.round(self.genome().scalar(VIEW_RANGE));
        GridPosition at = self.position();
        List<Perception.TileView> tiles = new ArrayList<>();
        for (GridPosition tile : grid.within(at, range)) {
            List<Perception.EntityView> seen = new ArrayList<>();
            for (int id : grid.occupants(tile)) {
                if (id != self.id()) {
                    Entity other = entities.get(id);
                    seen.add(new Perception.EntityView(id, other.species().name(), ViewParts.seen(other.parts())));
                }
            }
            tiles.add(new Perception.TileView(tile.x() - at.x(), tile.y() - at.y(), Map.of(), seen));
        }
        return new Perception(new Perception.SelfView(self.id(), ViewParts.own(self.parts())), tiles);
    }
}
