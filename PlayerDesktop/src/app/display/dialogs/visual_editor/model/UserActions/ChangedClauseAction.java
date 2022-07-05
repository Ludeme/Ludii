package app.display.dialogs.visual_editor.model.UserActions;

import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.model.DescriptionGraph;
import app.display.dialogs.visual_editor.model.LudemeNode;
import app.display.dialogs.visual_editor.model.NodeArgument;
import app.display.dialogs.visual_editor.view.panels.IGraphPanel;
import main.grammar.Clause;

import java.util.Arrays;
import java.util.LinkedHashMap;

/**
 * Created when the active clause of a node is changed.
 * @author Filipp Dokienko
 */


public class ChangedClauseAction implements IUserAction
{

    private final IGraphPanel graphPanel;
    private final DescriptionGraph graph;
    private final LudemeNode node;
    private final Clause previousClause;
    private final Clause currentClause;
    private final LinkedHashMap<NodeArgument, Object> removedData; // Inputs that were removed when the node was modified
    private boolean isUndone = false;

    /**
     * Constructor.
     * @param graphPanel
     * @param node
     * @param previousClause
     * @param currentClause
     */
    public ChangedClauseAction(IGraphPanel graphPanel, LudemeNode node, Clause previousClause, Clause currentClause)
    {
        this.graphPanel = graphPanel;
        this.graph = graphPanel.graph();
        this.node = node;
        this.previousClause = previousClause;
        this.currentClause = currentClause;

        // copy previous inputs
        removedData = new LinkedHashMap<>(node.providedInputsMap());
        for(NodeArgument arg : node.providedInputsMap().keySet())
        {
            if(removedData.get(arg) instanceof Object[])
            {
                Object[] copy = Arrays.copyOf((Object[])removedData.get(arg), ((Object[])removedData.get(arg)).length);
                removedData.put(arg, null);
                removedData.put(arg, copy);
            }
        }
    }

    /**
     * @return The type of the action
     */
    @Override
    public ActionType actionType() {
        return ActionType.CHANGED_CLAUSE;
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
        Handler.updateCurrentClause(graph, node, previousClause);
        // insert old inputs again
        for(NodeArgument arg : removedData.keySet())
        {
            Object input = removedData.get(arg);
            if(input == null) continue;
            if(input instanceof LudemeNode)
                Handler.addEdge(graph, node, (LudemeNode) input, arg);
            else if(input instanceof Object[])
            {
                //Handler.updateInput(graph, removedNode, arg, input);
                for(int i = 0; i < ((Object[]) input).length; i++)
                {
                    if(!(((Object[]) input)[i] instanceof LudemeNode)) continue;
                    Handler.addEdge(graph, node, (LudemeNode) ((Object[]) input)[i], arg, i);
                }
            }
            else Handler.updateInput(graph, node, arg, input);
            node.setProvidedInput(arg, removedData.get(arg));
        }
        graphPanel.notifyInputsUpdated(graphPanel.nodeComponent(node));
        isUndone = true;
    }

    /**
     * Redoes the action
     */
    @Override
    public void redo() {
        Handler.updateCurrentClause(graph, node, currentClause);
        isUndone = false;
    }
}
