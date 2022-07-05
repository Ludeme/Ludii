package app.display.dialogs.visual_editor.view.panels.editor.tabPanels;

import app.display.dialogs.visual_editor.LayoutManagement.DFBoxDrawing;
import app.display.dialogs.visual_editor.LayoutManagement.GraphRoutines;
import app.display.dialogs.visual_editor.LayoutManagement.LayoutHandler;
import app.display.dialogs.visual_editor.LayoutManagement.NodePlacementRoutines;
import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.view.panels.IGraphPanel;
import app.display.dialogs.visual_editor.view.panels.editor.EditorSidebar;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;

/**
 * Class for the single instance of the Layout Settings Panel
 * @author nic0gin
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


    private LayoutSettingsPanel()
    {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        selectedComponent = new JLabel("Selection: Empty");

        lh = Handler.currentGraphPanel.getLayoutHandler();

        oSl = new JSlider(0, (int) (100 * GraphRoutines.odsTuning()[0]));
        dSl = new JSlider(0, (int) (100 * GraphRoutines.odsTuning()[1]));
        sSl = new JSlider(0, (int) (100 * GraphRoutines.odsTuning()[2]));
        cSl = new JSlider(0, 100);

        updateSliderValues(DFBoxDrawing.defaultO(), DFBoxDrawing.defaultD(), DFBoxDrawing.defaultS());

        JLabel offsetText = new JLabel("Offset: " + getSliderValue(oSl));
        JLabel distanceText = new JLabel("Distance: " + Math.round(getSliderValue(dSl)/GraphRoutines.odsTuning()[1]*100)/100.0);
        JLabel spreadText = new JLabel("Spread: " + Math.round(getSliderValue(sSl)/GraphRoutines.odsTuning()[2]*100)/100.0);
        JLabel compactnessText = new JLabel("Compactness: " + getSliderValue(cSl));

        Dimension buttonDim = new Dimension(175, 20);
        JButton redraw = createButton("Arrange graph", buttonDim);
        JButton alignX = createButton("Align vertically", buttonDim);
        JButton alignY = createButton("Align horizontally", buttonDim);

        fixNodes = createButton("Group subtree", buttonDim);
        fixNodes.setEnabled(false);
        unfixNodes = createButton("Ungroup subtree", buttonDim);
        unfixNodes.setEnabled(false);

        redraw.addActionListener(e -> {
            lh.evaluateGraphWeights();
            executeDFSLayout(Handler.currentGraphPanel);
            Handler.currentGraphPanel.deselectEverything();
        });

        alignX.addActionListener(e -> {
            NodePlacementRoutines.alignNodes(Handler.currentGraphPanel.selectedNodes(), NodePlacementRoutines.X_AXIS, Handler.currentGraphPanel);
            // graphPanel.updateNodePositions();
            // graphPanel.deselectEverything();
        });

        alignY.addActionListener(e -> {
            NodePlacementRoutines.alignNodes(Handler.currentGraphPanel.selectedNodes(), NodePlacementRoutines.Y_AXIS, Handler.currentGraphPanel);
            // graphPanel.updateNodePositions();
            // graphPanel.deselectEverything();
        });

        ChangeListener sliderUpdateListener = e -> {
            offsetText.setText("Offset: " + getSliderValue(oSl));
            distanceText.setText("Distance: " + getSliderValue(dSl));
            spreadText.setText("Spread: " + getSliderValue(sSl));
            compactnessText.setText("Compactness: " + getSliderValue(cSl));
            if (changeListen)
            {
                updateWeights();
                executeDFSLayout(Handler.currentGraphPanel);
            }
        };

        dSl.addChangeListener(sliderUpdateListener);
        oSl.addChangeListener(sliderUpdateListener);
        sSl.addChangeListener(sliderUpdateListener);
        cSl.addChangeListener(sliderUpdateListener);

        add(selectedComponent);

        // # Adding sliders #

        Box sliderBox = Box.createVerticalBox();

        sliderBox.add(offsetText);
        sliderBox.add(oSl);
        sliderBox.add(distanceText);
        sliderBox.add(dSl);
        sliderBox.add(spreadText);
        sliderBox.add(sSl);
        sliderBox.add(compactnessText);
        sliderBox.add(cSl);

        add(sliderBox);

        // # Adding buttons #
        Box buttonBox = Box.createVerticalBox();
        addAButton(redraw, buttonBox);
        addAButton(alignX, buttonBox);
        addAButton(alignY, buttonBox);
        add(buttonBox);

        // # Adding check boxes #
        autoPlacement.addChangeListener(e -> Handler.autoplacement = ((JCheckBox) e.getSource()).isSelected());
        autoPlacement.setSelected(Handler.autoplacement);
        //add(autoPlacement);

        animatePlacement.addChangeListener(e -> Handler.animation = ((JCheckBox) e.getSource()).isSelected());
        animatePlacement.setSelected(Handler.animation);
        //add(animatePlacement);

        // # Adding fix buttons

        fixNodes.addActionListener(e -> {
            Handler.currentGraphPanel.graph().getNode(Handler.currentGraphPanel.graph().selectedRoot()).setFixed(true);
            fixNodes.setEnabled(false);
            unfixNodes.setEnabled(true);
            Handler.currentGraphPanel.repaint();
        });
        unfixNodes.addActionListener(e -> {
            Handler.currentGraphPanel.graph().getNode(Handler.currentGraphPanel.graph().selectedRoot()).setFixed(false);
            fixNodes.setEnabled(true);
            unfixNodes.setEnabled(false);
            Handler.currentGraphPanel.repaint();
        });

        Box buttonFixBox = Box.createVerticalBox();
        addAButton(fixNodes, buttonFixBox);
        addAButton(unfixNodes, buttonFixBox);
        add(buttonFixBox);

        JButton closeLayoutTab = createButton("Close Layout Settings", buttonDim);
        closeLayoutTab.addActionListener(e -> EditorSidebar.getEditorSidebar().setSidebarVisible(false));
        Box closeBox = Box.createVerticalBox();
        addAButton(closeLayoutTab, closeBox);
        add(closeBox);
    }

    private Box getSplitterBox()
    {
        Box splitterBox = Box.createHorizontalBox();
        splitterBox.add(new JSeparator());
        return splitterBox;
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
        if (lsPanel == null) lsPanel = new LayoutSettingsPanel();
        return lsPanel;
    }

    private void updateWeights()
    {
        lh.updateCompactness(getSliderValue(cSl));
        lh.updateDFSWeights(getSliderValue(oSl),
                getSliderValue(dSl),
                getSliderValue(sSl));
    }

    public void updateSliderValues(double o, double d, double s)
    {
        changeListen = false;
        oSl.setValue((int)(o * 100));
        dSl.setValue((int)(d * 100));
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

    public static void getSettingsFrame()
    {
        JFrame frame = new JFrame("Layout Settings");
        frame.setSize(300, 400);
        frame.add(new LayoutSettingsPanel());
        frame.setVisible(true);
        frame.setResizable(false);
    }

    public void enableFixButton() { fixNodes.setEnabled(true); }

    public void disableFixButton() { fixNodes.setEnabled(false); }

    public void enableUnfixButton() { unfixNodes.setEnabled(true); }

    public void disableUnfixButton() { unfixNodes.setEnabled(false); }

    public JCheckBox animatePlacement() {return animatePlacement;}

    public JCheckBox autoPlacement() {return autoPlacement;}
}
