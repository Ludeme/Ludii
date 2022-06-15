package app.display.dialogs.visual_editor.LayoutManagement.LayoutManager;

import app.display.dialogs.visual_editor.LayoutManagement.GraphRoutines;
import app.display.dialogs.visual_editor.LayoutManagement.LayoutConfigs;
import app.display.dialogs.visual_editor.model.interfaces.iGraph;
import app.display.dialogs.visual_editor.view.panels.IGraphPanel;

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

    public static void applyOnPanel(IGraphPanel graphPanel)
    {
        LayoutHandler lm = graphPanel.getLayoutHandler();
        lm.evaluateGraphWeights();
        lm.executeLayout(graphPanel.graph().getRoot().id());
        graphPanel.drawGraph(graphPanel.graph());
    }

    public void executeLayout(int root)
    {
        updateNodeDepth(graph, graph.getRoot().id());
        layout.setRoot(root);
        layout.applyLayout();
    }

}
