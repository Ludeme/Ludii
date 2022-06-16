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

    LudemeNode ROOT;

    public DescriptionGraph()
    {

    }

    public DescriptionGraph(LudemeNode root){
        this.ROOT = root;
    }

    @Override
    public void setRoot(iGNode node){
        if (!allLudemeNodes.contains((LudemeNode) node)) allLudemeNodes.add((LudemeNode) node);
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

    public List<LudemeNode> getNodes(Symbol symbol) {
        List<LudemeNode> result = new ArrayList<>();
        for(LudemeNode ln : allLudemeNodes) if(ln.symbol() == symbol) result.add(ln);
        return result;
    }

    public List<LudemeNode> getNodes(String symbolName) {
        List<LudemeNode> result = new ArrayList<>();
        for(LudemeNode ln : allLudemeNodes) if(Objects.equals(ln.symbol().name(), symbolName)) result.add(ln);
        return result;
    }

    @Override
    public int addNode(iGNode ludemeNode) {
        this.allLudemeNodes.add((LudemeNode) ludemeNode);
        int id = ludemeNode.id();
        nodeMap.put(id, ludemeNode);
        return id;
    }

    @Override
    public int removeNode(iGNode node) {
        this.allLudemeNodes.remove((LudemeNode) node);
        nodeMap.remove(node.id());
        return node.id();
    }

    @Override
    public int removeNode(int id) {
        iGNode node = getNode(id);
        nodeMap.remove(id);
        this.allLudemeNodes.remove((LudemeNode) node);
        return node.id();
    }

    @Override
    public void addEdge(int from, int to) {
        Edge e = new Edge(from, to);
        for(Edge edge : edgeList) {
            if(edge.equals(e)) return;
        }
        edgeList.add(new Edge(from , to));
    }

    @Override
    public void addEdge(int from, int to, int field) {
        Edge e = new Edge(from, to, field);
        for(Edge edge : edgeList) {
            if(edge.equals(e)) return;
        }
        edgeList.add(new Edge(from , to, field));
    }

    public void remove(LudemeNode ludemeNode) {
        this.allLudemeNodes.remove(ludemeNode);
    }

    public String toLud() {
        return ROOT.stringRepresentation();
    }

    public String toLudCodeCompletion(LudemeNode nodeToMark, int inputIndex, String mark)
    {
        return ROOT.codeCompletionGameDescription(nodeToMark, inputIndex, mark);
    }

    public DescriptionGraph clone(){

        ArrayList<Integer> indeces = new ArrayList<>();
        ArrayList<LudemeNode> from = new ArrayList<>();
        ArrayList<LudemeNode> to = new ArrayList<>();

        DescriptionGraph graphNew = new DescriptionGraph();
        for(LudemeNode node : getNodes()){
            LudemeNode node_new = new LudemeNode(node.symbol(), (int)node.pos().getX(), (int)node.pos().getY());
            node_new.setSelectedClause(node.selectedClause());

            if(to.contains(node)){
                int index = to.indexOf(node);
                int inputIndex = indeces.get(index);
                from.get(index).setProvidedInput(inputIndex, node);

                to.remove(index);
                indeces.remove(index);
                from.remove(index);

            }

            for(int i = 0; i < node.providedInputs().length; i++){
                Object in = node.providedInputs()[i];
                if(in instanceof LudemeNode){
                    indeces.add(i);
                    from.add(node_new);
                    to.add((LudemeNode) in);
                } else {
                    node_new.setProvidedInput(i, in);
                }
            }
        }

        return graphNew;
    }

}
