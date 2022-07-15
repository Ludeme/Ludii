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
 	private static final long serialVersionUID = 1L;

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
        cSl.setValue(100);

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

        redraw.addActionListener(lh.getEvaluateAndArrange());

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

    /**
     * Adds a JButton of specified size to container
     * @param button
     * @param container
     */
    private static void addAButton(JButton button, Container container)
    {
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setPreferredSize(new Dimension(200, 20));
        button.setMinimumSize(new Dimension(200, 20));
        container.add(button);
    }

    /**
     * Creates a JButton with specified text and size
     * @param text
     * @param size
     * @return
     */
    private JButton createButton(String text, Dimension size)
    {
        JButton button = new JButton(text);
        button.setPreferredSize(size);
        button.setMinimumSize(size);
        button.setMaximumSize(size);
        return button;
    }

    /**
     * Singleton getter method for layout settings panel
     * @return
     */
    public static LayoutSettingsPanel getLayoutSettingsPanel()
    {
        if (lsPanel == null) lsPanel = new LayoutSettingsPanel();
        return lsPanel;
    }

    /**
     * Updates layout metrics values in LayoutHander according to slider values
     */
    private void updateWeights()
    {
        lh.updateCompactness(getSliderValue(cSl));
        lh.updateDFSWeights(getSliderValue(oSl),
                getSliderValue(dSl),
                getSliderValue(sSl));
    }

    /**
     * Update slider values for layout metrics
     * @param o
     * @param d
     * @param s
     */
    public void updateSliderValues(double o, double d, double s)
    {
        changeListen = false;
        oSl.setValue((int)(o * 100));
        dSl.setValue((int)(d * 100));
        sSl.setValue((int)(s * 100));
        changeListen = true;
    }

    /**
     * Get slider value for arrangement
     * @param slider
     * @return
     */
    private double getSliderValue(JSlider slider) {return slider.getValue() / 100.0;}

    /**
     * Executes arrangement procedure of graph in a specified panel
     * @param graphPanel
     */
    private void executeDFSLayout(IGraphPanel graphPanel)
    {
        graphPanel.getLayoutHandler().executeLayout();
    }

    /**
     * Display name of selected ludeme node
     * @param node
     * @param subtree
     */
    public void setSelectedComponent(String node, boolean subtree)
    {
        if (subtree) selectedComponent.setText("Selected subtree of: "+node);
        else selectedComponent.setText("Selected: "+node);
    }

    public void enableFixButton() { fixNodes.setEnabled(true); }

    public void disableFixButton() { fixNodes.setEnabled(false); }

    public void enableUnfixButton() { unfixNodes.setEnabled(true); }

    public void disableUnfixButton() { unfixNodes.setEnabled(false); }

    public JCheckBox animatePlacement() {return animatePlacement;}

    public JCheckBox autoPlacement() {return autoPlacement;}

}
