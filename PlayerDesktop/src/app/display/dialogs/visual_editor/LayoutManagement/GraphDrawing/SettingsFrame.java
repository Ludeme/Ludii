package app.display.dialogs.visual_editor.LayoutManagement.GraphDrawing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Settings of the layout algorithms
 * @author nic0gin
 */

public class SettingsFrame extends JFrame
{

    public SettingsFrame(Timer timer)
    {
        setTitle("Settings");
        setSize(400, 250);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        add(getSettingsPanel(timer));

        setVisible(true);
    }

    private JPanel getSettingsPanel(Timer timer) {
        // Start button
        Button startBtn = new Button("Start");
        startBtn.addActionListener(new GraphPanel.StartListener(timer));

        // Re-start button
        Button restartBtn = new Button("Restart");

        JTextField constantC = new JTextField("1.0");

        restartBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO: randomise positions
                // ExpGraph.getGraphInstance().randomizeNodePos();
                //TODO implement with an instance of LayoutManager
                //LayoutManager.t = (double) DrawingFrame.getWIDTH()/10;
                //LayoutManager.C = Double.parseDouble(constantC.getText());
            }
        });

        // Stop button
        Button stopBtn = new Button("Stop");
        stopBtn.addActionListener(new GraphPanel.StopListener(timer));

        JSlider coolingRate = new JSlider(1, 100);
        //TODO implement with an instance of LayoutManager
        //coolingRate.addChangeListener(e -> LayoutManager.coolRate = (double)(coolingRate.getValue())/100.0);

        JPanel settings = new JPanel();
        settings.add(startBtn);
        settings.add(restartBtn);
        settings.add(stopBtn);
        settings.add(constantC);
        settings.add(coolingRate);

        return settings;
    }

}
