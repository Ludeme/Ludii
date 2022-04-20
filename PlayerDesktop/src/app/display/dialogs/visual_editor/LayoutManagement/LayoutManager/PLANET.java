package app.display.dialogs.visual_editor.LayoutManagement.LayoutManager;

import app.display.dialogs.visual_editor.LayoutManagement.GraphRoutines;
import app.display.dialogs.visual_editor.LayoutManagement.Math.Vector2D;
import app.display.dialogs.visual_editor.model.interfaces.iGNode;
import app.display.dialogs.visual_editor.model.interfaces.iGraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static app.display.dialogs.visual_editor.LayoutManagement.GraphRoutines.getChildIndex;
import static app.display.dialogs.visual_editor.LayoutManagement.GraphRoutines.getNumSiblings;
import static java.lang.Math.*;

/**
 * Radial PLANET algorithm by Huang, G. et al (2020). https://doi.org/10.1016/j.physa.2019.122948
 * @author nic0gin
 */
public class PLANET implements LayoutMethod
{

    private iGraph graph;
    private final int ROOT_ID;
    private final int XI;
    private HashMap<Integer, Double> thetaMap;

    private final double CIRC = PI*2.0; // Originally equals 2*PI

    public PLANET(iGraph graph, int ROOT_ID, int XI)
    {
        this.graph = graph;
        this.ROOT_ID = ROOT_ID;
        this.XI = XI;
        thetaMap = new HashMap<>();
    }

    /**
     * Node angle
     * @param i child index of node
     * @param nd number of siblings?
     * @return parameter theta
     */
    private double calculateTheta1(int i, int nd)
    {
        return (2*(i - 1)*PI) / nd;
    }

    private double calculateTheta2(int v, int i, int nd)
    {
        double theta1 = thetaMap.get(graph.getNode(v).getParent());
        if (nd == 1) return theta1;
        else
        {
            double f0 = fJ(0);
            return theta1 - PI/f0 + 2*(i - 1)*PI/((nd - 1)*f0);
        }
    }

    /**
     * Node angle
     * @param v node index
     * @param d layer depth
     * @param i child index of node
     * @param nd number of siblings?
     * @return parameter theta
     */
    private double calculateThetaNd(int v, int d, int i, int nd)
    {
        int dm1 = graph.getNode(v).getParent();
        int dm2 = graph.getNode(dm1).getParent();
        double thetadm1 = thetaMap.get(dm1);
        double thetadm2 = thetaMap.get(dm2);

        int m = d-2;

        if (nd == 1) return thetadm1;
        else if (thetadm1 < thetadm2)
        {
            int fJProd = prodFk(m);
            return thetadm1 + 2*(i-1)*PI/((nd-1)*fJProd);
        }
        else if (thetadm1 > thetadm2)
        {
            int fJProd = prodFk(m);
            return thetadm1 - 2*(i-1)*PI/((nd-1)*fJProd);
        }
        else
        {
            int fJProd = prodFk(m);
            return thetadm1 - PI/fJProd + 2*(i-1)*PI/((nd-1)*fJProd);
        }
    }

    /**
     * Number of child nodes of the jth layer parent node
     * @param j layer id
     * @return positive integer
     */
    private int fJ(int j)
    {
        List<Integer> pLayer = GraphRoutines.getLayerNodes(graph, j, ROOT_ID);
        AtomicInteger count = new AtomicInteger();
        pLayer.forEach((p) -> {
            count.addAndGet(graph.getNode(p).getChildren().size());
        });
        return count.get();
    }

    private int prodFk(int m)
    {
        int prod = 1;
        for (int j = 0; j < m; j++)
        {
            prod *= fJ(j);
        }
        return prod;
    }

    private void PLANET(int r)
    {
        // Initialization
        int d = 0;
        int r0 = 50;

        iGNode rN = graph.getNode(r);
        List<Integer> Q = new ArrayList<>(rN.getChildren());

        // Iteration: calculate the coordinate of nodes on dth levels
        while (!Q.isEmpty())
        {
            d++;
            int rd = r0 + XI*d;
            int N = Q.size();
            List<Integer> QCopy = new ArrayList<>(Q);
            if (d == 1)
            {
                for (int i = 0; i < N; i++)
                {
                    int v = Q.get(i);
                    double theta = calculateTheta1(getChildIndex(graph, v), getNumSiblings(graph, v));
                    innerStep(Q, rd, v, theta);
                }
            }
            else if (d == 2)
            {
                for (int i = 0; i < N; i++)
                {
                    int v = Q.get(i);
                    double theta = calculateTheta2(v, getChildIndex(graph, v), getNumSiblings(graph, v));
                    innerStep(Q, rd, v, theta);
                }
            }
            else
            {
                for (int i = 0; i < N; i++)
                {
                    int v = Q.get(i);
                    double theta = calculateThetaNd(v, d, getChildIndex(graph, v), getNumSiblings(graph, v));
                    innerStep(Q, rd, v, theta);
                }
            }
            Q.removeAll(QCopy);
        }
    }

    private void innerStep(List<Integer> q, int rd, int v, double theta)
    {
        thetaMap.put(v, theta);
        iGNode nV = graph.getNode(v);
        Vector2D pXY = graph.getNode(graph.getNode(v).getParent()).getPos();
        nV.setPos(pXY.add(new Vector2D(rd*cos(theta), rd*sin(theta))));
        List<Integer> m = nV.getChildren();
        q.addAll(m);
    }

    @Override
    public void applyLayout()
    {
        PLANET(ROOT_ID);
    }
}
