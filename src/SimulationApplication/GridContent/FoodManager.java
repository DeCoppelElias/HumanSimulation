package SimulationApplication.GridContent;

import SimulationApplication.GridContent.Entity.Human.Human;
import SimulationApplication.GridPosition;
import SimulationApplication.GridWorld;

import java.util.ArrayList;
import java.util.Hashtable;

public class FoodManager implements Manager {
    private GridWorld gridWorld;

    private ArrayList<Food> foods = new ArrayList<>();

    public FoodManager(GridWorld gridWorld){
        this.gridWorld = gridWorld;
    }

    public void addFood(Food food){
        this.foods.add(food);
    }

    public void removeFood(Food food){
        this.foods.remove(food);
    }

    public ArrayList<GridPosition> getAllFoodPositions(){
        ArrayList<GridPosition> result = new ArrayList<>();

        for(Food food : foods){
            result.add(food.getGridPosition());
        }

        return result;
    }

    public void advanceTime() throws Exception {
        ArrayList<Food> eaten = new ArrayList<>();

        for(Food food : foods){
            if(food.getTryingToCollectSize() > 0){
                Hashtable<String, ArrayList<Human>> info = food.decideWinner();

                ArrayList<Human> wonFood = info.get("Won Food");
                ArrayList<Human> died = info.get("Died");

                for(Human human : wonFood){
                    human.addFood(1f / wonFood.size());
                }

                for(Human human : died){
                    this.gridWorld.removeContent(human);
                    human.setAlive(false);
                }

                if(wonFood.size() > 0){
                    eaten.add(food);
                }
            }
        }

        for(Food food : eaten){
            this.gridWorld.removeContent(food);
        }
    }
}
