package app.display.dialogs.visual_editor.handler;

import app.display.dialogs.visual_editor.model.DescriptionGraph;
import app.display.dialogs.visual_editor.model.Edge;
import app.display.dialogs.visual_editor.model.LudemeNode;
import app.display.dialogs.visual_editor.model.NodeArgument;
import app.display.dialogs.visual_editor.model.UserActions.*;
import app.display.dialogs.visual_editor.recs.codecompletion.Ludeme;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.LudemeNodeComponent;
import app.display.dialogs.visual_editor.view.panels.IGraphPanel;
import app.display.dialogs.visual_editor.view.panels.MainPanel;
import app.display.dialogs.visual_editor.view.panels.editor.EditorPanel;
import app.display.dialogs.visual_editor.view.panels.editor.tabPanels.LayoutSettingsPanel;
import app.display.dialogs.visual_editor.view.panels.header.ToolsPanel;
import main.grammar.Clause;
import main.grammar.Symbol;

import javax.swing.*;
import java.util.*;
import java.awt.*;
import java.util.List;

public class Handler
{
    public static DescriptionGraph gameDescriptionGraph;

    // Single EditorPanel
    public static EditorPanel editorPanel;

    // Single ToolsPanel
    public static ToolsPanel toolsPanel;

    public static LayoutSettingsPanel lsPanel;

    public static MainPanel mainPanel;

    private static Stack<IUserAction> performedUserActions = new Stack<>();
    private static Stack<IUserAction> undoneUserActions = new Stack<>();
    public static IUserAction currentUndoAction;
    public static IUserAction currentRedoAction;
    public static boolean recordUserActions = true;

    private static HashMap<DescriptionGraph, IGraphPanel> graphPanelMap = new HashMap<>();

    private static final boolean DEBUG = true;


    /**
     * Adds a node to the graph
     * @param graph graph in operation
     * @param node node to be added
     */
    public static void addNode(DescriptionGraph graph, LudemeNode node )
    {
        addNode(graph, node, false);
    }

    /**
     * Adds a node to the graph
     * @param graph graph in operation
     * @param node node to be added
     * @param connect Whether the node will be connected after insertion
     */
    public static void addNode(DescriptionGraph graph, LudemeNode node, boolean connect)
    {
        if(DEBUG) System.out.println("[HANDLER] addNode(graph. node, connect) Adding node: " + node.title());
        if(graph.getNodes().isEmpty()) graph.setRoot(node);
        graph.addNode(node);
        // notify graph panel
        IGraphPanel graphPanel = graphPanelMap.get(graph);
        addAction(new AddedNodeAction(graphPanel, node));
        graphPanel.notifyNodeAdded(node, connect);
        if(recordUserActions && node.parentNode() != null && (node.parentNode().providedInputsMap().get(node.creatorArgument()) instanceof Object[]))
        {
            Object[] collectionInput = (Object[]) node.parentNode().providedInputsMap().get(node.creatorArgument());
            int elementIndex = Arrays.asList(collectionInput).indexOf(node);
            ((AddedNodeAction) performedUserActions.peek()).setCollectionIndex(elementIndex);
        }
    }

    /**
     * Creates and adds a node to the graph
     * @param graph Graph to add the node to
     * @param symbol The symbol of the node to be created
     * @param nodeArgument The NodeArgument which created this node
     * @param x The x-position of the node
     * @param y The y-position of the node
     * @param connect Whether the node will be connected after insertion
     * @return The created node
     */
    public static LudemeNode addNode(DescriptionGraph graph, Symbol symbol, NodeArgument nodeArgument, int x, int y, boolean connect)
    {
        LudemeNode node = new LudemeNode(symbol, nodeArgument, x, y);
        addNode(graph, node, connect);
        return node;
    }

