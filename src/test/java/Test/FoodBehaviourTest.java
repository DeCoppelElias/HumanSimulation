package Test;

import SimulationApplication.GridContent.Entity.Human.HumanBehaviour.FoodBehaviour;
import SimulationApplication.GridWorld;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class FoodBehaviourTest {

    @Test
    void anAllAggressiveHumanIsAlwaysAggressive() {
        GridWorld world = TestWorlds.seeded(3, 3, 1L);
        FoodBehaviour behaviour = new FoodBehaviour(world.getRandom(), null, 1, 0);

        for (int roll = 0; roll < 200; roll++) {
            Assertions.assertEquals("aggressive", behaviour.getBehaviour());
        }
    }

    @Test
    void anAllPeacefulHumanIsNeverAggressive() {
        GridWorld world = TestWorlds.seeded(3, 3, 1L);
        FoodBehaviour behaviour = new FoodBehaviour(world.getRandom(), null, 0, 1);

        for (int roll = 0; roll < 200; roll++) {
            Assertions.assertEquals("peacefully", behaviour.getBehaviour());
        }
    }

    @Test
    void aMixedHumanDoesBoth() {
        GridWorld world = TestWorlds.seeded(3, 3, 1L);
        FoodBehaviour behaviour = new FoodBehaviour(world.getRandom(), null, 0.5, 0.5);

        boolean sawAggressive = false;
        boolean sawPeaceful = false;
        for (int roll = 0; roll < 200; roll++) {
            if (behaviour.getBehaviour().equals("aggressive")) sawAggressive = true;
            else sawPeaceful = true;
        }

        Assertions.assertTrue(sawAggressive && sawPeaceful, "a 50/50 human should do both");
    }
}
