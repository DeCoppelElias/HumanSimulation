package GuiPackage.Panels;

import GuiPackage.GuiController;
import SimulationApplication.GridPosition;

import javax.swing.*;
import java.awt.*;

public class GridPanel extends JPanel {
    private GuiController guiController;
    private JButton[][] grid;

    private int width;
    private int height;

    private Icon humanIcon;
    private Icon foodIcon;

    public GridPanel(GuiController guiController, int width, int height) {
        this.guiController = guiController;
        this.width = width;
        this.height = height;
        this.grid = new JButton[height][width];

        ImageIcon originalHumanIcon = new ImageIcon("Images/Human.png");
        this.humanIcon = resizeIcon(originalHumanIcon, 500 / width, 500 / height);

        ImageIcon originalFoodIcon = new ImageIcon("Images/Food.jpg");
        this.foodIcon = resizeIcon(originalFoodIcon, 500 / width, 500 / height);

        this.setLayout(new BorderLayout());

        JPanel gridPanel = new JPanel(new GridLayout(height,width,50 / width,50 / height));
        gridPanel.setPreferredSize(new Dimension(500,500));
        for(int y = 0; y < height; y++){
            for(int x = 0; x < width; x++){
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(500 / width, 500 / height));
                gridPanel.add(button);
                grid[height - 1 - y][x] = button;

                final int finalX = x;
                final int finalY = height - 1 - y;
                button.addActionListener(e -> {
                    try {
                        guiController.displayInfo(new GridPosition(finalX, finalY));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
            }
        }
        this.add(gridPanel);
    }

    public void setTileColor(GridPosition gridPosition, Color color) {
        JButton button = grid[gridPosition.getY()][gridPosition.getX()];
        button.setBackground(color);
    }

    public void setTileImage(GridPosition gridPosition, String imageName){
        JButton button = grid[gridPosition.getY()][gridPosition.getX()];
        if(imageName == null) button.setIcon(null);
        else if(imageName.equals("Human")){
            button.setIcon(humanIcon);
        }
        else if(imageName.equals("Food")){
            button.setIcon(foodIcon);
        }
    }

    private Icon resizeIcon(ImageIcon icon, int resizedWidth, int resizedHeight) {
        Image img = icon.getImage();
        Image resizedImage = img.getScaledInstance(resizedWidth, resizedHeight,  java.awt.Image.SCALE_SMOOTH);
        return new ImageIcon(resizedImage);
    }
}