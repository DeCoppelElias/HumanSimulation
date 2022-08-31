package GuiPackage;

import GuiPackage.Panels.ButtonPanel;
import GuiPackage.Panels.GridPanel;
import GuiPackage.Panels.InfoPanel;
import SimulationApplication.GridPosition;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class Gui {
    private JFrame mainFrame;

    private GuiController guiController;

    private InfoPanel infoPanel;
    private GridPanel gridPanel;

    private Boolean initialized = false;

    public Gui(GuiController guiController) {
        this.guiController = guiController;
    }

    private Toolkit toolkit = Toolkit.getDefaultToolkit();

    public void initialize(int width, int height) throws Exception {
        this.initialized = true;

        mainFrame = new JFrame("App");
        mainFrame.setSize(900,500);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Main Panel
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Button Panel
        ButtonPanel buttonPanel = new ButtonPanel(this.guiController);
        mainPanel.add(buttonPanel, BorderLayout.LINE_START);
        //buttonPanel.setPreferredSize(new Dimension(200,500));

        // Grid Panel
        gridPanel = new GridPanel(guiController, width, height);
        mainPanel.add(gridPanel, BorderLayout.CENTER);
        //gridPanel.setPreferredSize(new Dimension(500,500));

        // Info Panel
        infoPanel = new InfoPanel(this.guiController);
        mainPanel.add(infoPanel, BorderLayout.LINE_END);
        //infoPanel.setPreferredSize(new Dimension(200,500));

        mainFrame.add(mainPanel);
        mainFrame.setVisible(true);

        guiController.refreshGrid();

        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            @Override
            public void run()
            {
                guiController.shutdown();
            }
        });
    }

    public void setTileColor(GridPosition gridPosition, Color color) {
        if(!initialized) return;

        gridPanel.setTileColor(gridPosition, color);
    }

    public void setTileImage(GridPosition gridPosition, String imageName) {
        if(!initialized) return;

        gridPanel.setTileImage(gridPosition, imageName);
    }

    public void displayInfo(ArrayList<Integer> gridContentIds){
        if(!initialized) return;

        infoPanel.displayInfo(gridContentIds);
    }

    public void resetInfo(){
        if(!initialized) return;

        infoPanel.resetInfo();
    }

    public void setCursor(Image image){
        if(!initialized) return;

        if(image == null) mainFrame.setCursor(null);
        else{
            Cursor c = toolkit.createCustomCursor(image , new Point(0, 0), "customCursor");
            mainFrame.setCursor(c);
        }
    }

    public void increaseGridSize(){
        this.gridPanel.increaseGridSize();
    }
}
