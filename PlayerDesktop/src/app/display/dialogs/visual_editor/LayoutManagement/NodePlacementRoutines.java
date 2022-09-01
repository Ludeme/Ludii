package app.display.dialogs.visual_editor.LayoutManagement;

import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.model.interfaces.iGNode;
import app.display.dialogs.visual_editor.model.interfaces.iGraph;
import app.display.dialogs.visual_editor.view.panels.IGraphPanel;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Provide methods for node placement
 * @author nic0gin
 */
public final class NodePlacementRoutines
{

    public static final int X_AXIS = 0;

    public static final int Y_AXIS = 1;

    private static final int NODE_GAP = 25;

    public static final int DEFAULT_X_POS = 20;

    public static final int DEFAULT_Y_POS = 20;

    /**
     * Translate node placement by oPos vector with respect to root
     * @param r root node id
     * @param oPos original root position
     * @param graph graph in operation
     */
	public static void translateByRoot(iGraph graph, int r, Vector2D oPos)
    {
        Vector2D t = graph.getNode(r).pos().sub(oPos);
        // iterate through descendants of a root node
        List<Integer> Q = new ArrayList<>();
        Q.add(Integer.valueOf(r));
        while (!Q.isEmpty())
        {
            int nid = Q.remove(0).intValue();
            iGNode n = graph.getNode(nid);
            if (!n.collapsed())
            {
                n.setPos(n.pos().sub(t));
                Q.addAll(n.children());
            }
        }
    }

    public static void alignNodes(List<iGNode> nodes, int axis, IGraphPanel graphPanel)
    {
        if (nodes.isEmpty() || ( Handler.animation &&
                GraphAnimator.getGraphAnimator().updateCounter() != 0)) return;

        // preserve initial node positions
        if (Handler.animation) GraphAnimator.getGraphAnimator().preserveInitPositions(nodes);

        // find min posX and posY in a list
        double posX = nodes.get(0).pos().x();
        double posY = nodes.get(0).pos().y();
        for (iGNode n:
             nodes) {
            if (n.pos().x() < posX) posX = n.pos().x();
            if (n.pos().y() < posY) posY = n.pos().y();
        }
        if (axis == X_AXIS)
        {
            for (int i = 1; i < nodes.size(); i++)
            {
                nodes.get(i).setPos(new Vector2D(nodes.get(i-1).pos().x()+nodes.get(i-1).width()+NODE_GAP, posY));
            }
        }
        else if (axis == Y_AXIS)
        {
            for (int i = 1; i < nodes.size(); i++)
            {
                nodes.get(i).setPos(new Vector2D(posX, nodes.get(i-1).pos().y()+nodes.get(i-1).height()+NODE_GAP));
            }
        }

        // preserve final
        if (Handler.animation) GraphAnimator.getGraphAnimator().preserveFinalPositions();

        if (Handler.animation && GraphAnimator.getGraphAnimator().updateCounter() == 0)
        {
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
            graphPanel.syncNodePositions();
        }
    }

}
