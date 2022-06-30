package app.display.dialogs.visual_editor.LayoutManagement;


import app.display.dialogs.visual_editor.model.interfaces.iGNode;
import app.display.dialogs.visual_editor.model.interfaces.iGraph;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.LudemeNodeComponent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static app.display.dialogs.visual_editor.LayoutManagement.NodePlacementRoutines.translateByRoot;

/**
 * By Y. Miyadera et al (1998) https://doi.org/10.1016/s0020-0190(98)00068-4
 * @author nic0gin
 */
public class DFSBoxDrawing
{
    private final iGraph graph;
    private int freeY;
    private final double wY;
    private final double wX;

    private double[] DOS_MAP;

    private final double DEFAULT_DISTANCE = 0.1;
    private final double DEFAULT_OFFSET = 0.1;
    private final double DEFAULT_SPREAD = 0.1;
    private double compactness = 0.5;

    private final int PADDING_X = 10;
    private final int PADDING_Y = 10;

    public static final int MIN_NODE_GAP = 50;

    /**
     *
     * @param graph graph
     * @param wX maximum inner-subtree distance
     * @param wY maximum inner-subtree spread
     */
    public DFSBoxDrawing(iGraph graph, double wY, double wX)
    {
        this.graph = graph;
        freeY = 0;

        this.wX = wX;
        this.wY = wY;

        DOS_MAP = new double[3];
        initWeights();
    }

    private void initWeights()
    {
        DOS_MAP[0] = DEFAULT_SPREAD;
        DOS_MAP[1] = DEFAULT_OFFSET;
        DOS_MAP[2] = DEFAULT_DISTANCE;
    }

    private void initPlacement(int nodeId, int freeX)
    {
        if (graph.getNode(nodeId).children() == null || graph.getNode(nodeId).children().size() == 0)
        {
            Vector2D piInit = new Vector2D(freeX, freeY);
            freeY += graph.getNode(nodeId).height() * wX * (DOS_MAP[0]) + graph.getNode(nodeId).height() + PADDING_X;
            // update node position
            graph.getNode(nodeId).setOldPos(graph.getNode(nodeId).pos());
            graph.getNode(nodeId).setPos(piInit);
            //graph.getNode(nodeId).setNewPos(piInit);
        }
        else
        {
            List<Integer> nodeCh = graph.getNode(nodeId).children();
            iGNode nFirst = graph.getNode(nodeCh.get(0));
            iGNode nLast = graph.getNode(nodeCh.get(nodeCh.size()-1));

            nodeCh.forEach((s) -> {
                initPlacement(s, (int) (freeX + graph.getNode(s).width() * wY * DOS_MAP[2]) + graph.getNode(s).width() + PADDING_X);
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

            double wOffset = DOS_MAP[1];
            double yCoord;

            yCoord = (X1 - X0) * wOffset + X0;

            Vector2D piInit = new Vector2D(freeX, yCoord);
            // update node position
            nV.setOldPos(nV.pos());
            nV.setPos(piInit);
            //nV.setNewPos(piInit);
        }
    }

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
                int nodeDist = (int)(lower.pos().y()-upper.pos().y()-upper.height());
                // check all the cases for upper and lower nodes x coordinates intersecting
                // add edges for upward visibility graph
                // construct next LE
                // CASE#1: left corner coordinates match
                if ((int)(upper.pos().x()) == (int)(lower.pos().x()))
                {
                    // check if right corner of upper exceeds right corner of lower to add to LE candidates
                    if ((int)(upper.pos().x()+upper.width()) > (int)(lower.pos().x()+lower.width()))
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
                    if ((int)(upper.pos().x()+upper.width()) > (int)(lower.pos().x()+lower.width()))
                    {
                        leCandidates.add(0, upper.id());
                    }
                    // CASE#2: left corner of upper is within lower
                    if ((int)(upper.pos().x()) <= (int)(lower.pos().x()+lower.width()))
                    {
                        addMinDistToGup(Gup, P, k, nodeDist);
                    }
                    j--;
                }
                else if ((int)(upper.pos().x()) < (int)(lower.pos().x()))
                {
                    // CASE#2: right corner of upper is within lower
                    if ((int)(upper.pos().x()+upper.width()) > (int)(lower.pos().x()))
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

    private void addMinDistToGup(HashMap<Integer, Integer> gup, List<Integer> p, int k, int nodeDist)
    {
        if (gup.containsKey(p.get(k))) gup.put(p.get(k), Math.min(gup.get(p.get(k)), nodeDist));
        else gup.put(p.get(k), nodeDist);
    }

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

    public void updateWeights(Double offset, Double distance, Double spread)
    {
        DOS_MAP[0] = spread;
        DOS_MAP[1] = offset;
        DOS_MAP[2] = distance;
    }

    public void setCompactness(double compactness)
    {
        this.compactness = compactness;
    }

    public void updateAllWeights(double[] weights)
    {
        DOS_MAP = weights.clone();
    }

    public void applyLayout(int root)
    {
        freeY = 0;
        Vector2D oPos = graph.getNode(root).pos();
        initPlacement(root,0);
        compactBox(root);
        translateByRoot(graph, root, oPos);
    }

}
