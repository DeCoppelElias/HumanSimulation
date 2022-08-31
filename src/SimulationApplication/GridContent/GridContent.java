package SimulationApplication.GridContent;

import SimulationApplication.GridPosition;
import SimulationApplication.GridWorld;

import java.util.Random;

public abstract class GridContent {
    private Boolean active = true;

    protected GridWorld gridWorld;

    private GridPosition gridPosition;

    public void setActive(Boolean active){
        this.active = active;
    }

    public Boolean getActive(){
        return active;
    }

    public GridContent(GridWorld gridWorld, GridPosition gridPosition) {
        this.gridPosition = gridPosition;
        this.gridWorld = gridWorld;
        gridWorld.addContent(this);
    }

    public GridPosition getGridPosition(){
        return this.gridPosition;
    }

    public void move(GridPosition newGridPosition){
        GridPosition oldGridPosition = this.gridPosition;

        this.gridPosition = newGridPosition;

        if(this.gridWorld == null) return;
        this.gridWorld.refreshContent(this, oldGridPosition);
    }

    public abstract Boolean checkDisplayability();

    @Override
    public abstract String toString();

    public abstract String gridString();


    @Override
    public abstract Object clone();
}
