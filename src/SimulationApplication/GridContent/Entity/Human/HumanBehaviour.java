package SimulationApplication.GridContent.Entity.Human;

import SimulationApplication.*;
import SimulationApplication.GridContent.Food;

import java.util.Arrays;
import java.util.Random;

public class HumanBehaviour {
    private GridWorld gridWorld;

    private Human human;

    private MovementBehaviour movementBehaviour;

    private int viewRange;

    private Random random = new Random();

    public HumanBehaviour(GridWorld gridWorld, Human human){
        this.gridWorld = gridWorld;
        this.human = human;

        this.viewRange = random.nextInt(3,6);

        this.movementBehaviour = new MovementBehaviour(gridWorld, human);
    }
    public HumanBehaviour(GridWorld gridWorld, Human human, int viewRange, MovementBehaviour movementBehaviour){
        this.gridWorld = gridWorld;
        this.human = human;
        this.viewRange = viewRange;

        this.movementBehaviour = movementBehaviour;
    }

    public MovementAction getNextAction(){
        return this.movementBehaviour.getMovementAction();
    }

    public void setHuman(Human human){
        this.human = human;
        this.movementBehaviour.setHuman(human);
    }

    public int getViewRange(){
        return this.viewRange;
    }

    public HumanBehaviour createVariation() throws Exception {
        int viewRangeR = random.nextInt(-1,2);
        int newViewingRange = this.viewRange + viewRangeR;
        MovementBehaviour newMovementBehaviour = this.movementBehaviour.createVariation();

        return new HumanBehaviour(this.gridWorld, this.human, newViewingRange, newMovementBehaviour);
    }

    @Override
    public String toString() {
        return "HumanBehaviour{" + "\n" +
                "viewRange=" + viewRange + "\n" +
                "movementBehaviour=" + movementBehaviour + "\n" +
                '}';
    }
}
