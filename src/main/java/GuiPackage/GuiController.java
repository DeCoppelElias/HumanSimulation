package GuiPackage;

import SimulationApplication.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.Timer;

public class GuiController {
    private enum GuiState {
        Normal,
        SpawningHuman,
        SpawningFood
    }

    private GuiState guiState = GuiState.Normal;

    private Gui gui;
    private GridWorldManager gridWorldManager;
    private int selectedGridContentId;
    private Boolean select = false;

    private Boolean automatic = false;

    private int rate = 500;
    private Timer timer;

    private Toolkit toolkit = Toolkit.getDefaultToolkit();
    private Image humanCursorImage;
    private Image foodCursorImage;

    public GuiController(GridWorldManager gridWorldManager) throws Exception {
        this.timer = new Timer(rate, e -> {
            try {
                advanceTime();
            } catch (RuntimeException ex) {
                ex.printStackTrace();
                automatic = false;
                timer.stop();
            }
        });
        this.gui = new Gui(this);
        this.gridWorldManager = gridWorldManager;

        humanCursorImage = ImageIO.read(getClass().getResource("/Human.png"));
        foodCursorImage = ImageIO.read(getClass().getResource("/Food.jpg"));
    }

    public void toSpawningHumanState() {
        this.guiState = GuiState.SpawningHuman;

        this.gui.setCursor(humanCursorImage);
    }

    public void toSpawningFoodState() {
        this.guiState = GuiState.SpawningFood;

        this.gui.setCursor(foodCursorImage);
    }

    public void toNormalState() {
        this.guiState = GuiState.Normal;

        this.gui.setCursor(null);
    }

    public void startGui() throws Exception {
        this.gui.initialize(gridWorldManager.getWidth(), gridWorldManager.getHeight());
    }

    public void spawnHuman(int amount) throws Exception {
        this.gridWorldManager.spawnHuman(amount);
        refreshGrid();
    }

    public void spawnHuman(GridPosition gridPosition) {
        this.gridWorldManager.spawnHuman(gridPosition);
        refreshGrid();
    }

    public void spawnFood(int amount) {
        this.gridWorldManager.spawnFood(amount);
        refreshGrid();
    }

    public void spawnFood(GridPosition gridPosition) {
        this.gridWorldManager.spawnFood(gridPosition);
        refreshGrid();
    }

    public String getInfoString(int id) {
        return this.gridWorldManager.getInfoString(id);
    }

    public void advanceTime() {
        this.gridWorldManager.automaticAdvance();

        gridWorldManager.advanceTime();
        refreshGrid();

        if (!select) return;
        displaySelectedGridContentInfo();
    }

    public void refreshGrid() {
        Hashtable<GridPosition, String> info = gridWorldManager.getInfo();

        Enumeration<GridPosition> e = info.keys();
        while (e.hasMoreElements()) {
            GridPosition gridPosition = e.nextElement();
            String s = info.get(gridPosition);

            gui.setTileColor(gridPosition, Color.white);
            if (s.contains("HUMAN")) {
                gui.setTileImage(gridPosition, "Human");
            } else if (s.contains("FOOD")) {
                gui.setTileImage(gridPosition, "Food");
            } else {
                gui.setTileImage(gridPosition, null);
            }
        }
    }

    public void displayInfo(GridPosition gridPosition) {
        this.gui.resetInfo();
        this.select = false;
        refreshGrid();

        ArrayList<Integer> gridContentIds = gridWorldManager.getEntityIds(gridPosition);

        this.gui.displayInfo(gridContentIds);
    }

    public void displaySelectedGridContentInfo() {
        if (!select) return;
        Boolean displayability = gridWorldManager.checkDisplayability(this.selectedGridContentId);
        if (displayability) {
            this.gui.resetInfo();
            this.gui.displayInfo(new ArrayList<>(List.of(this.selectedGridContentId)));
            if (gridWorldManager.isHuman(this.selectedGridContentId)) {
                int range = gridWorldManager.getRange(this.selectedGridContentId);
                GridPosition gridPosition = gridWorldManager.getGridPosition(this.selectedGridContentId);

                for (int x = gridPosition.getX() - range; x <= gridPosition.getX() + range; x++) {
                    for (int y = gridPosition.getY() - range; y <= gridPosition.getY() + range; y++) {
                        GridPosition currentGridPosition = new GridPosition(x, y);
                        if (gridWorldManager.isWithinBounds(currentGridPosition))
                            this.gui.setTileColor(currentGridPosition, Color.gray);
                    }
                }
            }
        } else {
            this.select = false;
            this.gui.resetInfo();
        }
    }

    public void displayAllHumans() {
        ArrayList<Integer> humanIds = this.gridWorldManager.getAllHumans();

        this.gui.displayInfo(humanIds);
    }

    public void selectGridContent(int id) {
        this.selectedGridContentId = id;
        this.select = true;
        displaySelectedGridContentInfo();
    }

    public void toggleAutomatic() {
        this.automatic = !this.automatic;

        if (automatic) timer.start();
        else timer.stop();
    }

    public void increaseAutomaticSpeed() {
        if (!automatic) return;
        if (rate > 100) {
            setRate(rate - 100);
        } else {
            setRate(Math.max(10, rate - 10));
        }
    }

    public void decreaseAutomaticSpeed() {
        if (!automatic) return;
        if (rate > 100) {
            setRate(Math.min(1000, rate + 100));
        } else {
            setRate(rate + 10);
        }
    }

    private void setRate(int newRate) {
        this.rate = newRate;
        timer.setDelay(rate);
        timer.setInitialDelay(rate);
    }

    public void resetStatistics() {
        gridWorldManager.resetStatistics();
    }

    public void displayHumanGraph() {
        this.gridWorldManager.displayHumanGraph();
    }

    public void gridAction(GridPosition gridPosition) throws Exception {
        if (guiState.equals(GuiState.Normal)) this.displayInfo(gridPosition);
        else if (guiState.equals(GuiState.SpawningHuman)) this.spawnHuman(gridPosition);
        else if (guiState.equals(GuiState.SpawningFood)) this.spawnFood(gridPosition);
    }

    public void resetGridWorld() {
        this.gridWorldManager.resetGridWorld();
        refreshGrid();
    }

    public Hashtable<String, Integer> getHumanParameterInfo() {
        return this.gridWorldManager.getHumanParameterInfo();
    }

    public void applyParameters(Hashtable<String, Integer> parameters) {
        this.gridWorldManager.applyParameters(parameters);
    }

    public ArrayList<Integer> sortOnSurvival(ArrayList<Integer> ids) {
        return this.gridWorldManager.sortOnSurvival(ids);
    }

    public ArrayList<Integer> sortOnFood(ArrayList<Integer> ids) {
        return this.gridWorldManager.sortOnFood(ids);
    }

    public void resetInfo() {
        this.gui.resetInfo();
    }
}