    /**
     * Removes a node from the graph.
     * @param graph The graph that contains the node.
     * @param node The node to remove.
     */
    public static void removeNode(DescriptionGraph graph, LudemeNode node)
    {
        if(graph.getRoot() == node) return;
        if(DEBUG) System.out.println("[HANDLER] removeNode(graph, node) -> Removing node: " + node.title());

        IUserAction action = new RemovedNodeAction(graphPanelMap.get(graph), node);
        addAction(action);
        if(performedUserActions.peek() == action) Handler.recordUserActions = false;
        // if the action is added, and the node was part of a collection, notify the action about the collection element index
        if(recordUserActions && node.parentNode() != null && (node.parentNode().providedInputsMap().get(node.creatorArgument()) instanceof Object[]))
        {
            Object[] collectionInput = (Object[]) node.parentNode().providedInputsMap().get(node.creatorArgument());
            int elementIndex = Arrays.asList(collectionInput).indexOf(node);
            ((RemovedNodeAction) performedUserActions.peek()).setCollectionIndex(elementIndex);
        }

        // Remove the node from the graph
        graph.removeNode(node);
        // notify its children that it's parent is null
        node.childrenNodes().forEach(child -> child.setParent(null));
        // notify its parent that it's child is null
        if(node.parentNode() != null) node.parentNode().removeChildren(node);
        // reset the parent's inputs
        if(node.parentNode() != null)
        {
            List<NodeArgument> args = new ArrayList<>(node.parentNode().providedInputsMap().keySet());
            List<Object> inputs = new ArrayList<>(node.parentNode().providedInputsMap().values());
            int index2 = inputs.indexOf(node);
            if(index2>=0 && !(node.parentNode().providedInputsMap().get(args.get(index2)) instanceof Object[]))
                Handler.updateInput(graph, node.parentNode(), args.get(index2), null); // TODO: what about collection?
        }
        // remove edge
        // TODO: ConenctionHandler
        graph.removeEdge(node.id());
        // notify graph panel
        IGraphPanel graphPanel = graphPanelMap.get(graph);
        graphPanel.notifyNodeRemoved(graphPanel.nodeComponent(node));
        if(performedUserActions.peek() == action) Handler.recordUserActions = true;
    }

    /**
     * Removes a list of nodes from the graph
     * @param graph
     * @param nodes
     */
    public static void removeNodes(DescriptionGraph graph, List<LudemeNode> nodes)
    {
        if(DEBUG) System.out.println("[HANDLER] removeNodes(graph, nodes) -> Removing nodes: " + nodes.size());

        // remove root node
        for(LudemeNode node : nodes)
        {
            if(graph.getRoot() == node)
            {
                nodes.remove(node);
                break;
            }
        }

        IUserAction action = new RemovedNodesAction(graphPanelMap.get(graph), nodes);
        addAction(action);

        if(performedUserActions.peek() == action) Handler.recordUserActions = false;
        for(LudemeNode n : nodes) removeNode(graph, n);
        if(performedUserActions.peek() == action) Handler.recordUserActions = true;
    }

    public static void remove(DescriptionGraph graph)
    {
        IGraphPanel graphPanel = graphPanelMap.get(graph);
        List<LudemeNode> selectedNodes = selectedNodes(graph);
        if(selectedNodes.isEmpty()) return;
        if(selectedNodes.size() == 1)
        {
            removeNode(graph, selectedNodes.get(0));
        }
        else
        {
            removeNodes(graph, selectedNodes);
        }
    }

    /**
     * Adds and edge between two nodes.
     * @param graph The graph that contains the nodes.
     * @param from The node that the edge starts from.
     * @param to The node that the edge ends at.
     * @param nodeArgument The nodeArgument of the field
     */
    public static void addEdge(DescriptionGraph graph, LudemeNode from, LudemeNode to, NodeArgument nodeArgument){
        // check whether the edge already exists
        for(Edge e : graph.getEdgeList()) if(e.getNodeA() == from.id() && e.getNodeB() == to.id()) return;
        if(DEBUG) System.out.println("[HANDLER] addEdge(graph, form, to, nodeArgument) -> Adding edge: " + from.title() + " -> " + to.title());
        graph.addEdge(from.id(), to.id());
        // here form is the parent node
        from.addChildren(to);
        to.setParent(from);

        // notify graph panel to draw edge
        IGraphPanel graphPanel = graphPanelMap.get(graph);
        graphPanel.notifyEdgeAdded(graphPanel.nodeComponent(from), graphPanel.nodeComponent(to), nodeArgument);
    }

