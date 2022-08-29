package SimulationApplication.GridContent.Entity;

import SimulationApplication.GridContent.GridContent;
import SimulationApplication.GridPosition;
import SimulationApplication.GridWorld;

public abstract class Entity extends GridContent {
    protected Boolean alive;
    protected Boolean bred;

    public abstract void action() throws Exception;

    public Entity(GridWorld gridWorld, GridPosition gridPosition) throws Exception {
        super(gridWorld, gridPosition);
        this.alive = true;
        this.bred = false;
    }

    public Boolean checkAlive() {
        return alive;
    }

    public Boolean checkBred() {
        return bred;
    }

    public abstract void createChild() throws Exception ;

    @Override
    public abstract Object clone();
}
