package SimulationApplication.GridContent.Entity.Human;

import SimulationApplication.*;
import SimulationApplication.GridContent.*;
import SimulationApplication.GridContent.Entity.*;

import java.util.Random;

public class Human extends Entity {
    private int viewRange;
    private int days;
    private int foodAmount;

    private Behaviour behaviour;

    public Human(GridWorld gridWorld, GridPosition gridPosition) throws Exception {
        super(gridWorld, gridPosition);

        this.behaviour = new Behaviour(gridWorld, this);
        this.foodAmount = 0;
        this.viewRange = 5;
        this.days = 1;
    }

    @Override
    public Boolean checkDisplayability() {
        return this.foodAmount >= 0;
    }

    public Human(GridWorld gridWorld, GridPosition gridPosition, Behaviour behaviour) throws Exception {
        super(gridWorld, gridPosition);

        this.behaviour = behaviour;
        behaviour.entity = this;
        this.foodAmount = 0;
        this.days = 0;
    }

    public int getViewRange(){
        return viewRange;
    }

    @Override
    public void action() throws Exception {
        days++;
        if(!this.alive) return;

        collectFoodAction();
        movementAction();

        if(days % 7 == 0){
            eat();
            breed();
        }
    }

    private void breed() {
        if(this.foodAmount <= 0) return;
        this.foodAmount--;
        this.bred = true;
    }

    private void eat() {
        this.foodAmount--;
        if(this.foodAmount < 0){
            this.alive = false;
        }
    }

    private void movementAction() throws Exception {
        Action action = behaviour.getNextAction();
        GridPosition newGridPosition = new GridPosition(this.getGridPosition().getX() + action.getAddition_X(), this.getGridPosition().getY() + action.getAddition_Y());

        int counter = 0;
        while(!this.gridWorld.isWithinBounds(newGridPosition) && counter < 10){
            action = behaviour.getNextAction();
            newGridPosition = new GridPosition(this.getGridPosition().getX() + action.getAddition_X(), this.getGridPosition().getY() + action.getAddition_Y());

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
    public void createChild() throws Exception {
        Behaviour behaviour = this.behaviour.createVariation();
        Human human = new Human(this.gridWorld, this.getGridPosition(), behaviour);
    }

    @Override
    public String toString() {
        return "Human{" + "\n" +
                "behaviour=" + behaviour + "\n" +
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
            return new Human(this.gridWorld, this.getGridPosition(), this.behaviour);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
