package SimulationApplication.GridContent.Entity.Human;

import SimulationApplication.*;
import SimulationApplication.GridContent.*;
import SimulationApplication.GridContent.Entity.*;

import java.util.Random;

public class Human extends Entity {
    private int days;
    private int foodAmount;

    private HumanParameters humanParameters;

    private HumanBehaviour humanBehaviour;

    public Human(GridWorld gridWorld, GridPosition gridPosition, HumanParameters humanParameters) {
        super(gridWorld, gridPosition);

        this.humanBehaviour = new HumanBehaviour(gridWorld, this);
        this.foodAmount = 0;
        this.days = 1;
        this.humanParameters = humanParameters;
    }

    public Human(GridWorld gridWorld, GridPosition gridPosition, HumanParameters humanParameters, HumanBehaviour humanBehaviour) throws Exception {
        super(gridWorld, gridPosition);

        this.humanBehaviour = humanBehaviour;
        humanBehaviour.setHuman(this);

        this.foodAmount = 0;
        this.days = 0;
        this.humanParameters = humanParameters;
    }

    @Override
    public void action() throws Exception {
        days++;
        if(!this.alive) return;

        collectFoodAction();
        movementAction();

        if(days % humanParameters.getEatInterval() == 0){
            eat();
        }
        if(days % humanParameters.getBreedInterval() == 0){
            breed();
        }
    }

    private void breed() {
        if(this.foodAmount < humanParameters.getBreedCost()) return;
        this.foodAmount -= humanParameters.getBreedCost();
        this.bred = true;
    }

    private void eat() {
        this.foodAmount -= humanParameters.getEatCost();
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

    @Override
    public Boolean checkDisplayability() {
        return this.foodAmount >= 0;
    }

    @Override
    public void createChildSpecific() throws Exception {
        HumanBehaviour humanBehaviour = this.humanBehaviour.createVariation();
        Human human = new Human(this.gridWorld, this.getGridPosition(), this.humanParameters, humanBehaviour);
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
            return new Human(this.gridWorld, this.getGridPosition(), this.humanParameters, this.humanBehaviour);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public int getViewRange() {
        return this.humanBehaviour.getViewRange();
    }
}
