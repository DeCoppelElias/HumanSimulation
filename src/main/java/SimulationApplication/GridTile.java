package SimulationApplication;

import SimulationApplication.GridContent.Entity.Human.Human;
import SimulationApplication.GridContent.Food;
import SimulationApplication.GridContent.GridContent;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;

public class GridTile {
    private GridPosition gridPosition;
    private Dictionary<Integer, GridContent> contents;

    public GridTile(int x, int y){
        this.gridPosition = new GridPosition(x,y);
        this.contents = new Hashtable<>();
    }

    public ArrayList<Integer> getGridContentIds(){
        ArrayList<Integer> result = new ArrayList<>();

        Enumeration<Integer> enumeration = contents.keys();
        while(enumeration.hasMoreElements()){
            int id = enumeration.nextElement();
            result.add(id);
        }

        return result;
    }

    public GridPosition getGridPosition(){
        return (GridPosition)this.gridPosition.clone();
    }

    public void addContent(GridContent content, int id){
        this.contents.put(id, content);
    }

    public void removeContent(int id){
        contents.remove(id);
    }

    public Boolean contains(int id){
        GridContent gridContent = this.contents.get(id);
        if(gridContent == null){
            System.out.println("lol");
        }
        return gridContent != null;
    }

    public Boolean containsFood(){
        Enumeration<Integer> enumeration = contents.keys();
        while(enumeration.hasMoreElements()){
            int id = enumeration.nextElement();
            GridContent content = contents.get(id);
            if(content instanceof Food){
                return true;
            }
        }
        return false;
    }

    public void collectFood(Human human){
        Food collectedFood = null;
        Enumeration<Integer> enumeration = contents.keys();
        while(enumeration.hasMoreElements()){
            int id = enumeration.nextElement();
            GridContent content = contents.get(id);
            if(content instanceof Food food){
                if (collectedFood == null){
                    collectedFood = food;
                }
                else{
                    if(collectedFood.getTryingToCollectSize() > food.getTryingToCollectSize()){
                        collectedFood = food;
                    }
                }
            }
        }

        collectedFood.collectFood(human);
    }

    public ArrayList<String> getInfo(){
        ArrayList<String> result = new ArrayList<>();

        Enumeration<Integer> enumeration = contents.keys();
        while(enumeration.hasMoreElements()){
            int id = enumeration.nextElement();
            GridContent content = contents.get(id);

            result.add(content.toString());
        }

        if(result.size() == 0) result.add("Empty");
        return result;
    }

    @Override
    public String toString() {
        if(contents.size() == 0) return "Empty";
        String contentString = "";
        Enumeration<Integer> enumeration = contents.keys();
        while(enumeration.hasMoreElements()){
            int id = enumeration.nextElement();
            GridContent content = contents.get(id);

            contentString += content.gridString() + "|";
        }
        return contentString.substring(0, contentString.length()-1);
    }
}
