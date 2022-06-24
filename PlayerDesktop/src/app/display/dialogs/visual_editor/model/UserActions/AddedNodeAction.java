package app.display.dialogs.visual_editor.model.UserActions;

import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.model.DescriptionGraph;
import app.display.dialogs.visual_editor.model.LudemeNode;
import app.display.dialogs.visual_editor.view.panels.IGraphPanel;

/**
 * Created when a node is added to the graph.
 * @author Filipp Dokienko
 */

public class AddedNodeAction implements IUserAction
{

    private final IGraphPanel graphPanel;
    private final DescriptionGraph graph;
    private final LudemeNode addedNode;
    private boolean isUndone = false;
    private Object[] removedData; // Inputs that were removed when the node was removed


    /**
     * Constructor.
     * @param graphPanel The graph panel that was affected by the action.
     * @param addedNode The node that was added.
     */
    public AddedNodeAction(IGraphPanel graphPanel, LudemeNode addedNode)
    {
        this.graphPanel = graphPanel;
        this.graph = graphPanel.graph();
        this.addedNode = addedNode;
    }

    /**
     * @return The type of the action
     */
    @Override
    public ActionType actionType()
    {
        return ActionType.ADDED_NODE;
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
        removedData = addedNode.providedInputs();
        Handler.removeNode(graph, addedNode);
        graphPanel().repaint();
        isUndone = true;
    }

    /**
     * Redoes the action
     */
    @Override
    public void redo() {
        Handler.addNode(graph, addedNode);
        // ensure that all of its connections are added to the graph
        for (int i = 0; i < removedData.length; i++)
        {
            Object input = removedData[i];
            Handler.updateInput(graph, addedNode, i, input);
        }
        graphPanel().repaint();
        isUndone = false;
    }

    @Override
    public String toString()
    {
        return "User Action: " + actionType() + " " + addedNode.toString();
    }
}
