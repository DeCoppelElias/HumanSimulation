package GuiPackage.Panels;

import GuiPackage.GuiController;
import SimulationApplication.GridPosition;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;

public class GridPanel extends JPanel {
    private GuiController guiController;
    private JButton[][] grid;

    private JPanel gridPanel;

    private int width;
    private int height;

    private ImageIcon humanIcon;
    private ImageIcon foodIcon;
    private boolean resizedHumanIconCheck = false;
    private boolean resizedFoodIconCheck = false;
    private Icon resizedHumanIcon;
    private Icon resizedFoodIcon;

    public GridPanel(GuiController guiController, int width, int height) throws IOException {
        this.guiController = guiController;
        this.width = width;
        this.height = height;

        this.setLayout(new BorderLayout());

        gridPanel = createGridPanel(width, height);
        this.add(gridPanel);

        this.humanIcon = new ImageIcon(ImageIO.read(getClass().getResource("/Human.png")));
        this.foodIcon = new ImageIcon(ImageIO.read(getClass().getResource("/Food.jpg")));
    }

    public JPanel createGridPanel(int width, int height){
        this.grid = new JButton[height][width];
        JPanel gridPanel = new JPanel(new GridLayout(height,width,50 / width,50 / height));
        for(int y = 0; y < height; y++){
            for(int x = 0; x < width; x++){
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(gridPanel.getPreferredSize().width / width, gridPanel.getPreferredSize().height / height));
                gridPanel.add(button);
                grid[height - 1 - y][x] = button;

                final int finalX = x;
                final int finalY = height - 1 - y;
                button.addActionListener(e -> {
                    try {
                        guiController.gridAction(new GridPosition(finalX, finalY));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
            }
        }

        return gridPanel;
    }

    public void setTileColor(GridPosition gridPosition, Color color) {
        JButton button = grid[gridPosition.getY()][gridPosition.getX()];
        button.setBackground(color);
    }

    public void setTileImage(GridPosition gridPosition, String imageName){
        JButton button = grid[gridPosition.getY()][gridPosition.getX()];
        if(imageName == null) button.setIcon(null);
        else if(imageName.equals("Human")){
            if(!resizedHumanIconCheck){
                resizedHumanIcon = resizeIcon(humanIcon,
                        gridPanel.getSize().width / width,
                        gridPanel.getSize().height / height);
                resizedHumanIconCheck = true;
            }
            button.setIcon(resizedHumanIcon);
        }
        else if(imageName.equals("Food")){
            if(!resizedFoodIconCheck){
                resizedFoodIcon = resizeIcon(foodIcon,
                        gridPanel.getSize().width / width,
                        gridPanel.getSize().height / height);
                resizedFoodIconCheck = true;
            }
            button.setIcon(resizedFoodIcon);
        }
    }

    private Icon resizeIcon(ImageIcon icon, int resizedWidth, int resizedHeight) {
        Image img = icon.getImage();
        Image resizedImage = img.getScaledInstance(resizedWidth, resizedHeight,  java.awt.Image.SCALE_SMOOTH);
        return new ImageIcon(resizedImage);
    }

    public void increaseGridSize(){
        this.width++;
        this.height++;

        this.remove(gridPanel);
        gridPanel = createGridPanel(width, height);
        this.add(gridPanel);

        this.revalidate();
        this.repaint();
    }
}
