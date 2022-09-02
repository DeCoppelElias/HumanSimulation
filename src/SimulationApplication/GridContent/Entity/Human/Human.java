package SimulationApplication.GridContent.Entity.Human;

import SimulationApplication.*;
import SimulationApplication.GridContent.*;
import SimulationApplication.GridContent.Entity.*;
import SimulationApplication.GridContent.Entity.Human.HumanBehaviour.FoodBehaviour;
import SimulationApplication.GridContent.Entity.Human.HumanBehaviour.MovementBehaviour;

import java.util.Hashtable;
import java.util.Random;

public class Human extends Entity {
    private int days;
    private float foodAmount;

    private int viewRange = 4;


    private Food collectingFood;

    private HumanParameters humanParameters;

    private MovementBehaviour movementBehaviour;
    private FoodBehaviour foodBehaviour;

    public Human(GridWorld gridWorld, GridPosition gridPosition, HumanParameters humanParameters) {
        super(gridWorld, gridPosition);

        this.movementBehaviour = new MovementBehaviour(gridWorld, this);
        this.foodBehaviour = new FoodBehaviour(this);
        this.foodAmount = 0;
        this.days = 1;
        this.humanParameters = humanParameters;
    }

    public Human(GridWorld gridWorld, GridPosition gridPosition, HumanParameters humanParameters, MovementBehaviour movementBehaviour, FoodBehaviour foodBehaviour) throws Exception {
        super(gridWorld, gridPosition);

        this.movementBehaviour = movementBehaviour;
        movementBehaviour.setHuman(this);

        this.foodBehaviour = foodBehaviour;
        foodBehaviour.setHuman(this);

        this.foodAmount = 0;
        this.days = 0;
        this.humanParameters = humanParameters;
    }

    public int getDays() {
        return days;
    }

    public float getFoodAmount() {
        return foodAmount;
    }

    public int getViewRange(){
        return this.viewRange;
    }

    @Override
    public void action() throws Exception {
        // If not alive anymore, return
        if(!this.alive) return;

        // Increase days alive
        days++;

        // Eat after x amount of days
        if(days % humanParameters.getEatInterval() == 0){
            eat();
        }

        // If not alive anymore, return
        if(!this.alive) return;

        // Movement
        movementAction();

        // Check for food
        if(gridWorld.containsFood(this.getGridPosition())){
            gridWorld.collectFood(this.getGridPosition(), this);
        }

        // Check if breed
        if(days % humanParameters.getBreedInterval() == 0){
            if(this.foodAmount < humanParameters.getBreedCost()){
                this.foodAmount -= humanParameters.getBreedCost();
                this.bred = true;
            }
        }
    }

    public void addFood(float foodAmount){
        this.foodAmount += foodAmount;
    }

    public FoodBehaviour getFoodBehaviour(){
        return this.foodBehaviour;
    }

    private void eat() {
        this.foodAmount -= humanParameters.getEatCost();
        if(this.foodAmount < 0){
            this.alive = false;
        }
    }

    private void movementAction() throws Exception {
        MovementAction movementAction = movementBehaviour.getMovementAction();
        GridPosition newGridPosition = new GridPosition(this.getGridPosition().getX() + movementAction.getAddition_X(), this.getGridPosition().getY() + movementAction.getAddition_Y());

        int counter = 0;
        while(!this.gridWorld.isWithinBounds(newGridPosition) && counter < 10){
            movementAction = movementBehaviour.getMovementAction();
            newGridPosition = new GridPosition(this.getGridPosition().getX() + movementAction.getAddition_X(), this.getGridPosition().getY() + movementAction.getAddition_Y());

            counter++;
        }
        if(counter < 10){
            this.move(newGridPosition);
        }
    }

    @Override
    public Boolean checkDisplayability() {
        return this.foodAmount >= 0;
    }

    @Override
    public Hashtable<String, Float> getInfo() {
        Hashtable<String, Float> info = new Hashtable<>();

        info.put("Days", (float) this.days);
        info.put("Food", this.foodAmount);

        return info;
    }

    @Override
    public void createChildSpecific() throws Exception {
        Random random = new Random();
        int viewRangeR = random.nextInt(-1,2);
        int newViewingRange = this.viewRange + viewRangeR;

        MovementBehaviour movementBehaviour = this.movementBehaviour.createVariation();
        FoodBehaviour foodBehaviour = this.foodBehaviour.createVariation();
        Human human = new Human(this.gridWorld, this.getGridPosition(), this.humanParameters, movementBehaviour, foodBehaviour);
    }

    @Override
    public String toString() {
        return "Human{" + "\n" +
                "human behaviour=" + movementBehaviour + "\n" +
                "food behaviour=" + foodBehaviour + "\n" +
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
            return new Human(this.gridWorld, this.getGridPosition(), this.humanParameters, this.movementBehaviour, this.foodBehaviour);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
