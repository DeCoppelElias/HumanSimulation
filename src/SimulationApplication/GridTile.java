package SimulationApplication;

import SimulationApplication.GridContent.Food;
import SimulationApplication.GridContent.GridContent;

import java.util.ArrayList;

public class GridTile {
    private GridPosition gridPosition;
    private ArrayList<GridContent> contents;

    public GridTile(int x, int y, ArrayList<GridContent> contents){
        this.gridPosition = new GridPosition(x,y);
        this.contents = contents;
    }
    public GridTile(int x, int y){
        this.gridPosition = new GridPosition(x,y);
        this.contents = new ArrayList<>();
    }

    public ArrayList<Integer> getGridContentIds(){
        ArrayList<Integer> result = new ArrayList<>();
        for(GridContent content : contents){
            result.add(content.getId());
        }
        return result;
    }

    public GridPosition getGridPosition(){
        return (GridPosition)this.gridPosition.clone();
    }

    public ArrayList<GridContent> getContents() {
        return (ArrayList<GridContent>)contents.clone();
    }

    public void addContent(GridContent content){
        this.contents.add(content);
    }

    public void removeContent(GridContent content){
        this.contents.remove(content);
    }

    public Boolean contains(GridContent content){
        return this.contents.contains(content);
    }

    public Boolean containsFood(){
        for(GridContent content : contents){
            if(content instanceof Food){
                return true;
            }
        }
        return false;
    }

    public Food collectFood(){
        Food food = null;
        for(GridContent content : contents){
            if(content instanceof Food){
                food = (Food) content;
            }
        }
        if(food == null) return null;

        contents.remove(food);
        return food;
    }

    public ArrayList<String> getInfo(){
        ArrayList<String> result = new ArrayList<>();
        if(contents.size() == 0) result.add("Empty");
        else{
            for(GridContent content : this.contents){
                result.add(content.toString());
            }
        }
        return result;
    }

    @Override
    public String toString() {
        if(contents.size() == 0) return "Empty";
        String contentString = "";
        for(GridContent content : this.contents){
            contentString += content.gridString() + "|";
        }
        return contentString.substring(0, contentString.length()-1);
    }
}
