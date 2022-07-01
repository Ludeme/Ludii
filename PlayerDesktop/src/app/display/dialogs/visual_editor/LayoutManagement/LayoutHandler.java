package app.display.dialogs.visual_editor.LayoutManagement;

import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.model.interfaces.iGNode;
import app.display.dialogs.visual_editor.model.interfaces.iGraph;
import app.display.dialogs.visual_editor.view.panels.IGraphPanel;
import app.display.dialogs.visual_editor.view.panels.editor.tabPanels.LayoutSettingsPanel;
import game.functions.ints.state.What;

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

public class LayoutHandler
{

    private final iGraph graph;
    private final DFSBoxDrawing layout;

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
        if (GraphAnimator.getGraphAnimator().updateCounter() != 0) return;
        boolean animate = LayoutSettingsPanel.getLayoutSettingsPanel().isAnimatePlacementOn();

        // determine graph components to be updated
        List<Integer> roots;
        if (graph.selectedRoot() == -1)
        {
            roots = new ArrayList<>(graph.connectedComponentRoots());
        }
        else
        {
            roots = new ArrayList<>();
            roots.add(graph.selectedRoot());
        }



        // update graph components
        Vector2D transPos = null;
        for (int i = 0; i < roots.size(); i++)
        {
            int root = roots.get(i);

            // translation position
            if (i > 0)
            {
                Rectangle rect = GraphRoutines.getSubtreeArea(graph, roots.get(i-1));
                transPos = new Vector2D(NodePlacementRoutines.DEFAULT_X_POS,
                        rect.y+rect.height+NodePlacementRoutines.DEFAULT_Y_POS);
            }

            // check if has children
            if (!graph.getNode(root).children().isEmpty())
            {
                // preserve initial positions of subtree
                if (animate) GraphAnimator.getGraphAnimator().preserveInitPositions(graph, root);
                // update depth of subtree
                updateNodeDepth(graph, root);
                // rearrange subtree layout with standard procedure
                layout.applyLayout(root);
                if (i > 0) NodePlacementRoutines.translateByRoot(graph, root, transPos);
                // preserve final position
                if (animate) GraphAnimator.getGraphAnimator().preserveFinalPositions();
            }
            else
            {
                iGNode rNode = graph.getNode(root);
                // preserve initial position of single node
                if (animate) GraphAnimator.getGraphAnimator().nodeInitPositions().put(rNode, rNode.pos());
                if (i > 0) NodePlacementRoutines.translateByRoot(graph, root, transPos);
                // preserve final position of single node
                if (animate) GraphAnimator.getGraphAnimator().nodeFinalPosition().put(rNode, rNode.pos());
            }
        }



        // display updated positions either through animation or single jump
        if (animate)
        {
            // animate change
            Timer animationTimer = new Timer(3, e -> {
                if (GraphAnimator.getGraphAnimator().animateGraphNodes())
                {
                    ((Timer)e.getSource()).stop();
                }
            });
            animationTimer.start();
        }
        else
        {
            Handler.editorPanel.syncNodePositions();
        }

    }

    public void updateCompactness(double sliderValue)
    {
        layout.setCompactness(sliderValue);
    }

}
