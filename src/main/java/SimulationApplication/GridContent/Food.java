package SimulationApplication.GridContent;

import SimulationApplication.GridContent.Entity.Human.Human;
import SimulationApplication.GridContent.Entity.Human.HumanBehaviour.FoodBehaviour;
import SimulationApplication.GridPosition;
import SimulationApplication.GridWorld;
import java.util.ArrayList;
import java.util.Hashtable;

public class Food extends GridContent {
    private ArrayList<Human> tryingToCollect = new ArrayList<>();

    public Food(GridWorld gridWorld, GridPosition gridPosition) {
        super(gridWorld, gridPosition);
    }

    public void collectFood(Human human) {
        tryingToCollect.add(human);
    }

    public Hashtable<String, ArrayList<Human>> decideWinner() {
        // Init result
        Hashtable<String, ArrayList<Human>> result = new Hashtable<>();
        ArrayList<Human> wonFood = new ArrayList<>();
        ArrayList<Human> died = new ArrayList<>();

        // Init behaviour types
        ArrayList<Human> peacefulHumans = new ArrayList<>();
        ArrayList<Human> aggressiveHumans = new ArrayList<>();

        // Check which behaviours will be aggressive and which will not
        for (Human human : tryingToCollect) {
            FoodBehaviour foodBehaviour = human.getFoodBehaviour();
            if (foodBehaviour.getBehaviour().equals("aggressive")) aggressiveHumans.add(human);
            else peacefulHumans.add(human);
        }

        // If only one aggressive behaviour => wins the food
        if (aggressiveHumans.size() == 1) {
            wonFood.add(aggressiveHumans.get(0));
        }

        // Aggressive behaviours > 1 => fight
        else if (aggressiveHumans.size() > 1) {
            int r = gridWorld.getRandom().nextInt(0, aggressiveHumans.size() + 1);
            for (int i = 0; i < aggressiveHumans.size(); i++) {
                Human human = aggressiveHumans.get(i);
                if (i == r) {
                    wonFood.add(human);
                } else {
                    died.add(human);
                }
            }
        }

        // If all aggressive behaviours died or there are no => split food
        if (wonFood.size() == 0) {
            wonFood.addAll(peacefulHumans);
        }

        result.put("Won Food", wonFood);
        result.put("Died", died);

        return result;
    }

    public void resetTryingToCollect() {
        this.tryingToCollect = new ArrayList<>();
    }

    public int getTryingToCollectSize() {
        return tryingToCollect.size();
    }

    @Override
    public Boolean checkDisplayability() {
        return true;
    }

    @Override
    public Hashtable<String, Float> getInfo() {
        return new Hashtable<>();
    }

    @Override
    public String toString() {
        return "Food: " + this.getActive();
    }

    @Override
    public String gridString() {
        return "FOOD";
    }
}
