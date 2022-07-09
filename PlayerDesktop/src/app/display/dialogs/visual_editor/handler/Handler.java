package app.display.dialogs.visual_editor.handler;

import app.DesktopApp;
import app.PlayerApp;
import app.display.dialogs.editor.LudiiTokeniser;
import app.display.dialogs.visual_editor.StartVisualEditor;
import app.display.dialogs.visual_editor.model.DescriptionGraph;
import app.display.dialogs.visual_editor.model.Edge;
import app.display.dialogs.visual_editor.model.LudemeNode;
import app.display.dialogs.visual_editor.model.NodeArgument;
import app.display.dialogs.visual_editor.model.UserActions.*;
import app.display.dialogs.visual_editor.view.VisualEditorFrame;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.LudemeNodeComponent;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.inputs.LInputField;
import app.display.dialogs.visual_editor.view.designPalettes.DesignPalette;
import app.display.dialogs.visual_editor.view.panels.IGraphPanel;
import app.display.dialogs.visual_editor.view.panels.editor.defineEditor.DefineEditor;
import app.display.dialogs.visual_editor.view.panels.editor.defineEditor.DefineGraphPanel;
import app.display.dialogs.visual_editor.view.panels.editor.gameEditor.GameGraphPanel;
import app.display.dialogs.visual_editor.view.panels.editor.backgrounds.CartesianGridBackground;
import app.display.dialogs.visual_editor.view.panels.editor.backgrounds.DotGridBackground;
import app.display.dialogs.visual_editor.view.panels.editor.backgrounds.EmptyBackground;
import app.display.dialogs.visual_editor.view.panels.editor.backgrounds.IBackground;
import app.display.dialogs.visual_editor.view.panels.editor.tabPanels.LayoutSettingsPanel;
import app.display.dialogs.visual_editor.view.panels.header.ToolsPanel;
import app.utils.GameUtil;
import game.Game;
import main.grammar.*;
import main.options.UserSelections;

import java.util.*;
import java.util.List;
import javax.swing.*;
import java.awt.*;


/**
 * Handler for the visual editor.
 * Handles all user actions and updates the view accordingly.
 * @author Filipp Dokienko
 */


public class Handler
{
    // Graph Panels
    /** Main Game Editor Graph Panel */
    public static GameGraphPanel gameGraphPanel;
    /** Define Graph Panels */
    public static final List<DefineGraphPanel> defineGraphPanels = new ArrayList<>();
    /** Currently selected Graph Panel */
    public static IGraphPanel currentGraphPanel;
    /** Map of Graph Panels keyed by their DescriptionGraph */
    private static final HashMap<DescriptionGraph, IGraphPanel> graphPanelMap = new HashMap<>();


    // Defines
    /** The Input Symbol indicating the Parameter of a Define */
    public static final Symbol PARAMETER_SYMBOL = new Symbol(Symbol.LudemeType.Ludeme, "PARAMETER", "PARAMETER", null);
    /** Map of a list of added define ludeme nodes keyed by their Define Graph */
    private static final Map<DescriptionGraph, List<LudemeNode>> defineLudemeNodes = new HashMap<>();
    /** Map of Graph Panels keyed by a define node which is part of the graph */
    private static final Map<LudemeNode, DescriptionGraph> defineNodeToGraphMap = new HashMap<>();


    // Undo and Redo functionality
    /** List of UserActions that were performed, keyed by the GraphPanel they were performed on */
    private static final Map<IGraphPanel, Stack<IUserAction>> performedUserActionsMap = new HashMap<>();
    /** List of UserActions that were undone, keyed by the GraphPanel they were undone on */
    private static final Map<IGraphPanel, Stack<IUserAction>> undoneUserActionsMap    = new HashMap<>();
    /** List of UserActions on the current graph panel */
    private static Stack<IUserAction> currentPerformedUserActions;
    /** List of undone UserActions on the current graph panel */
    private static Stack<IUserAction> currentUndoneUserActions;
    /** The Action that is currently being undone */
    private static IUserAction currentUndoAction;
    /** The Action that is currently being redone */
    private static IUserAction currentRedoAction;
    /** Whether the Handler should record User Actions (store them) */
    public static boolean recordUserActions = true;


    // Copied nodes
    /** List of currently copied nodes */
    private static List<LudemeNode> clipboard = new ArrayList<>();


    // Compiling
    /** The last compile */
    public static Object[] lastCompile;
    /** Whether the game should be compiled after each change */
    public static boolean liveCompile = true;


    // Sensitivity to changes
    /** Whether sensitivive actions should be confirmed by the user */
    public static boolean sensitivityToChanges = true;
    /** When X nodes are removed at once, ask the user for confirmation */
    public static final int SENSITIVITY_REMOVAL = 6;
    /** When X collection elements are removed at once, ask the user for confirmation */
    public static final int SENSITIVITY_COLLECTION_REMOVAL = 4;


    // Additional Panels of the Frame
    /** Main Frame */
    public static VisualEditorFrame visualEditorFrame;
    /** Define Editor */
    public static DefineEditor defineEditor;
    /** ToolsPanel, including undo, redo and play buttons */
    public static ToolsPanel toolsPanel;
    /** Panel for layout settings */
    public static LayoutSettingsPanel lsPanel;


    // Layout Settings
    /** Whether the layout-settings sidebar is visible */
    public static final boolean sidebarVisible = true;
    /** Whether layout-arrangement animations are enabled */
    public static boolean animation = true;
    /** Whether nodes should be placed arranged automatically */
    public static boolean autoplacement = false;


    // Appearance Settings
    /** Backgrounds: Dot Grid, Cartesian Grid, and no Grid */
    public static final IBackground DotGridBackground = new DotGridBackground();
    public static final IBackground EmptyBackground = new EmptyBackground();
    public static final IBackground CartesianGridBackground = new CartesianGridBackground();
    /** Currently active Background */
    private static IBackground currentBackground = DotGridBackground;
    /** Instanstantiate the DesignPalette */
    private static final DesignPalette designPalette = DesignPalette.instance();


