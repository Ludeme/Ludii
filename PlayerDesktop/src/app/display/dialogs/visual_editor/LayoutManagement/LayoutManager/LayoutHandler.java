package app.display.dialogs.visual_editor.LayoutManagement.LayoutManager;

import app.display.dialogs.visual_editor.LayoutManagement.GraphRoutines;
import app.display.dialogs.visual_editor.LayoutManagement.LayoutConfigs;
import app.display.dialogs.visual_editor.LayoutManagement.Math.Vector2D;
import app.display.dialogs.visual_editor.model.interfaces.iGraph;
import app.display.dialogs.visual_editor.LayoutManagement.LayoutConfigs.*;
import game.rules.play.moves.nonDecision.effect.requirement.Do;

import javax.swing.*;

import java.util.Arrays;
import java.util.HashMap;

import static app.display.dialogs.visual_editor.LayoutManagement.GraphRoutines.updateNodeDepth;

/**
 * TODO: implement application to the trees with different roots
 * @author nic0gin
 */

public class LayoutHandler {

    private final iGraph graph;
    private int root;

    private LayoutMethod layout;

    public LayoutHandler(iGraph graph, int root)
    {
        this.graph = graph;
        this.root = root;
        layout = new DFSBoxDrawing(graph, root, 5, 4.0, 4.0);
    }

    public void setRoot(int root) {
        this.root = root;
    }

    // ################

    public void updateDFSWeights(double offset, double distance, double spread)
    {
        ((DFSBoxDrawing) layout).updateAllWeights(offset, distance, spread);
    }

    public void updateDFSWeights(HashMap<Integer, Double[]> weights)
    {
        ((DFSBoxDrawing) layout).updateAllWeights(weights);
    }

    public void evaluateGraphWeights()
    {
        HashMap<Integer, Double[]> weights = GraphRoutines.getSubtreeDOS(graph, root);
        updateDFSWeights(weights);
        if (LayoutConfigs.DEBUG)
        {
            System.out.println("Graph weights");
            weights.forEach((id, w) -> {
                System.out.println("Subtree #" + id + ": " + Arrays.toString(w));
            });
        }
    }

    // ################

    public void executeLayout()
    {
        // Prepare the graph

        // Calculate the depth for each node with respect with selected root
        // TODO implement int root into constructor
        int r = 1;
        updateNodeDepth(graph, r);

        layout.applyLayout();
    }

}
