package app.display.dialogs.visual_editor.LayoutManagement.GraphDrawing;

import app.display.dialogs.visual_editor.LayoutManagement.LayoutManager.LayoutHandler;
import app.display.dialogs.visual_editor.LayoutManagement.Math.Vector2D;
import app.display.dialogs.visual_editor.model.interfaces.iGNode;
import app.display.dialogs.visual_editor.model.interfaces.iGraph;
import game.rules.meta.no.No;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static app.display.dialogs.visual_editor.LayoutManagement.GraphRoutines.getNodeDepth;
import static java.lang.Math.abs;

public final class NodePlacementRoutines
{

    public static final int X_AXIS = 0;

    public static final int Y_AXIS = 1;

    private static final int NODE_GAP = 50;


    /**
     * Translate node placement by oPos vector with respect to root
     * @param r
     * @param oPos
     * @param graph
     */
    public static void translateByRoot(iGraph graph, int r, Vector2D oPos)
    {
        Vector2D t = graph.getNode(r).pos().sub(oPos);
        graph.getNodeList().forEach((i,v) -> {
            v.setPos(v.pos().sub(t));
        });
    }

    /**
     * Set inner horizontal layer distance. a < b.
     * @param r root
     * @param a layer
     * @param b layer
     * @param d distance
     */
    public void setInLayerDistance(iGraph graph, int r, int a, int b, int d)
    {
        if ((abs(b-a) != 1)) return;

        int nId = a;
        int layer = getNodeDepth(graph, nId);
        while (layer != a)
        {

        }
    }

    public static void packLayers(iGraph graph, int r)
    {
        List<Integer> layer = new ArrayList<>();
        List<Integer> nextLayer = new ArrayList<>();

        int d = 1;
        int basicX = 0;
        layer.add(r);
        while (!layer.isEmpty())
        {
            if (d == 2)
            {

                basicX = (int) graph.getNode(layer.get(0)).pos().getX();
            }

            int finalBasicX;
            if (d > 2) finalBasicX = basicX*d;
            else finalBasicX = 1;

            int finalD = d;
            nextLayer.clear();
            layer.forEach((v) -> {
                iGNode vN = graph.getNode(v);
                if (finalD > 2) vN.setPos(new Vector2D(finalBasicX, vN.pos().getY()));
                nextLayer.addAll(graph.getNode(v).children());
            });
            layer = new ArrayList<>(nextLayer);
            d++;
        }
    }

    public static void resolveNodeTranslation(iGraph graph, int r)
    {
        HashMap<Integer, Vector2D> transPos = new HashMap<>();
        transPos.put(r, graph.getNode(r).pos());

        List<Integer> Q = new ArrayList<>();
        List<Integer> Visited = new ArrayList<>();

        Visited.add(r);
        Q.add(r);
        while (!Q.isEmpty())
        {
            int n = Q.remove(0);

            if (n != r)
            {
                Vector2D vPos = graph.getNode(n).pos();
                iGNode p = graph.getNode(graph.getNode(n).parent());
                Vector2D pPos = p.pos();
                Vector2D relPos = vPos.sub(pPos);
                transPos.put(n, new Vector2D(p.width()+relPos.getX(), p.height()+relPos.getY()));
            }

            List<Integer> children = graph.getNode(n).children();
            children.forEach((v) -> {
                if (!Visited.contains(v))
                {
                    Q.add(v);
                    Visited.add(v);
                }
            });
        }
        graph.getNodeList().forEach((i,v) -> {
            v.setPos(transPos.get(i));
        });
    }

    public static void alignNodes(List<iGNode> nodes, int axis)
    {
        double posX = nodes.get(0).getPos().getX();
        double posY = nodes.get(0).getPos().getY();
        if (nodes.isEmpty()) return;
        if (axis == X_AXIS)
        {
            for (int i = 1; i < nodes.size(); i++)
            {
                nodes.get(i).setPos(new Vector2D(posX+nodes.get(i-1).getWidth()+NODE_GAP*i, posY));
            }
        }
        else if (axis == Y_AXIS)
        {
            for (int i = 1; i < nodes.size(); i++)
            {
                nodes.get(i).setPos(new Vector2D(posX, posY+nodes.get(i-1).getHeight()+NODE_GAP*i));
            }
        }
    }

}
