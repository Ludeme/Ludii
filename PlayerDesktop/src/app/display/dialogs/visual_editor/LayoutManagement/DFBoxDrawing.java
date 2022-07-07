package app.display.dialogs.visual_editor.LayoutManagement;


import app.display.dialogs.visual_editor.model.interfaces.iGNode;
import app.display.dialogs.visual_editor.model.interfaces.iGraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static app.display.dialogs.visual_editor.LayoutManagement.NodePlacementRoutines.translateByRoot;

/**
 * Layout drawing method
 * @author nic0gin
 */
public class DFBoxDrawing
{
    private final iGraph graph;
    private int freeY;

    private double[] odsMetrics;

    private static final double DEFAULT_DISTANCE = 0.1;
    private static final double DEFAULT_OFFSET = 0.1;
    private static final double DEFAULT_SPREAD = 0.1;
    private double compactness = 0.5;

    private final int PADDING_X = 10;

    public static final int MIN_NODE_GAP = 20;

    /**
     * Constructor
     * @param graph graph
     */
    public DFBoxDrawing(iGraph graph)
    {
        this.graph = graph;
        freeY = 0;
        odsMetrics = new double[3];
        initWeights();
    }

    /**
     * Initialize layout metrics with default values
     */
    private void initWeights()
    {
        odsMetrics[0] = DEFAULT_OFFSET;
        odsMetrics[1] = DEFAULT_DISTANCE;
        odsMetrics[2] = DEFAULT_SPREAD;
    }

