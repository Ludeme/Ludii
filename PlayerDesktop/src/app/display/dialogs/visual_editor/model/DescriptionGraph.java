package app.display.dialogs.visual_editor.model;


import app.display.dialogs.visual_editor.model.interfaces.iGNode;
import app.display.dialogs.visual_editor.model.interfaces.iGraph;
import main.grammar.Symbol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * Graph of LudemeNode objects
 * @author Filipp Dokienko
 */

public class DescriptionGraph implements iGraph {

    //TODO: change to the HashMap
    List<LudemeNode> allLudemeNodes = new ArrayList<>();
    HashMap<Integer, iGNode> nodeMap = new HashMap<>();
    List<Edge> edgeList = new ArrayList<>();
    private boolean isDefine = false;

    LudemeNode ROOT;


    public DescriptionGraph()
    {
    }
    public DescriptionGraph(boolean isDefine)
    {
        this.isDefine = isDefine;
    }

    public DescriptionGraph(LudemeNode root){
        this.ROOT = root;
    }

    @Override
    public void setRoot(iGNode node){
        this.ROOT = (LudemeNode) node;
    }

    @Override
    public iGNode getRoot() {
        return ROOT;
    }

    @Override
    public void setRoot(int id) {
        this.ROOT = getNode(id);
    }

    @Override
    public HashMap<Integer, List<Integer>> getAdjacencyList()
    {
        // TODO remove this
        return null;
    }

    @Override
    public List<Edge> getEdgeList()
    {
        return edgeList;
    }

    @Override
    public HashMap<Integer, iGNode> getNodeList()
    {
        return nodeMap;
    }

    @Override
    public LudemeNode getNode(int id) {
        for(LudemeNode ln : allLudemeNodes){
            if(ln.id() == id) return ln;
        }
        return null;
    }

    @Override
    public int addNode() {
        return 0;
    }

    @Override
    public int addNode(String label) {
        return 0;
    }

    public List<LudemeNode> getNodes() {
        return allLudemeNodes;
    }

    public List<LudemeNode> getNodes(Symbol symbol)
    {
        List<LudemeNode> result = new ArrayList<>();
        for(LudemeNode ln : allLudemeNodes) if(ln.symbol() == symbol) result.add(ln);
        return result;
    }

    public List<LudemeNode> getNodes(String symbolName)
    {
        List<LudemeNode> result = new ArrayList<>();
        for(LudemeNode ln : allLudemeNodes) if(Objects.equals(ln.symbol().name(), symbolName)) result.add(ln);
        return result;
    }

    @Override
    public int addNode(iGNode ludemeNode)
    {
        this.allLudemeNodes.add((LudemeNode) ludemeNode);
        int id = ludemeNode.id();
        nodeMap.put(id, ludemeNode);
        return id;
    }

    @Override
    public int removeNode(iGNode node)
    {
        this.allLudemeNodes.remove((LudemeNode) node);
        nodeMap.remove(node.id());
        return node.id();
    }

    @Override
    public int removeNode(int id)
    {
        iGNode node = getNode(id);
        nodeMap.remove(id);
        this.allLudemeNodes.remove((LudemeNode) node);
        return node.id();
    }

    @Override
    public void addEdge(int from, int to)
    {
        Edge e = new Edge(from, to);
        for(Edge edge : edgeList)
        {
            if(edge.equals(e)) return;
        }
        edgeList.add(new Edge(from , to));
    }

    @Override
    public void removeEdge(int from, int to)
    {
        for(Edge e : edgeList)
        {
            if(e.getNodeA() == from && e.getNodeB() == to)
            {
                edgeList.remove(e);
                return;
            }
        }
    }

    @Override
    public void removeEdge(int containsId) {
        for(Edge e : new ArrayList<>(edgeList))
        {
            if(e.getNodeA() == containsId ||e.getNodeB() == containsId)
            {
                edgeList.remove(e);
            }
        }
    }

    @Override
    public void addEdge(int from, int to, int field)
    {
        Edge e = new Edge(from, to, field);
        for(Edge edge : edgeList)
        {
            if(edge.equals(e)) return;
        }
        edgeList.add(e);
    }

    public void remove(LudemeNode ludemeNode) {
        this.allLudemeNodes.remove(ludemeNode);
    }

    public String toLud() {
        return ROOT.toLud();
    }

    public boolean isDefine()
    {
        return isDefine;
    }

}
