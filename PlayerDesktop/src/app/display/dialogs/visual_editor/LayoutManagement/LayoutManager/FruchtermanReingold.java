package app.display.dialogs.visual_editor.LayoutManagement.LayoutManager;

import app.display.dialogs.visual_editor.LayoutManagement.Math.Vector2D;
import app.display.dialogs.visual_editor.model.Edge;
import app.display.dialogs.visual_editor.model.interfaces.iGNode;
import app.display.dialogs.visual_editor.model.interfaces.iGraph;

import javax.swing.*;
import java.util.HashMap;
import java.util.List;

import static java.lang.Math.*;

/**
 * Force-directed drawing
 * Fruchterman-Reingold algorithm
 *
 * @author nic0gin
 */
public class FruchtermanReingold extends FDP implements LayoutMethod
{

    private final double W;
    private final double H;
    private final double coolRate;
    private final double C;
    private final double k;
    private double t;

    private Timer timer;

    private HashMap<Integer, iGNode> nodeList;
    private HashMap<Integer, Vector2D> dispMap;
    private List<Edge> edgeList;
    private iGraph graph;

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

    public FruchtermanReingold(iGraph graph, double C, double coolRate, Vector2D boundaries)
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
    }

    protected void iteration()
    {
        nodeList.forEach((iv, v)-> {
            dispMap.put(iv, new Vector2D(0, 0));
            nodeList.forEach((iu, u)-> {
                calculateRepForce(iv, iu);
            });
        });

        edgeList.forEach((e)->{
            int aId = e.getNodeA();
            int bId = e.getNodeB();
            calculateActForce(aId, bId);
        });

        nodeList.forEach((id, v)->{
            applyForce(id, new Vector2D(W, H));
        });

        t = cool(t);
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
