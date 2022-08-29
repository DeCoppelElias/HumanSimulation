package GuiPackage.Panels;

import GuiPackage.GuiController;
import SimulationApplication.GridWorld;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

public class InfoPanel extends Panel {
    GuiController guiController;

    JPanel contentPanel;

    public InfoPanel(GuiController guiController){
        this.guiController = guiController;

        this.setLayout(new BorderLayout());

        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        JPanel holderPanel = new JPanel(new BorderLayout());
        holderPanel.add(contentPanel);

        JScrollPane scrollPane = new JScrollPane(holderPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        this.add(scrollPane);
    }

    public void displayInfo(ArrayList<String> info){
        for (String infoString : info){
            JLabel label = new JLabel();
            label.setText("<html>" + infoString.replaceAll("<","&lt;").replaceAll(">", "&gt;").replaceAll("\n", "<br/>") + "</html>");
            contentPanel.add(label);
        }

        contentPanel.repaint();
        contentPanel.revalidate();
    }

    public void resetInfo(){
        contentPanel.removeAll();
        contentPanel.repaint();
        contentPanel.revalidate();
    }

    public static void main(String[] args) throws Exception {
        JFrame frame = new JFrame("App");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(new Dimension(500,100));

        InfoPanel infoPanel = new InfoPanel(new GuiController(new GridWorld(5,5)));
        for(int i = 0; i < 1000; i++) infoPanel.displayInfo(new ArrayList<>(Arrays.asList("lol")));
        infoPanel.setPreferredSize(new Dimension(100,500));

        frame.add(infoPanel);
        frame.setVisible(true);
    }
}
