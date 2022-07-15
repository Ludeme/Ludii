package app.display.dialogs.visual_editor.model.UserActions;

import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.model.DescriptionGraph;
import app.display.dialogs.visual_editor.model.LudemeNode;
import app.display.dialogs.visual_editor.model.NodeArgument;
import app.display.dialogs.visual_editor.view.panels.IGraphPanel;

public class RemovedCollectionAction implements IUserAction
{

    private final IGraphPanel graphPanel;
    private final DescriptionGraph graph;
    private final LudemeNode affectedNode;
    private final NodeArgument nodeArgument;
    private final int elementIndex;
    private Object collectionInput;
    private boolean isUndone = false;

    public RemovedCollectionAction(IGraphPanel graphPanel, LudemeNode affectedNode, NodeArgument nodeArgument, int elementIndex, Object input)
    {
        this.graphPanel = graphPanel;
        this.graph = graphPanel.graph();
        this.affectedNode = affectedNode;
        this.nodeArgument = nodeArgument;
        this.elementIndex = elementIndex;
        this.collectionInput = input;
    }

    /**
     * @return The type of the action
     */
    @Override
    public ActionType actionType() {
        return ActionType.REMOVED_COLLECTION;
    }

    /**
     * @return The graph panel that was affected by the action
     */
    @Override
    public IGraphPanel graphPanel() {
        return graphPanel;
    }

    /**
     * @return The description graph that was affected by the action
     */
    @Override
    public DescriptionGraph graph() {
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
    public void undo() {
        Handler.addCollectionElement(graph, affectedNode, nodeArgument);
        if(collectionInput != null)
        {
            System.out.println("INPUT:: " + collectionInput + ", " + elementIndex);
            if(collectionInput instanceof LudemeNode) Handler.addEdge(graph, affectedNode, (LudemeNode) collectionInput, nodeArgument, elementIndex);
            Handler.updateCollectionInput(graph, affectedNode, nodeArgument, collectionInput, elementIndex);
        }
        isUndone = false;
    }

    /**
     * Redoes the action
     */
    @Override
    public void redo() {
        Handler.removeCollectionElement(graph, affectedNode,nodeArgument, elementIndex);
        if(collectionInput != null)
        {
            System.out.println("INPUT:: " + collectionInput + ", " + elementIndex);
            if(collectionInput instanceof LudemeNode) Handler.removeEdge(graph, affectedNode, (LudemeNode) collectionInput, elementIndex);}
        isUndone = true;
    }
}
