package Test;

import SimulationApplication.GridContent.Entity.Human.Behaviour;
import SimulationApplication.GridContent.Entity.Human.Human;
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

        Behaviour behaviour = new Behaviour(gridWorld, null, 0,new float[]{1});
        Human human = new Human(gridWorld, new GridPosition(2,2), behaviour);
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