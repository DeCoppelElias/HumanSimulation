package SimulationApplication;

import DataAnalytics.DataAnalytics;

import java.util.ArrayList;
import java.util.Hashtable;

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

    public void setSpawnFoodInterval(int spawnFoodInterval) {
        this.spawnFoodInterval = spawnFoodInterval;
    }

    public void setSpawnFoodAmount(int spawnFoodAmount) {
        this.spawnFoodAmount = spawnFoodAmount;
    }

    public void setEatInterval(int eatInterval) {
        this.gridWorld.setEatInterval(eatInterval);
    }

    public void setEatCost(int eatCost) {
        this.gridWorld.setEatCost(eatCost);
    }

    public void setBreedInterval(int breedInterval) {
        this.gridWorld.setBreedInterval(breedInterval);
    }

    public void setBreedCost(int breedCost) {
        this.gridWorld.setBreedCost(breedCost);
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
}