    /** Whether there is any output to the console */
    private static final boolean DEBUG = true;


    // ~~~~~~~  Graph Panels  ~~~~~~~

    /**
     * Assigns a IGraphPanel to a DescriptionGraph
     * @param graph graph to be assigned
     * @param graphPanel graph panel
     */
    public static void addGraphPanel(DescriptionGraph graph, IGraphPanel graphPanel)
    {
        if(!graphPanelMap.containsKey(graph))
        {
            graphPanelMap.put(graph, graphPanel);
            if(graphPanel.isDefineGraph() && !defineGraphPanels.contains(graphPanel))
                defineGraphPanels.add((DefineGraphPanel) graphPanel);
            performedUserActionsMap.put(graphPanel, new Stack<>());
            undoneUserActionsMap.put(graphPanel, new Stack<>());
        }
    }

    /**
     * Removes a IGraphPanel from the map
     * @param graphPanel
     */
    public static void removeGraphPanel(IGraphPanel graphPanel)
    {
        graphPanelMap.remove(graphPanel.graph());
        if(graphPanel.isDefineGraph())
            defineGraphPanels.remove(graphPanel);
        performedUserActionsMap.remove(graphPanel);
        undoneUserActionsMap.remove(graphPanel);

        System.out.println("Removed graph panel: " + graphPanel.graph());
        System.out.println("Remaining graph panels: " + defineGraphPanels.size());


    }

    /**
     * Updates the current active graph panel
     * @param graphPanel graph panel to be set as active
     */
    public static void updateCurrentGraphPanel(IGraphPanel graphPanel)
    {
        currentGraphPanel = graphPanel;
        currentPerformedUserActions = performedUserActionsMap.get(graphPanel);
        currentUndoneUserActions = undoneUserActionsMap.get(graphPanel);
        if(toolsPanel != null)
            toolsPanel.updateUndoRedoBtns(currentPerformedUserActions, currentUndoneUserActions);
    }

