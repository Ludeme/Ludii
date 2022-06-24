package app.display.dialogs.visual_editor.model.UserActions;

import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.model.DescriptionGraph;
import app.display.dialogs.visual_editor.model.LudemeNode;
import app.display.dialogs.visual_editor.view.panels.IGraphPanel;

public class RemovedNodeAction implements IUserAction
{
    private final IGraphPanel graphPanel;
    private final DescriptionGraph graph;
    private final LudemeNode removedNode;
    private boolean isUndone = false;
    private LudemeNode parent; // remembers the parent of the node
    private int parentInputFieldIndex = -1; // remembers the input index of the removed node in its parent
    private Object[] removedData; // Inputs that were removed when the node was removed


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
        removedData = removedNode.providedInputs();
        if(removedNode.parentNode() != null) {
            Object[] parentProvidedInputs = parent.providedInputs();
            for (int i = 0; i < parentProvidedInputs.length; i++) {
                if (parentProvidedInputs[i] == removedNode) {
                    parentInputFieldIndex = i;
                    break;
                }
            }
            // then its a collection
            if(parentInputFieldIndex == -1)
            {
                for(int i = 0; i < parentProvidedInputs.length; i++)
                {
                    if(!(parentProvidedInputs[i] instanceof Object[])) continue;
                    for(int j = 0; j < ((Object[])parentProvidedInputs[i]).length; j++)
                    {
                        if(((Object[])parentProvidedInputs[i])[j] == removedNode)
                        {
                            parentInputFieldIndex = i + j;
                            break;
                        }
                    }
                    if(parentInputFieldIndex != -1) break;
                }
            }
        }
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
        Handler.recordUserActions = false;
        Handler.addNode(graph, removedNode);
        // ensure that all of its connections are added to the graph
        for (int i = 0; i < removedData.length; i++)
        {
            Object input = removedData[i];
            if(input == null) continue;
            Handler.updateInput(graph, removedNode, i, input);
        }
        if(parent != null) Handler.addEdge(graph, parent, removedNode, parentInputFieldIndex);
        graphPanel().repaint();
        isUndone = false;
        Handler.recordUserActions = true;
    }

    /**
     * Redoes the action
     */
    @Override
    public void redo() {
        Handler.recordUserActions = false;
        Handler.removeNode(graph, removedNode);
        graphPanel().repaint();
        isUndone = true;
        Handler.recordUserActions = true;
    }

    @Override
    public String toString()
    {
        return "User Action: " + actionType() + " " + removedNode.toString();
    }
}
