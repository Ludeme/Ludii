package app.display.dialogs.visual_editor.LayoutManagement;


import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.model.interfaces.iGNode;
import app.display.dialogs.visual_editor.model.interfaces.iGraph;
import app.display.dialogs.visual_editor.view.panels.IGraphPanel;

import java.awt.*;
import java.util.*;
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
    public static final double VISUAL_CONSTANT = 2500;

    /**
     * Animation updates
     */
    public static final int ANIMATION_UPDATES = 25;

    /**
     * Animation update counter
     */
    public static int updateCounter = 0;

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
     * compute animation nodes increments
     * set nodes positions to the old ones
     * @param graph graph in operation
     * @param r root id
     */
    public static HashMap<Integer, Vector2D> computeNodeIncrements(iGraph graph, int r)
    {
        HashMap<Integer, Vector2D> incrementsMap = new HashMap<>();

        List<Integer> Q = new ArrayList<>();
        Q.add(r);
        while (!Q.isEmpty())
        {
            int nid = Q.remove(0);
            iGNode n = graph.getNode(nid);
            // compute increments
            double incX = (n.newPos().x() - n.oldPos().x()) / ANIMATION_UPDATES;
            double incY = (n.newPos().y() - n.oldPos().y()) / ANIMATION_UPDATES;
            incrementsMap.put(nid, new Vector2D(incX, incY));
            // update pos to oldPos
            n.setPos(new Vector2D(n.oldPos().x(), n.oldPos().y()));
            Q.addAll(n.children());
        }
        return incrementsMap;
    }

    /**
     * compute animation nodes increments
     * set nodes positions to the old ones
     * @param nodes list of nodes
     */
    public static HashMap<Integer, Vector2D> computeNodeIncrements(List<iGNode> nodes)
    {
        HashMap<Integer, Vector2D> incrementsMap = new HashMap<>();

        nodes.forEach(n -> {
            double incX = (n.newPos().x() - n.oldPos().x()) / (ANIMATION_UPDATES-1);
            double incY = (n.newPos().y() - n.oldPos().y()) / (ANIMATION_UPDATES-1);
            incrementsMap.put(n.id(), new Vector2D(incX, incY));
            // update pos to oldPos
            n.setPos(new Vector2D(n.oldPos().x(), n.oldPos().y()));
        });

        return incrementsMap;
    }

    public static boolean animateGraphNodes(iGraph graph, int r, HashMap<Integer, Vector2D> increments)
    {
        List<Integer> Q = new ArrayList<>();
        Q.add(r);
        while (!Q.isEmpty())
        {
            int nid = Q.remove(0);
            iGNode n = graph.getNode(nid);
            n.setPos(new Vector2D(n.oldPos().x()+increments.get(nid).x()*updateCounter,
                    n.oldPos().y()+increments.get(nid).y()*updateCounter));
            Q.addAll(n.children());
        }
        updateCounter++;
        Handler.editorPanel.syncNodePositions();
        return updateCounter == ANIMATION_UPDATES;
    }

    public static boolean animateGraphNodes(List<iGNode> nodes, HashMap<Integer, Vector2D> increments)
    {
        nodes.forEach(n -> n.setPos(new Vector2D(n.oldPos().x()+increments.get(n.id()).x()*updateCounter,
                n.oldPos().y()+increments.get(n.id()).y()*updateCounter)));
        Handler.editorPanel.syncNodePositions();
        updateCounter++;
        return updateCounter == ANIMATION_UPDATES;
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
    public static double[] treeDOS(iGraph graph, int root)
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
                double xDiffMean = (children.stream().mapToDouble(id -> graph.getNode(id).pos().x()).sum() / N) - node.pos().x();
                double yDiffMean = (children.stream().mapToDouble(id -> graph.getNode(id).pos().y()).sum() / N) - node.pos().y();

                int depth = graph.getNode(children.get(0)).depth();
                double D = (Math.max(0, Math.min(xDiffMean, VISUAL_CONSTANT))) / (2*VISUAL_CONSTANT);
                double O = (Math.max(-VISUAL_CONSTANT,Math.min(yDiffMean, VISUAL_CONSTANT)) + VISUAL_CONSTANT) / (4*VISUAL_CONSTANT);

                double Smean = 0;
                // order children by Y coordinate
                children.sort((o1, o2) -> (int) (graph.getNode(o1).pos().y() - graph.getNode(o2).pos().y()));
                for (int i = 0; i < children.size() - 1; i++)
                {
                    Smean += abs(graph.getNode(children.get(i)).pos().y() - graph.getNode(children.get(i+1)).pos().y());
                }
                double S = Math.max(0, Math.min(Smean, VISUAL_CONSTANT)) / (2*VISUAL_CONSTANT);

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
        for (Integer key : keys)
        {
            double localAvg = 0.0;
            int k = key;
            List<Double> list = weightMap.get(k);
            for (Double aDouble : list)
            {
                localAvg += aDouble;
            }
            localAvg /= list.size();
            // TODO: take a weighted average such that higher layers have more significance
            avg += localAvg;
        }
        return avg / keys.size();
    }

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

    public static Rectangle getSubtreeArea(iGraph graph, int root)
    {
        List<iGNode> nodeList = new ArrayList<>();

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

}