    // ~~~~~~~  Changes to the graph  ~~~~~~~

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
        // if added node is a define node, store this node in the map
        if(node.isDefineNode())
        {
            List<LudemeNode> defineNodes = defineLudemeNodes.computeIfAbsent(node.defineGraph(), k -> new ArrayList<>());
            defineNodes.add(node);
            defineNodeToGraphMap.put(node, graph);
        }
        // notify graph panel
        IGraphPanel graphPanel = graphPanelMap.get(graph);
        addAction(new AddedNodeAction(graphPanel, node));
        graphPanel.notifyNodeAdded(node, connect);
        if(recordUserActions && node.parentNode() != null && (node.parentNode().providedInputsMap().get(node.creatorArgument()) instanceof Object[]))
        {
            Object[] collectionInput = (Object[]) node.parentNode().providedInputsMap().get(node.creatorArgument());
            int elementIndex = Arrays.asList(collectionInput).indexOf(node);
            ((AddedNodeAction) currentPerformedUserActions.peek()).setCollectionIndex(elementIndex);
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
     * Adds a 1D Collection Node to the graph, which is a node that can be connected to a 2D Collection Input
     * @param graph Graph to add the node to
     * @param nodeArgument2DCollection The NodeArgument which created this node (must be 2D collection)
     * @param x The x-position of the node
     * @param y The y-position of the node
     */
    public static void addNode(DescriptionGraph graph, NodeArgument nodeArgument2DCollection, int x, int y)
    {
        LudemeNode node = new LudemeNode(nodeArgument2DCollection, x, y);
        addNode(graph, node, true);
    }


    /**
     * Removes a node from the graph.
     * @param graph The graph that contains the node.
     * @param node The node to remove.
     */
    public static void removeNode(DescriptionGraph graph, LudemeNode node)
    {
        if(graph.getRoot() == node)
            return;
        if(DEBUG) System.out.println("[HANDLER] removeNode(graph, node) -> Removing node: " + node.title());

        // remove node from map if a define node
        if(node.isDefineNode())
        {
            defineLudemeNodes.get(node.defineGraph()).remove(node);
            defineNodeToGraphMap.remove(node);
        }

        IUserAction action = new RemovedNodeAction(graphPanelMap.get(graph), node);
        addAction(action);
        if(lastActionEquals(action)) Handler.recordUserActions = false;
        // if the action is added, and the node was part of a collection, notify the action about the collection element index
        if(recordUserActions && node.parentNode() != null && (node.parentNode().providedInputsMap().get(node.creatorArgument()) instanceof Object[]))
        {
            Object[] collectionInput = (Object[]) node.parentNode().providedInputsMap().get(node.creatorArgument());
            int elementIndex = Arrays.asList(collectionInput).indexOf(node);
            ((RemovedNodeAction) currentPerformedUserActions.peek()).setCollectionIndex(elementIndex);
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
            {
                Handler.updateInput(graph, node.parentNode(), args.get(index2), null);
            }
        }
        IGraphPanel graphPanel = graphPanelMap.get(graph);
        graphPanel.notifyNodeRemoved(graphPanel.nodeComponent(node));
        if(lastActionEquals(action))
            Handler.recordUserActions = true;
    }

    /**
     * Removes a list of nodes from the graph
     * @param graph
     * @param nodes
     */
    public static void removeNodes(DescriptionGraph graph, List<LudemeNode> nodes)
    {
        if(DEBUG) System.out.println("[HANDLER] removeNodes(graph, nodes) -> Removing nodes: " + nodes.size());

        // display dialog to confirm removal of nodes
        if(nodes.size() >= SENSITIVITY_REMOVAL && Handler.sensitivityToChanges)
        {
            int userChoice = JOptionPane.showConfirmDialog(null, "Are you sure you want to remove " + nodes.size() + " nodes?", "Remove nodes", JOptionPane.YES_NO_OPTION);
            if(userChoice != JOptionPane.YES_OPTION)
            {
                return;
            }
        }

        // remove root node and already deleted nodes
        for(LudemeNode node : new ArrayList<>(nodes))
        {
            if(graph.getRoot() == node)
            {
                nodes.remove(node);
                break;
            }
        }

        IUserAction action = new RemovedNodesAction(graphPanelMap.get(graph), nodes);
        addAction(action);

        if(lastActionEquals(action))
            Handler.recordUserActions = false;
        for(LudemeNode n : nodes)
            removeNode(graph, n);
        if(lastActionEquals(action))
            Handler.recordUserActions = true;
    }

    /**
     * Removes currently selected nodes from the currently selected graph
     */
    public static void remove()
    {
        remove(currentGraphPanel.graph());
    }

    /**
     * Removes currently selected nodes from the given graph
     * @param graph Graph to remove nodes from
     */
    public static void remove(DescriptionGraph graph)
    {
        List<LudemeNode> selectedNodes = selectedNodes(graph);
        if(selectedNodes.isEmpty())
            return;
        if(selectedNodes.size() == 1)
            removeNode(graph, selectedNodes.get(0));
        else
            removeNodes(graph, selectedNodes);
    }


    // ~~~~~~~  Changes to the nodes  ~~~~~~~

    /**
     * Updates the selected clause of a node
     * @param graph Graph to update the node in
     * @param node Node to update
     * @param c Clause to update to
     */
    public static void updateCurrentClause(DescriptionGraph graph, LudemeNode node, Clause c)
    {
        Clause oldClause = node.selectedClause();
        if(oldClause == c)
            return;
        IGraphPanel graphPanel = graphPanelMap.get(graph);
        IUserAction action = new ChangedClauseAction(graphPanel, node, oldClause, c);
        addAction(action);
        // remove all edges from the node
        for(Object input : node.providedInputsMap().values())
        {
            if(input instanceof LudemeNode)
            {
                if(lastActionEquals(action))
                {
                    Handler.recordUserActions = false;
                    removeEdge(graph, node, (LudemeNode) input);
                    Handler.recordUserActions = true;
                }
            }
        }

        if(c.args() == null)
            node.setSelectedClause(c.symbol().rule().rhs().get(0));
        else
            node.setSelectedClause(c);

        graphPanel.notifySelectedClauseChanged(graphPanel.nodeComponent(node));
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
            IUserAction action = new AddedCollectionAction(graphPanel, node, nodeArgument, 1, null);
            addAction(action);
            if(lastActionEquals(action))
            {
                Handler.recordUserActions = false;
            }
            updateInput(graph, node, nodeArgument, new Object[2]);
            if(lastActionEquals(action))
            {
                Handler.recordUserActions = true;
            }
            graphPanel.notifyCollectionAdded(graphPanel.nodeComponent(node), nodeArgument, 1);
            return;
        }
        Object[] newCollection = new Object[oldCollection.length + 1];
        System.arraycopy(oldCollection, 0, newCollection, 0, oldCollection.length);
        IUserAction action = new AddedCollectionAction(graphPanel, node, nodeArgument, oldCollection.length, null);
        addAction(action);
        if(lastActionEquals(action))
            Handler.recordUserActions = false;
        updateInput(graph, node, nodeArgument, newCollection);
        if(lastActionEquals(action))
            Handler.recordUserActions = true;

        graphPanel.notifyCollectionAdded(graphPanel.nodeComponent(node), nodeArgument, newCollection.length - 1);
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
        if(oldCollection == null)
            return;

        // get input
        Object input = oldCollection[elementIndex];

        IUserAction action = new RemovedCollectionAction(graphPanelMap.get(graph), node, nodeArgument, elementIndex, input);
        addAction(action);

        Object[] newCollection = new Object[oldCollection.length - 1];

        if(currentUndoAction instanceof AddedCollectionAction)
            if(((AddedCollectionAction) currentUndoAction).isUpdated(node, nodeArgument, elementIndex))
                ((AddedCollectionAction) currentUndoAction).setInput(input);

        System.arraycopy(oldCollection, 0, newCollection, 0, elementIndex);
        if (oldCollection.length - (elementIndex + 1) >= 0)
            System.arraycopy(oldCollection, elementIndex + 1, newCollection, elementIndex + 1 - 1, oldCollection.length - (elementIndex + 1));

        if(lastActionEquals(action))
            Handler.recordUserActions = false;
        updateInput(graph, node, nodeArgument, newCollection);
        if(input instanceof LudemeNode)
            removeEdge(graph, node, (LudemeNode) input, elementIndex);
        if(lastActionEquals(action))
            Handler.recordUserActions = true;
        IGraphPanel graphPanel = graphPanelMap.get(graph);
        graphPanel.notifyCollectionRemoved(graphPanel.nodeComponent(node), nodeArgument, elementIndex);
    }


