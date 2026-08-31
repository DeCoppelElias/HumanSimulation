package Test;

import SimulationApplication.GridContent.Entity.Human.Human;
import SimulationApplication.GridContent.Entity.Human.HumanBehaviour.FindFoodBehaviour;
import SimulationApplication.GridContent.Entity.Human.HumanBehaviour.FoodBehaviour;
import SimulationApplication.GridContent.Entity.Human.HumanBehaviour.MovementBehaviour;
import SimulationApplication.GridContent.Entity.Human.HumanParameters;
import SimulationApplication.GridContent.Food;
import SimulationApplication.GridPosition;
import SimulationApplication.GridWorld;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ModelBugRegressionTest {

    private Human aggressiveHuman(GridWorld world, GridPosition at) throws Exception {
        MovementBehaviour movement =
                new MovementBehaviour(world, null, 0, 1, new double[] {1}, new FindFoodBehaviour(world, null));
        FoodBehaviour food = new FoodBehaviour(world.getRandom(), null, 1, 0);
        return new Human(world, at, TestWorlds.inert(), movement, food);
    }

    @Test
    void exactlyOneAggressiveHumanWinsEveryFight() throws Exception {
        for (long seed = 0; seed < 50; seed++) {
            GridWorld world = TestWorlds.seeded(3, 3, seed);
            aggressiveHuman(world, new GridPosition(2, 1));
            aggressiveHuman(world, new GridPosition(2, 1));
            new Food(world, new GridPosition(2, 2));

            world.advanceTime();

            Assertions.assertEquals(1, world.getAllHumanPositions().size(), "two aggressors, seed " + seed);
            Assertions.assertTrue(world.getAllFoodPositions().isEmpty(), "the winner eats, seed " + seed);
        }
    }

    @Test
    void aZeroEatIntervalIsRejected() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new HumanParameters(0, 1, 15, 3));
    }

    @Test
    void aZeroBreedIntervalIsRejected() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new HumanParameters(15, 1, 0, 3));
    }

    @Test
    void aZeroIntervalIsRejectedBySettersToo() {
        HumanParameters parameters = new HumanParameters();
        Assertions.assertThrows(IllegalArgumentException.class, () -> parameters.setEatInterval(0));
        Assertions.assertThrows(IllegalArgumentException.class, () -> parameters.setBreedInterval(-1));
    }
}
