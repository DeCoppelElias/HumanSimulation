package Test;

import SimulationApplication.GridContent.Entity.Human.Human;
import SimulationApplication.GridContent.Entity.Human.HumanParameters;
import SimulationApplication.GridContent.Food;
import SimulationApplication.GridPosition;
import SimulationApplication.GridWorld;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class HumanLifecycleTest {

    @Test
    void aHumanWithNoReservesStarvesAtTheEatInterval() throws Exception {
        GridWorld world = TestWorlds.seeded(3, 3, 1L);
        HumanParameters parameters = new HumanParameters(2, 1, 100, 3);
        Human human = TestWorlds.walker(world, new GridPosition(1, 1), parameters, 0);

        world.advanceTime();
        Assertions.assertTrue(human.checkAlive(), "day 1 is not an eating day");

        world.advanceTime();
        Assertions.assertFalse(human.checkAlive(), "day 2 costs 1 food it does not have");
    }

    @Test
    void aStarvedHumanLeavesTheWorld() throws Exception {
        GridWorld world = TestWorlds.seeded(3, 3, 1L);
        HumanParameters parameters = new HumanParameters(2, 1, 100, 3);
        TestWorlds.walker(world, new GridPosition(1, 1), parameters, 0);

        world.advanceTime();
        world.advanceTime();

        Assertions.assertTrue(world.getAllHumanPositions().isEmpty());
    }

    @Test
    void aHumanWithReservesSurvivesTheEatInterval() throws Exception {
        GridWorld world = TestWorlds.seeded(3, 3, 1L);
        HumanParameters parameters = new HumanParameters(2, 1, 100, 3);
        Human human = TestWorlds.walker(world, new GridPosition(1, 1), parameters, 0);
        human.addFood(5);

        world.advanceTime();
        world.advanceTime();

        Assertions.assertTrue(human.checkAlive());
        Assertions.assertEquals(4f, human.getFoodAmount());
    }

    @Test
    void aHumanWithTheBreedCostBreeds() throws Exception {
        GridWorld world = TestWorlds.seeded(3, 3, 1L);
        HumanParameters parameters = new HumanParameters(100, 1, 1, 1);
        Human human = TestWorlds.walker(world, new GridPosition(1, 1), parameters, 0);
        human.addFood(5);

        world.advanceTime();

        Assertions.assertEquals(2, world.getAllHumanPositions().size());
        Assertions.assertEquals(4f, human.getFoodAmount(), "breeding costs 1");
    }

    @Test
    void aHumanWithoutTheBreedCostDoesNotBreed() throws Exception {
        GridWorld world = TestWorlds.seeded(3, 3, 1L);
        HumanParameters parameters = new HumanParameters(100, 1, 1, 1);
        TestWorlds.walker(world, new GridPosition(1, 1), parameters, 0);

        world.advanceTime();

        Assertions.assertEquals(1, world.getAllHumanPositions().size());
    }

    @Test
    void aPeacefulHumanEatsTheFoodItReaches() throws Exception {
        GridWorld world = TestWorlds.seeded(3, 3, 1L);
        Human human = TestWorlds.walker(world, new GridPosition(2, 1), TestWorlds.inert(), 0);
        new Food(world, new GridPosition(2, 2));

        world.advanceTime();

        Assertions.assertEquals(1f, human.getFoodAmount());
        Assertions.assertTrue(world.getAllFoodPositions().isEmpty(), "the food is consumed");
    }
}