    /**
     * Adds and edge between two nodes.
     * @param graph The graph that contains the nodes.
     * @param from The node that the edge starts from.
     * @param to The node that the edge ends at.
     * @param nodeArgument The nodeArgument of the field
     */
    public static void addEdge(DescriptionGraph graph, LudemeNode from, LudemeNode to, NodeArgument nodeArgument)
    {
        // check whether the edge already exists
        for(Edge e : graph.getEdgeList())
            if(e.getNodeA() == from.id() && e.getNodeB() == to.id())
                return;
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
    public static void addEdge(DescriptionGraph graph, LudemeNode from, LudemeNode to, NodeArgument nodeArgument, int elementIndex)
    {
        // check whether the edge already exists
        for(Edge e : graph.getEdgeList())
            if(e.getNodeA() == from.id() && e.getNodeB() == to.id())
                return;
        if(DEBUG) System.out.println("[HANDLER] addEdge(graph, form, to, nodeArgument, elementIndex) -> Adding edge: " + from.title() + " -> " + to.title() + ", elementIndex: " + elementIndex);
        graph.addEdge(from.id(), to.id());
        // here form is the parent node
        from.addChildren(to);
        to.setParent(from);

        // if the edge is part of a collection, adjust the collection size
        while(from.providedInputsMap().get(nodeArgument) == null || elementIndex+1>((Object[])from.providedInputsMap().get(nodeArgument)).length)
            addCollectionElement(graph, from, nodeArgument);

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
    public static void addEdge(DescriptionGraph graph, LudemeNode from, LudemeNode to, int inputFieldIndex)
    {
        // check whether the edge already exists
        for(Edge e : graph.getEdgeList())
            if(e.getNodeA() == from.id() && e.getNodeB() == to.id())
                return;

        if(DEBUG) System.out.println("[HANDLER] nodeArgument(graph, form, to, inputFieldIndex) -> Adding edge: " + from.title() + " -> " + to.title() + ", inputFieldIndex: " + inputFieldIndex);

        graph.addEdge(from.id(), to.id());
        // here form is the parent node
        from.addChildren(to);
        to.setParent(from);

        // notify graph panel to draw edge
        IGraphPanel graphPanel = graphPanelMap.get(graph);
        graphPanel.notifyEdgeAdded(graphPanel.nodeComponent(from), graphPanel.nodeComponent(to), inputFieldIndex);
    }


    /**
     * Removes an edge between two nodes.
     * @param graph The graph that contains the nodes.
     * @param from The node that the edge starts from.
     * @param to The node that the edge ends at.
     */
    public static void removeEdge(DescriptionGraph graph, LudemeNode from, LudemeNode to)
    {
        removeEdge(graph, from, to, true);
    }

    /**
     * Removes an edge between two nodes.
     * @param graph The graph that contains the nodes.
     * @param from The node that the edge starts from.
     * @param to The node that the edge ends at.
     * @param notify Whether the graph panel should be notified about the removal of the edge
     */
    public static void removeEdge(DescriptionGraph graph, LudemeNode from, LudemeNode to, boolean notify)
    {
        if(DEBUG) System.out.println("[HANDLER] removeEdge(graph, from, to) -> Removing edge: " + from.title() + " -> " + to.title());
        graph.removeEdge(from.id(), to.id());
        from.removeChildren(to);
        to.setParent(null);
        if(notify)
        {
            IGraphPanel graphPanel = graphPanelMap.get(graph);
            graphPanel.notifyEdgeRemoved(graphPanel.nodeComponent(from), graphPanel.nodeComponent(to));
        }
    }

    /**
     * Removes an edge between two nodes of a given collection element index.
     * @param graph The graph that contains the nodes.
     * @param from The node that the edge starts from.
     * @param to The node that the edge ends at.
     * @param elementIndex The index of the element in the collection
     */
    public static void removeEdge(DescriptionGraph graph, LudemeNode from, LudemeNode to, int elementIndex)
    {
        removeEdge(graph, from, to, elementIndex, true);
    }

    /**
     * Removes an edge between two nodes of a given collection element index.
     * @param graph The graph that contains the nodes.
     * @param from The node that the edge starts from.
     * @param to The node that the edge ends at.
     * @param elementIndex The index of the element in the collection
     * @param notify Whether the graph panel should be notified about the removal of the edge
     */
    public static void removeEdge(DescriptionGraph graph, LudemeNode from, LudemeNode to, int elementIndex, boolean notify)
    {
        if(DEBUG) System.out.println("[HANDLER] removeEdge(graph, from, to, elementIndex) -> Removing edge: " + from.title() + " -> " + to.title() + " at index " + elementIndex);
        graph.removeEdge(from.id(), to.id());
        from.removeChildren(to);
        to.setParent(null);
        if(notify)
        {
            IGraphPanel graphPanel = graphPanelMap.get(graph);
            graphPanel.notifyEdgeRemoved(graphPanel.nodeComponent(from), graphPanel.nodeComponent(to), elementIndex);
        }
    }


    /**
     * An optional terminal argument was activated/deactivated
     * @param graph
     * @param node
     * @param argument
     * @param activate
     */
    public static void activateOptionalTerminalField(DescriptionGraph graph, LudemeNode node, NodeArgument argument, boolean activate)
    {
        IGraphPanel graphPanel = graphPanelMap.get(graph);
        IUserAction action = new ActivateOptionalTerminalAction(graphPanel, node, argument, activate);

        addAction(action);
        if(lastActionEquals(action))
            Handler.recordUserActions = false;
        graphPanel.notifyTerminalActivated(graphPanel.nodeComponent(node), argument, activate);
        if(lastActionEquals(action))
            Handler.recordUserActions = true;
    }


    // ~~~~~~~  Node Input Updates  ~~~~~~~

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
            IUserAction lastAction = null;
            if(!currentPerformedUserActions.isEmpty())
            {
                lastAction = currentPerformedUserActions.peek();
            }
            if(lastAction instanceof AddedNodeAction)
            {
                if(!(((AddedNodeAction) lastAction).addedNode() == input && ((AddedNodeAction) lastAction).addedNode().creatorArgument() == nodeArgument))
                {
                    addAction(new AddedConnectionAction(graphPanel, node, (LudemeNode) input, nodeArgument));
                }
            }
            else
            {
                addAction(new AddedConnectionAction(graphPanel, node, (LudemeNode) input, nodeArgument));
            }
        }
        if(input == null)
        {
            Object oldInput = node.providedInputsMap().get(nodeArgument);
            if(oldInput instanceof LudemeNode)
            {
                addAction(new RemovedConnectionAction(graphPanelMap.get(graph), node, (LudemeNode) oldInput, nodeArgument));
                //node.removeChildren((LudemeNode) oldInput);
                removeEdge(graph, node, (LudemeNode) oldInput, false);
            }
        }

        node.setProvidedInput(nodeArgument, input);

        if(node.isSatisfied() && graphPanel.nodeComponent(node) != null)
        {
            LudemeNodeComponent lnc = graphPanel.nodeComponent(node);
            if(lnc.isMarkedUncompilable())
            {
                lnc.markUncompilable(false);
            }
        }
        // if its a define graph, notify the graph about that
        if(graph.isDefine())
            graph.notifyDefineChanged(nodeArgument, input);

        if(!graph.isDefine() && liveCompile && isConnectedToRoot(graph, node))
            compile();
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
            node.setProvidedInput(nodeArgument, new Object[1]);

        if(input == null && node.providedInputsMap().get(nodeArgument) instanceof Object[] && elementIndex >= ((Object[])(node.providedInputsMap().get(nodeArgument))).length)
            return;

        Object oldInput = node.providedInputsMap().get(nodeArgument);
        if(oldInput instanceof Object[] && input == null && ((Object[])(oldInput)).length > elementIndex && ((Object[])oldInput)[elementIndex] instanceof LudemeNode)
        {
            Handler.removeEdge(graph, node, (LudemeNode) ((Object[])(oldInput))[elementIndex], elementIndex, false);
        }

        if(node.providedInputsMap().get(nodeArgument) == null)
            return;

        while(elementIndex >= ((Object[])(node.providedInputsMap().get(nodeArgument))).length)
            addCollectionElement(graph, node, nodeArgument);
        Object[] in = (Object[]) oldInput;
        in[elementIndex] = input;
        Handler.updateInput(graph, node, nodeArgument, in);
    }


