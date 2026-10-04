package io.github.decoppelelias.humansimulation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import org.junit.jupiter.api.Test;

class CircularSenseTest {
    private final Species walker = TestSpecies.walker(new Intent.Idle());
    private final Grid grid = new Grid(5, 5);
    private final Map<Integer, Entity> entities = new TreeMap<>();

    private Entity put(int id, GridPosition at) {
        Entity entity = new Entity(id, walker, walker.baseline(), at, Optional.empty(), List.of());
        entities.put(id, entity);
        grid.place(id, at);
        return entity;
    }

    @Test
    void seesTilesWithinItsViewRangeRelativeToItself() {
        Entity self = put(0, new GridPosition(2, 2));
        Perception perception = CircularSense.perceive(self, grid, entities);
        assertThat(perception.tiles())
                .extracting(tile -> List.of(tile.dx(), tile.dy()))
                .containsExactly(List.of(0, 0), List.of(0, -1), List.of(-1, 0), List.of(1, 0), List.of(0, 1));
        assertThat(perception.self().id()).isZero();
    }

    @Test
    void anEdgeIsTilesThatAreAbsent() {
        Entity self = put(0, new GridPosition(0, 0));
        assertThat(CircularSense.perceive(self, grid, entities).tiles())
                .extracting(tile -> List.of(tile.dx(), tile.dy()))
                .containsExactly(List.of(0, 0), List.of(1, 0), List.of(0, 1));
    }

    @Test
    void seesOthersButNotItself() {
        Entity self = put(0, new GridPosition(0, 0));
        put(1, new GridPosition(1, 0));
        put(2, new GridPosition(0, 0));
        List<Perception.TileView> tiles =
                CircularSense.perceive(self, grid, entities).tiles();
        assertThat(tiles.get(0).entities())
                .extracting(Perception.EntityView::id)
                .containsExactly(2);
        assertThat(tiles.get(1).entities())
                .extracting(Perception.EntityView::id, Perception.EntityView::species)
                .containsExactly(tuple(1, "walker"));
    }
}
