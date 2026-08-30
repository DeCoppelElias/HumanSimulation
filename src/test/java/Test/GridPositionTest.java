package Test;

import SimulationApplication.GridPosition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class GridPositionTest {

    @Test
    void distanceIsEuclidean() {
        Assertions.assertEquals(5.0, GridPosition.distance(new GridPosition(0, 0), new GridPosition(3, 4)), 1e-9);
    }

    @Test
    void distanceToItselfIsZero() {
        GridPosition position = new GridPosition(7, 2);
        Assertions.assertEquals(0.0, GridPosition.distance(position, position), 1e-9);
    }

    @Test
    void distanceIsSymmetric() {
        GridPosition a = new GridPosition(1, 9);
        GridPosition b = new GridPosition(6, 3);
        Assertions.assertEquals(GridPosition.distance(a, b), GridPosition.distance(b, a), 1e-9);
    }
}
