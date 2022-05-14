package app.display.dialogs.visual_editor.LayoutManagement.LayoutManager;


import app.display.dialogs.visual_editor.LayoutManagement.Math.Vector2D;
import app.display.dialogs.visual_editor.model.interfaces.iGNode;
import app.display.dialogs.visual_editor.model.interfaces.iGraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static app.display.dialogs.visual_editor.LayoutManagement.GraphDrawing.NodePlacementRoutines.translateByRoot;

/**
 * By Y. Miyadera et al (1998) https://doi.org/10.1016/s0020-0190(98)00068-4
 * @author nic0gin
 */
public class DFSBoxDrawing implements LayoutMethod
{
    private iGraph graph;
    private final int C3j;
    private int freeY;
    // spread
    private final double wY;
    // distance
    private final double wX;
    private final int root;

    private HashMap<Integer, Double[]> DOS_MAP;

    private final double DEFAULT_DISTANCE = 0.3;
    private final double DEFAULT_OFFSET = 0.5;
    private final double DEFAULT_SPREAD = 0.4;

    private final int PADDING_X = 10;
    private final int PADDING_Y = 10;

    /**
     *
     * @param graph graph
     * @param root
     * @param C3j a non-negative integer constraint
     * @param wX maximum inner-subtree distance
     * @param wY maximum inner-subtree spread
     */
    public DFSBoxDrawing(iGraph graph, int root, int C3j, double wY, double wX)
    {
        this.C3j = C3j;
        this.graph = graph;
        freeY = 0;
        this.root = root;

        this.wX = wX;
        this.wY = wY;

        DOS_MAP = new HashMap<>();
        initWeights();
    }

    private void initWeights()
    {
        graph.getNodeList().forEach((id,n) ->{
            if (!n.getChildren().isEmpty())
            {
                if (!DOS_MAP.containsKey(id)) DOS_MAP.put(id, new Double[]{DEFAULT_DISTANCE, DEFAULT_OFFSET, DEFAULT_SPREAD});
            }
        });
    }

    private void initPlacement(int nodeId, int freeX)
    {
        if (graph.getNode(nodeId).getChildren() == null || graph.getNode(nodeId).getChildren().size() == 0)
        {
            Vector2D piInit = new Vector2D(freeX, freeY);
            freeY += graph.getNode(nodeId).getHeight() * wX * (DOS_MAP.get(graph.getNode(nodeId).getParent())[2]) + graph.getNode(nodeId).getHeight() + PADDING_X;

            graph.getNode(nodeId).setPos(piInit);
        }
        else
        {
            List<Integer> nodeCh = graph.getNode(nodeId).getChildren();
            iGNode nFirst = graph.getNode(nodeCh.get(0));
            iGNode nLast = graph.getNode(nodeCh.get(nodeCh.size()-1));

            nodeCh.forEach((s) -> {
                initPlacement(s, (int) (freeX + graph.getNode(s).getWidth() * wY * (DOS_MAP.get(graph.getNode(s).getParent())[0])) + graph.getNode(s).getWidth() + PADDING_X);
                // freeX + getNodeDepth(graph, s)*graph.getNode(s).getWidth()*wX


                // H-V downward layout: nFirst.getPos().getY() + 0
                // intermediate values: between 0.0 and 0.5
                // Symmetry: nFirst.getPos().getY() + (nLast.getPos().getY() - nFirst.getPos().getY())/2
                // intermediate values between 0.5 and 1.0
                // H-V upward layout: nLast.getPos().getY()


                // Several options to set Y coordinate based positions of children nodes
                // (nLast.getPos().getY() - nFirst.getPos().getY())/2
                // min(C3j, nLast.getPos().getY() - nFirst.getPos().getY())

                // uncomment to see fun effect
                //freeY = Math.max(freeY, (int) (nV.getPos().getY() + nV.getHeight()*wY));

            });

            iGNode nV = graph.getNode(nodeId);

            double X0 = nFirst.getPos().getY();
            double X1 = nLast.getPos().getY();

            double wOffset = DOS_MAP.get(nodeId)[1];
            double yCoord;

            yCoord = (X1 - X0) * ((wOffset + 1)/2) + X0;

            Vector2D piInit = new Vector2D(freeX, yCoord);
            nV.setPos(piInit);

        }
    }

    public void updateAllWeights(Double offset, Double distance, Double spread)
    {
        DOS_MAP.forEach((id, w) -> {
            updateSubtreeWeights(offset, distance, spread, id);
        });
    }

    public void updateSubtreeWeights(Double offset, Double distance, Double spread, int p)
    {
        if (distance != null) DOS_MAP.get(p)[0] = distance;
        if (offset != null) DOS_MAP.get(p)[1] = offset;
        if (spread != null) DOS_MAP.get(p)[2] = spread;
    }

    public void updateAllWeights(HashMap<Integer, Double[]> weights)
    {
        DOS_MAP = new HashMap<>(weights);
    }

    private void shift(int root)
    {
        List<Integer> Q = new ArrayList<>();
        Q.add(root);
        List<Integer> childNodes;
        int nId;
        while (!Q.isEmpty())
        {
            nId = Q.remove(0);
            childNodes = graph.getNode(nId).getChildren();
            for (int i = childNodes.size()-1; i >= 0; i--)
            {
                if (i > 1)
                {
                    iGNode nV = graph.getNode(i);
                    iGNode nVl = graph.getNode(childNodes.get(i-1));
                    nV.setPos(nV.getPos().sub(new Vector2D(0, nVl.getPos().getY() + nVl.getHeight() )));
                }
                Q.add(childNodes.get(i));
            }
        }
    }

    @Override
    public void applyLayout()
    {
        freeY = 0;
        //LudemeNode lN = (LudemeNode) graph.getNode(root);
        //Vector2D lPos = lN.getPos();
        Vector2D oPos = graph.getNode(root).getPos();

        initWeights();

        initPlacement(root,0);
        //shift(r);

        translateByRoot(graph, root, oPos);

        //packLayers(graph, root);
        //NodePlacementRoutines.packLayers(graph, root);
        // translate graph by root vertex coordinates

        //NodePlacementRoutines.resolveNodeTranslation(graph, root);
    }
}
