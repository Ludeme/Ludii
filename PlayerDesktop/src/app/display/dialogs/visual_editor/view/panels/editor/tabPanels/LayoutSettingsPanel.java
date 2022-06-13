package app.display.dialogs.visual_editor.view.panels.editor.tabPanels;

import app.display.dialogs.visual_editor.LayoutManagement.GraphDrawing.NodePlacementRoutines;
import app.display.dialogs.visual_editor.LayoutManagement.GraphRoutines;
import app.display.dialogs.visual_editor.LayoutManagement.LayoutManager.LayoutHandler;
import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.view.panels.IGraphPanel;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Class for the single instance of the Layout Settings Panel
 */
public class LayoutSettingsPanel extends JPanel
{
    private final JSlider dSl;
    private final JSlider oSl;
    private final JSlider sSl;
    private final LayoutHandler lh;
    private final JLabel selectedComponent;

    private static LayoutSettingsPanel lsPanel;

    private final JCheckBox autoUpdateWeights = new JCheckBox("Automatically update weights");
    private final JCheckBox autoPlacement = new JCheckBox("Automatic placement");
    private final JCheckBox layerPlacement = new JCheckBox("Layer placement");

    private LayoutSettingsPanel(IGraphPanel graphPanel)
    {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        selectedComponent = new JLabel("Selection: Empty");

        lh = graphPanel.getLayoutHandler();

        dSl = new JSlider(0, 100);
        oSl = new JSlider(-100, 100);
        sSl = new JSlider(0, 100);

        JLabel distanceText = new JLabel("Distance: " + getSliderValue(dSl));
        JLabel offsetText = new JLabel("Offset: " + getSliderValue(oSl));
        JLabel spreadText = new JLabel("Spread: " + getSliderValue(sSl));

        Button redraw = new Button("Arrange graph");
        Button alignX = new Button("Align by X-axis");
        Button alignY = new Button("Align by Y-axis");

        redraw.addActionListener(e -> {
            lh.evaluateGraphWeights();
            executeDFSLayout(graphPanel);
        });

        alignX.addActionListener(e -> {
            NodePlacementRoutines.alignNodes(graphPanel.getSelectedNodes(), NodePlacementRoutines.X_AXIS);
            graphPanel.drawGraph(graphPanel.getGraph());
        });

        alignY.addActionListener(e -> {
            NodePlacementRoutines.alignNodes(graphPanel.getSelectedNodes(), NodePlacementRoutines.Y_AXIS);
            graphPanel.drawGraph(graphPanel.getGraph());
        });

        ChangeListener sliderUpdateListener = e -> {
            distanceText.setText("Distance: " + getSliderValue(dSl));
            offsetText.setText("Offset: " + getSliderValue(oSl));
            spreadText.setText("Spread: " + getSliderValue(sSl));
            updateWeights();
            executeDFSLayout(graphPanel);
        };

        dSl.addChangeListener(sliderUpdateListener);
        oSl.addChangeListener(sliderUpdateListener);
        sSl.addChangeListener(sliderUpdateListener);

        // adding sliders
        add(selectedComponent);
        add(distanceText);
        add(dSl);
        add(offsetText);
        add(oSl);
        add(spreadText);
        add(sSl);
        // redraw button
        redraw.setMaximumSize(new Dimension(550, 25));
        alignX.setMaximumSize(new Dimension(550, 25));
        alignY.setMaximumSize(new Dimension(550, 25));
        add(redraw);
        add(alignX);
        add(alignY);
        // adding check boxes
        add(autoUpdateWeights);
        add(autoPlacement);
        add(layerPlacement);
    }

    public static LayoutSettingsPanel getLayoutSettingsPanel()
    {
        if (lsPanel == null) lsPanel = new LayoutSettingsPanel(Handler.editorPanel);
        return lsPanel;
    }

    private void updateWeights()
    {
        lh.updateDFSWeights(getSliderValue(oSl),
                getSliderValue(dSl),
                getSliderValue(sSl));
    }

    public void updateSliderValues(double d, double o, double s)
    {
        dSl.setValue((int)(d * 100));
        oSl.setValue((int)(o * 100));
        sSl.setValue((int)(s * 100));
    }

    private double getSliderValue(JSlider slider) {return slider.getValue() / 100.0;}

    private void executeDFSLayout(IGraphPanel graphPanel)
    {
        int root = graphPanel.getSelectedRootId();
        graphPanel.getLayoutHandler().executeLayout(root);
        graphPanel.drawGraph(graphPanel.getGraph());
    }

    public void setSelectedComponent(String node, boolean subtree)
    {
        if (subtree) selectedComponent.setText("Selected subtree of: "+node);
        else selectedComponent.setText("Selected: "+node);
    }

    public static void getSettingsFrame(IGraphPanel graphPanel)
    {
        JFrame frame = new JFrame("Layout Settings");
        frame.setSize(300, 400);
        frame.add(new LayoutSettingsPanel(graphPanel));
        frame.setVisible(true);
        frame.setResizable(false);
    }

    public boolean isAutoUpdateWeightOn() {return autoUpdateWeights.isSelected();}

    public boolean isAutoPlacementOn() {return autoPlacement.isSelected();}

    public boolean isLayerPlacementOn() {return layerPlacement.isSelected();}

}
