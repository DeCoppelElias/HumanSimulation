package Test;

import SimulationApplication.GridContent.Entity.Human.FindFoodBehaviour;
import SimulationApplication.GridContent.Entity.Human.HumanBehaviour;
import SimulationApplication.GridContent.Entity.Human.Human;
import SimulationApplication.GridContent.Entity.Human.MovementBehaviour;
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

        FindFoodBehaviour findFoodBehaviour = new FindFoodBehaviour(gridWorld, null, 1,0,0,0);
        MovementBehaviour movementBehaviour = new MovementBehaviour(gridWorld, null, 0,1, new double[]{1}, findFoodBehaviour);
        HumanBehaviour humanBehaviour = new HumanBehaviour(gridWorld, null,5, movementBehaviour);
        Human human = new Human(gridWorld, new GridPosition(2,2), humanBehaviour);
        Assertions.assertEquals(gridWorld.toString(),
                "Empty Empty Empty \n" +
                        "Empty Empty Empty \n" +
                        "Empty Empty HUMAN \n" +
                        "Empty Empty Empty \n" +
                        "Empty Empty Empty \n" +
                        "Empty Empty Empty \n");

        human.move(new GridPosition(0,0));
        Assertions.assertEquals(gridWorld.toString(),
                "HUMAN Empty Empty \n" +
                        "Empty Empty Empty \n" +
                        "Empty Empty Empty \n" +
                        "Empty Empty Empty \n" +
                        "Empty Empty Empty \n" +
                        "Empty Empty Empty \n");

        Food food = new Food(gridWorld, new GridPosition(2,2));
        Assertions.assertEquals(gridWorld.toString(),
                "HUMAN Empty Empty \n" +
                        "Empty Empty Empty \n" +
                        "Empty Empty FOOD \n" +
                        "Empty Empty Empty \n" +
                        "Empty Empty Empty \n" +
                        "Empty Empty Empty \n");

        Food food2 = new Food(gridWorld, new GridPosition(2,4));
        System.out.println(human);
        for(int i = 0; i < 20; i++){
            gridWorld.advanceTime();
            System.out.println(gridWorld);
        }
    }
    @Test
    public void systemTest() throws Exception {
        GridWorld gridWorld = new GridWorld(15,15);
        Human.spawnRandomClones(gridWorld, 5);
        Food.spawnRandomClones(gridWorld, 15);

        for (int i = 0; i < 20; i++) {
            gridWorld.advanceTime();
            System.out.println(gridWorld);
        }
    }
}