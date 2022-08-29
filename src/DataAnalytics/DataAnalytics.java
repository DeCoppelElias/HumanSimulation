package DataAnalytics;

import SimulationApplication.GridContent;
import SimulationApplication.GridPosition;
import SimulationApplication.GridWorld;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;

public class DataAnalytics {
    private GridWorld gridWorld;
    private Hashtable<Integer, Hashtable<String,Integer>> data = new Hashtable<>();

    public DataAnalytics(GridWorld gridWorld){
        this.gridWorld = gridWorld;
    }

    public Hashtable<String, Integer> getEntityInfo(){
        Hashtable<String, Integer> result = new Hashtable<>();
        Hashtable<GridPosition, String> info = gridWorld.getInfo();

        int humanCount = 0;
        int foodCount = 0;

        Enumeration<GridPosition> e = info.keys();
        while (e.hasMoreElements()) {
            GridPosition gridPosition = e.nextElement();
            String s = info.get(gridPosition);

            humanCount += ((s.length() - s.replace("HUMAN", "").length()) / 5);
            foodCount += ((s.length() - s.replace("FOOD", "").length()) / 4);
        }

        result.put("HUMAN", humanCount);
        result.put("FOOD", foodCount);

        return result;
    }

    public void updateData(int day){
        Hashtable<String, Integer> newData = getEntityInfo();
        this.data.put(day, newData);
    }

    public String getDataString(){
        String result = "-----------------------------------\n";

        int counter = 1;
        Enumeration<Integer> e1 = this.data.keys();
        while (e1.hasMoreElements()) {
            e1.nextElement();

            String dayString = "-----------------\n";
            dayString += "day: " + counter + "\n";
            Integer day = counter;
            counter++;
            Hashtable<String, Integer> dayData = this.data.get(day);

            Enumeration<String> e2 = dayData.keys();
            while (e2.hasMoreElements()) {

                String name = e2.nextElement();
                Integer amount = dayData.get(name);

                dayString += name + ": " + amount + "\n";
            }

            dayString += "-----------------\n";
            result += dayString;
        }
        result += "-----------------------------------";
        return result;
    }

    public void resetStatistics(){
        this.data = new Hashtable<>();
    }

    public void drawHumanGraph() {
        ArrayList<Integer> amounts = new ArrayList<>();

        int counter = 1;
        Enumeration<Integer> e1 = this.data.keys();
        while (e1.hasMoreElements()) {
            e1.nextElement();
            Integer day = counter;
            counter++;
            Hashtable<String, Integer> dayData = this.data.get(day);
            amounts.add(dayData.get("HUMAN"));
        }

        LineChart.Create("Human Statistics",
                "Human population per day",
                "Human Serie",
                "Day",
                "Humans",
                amounts);
    }
}
