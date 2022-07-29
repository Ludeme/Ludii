package app.display.dialogs.visual_editor.LayoutManagement;


import app.display.dialogs.visual_editor.model.interfaces.iGNode;
import app.display.dialogs.visual_editor.model.interfaces.iGraph;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static java.lang.Math.abs;

/**
 * Graph manipulation procedures
 * @author nic0gin
 */
public final class GraphRoutines
{
    /**
     * Tuning constants for metric evaluation
     */
    private static final double NODES_MAX_DIST = 300;

    private static final double NODES_MAX_SPREAD = 400;

    private static final double[] ODS_TUNING = new double[] {1.0, 1.0, 1.0};

    /**
     * Update of depth for graph nodes by BFS traversal
     * @param graph graph in operation
     * @param r root id
     */
	public static void updateNodeDepth(iGraph graph, int r)
    {
        List<Integer> layer = new ArrayList<>();
        List<Integer> nextLayer = new ArrayList<>();

        int d = 1;
        layer.add(r);
        while (!layer.isEmpty())
        {
            int finalD = d;
            nextLayer.clear();
            layer.forEach((v) ->
            {
                graph.getNode(v).setDepth(finalD);
                nextLayer.addAll(graph.getNode(v).children());
            });
            layer = new ArrayList<>(nextLayer);
            d++;
        }

    }

    /**
     * Get depth of a node from the graph
     * @param graph graph in operation
     * @param v index of a node
     * @return depth of a node
     */
    public static int getNodeDepth(iGraph graph, int v)
    {
        return graph.getNode(v).depth();
    }

    /**
     * Get number of siblings of a node
     * @param graph graph in operation
     * @param v index of a node
     * @return number of siblings
     */
    public static int getNumSiblings(iGraph graph, int v)
    {
        return graph.getNode(graph.getNode(v).parent()).children().size();
    }

    /**
     * Evaluate subtree configurations of
     * @param graph graph
     * @param root starting from a root
     * @return layout metrics
     */
	public static double[] computeLayoutMetrics(iGraph graph, int root)
    {
        double[] odsWeights = new double[3];
        HashMap<Integer, List<Double>> layerOffset = new HashMap<>();
        HashMap<Integer, List<Double>> layerDist = new HashMap<>();
        HashMap<Integer, List<Double>> layerSpread = new HashMap<>();

        List<Integer> Q = new ArrayList<>();
        Q.add(root);

        while (!Q.isEmpty())
        {
            int n = Q.remove(0);
            iGNode node = graph.getNode(n);
            // if node is a parent: find its configurations
            if (!node.children().isEmpty())
            {
                List<Integer> children = graph.getNode(n).children();
                int depth = graph.getNode(children.get(0)).depth();
                // compute D
                double xDiffMean = 0.0;
                for (Integer child: children)
                {
                    xDiffMean += Math.abs(computeNodeHorizontalDistance(n, child, graph));
                }
                xDiffMean /= children.size();
                double D = (Math.max(0, Math.min(xDiffMean, NODES_MAX_DIST))) / (NODES_MAX_DIST);
                // compute O
                double O;
                if (children.size() == 1)
                {
                    O = DFBoxDrawing.defaultO();
                }
                else
                {
                    iGNode f = graph.getNode(children.get(0));
                    iGNode l = graph.getNode(children.get(children.size()-1));
                    O = ((node.pos().y()+node.height()/2.0) - f.pos().y()) / Math.abs(l.pos().y()+l.height() - f.pos().y());
                    O = Math.max(0.0, Math.min(1.0, O));
                }
                // compute S
                double Smean = 0;
                double S;
                if (children.size() == 1)
                {
                    S = DFBoxDrawing.defaultS();
                }
                else
                {
                    // order children by Y coordinate
                    children.sort((o1, o2) -> (int) (graph.getNode(o1).pos().y() - graph.getNode(o2).pos().y()));
                    for (int i = 0; i < children.size()-1; i++)
                        Smean += abs(computeNodeVerticalDistance(children.get(i), children.get(i+1), graph));
                    Smean /= children.size()-1;
                    S = Math.max(0, Math.min(Smean, NODES_MAX_SPREAD)) / (NODES_MAX_SPREAD);
                }

                addWeight(depth, D, layerDist);
                addWeight(depth, O, layerOffset);
                addWeight(depth, S, layerSpread);

                // Add children to the Q
                Q.addAll(children);
            }
        }

        if (graph.getNode(root).children().isEmpty())
        {
            odsWeights[0] = DFBoxDrawing.defaultO();
            odsWeights[1] = DFBoxDrawing.defaultD();
            odsWeights[2] = DFBoxDrawing.defaultS();
        }
        else
        {
            odsWeights[0] = getAvgWeight(layerOffset) * ODS_TUNING[0];
            odsWeights[1] = getAvgWeight(layerDist) * ODS_TUNING[1];
            odsWeights[2] = getAvgWeight(layerSpread) * ODS_TUNING[2];
        }
        return odsWeights;
    }

