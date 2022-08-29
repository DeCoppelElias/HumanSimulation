import DataAnalytics.DataAnalytics;
import GuiPackage.Gui;
import GuiPackage.GuiController;
import GuiPackage.GuiShutdownListener;
import SimulationApplication.GridWorld;

import java.io.IOException;

public class Application implements GuiShutdownListener {
    private GuiController guiController;
    private GridWorld gridWorld;

    public Application() throws Exception {
        this.gridWorld = new GridWorld(20,20);
        this.guiController = new GuiController(gridWorld);
        this.guiController.subscribe(this);
    }

    public void execute() throws Exception {
        guiController.startGui();
    }

    public DataAnalytics getDataAnalytics(){
        return this.gridWorld.getDataAnalytics();
    }

    public void notifyShutdown() {

    }
}
