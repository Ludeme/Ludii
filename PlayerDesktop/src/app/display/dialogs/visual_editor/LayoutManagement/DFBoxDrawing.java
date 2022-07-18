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
    private static boolean RECORD_TIME;
    private final iGraph graph;
    private int freeY;

    private double[] odsMetrics;

    private static final double DEFAULT_DISTANCE = 0.4;
    private static final double DEFAULT_OFFSET = 0.2;
    private static final double DEFAULT_SPREAD = 0.1;
    private double compactness = 0.9;

    private final int PADDING_X = 10;

    public static final int MIN_NODE_GAP = 20;

    private final HashMap<Integer, Integer> gupDistances = new HashMap<>();

    /**
     * Constructor
     * @param graph graph
     */
    public DFBoxDrawing(iGraph graph, boolean RECORD_TIME)
    {
        this.graph = graph;
        DFBoxDrawing.RECORD_TIME = RECORD_TIME;
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
                freeY += GraphRoutines.nodesMaxSpread() * (odsMetrics[2] * (1.0 - compactness)) + GraphRoutines.getSubtreeArea(graph, nodeId).height + PADDING_X;
                translateByRoot(graph, nodeId, piInit);
            }
            else
            {
                freeY += GraphRoutines.nodesMaxSpread() * (odsMetrics[2] * (1.0 - compactness)) + graph.getNode(nodeId).height() + PADDING_X;
            }
            // update node position
            graph.getNode(nodeId).setPos(piInit);
        }
        else
        {
            List<Integer> nodeCh = new ArrayList<>(graph.getNode(nodeId).children());
            graph.getNode(nodeId).children().forEach(v -> {
                if (graph.getNode(v).collapsed()) nodeCh.remove((Object) v);
            });
            if (nodeCh.size() == 0) return;

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

            yCoord = (X1 - X0) * wOffset * (0.5 * (2.0 - compactness)) + X0;

            Vector2D piInit = new Vector2D(freeX, yCoord);
            // update node position
            nV.setPos(piInit);
            freeY = Math.max(freeY, (int)(yCoord+nV.height()+PADDING_X));
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
        gupDistances.clear();
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

                int upperWidth = upper.fixed() ? GraphRoutines.getSubtreeArea(graph, upper.id()).width : upper.width();
                int lowerWidth = lower.fixed() ? GraphRoutines.getSubtreeArea(graph, lower.id()).width : lower.width();

                // int nodeDist = (int)(lower.pos().y() - upper.pos().y() - upperHeight);
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
                    addMinDistToGup(Gup, upper, lower);
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
                        addMinDistToGup(Gup, upper, lower);
                    }
                    j--;
                }
                else if ((int)(upper.pos().x()) < (int)(lower.pos().x()))
                {
                    // CASE#2: right corner of upper is within lower
                    if ((int)(upper.pos().x()+ upperWidth) > (int)(lower.pos().x()))
                    {
                        addMinDistToGup(Gup, upper, lower);
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
     * @param upper upper node
     * @param lower lower node
     */
    private void addMinDistToGup(HashMap<Integer, Integer> gup, iGNode upper, iGNode lower)
    {
        int newDist = GraphRoutines.computeNodeVerticalDistance(upper.id(), lower.id(), graph);
        if (gup.containsKey(lower.id()))
        {
            if (newDist < gupDistances.get(lower.id()))
            {
                gup.put(lower.id(), upper.id());
                gupDistances.put(lower.id(), newDist);
            }
        }
        else
        {
            gup.put(lower.id(), upper.id());
            gupDistances.put(lower.id(), newDist);
        }
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
                if (!subTree.contains(P.get(j)) && gup.containsKey(nid)) {minDist = Math.min(minDist,
                        GraphRoutines.computeNodeVerticalDistance(gup.get(nid), nid, graph));}
            }
            for (int j = 1; j < P.size(); j++)
            {
                iGNode n = graph.getNode(P.get(j));
                int dist = Math.abs(minDist - MIN_NODE_GAP);
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

        long startTime = System.nanoTime();
        initPlacement(root,0);
        long endTime = System.nanoTime();
        if (RECORD_TIME) System.out.println("Init placement: " + (endTime - startTime)/1E6);

        startTime = System.nanoTime();
        compactBox(root);
        endTime = System.nanoTime();
        if (RECORD_TIME) System.out.println("Compact box: " + (endTime - startTime)/1E6);

        startTime = System.nanoTime();
        translateByRoot(graph, root, oPos);
        endTime = System.nanoTime();
        if (RECORD_TIME) System.out.println("Translate by root: " + (endTime - startTime)/1E6);

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