    // ~~~~~~~  Defines  ~~~~~~~

    /**
     *
     * @return A list of define ludeme nodes
     */
    public static List<LudemeNode> defineNodes()
    {
        List<LudemeNode> list = new ArrayList<>();
        for(DefineGraphPanel dgp : defineGraphPanels)
        {
            LudemeNode defineNode = dgp.graph().defineNode();
            if(defineNode != null)
            {
                list.add(defineNode);
            }
        }
        return list;
    }

    /**
     * Updates all define nodes' symbol. Called when the title was changed.
     * The effect of this is that the title of the affected define nodes is changed.
     * @param graph The modified define graph
     * @param symbol The new symbol
     */
    public static void updateDefineNodes(DescriptionGraph graph, Symbol symbol)
    {
        if(DEBUG) System.out.println("[HANDLER] Title of Define Node changed to " + symbol.name());
        defineEditor.updateName(symbol.name());
        // Get List of define nodes
        List<LudemeNode> defineNodes = defineLudemeNodes.get(graph);
        if(defineNodes == null)
            return;
        for(LudemeNode ln : defineNodes)
        {
            // Update symbol
            ln.updateDefineNode(symbol);
            // Update component
            // get graph panel
            IGraphPanel gp = graphPanelMap.get(defineNodeToGraphMap.get(ln));
            // get component and update component
            gp.nodeComponent(ln).repaint();
        }

    }

    /**
     * Updates all define nodes' list of required parameters. Called when the parameters were changed.
     *     1) currentNodeArguments field in LudemeNode updated
     *     2) Remove invalid collections
     *     3) Add/Remove input fields according to new list
     * @param graph
     * @param parameters
     */
    public static void updateDefineNodes(DescriptionGraph graph, List<NodeArgument> parameters)
    {
        if(DEBUG) System.out.println("[HANDLER] Parameters of Define Node changed: " + parameters);
        // Get List of define nodes
        List<LudemeNode> defineNodes = defineLudemeNodes.get(graph);
        if(defineNodes == null)
            return;

        for(LudemeNode ln : defineNodes)
        {
            // Update parameters
            ln.updateDefineNode(parameters);
            // Update component
            // get graph panel
            IGraphPanel gp = graphPanelMap.get(defineNodeToGraphMap.get(ln));
            // get component and update component
            gp.nodeComponent(ln).changedArguments(parameters);
        }
    }

    public static void updateDefineNodes(DescriptionGraph graph, LudemeNode macroNode)
    {
        if(DEBUG) System.out.println("[HANDLER] Macro Node changed");
        // Get List of define nodes
        List<LudemeNode> defineNodes = defineLudemeNodes.get(graph);
        if(defineNodes == null)
            return;

        for(LudemeNode ln : defineNodes)
        {
            // Update macro node
            ln.updateDefineNode(macroNode);
            // remove ingoing connections
            if(ln.parentNode() != null)
            {
                removeEdge(defineNodeToGraphMap.get(ln), ln.parentNode(), ln);
            }
        }
    }

    /**
     * Removes a define
     * @param graph
     */
    public static void removeDefine(DescriptionGraph graph)
    {
        defineEditor.removalGraph(graphPanelMap.get(graph));
    }


    // ~~~~~~~  Compiling  ~~~~~~~

    /**
     * Attempts to compile the graph.
     * @return Array with 3 elements: Game (or null), List of Error Messages, List of unsatisfied nodes.
     */
    public static Object[] compile()
    {
        return compile(false);
    }

    /**
     * Attempts to compile the graph.
     * @param openDialog Whether a dialog displaying the errors should be opened.
     * @return Array with 3 elements: Game (or null), List of Error Messages, List of unsatisfied nodes.
     */
    public static Object[] compile(boolean openDialog)
    {
        if(!recordUserActions)
            return new Object[]{null, null, null};
        Object[] output = new Object[3];
        List<LudemeNode> unsatisfiedNodes = isComplete(gameGraphPanel.graph());
        if(!unsatisfiedNodes.isEmpty())
        {
            List<String> errors = new ArrayList<>();
            StringBuilder errorMessage = new StringBuilder("Nodes are missing required arguments:\n");
            for(LudemeNode node : unsatisfiedNodes)
            {
                errorMessage.append(node.title()).append(", \n");
            }
            errors.add(errorMessage.toString());
            output[1] = errors;
            output[2] = unsatisfiedNodes;
            if(liveCompile)
            {
                lastCompile = output;
                if(toolsPanel != null)
                {
                    toolsPanel.play.updateCompilable(output);
                }
            }
            if(openDialog)
            {
                Handler.markUncompilable();
                JOptionPane.showMessageDialog(null, errorMessage.toString(), "Couldn't compile", JOptionPane.ERROR_MESSAGE);
            }
            return output;
        }

        Description d = gameGraphPanel.graph().description();
        Report r = new Report();
        try
        {
            if (StartVisualEditor.app != null)
            {
                output[0] = compiler.Compiler.compile(d, StartVisualEditor.app.manager().settingsManager().userSelections(), r, false);
            }
            else
            {
                output[0] = compiler.Compiler.compile(d, new UserSelections(new ArrayList<>()), r, false);
            }
        }
        catch(Exception ignored)
        {}

        output[1] = r.errors();
        if(liveCompile)
        {
            lastCompile = output;
            if(toolsPanel != null)
            {
                toolsPanel.play.updateCompilable(output);
            }
        }
        if(output[0] == null && openDialog)
        {
            java.util.List<String> errors = (List<String>) output[1];
            String errorMessage;
            if (errors.isEmpty())
            {
                errorMessage = "Could not create \"game\" ludeme from description.";
            }
            else
            {
                errorMessage = errors.toString();
                errorMessage = errorMessage.substring(1, errorMessage.length() - 1);
            }
            JOptionPane.showMessageDialog(null, errorMessage, "Couldn't compile", JOptionPane.ERROR_MESSAGE);
        }
        return output;
    }

