package Test;

import SimulationApplication.GridContent.Food;
import SimulationApplication.GridPosition;
import SimulationApplication.GridWorld;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class GridWorldBookkeepingTest {

    @Test
    void addedContentLandsOnItsTile() {
        GridWorld world = TestWorlds.seeded(3, 3, 1L);

        new Food(world, new GridPosition(1, 1));

        Assertions.assertEquals(
                "FOOD", world.getGridTile(new GridPosition(1, 1)).toString());
    }

    @Test
    void removedContentLeavesTheTileEmpty() throws Exception {
        GridWorld world = TestWorlds.seeded(3, 3, 1L);
        Food food = new Food(world, new GridPosition(1, 1));

        world.removeContent(food);

        Assertions.assertEquals(
                "Empty", world.getGridTile(new GridPosition(1, 1)).toString());
        Assertions.assertTrue(world.getAllFoodPositions().isEmpty());
    }

    @Test
    void yIsCountedFromTheBottomRow() {
        GridWorld world = TestWorlds.seeded(2, 2, 1L);

        new Food(world, new GridPosition(0, 0));

        Assertions.assertEquals("Empty Empty \nFOOD Empty \n", world.toString());
    }

    @Test
    void contentOutsideTheGridIsRejected() {
        GridWorld world = TestWorlds.seeded(3, 3, 1L);

        new Food(world, new GridPosition(5, 5));

        Assertions.assertTrue(world.getAllFoodPositions().isEmpty());
    }

    @Test
    void eachContentGetsItsOwnId() {
        GridWorld world = TestWorlds.seeded(3, 3, 1L);

        Food first = new Food(world, new GridPosition(0, 0));
        Food second = new Food(world, new GridPosition(0, 0));

        Assertions.assertNotEquals(world.getId(first), world.getId(second));
        Assertions.assertEquals(
                "FOOD|FOOD", world.getGridTile(new GridPosition(0, 0)).toString());
    }

    @Test
    void boundsCoverTheCornersAndRejectWhatIsOutside() {
        GridWorld world = TestWorlds.seeded(4, 6, 1L);

        Assertions.assertTrue(world.isWithinBounds(new GridPosition(0, 0)));
        Assertions.assertTrue(world.isWithinBounds(new GridPosition(3, 5)));
        Assertions.assertFalse(world.isWithinBounds(new GridPosition(4, 5)));
        Assertions.assertFalse(world.isWithinBounds(new GridPosition(3, 6)));
        Assertions.assertFalse(world.isWithinBounds(new GridPosition(-1, 0)));
    }
}
