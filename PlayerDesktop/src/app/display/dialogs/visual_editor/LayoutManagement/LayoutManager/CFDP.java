package app.display.dialogs.visual_editor.LayoutManagement.LayoutManager;

import app.display.dialogs.visual_editor.LayoutManagement.Math.Vector2D;
import app.display.dialogs.visual_editor.model.Edge;
import app.display.dialogs.visual_editor.model.interfaces.iGNode;
import app.display.dialogs.visual_editor.model.interfaces.iGraph;

import javax.swing.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static java.lang.Math.*;

/**
 * Constraint Force-Directed Placement
 * Based on Fruchterman-Reingold
 * @author nic0gin
 */

public class CFDP implements LayoutMethod
{

    // Constraint Force-directed drawing
    // Based on Fruchterman-Reingold algorithm

    // Incorporates the following constraints
    // 1. Zoning
    // 2. Fixing positions
    // 3. User-constraints: a) preserving relative positions and b) preserving distance by force

    // Further, enhance the system with automatic placement, based on previous interactions

    private final double W;
    private final double H;
    private final double coolRate;
    private final double C;
    private final double k;
    private double t;

    private Timer timer;

    private final HashMap<Integer, iGNode> nodeList;
    private final HashMap<Integer, Vector2D> dispMap;

    // Set of nodes which positions can't be changed
    private final List<Integer> fixedNodes;

    private final List<Edge> edgeList;
    private final iGraph graph;

    private double repForce(double x) {
        return (k*k)/x;
    }

    private double actForce(double x) {
        return (x*x)/k;
    }

    private double cool(double x) {
        return x*(1-coolRate);
    }

    public CFDP(iGraph graph, double C, double coolRate, Vector2D boundaries)
    {
        this.graph = graph;
        this.dispMap = new HashMap<>();

        this.C = C;
        this.coolRate = coolRate;

        W = boundaries.getX();
        H = boundaries.getY();

        nodeList = graph.getNodeList();
        edgeList = graph.getEdgeList();

        //k = C*sqrt((W*H)/nodeList.size());
        k = 200;
        t = W/10;

        fixedNodes = new ArrayList<>();
        // fix root node
        fixedNodes.add(1);
    }

    private void createZones()
    {

    }

    private void newIteration(int r, Vector2D fZone)
    {
        List<Integer> C = nodeList.get(r).getChildren();
        List<Integer> nodes = new ArrayList<>(C);
        nodes.add(r);

        // repulsive forces
        nodes.forEach(v -> {
            dispMap.put(v, new Vector2D(0, 0));
            iGNode vN = graph.getNode(v);

            nodes.forEach(u -> {
                if (!v.equals(u))
                {
                    iGNode uN = graph.getNode(u);
                    Vector2D delta = vN.getPos().sub(uN.getPos());
                    Vector2D repF = (delta.normalize()).mult(repForce(delta.euclideanNorm()));

                    // Add vertical alignment force (C1)
                    Vector2D totalF = repF.add(new Vector2D(vN.getWidth()/2.0 + uN.getWidth()/2.0, 0));

                    dispMap.put(v, dispMap.get(v).add(totalF));
                }
            });
        });

        for (int i = 0; i < C.size(); i++)
        {
            // f_a for vi and r
            // f_a for vi and left sibling
            // f_a for vi and right sibling
        }

        C.forEach((id) -> {
            // 1. Calculate repulsive between all vertices
            // 2. Calculate attractive forces between edge pair + close siblings
            // 3. If node has children:
            //      add force to fZone
            //    else:
            //      apply total force
        });
    }

    private void iteration()
    {
        // repulsive forces
        nodeList.forEach((iv, v)-> {
            dispMap.put(iv, new Vector2D(0, 0));
            nodeList.forEach((iu, u)-> {
                if (v.getId() != u.getId())
                {
                    Vector2D delta = v.getPos().sub(u.getPos());
                    Vector2D repF = (delta.normalize()).mult(repForce(delta.euclideanNorm()));

                    // Add vertical alignment force (C1)
                    Vector2D totalF = repF.add(new Vector2D(v.getWidth(), 0));

                    dispMap.put(iv, dispMap.get(iv).add(totalF));
                }
            });
        });

        // attractive forces
        edgeList.forEach((e)->{
            int aId = e.getNodeA();
            int bId = e.getNodeB();
            Vector2D delta = graph.getNode(aId).getPos().sub(graph.getNode(bId).getPos());

            dispMap.put(aId, dispMap.get(aId).sub(
                    delta.normalize().mult(actForce(delta.euclideanNorm()))
            ));

            dispMap.put(bId, dispMap.get(bId).add(
                    delta.normalize().mult(actForce(delta.euclideanNorm()))
            ));

        });

        // apply forces
        nodeList.forEach((id, v)->{
            // Apply forces to all non-fixed nodes
            if (!fixedNodes.contains(id))
            {
                Vector2D tempPos = new Vector2D(v.getPos().getX(), v.getPos().getY());

                Vector2D dispNorm = dispMap.get(id).normalize().mult(min(dispMap.get(id).euclideanNorm(), t));

                v.setPos(v.getPos().add(dispNorm));

                // Global boundaries
                double x = min(W/2, max(-W/2, v.getPos().getX()));
                double y = min(H/2, max(-H/2, v.getPos().getY()));

                System.out.println(nodeList.get(nodeList.get(id).getParent()).getPos().getX());
                System.out.println(x);

                if (Double.isNaN(x) || Double.isNaN(y))
                {
                    v.setPos(tempPos);
                }
                else if (x < nodeList.get(nodeList.get(id).getParent()).getPos().getX())
                {
                    v.setPos(new Vector2D(tempPos.getX(), y));
                }
                else
                {
                    v.setPos(new Vector2D(x, y));
                }
            }
        });

        t = cool(t);
    }

    public void fixVertex(int id)
    {
        if (!fixedNodes.contains(id)) fixedNodes.add(id);
    }

    public void releaseVertex(int id)
    {
        fixedNodes.remove(id);
    }

    public void incrementT(double incrementor) {
        this.t += incrementor;
    }

    public void setTimer(Timer timer) {
        this.timer = timer;
    }

    @Override
    public void applyLayout()
    {
        if (timer == null)
        {
            for (int i = 0; i < 500; i++)
                iteration();
        }
        else
        {
            iteration();
            if (pow((t-0.1),2) <= 1E-2) timer.stop();
        }
    }

}