    /**
     * Marks uncompilable nodes in the graph.
     */
    public static void markUncompilable()
    {
        List<LudemeNode> unsatisfiedNodes = isComplete(gameGraphPanel.graph());
        List<LudemeNodeComponent> lncs = new ArrayList<>();
        for(LudemeNode node : unsatisfiedNodes)
            lncs.add(gameGraphPanel.nodeComponent(node));
        gameGraphPanel.notifyUncompilable(lncs);
    }

    /**
     * Executes the game
     */
    public static void play()
    {
        Object[] output = lastCompile;
        // first compile
        if(lastCompile == null)
            output = compile();
        if(output[0] == null)
            return;
        Game game = (Game) output[0];
        // load game
        loadGame(game, StartVisualEditor.app);
    }

    /**
     * Executes a given game
     * @param game
     */
    public static void play(Game game)
    {
        loadGame(game, StartVisualEditor.app);
    }

    /**
     * Loads the game into the game engine.
     * @param game
     * @param app
     */
    public static void loadGame(Game game, PlayerApp app)
    {
        app.manager().ref().setGame(app.manager(), game);
        GameUtil.startGame(app);
        app.restartGame();
        DesktopApp.frame().requestFocus();
    }

    /**
     *
     * @return The .lud equivalent of the game graph
     */
    public static String toLud()
    {
        return gameGraphPanel.graph().toLud();
    }

    // ~~~~~~~  Selecting Nodes  ~~~~~~~

    /**
     *
     * @param graph
     * @return The selected nodes of a graph
     */
    public static List<LudemeNode> selectedNodes(DescriptionGraph graph)
    {
        IGraphPanel graphPanel = graphPanelMap.get(graph);
        List<LudemeNode> nodes = new ArrayList<>();
        for(LudemeNodeComponent lnc : graphPanel.selectedLnc())
            nodes.add(lnc.node());
        return nodes;
    }

    /**
     * Selects all nodes of the current graph.
     */
    public static void selectAll()
    {
        selectAll(currentGraphPanel.graph());
    }

    /**
     * Unselects all nodes of the current graph.
     */
    public static void unselectAll()
    {
        currentGraphPanel.deselectEverything();
    }

    /**
     * Selects all nodes of a graph.
     * @param graph
     */
    public static void selectAll(DescriptionGraph graph)
    {
        IGraphPanel graphPanel = graphPanelMap.get(graph);
        graphPanel.selectAllNodes();
    }

    /**
     * Selects a node
     * @param node
     */
    public static void selectNode(LudemeNodeComponent node)
    {
        currentGraphPanel.addNodeToSelections(node);
        currentGraphPanel.repaint();
    }

    /**
     * Activates the selection mode
     */
    public static void activateSelectionMode()
    {
        currentGraphPanel.enterSelectionMode();
    }

    /**
     * Deactivates the selection mode
     */
    public static void deactivateSelectionMode()
    {
        currentGraphPanel.exitSelectionMode();
        toolsPanel.deactivateSelection();
        currentGraphPanel.repaint();
    }

    /**
     * Turns off the selection button in the tools panel.
     */
    public static void turnOffSelectionBtn()
    {
        toolsPanel.deactivateSelection();
        toolsPanel.repaint();
        toolsPanel.revalidate();
    }


    // ~~~~~~~  Copying Nodes  ~~~~~~~

    /**
     * Copies the selected nodes of the currently active graph panel.
     */
    public static void copy()
    {
        copy(currentGraphPanel.graph());
    }

    /**
     * Copies the selected nodes of the given graph.
     * @param graph
     */
    public static void copy(DescriptionGraph graph)
    {
        IGraphPanel graphPanel = graphPanelMap.get(graph);
        List<LudemeNode> toCopy = new ArrayList<>();
        for(LudemeNodeComponent lnc : graphPanel.selectedLnc())
            if(graph.getRoot() != lnc.node())
                toCopy.add(lnc.node());
        copy(toCopy);
    }

    /**
     * Copies the given nodes.
     * @param nodes
     */
    public static void copy(List<LudemeNode> nodes)
    {
        if(nodes.isEmpty())
            return;
        if(DEBUG) System.out.println("[HANDLER] copy(graph, nodes) Copying " + nodes.size() + " nodes");
        clipboard.clear();
        clipboard = copyTemporarily( nodes);
    }

