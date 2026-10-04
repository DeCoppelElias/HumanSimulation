package io.github.decoppelelias.humansimulation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class GridTest {
    @Test
    void offersFourDirectionsInAFixedOrder() {
        assertThat(new Grid(5, 5).directions())
                .extracting(Direction::name)
                .containsExactly("north", "east", "south", "west");
    }

    @Test
    void rejectsAGridWithNoTiles() {
        assertThatThrownBy(() -> new Grid(0, 5)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Grid(5, 0)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void stepsOneTileInADirection() {
        Grid grid = new Grid(5, 5);
        GridPosition centre = new GridPosition(2, 2);
        assertThat(grid.directions())
                .extracting(direction -> grid.step(centre, direction))
                .containsExactly(
                        new GridPosition(2, 1), new GridPosition(3, 2), new GridPosition(2, 3), new GridPosition(1, 2));
    }

    @Test
    void containsOnlyTilesInsideItsBounds() {
        Grid grid = new Grid(5, 4);
        assertThat(grid.contains(new GridPosition(0, 0))).isTrue();
        assertThat(grid.contains(new GridPosition(4, 3))).isTrue();
        assertThat(grid.contains(new GridPosition(5, 0))).isFalse();
        assertThat(grid.contains(new GridPosition(0, 4))).isFalse();
        assertThat(grid.contains(new GridPosition(-1, 0))).isFalse();
    }

    @Test
    void withinOrdersTilesByStraightLineDistanceThenRowThenColumn() {
        assertThat(new Grid(5, 5).within(new GridPosition(2, 2), 1))
                .containsExactly(
                        new GridPosition(2, 2),
                        new GridPosition(2, 1),
                        new GridPosition(1, 2),
                        new GridPosition(3, 2),
                        new GridPosition(2, 3));
    }

    @Test
    void withinMeasuresStraightLinesNotSteps() {
        assertThat(new Grid(9, 9).within(new GridPosition(4, 4), 2))
                .hasSize(13)
                .contains(new GridPosition(5, 5), new GridPosition(4, 6))
                .doesNotContain(new GridPosition(5, 6));
    }

    @Test
    void withinLeavesOutTilesOffTheGrid() {
        assertThat(new Grid(5, 5).within(new GridPosition(0, 0), 1))
                .containsExactly(new GridPosition(0, 0), new GridPosition(1, 0), new GridPosition(0, 1));
    }

    @Test
    void listsEveryTileRowByRow() {
        assertThat(new Grid(2, 2).tiles())
                .containsExactly(
                        new GridPosition(0, 0), new GridPosition(1, 0), new GridPosition(0, 1), new GridPosition(1, 1));
    }

    @Test
    void occupantsComeBackInIdOrder() {
        Grid grid = new Grid(3, 3);
        GridPosition tile = new GridPosition(1, 1);
        grid.place(7, tile);
        grid.place(2, tile);
        grid.place(5, tile);
        assertThat(grid.occupants(tile)).containsExactly(2, 5, 7);

        grid.remove(5, tile);
        assertThat(grid.occupants(tile)).containsExactly(2, 7);
        assertThat(grid.occupants(new GridPosition(0, 0))).isEmpty();
    }

    @Test
    void placingOffTheGridThrows() {
        assertThatThrownBy(() -> new Grid(3, 3).place(1, new GridPosition(3, 0)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
