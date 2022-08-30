package GuiPackage.Panels;

import GuiPackage.GuiController;
import SimulationApplication.GridWorld;
import org.jfree.chart.axis.Axis;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class ButtonPanel extends Panel {
    private GuiController guiController;

    private CardLayout cardLayout;
    private Container contentPane;

    public ButtonPanel(GuiController guiController){
        this.guiController = guiController;

        cardLayout = new CardLayout();
        this.setLayout(cardLayout);

        JPanel mainPanel = createMainPanel();
        JPanel addEntitiesPanel = createAddEntitiesPanel();
        JPanel advancePanel = createAdvancePanel();
        JPanel statisticsPanel = createStatisticsPanel();

        this.add("mainPanel", mainPanel);
        this.add("addEntitiesPanel", addEntitiesPanel);
        this.add("advancePanel", advancePanel);
        this.add("statisticsPanel", statisticsPanel);

        this.contentPane = this;
    }

    private void toAddEntitiesLayout(){
        cardLayout.show(contentPane, "addEntitiesPanel");
    }

    private void toMainLayout(){
        cardLayout.show(contentPane, "mainPanel");
    }

    private void toAdvanceLayout(){
        cardLayout.show(contentPane, "advancePanel");
    }

    private void toStatisticsLayout(){
        cardLayout.show(contentPane, "statisticsPanel");
    }

    private JPanel createMainPanel(){
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setPreferredSize(new Dimension(100,500));

        JButton addButton = new JButton("Add");
        addButton.addActionListener(e -> {
            toAddEntitiesLayout();
        });
        mainPanel.add(addButton);

        JButton advanceButton = new JButton("Advance");
        advanceButton.addActionListener(e -> {
            toAdvanceLayout();
        });
        mainPanel.add(advanceButton);

        JButton statisticsButton = new JButton("Statistics");
        statisticsButton.addActionListener(e -> {
            toStatisticsLayout();
        });
        mainPanel.add(statisticsButton);

        JButton resetGridButton = new JButton("Reset GridWorld");
        resetGridButton.addActionListener(e -> {
            try {
                this.guiController.resetGridWorld();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        mainPanel.add(resetGridButton);

        return mainPanel;
    }

    private JPanel createAddEntitiesPanel(){
        JPanel addEntitiesPanel = new JPanel();
        addEntitiesPanel.setLayout(new BoxLayout(addEntitiesPanel, BoxLayout.Y_AXIS));

        // Add Humans Random
        JPanel addHumanAmountPanel = new JPanel();
        addHumanAmountPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        addHumanAmountPanel.setAlignmentX(0);

        JTextArea amountHumansTextArea = new JTextArea("1",1,1);
        amountHumansTextArea.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if ( ((c < '0') || (c > '9')) && (c != KeyEvent.VK_BACK_SPACE)) {
                    e.consume();  // if it's not a number, ignore the event
                }
            }
        });

        JButton addHumanRandomButton = new JButton("Add Human Random");
        addHumanRandomButton.addActionListener(e -> {
            try {
                int amount = Integer.parseInt(amountHumansTextArea.getText());
                guiController.spawnHuman(amount);
                guiController.toNormalState();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        addHumanAmountPanel.add(addHumanRandomButton);
        addHumanAmountPanel.add(amountHumansTextArea);

        addEntitiesPanel.add(addHumanAmountPanel);

        // Add Food Random
        JPanel addFoodAmountPanel = new JPanel();
        addFoodAmountPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        addFoodAmountPanel.setAlignmentX(0);

        JTextArea amountFoodTextArea = new JTextArea("1",1,1);
        amountFoodTextArea.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if ( ((c < '0') || (c > '9')) && (c != KeyEvent.VK_BACK_SPACE)) {
                    e.consume();  // if it's not a number, ignore the event
                }
            }
        });

        JButton addFoodRandomButton = new JButton("Add Food Random");
        addFoodRandomButton.addActionListener(e -> {
            try {
                int amount = Integer.parseInt(amountFoodTextArea.getText());
                guiController.spawnFood(amount);
                guiController.toNormalState();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        addFoodAmountPanel.add(addFoodRandomButton);
        addFoodAmountPanel.add(amountFoodTextArea);

        addEntitiesPanel.add(addFoodAmountPanel);

        // Add Humans Position
        JButton addHumanPositionButton = new JButton("Add Human Position");
        addHumanPositionButton.addActionListener(e -> {
            try {
                guiController.toSpawingHumanState();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        addHumanPositionButton.setAlignmentX(0);
        addEntitiesPanel.add(addHumanPositionButton);

        // Add Food Position
        JButton addFoodPositionButton = new JButton("Add Food Position");
        addFoodPositionButton.addActionListener(e -> {
            try {
                guiController.toSpawingFoodState();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        addFoodPositionButton.setAlignmentX(0);
        addEntitiesPanel.add(addFoodPositionButton);

        // Back to main panel
        JButton returnButton = new JButton("Return");
        returnButton.addActionListener(e -> {
            toMainLayout();
            this.guiController.toNormalState();
        });
        returnButton.setAlignmentX(0);
        addEntitiesPanel.add(returnButton);

        Box.Filler glue = (Box.Filler) Box.createVerticalGlue();
        glue.changeShape(glue.getMinimumSize(),
                new Dimension(0, Short.MAX_VALUE), // make glue greedy
                glue.getMaximumSize());
        addEntitiesPanel.add(glue);

        return addEntitiesPanel;
    }

    private JPanel createAdvancePanel(){
        JPanel advancePanel = new JPanel();
        advancePanel.setLayout(new BoxLayout(advancePanel, BoxLayout.Y_AXIS));
        advancePanel.setPreferredSize(new Dimension(100,500));

        // Advance Time Button
        JButton advanceTimeButton = new JButton("Advance Time");
        advanceTimeButton.addActionListener(e -> {
            try {
                guiController.advanceTime();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        advancePanel.add(advanceTimeButton);

        JButton automaticButton = new JButton("Automatic");
        automaticButton.addActionListener(e -> {
            try {
                guiController.toggleAutomatic();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        advancePanel.add(automaticButton);

        JButton increaseSpeedButton = new JButton("Increase Speed");
        increaseSpeedButton.addActionListener(e -> {
            try {
                guiController.increaseAutomaticSpeed();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        advancePanel.add(increaseSpeedButton);

        JButton decreaseSpeedButton = new JButton("Decrease Speed");
        decreaseSpeedButton.addActionListener(e -> {
            try {
                guiController.decreaseAutomaticSpeed();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        advancePanel.add(decreaseSpeedButton);

        // Back to main panel
        JButton returnButton = new JButton("Return");
        returnButton.addActionListener(e -> {
            toMainLayout();
        });
        advancePanel.add(returnButton);

        return advancePanel;
    }

    private JPanel createStatisticsPanel(){
        JPanel statisticsPanel = new JPanel();
        statisticsPanel.setLayout(new BoxLayout(statisticsPanel, BoxLayout.Y_AXIS));
        statisticsPanel.setPreferredSize(new Dimension(100,500));

        JButton displayGraphButton = new JButton("Display Graph");
        displayGraphButton.addActionListener(e -> {
            try {
                guiController.displayHumanGraph();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        statisticsPanel.add(displayGraphButton);

        JButton resetStatisticsButton = new JButton("Reset Statistics");
        resetStatisticsButton.addActionListener(e -> {
            try {
                guiController.resetStatistics();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        statisticsPanel.add(resetStatisticsButton);

        // Back to main panel
        JButton returnButton = new JButton("Return");
        returnButton.addActionListener(e -> {
            toMainLayout();
        });
        statisticsPanel.add(returnButton);

        return statisticsPanel;
    }

    public static void main(String[] args) throws Exception {
        JFrame frame = new JFrame("App");
        frame.setSize(800,500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        ButtonPanel buttonPanel = new ButtonPanel(new GuiController(new GridWorld(5,5)));

        frame.add(buttonPanel);
        frame.setVisible(true);
    }
}
