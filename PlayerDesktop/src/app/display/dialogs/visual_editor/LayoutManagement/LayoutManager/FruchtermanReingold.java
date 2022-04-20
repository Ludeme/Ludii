package app.display.dialogs.visual_editor.LayoutManagement.LayoutManager;

import app.display.dialogs.visual_editor.LayoutManagement.Math.Vector2D;
import app.display.dialogs.visual_editor.model.Edge;
import app.display.dialogs.visual_editor.model.interfaces.iGNode;
import app.display.dialogs.visual_editor.model.interfaces.iGraph;

import java.util.HashMap;
import java.util.List;

import static java.lang.Math.*;

public class FruchtermanReingold implements LayoutMethod
{

    // Force-directed drawing
    // Fruchterman-Reingold algorithm
    private final double W;
    private final double H;
    private final double coolRate;
    private final double C;
    private final double k;
    private double t;

    private HashMap<Integer, iGNode> nodeList;
    private HashMap<Integer, Vector2D> dispMap;
    private List<Edge> edgeList;
    private iGraph graph;

    private double repForce(double x) {
        return (k*k)/x;
    }

    private double actForce(double x) {
        return (x*x)/k;
    }

    private double cool(double x) {
        return max(x*(1-coolRate), 0.1);
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

        k = C*sqrt((W*H)/nodeList.size());
        t = W/10;
    }

    public void FruchReinIteration()
    {
        nodeList.forEach((iv, v)-> {
            dispMap.put(iv, new Vector2D(0, 0));
            nodeList.forEach((iu, u)-> {
                if (v.getId() != u.getId())
                {
                    Vector2D delta = v.getPos().sub(u.getPos());
                    dispMap.put(iv, dispMap.get(iv).add( (delta.normalize()).mult(repForce(delta.euclideanNorm()))) );
                }
            });
        });

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

        nodeList.forEach((id, v)->{
            Vector2D tempPos = new Vector2D(v.getPos().getX(), v.getPos().getY());

            Vector2D dispNorm = dispMap.get(id).normalize().mult(min(dispMap.get(id).euclideanNorm(), t));

            v.setPos(v.getPos().add(dispNorm));
            double x = min(W/2, max(-W/2, v.getPos().getX()));
            double y = min(H/2, max(-H/2, v.getPos().getY()));

            if (Double.isNaN(x) || Double.isNaN(y))
            {
                v.setPos(tempPos);
            }
            else
            {
                v.setPos(new Vector2D(x, y));
            }
            System.out.println(x + " " + y);
        });

        t = cool(t);
    }

    public void incrementT(double incrementor) {
        this.t += incrementor;
    }

    @Override
    public void applyLayout()
    {

        // Set up variables (only once)

        // Execute algorithm iteration
        for (int i = 0; i < 500; i++)
        {
            FruchReinIteration();
        }


    }

}
