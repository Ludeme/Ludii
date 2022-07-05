package app.display.dialogs.visual_editor.model.UserActions;

import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.model.DescriptionGraph;
import app.display.dialogs.visual_editor.model.LudemeNode;
import app.display.dialogs.visual_editor.model.NodeArgument;
import app.display.dialogs.visual_editor.view.panels.IGraphPanel;

/**
 * Created when a connection between two nodes is removed.
 * @author Filipp Dokienko
 */

public class RemovedConnectionAction implements IUserAction
{

    private final IGraphPanel graphPanel;
    private final DescriptionGraph graph;
    private final LudemeNode from;
    private final LudemeNode to;
    private final NodeArgument nodeArgument;
    //private final int index;
    private boolean isUndone = false;

    public RemovedConnectionAction(IGraphPanel graphPanel, LudemeNode from, LudemeNode to, NodeArgument nodeArgument)
    {
        this.graphPanel = graphPanel;
        this.graph = graphPanel.graph();
        this.from = from;
        this.to = to;
        this.nodeArgument = nodeArgument;
        //this.index = index;
    }

    /**
     * @return The type of the action
     */
    @Override
    public ActionType actionType() {
        return ActionType.REMOVED_INPUT;
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
     * Undoes the action
     */
    @Override
    public void undo() {
        Handler.addEdge(graph, from, to, nodeArgument);
        Handler.updateInput(graph, from, nodeArgument, to);
        isUndone = true;
    }

    /**
     * Redoes the action
     */
    @Override
    public void redo() {
        Handler.removeEdge(graph, from, to);
        Handler.updateInput(graph, from, nodeArgument, null);
        isUndone = true;
    }
}
