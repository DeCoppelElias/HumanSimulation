package SimulationApplication.Human;

import SimulationApplication.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class Behaviour {
    private GridWorld gridWorld;

    protected Entity entity;

    private float randomMovement;
    private float toClosestFoodMovement;

    private float[] stepAmount;

    private Random random = new Random();

    public Behaviour(GridWorld gridWorld, Entity entity){
        this.randomMovement = this.random.nextFloat();
        this.toClosestFoodMovement = 1 - randomMovement;

        int maxStepAmount = this.random.nextInt(1,4);
        this.stepAmount = new float[maxStepAmount];
        float[] checkFilled = new float[maxStepAmount];
        float chanceLeft = 1;
        for(int i = 0; i < maxStepAmount; i++){
            int pos = random.nextInt(0,maxStepAmount);
            while(checkFilled[pos] == 1){
                pos = random.nextInt(0,maxStepAmount);
            }
            float chance = 0;
            if(i == maxStepAmount-1){
                chance = chanceLeft;
            }
            else{
                chance = random.nextFloat(0,chanceLeft);
            }
            chanceLeft -= chance;
            checkFilled[pos] = 1;
            stepAmount[pos] = chance;
        }

        this.gridWorld = gridWorld;
        this.entity = entity;
    }
    public Behaviour(GridWorld gridWorld, Entity entity, float randomMovement, float[] stepAmount) throws Exception {
        if(randomMovement > 1 || randomMovement < 0) throw new Exception("randomMovement variables must be between 0 and 1");
        this.randomMovement = randomMovement;
        this.toClosestFoodMovement = 1 - randomMovement;

        this.stepAmount = stepAmount;

        this.gridWorld = gridWorld;
        this.entity = entity;
    }

    public Action getNextAction(){
        float r = random.nextFloat();
        if (r < randomMovement){
            return moveRandomAction();
        }
        else{
            return moveToClosestFoodAction();
        }
    }

    private Action moveRandomAction(){
        int r = random.nextInt(4);

        int distance = 1;
        float distanceR = random.nextFloat();
        while(distanceR > stepAmount[distance - 1]){
            distanceR -= stepAmount[distance - 1];
            distance += 1;
        }

        switch (r){
            case 0:
                return new Action(distance,0);
            case 1:
                return new Action(-distance,0);
            case 2:
                return new Action(0,-distance);
            case 3:
                return new Action(0,distance);
            default:
                return new Action(0,0);
        }
    }

    private Action moveToClosestFoodAction(){
        GridPosition targetGridPosition = gridWorld.getClosestFoodPosition(entity);
        GridPosition entityGridPosition = this.entity.getGridPosition();

        int distance = 1;
        float distanceR = random.nextFloat();
        while(distanceR > stepAmount[distance - 1]){
            distanceR -= stepAmount[distance - 1];
            distance += 1;
        }

        if(targetGridPosition.getX() > entityGridPosition.getX()){
            return new Action(distance,0);
        }
        else if(targetGridPosition.getX() < entityGridPosition.getX()){
            return new Action(-distance,0);
        }
        else if(targetGridPosition.getY() > entityGridPosition.getY()){
            return new Action(0,distance);
        }
        else if(targetGridPosition.getY() < entityGridPosition.getY()){
            return new Action(0,-distance);
        }
        else return new Action(0,0);
    }

    public Behaviour createVariation() throws Exception {
        float r = random.nextFloat(-0.2f,0.2f);
        float newRandomMovement = clamp(this.randomMovement + r,0,1);

        float[] newStepAmount = createStepVariation(this.stepAmount);

        return new Behaviour(this.gridWorld, null, newRandomMovement, newStepAmount);
    }

    public float[] createStepVariation(float[] originalStepAmount){
        float[] copy = originalStepAmount.clone();

        int sizeR = random.nextInt(-1,2);
        int newSize = (int)clamp((float)(copy.length + sizeR), 1f,3f);
        if(newSize == copy.length){
            int size = copy.length;
            int first = random.nextInt(0,size);
            int second = random.nextInt(0,size);
            float r = random.nextFloat(-0.2f,0.2f);
            copy[first] += r;
            copy[second] -= r;
        }
        else{
            copy = new float[newSize];
            float[] checkFilled = new float[newSize];
            float chanceLeft = 1;
            for(int i = 0; i < newSize; i++){
                int pos = random.nextInt(0,newSize);
                while(checkFilled[pos] == 1){
                    pos = random.nextInt(0,newSize);
                }
                float chance = 0;
                if(i == newSize-1){
                    chance = chanceLeft;
                }
                else{
                    chance = random.nextFloat(0,chanceLeft);
                }
                chanceLeft -= chance;
                checkFilled[pos] = 1;
                copy[pos] = chance;
            }
        }
        return copy;
    }

    public float clamp(float value, float lowerbound, float upperbound){
        if(value < lowerbound) return lowerbound;
        if(value > upperbound) return upperbound;
        return value;
    }

    @Override
    public String toString() {
        return "Behaviour{" + "\n" +
                "randomMovement=" + randomMovement + "\n" +
                "toClosestFoodMovement=" + toClosestFoodMovement + "\n" +
                "stepAmount=" + Arrays.toString(stepAmount) + "\n" +
                '}';
    }
}
