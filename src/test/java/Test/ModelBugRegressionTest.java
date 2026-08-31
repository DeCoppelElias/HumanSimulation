package Test;

import SimulationApplication.GridContent.Entity.Human.Human;
import SimulationApplication.GridContent.Entity.Human.HumanBehaviour.FindFoodBehaviour;
import SimulationApplication.GridContent.Entity.Human.HumanBehaviour.FoodBehaviour;
import SimulationApplication.GridContent.Entity.Human.HumanBehaviour.MovementBehaviour;
import SimulationApplication.GridContent.Entity.Human.HumanParameters;
import SimulationApplication.GridContent.Food;
import SimulationApplication.GridPosition;
import SimulationApplication.GridWorld;
import java.util.ArrayList;
import java.util.List;
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
    void stepVariationChangesTheWeightsWhenTheLengthIsUnchanged() throws Exception {
        GridWorld world = TestWorlds.seeded(3, 3, 7L);
        MovementBehaviour behaviour =
                new MovementBehaviour(world, null, 0, 1, new double[] {0.5, 0.5}, new FindFoodBehaviour(world, null));

        boolean changed = false;
        for (int attempt = 0; attempt < 200 && !changed; attempt++) {
            double[] varied = behaviour.createStepVariation(new double[] {0.5, 0.5});
            changed = varied.length == 2 && varied[0] != 0.5;
        }

        Assertions.assertTrue(changed, "the same-length branch never moved a weight");
    }

    @Test
    void theHumanCountsSurviveAStatisticsReset() throws Exception {
        GridWorld world = TestWorlds.seeded(5, 5, 3L);
        world.spawnHumanRandom(2);
        for (int day = 0; day < 5; day++) {
            world.advanceTime();
        }

        world.resetStatistics();
        world.advanceTime();
        world.advanceTime();

        List<Integer> counts = world.getDataAnalytics().humanCountsPerDay();
        Assertions.assertEquals(2, counts.size(), "two days recorded since the reset");
        Assertions.assertFalse(counts.contains(null), "no day is missing its count");
    }

    @Test
    void theHumanCountsCoverEveryRecordedDay() throws Exception {
        GridWorld world = TestWorlds.seeded(5, 5, 3L);
        world.spawnHumanPosition(1, new GridPosition(0, 0));
        for (int day = 0; day < 4; day++) {
            world.advanceTime();
        }

        Assertions.assertEquals(4, world.getDataAnalytics().humanCountsPerDay().size());
    }

    @Test
    void stepVariationAlwaysReturnsAValidDistribution() throws Exception {
        GridWorld world = TestWorlds.seeded(3, 3, 11L);
        MovementBehaviour behaviour =
                new MovementBehaviour(world, null, 0, 1, new double[] {1}, new FindFoodBehaviour(world, null));

        for (int attempt = 0; attempt < 500; attempt++) {
            double[] varied = behaviour.createStepVariation(new double[] {0.3, 0.3, 0.4});

            double sum = 0;
            for (double weight : varied) {
                Assertions.assertTrue(weight >= 0, "negative weight on attempt " + attempt);
                sum += weight;
            }
            Assertions.assertEquals(1.0, sum, 1e-6, "weights do not sum to one on attempt " + attempt);
        }
    }

    @Test
    void movementNeverWalksOffTheStepArray() throws Exception {
        GridWorld world = TestWorlds.seeded(5, 5, 13L);
        MovementBehaviour behaviour =
                new MovementBehaviour(world, null, 1, 0, new double[] {0.2, 0.2}, new FindFoodBehaviour(world, null));

        for (int attempt = 0; attempt < 1000; attempt++) {
            Assertions.assertDoesNotThrow(behaviour::getMovementAction, "attempt " + attempt);
        }
    }

    @Test
    void farthestFromOtherHumansAvoidsTheCrowd() throws Exception {
        GridWorld world = TestWorlds.seeded(10, 10, 1L);
        Human human = TestWorlds.walker(world, new GridPosition(5, 5), TestWorlds.inert(), 0);
        FindFoodBehaviour behaviour = new FindFoodBehaviour(world, human);

        ArrayList<GridPosition> crowd = new ArrayList<>(List.of(new GridPosition(2, 5), new GridPosition(2, 6)));
        ArrayList<GridPosition> food = new ArrayList<>(List.of(new GridPosition(3, 5), new GridPosition(8, 5)));

        GridPosition found = behaviour.findFarthestFromOtherHumansFood(food, crowd);

        Assertions.assertEquals(8, found.getX(), "should walk away from the crowd at x=2");
        Assertions.assertEquals(5, found.getY());
    }

    @Test
    void removingUnknownContentFailsWithoutTouchingTheWorld() throws Exception {
        GridWorld world = TestWorlds.seeded(3, 3, 1L);
        GridWorld elsewhere = TestWorlds.seeded(3, 3, 1L);
        Food resident = new Food(world, new GridPosition(1, 1));
        Food stranger = new Food(elsewhere, new GridPosition(1, 1));

        Exception thrown = Assertions.assertThrows(Exception.class, () -> world.removeContent(stranger));
        Assertions.assertFalse(thrown instanceof NullPointerException, "should be a stated failure, not an NPE");

        Assertions.assertEquals(
                "FOOD", world.getGridTile(new GridPosition(1, 1)).toString());
        Assertions.assertNotNull(world.getId(resident), "the resident keeps its id");
        Assertions.assertNotNull(elsewhere.getId(stranger), "the stranger keeps its own world");
    }

    @Test
    void aChildInheritsAVariedViewRange() throws Exception {
        boolean sawADifferentRange = false;

        for (long seed = 0; seed < 30 && !sawADifferentRange; seed++) {
            GridWorld world = TestWorlds.seeded(3, 3, seed);
            HumanParameters parameters = new HumanParameters(100, 1, 1, 1);
            Human parent = TestWorlds.walker(world, new GridPosition(1, 1), parameters, 0);
            parent.addFood(5);

            world.advanceTime();

            for (int id : world.getAllHumanIds()) {
                if (world.getRange(id) != parent.getViewRange()) sawADifferentRange = true;
            }
        }

        Assertions.assertTrue(sawADifferentRange, "every child copied the parent's view range");
    }

    @Test
    void aViewRangeNeverDropsBelowOne() throws Exception {
        GridWorld world = TestWorlds.seeded(3, 3, 5L);
        HumanParameters parameters = new HumanParameters(100, 1, 1, 1);
        Human parent = TestWorlds.walker(world, new GridPosition(1, 1), parameters, 0);
        parent.addFood(500);

        for (int day = 0; day < 40; day++) {
            world.advanceTime();
            for (int id : world.getAllHumanIds()) {
                Assertions.assertTrue(world.getRange(id) >= 1, "view range fell below 1 on day " + day);
            }
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
