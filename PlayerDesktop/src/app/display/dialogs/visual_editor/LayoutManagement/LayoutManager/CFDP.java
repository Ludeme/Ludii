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

public class CFDP extends FDP implements LayoutMethod
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

    private void subtreeIteration(int r, Vector2D fZone)
    {
        List<Integer> C = nodeList.get(r).getChildren();
        List<Integer> nodes = new ArrayList<>(C);
        nodes.add(r);

        // repulsive forces
        nodes.forEach(v -> {
            dispMap.put(v, new Vector2D(0, 0));
            nodes.forEach(u -> {
                calculateRepForce(v, u);
            });
        });

        // Calculate attractive forces
        for (int i = 0; i < C.size(); i++)
        {
            // f_a for vi and r
            calculateActForce(r, C.get(i));
            // f_a for vi and left sibling
            if (i > 0) calculateActForce(C.get(i), C.get(i-1));
            // f_a for vi and right sibling
            if (i < C.size()-1) calculateActForce(C.get(i), C.get(i+1));
        }

        // Apply force on root and leafs
        // Apply procedure for non-leaf nodes
        nodes.forEach(v -> {
            if (!fixedNodes.contains(v))
            {
                if (v != r)
                {
                    // TODO: consider individual zones as boundaries
                    applyForce(v, new Vector2D(W, H));
                    subtreeIteration(v, new Vector2D(0,0));
                }
            }
        });

    }

    protected void iteration()
    {
        subtreeIteration(1, new Vector2D(0,0));

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

    @Override
    protected HashMap<Integer, Vector2D> getDispMap() {
        return dispMap;
    }

    @Override
    protected iGraph getGraph() {
        return graph;
    }

    @Override
    protected double getK() {
        return k;
    }

    @Override
    protected double getCoolRate() {
        return coolRate;
    }

    @Override
    protected double getTemp() {
        return t;
    }
}