    /**
     * Adds and edge between two nodes.
     * @param graph The graph that contains the nodes.
     * @param from The node that the edge starts from.
     * @param to The node that the edge ends at.
     * @param nodeArgument The nodeArgument of the field
     * @param elementIndex If the edge is part of a collection, the index of the element in the collection
     */
    public static void addEdge(DescriptionGraph graph, LudemeNode from, LudemeNode to, NodeArgument nodeArgument, int elementIndex){
        // check whether the edge already exists
        for(Edge e : graph.getEdgeList()) if(e.getNodeA() == from.id() && e.getNodeB() == to.id()) return;
        if(DEBUG) System.out.println("[HANDLER] addEdge(graph, form, to, nodeArgument, elementIndex) -> Adding edge: " + from.title() + " -> " + to.title() + ", elementIndex: " + elementIndex);
        graph.addEdge(from.id(), to.id());
        // here form is the parent node
        from.addChildren(to);
        to.setParent(from);

        // if the edge is part of a collection, adjust the collection size
        while(elementIndex+1>((Object[])from.providedInputsMap().get(nodeArgument)).length)
        {
            addCollectionElement(graph, from, nodeArgument);
        }

        // notify graph panel to draw edge
        IGraphPanel graphPanel = graphPanelMap.get(graph);
        graphPanel.notifyEdgeAdded(graphPanel.nodeComponent(from), graphPanel.nodeComponent(to), nodeArgument, elementIndex);
    }

    /**
     * Adds and edge between two nodes.
     * @param graph The graph that contains the nodes.
     * @param from The node that the edge starts from.
     * @param to The node that the edge ends at.
     * @param inputFieldIndex The index of the inputfield where the connection stems from
     */
    public static void addEdge(DescriptionGraph graph, LudemeNode from, LudemeNode to, int inputFieldIndex){
        // check whether the edge already exists
        for(Edge e : graph.getEdgeList()) if(e.getNodeA() == from.id() && e.getNodeB() == to.id()) return;

        if(DEBUG) System.out.println("[HANDLER] nodeArgument(graph, form, to, inputFieldIndex) -> Adding edge: " + from.title() + " -> " + to.title() + ", inputFieldIndex: " + inputFieldIndex);

        graph.addEdge(from.id(), to.id());
        // here form is the parent node
        from.addChildren(to);
        to.setParent(from);

        // notify graph panel to draw edge
        IGraphPanel graphPanel = graphPanelMap.get(graph);
        graphPanel.notifyEdgeAdded(graphPanel.nodeComponent(from), graphPanel.nodeComponent(to), inputFieldIndex);
    }

    public static void removeEdge(DescriptionGraph graph, LudemeNode from, LudemeNode to){
        if(DEBUG) System.out.println("[HANDLER] removeEdge(graph, from, to) -> Removing edge: " + from.title() + " -> " + to.title());
        graph.removeEdge(from.id(), to.id());
        from.removeChildren(to);
        to.setParent(null);
        IGraphPanel graphPanel = graphPanelMap.get(graph);
        graphPanel.notifyEdgeRemoved(graphPanel.nodeComponent(from), graphPanel.nodeComponent(to));
    }

    public static void removeEdge(DescriptionGraph graph, LudemeNode from, LudemeNode to, int elementIndex){
        if(DEBUG) System.out.println("[HANDLER] removeEdge(graph, from, to, elementIndex) -> Removing edge: " + from.title() + " -> " + to.title());
        graph.removeEdge(from.id(), to.id());
        from.removeChildren(to);
        to.setParent(null);
        IGraphPanel graphPanel = graphPanelMap.get(graph);
        graphPanel.notifyEdgeRemoved(graphPanel.nodeComponent(from), graphPanel.nodeComponent(to), elementIndex);
    }

    /**
     * Updates the current clause of a node.
     */
    public static void updateCurrentClause(DescriptionGraph graph, LudemeNode node, Clause c)
    {
        Clause oldClause = node.selectedClause();
        if(oldClause == c) return;
        IGraphPanel graphPanel = graphPanelMap.get(graph);
        IUserAction action = new ChangedClauseAction(graphPanel, node, oldClause, c);
        addAction(action);
        // remove all edges from the node
        for(Object input : node.providedInputsMap().values())
            if(input instanceof LudemeNode)
            {
                if(!performedUserActions.isEmpty() && performedUserActions.peek() == action)
                {
                    Handler.recordUserActions = false;
                    removeEdge(graph, node, (LudemeNode) input);
                    Handler.recordUserActions = true;
                }
            }
        if(c.args() == null) node.setSelectedClause(c.symbol().rule().rhs().get(0));
        else node.setSelectedClause(c);
        graphPanel.notifySelectedClauseChanged(graphPanel.nodeComponent(node), c);}

    /**
     * Assigns a IGraphPanel to a DescriptionGraph
     * @param graph graph to be assigned
     * @param graphPanel graph panel
     */
    public static void addGraphPanel(DescriptionGraph graph, IGraphPanel graphPanel)
    {
        if(!graphPanelMap.containsKey(graph)) {
            graphPanelMap.put(graph, graphPanel);
        }
    }

