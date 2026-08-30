package Test;

import SimulationApplication.GridContent.Entity.Human.Human;
import SimulationApplication.GridPosition;
import SimulationApplication.GridWorld;
import SimulationApplication.GridWorldManager;
import java.util.ArrayList;
import java.util.Hashtable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class GridWorldManagerTest {

    @Test
    void sortOnSurvivalPutsTheLongestLivedFirst() throws Exception {
        GridWorld world = TestWorlds.seeded(5, 5, 1L);
        Human older = TestWorlds.walker(world, new GridPosition(0, 0), TestWorlds.inert(), 0);
        world.advanceTime();
        world.advanceTime();
        world.advanceTime();
        Human younger = TestWorlds.walker(world, new GridPosition(4, 4), TestWorlds.inert(), 0);
        world.advanceTime();

        GridWorldManager manager = new GridWorldManager(world);
        ArrayList<Integer> sorted = manager.sortOnSurvival(manager.getAllHumans());

        Assertions.assertEquals(world.getId(older), sorted.get(0));
        Assertions.assertEquals(world.getId(younger), sorted.get(1));
    }

    @Test
    void sortOnFoodPutsTheBestFedFirst() throws Exception {
        GridWorld world = TestWorlds.seeded(5, 5, 1L);
        Human poor = TestWorlds.walker(world, new GridPosition(0, 0), TestWorlds.inert(), 0);
        Human rich = TestWorlds.walker(world, new GridPosition(4, 4), TestWorlds.inert(), 0);
        rich.addFood(9);

        GridWorldManager manager = new GridWorldManager(world);
        ArrayList<Integer> sorted = manager.sortOnFood(manager.getAllHumans());

        Assertions.assertEquals(world.getId(rich), sorted.get(0));
        Assertions.assertEquals(world.getId(poor), sorted.get(1));
    }

    @Test
    void appliedParametersReachTheModel() {
        GridWorld world = TestWorlds.seeded(5, 5, 1L);
        GridWorldManager manager = new GridWorldManager(world);

        Hashtable<String, Integer> changes = new Hashtable<>();
        changes.put("Human Eating Cost", 7);
        changes.put("Spawn Food Amount", 3);
        manager.applyParameters(changes);

        Hashtable<String, Integer> applied = manager.getHumanParameterInfo();
        Assertions.assertEquals(7, applied.get("Human Eating Cost"));
        Assertions.assertEquals(3, applied.get("Spawn Food Amount"));
    }

    @Test
    void unknownParametersAreIgnored() {
        GridWorld world = TestWorlds.seeded(5, 5, 1L);
        GridWorldManager manager = new GridWorldManager(world);
        int before = manager.getHumanParameterInfo().get("Human Eating Cost");

        Hashtable<String, Integer> changes = new Hashtable<>();
        changes.put("Not A Parameter", 99);
        manager.applyParameters(changes);

        Assertions.assertEquals(before, manager.getHumanParameterInfo().get("Human Eating Cost"));
    }
}
