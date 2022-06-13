package app.display.dialogs.visual_editor.LayoutManagement.LayoutManager;


import app.display.dialogs.visual_editor.LayoutManagement.Math.Vector2D;
import app.display.dialogs.visual_editor.model.interfaces.iGNode;
import app.display.dialogs.visual_editor.model.interfaces.iGraph;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.LudemeNodeComponent;

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
    private int root;

    private HashMap<Integer, Double[]> DOS_MAP;

    private final double DEFAULT_DISTANCE = 0.3;
    private final double DEFAULT_OFFSET = 0.5;
    private final double DEFAULT_SPREAD = 0.4;

    private final int PADDING_X = 10;
    private final int PADDING_Y = 10;

    private List<LudemeNodeComponent> selectedNodes;

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
            if (!n.children().isEmpty())
            {
                if (!DOS_MAP.containsKey(id)) DOS_MAP.put(id, new Double[]{DEFAULT_DISTANCE, DEFAULT_OFFSET, DEFAULT_SPREAD});
            }
        });
    }

    private void initPlacement(int nodeId, int freeX)
    {
        if (graph.getNode(nodeId).children() == null || graph.getNode(nodeId).children().size() == 0)
        {
            Vector2D piInit = new Vector2D(freeX, freeY);
            freeY += graph.getNode(nodeId).height() * wX * (DOS_MAP.get(graph.getNode(nodeId).parent())[2]) + graph.getNode(nodeId).height() + PADDING_X;

            graph.getNode(nodeId).setPos(piInit);
        }
        else
        {
            List<Integer> nodeCh = graph.getNode(nodeId).children();
            iGNode nFirst = graph.getNode(nodeCh.get(0));
            iGNode nLast = graph.getNode(nodeCh.get(nodeCh.size()-1));

            nodeCh.forEach((s) -> {
                initPlacement(s, (int) (freeX + graph.getNode(s).width() * wY * (DOS_MAP.get(graph.getNode(s).parent())[0])) + graph.getNode(s).width() + PADDING_X);
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

            double X0 = nFirst.pos().getY();
            double X1 = nLast.pos().getY();

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

    public void setSelectedNodes(List<LudemeNodeComponent> selectedNodes)
    {
        this.selectedNodes = selectedNodes;
    }

    @Override
    public void applyLayout()
    {
        freeY = 0;
        Vector2D oPos = graph.getNode(root).pos();
        initWeights();
        initPlacement(root,0);
        translateByRoot(graph, root, oPos);
    }

    @Override
    public void setRoot(int root) {
        this.root = root;
    }
}