    /**
     * Copies the given nodes and returns a list of the copied nodes.
     * @param nodes
     * @return
     */
    public static List<LudemeNode> copyTemporarily(List<LudemeNode> nodes)
    {
        if(nodes.isEmpty())
            return new ArrayList<>();
        if(DEBUG) System.out.println("[HANDLER] copy(graph, nodes) Copying " + nodes.size() + " nodes");

        HashMap<LudemeNode, LudemeNode> copiedNodes = new HashMap<>(); // <original, copy>

        // create copies
        for(LudemeNode node : nodes)
            copiedNodes.put(node, node.copy());
        // fill inputs (connections and collections)
        for(LudemeNode node : nodes)
        {
            LudemeNode copy = copiedNodes.get(node);
            // iterate each original node's provided inputs
            for (NodeArgument arg : node.providedInputsMap().keySet())
            {
                Object input = node.providedInputsMap().get(arg);
                // input is a node
                if (input instanceof LudemeNode)
                {
                    LudemeNode inputNode = (LudemeNode) input;
                    // if input node is in the list of nodes to copy, copy it
                    if (nodes.contains(inputNode))
                    {
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
                    for (int i = 0; i < inputCollection.length; i++)
                    {
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
        return new ArrayList<>(copiedNodes.values());
    }

    /**
     *
     * @return List of currently copied nodes
     */
    public static List<LudemeNode> clipboard()
    {
        return clipboard;
    }


    // ~~~~~~~  Pasting Nodes  ~~~~~~~

    /**
     * Pastes nodes copied to the clipboard
     * @param x x-coordinate of the paste location
     * @param y y-coordinate of the paste location
     */
    public static void paste(int x, int y)
    {
        paste(currentGraphPanel.graph(), x, y);
    }

    /**
     * Pastes nodes copied to the clipboard to the given graph
     * @param graph
     * @param x
     * @param y
     */
    public static void paste(DescriptionGraph graph, int x, int y)
    {
        if(clipboard.isEmpty())
            return;
        // old copy
        List<LudemeNode> oldCopy = new ArrayList<>(clipboard);
        clipboard.clear();
        copy(oldCopy);
        paste(graph, oldCopy, x, y);
    }

    /**
     * Pastes a list of nodes
     * @param graph
     * @param nodes
     * @param x x-coordinate of the first node
     * @param y y-coordinate of the first node
     */
    public static void paste(DescriptionGraph graph, List<LudemeNode> nodes, int x, int y)
    {
        if(DEBUG) System.out.println("[HANDLER] paste(graph, x, y) Pasting " + nodes.size() + " nodes");
        recordUserActions = false;
        IGraphPanel graphPanel = graphPanelMap.get(graph);
        // find left-most node
        LudemeNode leftMostNode = nodes.get(0);
        for(LudemeNode node : nodes)
            if(node.x() < leftMostNode.x())
                leftMostNode = node;

        int x_shift, y_shift;

        if(x == -1 && y == -1)
        {
            Rectangle v = graphPanel.parentScrollPane().getViewport().getViewRect();
            x_shift = (int) (v.x - leftMostNode.x() + (v.getWidth()/2));
            y_shift = (int) (v.y - leftMostNode.y() + (v.getHeight()/2));

        }
        else
        {
            x_shift = x - leftMostNode.x();
            y_shift = y - leftMostNode.y();
        }

        // add nodes to graph
        for(LudemeNode node : nodes)
        {
            node.setX(node.x() + x_shift);
            node.setY(node.y() + y_shift);
            addNode(graph, node);
        }
        // update all edges
        for(LudemeNode parent : nodes)
        {
            for(NodeArgument argument : parent.providedInputsMap().keySet())
            {
                Object input = parent.providedInputsMap().get(argument);
                if(input instanceof LudemeNode)
                {
                    LudemeNode inputNode = (LudemeNode) input;
                    if(nodes.contains(inputNode))
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
                            if(nodes.contains(inputNode))
                            {
                                addEdge(graph, parent, inputNode, argument, i);
                            }
                        }
                        else if(i>0)
                        {
                            addCollectionElement(graph, parent, argument);
                        }
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
        IUserAction action = new PasteAction(graphPanel, nodes);
        addAction(action);
    }


    // ~~~~~~~  Duplicating Nodes  ~~~~~~~

    /**
     * Duplicates the currently selected nodes of the currently active graph
     */
    public static void duplicate()
    {
        duplicate(currentGraphPanel.graph());
    }

    /**
     * Duplicates the currently selected nodes
     * @param graph
     */
    public static void duplicate(DescriptionGraph graph)
    {
        List<LudemeNode> nodes = selectedNodes(graph);
        duplicate(graph, nodes);
    }

    /**
     * Duplicates a list of nodes
     * @param graph
     * @param nodes
     */
    public static void duplicate(DescriptionGraph graph, List<LudemeNode> nodes)
    {
        // remove root node
        nodes.remove(graph.getRoot());
        if(nodes.isEmpty())
            return;
        // get left most node
        LudemeNode leftMostNode = nodes.get(0);
        for(LudemeNode node : nodes)
            if(node.x() < leftMostNode.x())
                leftMostNode = node;
        int y = leftMostNode.y() + leftMostNode.height();
        paste(graph, copyTemporarily(nodes), leftMostNode.x(), y);
    }


    // ~~~~~~~  Collapsing and Expanding Nodes  ~~~~~~~

    /**
     * Collapses the currently selected nodes of the currently active graph
     */
    public static void collapse()
    {
        collapse(currentGraphPanel.graph());
    }

    /**
     * Collapses the currently selected nodes of a given graph
     * @param graph
     */
    public static void collapse(DescriptionGraph graph)
    {
        List<LudemeNode> selectedNodes = selectedNodes(graph);
        if(selectedNodes.isEmpty())
            return;
        if(selectedNodes.size() == 1)
        {
            if(selectedNodes.get(0).parentNode() != null)
            {
                collapseNode(graph, selectedNodes.get(0), true);
            }
            return;
        }
        List<LudemeNode> roots = new ArrayList<>();
        for(LudemeNode node : selectedNodes)
            if(node.parentNode() == null)
                roots.add(node);
            else if(!selectedNodes.contains(node.parentNode()))
                roots.add(node.parentNode());

        // find all subtree root's children
        List<LudemeNode> toCollapse = new ArrayList<>();
        for(LudemeNode node : selectedNodes)
            if(roots.contains(node.parentNode()))
                toCollapse.add(node);

        for(LudemeNode node : toCollapse)
            collapseNode(graph, node, true);
        graphPanelMap.get(graph).deselectEverything();
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

    /**
     * Expands the currently selected nodes
     */
    public static void expand()
    {
        expand(currentGraphPanel.graph());
    }

    /*
     * Expands the currently selected nodes of a given graph
     */
    public static void expand(DescriptionGraph graph)
    {
        List<LudemeNode> selectedNodes = selectedNodes(graph);
        // find all subtree root
        List<LudemeNode> toExpand = new ArrayList<>();
        for(LudemeNode node : selectedNodes)
            toExpand.addAll(node.childrenNodes());

        for (LudemeNode node : toExpand)
            collapseNode(graph, node, false);

        graphPanelMap.get(graph).deselectEverything();
    }


    // ~~~~~~~  Undo and Redo  ~~~~~~~

    /**
     * Undoes the last action
     */
    public static void undo()
    {
        if(currentPerformedUserActions.isEmpty())
            return;
        currentUndoAction = currentPerformedUserActions.pop();
        if(DEBUG) System.out.println("[HANDLER] undo() Undoing " + currentUndoAction.actionType());
        currentUndoneUserActions.add(currentUndoAction);
        Handler.recordUserActions = false;
        currentUndoAction.undo();
        currentUndoAction.graphPanel().repaint();
        Handler.recordUserActions = true;
        if(DEBUG) System.out.println("[HANDLER] undo() Completed " + currentUndoAction.actionType());
        toolsPanel.updateUndoRedoBtns(currentPerformedUserActions, currentUndoneUserActions);
        if(liveCompile)
            compile();
        currentUndoAction = null;
    }

    /**
     * Redoes the last undone action
     */
    public static void redo()
    {
        if(currentUndoneUserActions.isEmpty())
            return;
        currentRedoAction = currentUndoneUserActions.pop();
        if(DEBUG) System.out.println("[HANDLER] redo() Redoing " + currentRedoAction.actionType());
        currentPerformedUserActions.add(currentRedoAction);
        Handler.recordUserActions = false;
        currentRedoAction.redo();
        currentRedoAction.graphPanel().repaint();
        Handler.recordUserActions = true;
        if(DEBUG) System.out.println("[HANDLER] redo() Completed " + currentRedoAction.actionType());
        toolsPanel.updateUndoRedoBtns(currentPerformedUserActions, currentUndoneUserActions);
        if(liveCompile)
            compile();
        currentRedoAction = null;
    }

    /**
     * Adds an action to the undo stack
     * @param action
     */
    private static void addAction(IUserAction action)
    {
        if(!recordUserActions)
            return;
        if(DEBUG) System.out.println("Adding action: " + action.actionType());
        currentPerformedUserActions.add(action);
        currentUndoneUserActions = new Stack<>();
        toolsPanel.updateUndoRedoBtns(currentPerformedUserActions, currentUndoneUserActions);
    }

    /**
     *
     * @param action
     * @return Whether the last action equals a given user action
     */
    private static boolean lastActionEquals(IUserAction action)
    {
        if(currentPerformedUserActions.isEmpty())
            return false;
        return currentPerformedUserActions.peek() == action;
    }


    // ~~~~~~~  Appearance  ~~~~~~~

    /**
     * Sets the current active design palette
     * @param palette
     */
    public static void setPalette(String paletteName)
    {
        DesignPalette.loadPalette(paletteName);
        for(IGraphPanel graphPanel : graphPanelMap.values())
            graphPanel.repaint();
    }

    public static List<String> palettes()
    {
        return DesignPalette.palettes();
    }

    /**
     * Sets the background
     * @param background
     */
    public static void setBackground(IBackground background)
    {
        currentBackground = background;
        for(IGraphPanel graphPanel : graphPanelMap.values())
            graphPanel.repaint();
    }

    /**
     *
     * @return The current background
     */
    public static IBackground currentBackground()
    {
        return currentBackground;
    }

    public static void setFont(String size)
    {
        switch(size)
        {
            case "Small":
                DesignPalette.makeSizeSmall();
                break;
            case "Medium":
                DesignPalette.makeSizeMedium();
                break;
            case "Large":
                DesignPalette.makeSizeLarge();
                break;
        }
        for(IGraphPanel graphPanel : graphPanelMap.values())
            graphPanel.repaint();
    }

    // ~~~~~~~  Utility  ~~~~~~~

    /**
     * If the graph is complete (all required inputs filled)
     * @param graph
     * @return
     */
    public static List<LudemeNode> isComplete(DescriptionGraph graph)
    {
        List<LudemeNode> unsatisfiedNodes = new ArrayList<>();
        for(LudemeNode ln : graph.getNodes())
            if((graph.getRoot() == ln || ln.parentNode()!=null) && !ln.isSatisfied())
                unsatisfiedNodes.add(ln);
        return unsatisfiedNodes;
    }


    /**
     * Whether a node has a path to the root
     * @param graph
     * @param node
     * @return
     */
    public static boolean isConnectedToRoot(DescriptionGraph graph, LudemeNode node)
    {
        LudemeNode last = node;
        while(last.parentNode() != null)
            last = last.parentNode();
        return last == graph.getRoot();
    }

    /**
     * Updates the position of a node
     * @param node node in operation
     * @param x x coordinate
     * @param y y coordinate
     */
    public static void updatePosition(LudemeNode node, int x, int y)
    {
        node.setPos(x, y);
    }


    public static Dimension getViewPortSize()
    {
        return currentGraphPanel.parentScrollPane().getViewport().getSize();
    }


    public static void reconstruct(IGraphPanel graph, LudemeNode node)
    {
        LudemeNodeComponent lnc = graph.nodeComponent(node);
        for(LInputField lif : lnc.inputArea().currentInputFields)
        {
            lif.reconstruct();
        }
        lnc.inputArea().drawInputFields();
    }

    public static void updateInputs(IGraphPanel graph, LudemeNode node)
    {
        LudemeNodeComponent lnc = graph.nodeComponent(node);
        lnc.inputArea().updateProvidedInputs();
    }

}