    /**
     * Main arrangement procedure. Based on the depth-search box layout algorithm by Miyadera et al., 1998.
     * Miyadera, Y., Anzai, K., Unno, H., and Yaku, T. (1998). Depth-first layout
     * algorithm for trees. Inf. Process. Lett., 66(4):187–194.
     * @param nodeId root node of tree/sub-tree to arrange
     * @param freeX initial x position
     */
    private void initPlacement(int nodeId, int freeX)
    {
        if (graph.getNode(nodeId).children() == null ||
                graph.getNode(nodeId).children().size() == 0 ||
                (graph.getNode(nodeId).fixed() && graph.selectedRoot() != nodeId))
        {
            Vector2D piInit = new Vector2D(freeX, freeY);
            if (graph.getNode(nodeId).fixed())
            {
                freeY += (graph.getNode(nodeId).pos().y() - GraphRoutines.getSubtreeArea(graph, nodeId).y);
                piInit = new Vector2D(freeX, freeY);
                freeY += GraphRoutines.nodesMaxSpread() * (odsMetrics[2]) + GraphRoutines.getSubtreeArea(graph, nodeId).height + PADDING_X;
                translateByRoot(graph, nodeId, piInit);
            }
            else
            {
                freeY += GraphRoutines.nodesMaxSpread() * (odsMetrics[2]) + graph.getNode(nodeId).height() + PADDING_X;
            }
            // update node position
            graph.getNode(nodeId).setPos(piInit);
        }
        else
        {
            List<Integer> nodeCh = graph.getNode(nodeId).children();
            iGNode nFirst = graph.getNode(nodeCh.get(0));
            iGNode nLast = graph.getNode(nodeCh.get(nodeCh.size()-1));

            nodeCh.forEach((s) -> {
                initPlacement(s, (int) (freeX + GraphRoutines.nodesMaxDist() * odsMetrics[1]) + graph.getNode(s).width() + PADDING_X);
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

            double X0 = nFirst.pos().y();
            double X1 = nLast.pos().y();

            double wOffset = odsMetrics[0];
            double yCoord;

            yCoord = (X1 - X0) * wOffset + X0;

            Vector2D piInit = new Vector2D(freeX, yCoord);
            // update node position
            nV.setPos(piInit);
        }
    }

    /**
     * Compaction procedure. Based on the paper by Hasan et al., 2003.
     * Hasan, M., Rahman, M. S., and Nishizeki, T. (2003). A linear algorithm for
     * compact box-drawings of trees. Networks, 42(3):160–164.
     * @param root root node of tree/sub-tree to arrange
     */
    private void compactBox(int root)
    {
        // 1. Find all paths
        ArrayList<List<Integer>> paths = new ArrayList<>();
        GraphRoutines.findAllPaths(paths, graph, root, new ArrayList<>());
        // 2. Compute upward visibility graph
        HashMap<Integer, Integer> Gup = findUpwardVisibilityGraph(paths, graph);
        // 3. Move nodes upward according to G_up and specified metrics
        moveNodeUpward(paths, Gup, graph);
    }

    /**
     * Finds upward visibility graph
     * @param paths paths of graph
     * @param graph graph in operation
     * @return hashmap where keys correspond to node ids and value to their minimum distance from upper node
     */
    private HashMap<Integer, Integer> findUpwardVisibilityGraph(List<List<Integer>> paths, iGraph graph)
    {
        // Initialize
        HashMap<Integer, Integer> Gup = new HashMap<>();
        List<Integer> LE = new ArrayList<>(paths.get(0));
        // iterate each path
        for (int i = 1; i < paths.size(); i++)
        {
            List<Integer> leCandidates = new ArrayList<>();
            List<Integer> P = new ArrayList<>(paths.get(i));
            // cursor on Lower Envelop
            int j = LE.size()-1;
            // cursor on current path
            int k = P.size()-1;
            // iterating through LE and current path to add edges into Gup
            while (j != 0 && k != 0)
            {
                int currentPk = P.get(k);
                iGNode upper = graph.getNode(LE.get(j));
                iGNode lower = graph.getNode(P.get(k));
                if (upper.equals(lower)) break;

                int upperHeight = upper.fixed() ? GraphRoutines.getSubtreeArea(graph, upper.id()).height : upper.height();
                int upperWidth = upper.fixed() ? GraphRoutines.getSubtreeArea(graph, upper.id()).width : upper.width();
                int lowerWidth = lower.fixed() ? GraphRoutines.getSubtreeArea(graph, lower.id()).width : lower.width();

                int nodeDist = (int)(lower.pos().y() - upper.pos().y() - upperHeight);
                // check all the cases for upper and lower nodes x coordinates intersecting
                // add edges for upward visibility graph
                // construct next LE
                // CASE#1: left corner coordinates match
                if ((int)(upper.pos().x()) == (int)(lower.pos().x()))
                {
                    // check if right corner of upper exceeds right corner of lower to add to LE candidates
                    if ((int)(upper.pos().x()+ upperWidth) > (int)(lower.pos().x()+lowerWidth))
                    {
                        leCandidates.add(0, upper.id());
                    }
                    addMinDistToGup(Gup, P, k, nodeDist);
                    j--;
                    k--;
                }
                else if ((int)(upper.pos().x()) > (int)(lower.pos().x()))
                {
                    // check if right corner of upper exceeds right corner of lower to add to LE candidates
                    if ((int)(upper.pos().x()+ upperWidth) > (int)(lower.pos().x()+lowerWidth))
                    {
                        leCandidates.add(0, upper.id());
                    }
                    // CASE#2: left corner of upper is within lower
                    if ((int)(upper.pos().x()) <= (int)(lower.pos().x()+lowerWidth))
                    {
                        addMinDistToGup(Gup, P, k, nodeDist);
                    }
                    j--;
                }
                else if ((int)(upper.pos().x()) < (int)(lower.pos().x()))
                {
                    // CASE#2: right corner of upper is within lower
                    if ((int)(upper.pos().x()+ upperWidth) > (int)(lower.pos().x()))
                    {
                        addMinDistToGup(Gup, P, k, nodeDist);
                    }
                    k--;
                }
                leCandidates.add(0, currentPk);
            }
            LE = new ArrayList<>(leCandidates);
            LE.addAll(0, paths.get(i).stream().limit(k+1).collect(Collectors.toList()));
        }
        return Gup;
    }

    /**
     * Helper method to add new min distance to upward visibility graph
     * @param gup upward visibility graph - hashmap where key is node id and value is minimum distance to upper node
     * @param p current path
     * @param k local id on path for current node
     * @param nodeDist distance from current node to upper node
     */
    private void addMinDistToGup(HashMap<Integer, Integer> gup, List<Integer> p, int k, int nodeDist)
    {
        if (gup.containsKey(p.get(k))) gup.put(p.get(k), Math.min(gup.get(p.get(k)), nodeDist));
        else gup.put(p.get(k), nodeDist);
    }

    /**
     * Move nodes upward according to upward visibility graph
     * @param paths paths of tree/subtree
     * @param gup upward visibility graph - hashmap where key is node id and value is minimum distance to upper node
     * @param graph graph in operation
     */
    private void moveNodeUpward(List<List<Integer>> paths, HashMap<Integer, Integer> gup, iGraph graph)
    {
        List<Integer> subTree = new ArrayList<>();
        List<Integer> P;
        // keep track of T_i-1 nodes
        for (int i = 1; i < paths.size(); i++)
        {
            P = new ArrayList<>(paths.get(i));
            int minDist = Integer.MAX_VALUE;
            for (int j = 1; j < P.size(); j++)
            {
                int nid = P.get(j);
                // TODO: can improve compactness
                if (gup.containsKey(nid)) {minDist = Math.min(minDist, gup.get(nid));}
            }
            for (int j = 1; j < P.size(); j++)
            {
                iGNode n = graph.getNode(P.get(j));
                int dist = minDist - MIN_NODE_GAP;
                if (!subTree.contains(P.get(j)))
                {
                    n.setPos(new Vector2D(n.pos().x(), n.pos().y() - Math.max(dist, 0)*compactness));
                    subTree.add(P.get(j));
                }

            }
        }
    }

    /**
     * Update layout metrics. Values should be between 0.0 and 1.0
     * @param offset relative offset of subtrees with respect to their root
     * @param distance relative distance between subtrees and their root
     * @param spread inner distance between nodes in a subtree
     */
    public void updateWeights(Double offset, Double distance, Double spread)
    {
        odsMetrics[0] = offset;
        odsMetrics[1] = distance;
        odsMetrics[2] = spread;
    }

    /**
     * Update layout metrics
     * @param weights array of O,D,S metrics
     */
    public void updateWeights(double[] weights)
    {
        odsMetrics = weights.clone();
    }

    /**
     * Update layout metric for graph compactness.
     * @param compactness value if between 0.0 and 1.0
     */
    public void setCompactness(double compactness)
    {
        this.compactness = compactness;
    }

    /**
     * Application of layout arrangement procedures.
     * @param root root node of tree/sub-tree to arrange
     */
    public void applyLayout(int root)
    {
        freeY = 0;
        Vector2D oPos = graph.getNode(root).pos();
        initPlacement(root,0);
        compactBox(root);
        translateByRoot(graph, root, oPos);
    }

    /**
     * Default offset metric
     */
    public static double defaultO() { return DEFAULT_OFFSET; }

    /**
     * Default distance metric
     */
    public static double defaultD() { return DEFAULT_DISTANCE; }

    /**
     * Default spread metric
     */
    public static double defaultS() { return DEFAULT_SPREAD; }

}
