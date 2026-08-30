import DataAnalytics.DataAnalytics;
import GuiPackage.GuiController;
import SimulationApplication.GridWorld;
import SimulationApplication.GridWorldManager;

public class Application {
    private GuiController guiController;
    private GridWorld gridWorld;

    public Application() throws Exception {
        this.gridWorld = new GridWorld(20, 20);
        GridWorldManager gridWorldManager = new GridWorldManager(gridWorld);
        this.guiController = new GuiController(gridWorldManager);
    }

    public void execute() throws Exception {
        guiController.startGui();
    }

    public DataAnalytics getDataAnalytics() {
        return this.gridWorld.getDataAnalytics();
    }
}
