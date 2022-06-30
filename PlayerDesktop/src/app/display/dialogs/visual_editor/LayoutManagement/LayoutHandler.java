package app.display.dialogs.visual_editor.LayoutManagement;

import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.model.interfaces.iGraph;
import app.display.dialogs.visual_editor.view.panels.IGraphPanel;
import app.display.dialogs.visual_editor.view.panels.editor.tabPanels.LayoutSettingsPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static app.display.dialogs.visual_editor.LayoutManagement.GraphRoutines.updateNodeDepth;

/**
 * @author nic0gin
 */

public class LayoutHandler {

    private final iGraph graph;
    private final DFSBoxDrawing layout;

    private static boolean layoutExecuted = false;

    public LayoutHandler(iGraph graph)
    {
        this.graph = graph;
        layout = new DFSBoxDrawing(graph, 4.0, 4.0);
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
        // TODO: improve functionality
        //double[] weights = GraphRoutines.treeDOS(graph, root);
        //Handler.lsPanel.updateSliderValues(weights[0], weights[1], weights[2]);
        //updateDFSWeights(weights);
    }

    // ################

    public void executeLayout()
    {
        if (GraphRoutines.updateCounter != 0) return;

        List<Integer> roots;
        if (graph.selectedRoot() == null)
        {
            roots = new ArrayList<>(graph.connectedComponentRoots());
        }
        else
        {
            roots = new ArrayList<>(graph.selectedRoot());
        }

        Vector2D translatePos = null;

        for (Integer root:
             roots)
        {
            // check if has children
            if (!graph.getNode(root).children().isEmpty())
            {
                updateNodeDepth(graph, root);
                layout.applyLayout(root);
            }
            else
            {
                graph.getNode(root).setOldPos(new Vector2D(graph.getNode(root).pos().x(), graph.getNode(root).pos().y()));
                graph.getNode(root).setNewPos(new Vector2D(graph.getNode(root).pos().x(), graph.getNode(root).pos().y()));
            }

            if (translatePos != null)
            {
                NodePlacementRoutines.translateByRoot(graph, root, translatePos);
            }
            Rectangle rect = GraphRoutines.getSubtreeArea(graph, root);
            translatePos = new Vector2D(NodePlacementRoutines.DEFAULT_X_POS,
                    rect.y+rect.height+NodePlacementRoutines.DEFAULT_Y_POS);

            // ####

            if (LayoutSettingsPanel.getLayoutSettingsPanel().isAnimatePlacementOn())
            {
                HashMap<Integer, Vector2D> incrementMap = GraphRoutines.computeNodeIncrements(graph, root);
                // animate change
                GraphRoutines.updateCounter = 0;
                Timer animationTimer = new Timer(3, e -> {
                    if (GraphRoutines.animateGraphNodes(graph, root, incrementMap))
                    {
                        ((Timer)e.getSource()).stop();
                        GraphRoutines.updateCounter = 0;
                    }
                });
                animationTimer.start();
            }
            else
            {
                Handler.editorPanel.syncNodePositions();
            }
        }

    }

    public void updateCompactness(double sliderValue)
    {
        layout.setCompactness(sliderValue);
    }

}
