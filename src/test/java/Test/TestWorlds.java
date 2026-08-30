package Test;

import SimulationApplication.GridContent.Entity.Human.Human;
import SimulationApplication.GridContent.Entity.Human.HumanBehaviour.FindFoodBehaviour;
import SimulationApplication.GridContent.Entity.Human.HumanBehaviour.FoodBehaviour;
import SimulationApplication.GridContent.Entity.Human.HumanBehaviour.MovementBehaviour;
import SimulationApplication.GridContent.Entity.Human.HumanParameters;
import SimulationApplication.GridPosition;
import SimulationApplication.GridWorld;
import java.util.Random;

/** Builders for tests that need a run they can repeat. */
final class TestWorlds {

    private TestWorlds() {}

    static GridWorld seeded(int width, int height, long seed) {
        return new GridWorld(width, height, new Random(seed));
    }

    /** Never eats and never breeds. */
    static HumanParameters inert() {
        return new HumanParameters(100, 1, 100, 1);
    }

    /** Walks straight at the food it can see, one tile per day. */
    static Human walker(GridWorld world, GridPosition at, HumanParameters parameters, double aggressive)
            throws Exception {
        MovementBehaviour movement =
                new MovementBehaviour(world, null, 0, 1, new double[] {1}, new FindFoodBehaviour(world, null));
        FoodBehaviour food = new FoodBehaviour(world.getRandom(), null, aggressive, 1 - aggressive);
        return new Human(world, at, parameters, movement, food);
    }
}
