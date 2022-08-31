package SimulationApplication;

import DataAnalytics.DataAnalytics;
import SimulationApplication.GridContent.Entity.Entity;
import SimulationApplication.GridContent.Entity.Human.Human;
import SimulationApplication.GridContent.Entity.Human.HumanParameters;
import SimulationApplication.GridContent.Food;
import SimulationApplication.GridContent.GridContent;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Random;

public class GridWorld {
    private HumanParameters humanParameters = new HumanParameters();

    private int width;
    private int height;

    private int idCounter = 0;
    private Hashtable<Integer, GridContent> ids = new Hashtable<>();
    private Hashtable<GridContent, Integer> gridContents = new Hashtable<>();

    private GridTile[][] gridWorld;

    private int day = 0;

    private DataAnalytics dataAnalytics;

    private ArrayList<Human> humans = new ArrayList<>();;
    private ArrayList<Food> food = new ArrayList<>();;

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

        this.dataAnalytics = new DataAnalytics(this);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public ArrayList<Integer> getEntityIds(GridPosition gridPosition){
        return getGridTile(gridPosition).getGridContentIds();
    }

    public Boolean isHuman(int gridContentId){
        return ids.get(gridContentId) instanceof Human;
    }

    public Boolean checkDisplayability(int gridContentId){
        if(!ids.containsKey(gridContentId)) return false;
        return ids.get(gridContentId).checkDisplayability();
    }

    public int getRange(int gridContentId) throws Exception {
        if(!(ids.get(gridContentId) instanceof Human)) throw new Exception("GridContent linked to id doesn't have a range");
        return ((Human)ids.get(gridContentId)).getViewRange();
    }

    public GridPosition getGridPosition(int gridContentId){
        return ids.get(gridContentId).getGridPosition();
    }

    public String getInfoString(int id){
        GridContent content = ids.get(id);
        return content.toString();
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
        return this.humans.size();
    }

    public int getDay(){
        return this.day;
    }

    public void addContent(GridContent content) {
        if(!isWithinBounds(content.getGridPosition())) return;

        int id = idCounter;
        ids.put(id, content);
        gridContents.put(content,id);
        idCounter++;

        GridTile gridTile = getGridTile(content.getGridPosition());
        gridTile.addContent(content, id);

        if(content instanceof Human h) humans.add(h);
        if(content instanceof Food f) food.add(f);
    }

    private void removeContent(GridContent content) throws Exception {
        if(!isWithinBounds(content.getGridPosition())) throw new Exception("Content is out of bounds");
        GridTile gridTile = getGridTile(content.getGridPosition());

        int id = gridContents.get(content);
        ids.remove(id);
        gridContents.remove(content);

        if(!gridTile.contains(id)) throw new Exception("Content doesn't exist in gridworld");

        gridTile.removeContent(id);

        if(content instanceof Human h) humans.remove(h);
        if(content instanceof Food f) food.remove(f);

        content.setActive(false);
    }

    public void advanceTime() throws Exception {
        // Add day
        day++;

        // Survived, died and bred entities
        ArrayList<Entity> diedEntities = new ArrayList<>();
        ArrayList<Entity> bredEntities = new ArrayList<>();

        // Entity action, check alive and check breeding
        for(Entity entity : this.humans){
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
        if(content.getGridPosition().equals(oldGridPosition)) return;

        GridTile oldGridTile = getGridTile(oldGridPosition);
        oldGridTile.removeContent(gridContents.get(content));

        GridTile newGridTile = getGridTile(content.getGridPosition());
        newGridTile.addContent(content, gridContents.get(content));
    }

    public Boolean isWithinBounds(GridPosition gridPosition){
        return gridPosition.getX() >= 0 && gridPosition.getY() >= 0 && gridPosition.getX() < width && gridPosition.getY() < height;
    }

    public Boolean containsFood(GridPosition gridPosition){
        if(!isWithinBounds(gridPosition)) return false;
        GridTile gridTile = getGridTile(gridPosition);
        return gridTile.containsFood();
    }

    public Food collectFood(GridPosition gridPosition) throws Exception {
        if(!containsFood(gridPosition)) return null;
        GridTile gridTile = getGridTile(gridPosition);
        Food food = gridTile.collectFood();
        this.removeContent(food);
        return food;
    }

    public GridTile getGridTile(GridPosition gridPosition){
        return this.gridWorld[height - 1 - gridPosition.getY()][gridPosition.getX()];
    }

    public void resetStatistics(){
        this.dataAnalytics.resetStatistics();
    }

    public ArrayList<Food> getAllFood(){
        return (ArrayList<Food>)this.food.clone();
    }

    public ArrayList<Human> getAllHumans(){
        return (ArrayList<Human>)this.humans.clone();
    }

    public void setEatInterval(int eatInterval) {
        this.humanParameters.setEatInterval(eatInterval);
    }

    public void setEatCost(int eatCost) {
        this.humanParameters.setEatCost(eatCost);
    }

    public void setBreedInterval(int breedInterval) {
        this.humanParameters.setBreedInterval(breedInterval);
    }

    public void setBreedCost(int breedCost) {
        this.humanParameters.setBreedCost(breedCost);
    }

    public Hashtable<String, Integer> getHumanParameterInfo(){
        return humanParameters.getHumanParameterInfo();
    }

    public void applyParameters(Hashtable<String, Integer> parameters){
        humanParameters.applyParameters(parameters);
    }

    public void spawnHumanRandom(int amount){
        for(int i = 0; i < amount; i++){
            GridPosition gridPosition = getRandomPosition();

            Human human = new Human(this, gridPosition, humanParameters);
        }
    }

    public void spawnHumanPosition(int amount, GridPosition gridPosition){
        for(int i = 0; i < amount; i++){
            Human human = new Human(this, gridPosition, humanParameters);
        }
    }

    public void spawnFoodRandom(int amount){
        for(int i = 0; i < amount; i++){
            GridPosition gridPosition = getRandomPosition();

            Food food = new Food(this, gridPosition);
        }
    }

    public void spawnFoodPosition(int amount, GridPosition gridPosition){
        for(int i = 0; i < amount; i++){
            Food food = new Food(this, gridPosition);
        }
    }

    public GridPosition getRandomPosition(){
        int randomX = random.nextInt(0, getWidth());
        int randomY = random.nextInt(0, getHeight());
        GridPosition gridPosition = new GridPosition(randomX,randomY);
        return gridPosition;
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
