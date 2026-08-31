package DataAnalytics;

import SimulationApplication.GridPosition;
import SimulationApplication.GridWorld;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;

public class DataAnalytics {
    private GridWorld gridWorld;
    private Hashtable<Integer, Hashtable<String, Integer>> data = new Hashtable<>();

    public DataAnalytics(GridWorld gridWorld) {
        this.gridWorld = gridWorld;
    }

    public Hashtable<String, Integer> getEntityInfo() {
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

    public void updateData(int day) {
        Hashtable<String, Integer> newData = getEntityInfo();
        this.data.put(day, newData);
    }

    public void resetStatistics() {
        this.data = new Hashtable<>();
    }

    public ArrayList<Integer> humanCountsPerDay() {
        ArrayList<Integer> days = Collections.list(this.data.keys());
        Collections.sort(days);

        ArrayList<Integer> amounts = new ArrayList<>();
        for (Integer day : days) {
            amounts.add(this.data.get(day).get("HUMAN"));
        }
        return amounts;
    }

    public void drawHumanGraph() {
        LineChart.Create(
                "Human Statistics", "Human population per day", "Human Serie", "Day", "Humans", humanCountsPerDay());
    }
}
