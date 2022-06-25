package app.display.dialogs.visual_editor.handler;

import app.display.dialogs.visual_editor.model.DescriptionGraph;
import app.display.dialogs.visual_editor.model.Edge;
import app.display.dialogs.visual_editor.model.LudemeNode;
import app.display.dialogs.visual_editor.model.NodeArgument;
import app.display.dialogs.visual_editor.model.UserActions.*;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.LudemeNodeComponent;
import app.display.dialogs.visual_editor.view.panels.IGraphPanel;
import app.display.dialogs.visual_editor.view.panels.MainPanel;
import app.display.dialogs.visual_editor.view.panels.editor.EditorPanel;
import app.display.dialogs.visual_editor.view.panels.editor.tabPanels.LayoutSettingsPanel;
import app.display.dialogs.visual_editor.view.panels.header.ToolsPanel;
import main.grammar.Clause;
import main.grammar.Symbol;

import java.util.*;
import java.awt.*;
import java.util.List;

public class Handler {

    // TODO: History for Undo/Redo

    public static DescriptionGraph gameDescriptionGraph;

    // Single EditorPanel
    public static EditorPanel editorPanel;

    // Single ToolsPanel
    public static ToolsPanel toolsPanel;

    public static LayoutSettingsPanel lsPanel;

    public static MainPanel mainPanel;

    private static List<LudemeNodeComponent> copyList = new ArrayList<>();



    private static Stack<IUserAction> performedUserActions = new Stack<>();
    private static Stack<IUserAction> undoneUserActions = new Stack<>();
    public static boolean recordUserActions = true;

    private static HashMap<DescriptionGraph, IGraphPanel> graphPanelMap = new HashMap<>();

    private static final boolean DEBUG = true;


    /**
     * Adds a node to the graph
     * @param graph
     * @param node
     */
    public static void addNode(DescriptionGraph graph, LudemeNode node )
    {
        addNode(graph, node, false);
    }

    /**
     * Adds a node to the graph
     * @param graph
     * @param node
     * @param connect Whether the node will be connected after insertion
     */
    public static void addNode(DescriptionGraph graph, LudemeNode node, boolean connect)
    {
        if(DEBUG) System.out.println("[HANDLER] addNode(graph. node, connect) Adding node: " + node.title());
        if(graph.getNodes().isEmpty()) graph.setRoot(node);
        graph.addNode(node);
        // notify graph panel
        IGraphPanel graphPanel = graphPanelMap.get(graph);
        graphPanel.notifyNodeAdded(node, connect);
        addAction(new AddedNodeAction(graphPanel, node));
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
        if(DEBUG) System.out.println("[HANDLER] removeNode(graph, node) -> Removing node: " + node.title());

        addAction(new RemovedNodeAction(graphPanelMap.get(graph), node));
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
        if(DEBUG) System.out.println("[HANDLER] nodeArgument(graph, form, to, nodeArgument) -> Adding edge: " + from.title() + " -> " + to.title());
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
        if(DEBUG) System.out.println("[HANDLER] nodeArgument(graph, form, to, nodeArgument, elementIndex) -> Adding edge: " + from.title() + " -> " + to.title());
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
        //IGraphPanel graphPanel = graphPanelMap.get(graph);
        // TODO: graphPanel.notifyEdgeRemoved(graphPanel.nodeComponent(from), graphPanel.nodeComponent(to));
    }

    /**
     * Updates the current clause of a node.
     */
    public static void updateCurrentClause(DescriptionGraph graph, LudemeNode node, Clause c)
    {
        Clause oldClause = node.selectedClause();
        if(c.args() == null) node.setSelectedClause(c.symbol().rule().rhs().get(0));
        else node.setSelectedClause(c);
        IGraphPanel graphPanel = graphPanelMap.get(graph);
        graphPanel.notifySelectedClauseChanged(graphPanel.nodeComponent(node), c);
        addAction(new ChangedClauseAction(graphPanel, node, oldClause, node.selectedClause()));
    }

    /**
     * Assigns a IGraphPanel to a DescriptionGraph
     * @param graph
     * @param graphPanel
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
        if(input instanceof LudemeNode) addAction(new AddedConnectionAction(graphPanelMap.get(graph), node, (LudemeNode) input, nodeArgument));
        // if the input is null but was a node before, remove the child from the parent
        if(input == null && node.providedInputsMap().get(nodeArgument) instanceof LudemeNode)
        {
            node.removeChildren((LudemeNode) node.providedInputsMap().get(nodeArgument));
        }
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
        Object[] oldCollection = (Object[]) node.providedInputsMap().get(nodeArgument);
        if(oldCollection == null)
        {
            updateInput(graph, node, nodeArgument, new Object[2]);
            return;
        }
        Object[] newCollection = new Object[oldCollection.length + 1];
        System.arraycopy(oldCollection, 0, newCollection, 0, oldCollection.length);
        updateInput(graph, node, nodeArgument, newCollection);
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
        if(DEBUG) System.out.println("[HANDLER] updateCollectionInput(graph, node, nodeArgument, input, elementIndex) Updating collection input of " + node.title() + ", " + nodeArgument + ", elementIndex: " + elementIndex);
        if(node.providedInputsMap().get(nodeArgument) == null)
        {
            node.setProvidedInput(nodeArgument, new Object[1]);
        }

        if(elementIndex >= ((Object[])(node.providedInputsMap().get(nodeArgument))).length) addCollectionElement(graph, node, nodeArgument);
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
        Object[] oldCollection = (Object[]) node.providedInputsMap().get(nodeArgument);
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
        updateInput(graph, node, nodeArgument, newCollection);
    }



    /**
     *
     * @param graph
     * @param node
     * @param x
     * @param y
     */
    public static void updatePosition(DescriptionGraph graph, LudemeNode node, int x, int y){
        node.setPos(x, y);
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
        IUserAction lastAction = performedUserActions.pop();
        undoneUserActions.add(lastAction);
        Handler.recordUserActions = false;
        lastAction.undo();
        lastAction.graphPanel().repaint();
        Handler.recordUserActions = true;
        System.out.println("undone: " + lastAction.toString());
        toolsPanel.updateUndoRedoBtns(performedUserActions, undoneUserActions);
    }

    public static void redo()
    {
        if(undoneUserActions.isEmpty()) return;
        IUserAction lastUndoneAction = undoneUserActions.pop();
        performedUserActions.add(lastUndoneAction);
        Handler.recordUserActions = false;
        lastUndoneAction.redo();
        lastUndoneAction.graphPanel().repaint();
        Handler.recordUserActions = true;
        System.out.println("redone: " + lastUndoneAction.toString());
        toolsPanel.updateUndoRedoBtns(performedUserActions, undoneUserActions);
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
    }

    public static void setAutoplacement(boolean var) {editorPanel.setAutoplacement(var);}

    public static List<LudemeNodeComponent> copyList()
    {
        return copyList;
    }

    public static void setCopyList(List<LudemeNodeComponent> copyList)
    {
        Handler.copyList = copyList;
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


}