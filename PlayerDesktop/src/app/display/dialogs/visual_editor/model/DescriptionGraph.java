package app.display.dialogs.visual_editor.model;


import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.model.interfaces.iGNode;
import app.display.dialogs.visual_editor.model.interfaces.iGraph;
import main.grammar.Description;
import main.grammar.Symbol;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * Graph of LudemeNode objects
 * @author Filipp Dokienko
 */

public class DescriptionGraph implements iGraph
{
    /** The Root node of this graph */
    private LudemeNode ROOT;
    /** All nodes this graph contains keyed by their id */
    private final HashMap<Integer, iGNode> nodeMap = new HashMap<>();
    /** List of all LudemeNodes this graph contains */
    private final List<LudemeNode> allLudemeNodes = new ArrayList<>();
    /** List of all Edges this graph contains */
    private final List<Edge> edgeList = new ArrayList<>();

    private final List<Integer> connectedComponentRoots = new ArrayList<>();
    private int selectedRoot = -1;

    /** Whether this graph is a define */
    private boolean isDefine = false;
    /** The title of the define */
    private String title;
    /** The symbol of the define */
    private Symbol defineSymbol;
    /** The macro node of the define */
    private LudemeNode defineMacroNode;
    /** The parameters of the define */
    private List<NodeArgument> defineParameters = new ArrayList<>();
    /** The define node to be used in other graphs, to access the define */
    private LudemeNode defineNode;


    /**
     * Constructor. Used for non-define graphs without a predefined root node
     */
    public DescriptionGraph()
    {

    }

    /**
     * Constructor for a define graph.
     * @param title Initial title of the define
     * @param isDefine Whether this graph is a define
     */
    public DescriptionGraph(String title, boolean isDefine)
    {
        assert isDefine;
        this.title = title;
        this.isDefine = true;
        this.defineSymbol = new Symbol(Symbol.LudemeType.Ludeme, title, title, null);
    }

    /**
     * Constructor for a non-define graph with a predefined root node
     * @param root The root node
     */
    public DescriptionGraph(LudemeNode root)
    {
        this.ROOT = root;
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
        return (LudemeNode) nodeMap.get(id);
    }

    public List<LudemeNode> getNodes()
    {
        return allLudemeNodes;
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
        this.allLudemeNodes.remove(node);
        removeConnectedComponentRoot(node.id());
        return node.id();
    }

    @Override
    public void addEdge(int from, int to)
    {
        Edge e = new Edge(from, to);
        for(Edge edge : edgeList)
            if(edge.equals(e))
                return;
        edgeList.add(new Edge(from , to));
        removeConnectedComponentRoot(to);
    }

    @Override
    public void removeEdge(int from, int to)
    {
        for(Edge e : edgeList)
            if(e.getNodeA() == from && e.getNodeB() == to)
            {
                edgeList.remove(e);
                addConnectedComponentRoot(to);
                return;
            }
    }

