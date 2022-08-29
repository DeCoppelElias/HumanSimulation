package SimulationApplication;

import java.util.Random;

public class Food extends GridContent {
    public Food(GridWorld gridWorld, GridPosition gridPosition) throws Exception {
        super(gridWorld, gridPosition);
    }

    public static void spawnRandomClones(GridWorld gridWorld, int amount) throws Exception {
        Random random = new Random();
        for(int i = 0; i < amount; i++){
            int randomX = random.nextInt(0,gridWorld.getWidth());
            int randomY = random.nextInt(0, gridWorld.getHeight());
            GridPosition gridPosition = new GridPosition(randomX,randomY);

            Food food = new Food(gridWorld, gridPosition);
        }
    }

    @Override
    public String toString() {
        return "Food";
    }

    @Override
    public String gridString() {
        return "FOOD";
    }

    @Override
    public Object clone() {
        try {
            return new Food(this.gridWorld, this.getGridPosition());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