    /**
     * Updates the input of a node.
     * @param graph The graph that contains the node.
     * @param node The node to update.
     * @param nodeArgument The argument of the node to update.
     * @param input The new input.
     */
    public static void updateInput(DescriptionGraph graph, LudemeNode node, NodeArgument nodeArgument, Object input)
    {
        if(DEBUG) System.out.println("[HANDLER] updateInput(graph, node, nodeArgument, input) -> Updating input: " + node.title() + ", " + nodeArgument + " -> " + input);
        IGraphPanel graphPanel = graphPanelMap.get(graph);
        if(input instanceof LudemeNode)
        {
            // if the input does not originate from a node creation, record the adding of the edge
            IUserAction lastAction = performedUserActions.peek();
            if(lastAction instanceof AddedNodeAction)
            {
                if(!(((AddedNodeAction) lastAction).addedNode() == input && ((AddedNodeAction) lastAction).addedNode().creatorArgument() == nodeArgument))
                {
                    addAction(new AddedConnectionAction(graphPanel, node, (LudemeNode) input, nodeArgument));
                }
            }
            else {
                addAction(new AddedConnectionAction(graphPanel, node, (LudemeNode) input, nodeArgument));
            }
        }
        if(input == null)
        {
            Object oldInput = node.providedInputsMap().get(nodeArgument);
            if(oldInput instanceof LudemeNode)
            {
                LudemeNode previouslyConnectedNode = (LudemeNode) oldInput;
                addAction(new RemovedConnectionAction(graphPanelMap.get(graph), node, previouslyConnectedNode, nodeArgument));
            }
        }
        // if the input is null but was a node before, remove the child from the parent
        if(input == null && node.providedInputsMap().get(nodeArgument) instanceof LudemeNode)
        {
            node.removeChildren((LudemeNode) node.providedInputsMap().get(nodeArgument));
        }
        if(input instanceof Object[]) System.out.println(Arrays.toString((Object[]) input));
        node.setProvidedInput(nodeArgument, input);
    }

    /**
     * Adds an empty collection input to collection of a node
     * @param graph The graph that contains the node.
     * @param node  The node to update.
     * @param nodeArgument The argument of the node to update, which is a collection.
     */
    public static void addCollectionElement(DescriptionGraph graph, LudemeNode node, NodeArgument nodeArgument)
    {
        if(DEBUG) System.out.println("[HANDLER] addCollectionElement(graph, node, nodeArgument) Adding collection element of " + node.title() + ", " + nodeArgument);

        IGraphPanel graphPanel = graphPanelMap.get(graph);

        Object[] oldCollection = (Object[]) node.providedInputsMap().get(nodeArgument);

        if(oldCollection == null)
        {
            IUserAction action = new AddedCollectionAction(graphPanel, node, nodeArgument, -404, 1, null);
            addAction(action);
            if(performedUserActions.peek() == action)
                Handler.recordUserActions = false;
            updateInput(graph, node, nodeArgument, new Object[2]);
            if(performedUserActions.peek() == action)
                Handler.recordUserActions = true;
            graphPanel.notifyCollectionAdded(graphPanel.nodeComponent(node), nodeArgument, 1);
            return;
        }
        Object[] newCollection = new Object[oldCollection.length + 1];
        System.arraycopy(oldCollection, 0, newCollection, 0, oldCollection.length);
        IUserAction action = new AddedCollectionAction(graphPanel, node, nodeArgument, -404, oldCollection.length, null);
        addAction(action);
        if(!performedUserActions.isEmpty() && performedUserActions.peek() == action)
            Handler.recordUserActions = false;
        updateInput(graph, node, nodeArgument, newCollection);
        if(!performedUserActions.isEmpty() && performedUserActions.peek() == action)
            Handler.recordUserActions = true;

        graphPanel.notifyCollectionAdded(graphPanel.nodeComponent(node), nodeArgument, newCollection.length - 1);
    }



    /**
     * Updates the input of a collection field
     * @param graph
     * @param node
     * @param nodeArgument
     * @param input
     * @param elementIndex The index of the element within the collection
     */
    public static void updateCollectionInput(DescriptionGraph graph, LudemeNode node, NodeArgument nodeArgument, Object input, int elementIndex)
    {
        if(DEBUG) System.out.println("[HANDLER] updateCollectionInput(graph, node, nodeArgument, input, elementIndex) Updating collection input of " + node.title() + ", " + nodeArgument + ", elementIndex: " + elementIndex + " to " + input);
        if(node.providedInputsMap().get(nodeArgument) == null)
        {
            node.setProvidedInput(nodeArgument, new Object[1]);
        }

        if(input == null && elementIndex >= ((Object[])(node.providedInputsMap().get(nodeArgument))).length) return;

        while(elementIndex >= ((Object[])(node.providedInputsMap().get(nodeArgument))).length) addCollectionElement(graph, node, nodeArgument);
        Object[] in = (Object[]) node.providedInputsMap().get(nodeArgument);
        in[elementIndex] = input;
        Handler.updateInput(graph, node, nodeArgument, in);
    }


