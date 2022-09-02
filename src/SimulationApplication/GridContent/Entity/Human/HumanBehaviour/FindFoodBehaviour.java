package SimulationApplication.GridContent.Entity.Human.HumanBehaviour;

import SimulationApplication.GridContent.Entity.Human.Human;
import SimulationApplication.GridPosition;
import SimulationApplication.GridWorld;

import java.util.ArrayList;
import java.util.Random;

public class FindFoodBehaviour extends Behaviour {
    private Human human;

    private GridWorld gridWorld;

    private Random random = new Random();

    private double closestFood;
    private double randomFood;
    private double farthestFromOtherHumansFood;
    private double mostFoodInVicinityFood;

    public FindFoodBehaviour(GridWorld gridWorld, Human human){
        this.gridWorld = gridWorld;
        this.human = human;

        generateRandomChances();
    }
    public FindFoodBehaviour(GridWorld gridWorld, Human human,
                             double closestFood, double randomFood, double farthestFromOtherHumansFood, double mostFoodInVicinityFood) throws Exception {
        this.gridWorld = gridWorld;
        this.human = human;

        double sum = closestFood + randomFood + farthestFromOtherHumansFood + mostFoodInVicinityFood;
        if(Math.abs(sum - 1) > 0.01)
            throw new Exception("Chances add up to more than one: " + sum);

        this.closestFood = closestFood;
        this.randomFood = randomFood;
        this.farthestFromOtherHumansFood = farthestFromOtherHumansFood;
        this.mostFoodInVicinityFood = mostFoodInVicinityFood;
    }

    public void generateRandomChances(){
        double[] chances = generateRandomArray(4);

        this.closestFood = chances[0];
        this.randomFood = chances[1];
        this.farthestFromOtherHumansFood = chances[2];
        this.mostFoodInVicinityFood = chances[3];
    }

    public void setHuman(Human human){
        this.human = human;
    }

    public GridPosition findFood(){
        ArrayList<GridPosition> allFoodPositions = gridWorld.getAllFoodPositions();

        double chance = random.nextDouble();
        if(chance < closestFood){
            return findClosestFood(allFoodPositions);
        }
        else if(chance < closestFood + randomFood){
            return findRandomFood(allFoodPositions);
        }
        else if(chance < closestFood + randomFood + farthestFromOtherHumansFood){
            ArrayList<GridPosition> humanPositions = gridWorld.getAllHumanPositions();
            return findFarthestFromOtherHumansFood(allFoodPositions, humanPositions);
        }
        else{
            return findMostFoodInVicinityFood(allFoodPositions);
        }
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

    public GridPosition findClosestFood(ArrayList<GridPosition> allFood){
        GridPosition closestFood = null;
        double shortestDistance = Integer.MAX_VALUE;

        for(GridPosition foodPosition : allFood){
            double distance = GridPosition.distance(foodPosition, human.getGridPosition());
            if (distance < shortestDistance && distance <= human.getViewRange()){
                shortestDistance = distance;
                closestFood = foodPosition;
            }
        }
        return closestFood;
    }

    public GridPosition findRandomFood(ArrayList<GridPosition> allFood){
        ArrayList<GridPosition> visibleFood = new ArrayList<>();

        for(GridPosition foodPosition : allFood){
            double distance = GridPosition.distance(foodPosition, human.getGridPosition());
            if (distance <= human.getViewRange()){
                visibleFood.add(foodPosition);
            }
        }
        if(visibleFood.size() == 0) return null;
        int r = random.nextInt(0,visibleFood.size());
        return visibleFood.get(r);
    }

    public GridPosition findFarthestFromOtherHumansFood(ArrayList<GridPosition> allFood, ArrayList<GridPosition> humanPositions){
        GridPosition farthestFromOtherPlayersFood = null;
        int smallestDistance = Integer.MAX_VALUE;

        for(GridPosition foodPosition : allFood){
            double distance = GridPosition.distance(foodPosition, human.getGridPosition());
            if (distance <= human.getViewRange()){
                int distanceFromOtherPlayers = 0;
                for(GridPosition humanPosition : humanPositions){
                    distanceFromOtherPlayers += GridPosition.distance(foodPosition, humanPosition);
                }
                if(distanceFromOtherPlayers < smallestDistance){
                    farthestFromOtherPlayersFood = foodPosition;
                    smallestDistance = distanceFromOtherPlayers;
                }
            }
        }
        return farthestFromOtherPlayersFood;
    }

    public GridPosition findMostFoodInVicinityFood(ArrayList<GridPosition> allFood){
        GridPosition mostFoodInVicinityFood = null;
        int smallestDistance = Integer.MAX_VALUE;

        for(GridPosition foodPosition : allFood){
            double distance = GridPosition.distance(foodPosition, human.getGridPosition());
            if (distance <= human.getViewRange()){
                int distanceToOtherFood = 0;
                for(GridPosition otherFoodPosition : allFood){
                    distanceToOtherFood += GridPosition.distance(foodPosition, otherFoodPosition);
                }
                if(distanceToOtherFood < smallestDistance){
                    mostFoodInVicinityFood = foodPosition;
                    smallestDistance = distanceToOtherFood;
                }
            }
        }
        return mostFoodInVicinityFood;
    }

    public FindFoodBehaviour createVariation() throws Exception {
        double r = (double)Math.round(random.nextDouble(0,0.2d) * 100) / 100;
        double[] chances = new double[]{this.closestFood, this.randomFood, this.farthestFromOtherHumansFood, this.mostFoodInVicinityFood};
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

        double newClosestFood = chances[0];
        double newRandomFood = chances[1];
        double newFarthestFromOtherHumansFood = chances[2];
        double newMostFoodInVicinityFood = chances[3];

        return new FindFoodBehaviour(this.gridWorld, null, newClosestFood, newRandomFood, newFarthestFromOtherHumansFood, newMostFoodInVicinityFood);
    }

    public double clamp(double value, double lowerbound, double upperbound){
        if(value < lowerbound) return lowerbound;
        if(value > upperbound) return upperbound;
        return value;
    }

    @Override
    public String toString() {
        return "FindFoodBehaviour{" + "\n" +
                "closestFood=" + closestFood + "\n" +
                "randomFood=" + randomFood + "\n" +
                "farthestFromOtherHumansFood=" + farthestFromOtherHumansFood + "\n" +
                "mostFoodInVicinityFood=" + mostFoodInVicinityFood + "\n" +
                '}';
    }
}