    @Override
    public void removeEdge(int containsId)
    {
        for(Edge e : new ArrayList<>(edgeList))
            if(e.getNodeA() == containsId ||e.getNodeB() == containsId)
            {
                edgeList.remove(e);
                addConnectedComponentRoot(e.getNodeB());
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
        if (!connectedComponentRoots.contains(root) && getNode(root) != null)
            connectedComponentRoots.add(root);
    }

    @Override
    public void removeConnectedComponentRoot(int root)
    {
        if (connectedComponentRoots.contains(root))
            connectedComponentRoots.remove((Object)root);
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
    public void setRoot(iGNode root)
    {
        this.ROOT = (LudemeNode) root;
    }

    @Override
    public iGNode getRoot()
    {
        return ROOT;
    }

    public LudemeNode rootLudemeNode()
    {
        return (LudemeNode) ROOT;
    }

    public void remove(LudemeNode ludemeNode)
    {
        this.allLudemeNodes.remove(ludemeNode);
    }

    /**
     * Converts the graph to a .lud String
     * @return The .lud equivalent of the graph
     */
    public String toLud()
    {
        if(!isDefine)
            return ROOT.toLud();
        else
            return defineLud();
    }


    // Methods for define graphs


    /**
     * Converts a define graph to a .lud String.
     * Replaces unprovided, but required arguments, indicated by " <PARAMETER> " with #[count]
     * @return The .lud equivalent of the define graph
     */
    private String defineLud()
    {
        String lud = ((LudemeNode) getRoot()).toLud(true);
        int count = 1;
        while(lud.contains("<PARAMETER>"))
        {
            lud = lud.replaceFirst("<PARAMETER>","#"+count);
            count++;
        }
        return lud;
    }

    /**
     *
     * @return Whether this is a define graph
     */
    public boolean isDefine()
    {
        return isDefine;
    }

    /**
     *
     * @return The title of the define
     */
    public String title()
    {
        return title;
    }

    /**
     * Changes the title of the define
     * @param title The new title
     */
    public void setTitle(String title)
    {
        if(!title.equals(this.title))
        {
            this.title = title;
            // update the symbol
            this.defineSymbol = new Symbol(Symbol.LudemeType.Ludeme, title, title, null);
            // TODO: Notify about change
            System.out.println("[NOTIFY] Title changed");
        }
    }

    /**
     *
     * @return The symbol of the define
     */
    public Symbol defineSymbol()
    {
        return defineSymbol;
    }

    /**
     *
     * @return The macro node (root of the define (excluding the define node itself)) of the define
     */
    public LudemeNode defineMacroNode()
    {
        return defineMacroNode;
    }

    /**
     *
     * @return The current macro node of the define
     */
    public LudemeNode computeDefineMacroNode()
    {
        return (LudemeNode) rootLudemeNode().providedInputsMap().get(rootLudemeNode().currentNodeArguments().get(1));
    }

    /**
     * Updates the field of the define macro node
     */
    public void updateMacroNode()
    {
        // if the macro node has changed, update it and notify about change
        if(defineMacroNode != computeDefineMacroNode())
        {
            defineMacroNode = computeDefineMacroNode();
            System.out.println("[NOTIFY] Macro node changed to " + defineMacroNode.title());
            // TODO: Notify
        }
    }

    public List<NodeArgument> parameters()
    {
        return defineParameters;
    }

    /**
     * Computes the required arguments of the define
     * @return List of NodeArguments that require an Input
     */
    public List<NodeArgument> computeDefineParameters()
    {
        assert isDefine;
        List<NodeArgument> parameters = new ArrayList<>();
        LudemeNode root = (LudemeNode) getRoot();
        // The node must be satisfied (means all inputs filled)
        if(!root.isSatisfied())
            return parameters;

        for(LudemeNode node : allLudemeNodes)
            parameters.addAll(node.unfilledRequiredArguments());

        return parameters;
    }

    /**
     * Updates the current parameters of the define.
     * Returns which NodeArguments were added/removed from the parameters (of a define node)
     * @return An array of two List<NodeArgument>. The first list contains newly added NodeArguments, the second list contains removed NodeArguments.
     */
    public Object[] changedParameters(List<NodeArgument> parameters)
    {
        assert isDefine;
        if(this.defineParameters.isEmpty())
        {
            this.defineParameters = parameters;
            return new Object[]{new ArrayList<>(), new ArrayList<>()};
        }
        List<NodeArgument> added = new ArrayList<>();
        List<NodeArgument> removed = new ArrayList<>(this.defineParameters);

        for(NodeArgument na : parameters)
            if(!parameters().contains(na))
                added.add(na);
            else
                removed.remove(na);

        this.defineParameters = parameters;

        if(!added.isEmpty() || !removed.isEmpty())
        {
            System.out.println("[NOTIFY] Parameters changed");
            // TODO: Notify about change
        }

        return new Object[]{added, removed};
    }

    /**
     *
     * @return The node of the define, used in other graphs
     */
    public LudemeNode defineNode()
    {
        assert isDefine;
        if(defineNode == null)
            defineNode = new LudemeNode(defineSymbol, defineMacroNode(), this, parameters(), rootLudemeNode().x(), rootLudemeNode().y(), true);
        return defineNode;
    }

}
