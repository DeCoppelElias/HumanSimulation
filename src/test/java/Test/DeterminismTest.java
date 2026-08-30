package Test;

import SimulationApplication.GridWorld;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DeterminismTest {

    private String run(long seed) throws Exception {
        GridWorld world = TestWorlds.seeded(10, 10, seed);
        world.spawnHumanRandom(5);
        world.spawnFoodRandom(20);

        StringBuilder trace = new StringBuilder();
        for (int day = 0; day < 30; day++) {
            world.advanceTime();
            trace.append(world.toString());
        }
        return trace.toString();
    }

    @Test
    void theSameSeedProducesTheSameRun() throws Exception {
        Assertions.assertEquals(run(4242L), run(4242L));
    }

    @Test
    void differentSeedsProduceDifferentRuns() throws Exception {
        Assertions.assertNotEquals(run(1L), run(2L));
    }
}
