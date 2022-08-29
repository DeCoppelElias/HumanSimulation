package SimulationApplication;

import DataAnalytics.DataAnalytics;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Random;

public class GridWorld {
    private int width;
    private int height;
    private GridTile[][] gridWorld;

    private int day = 0;

    private DataAnalytics dataAnalytics;

    private ArrayList<Entity> entities;
    private ArrayList<Food> food;

    private Random random  = new Random();

    public GridWorld(int width, int height){
        this.width = width;
        this.height = height;

        gridWorld = new GridTile[height][width];
        for(int x = 0; x < width; x++){
            for(int y = 0; y < height; y++){
                gridWorld[height - 1 - y][x] = new GridTile(x,y);
            }
        }

        entities = new ArrayList<>();
        food = new ArrayList<>();

        this.dataAnalytics = new DataAnalytics(this);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public DataAnalytics getDataAnalytics(){
        return this.dataAnalytics;
    }

    public Hashtable<GridPosition, String> getInfo(){
        Hashtable<GridPosition, String> result = new Hashtable<GridPosition, String>();

        for(int x = 0; x < width; x++){
            for(int y = 0; y < height; y++){
                GridPosition gridPosition = new GridPosition(x,y);
                String s = getGridTile(gridPosition).toString();

                result.put(gridPosition, s);
            }
        }

        return result;
    }

    public ArrayList<String> getInfo(GridPosition gridPosition){
        return getGridTile(gridPosition).getInfo();
    }

    public int getFoodAmount(){
        return this.food.size();
    }

    public int getEntityAmount(){
        return this.entities.size();
    }

    public int getDay(){
        return this.day;
    }

    public void addContent(GridContent content) throws Exception {
        if(!isWithinBounds(content.getGridPosition())) throw new Exception("Content is out of bounds");

        GridTile gridTile = getGridTile(content.getGridPosition());
        gridTile.addContent(content);

        if(content instanceof Entity e) entities.add(e);
        if(content instanceof Food f) food.add(f);
    }

    public void removeContent(GridContent content) throws Exception {
        if(!isWithinBounds(content.getGridPosition())) throw new Exception("Content is out of bounds");
        GridTile gridTile = getGridTile(content.getGridPosition());

        if(!gridTile.contains(content)) throw new Exception("Content doesn't exist in gridworld");

        gridTile.removeContent(content);

        if(content instanceof Entity e) entities.remove(e);
        if(content instanceof Food f) food.remove(f);
    }

    public void advanceTime() throws Exception {
        // Add day
        day++;

        // Survived, died and bred entities
        ArrayList<Entity> diedEntities = new ArrayList<>();
        ArrayList<Entity> bredEntities = new ArrayList<>();

        // Entity action, check alive and check breeding
        for(Entity entity : this.entities){
            entity.action();
            if (!entity.checkAlive()){
                diedEntities.add(entity);
            }
            if (entity.checkBred()){
                bredEntities.add(entity);
            }
        }

        // Remove died entities from grid
        for(Entity entity : diedEntities){
            removeContent(entity);
        }

        // Create babies
        for(Entity entity : bredEntities){
            entity.createChild();
        }

        // Update data
        dataAnalytics.updateData(day);
    }

    public void refreshContent(GridContent content, GridPosition oldGridPosition){
        GridTile newGridTile = getGridTile(content.getGridPosition());
        newGridTile.addContent(content);

        GridTile oldGridTile = getGridTile(oldGridPosition);
        oldGridTile.removeContent(content);
    }

    public GridPosition getClosestFoodPosition(Entity entity){
        if(this.food.size() == 0) return entity.getGridPosition();

        GridPosition closestGridPosition = new GridPosition(0,0);
        double shortestDistance = Integer.MAX_VALUE;
        for(Food food : this.food){
            double distance = GridPosition.distance(food.getGridPosition(), entity.getGridPosition());
            if (distance < shortestDistance){
                shortestDistance = distance;
                closestGridPosition = food.getGridPosition();
            }
        }
        return closestGridPosition;
    }

    public Boolean isWithinBounds(GridPosition gridPosition){
        return gridPosition.getX() >= 0 && gridPosition.getY() >= 0 && gridPosition.getX() < width && gridPosition.getY() < height;
    }

    public Boolean containsFood(GridPosition gridPosition){
        if(!isWithinBounds(gridPosition)) return false;
        GridTile gridTile = getGridTile(gridPosition);
        return gridTile.containsFood();
    }

    public Food collectFood(GridPosition gridPosition){
        if(!containsFood(gridPosition)) return null;
        GridTile gridTile = getGridTile(gridPosition);
        Food food = gridTile.collectFood();
        this.food.remove(food);
        return food;
    }

    public GridTile getGridTile(GridPosition gridPosition){
        return this.gridWorld[height - 1 - gridPosition.getY()][gridPosition.getX()];
    }

    public void resetStatistics(){
        this.dataAnalytics.resetStatistics();
    }

    @Override
    public String toString() {
        String result = "";
        for(int y = 0; y < height; y++){
            String row = "";
            for(int x = 0; x < width; x++){
                row += this.gridWorld[y][x].toString() + " ";
            }
            row += "\n";
            result += row;
        }
        return result;
    }
}
