package SimulationApplication.GridContent.Entity.Human;

import SimulationApplication.GridContent.Entity.Entity;
import SimulationApplication.GridContent.Food;
import SimulationApplication.GridContent.Manager;
import SimulationApplication.GridPosition;
import SimulationApplication.GridWorld;

import java.util.ArrayList;
import java.util.Hashtable;

public class HumanManager implements Manager {
    private GridWorld gridWorld;

    private ArrayList<Human> humans = new ArrayList<>();

    public HumanManager(GridWorld gridWorld){
        this.gridWorld = gridWorld;
    }

    public void addHuman(Human human){
        this.humans.add(human);
    }

    public void removeHuman(Human human){
        this.humans.remove(human);
    }

    public void advanceTime() throws Exception {
        // Survived, died and bred entities
        ArrayList<Entity> diedEntities = new ArrayList<>();
        ArrayList<Entity> bredEntities = new ArrayList<>();

        // Food that is going to be eaten
        Hashtable<Food, ArrayList<Human>> food = new Hashtable<>();

        // Entity action, check alive and check breeding
        for(Human human : this.humans){
            human.action();
            if (!human.checkAlive()){
                diedEntities.add(human);
            }
            if (human.checkBred()){
                bredEntities.add(human);
            }
        }

        // Remove died entities from grid
        for(Entity entity : diedEntities){
            gridWorld.removeContent(entity);
        }

        // Create babies
        for(Entity entity : bredEntities){
            entity.createChild();
        }
    }

    public ArrayList<GridPosition> getAllHumanPositions(){
        ArrayList<GridPosition> result = new ArrayList<>();

        for(Human human : humans){
            result.add(human.getGridPosition());
        }

        return result;
    }

    public ArrayList<Integer> getAllHumanIds(){
        ArrayList<Integer> result = new ArrayList<>();

        for(Human human : humans){
            result.add(gridWorld.getId(human));
        }

        return result;
    }
}