    /**
     * if a collection element was removed, update the provided input array
     * @param graph
     * @param node
     * @param nodeArgument
     * @param elementIndex Index of collection element
     */
    public static void removeCollectionElement(DescriptionGraph graph, LudemeNode node, NodeArgument nodeArgument, int elementIndex)
    {
        if(DEBUG) System.out.println("[HANDLER] removeCollectionElement(graph, node, nodeArgument, elementIndex) Removing collection element of " + node.title() + ", " + nodeArgument + ", elementIndex: " + elementIndex);
        Object[] oldCollection = (Object[]) node.providedInputsMap().get(nodeArgument);
        if(oldCollection == null) return;

        // get input
        Object input = oldCollection[elementIndex];

        IUserAction action = new RemovedCollectionAction(graphPanelMap.get(graph), node, nodeArgument, elementIndex, input);
        addAction(action);

        Object[] newCollection = new Object[oldCollection.length - 1];

        if(currentUndoAction instanceof AddedCollectionAction)
        {
            if(((AddedCollectionAction) currentUndoAction).isUpdated(node, nodeArgument, elementIndex))
            {
                ((AddedCollectionAction) currentUndoAction).setInput(input);
            }
        }

        for(int i = 0; i < elementIndex; i++)
        {
            newCollection[i] = oldCollection[i];
        }
        for(int i = elementIndex + 1; i < oldCollection.length; i++)
        {
            newCollection[i - 1] = oldCollection[i];
        }

        if(!performedUserActions.isEmpty() && performedUserActions.peek() == action)
            Handler.recordUserActions = false;
        updateInput(graph, node, nodeArgument, newCollection);
        if(input instanceof LudemeNode)
            removeEdge(graph, node, (LudemeNode) input, elementIndex);
        if(!performedUserActions.isEmpty() && performedUserActions.peek() == action)
            Handler.recordUserActions = true;
        IGraphPanel graphPanel = graphPanelMap.get(graph);
        graphPanel.notifyCollectionRemoved(graphPanel.nodeComponent(node), nodeArgument, elementIndex);
    }


    private static List<LudemeNode> currentCopy = new ArrayList<LudemeNode>(); // current copy


    public static void copy(DescriptionGraph graph)
    {
        IGraphPanel graphPanel = graphPanelMap.get(graph);
        List<LudemeNode> toCopy = new ArrayList<>();
        for(LudemeNodeComponent lnc : graphPanel.selectedLnc())
        {
            if(graph.getRoot() == lnc.node()) continue;
            toCopy.add(lnc.node());
        }
        copy(graph, toCopy);
    }

    public static void copy(DescriptionGraph graph, List<LudemeNode> nodes)
    {
        if(nodes.isEmpty()) return;
        if(DEBUG) System.out.println("[HANDLER] copy(graph, nodes) Copying " + nodes.size() + " nodes");
        currentCopy.clear();
        IGraphPanel graphPanel = graphPanelMap.get(graph);

        HashMap<LudemeNode, LudemeNode> copiedNodes = new HashMap<>(); // <original, copy>

        // create copies
        for(LudemeNode node : nodes) copiedNodes.put(node, node.copy());
        // fill inputs (connections and collections)
        for(LudemeNode node : nodes) {
            LudemeNode copy = copiedNodes.get(node);
            // iterate each original node's provided inputs
            for (NodeArgument arg : node.providedInputsMap().keySet()) {
                Object input = node.providedInputsMap().get(arg);
                // input is a node
                if (input instanceof LudemeNode) {
                    LudemeNode inputNode = (LudemeNode) input;
                    // if input node is in the list of nodes to copy, copy it
                    if (nodes.contains(inputNode)) {
                        LudemeNode inputNodeCopy = copiedNodes.get(inputNode);
                        copy.setProvidedInput(arg, inputNodeCopy);
                        copy.addChildren(inputNodeCopy);
                        inputNodeCopy.setParent(copy);
                    }
                }
                // input is a collection
                else if (input instanceof Object[])
                {
                    Object[] inputCollection = (Object[]) input;
                    Object[] inputCollectionCopy = new Object[inputCollection.length];
                    for (int i = 0; i < inputCollection.length; i++) {
                        // if input element is a node
                        if (inputCollection[i] instanceof LudemeNode)
                        {
                            LudemeNode inputNode = (LudemeNode) inputCollection[i];
                            // if input node is in the list of nodes to copy, copy it
                            if (nodes.contains(inputNode))
                            {
                                LudemeNode inputNodeCopy = copiedNodes.get(inputNode);
                                inputCollectionCopy[i] = inputNodeCopy;
                                copy.addChildren(inputNodeCopy);
                                inputNodeCopy.setParent(copy);
                            }
                        }
                    }
                    copy.setProvidedInput(arg, inputCollectionCopy);
                }
            }
        }
        // store copied nodes
        currentCopy = new ArrayList<>(copiedNodes.values());
    }

