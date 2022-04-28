package app.display.dialogs.visual_editor.LayoutManagement.LayoutManager;

import app.display.dialogs.visual_editor.LayoutManagement.Math.Vector2D;
import app.display.dialogs.visual_editor.model.interfaces.iGNode;
import app.display.dialogs.visual_editor.model.interfaces.iGraph;

import java.util.HashMap;

import static java.lang.Math.*;

public abstract class FDP
{

    protected double repForce(double x) {
        return (pow(getK(),2))/x;
    }

    protected double actForce(double x) {
        return (x*x)/getK();
    }

    protected double logForce(double x) {return 2.0*log(x);}

    protected double invForce(double x) {return 1.0/(x*x);}

    protected double cool(double x) {
        return x*(1.0-getCoolRate());
    }

    protected void calculateActForce(int aId, int bId)
    {
        iGraph graph = getGraph();
        HashMap<Integer, Vector2D> dispMap = getDispMap();

        Vector2D delta = graph.getNode(aId).getPos().sub(graph.getNode(bId).getPos());

        dispMap.put(aId, dispMap.get(aId).sub(
                delta.normalize().mult(actForce(delta.euclideanNorm()))
        ));

        dispMap.put(bId, dispMap.get(bId).add(
                delta.normalize().mult(actForce(delta.euclideanNorm()))
        ));
    }

    protected void calculateRepForce(int v, int u)
    {
        if (v != u)
        {
            iGraph graph = getGraph();
            HashMap<Integer, Vector2D> dispMap = getDispMap();

            iGNode vN = graph.getNode(v);
            iGNode uN = graph.getNode(u);

            Vector2D delta = vN.getPos().sub(uN.getPos());
            Vector2D repF = (delta.normalize()).mult(repForce(delta.euclideanNorm()));

            // Add vertical alignment force (C1)
            Vector2D totalF = repF.add(new Vector2D(vN.getWidth()/2.0 + uN.getWidth()/2.0, 0));

            dispMap.put(v, dispMap.get(v).add(totalF));
        }
    }

    protected void applyForce(int v, Vector2D bounds)
    {
        iGraph graph = getGraph();
        HashMap<Integer, Vector2D> dispMap = getDispMap();
        iGNode vN = graph.getNode(v);

        Vector2D tempPos = new Vector2D(vN.getPos().getX(), vN.getPos().getY());
        Vector2D dispNorm = dispMap.get(v).normalize().mult(min(dispMap.get(v).euclideanNorm(), getTemp()));
        vN.setPos(vN.getPos().add(dispNorm));

        // boundaries
        double x = min(bounds.getX()/2, max(-bounds.getX()/2, vN.getPos().getX()));
        double y = min(bounds.getY()/2, max(-bounds.getY()/2, vN.getPos().getY()));

        if (Double.isNaN(x) || Double.isNaN(y)) vN.setPos(tempPos);
        else vN.setPos(new Vector2D(x, y));
    }

    protected abstract void iteration();

    protected abstract HashMap<Integer, Vector2D> getDispMap();

    protected abstract iGraph getGraph();

    protected abstract double getK();

    protected abstract double getCoolRate();

    protected abstract double getTemp();

}
