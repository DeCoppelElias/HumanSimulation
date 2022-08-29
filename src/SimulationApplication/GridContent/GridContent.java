package SimulationApplication.GridContent;

import SimulationApplication.GridPosition;
import SimulationApplication.GridWorld;

import java.util.Random;

public abstract class GridContent {
    protected int id;

    protected GridWorld gridWorld;

    private GridPosition gridPosition;

    protected Random random = new Random();

    public GridContent(GridWorld gridWorld, int x, int y) throws Exception {
        this.gridPosition = new GridPosition(x, y);

        this.gridWorld = gridWorld;
        this.id = gridWorld.addContent(this);
    }

    public GridContent(GridWorld gridWorld, GridPosition gridPosition) throws Exception {
        this.gridPosition = gridPosition;

        this.gridWorld = gridWorld;
        this.id = gridWorld.addContent(this);
    }

    public GridPosition getGridPosition(){
        return this.gridPosition;
    }

    public int getId(){
        return id;
    }

    public void move(int x, int y){
        GridPosition oldGridPosition = this.gridPosition;

        this.gridPosition = new GridPosition(x, y);

        this.gridWorld.refreshContent(this, oldGridPosition);
    }

    public void move(GridPosition newGridPosition){
        GridPosition oldGridPosition = this.gridPosition;

        this.gridPosition = newGridPosition;

        this.gridWorld.refreshContent(this, oldGridPosition);
    }

    public abstract Boolean checkDisplayability();

    @Override
    public abstract String toString();

    public abstract String gridString();

    @Override
    public abstract Object clone();

    public Object cloneAt(GridPosition gridPosition){
        GridContent clone = (GridContent) this.clone();
        clone.move(gridPosition);
        return clone;
    }
}
