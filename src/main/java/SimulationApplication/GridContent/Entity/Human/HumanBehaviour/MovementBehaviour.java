package SimulationApplication.GridContent.Entity.Human.HumanBehaviour;

import SimulationApplication.GridContent.Entity.Human.Human;
import SimulationApplication.GridPosition;
import SimulationApplication.MovementAction;
import SimulationApplication.GridWorld;

import java.util.Arrays;
import java.util.Random;

public class MovementBehaviour extends Behaviour {
    private double[] stepAmount;

    private Human human;

    private GridWorld gridWorld;

    private Random random = new Random();

    private FindFoodBehaviour findFoodBehaviour;

    private GridPosition selectedFoodPosition;

    private double randomMovement;
    private double toFoodMovement;

    public MovementBehaviour(GridWorld gridWorld, Human human){
        this.gridWorld = gridWorld;
        this.human = human;
        this.findFoodBehaviour = new FindFoodBehaviour(gridWorld,human);

        generateRandomChances();
    }
    public MovementBehaviour(GridWorld gridWorld, Human human, double randomMovement, double toFoodMovement, double[] stepAmount, FindFoodBehaviour findFoodBehaviour) throws Exception {
        this.gridWorld = gridWorld;
        this.human = human;

        double sum = randomMovement + toFoodMovement;
        if(Math.abs(sum - 1) > 0.01)
            throw new Exception("Chances add up to more than one: " + sum);

        this.findFoodBehaviour = findFoodBehaviour;
        this.randomMovement = randomMovement;
        this.toFoodMovement = toFoodMovement;
        this.stepAmount = stepAmount;
    }

    public void generateRandomChances(){
        double[] chances = generateRandomArray(2);

        this.randomMovement = chances[0];
        this.toFoodMovement = chances[1];

        int maxDistance = random.nextInt(1,4);
        this.stepAmount = generateRandomArray(maxDistance);
    }

    public double[] generateRandomArray(int size){
        double[] chances = new double[size];
        int[] checkFilled = new int[size];
        double chanceLeft = 1;
        for(int i = 0; i < size; i++){
            int pos = random.nextInt(0,size);
            while(checkFilled[pos] == 1){
                pos = random.nextInt(0,size);
            }
            double chance = 0;
            if(i == size - 1 || chanceLeft <= 0){
                chance = chanceLeft;
            }
            else{
                chance = Math.round(random.nextDouble(0,chanceLeft) * 100.0) / 100.0;
            }

            chanceLeft -= chance;
            checkFilled[pos] = 1;
            chances[pos] = chance;
        }

        return chances;
    }

    public void setHuman(Human human){
        this.human = human;
        this.findFoodBehaviour.setHuman(human);
    }

    public MovementAction getMovementAction(){
        double chance = random.nextDouble();
        if(chance < randomMovement){
            return generateRandomAction();
        }
        else {
            return generateToFoodAction();
        }
    }

    private MovementAction generateRandomAction(){
        int r = random.nextInt(5);

        int distance = 1;
        float distanceR = random.nextFloat();
        while(distanceR > stepAmount[distance - 1]){
            distanceR -= stepAmount[distance - 1];
            distance += 1;
        }

        switch (r){
            case 0:
                return new MovementAction(distance,0);
            case 1:
                return new MovementAction(-distance,0);
            case 2:
                return new MovementAction(0,-distance);
            case 3:
                return new MovementAction(0,distance);
            default:
                return new MovementAction(0,0);
        }
    }

    private MovementAction generateToFoodAction(){
        if(selectedFoodPosition == null || !gridWorld.containsFood(selectedFoodPosition)){
            this.selectedFoodPosition = findFoodBehaviour.findFood();
        }

        if(this.selectedFoodPosition == null) return generateRandomAction();

        GridPosition entityGridPosition = this.human.getGridPosition();

        int distance = 1;
        float distanceR = random.nextFloat();
        while(distanceR > stepAmount[distance - 1]){
            distanceR -= stepAmount[distance - 1];
            distance += 1;
        }

        GridPosition targetGridPosition = this.selectedFoodPosition;

        if(targetGridPosition.getX() > entityGridPosition.getX()){
            return new MovementAction(distance,0);
        }
        else if(targetGridPosition.getX() < entityGridPosition.getX()){
            return new MovementAction(-distance,0);
        }
        else if(targetGridPosition.getY() > entityGridPosition.getY()){
            return new MovementAction(0,distance);
        }
        else if(targetGridPosition.getY() < entityGridPosition.getY()){
            return new MovementAction(0,-distance);
        }
        else return new MovementAction(0,0);
    }

    public MovementBehaviour createVariation() throws Exception {
        double r = (double)Math.round(random.nextDouble(0,0.2d) * 100) / 100;
        double[] chances = new double[]{this.randomMovement, this.toFoodMovement};
        int index1 = random.nextInt(0,chances.length);
        int index2 = random.nextInt(0,chances.length);
        while(index1 == index2){
            index2 = random.nextInt(0,chances.length);
        }

        double distanceToOne = 1 - chances[index1];
        double distanceToZero = chances[index2];

        double maxR = Math.min(distanceToOne, distanceToZero);
        if(r > maxR) r = maxR;

        chances[index1] += r;
        chances[index2] -= r;

        for(int i = 0; i < chances.length; i++){
            chances[i] = clamp(chances[i],0,1);
        }

        double newRandomMovement = chances[0];
        double newToFoodMovement = chances[1];
        double[] newStepAmount = createStepVariation(this.stepAmount);
        FindFoodBehaviour newFindFoodBehaviour = this.findFoodBehaviour.createVariation();

        return new MovementBehaviour(gridWorld, human, newRandomMovement, newToFoodMovement, newStepAmount, newFindFoodBehaviour);
    }

    public double[] createStepVariation(double[] originalStepAmount){
        double[] copy = originalStepAmount.clone();

        int sizeR = random.nextInt(-1,2);
        int newSize = (int)clamp((float)(copy.length + sizeR), 1f,3f);
        if(newSize == copy.length){
            int size = copy.length;
            int first = random.nextInt(0,size);
            int second = random.nextInt(0,size);
            double r = Math.round(random.nextDouble(-0.2d,0.2d) * 100) / 100;
            copy[first] += r;
            copy[second] -= r;
        }
        else{
            copy = new double[newSize];
            double[] checkFilled = new double[newSize];
            float chanceLeft = 1;
            for(int i = 0; i < newSize; i++){
                int pos = random.nextInt(0,newSize);
                while(checkFilled[pos] == 1){
                    pos = random.nextInt(0,newSize);
                }
                double chance = 0;
                if(i == newSize-1 || chanceLeft == 0){
                    chance = chanceLeft;
                }
                else{
                    chance = Math.round(random.nextDouble(0,chanceLeft) * 100.0) / 100.0;
                }
                chanceLeft -= chance;
                checkFilled[pos] = 1;
                copy[pos] = chance;
            }
        }
        return copy;
    }

    @Override
    public String toString() {
        return "MovementBehaviour{" + "\n" +
                "stepAmount=" + Arrays.toString(stepAmount) + "\n" +
                "selectedFood=" + selectedFoodPosition + "\n" +
                "randomMovement=" + randomMovement + "\n" +
                "toFoodMovement=" + toFoodMovement + "\n" +
                "findFoodBehaviour=" + findFoodBehaviour + "\n" +
                '}';
    }
}
