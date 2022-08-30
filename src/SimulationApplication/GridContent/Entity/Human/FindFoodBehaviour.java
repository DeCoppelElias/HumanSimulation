package SimulationApplication.GridContent.Entity.Human;

import SimulationApplication.GridContent.Entity.Entity;
import SimulationApplication.GridContent.Food;
import SimulationApplication.GridPosition;
import SimulationApplication.GridWorld;

import java.util.ArrayList;
import java.util.Random;

public class FindFoodBehaviour {
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

    public Food findFood(){
        ArrayList<Food> allFood = gridWorld.getAllFood();

        double chance = random.nextDouble();
        if(chance < closestFood){
            return findClosestFood(allFood);
        }
        else if(chance < closestFood + randomFood){
            return findRandomFood(allFood);
        }
        else if(chance < closestFood + randomFood + farthestFromOtherHumansFood){
            ArrayList<Human> humans = gridWorld.getAllHumans();
            return findFarthestFromOtherHumansFood(allFood, humans);
        }
        else{
            return findMostFoodInVicinityFood(allFood);
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

    public Food findClosestFood(ArrayList<Food> allFood){
        Food closestFood = null;
        double shortestDistance = Integer.MAX_VALUE;

        for(Food food : allFood){
            double distance = GridPosition.distance(food.getGridPosition(), human.getGridPosition());
            if (distance < shortestDistance && distance <= human.getViewRange()){
                shortestDistance = distance;
                closestFood = food;
            }
        }
        return closestFood;
    }

    public Food findRandomFood(ArrayList<Food> allFood){
        ArrayList<Food> visibleFood = new ArrayList<>();

        for(Food food : allFood){
            double distance = GridPosition.distance(food.getGridPosition(), human.getGridPosition());
            if (distance <= human.getViewRange()){
                visibleFood.add(food);
            }
        }
        if(visibleFood.size() == 0) return null;
        int r = random.nextInt(0,visibleFood.size());
        return visibleFood.get(r);
    }

    public Food findFarthestFromOtherHumansFood(ArrayList<Food> allFood, ArrayList<Human> humans){
        Food farthestFromOtherPlayersFood = null;
        int smallestDistance = Integer.MAX_VALUE;

        for(Food food : allFood){
            double distance = GridPosition.distance(food.getGridPosition(), human.getGridPosition());
            if (distance <= human.getViewRange()){
                int distanceFromOtherPlayers = 0;
                for(Human human : humans){
                    distanceFromOtherPlayers += GridPosition.distance(food.getGridPosition(), human.getGridPosition());
                }
                if(distanceFromOtherPlayers < smallestDistance){
                    farthestFromOtherPlayersFood = food;
                    smallestDistance = distanceFromOtherPlayers;
                }
            }
        }
        return farthestFromOtherPlayersFood;
    }

    public Food findMostFoodInVicinityFood(ArrayList<Food> allFood){
        Food mostFoodInVicinityFood = null;
        int smallestDistance = Integer.MAX_VALUE;

        for(Food food : allFood){
            double distance = GridPosition.distance(food.getGridPosition(), human.getGridPosition());
            if (distance <= human.getViewRange()){
                int distanceToOtherFood = 0;
                for(Food otherFood : allFood){
                    distanceToOtherFood += GridPosition.distance(food.getGridPosition(), otherFood.getGridPosition());
                }
                if(distanceToOtherFood < smallestDistance){
                    mostFoodInVicinityFood = food;
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
