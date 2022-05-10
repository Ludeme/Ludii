package app.display.dialogs.visual_editor.view.panels.settings;

import app.display.dialogs.visual_editor.LayoutManagement.GraphDrawing.GraphPanel;
import app.display.dialogs.visual_editor.LayoutManagement.LayoutManager.LayoutHandler;
import app.display.dialogs.visual_editor.LayoutManagement.LayoutManager.LayoutMethod;
import app.display.dialogs.visual_editor.view.panels.IGraphPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LayoutSettingsPanel extends JPanel
{
    public LayoutSettingsPanel(LayoutHandler lh)
    {
        JSlider distanceSlider = new JSlider(0, 100);
        JSlider offsetSlider = new JSlider(0, 100);
        JSlider spreadSlider = new JSlider(0, 100);

        Button redraw = new Button("Redraw");
        JCheckBox auto = new JCheckBox("Redraw automatically");

        redraw.addActionListener(e -> {
            lh.updateAllWeights(offsetSlider.getValue() / 100.0,
                    distanceSlider.getValue() / 100.0,
                    spreadSlider.getValue() / 100.0);
            lh.executeLayout();
        });

        distanceSlider.addChangeListener(e -> {
            if (auto.isSelected()){
                lh.updateDistance(distanceSlider.getValue() / 100.0);
                lh.executeLayout();
            }
        });

        offsetSlider.addChangeListener(e -> {
            if (auto.isSelected()){
                lh.updateDistance(offsetSlider.getValue() / 100.0);
                lh.executeLayout();
            }
        });

        spreadSlider.addChangeListener(e -> {
            if (auto.isSelected()){
                lh.updateDistance(spreadSlider.getValue() / 100.0);
                lh.executeLayout();
            }
        });

        add(distanceSlider);
        add(offsetSlider);
        add(spreadSlider);
        add(redraw);
        add(auto);
    }

    public static JFrame getSettingsFrame(LayoutHandler lh)
    {
        JFrame frame = new JFrame("Layout Settings");
        frame.setSize(250, 400);
        frame.add(new LayoutSettingsPanel(lh));
        frame.setVisible(true);
        return frame;
    }
}
