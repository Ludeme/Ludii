package app.display.dialogs.visual_editor.model;


import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.model.interfaces.iGNode;
import app.display.dialogs.visual_editor.model.interfaces.iGraph;
import main.grammar.Description;
import main.grammar.Symbol;

import java.util.*;

/**
 * Graph of LudemeNode objects
 * @author Filipp Dokienko
 */

public class DescriptionGraph implements iGraph {

    //TODO: change to the HashMap
    List<LudemeNode> allLudemeNodes = new ArrayList<>();
    HashMap<Integer, iGNode> nodeMap = new HashMap<>();
    List<Edge> edgeList = new ArrayList<>();
    private final List<Integer> connectedComponentRoots = new ArrayList<>();
    private int selectedRoot = -1;
    private boolean isDefine = false;
    private String title;
    private Symbol symbol;
    LudemeNode ROOT;


    public DescriptionGraph()
    {
    }
    public DescriptionGraph(String title, boolean isDefine)
    {
        this.title = title;
        this.isDefine = isDefine;
        this.symbol = new Symbol(Symbol.LudemeType.Ludeme, title, title, null);
    }

    public DescriptionGraph(LudemeNode root){
        this.ROOT = root;
    }

    public String title()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public Description description()
    {
        return new Description(ROOT.toLud());
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
    public LudemeNode getNode(int id)
    {
        for(LudemeNode ln : allLudemeNodes){
            if(ln.id() == id) return ln;
        }
        return null;
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
        addConnectedComponentRoot(id);
        return id;
    }

    @Override
    public int removeNode(iGNode node)
    {
        this.allLudemeNodes.remove((LudemeNode) node);
        nodeMap.remove(node.id());
        removeConnectedComponentRoot(node.id());
        return node.id();
    }

    @Override
    public int removeNode(int id)
    {
        iGNode node = getNode(id);
        nodeMap.remove(id);
        this.allLudemeNodes.remove((LudemeNode) node);
        removeConnectedComponentRoot(node.id());
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
        removeConnectedComponentRoot(to);
    }

    @Override
    public void removeEdge(int from, int to)
    {
        for(Edge e : edgeList)
        {
            if(e.getNodeA() == from && e.getNodeB() == to)
            {
                edgeList.remove(e);
                addConnectedComponentRoot(to);
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
                addConnectedComponentRoot(e.getNodeB());
            }
        }
    }

    @Override
    public List<Integer> connectedComponentRoots()
    {
        return connectedComponentRoots;
    }

    @Override
    public void addConnectedComponentRoot(int root)
    {
        if (!connectedComponentRoots.contains(root) && getNode(root) != null) connectedComponentRoots.add(root);
    }

    @Override
    public void removeConnectedComponentRoot(int root)
    {
        if (connectedComponentRoots.contains(root)) connectedComponentRoots.remove((Object)root);
    }

    @Override
    public int selectedRoot()
    {
        return selectedRoot;
    }

    @Override
    public void setSelectedRoot(int root)
    {
        this.selectedRoot = root;
    }

    @Override
    public void setRoot(iGNode root) {
        this.ROOT = (LudemeNode) root;
    }

    @Override
    public iGNode getRoot() {
        return ROOT;
    }

    public void remove(LudemeNode ludemeNode) {
        this.allLudemeNodes.remove(ludemeNode);
    }

    public String toLud()
    {
        if(!isDefine)
            return ROOT.toLud();
        else
            return defineLud();
    }

    private String defineLud()
    {
        String lud = ((LudemeNode) getRoot()).toLud(true);
        int count = 1;
        while(lud.contains("<PARAMETER>"))
        {
            lud = lud.replaceFirst("<PARAMETER>","#"+Integer.toString(count));
            count++;
        }
        return lud;
    }

    public boolean isDefine()
    {
        return isDefine;
    }

    public LudemeNode defineNode()
    {
        assert isDefine;
        LudemeNode lnRoot = (LudemeNode) getRoot();
        if(!lnRoot.isSatisfied())
            return null;
        Map<LudemeNode, List<NodeArgument>> parameters = Handler.defineParameters(this);
        List<NodeArgument> parameterNAs = new ArrayList<>();
        for(List<NodeArgument> lna : parameters.values()) parameterNAs.addAll(lna);
        System.out.println(lnRoot.providedInputsMap().values());
        String t = lnRoot.providedInputsMap().values().iterator().next().toString();
        if(!t.equals(title))
        {
            title = t;
            symbol = new Symbol(Symbol.LudemeType.Ludeme, t, t, null);
        }
        return new LudemeNode(symbol, lnRoot.currentNodeArguments().get(1), parameterNAs, lnRoot.x(), lnRoot.y(), true);
    }

}
