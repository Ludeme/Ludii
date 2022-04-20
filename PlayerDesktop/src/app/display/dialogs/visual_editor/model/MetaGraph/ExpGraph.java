package app.display.dialogs.visual_editor.model.MetaGraph;


import app.display.dialogs.visual_editor.LayoutManagement.GraphDrawing.DrawingFrame;
import app.display.dialogs.visual_editor.LayoutManagement.Math.Vector2D;
import app.display.dialogs.visual_editor.model.Edge;
import app.display.dialogs.visual_editor.model.interfaces.iGNode;
import app.display.dialogs.visual_editor.model.interfaces.iGraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static app.display.dialogs.visual_editor.LayoutManagement.VisualEditor.LayoutConfigs.NODE_SIZE;

/**
 * Experimental graph
 * @author nic0gin
 */

public class ExpGraph implements iGraph {

    private ExpNode root;
    private List<Edge> edgeList;
    private HashMap<Integer, iGNode> nodeList;

    public ExpGraph()
    {
        nodeList = new HashMap<>();
        edgeList = new ArrayList<>();

        //root = new ExpNode("root");
        //nodeList.put(root.getId(), root);
    }

    //### Implementation of interface methods ###

    @Override
    public HashMap<Integer, iGNode> getNodeList() {
        return nodeList;
    }

    public iGNode getNode(int id) {
        return nodeList.get(id);
    }

    @Override
    public iGNode getRoot() {
        return root;
    }

    @Override
    public void setRoot(int id)
    {
        root = (ExpNode) nodeList.get(id);
    }

    @Override
    public void setRoot(iGNode node)
    {
        root = (ExpNode) node;
    }

    @Override
    public HashMap<Integer, List<Integer>> getAdjacencyList() {
        return null;
    }

    @Override
    public List<Edge> getEdgeList() {
        return edgeList;
    }

    @Override
    public void addEdge(int from, int to)
    {
        Edge e = new Edge(from, to);

        ((ExpNode) nodeList.get(from)).addChildNode(to);
        ((ExpNode) nodeList.get(to)).addParent(from);

        edgeList.add(e);
    }

    @Override
    public void addEdge(int from, int to, int field)
    {

    }

    @Override
    public int addNode(String data)
    {
        ExpNode n = new ExpNode(data);
        nodeList.put(n.getId(), n);
        return n.getId();
    }

    @Override
    public int addNode(iGNode node) {
        return 0;
    }

    @Override
    public int removeNode(iGNode node) {
        // TODO: implement
        return 0;
    }

    @Override
    public int removeNode(int id) {
        // TODO: implement
        return 0;
    }

    @Override
    public int addNode()
    {
        ExpNode n = new ExpNode();
        nodeList.put(n.getId(), n);
        return n.getId();
    }

    //### Functionality for the experimental graph panel ###

    public void updateNodePos(Vector2D pos)
    {
        // detect dragged node
        AtomicReference<Double> minDist = new AtomicReference<>(Double.MAX_VALUE);
        AtomicInteger clickedId = new AtomicInteger(0);
        nodeList.forEach((k,v) -> {
            Vector2D delta = pos.sub(v.getPos());
            double dist = delta.euclideanNorm();
            if (dist < NODE_SIZE && dist < minDist.get()) {
                minDist.set(dist);
                clickedId.set(v.getId());
            }
        });

        try {
            nodeList.get(clickedId.get()).setPos(pos);
        }
        catch (NullPointerException ignored)
        {

        }

    }

    public void removeNode(Vector2D pos)
    {
        // detect clicked node
        AtomicReference<Double> minDist = new AtomicReference<>(Double.MAX_VALUE);
        AtomicInteger clickedId = new AtomicInteger(0);
        nodeList.forEach((k,v) -> {
            Vector2D delta = pos.sub(v.getPos());
            double dist = delta.euclideanNorm();
            if (dist < NODE_SIZE && dist < minDist.get()) {
                minDist.set(dist);
                clickedId.set(v.getId());
            }
        });

        // remove edges
        edgeList.removeIf(e -> (e.getNodeA() == clickedId.get()) || (e.getNodeB() == clickedId.get()));

        // remove vertex
        nodeList.remove(clickedId.get());

    }

    public void randomizeNodePos() {
        nodeList.forEach((k, n)-> n.setPos(DrawingFrame.getRandomScreenPos()));
    }
}
