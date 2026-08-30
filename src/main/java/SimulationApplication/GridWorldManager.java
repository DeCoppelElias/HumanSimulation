package SimulationApplication;

import DataAnalytics.DataAnalytics;

import java.util.*;

public class GridWorldManager {
    private GridWorld gridWorld;

    private int spawnFoodInterval = 7;
    private int spawnFoodAmount = 7;

    public GridWorldManager(GridWorld gridWorld){
        this.gridWorld = gridWorld;
    }

    public int getWidth(){
        return this.gridWorld.getWidth();
    }

    public int getHeight(){
        return this.gridWorld.getHeight();
    }

    public void spawnHuman(int amount){
        gridWorld.spawnHumanRandom(amount);
    }

    public void spawnHuman(GridPosition gridPosition){
        gridWorld.spawnHumanPosition(1, gridPosition);
    }

    public void spawnFood(int amount){
        gridWorld.spawnFoodRandom(amount);
    }

    public void spawnFood(GridPosition gridPosition){
        gridWorld.spawnFoodPosition(1, gridPosition);
    }

    public String getInfoString(int id){
        return this.gridWorld.getInfoString(id);
    }

    public Hashtable<GridPosition, String> getInfo(){
        try {
            return this.gridWorld.getInfo();
        } catch (Exception e) {
            e.printStackTrace();
            return new Hashtable<>();
        }
    }

    public void advanceTime(){
        try{
            this.gridWorld.advanceTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Integer> getEntityIds(GridPosition gridPosition){
        return this.gridWorld.getEntityIds(gridPosition);
    }

    public Boolean checkDisplayability(int id){
        return this.gridWorld.checkDisplayability(id);
    }

    public Boolean isHuman(int id) {
        return this.gridWorld.isHuman(id);
    }

    public int getRange(int id){
        try {
            return this.gridWorld.getRange(id);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public GridPosition getGridPosition(int id){
        return gridWorld.getGridPosition(id);
    }

    public Boolean isWithinBounds(GridPosition gridPosition){
        return gridWorld.isWithinBounds(gridPosition);
    }

    public void automaticAdvance() {
        int day = gridWorld.getDay();
        if(spawnFoodInterval > 0 && day % spawnFoodInterval == 0){
            spawnFood(spawnFoodAmount);
        }
    }

    public void resetStatistics(){
        gridWorld.resetStatistics();
    }

    public void displayHumanGraph(){
        DataAnalytics dataAnalytics = this.gridWorld.getDataAnalytics();
        dataAnalytics.drawHumanGraph();
    }

    public void resetGridWorld() {
        this.gridWorld = new GridWorld(this.getWidth(), this.getHeight());
    }

    public Hashtable<String, Integer> getHumanParameterInfo(){
        Hashtable<String, Integer> params = new Hashtable<>();

        params.put("Spawn Food Interval", this.spawnFoodInterval);
        params.put("Spawn Food Amount", this.spawnFoodAmount);

        Hashtable<String, Integer> humanParams = gridWorld.getHumanParameterInfo();

        params.putAll(humanParams);

        return params;
    }

    public void applyParameters(Hashtable<String, Integer> parameters){
        if(parameters.containsKey("Spawn Food Interval")){
            this.spawnFoodInterval = parameters.get("Spawn Food Interval");
        }
        if(parameters.containsKey("Spawn Food Amount")){
            this.spawnFoodAmount = parameters.get("Spawn Food Amount");
        }

        gridWorld.applyParameters(parameters);
    }

    public ArrayList<Integer> getAllHumans(){
        return gridWorld.getAllHumanIds();
    }

    public ArrayList<Integer> sortOnSurvival(ArrayList<Integer> ids){
        Comparator<Integer> comparator = (o1, o2) -> {
            Hashtable<String, Float> info1 = gridWorld.getInfo(o1);
            Hashtable<String, Float> info2 = gridWorld.getInfo(o2);

            if(info1.containsKey("Days") && info2.containsKey("Days")){
                return Float.compare(info1.get("Days"), info2.get("Days"));
            }
            else if(info1.containsKey("Days")){
                return 1;
            }
            else if(info2.containsKey("Days")){
                return -1;
            }
            else return 0;
        };

        ids.sort(comparator);
        Collections.reverse(ids);

        return ids;
    }

    public ArrayList<Integer> sortOnFood(ArrayList<Integer> ids){
        Comparator<Integer> comparator = (o1, o2) -> {
            Hashtable<String, Float> info1 = gridWorld.getInfo(o1);
            Hashtable<String, Float> info2 = gridWorld.getInfo(o2);

            if(info1.containsKey("Food") && info2.containsKey("Food")){
                return Float.compare(info1.get("Food"), info2.get("Food"));
            }
            else if(info1.containsKey("Food")){
                return 1;
            }
            else if(info2.containsKey("Food")){
                return -1;
            }
            else return 0;
        };

        ids.sort(comparator);
        Collections.reverse(ids);

        return ids;
    }
}
