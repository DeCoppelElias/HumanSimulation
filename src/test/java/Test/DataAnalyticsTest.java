package Test;

import SimulationApplication.GridPosition;
import SimulationApplication.GridWorld;
import java.util.Hashtable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DataAnalyticsTest {

    @Test
    void entityInfoCountsWhatWasSpawned() {
        GridWorld world = TestWorlds.seeded(5, 5, 1L);
        world.spawnHumanPosition(3, new GridPosition(0, 0));
        world.spawnFoodPosition(2, new GridPosition(4, 4));

        Hashtable<String, Integer> info = world.getDataAnalytics().getEntityInfo();

        Assertions.assertEquals(3, info.get("HUMAN"));
        Assertions.assertEquals(2, info.get("FOOD"));
    }

    @Test
    void entityInfoCountsZeroOnAnEmptyWorld() {
        GridWorld world = TestWorlds.seeded(5, 5, 1L);

        Hashtable<String, Integer> info = world.getDataAnalytics().getEntityInfo();

        Assertions.assertEquals(0, info.get("HUMAN"));
        Assertions.assertEquals(0, info.get("FOOD"));
    }
}
