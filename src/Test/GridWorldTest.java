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
    }
    @Test
    public void systemTest() throws Exception {
        GridWorld gridWorld = new GridWorld(15,15);

        for (int i = 0; i < 20; i++) {
            gridWorld.advanceTime();
            System.out.println(gridWorld);
        }
    }
}