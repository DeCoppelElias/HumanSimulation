package Test;

import SimulationApplication.GridContent.Entity.Human.Human;
import SimulationApplication.GridContent.Entity.Human.HumanBehaviour.FindFoodBehaviour;
import SimulationApplication.GridPosition;
import SimulationApplication.GridWorld;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class FindFoodBehaviourTest {

    private static final int VIEW_RANGE = 4;

    private FindFoodBehaviour behaviourAt(GridWorld world, int x, int y) throws Exception {
        Human human = TestWorlds.walker(world, new GridPosition(x, y), TestWorlds.inert(), 0);
        return new FindFoodBehaviour(world, human);
    }

    private ArrayList<GridPosition> positions(GridPosition... items) {
        return new ArrayList<>(List.of(items));
    }

    @Test
    void closestFoodPicksTheNearestInView() throws Exception {
        GridWorld world = TestWorlds.seeded(10, 10, 1L);
        FindFoodBehaviour behaviour = behaviourAt(world, 5, 5);

        GridPosition found = behaviour.findClosestFood(
                positions(new GridPosition(5, 8), new GridPosition(5, 6), new GridPosition(2, 5)));

        Assertions.assertEquals(5, found.getX());
        Assertions.assertEquals(6, found.getY());
    }

    @Test
    void closestFoodIgnoresFoodBeyondTheViewRange() throws Exception {
        GridWorld world = TestWorlds.seeded(10, 10, 1L);
        FindFoodBehaviour behaviour = behaviourAt(world, 5, 5);

        Assertions.assertNull(behaviour.findClosestFood(positions(new GridPosition(5, 0))));
    }

    @Test
    void randomFoodOnlyEverReturnsSomethingInView() throws Exception {
        GridWorld world = TestWorlds.seeded(10, 10, 1L);
        FindFoodBehaviour behaviour = behaviourAt(world, 5, 5);
        ArrayList<GridPosition> food =
                positions(new GridPosition(5, 6), new GridPosition(5, 0), new GridPosition(0, 0));

        for (int attempt = 0; attempt < 50; attempt++) {
            GridPosition found = behaviour.findRandomFood(food);
            Assertions.assertEquals(5, found.getX());
            Assertions.assertEquals(6, found.getY());
        }
    }

    @Test
    void randomFoodReturnsNullWhenNothingIsInView() throws Exception {
        GridWorld world = TestWorlds.seeded(10, 10, 1L);
        FindFoodBehaviour behaviour = behaviourAt(world, 5, 5);

        Assertions.assertNull(behaviour.findRandomFood(positions(new GridPosition(0, 0))));
    }

    @Test
    void mostFoodInVicinityPrefersTheClusterOverTheLoner() throws Exception {
        GridWorld world = TestWorlds.seeded(10, 10, 1L);
        FindFoodBehaviour behaviour = behaviourAt(world, 5, 5);
        GridPosition loner = new GridPosition(5, 3);

        GridPosition found = behaviour.findMostFoodInVicinityFood(
                positions(loner, new GridPosition(5, 6), new GridPosition(6, 6), new GridPosition(6, 7)));

        Assertions.assertFalse(
                found.getX() == loner.getX() && found.getY() == loner.getY(),
                "picked the isolated food over the cluster");
    }

    @Test
    void everyStrategyReturnsNullWhenThereIsNoFoodAtAll() throws Exception {
        GridWorld world = TestWorlds.seeded(10, 10, 1L);
        FindFoodBehaviour behaviour = behaviourAt(world, 5, 5);
        ArrayList<GridPosition> nothing = positions();

        Assertions.assertNull(behaviour.findClosestFood(nothing));
        Assertions.assertNull(behaviour.findRandomFood(nothing));
        Assertions.assertNull(behaviour.findMostFoodInVicinityFood(nothing));
        Assertions.assertNull(behaviour.findFarthestFromOtherHumansFood(nothing, nothing));
    }
}
