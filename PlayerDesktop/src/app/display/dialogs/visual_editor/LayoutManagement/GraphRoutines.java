package app.display.dialogs.visual_editor.LayoutManagement;


import app.display.dialogs.visual_editor.model.interfaces.iGNode;
import app.display.dialogs.visual_editor.model.interfaces.iGraph;
import game.rules.play.moves.nonDecision.effect.requirement.Do;
import game.util.graph.Graph;
import org.junit.Ignore;

import java.util.*;

/**
 * Graph manipulation procedures
 * @author nic0gin
 */
public final class GraphRoutines
{
    private static double DM = 3000;
    private static double OM = 150;
    private static double SM = 3500;


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
                nextLayer.addAll(graph.getNode(v).getChildren());
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
        return graph.getNode(v).getDepth();
    }

    /**
     * Get index of a node w.r.t. its parent's list
     * @param graph
     * @param v index of a node w.r.t. node list
     * @return
     */
    public static int getChildIndex(iGraph graph, int v)
    {
        return graph.getNode(graph.getNode(v).getParent()).getChildren().indexOf(v)+1;
    }

    /**
     * Get number of siblings of a node
     * @param graph
     * @param v index of a node
     * @return
     */
    public static int getNumSiblings(iGraph graph, int v)
    {
        return graph.getNode(graph.getNode(v).getParent()).getChildren().size();
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
                List<Integer> children = graph.getNode(n).getChildren();
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
     * @return HashMap of indices and configurations
     */
    public static HashMap<Integer, Double[]> getSubtreeDOS(iGraph graph, int root)
    {
        HashMap<Integer, Double[]> DOS_MAP = new HashMap<>();

        List<Integer> Q = new ArrayList<>();
        Q.add(root);

        while (!Q.isEmpty())
        {
            int n = Q.remove(0);
            iGNode node = graph.getNode(n);
            // if node is a parent: find its configurations
            if (!node.getChildren().isEmpty())
            {
                // DOS
                List<Integer> children = graph.getNode(n).getChildren();
                int N = children.size();
                double Xdiffmean;
                double Ydiffmean;
                Xdiffmean = children.stream().mapToDouble(id -> Math.abs(node.getPos().getX() - graph.getNode(id).getPos().getX())).sum();
                Ydiffmean = children.stream().mapToDouble(id -> node.getPos().getY() - graph.getNode(id).getPos().getY()).sum();

                Xdiffmean /= N;
                Ydiffmean /= N;

                double Smean = 0;
                // order children by Y coordinate
                Collections.sort(children, new Comparator<Integer>() {
                    @Override
                    public int compare(Integer o1, Integer o2) {
                        return (int)(graph.getNode(o1).getPos().getY() - graph.getNode(o2).getPos().getY());
                    }
                });
                for (int i = 0; i < children.size() - 1; i++) {
                    Smean += Math.abs(graph.getNode(children.get(i)).getPos().getY() - graph.getNode(children.get(i+1)).getPos().getY());
                }
                Smean /= N;

                double D = Math.max(0.0, Math.min(1.0, Xdiffmean/DM));
                double O = Math.max(-1.0, Math.min(1.0, Ydiffmean/OM));
                double S = Math.max(0.0, Math.min(1.0, Smean/SM));

                DOS_MAP.put(n, new Double[]{D,O,S});
                // Add children to the Q
                Q.addAll(children);
            }
        }
        return DOS_MAP;
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

    public static String repeatString(String str, int n)
    {
        String repStr = ""; // if error occur change to String repStr = str;
        for (int i = 0; i < n; i++) {
            repStr = repStr.concat(str);
        }
        return repStr;
    }
}
