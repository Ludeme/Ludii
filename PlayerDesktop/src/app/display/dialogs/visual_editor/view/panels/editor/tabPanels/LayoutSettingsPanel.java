package app.display.dialogs.visual_editor.view.panels.editor.tabPanels;

import app.display.dialogs.visual_editor.LayoutManagement.NodePlacementRoutines;
import app.display.dialogs.visual_editor.LayoutManagement.LayoutHandler;
import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.view.panels.IGraphPanel;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;

/**
 * Class for the single instance of the Layout Settings Panel
 */
public class LayoutSettingsPanel extends JPanel
{
    private final JSlider dSl;
    private final JSlider oSl;
    private final JSlider sSl;
    private final JSlider cSl;
    private final LayoutHandler lh;
    private final JLabel selectedComponent;

    private static LayoutSettingsPanel lsPanel;

    private boolean changeListen = true;

    private final JCheckBox autoPlacement = new JCheckBox("Automatic placement");
    private final JCheckBox animatePlacement = new JCheckBox("Animate layout");

    private final JButton fixNodes;
    private final JButton unfixNodes;

    private LayoutSettingsPanel(IGraphPanel graphPanel)
    {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        selectedComponent = new JLabel("Selection: Empty");

        lh = graphPanel.getLayoutHandler();

        dSl = new JSlider(0, 100);
        oSl = new JSlider(0, 100);
        sSl = new JSlider(0, 100);
        cSl = new JSlider(0, 100);

        JLabel distanceText = new JLabel("Distance: " + getSliderValue(dSl));
        JLabel offsetText = new JLabel("Offset: " + getSliderValue(oSl));
        JLabel spreadText = new JLabel("Spread: " + getSliderValue(sSl));
        JLabel compactnessText = new JLabel("Compactness: " + getSliderValue(cSl));

        Dimension buttonDim = new Dimension(150, 20);
        JButton redraw = createButton("Arrange graph", buttonDim);
        JButton alignX = createButton("Align vertically", buttonDim);
        JButton alignY = createButton("Align horizontally", buttonDim);

        fixNodes = createButton("Fix subtree", buttonDim);
        fixNodes.setEnabled(false);
        unfixNodes = createButton("Unfix subtree", buttonDim);
        unfixNodes.setEnabled(false);

        redraw.addActionListener(e -> {
            lh.evaluateGraphWeights();
            executeDFSLayout(graphPanel);
            graphPanel.deselectEverything();
        });

        alignX.addActionListener(e -> {
            NodePlacementRoutines.alignNodes(graphPanel.selectedNodes(), NodePlacementRoutines.X_AXIS, graphPanel);
            // graphPanel.updateNodePositions();
            // graphPanel.deselectEverything();
        });

        alignY.addActionListener(e -> {
            NodePlacementRoutines.alignNodes(graphPanel.selectedNodes(), NodePlacementRoutines.Y_AXIS, graphPanel);
            // graphPanel.updateNodePositions();
            // graphPanel.deselectEverything();
        });

        ChangeListener sliderUpdateListener = e -> {
            distanceText.setText("Distance: " + getSliderValue(dSl));
            offsetText.setText("Offset: " + getSliderValue(oSl));
            spreadText.setText("Spread: " + getSliderValue(sSl));
            compactnessText.setText("Compactness: " + getSliderValue(cSl));
            if (changeListen)
            {
                updateWeights();
                executeDFSLayout(graphPanel);
            }
        };

        dSl.addChangeListener(sliderUpdateListener);
        oSl.addChangeListener(sliderUpdateListener);
        sSl.addChangeListener(sliderUpdateListener);
        cSl.addChangeListener(sliderUpdateListener);

        // # Adding sliders #
        add(selectedComponent);
        add(distanceText);
        add(dSl);
        add(offsetText);
        add(oSl);
        add(spreadText);
        add(sSl);
        add(compactnessText);
        add(cSl);

        // # Adding buttons #
        Box buttonBox = Box.createVerticalBox();
        addAButton(redraw, buttonBox);
        addAButton(alignX, buttonBox);
        addAButton(alignY, buttonBox);
        add(buttonBox);

        // # Adding check boxes #
        add(autoPlacement);
        animatePlacement.setSelected(true);
        add(animatePlacement);

        // # Adding fix buttons

        fixNodes.addActionListener(e -> {
            graphPanel.graph().getNode(graphPanel.graph().selectedRoot()).setFixed(true);
            fixNodes.setEnabled(false);
            unfixNodes.setEnabled(true);
            graphPanel.repaint();
        });
        unfixNodes.addActionListener(e -> {
            graphPanel.graph().getNode(graphPanel.graph().selectedRoot()).setFixed(false);
            fixNodes.setEnabled(true);
            unfixNodes.setEnabled(false);
            graphPanel.repaint();
        });

        Box buttonFixBox = Box.createVerticalBox();
        addAButton(fixNodes, buttonFixBox);
        addAButton(unfixNodes, buttonFixBox);
        add(buttonFixBox);
    }

    private static void addAButton(JButton button, Container container)
    {
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setPreferredSize(new Dimension(200, 20));
        button.setMinimumSize(new Dimension(200, 20));
        container.add(button);
    }

    private JButton createButton(String text, Dimension size)
    {
        JButton button = new JButton(text);
        button.setPreferredSize(size);
        button.setMinimumSize(size);
        button.setMaximumSize(size);
        return button;
    }

    private Component createStrut()
    {
        JComponent component = (JComponent) Box.createVerticalStrut(5);
        component.setMinimumSize(new Dimension(0, 0));
        component.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
        return component;
    }

    public static LayoutSettingsPanel getLayoutSettingsPanel()
    {
        if (lsPanel == null) lsPanel = new LayoutSettingsPanel(Handler.gameGraphPanel);
        return lsPanel;
    }

    private void updateWeights()
    {
        lh.updateCompactness(getSliderValue(cSl));
        lh.updateDFSWeights(getSliderValue(oSl)/2.0,
                getSliderValue(dSl)/2.0,
                getSliderValue(sSl)/2.0);
    }

    public void updateSliderValues(double d, double o, double s)
    {
        changeListen = false;
        dSl.setValue((int)(d * 100));
        oSl.setValue((int)(o * 2 * 100));
        sSl.setValue((int)(s * 100));
        changeListen = true;
    }

    private double getSliderValue(JSlider slider) {return slider.getValue() / 100.0;}

    private void executeDFSLayout(IGraphPanel graphPanel)
    {
        graphPanel.getLayoutHandler().executeLayout();
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

    public boolean isAutoPlacementOn() {return autoPlacement.isSelected();}

    public boolean isAnimatePlacementOn() {return animatePlacement.isSelected();}

    public void enableFixButton() { fixNodes.setEnabled(true); }

    public void disableFixButton() { fixNodes.setEnabled(false); }

    public void enableUnfixButton() { unfixNodes.setEnabled(true); }

    public void disableUnfixButton() { unfixNodes.setEnabled(false); }

}
