package app.display.dialogs.visual_editor.view.panels.settings;

import app.display.dialogs.visual_editor.LayoutManagement.GraphRoutines;
import app.display.dialogs.visual_editor.LayoutManagement.LayoutManager.LayoutHandler;
import app.display.dialogs.visual_editor.view.panels.IGraphPanel;

import javax.swing.*;
import java.awt.*;

public class LayoutSettingsPanel extends JPanel
{
    private final JSlider dSl;
    private final JSlider oSl;
    private final JSlider sSl;
    private final LayoutHandler lh;

    private final double DISTANCE_ADD = 0.26;
    private final double SPREAD_ADD = 0.34;

    public LayoutSettingsPanel(IGraphPanel graphPanel)
    {
        lh = graphPanel.getLayoutHandler();

        dSl = new JSlider(0, 100);
        oSl = new JSlider(-100, 100);
        sSl = new JSlider(0, 100);

        JLabel distanceText = new JLabel("Distance: " + getSliderValue(dSl));
        JLabel offsetText = new JLabel("Offset: " + getSliderValue(oSl));
        JLabel spreadText = new JLabel("Spread: " + getSliderValue(sSl));

        Button redraw = new Button("Redraw");
        Button evaluate = new Button("Evaluate metrics");

        JCheckBox auto = new JCheckBox("Redraw automatically");
        JCheckBox metrics = new JCheckBox("Use slider metrics");
        metrics.setSelected(true);

        JTextField dmTextField = new JTextField("3000");
        JLabel dmLabel = new JLabel("DM");

        JTextField omTextField = new JTextField("150");
        JLabel omLabel = new JLabel("OM");

        JTextField smTextField = new JTextField("3500");
        JLabel smLabel = new JLabel("SM");

        redraw.addActionListener(e -> {
            if (metrics.isSelected())
            {
                updateWeights();
            }
            else
            {
                GraphRoutines.setDM(Integer.parseInt(dmTextField.getText()));
                GraphRoutines.setOM(Integer.parseInt(omTextField.getText()));
                GraphRoutines.setSM(Integer.parseInt(smTextField.getText()));
                lh.evaluateGraphWeights();
            }
            executeDFSLayout(graphPanel);
        });

        evaluate.addActionListener(e -> {
            GraphRoutines.setDM(Integer.parseInt(dmTextField.getText()));
            GraphRoutines.setOM(Integer.parseInt(omTextField.getText()));
            GraphRoutines.setSM(Integer.parseInt(smTextField.getText()));
            lh.evaluateGraphWeights();
        });

        dSl.addChangeListener(e -> {
            distanceText.setText("Distance: " + getSliderValue(dSl));
            if (auto.isSelected()){
                updateWeights();
                executeDFSLayout(graphPanel);
            }
        });

        oSl.addChangeListener(e -> {
            offsetText.setText("Offset: " + getSliderValue(oSl));
            if (auto.isSelected()){
                updateWeights();
                executeDFSLayout(graphPanel);
            }
        });

        sSl.addChangeListener(e -> {
            spreadText.setText("Spread: " + getSliderValue(sSl));
            if (auto.isSelected()){
                updateWeights();
                executeDFSLayout(graphPanel);
            }
        });

        add(distanceText);
        add(dSl);

        add(offsetText);
        add(oSl);

        add(spreadText);
        add(sSl);
        add(redraw);
        add(auto);
        //add(evaluate);
        //add(metrics);

        //add(dmLabel);
        //add(dmTextField);
        //add(omLabel);
        //add(omTextField);
        //add(smLabel);
        //add(smTextField);
    }

    private void updateWeights()
    {
        lh.updateDFSWeights(getSliderValue(oSl),
                getSliderValue(dSl),
                getSliderValue(sSl));
    }

    private double getSliderValue(JSlider slider) {return slider.getValue() / 100.0;}

    private void executeDFSLayout(IGraphPanel graphPanel)
    {
        graphPanel.getLayoutHandler().setLayoutMethod(1);
        graphPanel.getLayoutHandler().executeLayout();
        graphPanel.drawGraph(graphPanel.getGraph());
    }

    public static void getSettingsFrame(IGraphPanel graphPanel)
    {
        JFrame frame = new JFrame("Layout Settings");
        frame.setSize(300, 400);
        frame.add(new LayoutSettingsPanel(graphPanel));
        frame.setVisible(true);
        frame.setResizable(false);
    }
}
