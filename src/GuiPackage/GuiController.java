package GuiPackage;

import DataAnalytics.DataAnalytics;
import SimulationApplication.*;
import SimulationApplication.Human.Human;

import java.awt.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class GuiController {
    private Gui gui;
    private GridWorld gridWorld;
    private GridContent selectedGridContent;
    private Boolean automatic = false;

    private ArrayList<GuiShutdownListener> shutdownListeners = new ArrayList<>();

    ScheduledFuture<?> t = null;
    ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    Runnable automaticAdvance = new Runnable() {
        public void run() {
            try {
                automaticAdvance();
            } catch (Exception e) {
                e.printStackTrace();
                automatic = false;
                t.cancel(false);
            }
        }
    };
    long rate = 500;

    public GuiController(GridWorld gridWorld) throws Exception {
        this.gui = new Gui(this);
        this.gridWorld = gridWorld;
    }

    public void startGui() throws Exception {
        this.gui.initialize(gridWorld.getWidth(), gridWorld.getHeight());
    }

    public void spawnHuman() throws Exception {
        Human.spawnRandomClones(gridWorld,1);
        refreshGrid();
    }

    public void spawnFood() throws Exception {
        Food.spawnRandomClones(gridWorld,1);
        refreshGrid();
    }

    public void advanceTime() throws Exception {
        gridWorld.advanceTime();
        refreshGrid();
        if(this.selectedGridContent == null) return;
        displayInfo(selectedGridContent.getGridPosition());
    }

    public void refreshGrid() throws Exception {
        Hashtable<GridPosition, String> info = gridWorld.getInfo();

        Enumeration<GridPosition> e = info.keys();
        while (e.hasMoreElements()) {
            GridPosition gridPosition = e.nextElement();
            String s = info.get(gridPosition);

            // Color
            if(s.contains("HUMAN")){
                gui.setTileImage(gridPosition, "Human");
            }

            else if(s.contains("FOOD")){
                gui.setTileImage(gridPosition, "Food");
            }

            else{
                gui.setTileImage(gridPosition, null);
            }
        }
    }

    public void displayInfo(GridPosition gridPosition){
        this.gui.resetInfo();

        ArrayList<String> info = this.gridWorld.getInfo(gridPosition);

        Boolean found = false;
        ArrayList<GridContent> gridContents = this.gridWorld.getGridTile(gridPosition).getContents();
        for (GridContent gridContent : gridContents){
            if(gridContent instanceof Human human){
                this.selectedGridContent = human;
                found = true;
                break;
            }
        }
        if(!found) this.selectedGridContent = null;

        this.gui.displayInfo(info);
    }

    public void toggleAutomatic(){
        if(automatic) this.automatic = false;
        else this.automatic = true;

        if(automatic){
            t = executor.scheduleAtFixedRate(automaticAdvance, 0, rate, TimeUnit.MILLISECONDS);
        }
        else{
            if(t == null) return;
            t.cancel(false);
        }
    }

    public void automaticAdvance() throws Exception {
        advanceTime();
        int day = gridWorld.getDay();
        int interval = 7;
        if(interval > 0 && day % interval == 0){
            Food.spawnRandomClones(this.gridWorld, 7);
        }
    }

    public void increaseAutomaticSpeed(){
        if(!automatic) return;
        if(rate > 100){
            rate -= 100;
        }
        else {
            rate -= 10;
            if(rate < 10) rate = 10;
        }
        if (t != null)
        {
            t.cancel(true);
        }
        t = executor.scheduleAtFixedRate(automaticAdvance, 0, rate, TimeUnit.MILLISECONDS);
    }

    public void decreaseAutomaticSpeed(){
        if(!automatic) return;
        rate += 100;
        if (t != null)
        {
            t.cancel(true);
        }
        t = executor.scheduleAtFixedRate(automaticAdvance, 0, rate, TimeUnit.MILLISECONDS);
    }

    public void shutdown() {
        for(GuiShutdownListener guiShutdownListener : this.shutdownListeners){
            guiShutdownListener.notifyShutdown();
        }
    }

    public void subscribe(GuiShutdownListener guiShutdownListener){
        this.shutdownListeners.add(guiShutdownListener);
    }

    public void unsubscribe(GuiShutdownListener guiShutdownListener){
        this.shutdownListeners.remove(guiShutdownListener);
    }

    public void resetStatistics(){
        gridWorld.resetStatistics();
    }

    public void displayHumanGraph(){
        DataAnalytics dataAnalytics = this.gridWorld.getDataAnalytics();
        dataAnalytics.drawHumanGraph();
    }
}