	private static void addWeight(int d, double w, HashMap<Integer, List<Double>> weightMap)
    {
        if (!weightMap.containsKey(d)) weightMap.put(d, new ArrayList<>());
        weightMap.get(d).add(w);
    }

	private static double getAvgWeight(HashMap<Integer, List<Double>> weightMap)
    {
        List<Integer> keys = new ArrayList<>(weightMap.keySet());
        double layerWeight = 1.0;
        double avg = 0.0;
        for (int i = 0; i < keys.size(); i++)
        {
            if(keys.size() - i > 1)
                layerWeight /= 2.0;
            double layerAvg = 0.0;
            int k = keys.get(i);
            List<Double> list = weightMap.get(k);
            for (Double aDouble : list)
                layerAvg += aDouble;
            layerAvg /= list.size();

            avg += layerAvg*layerWeight;
        }
        return avg;
    }

    /**
     * A method for finding all paths in a tree graph starting from specified node
     * @param paths list of paths to be filled in
     * @param graph graph in operation
     * @param root starting node
     * @param pprime helper parameter for recursion; provide empty list at the beginning
     */
	public static void findAllPaths(ArrayList<List<Integer>> paths, iGraph graph, int root, List<Integer> pprime)
    {
        // current node
        iGNode node = graph.getNode(root);
        List<Integer> p = new ArrayList<>(pprime);
        p.add(root);
        node.children().forEach(cid ->
        {
            iGNode c = graph.getNode(cid);
            List<Integer> pTemp = new ArrayList<>(p);
            pTemp.add(cid);
            if (c.children().isEmpty() || c.fixed())
            {
                paths.add(pTemp);
            }
            else
            {
                findAllPaths(paths, graph, cid, p);
            }
        });
    }

    /**
     * Method to compute the rectangular area around subtree of specified root
     * @param graph graph in operation
     * @param root starting root
     * @return Rectangle object
     */
	public static Rectangle getSubtreeArea(iGraph graph, int root)
    {
        int ltX = (int) graph.getNode(root).pos().x();
        int ltY = (int) graph.getNode(root).pos().y();
        int rbX = (int) graph.getNode(root).pos().x();
        int rbY = (int) graph.getNode(root).pos().y();

        List<Integer> Q = new ArrayList<>();
        Q.add(root);
        while (!Q.isEmpty())
        {
            int nId = Q.remove(0);
            iGNode node = graph.getNode(nId);
            if (node.pos().x() < ltX) ltX = (int) node.pos().x();
            if (node.pos().x() + node.width() > rbX) rbX = (int) node.pos().x() + node.width();
            if (node.pos().y() < ltY) ltY = (int) node.pos().y();
            if (node.pos().y() + node.height() > rbY) rbY = (int) node.pos().y() + node.height();
            Q.addAll(node.children());
        }

        return new Rectangle(ltX, ltY, rbX-ltX, rbY-ltY);
    }

    /**
     * Compute vertical distance between two nodes
     * @param upper id of upper node
     * @param lower id of lower node
     * @param graph graph in operation
     * @return distance
     */
    public static int computeNodeVerticalDistance(int upper, int lower, iGraph graph)
    {
        iGNode u = graph.getNode(upper);
        iGNode l = graph.getNode(lower);
        int upperHeight = u.fixed() ? GraphRoutines.getSubtreeArea(graph, u.id()).height : u.height();
        return (int) (l.pos().y() - u.pos().y() - upperHeight);
    }

    /**
     * Compute horizontal distance between two nodes
     * @param left id of left node
     * @param right id of right node
     * @param graph graph in operation
     * @return distance
     */
    public static int computeNodeHorizontalDistance(int left, int right, iGraph graph)
    {
        iGNode l = graph.getNode(left);
        iGNode r = graph.getNode(right);
        return (int) (r.pos().x()-(l.pos().x()+l.width()));
    }

    /**
     * Tuning constants for layout metrics
     * @return array of tuning constants in order o,d,s
     */
    public static double[] odsTuning() {return ODS_TUNING;}

    public static double nodesMaxDist() {
        return NODES_MAX_DIST;
    }

    public static double nodesMaxSpread() {
        return NODES_MAX_SPREAD;
    }
}
