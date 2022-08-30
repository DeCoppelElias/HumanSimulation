package SimulationApplication.GridContent.Entity.Human;

import SimulationApplication.*;
import SimulationApplication.GridContent.*;
import SimulationApplication.GridContent.Entity.*;

import java.util.Random;

public class Human extends Entity {
    private int days;
    private int foodAmount;

    private HumanBehaviour humanBehaviour;

    public Human(GridWorld gridWorld, GridPosition gridPosition) throws Exception {
        super(gridWorld, gridPosition);

        this.humanBehaviour = new HumanBehaviour(gridWorld, this);
        this.foodAmount = 0;
        this.days = 1;
    }

    public Human(GridWorld gridWorld, GridPosition gridPosition, HumanBehaviour humanBehaviour) throws Exception {
        super(gridWorld, gridPosition);

        this.humanBehaviour = humanBehaviour;
        humanBehaviour.setHuman(this);

        this.foodAmount = 0;
        this.days = 0;
    }

    @Override
    public void action() throws Exception {
        days++;
        if(!this.alive) return;

        collectFoodAction();
        movementAction();

        if(days % 15 == 0){
            eat();
            breed();
        }
    }

    private void breed() {
        if(this.foodAmount <= 3) return;
        this.foodAmount -= 4;
        this.bred = true;
    }

    private void eat() {
        this.foodAmount--;
        if(this.foodAmount < 0){
            this.alive = false;
        }
    }

    private void movementAction() throws Exception {
        MovementAction movementAction = humanBehaviour.getNextAction();
        GridPosition newGridPosition = new GridPosition(this.getGridPosition().getX() + movementAction.getAddition_X(), this.getGridPosition().getY() + movementAction.getAddition_Y());

        int counter = 0;
        while(!this.gridWorld.isWithinBounds(newGridPosition) && counter < 10){
            movementAction = humanBehaviour.getNextAction();
            newGridPosition = new GridPosition(this.getGridPosition().getX() + movementAction.getAddition_X(), this.getGridPosition().getY() + movementAction.getAddition_Y());

            counter++;
        }
        if(counter < 10){
            this.move(newGridPosition);
        }
    }

    private void collectFoodAction() throws Exception {
        if(gridWorld.containsFood(this.getGridPosition())){
            Food food = gridWorld.collectFood(this.getGridPosition());
            if(food == null) throw new Exception("Human tried to collect food but no food was present");
            foodAmount++;
        }
    }

    public static void spawnRandomClones(GridWorld gridWorld, int amount) throws Exception {
        Random random = new Random();
        for(int i = 0; i < amount; i++){
            int randomX = random.nextInt(0,gridWorld.getWidth());
            int randomY = random.nextInt(0, gridWorld.getHeight());
            GridPosition gridPosition = new GridPosition(randomX,randomY);

            Human human = new Human(gridWorld, gridPosition);
        }
    }

    @Override
    public Boolean checkDisplayability() {
        return this.foodAmount >= 0;
    }

    @Override
    public void createChild() throws Exception {
        HumanBehaviour humanBehaviour = this.humanBehaviour.createVariation();
        Human human = new Human(this.gridWorld, this.getGridPosition(), humanBehaviour);
    }

    @Override
    public String toString() {
        return "Human{" + "\n" +
                "behaviour=" + humanBehaviour + "\n" +
                "food=" + foodAmount + "\n" +
                "days=" + days + "\n" +
                '}';
    }

    @Override
    public String gridString() {
        return "HUMAN";
    }

    @Override
    public Object clone() {
        try {
            return new Human(this.gridWorld, this.getGridPosition(), this.humanBehaviour);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public int getViewRange() {
        return this.humanBehaviour.getViewRange();
    }
}
