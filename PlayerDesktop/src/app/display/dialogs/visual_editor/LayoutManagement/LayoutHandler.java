package app.display.dialogs.visual_editor.LayoutManagement;

import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.model.interfaces.iGraph;
import app.display.dialogs.visual_editor.view.panels.IGraphPanel;
import app.display.dialogs.visual_editor.view.panels.editor.tabPanels.LayoutSettingsPanel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import static app.display.dialogs.visual_editor.LayoutManagement.GraphRoutines.updateNodeDepth;

/**
 * @author nic0gin
 */

public class LayoutHandler {

    private final iGraph graph;
    private int root;
    private final DFSBoxDrawing layout;

    public static Timer animationTimer;

    private static boolean layoutExecuted = false;

    public LayoutHandler(iGraph graph, int root)
    {
        this.graph = graph;
        this.root = root;
        layout = new DFSBoxDrawing(graph, root, 4.0, 4.0);
    }

    public void setRoot(int root) {
        this.root = root;
    }

    // ################

    public void updateDFSWeights(double offset, double distance, double spread)
    {
        layout.updateWeights(offset, distance, spread);
    }

    public void updateDFSWeights(double[] weights)
    {
        layout.updateAllWeights(weights);
    }

    public void evaluateGraphWeights()
    {
        double[] weights = GraphRoutines.treeDOS(graph, root);
        Handler.lsPanel.updateSliderValues(weights[0], weights[1], weights[2]);
        updateDFSWeights(weights);
    }

    // ################

    public static void applyOnPanel(IGraphPanel graphPanel)
    {
        LayoutHandler lm = graphPanel.getLayoutHandler();
        lm.evaluateGraphWeights();
        lm.executeLayout(graphPanel.graph().getRoot().id(), false);
        layoutExecuted = true;


    }

    public void executeLayout(int root, boolean animated)
    {
        // check if has children
        if (graph.getNode(root).children().isEmpty()) return;
        updateNodeDepth(graph, graph.getRoot().id());
        layout.setRoot(root);
        layout.applyLayout();
        if (animated)
        {
            HashMap<Integer, Vector2D> incrementMap = GraphRoutines.computeNodeIncrements(graph, root);
            // animate change
            GraphRoutines.updateCounter = 0;
            animationTimer = new Timer(3, e -> {
                if (GraphRoutines.animateGraphNodes(graph, root, incrementMap))
                {
                    ((Timer)e.getSource()).stop();
                }
            });
            animationTimer.start();
        }
        else
        {
            Handler.updateNodePositions();
        }
    }

    public void updateCompactness(double sliderValue)
    {
        layout.setCompactness(sliderValue);
    }

}
