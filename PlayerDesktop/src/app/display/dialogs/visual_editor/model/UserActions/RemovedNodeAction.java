package app.display.dialogs.visual_editor.model.UserActions;

import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.model.DescriptionGraph;
import app.display.dialogs.visual_editor.model.LudemeNode;
import app.display.dialogs.visual_editor.model.NodeArgument;
import app.display.dialogs.visual_editor.view.panels.IGraphPanel;

import java.util.Arrays;
import java.util.LinkedHashMap;

/**
 * Created when a node is removed from the graph.
 * @author Filipp Dokienko
 */

public class RemovedNodeAction implements IUserAction
{
    private final IGraphPanel graphPanel;
    private final DescriptionGraph graph;
    private final LudemeNode removedNode;
    private boolean isUndone = false;
    private LudemeNode parent; // remembers the parent of the node
    private LinkedHashMap<NodeArgument, Object> removedData; // Inputs that were removed when the node was removed


    /**
     * Constructor.
     * @param graphPanel The graph panel that was affected by the action.
     * @param removedNode The node that was added.
     */
    public RemovedNodeAction(IGraphPanel graphPanel, LudemeNode removedNode)
    {
        this.graphPanel = graphPanel;
        this.graph = graphPanel.graph();
        this.removedNode = removedNode;

        parent = removedNode.parentNode();
        removedData = new LinkedHashMap<>(removedNode.providedInputsMap());
    }

    /**
     * @return The type of the action
     */
    @Override
    public IUserAction.ActionType actionType()
    {
        return ActionType.REMOVED_NODE;
    }

    /**
     * @return The graph panel that was affected by the action
     */
    @Override
    public IGraphPanel graphPanel()
    {
        return graphPanel;
    }

    /**
     * @return The description graph that was affected by the action
     */
    @Override
    public DescriptionGraph graph()
    {
        return graph;
    }

    /**
     * @return Whether the action was undone
     */
    @Override
    public boolean isUndone() {
        return isUndone;
    }

    /**
     * Undoes the action
     */
    @Override
    public void undo()
    {
        Handler.addNode(graph, removedNode);
        for(NodeArgument arg : removedData.keySet()) {
            Object input = removedData.get(arg);
            if(input == null) continue;
            if(input instanceof LudemeNode) Handler.addEdge(graph, removedNode, (LudemeNode) input, arg);
            Handler.updateInput(graph, removedNode, arg, input);
            removedNode.setProvidedInput(arg, removedData.get(arg));
        }

        if(parent != null) Handler.addEdge(graph, parent, removedNode, removedNode.creatorArgument());
        graphPanel().repaint();
        isUndone = false;
    }

    /**
     * Redoes the action
     */
    @Override
    public void redo() {
        Handler.removeNode(graph, removedNode);
        graphPanel().repaint();
        isUndone = true;
    }

    @Override
    public String toString()
    {
        return "User Action: " + actionType() + " " + removedNode.toString();
    }
}
