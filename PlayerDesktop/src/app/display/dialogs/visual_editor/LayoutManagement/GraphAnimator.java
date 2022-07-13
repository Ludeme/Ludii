package app.display.dialogs.visual_editor.LayoutManagement;

import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.model.interfaces.iGNode;
import app.display.dialogs.visual_editor.model.interfaces.iGraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Provides functionality for smooth animation between changes of graph layouts
 * @author nic0gin
 */
public class GraphAnimator
{

    /**
     * Animation updates
     */
    private final int ANIMATION_UPDATES = 25;

    /**
     * Animation update counter
     */
    private int updateCounter = 0;

    private static GraphAnimator graphAnimator;

    private final HashMap<iGNode, Vector2D> nodeInitPositions;

    private final HashMap<iGNode, Vector2D> nodeFinalPosition;

    private final HashMap<iGNode, Vector2D> nodePosIncrements;

    private final List<iGNode> nodesToProcess;

    /**
     * Constructor
     */
    private GraphAnimator()
    {
        nodeInitPositions = new HashMap<>();
        nodeFinalPosition = new HashMap<>();
        nodePosIncrements = new HashMap<>();
        nodesToProcess = new ArrayList<>();
    }

    /**
     * Returns single instance of GraphAnimator
     * @return
     */
    public static GraphAnimator getGraphAnimator()
    {
        if (graphAnimator == null) graphAnimator = new GraphAnimator();
        return graphAnimator;
    }

    /**
     * Animates smooth transition between previous nodes positions and new positions
     * @return return true if finished animation
     */
    public boolean animateGraphNodes()
    {
        if (updateCounter == 0)
        {
            updateToInitPositions();
        }

        nodePosIncrements.forEach((k,v) -> k.setPos(new Vector2D(nodeInitPositions.get(k).x()+v.x()*updateCounter,
                nodeInitPositions.get(k).y()+v.y()*updateCounter)));
        updateCounter++;
        Handler.currentGraphPanel.syncNodePositions();

        if (updateCounter == ANIMATION_UPDATES)
        {
            updateCounter = 0;
            return true;
        }
        return false;
    }

    public void clearPositionHistory()
    {
        nodeInitPositions.clear();
        nodeFinalPosition.clear();
        nodePosIncrements.clear();
    }

    /**
     * Sets nodes to their previous positions and calculates increments for further animation
     */
    public void updateToInitPositions()
    {
        nodeFinalPosition.forEach((k,v) -> {
            // compute node increments
            double incX = (v.x() - nodeInitPositions.get(k).x()) / (ANIMATION_UPDATES-1);
            double incY = (v.y() - nodeInitPositions.get(k).y()) / (ANIMATION_UPDATES-1);
            nodePosIncrements.put(k, new Vector2D(incX, incY));
            // set node positions to initial
            k.setPos(nodeInitPositions.get(k));
        });
        Handler.currentGraphPanel.syncNodePositions();
    }

    /**
     * Sets nodes to their new position
     */
    public void updateToFinalPositions()
    {
        nodeFinalPosition.forEach(iGNode::setPos);
        Handler.currentGraphPanel.syncNodePositions();
    }

    /**
     * Preserves initial/previous positions of nodes of a subtree that starts with provided root
     * @param graph
     * @param root
     */
    public void preserveInitPositions(iGraph graph, int root)
    {
        List<Integer> Q = new ArrayList<>();
        Q.add(root);
        while (!Q.isEmpty())
        {
            int nId = Q.remove(0);
            iGNode n = graph.getNode(nId);
            nodesToProcess.add(n);
            nodeInitPositions.put(n, n.pos());
            Q.addAll(n.children());
        }
    }

    /**
     * Preserves initial/previous positions of a given list of nodes
     * @param nodes
     */
    public void preserveInitPositions(List<iGNode> nodes)
    {
        nodes.forEach(n -> {
            nodesToProcess.add(n);
            nodeInitPositions.put(n, n.pos());
        });
    }

    /**
     * Preserving final positions of nodes whose initial positions were already preserved
     */
    public void preserveFinalPositions()
    {
        nodesToProcess.forEach(n -> nodeFinalPosition.put(n, n.pos()));
        nodesToProcess.clear();
    }

    public HashMap<iGNode, Vector2D> nodeFinalPosition()
    {
        return nodeFinalPosition;
    }

    public HashMap<iGNode, Vector2D> nodeInitPositions()
    {
        return nodeInitPositions;
    }

    public int updateCounter()
    {
        return updateCounter;
    }
}
