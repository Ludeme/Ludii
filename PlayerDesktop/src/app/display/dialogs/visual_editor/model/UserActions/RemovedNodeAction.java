package app.display.dialogs.visual_editor.model.UserActions;

import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.model.DescriptionGraph;
import app.display.dialogs.visual_editor.model.LudemeNode;
import app.display.dialogs.visual_editor.model.NodeArgument;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.LudemeNodeComponent;
import app.display.dialogs.visual_editor.view.panels.IGraphPanel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

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
    private final LudemeNode parent; // remembers the parent of the node
    private final LinkedHashMap<NodeArgument, Object> removedData; // Inputs that were removed when the node was removed
    private int collectionIndex = -1; // If the node was removed from a collection, this is the index of the node in the collection


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
        for(NodeArgument arg : removedNode.providedInputsMap().keySet())
        {
            if(removedData.get(arg) instanceof Object[])
            {
                Object[] copy = Arrays.copyOf((Object[])removedData.get(arg), ((Object[])removedData.get(arg)).length);
                removedData.put(arg, null);
                removedData.put(arg, copy);
            }
        }

        // find collection index
        if(parent==null) return;
        LinkedHashMap<NodeArgument, Object> parentInputs = parent.providedInputsMap();
        if(parentInputs.containsValue(removedNode)) collectionIndex = -1;
        else
        {
            for(NodeArgument arg : parentInputs.keySet())
            {
                if(parentInputs.get(arg) instanceof Object[])
                {
                    Object[] collection = (Object[])parentInputs.get(arg);
                    for(int i = 0; i < collection.length; i++)
                    {
                        if(collection[i] == removedNode)
                        {
                            collectionIndex = i;
                            break;
                        }
                    }
                }
            }
        }

    }

    public void setCollectionIndex(int index)
    {
        System.out.println("RemovedNodeAction.setCollectionIndex: " + index);
        collectionIndex = index;
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
     * Undoes the action
     */
    @Override
    public void undo()
    {
        Handler.addNode(graph, removedNode);
        for (NodeArgument arg : removedData.keySet()) {
            Object input = removedData.get(arg);
            if (input == null) continue;
            if (input instanceof LudemeNode) Handler.addEdge(graph, removedNode, (LudemeNode) input, arg);
            else if (input instanceof Object[]) {
                Object[] collection = (Object[]) input;
                Handler.updateInput(graph, removedNode, arg, input);
                for (int i = 0; i < collection.length; i++) {
                    if (!(collection[i] instanceof LudemeNode)) continue;
                    Handler.addEdge(graph, removedNode, (LudemeNode) collection[i], arg, i);
                }
            } else Handler.updateInput(graph, removedNode, arg, input);
            removedNode.setProvidedInput(arg, removedData.get(arg));
        }

        if(parent != null)
        {
            if(collectionIndex == -1)
                Handler.addEdge(graph, parent, removedNode, removedNode.creatorArgument());
            else
                Handler.addEdge(graph, parent, removedNode, removedNode.creatorArgument(), collectionIndex);
        }
        graphPanel().repaint();

        List<LudemeNodeComponent> lncs = new ArrayList<>();
        lncs.add(graphPanel.nodeComponent(removedNode));
        graphPanel.updateCollapsed(lncs);

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