    public static void paste(DescriptionGraph graph, int x, int y)
    {
        if(currentCopy.isEmpty()) return;
        if(DEBUG) System.out.println("[HANDLER] paste(graph, x, y) Pasting " + currentCopy.size() + " nodes");
        IGraphPanel graphPanel = graphPanelMap.get(graph);

        recordUserActions = false;

        // old copy
        List<LudemeNode> oldCopy = new ArrayList<>(currentCopy);
        currentCopy.clear();
        copy(graph, oldCopy);

        // find left-most node
        LudemeNode leftMostNode = oldCopy.get(0);
        for(LudemeNode node : oldCopy)
        {
            if(node.x() < leftMostNode.x()) leftMostNode = node;
        }

        int x_shift, y_shift;

        if(x == -1 && y == -1)
        {
            Point v = mainPanel.getPanel().getViewport().getViewPosition();
            x_shift = v.x;
            y_shift = v.y;
        }
        else
        {
            x_shift = x - leftMostNode.x();
            y_shift = y - leftMostNode.y();
        }

        // add nodes to graph
        for(LudemeNode node : oldCopy)
        {
            node.setX(node.x() + x_shift);
            node.setY(node.y() + y_shift);
            addNode(graph, node);
        }
        // update all edges
        for(LudemeNode parent : oldCopy)
        {
            for(NodeArgument argument : parent.providedInputsMap().keySet())
            {
                Object input = parent.providedInputsMap().get(argument);
                if(input instanceof LudemeNode)
                {
                    LudemeNode inputNode = (LudemeNode) input;
                    if(oldCopy.contains(inputNode))
                    {
                        addEdge(graph, parent, inputNode, argument);
                    }
                }
                else if(input instanceof Object[])
                {
                    Object[] inputCollection = (Object[]) input;
                    Object[] placeholder = new Object[1];
                    parent.setProvidedInput(argument, placeholder);
                    for(int i = 0; i < inputCollection.length; i++)
                    {
                        if(inputCollection[i] instanceof LudemeNode)
                        {
                            LudemeNode inputNode = (LudemeNode) inputCollection[i];
                            if(oldCopy.contains(inputNode))
                            {
                                addEdge(graph, parent, inputNode, argument, i);
                            }
                        }
                        else if(i>0) addCollectionElement(graph, parent, argument);
                    }
                    if(parent.providedInputsMap().get(argument) == placeholder)
                    {
                        parent.setProvidedInput(argument, inputCollection);
                    }
                }
            }
        }
        graphPanel.repaint();
        recordUserActions = true;
        IUserAction action = new PasteAction(graphPanel, oldCopy);
        addAction(action);
    }
    /**
     *
     * @param graph graph in operation
     * @param node node in operation
     * @param x x coordinate
     * @param y y coordinate
     */
    public static void updatePosition(DescriptionGraph graph, LudemeNode node, int x, int y)
    {
        node.setPos(x, y);
    }

    public static void collapse(DescriptionGraph graph)
    {
        IGraphPanel graphPanel = graphPanelMap.get(graph);
        List<LudemeNode> selectedNodes = selectedNodes(graph);
        if(selectedNodes.isEmpty()) return;
        if(selectedNodes.size() == 1)
        {
            if(selectedNodes.get(0).parentNode() != null) collapseNode(graph, selectedNodes.get(0), true);
            return;
        }
        List<LudemeNode> roots = new ArrayList<>();
        for(LudemeNode node : selectedNodes)
        {
            if(node.parentNode() == null) roots.add(node);
            else if(!selectedNodes.contains(node.parentNode())) roots.add(node.parentNode());
        }
        // find all subtree root's children
        List<LudemeNode> toCollapse = new ArrayList<>();
        for(LudemeNode node : selectedNodes)
        {
            if(roots.contains(node.parentNode())) toCollapse.add(node);
        }
        System.out.println("[HANDLER] collapse(graph) Collapsing " + toCollapse.size() + " nodes");
        for(LudemeNode node : toCollapse)
        {
            collapseNode(graph, node, true);
        }
        graphPanelMap.get(graph).deselectEverything();
    }

