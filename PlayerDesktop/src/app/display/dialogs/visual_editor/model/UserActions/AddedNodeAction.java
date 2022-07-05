package app.display.dialogs.visual_editor.model.UserActions;

import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.model.DescriptionGraph;
import app.display.dialogs.visual_editor.model.LudemeNode;
import app.display.dialogs.visual_editor.model.NodeArgument;
import app.display.dialogs.visual_editor.view.panels.IGraphPanel;

import java.util.Arrays;
import java.util.LinkedHashMap;

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

    private LudemeNode parent; // remembers the parent of the node
    private LinkedHashMap<NodeArgument, Object> removedData; // Inputs that were removed when the node was removed

    private int collectionIndex = -1; // If the node was removed from a collection, this is the index of the node in the collection

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

    public LudemeNode addedNode()
    {
        return addedNode;
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
     * Undoes the action
     */
    @Override
    public void undo()
    {
        parent = addedNode.parentNode();
        removedData = new LinkedHashMap<>(addedNode.providedInputsMap());
        for(NodeArgument arg : addedNode.providedInputsMap().keySet())
        {
            if(removedData.get(arg) instanceof Object[])
            {
                Object[] copy = Arrays.copyOf((Object[])removedData.get(arg), ((Object[])removedData.get(arg)).length);
                removedData.put(arg, null);
                removedData.put(arg, copy);
            }
        }
        // find the index of the removed node in its parent
        Handler.removeNode(graph, addedNode);
        graphPanel().repaint();
        isUndone = true;
    }

    public void setCollectionIndex(int index)
    {
        System.out.println("AddedNodeAction.setCollectionIndex: " + index);
        collectionIndex = index;
    }



    /**
     * Redoes the action
     */
    @Override
    public void redo() {

        Handler.addNode(graph, addedNode);
        for (NodeArgument arg : removedData.keySet()) {
            Object input = removedData.get(arg);
            if (input == null) continue;
            if (input instanceof LudemeNode) Handler.addEdge(graph, addedNode, (LudemeNode) input, arg);
            else if (input instanceof Object[]) {
                Object[] collection = (Object[]) input;
                Handler.updateInput(graph, addedNode, arg, input);
                for (int i = 0; i < collection.length; i++) {
                    if (!(collection[i] instanceof LudemeNode)) continue;
                    Handler.addEdge(graph, addedNode, (LudemeNode) collection[i], arg, i);
                }
            } else Handler.updateInput(graph, addedNode, arg, input);
            addedNode.setProvidedInput(arg, removedData.get(arg));
        }

        if (parent != null) {
            if (collectionIndex == -1)
                Handler.addEdge(graph, parent, addedNode, addedNode.creatorArgument());
            else
                Handler.addEdge(graph, parent, addedNode, addedNode.creatorArgument(), collectionIndex);
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
