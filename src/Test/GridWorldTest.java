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

class GridWorldTest {

    @Test
    public void toStringTest() throws Exception {
        GridWorld gridWorld = new GridWorld(3,6);
        Assertions.assertEquals(gridWorld.toString(),
                "Empty Empty Empty \n" +
                        "Empty Empty Empty \n" +
                        "Empty Empty Empty \n" +
                        "Empty Empty Empty \n" +
                        "Empty Empty Empty \n" +
                        "Empty Empty Empty \n");
    }
    @Test
    public void singleHumanWalkingToFoodTest() throws Exception {
        GridWorld gridWorld = new GridWorld(3,3);

        // Adding Human to GridWorld
        MovementBehaviour movementBehaviour = new MovementBehaviour(gridWorld,
                null, 0,1,new double[]{1},
                new FindFoodBehaviour(gridWorld, null));
        Human human = new Human(gridWorld,
                new GridPosition(0,0),
                new HumanParameters(),
                movementBehaviour,
                new FoodBehaviour(null));

        Assertions.assertEquals(gridWorld.toString(),
                "Empty Empty Empty \n" +
                        "Empty Empty Empty \n" +
                        "HUMAN Empty Empty \n");

        // Adding Food to GridWorld
        Food food = new Food(gridWorld, new GridPosition(2,2));

        Assertions.assertEquals(gridWorld.toString(),
                "Empty Empty FOOD \n" +
                        "Empty Empty Empty \n" +
                        "HUMAN Empty Empty \n");

        // Advancing Time
        gridWorld.advanceTime();
        Assertions.assertEquals(gridWorld.toString(),
                "Empty Empty FOOD \n" +
                        "Empty Empty Empty \n" +
                        "Empty HUMAN Empty \n");

        gridWorld.advanceTime();
        Assertions.assertEquals(gridWorld.toString(),
                "Empty Empty FOOD \n" +
                        "Empty Empty Empty \n" +
                        "Empty Empty HUMAN \n");

        gridWorld.advanceTime();
        Assertions.assertEquals(gridWorld.toString(),
                "Empty Empty FOOD \n" +
                        "Empty Empty HUMAN \n" +
                        "Empty Empty Empty \n");

        Assertions.assertEquals(human.getFoodAmount(), 0);

        gridWorld.advanceTime();
        Assertions.assertEquals(gridWorld.toString(),
                "Empty Empty HUMAN \n" +
                        "Empty Empty Empty \n" +
                        "Empty Empty Empty \n");

        Assertions.assertEquals(human.getFoodAmount(), 1);
    }

    @Test
    public void fightingForFoodTest() throws Exception {
        GridWorld gridWorld = new GridWorld(3,3);

        // Adding 2 aggressive Humans to GridWorld
        MovementBehaviour movementBehaviour = new MovementBehaviour(gridWorld,
                null, 0,1,new double[]{1},
                new FindFoodBehaviour(gridWorld, null));

        FoodBehaviour foodBehaviour = new FoodBehaviour(null,1,0);
        Human human1 = new Human(gridWorld,
                new GridPosition(2,1),
                new HumanParameters(),
                movementBehaviour,
                foodBehaviour);

        Human human2 = new Human(gridWorld,
                new GridPosition(2,1),
                new HumanParameters(),
                movementBehaviour,
                foodBehaviour);

        // Adding Food to GridWorld
        Food food = new Food(gridWorld, new GridPosition(2,2));

        // Humans fight for food
        gridWorld.advanceTime();
        Assertions.assertTrue(gridWorld.toString().equals(
                "Empty Empty HUMAN \n" +
                        "Empty Empty Empty \n" +
                        "Empty Empty Empty \n")
                || gridWorld.toString().equals(
                "Empty Empty FOOD \n" +
                        "Empty Empty Empty \n" +
                        "Empty Empty Empty \n"));

        Assertions.assertTrue(!(human1.checkAlive() && human2.checkAlive()));
    }
}