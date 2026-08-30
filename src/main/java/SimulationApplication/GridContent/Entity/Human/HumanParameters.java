package SimulationApplication.GridContent.Entity.Human;

import java.util.ArrayList;
import java.util.Hashtable;

public class HumanParameters {
    private int eatInterval = 15;
    private int eatCost = 1;

    private int breedInterval = 15;
    private int breedCost = 3;

    public HumanParameters(){

    }

    public HumanParameters(int eatInterval, int eatCost,
                           int breedInterval, int breedCost){
        this.eatInterval = eatInterval;
        this.eatCost = eatCost;
        this.breedInterval = breedInterval;
        this.breedCost = breedCost;
    }

    public int getEatInterval() {
        return eatInterval;
    }

    public int getEatCost() {
        return eatCost;
    }

    public int getBreedInterval() {
        return breedInterval;
    }

    public int getBreedCost() {
        return breedCost;
    }

    public void setEatInterval(int eatInterval) {
        this.eatInterval = eatInterval;
    }

    public void setEatCost(int eatCost) {
        this.eatCost = eatCost;
    }

    public void setBreedInterval(int breedInterval) {
        this.breedInterval = breedInterval;
    }

    public void setBreedCost(int breedCost) {
        this.breedCost = breedCost;
    }

    public Hashtable<String, Integer> getHumanParameterInfo(){
        Hashtable<String, Integer> humanParams = new Hashtable<>();

        humanParams.put("Human Eating Interval",eatInterval);
        humanParams.put("Human Eating Cost",eatCost);
        humanParams.put("Human Breeding Interval",breedInterval);
        humanParams.put("Human Breed Cost",breedCost);

        return humanParams;
    }

    public void applyParameters(Hashtable<String, Integer> parameters){
        String s = "Human Eating Interval";
        if(parameters.containsKey(s)){
            this.setEatInterval(parameters.get(s));
        }
        s = "Human Eating Cost";
        if(parameters.containsKey(s)){
            this.setEatCost(parameters.get(s));
        }
        s = "Human Breeding Interval";
        if(parameters.containsKey(s)){
            this.setBreedInterval(parameters.get(s));
        }
        s = "Human Breed Cost";
        if(parameters.containsKey(s)){
            this.setBreedCost(parameters.get(s));
        }
    }
}
