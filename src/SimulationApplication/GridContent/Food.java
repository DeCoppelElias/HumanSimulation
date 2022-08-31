package SimulationApplication.GridContent;

import SimulationApplication.GridPosition;
import SimulationApplication.GridWorld;

import java.util.Random;

public class Food extends GridContent {
    public Food(GridWorld gridWorld, GridPosition gridPosition) {
        super(gridWorld, gridPosition);
    }

    @Override
    public Boolean checkDisplayability() {
        return true;
    }

    @Override
    public String toString() {
        return "Food: " + this.getActive();
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
