package app.display.dialogs.visual_editor.LayoutManagement;


import app.display.dialogs.visual_editor.model.interfaces.iGNode;
import app.display.dialogs.visual_editor.model.interfaces.iGraph;

import java.util.*;

/**
 * Graph manipulation procedures
 * @author nic0gin
 */
public final class GraphRoutines
{
    /**
     * Tuning constants for metric evaluation
     * TODO: find appropriate values
     */
    private static double DM = 3000;
    private static double OM = 150;
    private static double SM = 3500;

    private static final double VISUAL_CONSTANT = 2500;

    /**
     * Update of depth for graph nodes by BFS traversal
     * @param graph
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
            layer.forEach((v) -> {
                graph.getNode(v).setDepth(finalD);
                nextLayer.addAll(graph.getNode(v).children());
            });
            layer = new ArrayList<>(nextLayer);
            d++;
        }

    }

    /**
     * Get depth of a node from the graph
     * @param graph
     * @param v index of a node
     * @return
     */
    public static int getNodeDepth(iGraph graph, int v)
    {
        return graph.getNode(v).depth();
    }

    /**
     * Get index of a node w.r.t. its parent's list
     * @param graph
     * @param v index of a node w.r.t. node list
     * @return
     */
    public static int getChildIndex(iGraph graph, int v)
    {
        return graph.getNode(graph.getNode(v).parent()).children().indexOf(v)+1;
    }

    /**
     * Get number of siblings of a node
     * @param graph
     * @param v index of a node
     * @return
     */
    public static int getNumSiblings(iGraph graph, int v)
    {
        return graph.getNode(graph.getNode(v).parent()).children().size();
    }

    public static List<Integer> getLayerNodes(iGraph graph, int j, int r)
    {
        List<Integer> Q = new ArrayList<>();
        List<Integer> Visited = new ArrayList<>();
        List<Integer> Layer = new ArrayList<>();

        int d = 0;
        Visited.add(r);
        Q.add(r);
        while (!Q.isEmpty())
        {
            int n = Q.remove(0);

            if (d == j) Layer.add(n);
            else
            {
                List<Integer> children = graph.getNode(n).children();
                children.forEach((v) -> {
                    if (!Visited.contains(v))
                    {
                        Q.add(v);
                        Visited.add(v);
                    }
                });
                d++;
            }
        }
        return Layer;
    }

    // TODO: account for node height/width

    /**
     * Evaluate subtree configurations of
     * @param graph graph
     * @param root starting from a root
     * @return
     */
    public static double[] getTreeDOS(iGraph graph, int root)
    {
        double[] DOS_MAP = new double[3];
        HashMap<Integer, List<Double>> layerDist = new HashMap<>();
        HashMap<Integer, List<Double>> layerOffset = new HashMap<>();
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
                // DOS
                List<Integer> children = graph.getNode(n).children();
                int N = children.size();
                double xDiffMean = (children.stream().mapToDouble(id -> graph.getNode(id).pos().getX()).sum() / N) - node.pos().getX();
                double yDiffMean = (children.stream().mapToDouble(id -> graph.getNode(id).pos().getY()).sum() / N) - node.pos().getY();

                int depth = graph.getNode(children.get(0)).depth();
                double D = (Math.max(0, Math.min(xDiffMean, VISUAL_CONSTANT))) / VISUAL_CONSTANT;
                double O = (Math.max(-VISUAL_CONSTANT,Math.min(yDiffMean, VISUAL_CONSTANT)) + VISUAL_CONSTANT) / (2*VISUAL_CONSTANT);

                double Smean = 0;
                // order children by Y coordinate
                children.sort((o1, o2) -> (int) (graph.getNode(o1).pos().getY() - graph.getNode(o2).pos().getY()));
                for (int i = 0; i < children.size() - 1; i++)
                {
                    Smean += Math.abs(graph.getNode(children.get(i)).pos().getY() - graph.getNode(children.get(i+1)).pos().getY());
                }
                double S = Math.max(0, Math.min(Smean, VISUAL_CONSTANT)) / VISUAL_CONSTANT;

                addWeight(depth, D, layerDist); // H
                addWeight(depth, O, layerOffset); // V
                addWeight(depth, S, layerSpread); // S

                // Add children to the Q
                Q.addAll(children);
            }
        }
        DOS_MAP[0] = getAvgWeight(layerDist);
        DOS_MAP[1] = getAvgWeight(layerOffset);
        DOS_MAP[2] = getAvgWeight(layerSpread);
        return DOS_MAP;
    }

    private static void addWeight(int d, double w, HashMap<Integer, List<Double>> weightMap)
    {
        if (!weightMap.containsKey(d)) weightMap.put(d, new ArrayList<>());
        weightMap.get(d).add(w);
    }

    private static double getAvgWeight(HashMap<Integer, List<Double>> weightMap)
    {
        List<Integer> keys = new ArrayList<>(weightMap.keySet());
        double avg = 0.0;
        for (int i = 0; i < keys.size(); i++)
        {
            double localAvg = 0.0;
            int k = keys.get(i);
            List<Double> list = weightMap.get(k);
            for (int j = 0; j < list.size(); j++)
            {
                localAvg += list.get(j);
            }
            localAvg /= list.size();
            // TODO: take a weighted average such that higher layers have more significance
            avg += localAvg;
        }
        return avg / keys.size();
    }


    public static void setDM(double DM) {
        GraphRoutines.DM = DM;
    }

    public static void setOM(double OM) {
        GraphRoutines.OM = OM;
    }

    public static void setSM(double SM) {
        GraphRoutines.SM = SM;
    }

    public static void findAllPaths(ArrayList<List<Integer>> paths, iGraph graph, int root, List<Integer> p)
    {
        // current node
        iGNode node = graph.getNode(root);
        p.add(root);
        node.children().forEach(cid -> {
            iGNode c = graph.getNode(cid);
            List<Integer> pTemp = new ArrayList<>(p);
            pTemp.add(cid);
            if (c.children().isEmpty())
            {
                paths.add(pTemp);
            }
            else
            {
                findAllPaths(paths, graph, cid, p);
            }
        });
    }

}