    public static void expand(DescriptionGraph graph)
    {
        List<LudemeNode> selectedNodes = selectedNodes(graph);
        // find all subtree root
        List<LudemeNode> toExpand = new ArrayList<>();
        for(LudemeNode node : selectedNodes)
        {
            for(LudemeNode child : node.childrenNodes())
            {
                toExpand.add(child);
            }
        }

        System.out.println("[HANDLER] expand(graph) Expanding " + toExpand.size() + " nodes");

        for (LudemeNode node : toExpand) {
            collapseNode(graph, node, false);
        }
        graphPanelMap.get(graph).deselectEverything();
    }

    public static List<LudemeNode> selectedNodes(DescriptionGraph graph)
    {
        IGraphPanel graphPanel = graphPanelMap.get(graph);
        List<LudemeNode> nodes = new ArrayList<>();
        for(LudemeNodeComponent lnc : graphPanel.selectedLnc())
        {
            nodes.add(lnc.node());
        }
        return nodes;
    }

    /**
     * (Un-)Collapses a node
     * @param graph The graph that contains the node.
     * @param node The node to update.
     * @param collapse Whether the node should be collapsed or not.
     */
    public static void collapseNode(DescriptionGraph graph, LudemeNode node, boolean collapse)
    {
        if(DEBUG) System.out.println("[HANDLER] collapse(graph, node) Collapsing " + node.title() + ": " + collapse);
        node.setCollapsed(collapse);
        IGraphPanel graphPanel = graphPanelMap.get(graph);
        addAction(new CollapsedAction(graphPanel, node, collapse));
        graphPanel.notifyCollapsed(graphPanel.nodeComponent(node), collapse);
    }

    public static void activateOptionalTerminalField(DescriptionGraph graph, LudemeNode node, NodeArgument argument, boolean activate)
    {
        IGraphPanel graphPanel = graphPanelMap.get(graph);
        IUserAction action = new ActivateOptionalTerminalAction(graphPanel, node, argument, activate);

        addAction(action);
        if(!performedUserActions.isEmpty() && performedUserActions.peek() == action)
            Handler.recordUserActions = false;
        graphPanel.notifyTerminalActivated(graphPanel.nodeComponent(node), argument, activate);
        if(!performedUserActions.isEmpty() && performedUserActions.peek() == action)
            Handler.recordUserActions = true;
    }

    public static String getLudString(DescriptionGraph graph){
        return graph.toLud();
    }

    public static void centerViewport(int x, int y)
    {
        if (mainPanel != null)
        {
            Rectangle view = mainPanel.getPanel().getViewport().getViewRect();
            //mainPanel.setView(x-view.width/2, y-view.height/2);
        }
    }

    public static void setMainPanel(MainPanel mainPanel) {
        Handler.mainPanel = mainPanel;
    }

    public static void activateSelectionMode()
    {
        editorPanel.enterSelectionMode();
    }

    public static void deactivateSelectionMode()
    {
        editorPanel.exitSelectionMode();
        toolsPanel.deactivateSelection();
        editorPanel.repaint();
    }

    public static void turnOffSelectionBtn()
    {
        toolsPanel.deactivateSelection();
        toolsPanel.repaint();
        toolsPanel.revalidate();
    }

    public static void undo()
    {
        if(performedUserActions.isEmpty()) return;
        currentUndoAction = performedUserActions.pop();
        if(DEBUG) System.out.println("[HANDLER] undo() Undoing " + currentUndoAction.actionType());
        undoneUserActions.add(currentUndoAction);
        Handler.recordUserActions = false;
        currentUndoAction.undo();
        currentUndoAction.graphPanel().repaint();
        Handler.recordUserActions = true;
        if(DEBUG) System.out.println("[HANDLER] undo() Completed " + currentUndoAction.actionType());
        toolsPanel.updateUndoRedoBtns(performedUserActions, undoneUserActions);
        currentUndoAction = null;
    }

