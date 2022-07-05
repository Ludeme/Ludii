package app.display.dialogs.visual_editor.handler;

import app.DesktopApp;
import app.PlayerApp;
import app.display.dialogs.visual_editor.StartVisualEditor;
import app.display.dialogs.visual_editor.model.DescriptionGraph;
import app.display.dialogs.visual_editor.model.Edge;
import app.display.dialogs.visual_editor.model.LudemeNode;
import app.display.dialogs.visual_editor.model.NodeArgument;
import app.display.dialogs.visual_editor.model.UserActions.*;
import app.display.dialogs.visual_editor.view.VisualEditorPanel;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.LudemeNodeComponent;
import app.display.dialogs.visual_editor.view.designPalettes.DesignPalette;
import app.display.dialogs.visual_editor.view.designPalettes.DesignPaletteDark;
import app.display.dialogs.visual_editor.view.designPalettes.DesignPaletteLight;
import app.display.dialogs.visual_editor.view.panels.IGraphPanel;
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
import main.grammar.Clause;
import main.grammar.Description;
import main.grammar.Report;
import main.grammar.Symbol;
import main.options.UserSelections;

import java.util.*;
import java.util.List;
import javax.swing.*;
import java.awt.*;


public class Handler
{

    // Single EditorPanel
    public static GameGraphPanel gameGraphPanel;

    // Single ToolsPanel
    public static ToolsPanel toolsPanel;

    public static LayoutSettingsPanel lsPanel;

    public static VisualEditorPanel visualEditorPanel;

    private static final Map<IGraphPanel, Stack<IUserAction>> performedUserActionsMap = new HashMap<>();
    private static final Map<IGraphPanel, Stack<IUserAction>> undoneUserActionsMap = new HashMap<>();
    private static Stack<IUserAction> currentPerformedUserActions;
    private static Stack<IUserAction> currentUndoneUserActions;
    public static IUserAction currentUndoAction;
    public static IUserAction currentRedoAction;
    public static boolean recordUserActions = true;

    private static final HashMap<DescriptionGraph, IGraphPanel> graphPanelMap = new HashMap<>();
    public static IGraphPanel currentGraphPanel;

    private static final boolean DEBUG = true;

    public static Object[] lastCompile;

    public static boolean liveCompile = true;

    public static boolean animation = true;

    public static boolean autoplacement = false;

    public static final boolean sidebarVisible = true;

    public static final IBackground DotGridBackground = new DotGridBackground();
    public static final IBackground EmptyBackground = new EmptyBackground();
    public static final IBackground CartesianGridBackground = new CartesianGridBackground();
    private static IBackground currentBackground = DotGridBackground;

    public static final DesignPalette lightPalette = DesignPaletteLight.instance();
    public static final DesignPalette darkPalette = DesignPaletteDark.instance();

    public static DesignPalette currentPalette = lightPalette;

    public static final int SENSITIVITY_COLLECTION_REMOVAL = 4;
    public static final int SENSITIVITY_REMOVAL = 6;

    public static final Symbol PARAMETER_SYMBOL = new Symbol(Symbol.LudemeType.Ludeme, "PARAMETER", "PARAMETER", null);

    public static final List<DefineGraphPanel> DEFINE_GRAPH_PANELS = new ArrayList<>();

    public static void updateCurrentGraphPanel(IGraphPanel graphPanel)
    {
        currentGraphPanel = graphPanel;
        currentPerformedUserActions = performedUserActionsMap.get(graphPanel);
        currentUndoneUserActions = undoneUserActionsMap.get(graphPanel);
        if(toolsPanel != null)
            toolsPanel.updateUndoRedoBtns(currentPerformedUserActions, currentUndoneUserActions);
    }


    public static List<LudemeNode> defineNodes()
    {
        List<LudemeNode> list = new ArrayList<>();
        for(DefineGraphPanel dgp : DEFINE_GRAPH_PANELS)
        {
            LudemeNode defineNode = dgp.graph().defineNode();
            if(defineNode != null)
                list.add(defineNode);
        }
        return list;
    }

