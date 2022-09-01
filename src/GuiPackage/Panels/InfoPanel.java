package GuiPackage.Panels;

import GuiPackage.GuiController;
import SimulationApplication.GridPosition;
import SimulationApplication.GridWorld;
import SimulationApplication.GridWorldManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

public class InfoPanel extends JPanel {
    private GuiController guiController;

    private JPanel contentPanel;

    private ArrayList<Integer> ids = new ArrayList<>();

    private JPopupMenu popupMenu;

    public InfoPanel(GuiController guiController){
        this.guiController = guiController;

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setAlignmentX(0);

        JLabel headerLabel = new JLabel("Info Panel");
        headerLabel.setAlignmentX(0);
        headerLabel.setBorder(new EmptyBorder(0,100,10,100));//top,left,bottom,right
        this.add(headerLabel);

        JButton sortButton = new JButton("Sort");
        sortButton.setAlignmentX(0);
        sortButton.addActionListener(e -> {
            popupMenu.show(sortButton,sortButton.getX() + sortButton.getWidth(), sortButton.getY() - sortButton.getHeight());
        });
        this.add(sortButton);

        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setAlignmentX(0);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        JPanel holderPanel = new JPanel(new BorderLayout());
        holderPanel.setAlignmentX(0);
        holderPanel.add(contentPanel);

        JScrollPane scrollPane = new JScrollPane(holderPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setAlignmentX(0);
        scrollPane.setBorder(new EmptyBorder(0,0,0,10));//top,left,bottom,right

        this.add(scrollPane);

        popupMenu = createJPopupMenu();
    }

    public void displayInfo(ArrayList<Integer> gridContentIds){
        ids = gridContentIds;

        display(gridContentIds);
    }

    private void display(ArrayList<Integer> gridContentIds){
        for (Integer id : gridContentIds){
            String infoString = guiController.getInfoString(id);

            JButton button = new JButton();
            button.setAlignmentX(0);
            button.setHorizontalAlignment(SwingConstants.LEFT);
            button.addActionListener(e -> {
                try {
                    guiController.selectGridContent(id);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            button.setText("<html>" + infoString.replaceAll("<","&lt;").replaceAll(">", "&gt;").replaceAll("\n", "<br/>") + "</html>");
            button.setFont(new Font("Serif", Font.PLAIN, 10));

            contentPanel.add(button);
        }

        contentPanel.repaint();
        contentPanel.revalidate();
    }

    public void resetInfo(){
        contentPanel.removeAll();
        contentPanel.repaint();
        contentPanel.revalidate();
    }

    public JPopupMenu createJPopupMenu(){
        JPopupMenu popupMenu = new JPopupMenu("Sort");
        popupMenu.setAlignmentX(0);

        JButton sortBySurvivalButton = new JButton("Sort By Days");
        sortBySurvivalButton.addActionListener(e -> {
            resetInfo();
            ArrayList<Integer> sortedIds = this.guiController.sortOnSurvival(ids);
            display(sortedIds);
        });
        popupMenu.add(sortBySurvivalButton);

        JButton sortByFoodButton = new JButton("Sort By Food");
        sortByFoodButton.addActionListener(e -> {
            resetInfo();
            ArrayList<Integer> sortedIds = this.guiController.sortOnFood(ids);
            display(sortedIds);
        });
        popupMenu.add(sortByFoodButton);

        return popupMenu;
    }

    public static void main(String[] args) throws Exception {
        JFrame frame = new JFrame("App");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(new Dimension(500,100));

        InfoPanel infoPanel = new InfoPanel(new GuiController(new GridWorldManager(new GridWorld(5,5))));
        infoPanel.setPreferredSize(new Dimension(100,500));

        frame.add(infoPanel);
        frame.setVisible(true);
    }
}
