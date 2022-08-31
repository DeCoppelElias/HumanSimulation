package GuiPackage.Panels;

import GuiPackage.GuiController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

public class GridWorldParametersPanel extends JPanel {
    private GuiController guiController;

    private Hashtable<String, Integer> parameters;

    private Hashtable<String, JTextArea> textAreas = new Hashtable<>();

    public GridWorldParametersPanel(GuiController guiController){
        this.guiController = guiController;
        this.parameters = guiController.getHumanParameterInfo();

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setAlignmentX(0);

        // Title
        JLabel label = new JLabel("Grid World Parameters");
        this.add(label);

        // Parameters with textbox
        Enumeration<String> e = parameters.keys();
        while(e.hasMoreElements()){
            JPanel parameterPanel = new JPanel();
            parameterPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
            parameterPanel.setAlignmentX(0);

            String name = e.nextElement();
            Integer value = parameters.get(name);

            JTextArea parameterTextArea = new JTextArea(Integer.toString(value),1,1);
            parameterTextArea.addKeyListener(new KeyAdapter() {
                public void keyTyped(KeyEvent e) {
                    char c = e.getKeyChar();
                    if ( ((c < '1') || (c > '9')) && (c != KeyEvent.VK_BACK_SPACE)) {
                        e.consume();  // if it's not a number, ignore the event
                    }
                }
            });
            textAreas.put(name, parameterTextArea);

            JLabel parameterLabel = new JLabel(name + ": ", SwingConstants.LEFT);
            parameterLabel.setFont(new Font("Serif", Font.PLAIN, 10));

            parameterPanel.add(parameterLabel);
            parameterPanel.add(parameterTextArea);

            this.add(parameterPanel);
        }

        // Apply button
        JButton applyButton = new JButton("Apply Changes");
        applyButton.setAlignmentX(0);
        applyButton.addActionListener(a -> {
            try {
                applyParameterChanges();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        this.add(applyButton);
    }

    private void applyParameterChanges(){
        Hashtable<String, Integer> newParameters = new Hashtable<>();

        Enumeration<String> e = textAreas.keys();
        while (e.hasMoreElements()){
            String name = e.nextElement();
            JTextArea textArea = textAreas.get(name);

            if(textArea.getText().equals(""))
                newParameters.put(name, 1);
            else{
                Integer value = Integer.parseInt(textArea.getText());
                newParameters.put(name, 1);
            }
        }

        this.parameters = newParameters;
        this.guiController.applyParameters(newParameters);
    }
}
