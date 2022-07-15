package app.display.dialogs.visual_editor.LayoutManagement;

import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.model.interfaces.iGNode;
import app.display.dialogs.visual_editor.model.interfaces.iGraph;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class NodePlacementRoutines
{

    public static final int X_AXIS = 0;

    public static final int Y_AXIS = 1;

    private static final int NODE_GAP = 50;

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
        Q.add(r);
        while (!Q.isEmpty())
        {
            int nid = Q.remove(0);
            iGNode n = graph.getNode(nid);
            n.setPos(n.pos().sub(t));
            n.setNewPos(n.pos());
            Q.addAll(n.children());
        }
    }

    public static void alignNodes(List<iGNode> nodes, int axis, boolean animate)
    {
        if (nodes.isEmpty()) return;
        // find min posX and posY in a list
        double posX = nodes.get(0).pos().x();
        double posY = nodes.get(0).pos().y();
        nodes.get(0).setOldPos(nodes.get(0).pos());
        nodes.get(0).setNewPos(nodes.get(0).pos());
        for (iGNode n:
             nodes) {
            if (n.pos().x() < posX) posX = n.pos().x();
            if (n.pos().y() < posY) posY = n.pos().y();
        }
        if (axis == X_AXIS)
        {
            for (int i = 1; i < nodes.size(); i++)
            {
                nodes.get(i).setOldPos(nodes.get(i).pos());
                nodes.get(i).setPos(new Vector2D(nodes.get(i-1).pos().x()+nodes.get(i-1).width()+NODE_GAP*i, posY));
                nodes.get(i).setNewPos(nodes.get(i).pos());
            }
        }
        else if (axis == Y_AXIS)
        {
            for (int i = 1; i < nodes.size(); i++)
            {
                nodes.get(i).setOldPos(nodes.get(i).pos());
                nodes.get(i).setPos(new Vector2D(posX, nodes.get(i-1).pos().y()+nodes.get(i-1).height()+NODE_GAP*i));
                nodes.get(i).setNewPos(nodes.get(i).pos());
            }
        }

        if (animate)
        {
            HashMap<Integer, Vector2D> incrementMap = GraphRoutines.computeNodeIncrements(nodes);
            // animate change
            GraphRoutines.updateCounter = 0;
            Timer animationTimer = new Timer(3, e -> {
                if (GraphRoutines.animateGraphNodes(nodes, incrementMap))
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

}