    public static void setPalette(DesignPalette palette)
    {
        currentPalette = palette;
        for(IGraphPanel graphPanel : graphPanelMap.values())
        {
            graphPanel.repaint();
        }
    }

    public static DesignPalette currentPalette()
    {
        return currentPalette;
    }

    public static IBackground currentBackground()
    {
        return currentBackground;
    }

    public static void setBackground(IBackground background)
    {
        currentBackground = background;
        for(IGraphPanel graphPanel : graphPanelMap.values())
            graphPanel.repaint();
    }


    public static Object[] compile()
    {
        return compile(false);
    }

    // first element = Game (or null), second element = Error Messages, third element = List of Nodes that are not satisfied
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
                output[0] = compiler.Compiler.compile(d, new UserSelections(new ArrayList<>()), r, false);
        }
        catch(Exception ex)
        {
            System.out.println("Couldnt compile");
            System.out.println(r.errors());
        }

        output[1] = r.errors();
        if(liveCompile)
        {
            lastCompile = output;
            if(toolsPanel != null)
                toolsPanel.play.updateCompilable(output);
        }
        if(output[0] == null && openDialog)
        {
            java.util.List<String> errors = (List<String>) output[1];
            String errorMessage;
            if (errors.isEmpty())
                errorMessage = "Could not create \"game\" ludeme from description.";
            else
            {
                errorMessage = errors.toString();
                errorMessage = errorMessage.substring(1, errorMessage.length() - 1);
            }
            JOptionPane.showMessageDialog(null, errorMessage, "Couldn't compile", JOptionPane.ERROR_MESSAGE);
        }
        return output;
    }

    public static void markUncompilable()
    {
        List<LudemeNode> unsatisfiedNodes = isComplete(gameGraphPanel.graph());
        List<LudemeNodeComponent> lncs = new ArrayList<>();
        for(LudemeNode node : unsatisfiedNodes)
            lncs.add(gameGraphPanel.nodeComponent(node));
        gameGraphPanel.notifyUncompilable(lncs);
    }

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

    public static void play(Game game)
    {
        // load game
        loadGame(game, StartVisualEditor.app);
    }

    public static void loadGame(Game game, PlayerApp app)
    {
        app.manager().ref().setGame(app.manager(), game);
        GameUtil.startGame(app);
        app.restartGame();
        DesktopApp.frame().requestFocus();
    }

    public static List<LudemeNode> isComplete(DescriptionGraph graph)
    {
        List<LudemeNode> unsatisfiedNodes = new ArrayList<>();
        for(LudemeNode ln : graph.getNodes())
            if((graph.getRoot() == ln || ln.parentNode()!=null) && !ln.isSatisfied())
                unsatisfiedNodes.add(ln);
        return unsatisfiedNodes;
    }

    public static Map<LudemeNode, List<NodeArgument>> defineParameters(DescriptionGraph graph)
    {
        assert graph.isDefine();
        LinkedHashMap<LudemeNode, List<NodeArgument>> parameters = new LinkedHashMap<>();
        for(LudemeNode ln : graph.getNodes())
            if(graph.getRoot() != ln && connectedToRoot(graph, ln))
            {
                List<NodeArgument> args = ln.unfilledRequiredArguments();
                if(!args.isEmpty())
                    parameters.put(ln, args);
            }
        return parameters;
    }

    private static boolean connectedToRoot(DescriptionGraph graph, LudemeNode node)
    {
        LudemeNode n = node;
        while(n.parentNode() != null)
            n = n.parentNode();
        return n == graph.getRoot();
    }

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
        if(graph.getRoot() == node) return;
        if(DEBUG) System.out.println("[HANDLER] removeNode(graph, node) -> Removing node: " + node.title());

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
                Handler.updateInput(graph, node.parentNode(), args.get(index2), null); // TODO: what about collection?
        }
        // remove edge
        // TODO: ConenctionHandler
        graph.removeEdge(node.id());
        // notify graph panel
        IGraphPanel graphPanel = graphPanelMap.get(graph);
        graphPanel.notifyNodeRemoved(graphPanel.nodeComponent(node));
        if(lastActionEquals(action)) Handler.recordUserActions = true;
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
        if(nodes.size() >= SENSITIVITY_REMOVAL)
        {
            int userChoice = JOptionPane.showConfirmDialog(null, "Are you sure you want to remove " + nodes.size() + " nodes?", "Remove nodes", JOptionPane.YES_NO_OPTION);
            if(userChoice != JOptionPane.YES_OPTION)
                return;
        }

        // remove root node and already deleted nodes
        for(LudemeNode node : new ArrayList<>(nodes))
            if(graph.getRoot() == node)
            {
                nodes.remove(node);
                break;
        }

        IUserAction action = new RemovedNodesAction(graphPanelMap.get(graph), nodes);
        addAction(action);

        if(lastActionEquals(action)) Handler.recordUserActions = false;
        for(LudemeNode n : nodes) removeNode(graph, n);
        if(lastActionEquals(action)) Handler.recordUserActions = true;
    }

    public static void remove()
    {
        remove(currentGraphPanel.graph());
    }

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
        while(from.providedInputsMap().get(nodeArgument) == null || elementIndex+1>((Object[])from.providedInputsMap().get(nodeArgument)).length)
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
        removeEdge(graph, from, to, true);
    }

    public static void removeEdge(DescriptionGraph graph, LudemeNode from, LudemeNode to, boolean notify){
        if(DEBUG) System.out.println("[HANDLER] removeEdge(graph, from, to) -> Removing edge: " + from.title() + " -> " + to.title());
        graph.removeEdge(from.id(), to.id());
        from.removeChildren(to);
        to.setParent(null);
        if(notify) {
            IGraphPanel graphPanel = graphPanelMap.get(graph);
            graphPanel.notifyEdgeRemoved(graphPanel.nodeComponent(from), graphPanel.nodeComponent(to));
        }
    }

    public static void removeEdge(DescriptionGraph graph, LudemeNode from, LudemeNode to, int elementIndex){
        removeEdge(graph, from, to, elementIndex, true);
    }

    public static void removeEdge(DescriptionGraph graph, LudemeNode from, LudemeNode to, int elementIndex, boolean notify){
        if(DEBUG) System.out.println("[HANDLER] removeEdge(graph, from, to, elementIndex) -> Removing edge: " + from.title() + " -> " + to.title());
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
                if(lastActionEquals(action))
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
        if(!graphPanelMap.containsKey(graph))
        {
            graphPanelMap.put(graph, graphPanel);
            if(graphPanel.isDefineGraph())
                DEFINE_GRAPH_PANELS.add((DefineGraphPanel) graphPanel);
            performedUserActionsMap.put(graphPanel, new Stack<>());
            undoneUserActionsMap.put(graphPanel, new Stack<>());
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
            IUserAction lastAction = currentPerformedUserActions.peek();
            if(lastAction instanceof AddedNodeAction)
            {
                if(!(((AddedNodeAction) lastAction).addedNode() == input && ((AddedNodeAction) lastAction).addedNode().creatorArgument() == nodeArgument))
                    addAction(new AddedConnectionAction(graphPanel, node, (LudemeNode) input, nodeArgument));
            }
            else
                addAction(new AddedConnectionAction(graphPanel, node, (LudemeNode) input, nodeArgument));
        }
        if(input == null)
        {
            Object oldInput = node.providedInputsMap().get(nodeArgument);
            if(oldInput instanceof LudemeNode)
                addAction(new RemovedConnectionAction(graphPanelMap.get(graph), node, (LudemeNode) oldInput, nodeArgument));
        }
        // if the input is null but was a node before, remove the child from the parent
        if(input == null && node.providedInputsMap().get(nodeArgument) instanceof LudemeNode)
            node.removeChildren((LudemeNode) node.providedInputsMap().get(nodeArgument));
        node.setProvidedInput(nodeArgument, input);
        if(node.isSatisfied() && graphPanel.nodeComponent(node) != null)
        {
            LudemeNodeComponent lnc = graphPanel.nodeComponent(node);
            if(lnc.isMarkedUncompilable())
                lnc.markUncompilable(false);
        }
        if(!graph.isDefine() && liveCompile && isConnectedToRoot(graph, node)) compile();
    }

    private static boolean isConnectedToRoot(DescriptionGraph graph, LudemeNode node)
    {
        LudemeNode last = node;
        while(last.parentNode() != null)
            last = last.parentNode();
        return last == graph.getRoot();
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
                Handler.recordUserActions = false;
            updateInput(graph, node, nodeArgument, new Object[2]);
            if(lastActionEquals(action))
                Handler.recordUserActions = true;
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

        if(input == null && node.providedInputsMap().get(nodeArgument) instanceof Object[] && elementIndex >= ((Object[])(node.providedInputsMap().get(nodeArgument))).length) return;

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


    private static List<LudemeNode> currentCopy = new ArrayList<>(); // current copy

    public static void copy()
    {
        copy(currentGraphPanel.graph());
    }

    public static void copy(DescriptionGraph graph)
    {
        IGraphPanel graphPanel = graphPanelMap.get(graph);
        List<LudemeNode> toCopy = new ArrayList<>();
        for(LudemeNodeComponent lnc : graphPanel.selectedLnc())
            if(graph.getRoot() != lnc.node())
                toCopy.add(lnc.node());
        copy(toCopy);
    }

    public static void copy(List<LudemeNode> nodes)
    {
        if(nodes.isEmpty())
            return;
        if(DEBUG) System.out.println("[HANDLER] copy(graph, nodes) Copying " + nodes.size() + " nodes");
        currentCopy.clear();
        currentCopy = copyTemporarily( nodes);
    }

    public static List<LudemeNode> copyTemporarily(List<LudemeNode> nodes)
    {
        if(nodes.isEmpty()) return new ArrayList<>();
        if(DEBUG) System.out.println("[HANDLER] copy(graph, nodes) Copying " + nodes.size() + " nodes");

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
        return new ArrayList<>(copiedNodes.values());
    }

    public static void duplicate(DescriptionGraph graph, List<LudemeNode> nodes)
    {
        // remove root node
        nodes.remove(graph.getRoot());
        if(nodes.isEmpty()) return;
        // get left most node
        LudemeNode leftMostNode = nodes.get(0);
        for(LudemeNode node : nodes) if(node.x() < leftMostNode.x()) leftMostNode = node;
        int y = leftMostNode.y() + leftMostNode.height();
        paste(graph, copyTemporarily(nodes), leftMostNode.x(), y);
    }

    public static void duplicate(DescriptionGraph graph)
    {
        List<LudemeNode> nodes = selectedNodes(graph);
        duplicate(graph, nodes);
    }

    public static void duplicate()
    {
        duplicate(currentGraphPanel.graph());
    }

    public static void paste(DescriptionGraph graph, List<LudemeNode> nodes, int x, int y)
    {
        if(DEBUG) System.out.println("[HANDLER] paste(graph, x, y) Pasting " + nodes.size() + " nodes");
        recordUserActions = false;
        IGraphPanel graphPanel = graphPanelMap.get(graph);
        // find left-most node
        LudemeNode leftMostNode = nodes.get(0);
        for(LudemeNode node : nodes)
        {
            if(node.x() < leftMostNode.x()) leftMostNode = node;
        }

        int x_shift, y_shift;

        if(x == -1 && y == -1)
        {
            Point v = graphPanel.parentScrollPane().getViewport().getViewPosition();
            x_shift = v.x;
            y_shift = v.y;
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
        IUserAction action = new PasteAction(graphPanel, nodes);
        addAction(action);
    }

    public static void paste(int x, int y)
    {
        paste(currentGraphPanel.graph(), x, y);
    }

    public static void paste(DescriptionGraph graph, int x, int y)
    {
        if(currentCopy.isEmpty()) return;
        // old copy
        List<LudemeNode> oldCopy = new ArrayList<>(currentCopy);
        currentCopy.clear();
        copy(oldCopy);
        paste(graph, oldCopy, x, y);
    }
    /**
     *
     * @param node node in operation
     * @param x x coordinate
     * @param y y coordinate
     */
    public static void updatePosition(LudemeNode node, int x, int y)
    {
        node.setPos(x, y);
    }

    public static void collapse()
    {
        collapse(currentGraphPanel.graph());
    }

    public static void collapse(DescriptionGraph graph)
    {
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


    public static void expand()
    {
        expand(currentGraphPanel.graph());
    }
    public static void expand(DescriptionGraph graph)
    {
        List<LudemeNode> selectedNodes = selectedNodes(graph);
        // find all subtree root
        List<LudemeNode> toExpand = new ArrayList<>();
        for(LudemeNode node : selectedNodes)
        {
            toExpand.addAll(node.childrenNodes());
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

    public static void selectAll()
    {
        selectAll(currentGraphPanel.graph());
    }

    public static void unselectAll()
    {
        currentGraphPanel.deselectEverything();
    }

    public static void selectAll(DescriptionGraph graph)
    {
        IGraphPanel graphPanel = graphPanelMap.get(graph);
        graphPanel.selectAllNodes();
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
        if(lastActionEquals(action))
            Handler.recordUserActions = false;
        graphPanel.notifyTerminalActivated(graphPanel.nodeComponent(node), argument, activate);
        if(lastActionEquals(action))
            Handler.recordUserActions = true;
    }

    public static void activateSelectionMode()
    {
        currentGraphPanel.enterSelectionMode();
    }

    public static void deactivateSelectionMode()
    {
        currentGraphPanel.exitSelectionMode();
        toolsPanel.deactivateSelection();
        currentGraphPanel.repaint();
    }

    public static void turnOffSelectionBtn()
    {
        toolsPanel.deactivateSelection();
        toolsPanel.repaint();
        toolsPanel.revalidate();
    }

    private static boolean lastActionEquals(IUserAction action)
    {
        if(currentPerformedUserActions.isEmpty()) return false;
        return currentPerformedUserActions.peek() == action;
    }

    public static void undo()
    {
        if(currentPerformedUserActions.isEmpty()) return;
        currentUndoAction = currentPerformedUserActions.pop();
        if(DEBUG) System.out.println("[HANDLER] undo() Undoing " + currentUndoAction.actionType());
        currentUndoneUserActions.add(currentUndoAction);
        Handler.recordUserActions = false;
        currentUndoAction.undo();
        currentUndoAction.graphPanel().repaint();
        Handler.recordUserActions = true;
        if(DEBUG) System.out.println("[HANDLER] undo() Completed " + currentUndoAction.actionType());
        toolsPanel.updateUndoRedoBtns(currentPerformedUserActions, currentUndoneUserActions);
        if(liveCompile) compile();
        currentUndoAction = null;
    }

    public static void redo()
    {
        if(currentUndoneUserActions.isEmpty()) return;
        currentRedoAction = currentUndoneUserActions.pop();
        if(DEBUG) System.out.println("[HANDLER] redo() Redoing " + currentRedoAction.actionType());
        currentPerformedUserActions.add(currentRedoAction);
        Handler.recordUserActions = false;
        currentRedoAction.redo();
        currentRedoAction.graphPanel().repaint();
        Handler.recordUserActions = true;
        if(DEBUG) System.out.println("[HANDLER] redo() Completed " + currentRedoAction.actionType());
        toolsPanel.updateUndoRedoBtns(currentPerformedUserActions, currentUndoneUserActions);
        if(liveCompile) compile();
        currentRedoAction = null;
    }

    private static void addAction(IUserAction action)
    {
        if(!recordUserActions) return;
        if(DEBUG) System.out.println("Adding action: " + action.actionType());
        currentPerformedUserActions.add(action);
        currentUndoneUserActions = new Stack<>();
        toolsPanel.updateUndoRedoBtns(currentPerformedUserActions, currentUndoneUserActions);
    }


    public static void selectNode(LudemeNodeComponent node)
    {
        currentGraphPanel.addNodeToSelections(node);
        currentGraphPanel.repaint();
    }

    public static List<LudemeNode> copyList()
    {
        return currentCopy;
    }

    public static Dimension getViewPortSize()
    {
        return currentGraphPanel.parentScrollPane().getViewport().getSize();
    }
}