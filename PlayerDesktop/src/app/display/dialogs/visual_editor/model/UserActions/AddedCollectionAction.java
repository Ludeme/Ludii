package app.display.dialogs.visual_editor.model.UserActions;

import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.model.DescriptionGraph;
import app.display.dialogs.visual_editor.model.LudemeNode;
import app.display.dialogs.visual_editor.model.NodeArgument;
import app.display.dialogs.visual_editor.view.panels.IGraphPanel;

/**
 * Created when a collection is added to the graph.
 * @author Filipp Dokienko
 */

public class AddedCollectionAction implements IUserAction
{

    private final IGraphPanel graphPanel;
    private final DescriptionGraph graph;
    private final LudemeNode affectedNode;
    private final NodeArgument nodeArgument;
    private final int elementIndex;
    private Object collectionInput;
    private boolean isUndone = false;

    /**
     * Constructor.
     * @param graphPanel
     * @param affectedNode
     * @param nodeArgument The argument that is provided by the collection.
     * @param elementIndex The index of the added element to the collection
     * @param parentIndex (InputField-)Index of the collection root
     * @param input The input that was added to the collection (null for non-terminal)
     */
    public AddedCollectionAction(IGraphPanel graphPanel, LudemeNode affectedNode, NodeArgument nodeArgument, int parentIndex, int elementIndex, Object input)
    {
        this.graphPanel = graphPanel;
        this.graph = graphPanel.graph();
        this.affectedNode = affectedNode;
        this.nodeArgument = nodeArgument;
        this.elementIndex = elementIndex;
        this.collectionInput = input;
    }

    public boolean isUpdated(LudemeNode node, NodeArgument nodeArgument, int elementIndex)
    {
        return node==affectedNode && nodeArgument==this.nodeArgument && elementIndex==this.elementIndex;
    }

    public void setInput(Object input)
    {
        this.collectionInput = input;
    }

    /**
     * @return The type of the action
     */
    @Override
    public ActionType actionType() {
        return ActionType.ADDED_COLLECTION;
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
        Handler.removeCollectionElement(graph, affectedNode,nodeArgument, elementIndex);
        isUndone = false;
    }

    /**
     * Redoes the action
     */
    @Override
    public void redo() {
        Handler.addCollectionElement(graph, affectedNode, nodeArgument);
        if(collectionInput != null)
        {
            System.out.println("INPUT:: " + collectionInput + ", " + elementIndex);
            Handler.updateCollectionInput(graph, affectedNode, nodeArgument, collectionInput, elementIndex);
        }
        isUndone = true;
    }
}