    public static void redo()
    {
        if(undoneUserActions.isEmpty()) return;
        currentRedoAction = undoneUserActions.pop();
        if(DEBUG) System.out.println("[HANDLER] redo() Redoing " + currentRedoAction.actionType());
        performedUserActions.add(currentRedoAction);
        Handler.recordUserActions = false;
        currentRedoAction.redo();
        currentRedoAction.graphPanel().repaint();
        Handler.recordUserActions = true;
        if(DEBUG) System.out.println("[HANDLER] redo() Completed " + currentRedoAction.actionType());
        toolsPanel.updateUndoRedoBtns(performedUserActions, undoneUserActions);
        currentRedoAction = null;
    }

    private static void addAction(IUserAction action)
    {
        if(!recordUserActions) return;
        performedUserActions.add(action);
        undoneUserActions = new Stack<>();
        toolsPanel.updateUndoRedoBtns(performedUserActions, undoneUserActions);
    }


    public static void selectNode(LudemeNodeComponent node)
    {
        editorPanel.addNodeToSelections(node);
        editorPanel.repaint();
        editorPanel.revalidate();
    }

    public static void setAutoplacement(boolean var) {editorPanel.setAutoplacement(var);}

    public static List<LudemeNode> copyList()
    {
        return currentCopy;
    }

    public static void setCopyList(List<LudemeNode> copyList)
    {
        Handler.currentCopy = copyList;
    }

    public static void updateNodePositions()
    {
        editorPanel.updateNodePositions();
    }



    // OLD METHODS WITH PROVIDED INPUTS AS Object[]


        /*public static void updateInput(DescriptionGraph graph, LudemeNode node, int index, Object input){
        if(DEBUG) System.out.println("[HANDLER] updateInput(graph, node, index, input) -> Updating input: " + node.title() + ", index: " + index + ", input: " + input);
        if(index < node.providedInputs().length) {
            // if the input is null but was a node before, remove the child from the parent
            if(input == null && node.providedInputs()[index] instanceof LudemeNode) {
                node.removeChildren((LudemeNode) node.providedInputs()[index]);
            }
            node.setProvidedInput(index, input);
        }
    }*/


    /*public static void addCollectionElement(DescriptionGraph graph, LudemeNode node, int inputIndex)
    {
        if(DEBUG) System.out.println("[HANDLER] addCollectionElement(graph, node, nodeArgument) Adding collection element of " + node.title() + ", " + inputIndex);
        Object[] oldCollection = (Object[]) node.providedInputs()[inputIndex];
        if(oldCollection == null)
        {
            updateInput(graph, node, inputIndex, new Object[2]);
            return;
        }
        Object[] newCollection = new Object[oldCollection.length + 1];
        System.arraycopy(oldCollection, 0, newCollection, 0, oldCollection.length);
        updateInput(graph, node, inputIndex, newCollection);
    }*/

        /*public static void updateCollectionInput(DescriptionGraph graph, LudemeNode node, int inputIndex, Object input, int elementIndex)
    {
        if(node.providedInputs()[inputIndex] == null)
        {
            node.setProvidedInput(inputIndex, new Object[1]);
        }
        if(elementIndex >= ((Object[])(node.providedInputs()[inputIndex])).length) addCollectionElement(graph, node, inputIndex);
        Object[] in = (Object[]) node.providedInputs()[inputIndex];
        in[elementIndex] = input;
        node.setProvidedInput(inputIndex, in);
    }*/

    /*
    /**
     * if a collection element was removed, update the provided input array
     * @param graph
     * @param node
     * @param inputIndex Index of collection argument in clause
     * @param elementIndex Index of collection element
     */
    /*public static void removeCollectionElement(DescriptionGraph graph, LudemeNode node, int inputIndex, int elementIndex)
    {
        if(DEBUG) System.out.println("[HANDLER] Removed collection element of " + node.symbol().name() + ", " + inputIndex + " at " + elementIndex);

        Object[] oldCollection = (Object[]) node.providedInputs()[inputIndex];
        if(oldCollection == null) return;
        Object[] newCollection = new Object[oldCollection.length - 1];
        for(int i = 0; i < elementIndex; i++)
        {
            newCollection[i] = oldCollection[i];
        }
        for(int i = elementIndex + 1; i < oldCollection.length; i++)
        {
            newCollection[i - 1] = oldCollection[i];
        }
        updateInput(graph, node, inputIndex, newCollection);
    }*/

    public static Dimension getViewPortSize()
    {
        return mainPanel.getViewPort();
    }

    public static void selectAll()
    {
        editorPanel.selectAllNodes();
    }
}