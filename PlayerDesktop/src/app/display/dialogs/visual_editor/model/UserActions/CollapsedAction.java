package app.display.dialogs.visual_editor.model.UserActions;

import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.model.DescriptionGraph;
import app.display.dialogs.visual_editor.model.LudemeNode;
import app.display.dialogs.visual_editor.view.panels.IGraphPanel;

/**
 * Created when a node is collapsed
 * @author Filipp Dokienko
 */

public class CollapsedAction implements IUserAction
{
    private final IGraphPanel graphPanel;
    private final DescriptionGraph graph;
    private final LudemeNode collapsedNode;
    private final boolean collapsed;
    private boolean isUndone = false;

    /**
     * Constructor.
     * @param graphPanel The graph panel that was affected by the action.
     * @param collapsedNode The node that was collapsed.
     */
    public CollapsedAction(IGraphPanel graphPanel, LudemeNode collapsedNode, boolean collapsed)
    {
        this.graphPanel = graphPanel;
        this.graph = graphPanel.graph();
        this.collapsedNode = collapsedNode;
        this.collapsed = collapsed;
    }

    /**
     * @return The type of the action
     */
    @Override
    public ActionType actionType() {
        return ActionType.COLLAPSED;
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
        Handler.collapseNode(graph, collapsedNode, !collapsed);
        isUndone = false;
    }

    /**
     * Redoes the action
     */
    @Override
    public void redo() {
        Handler.collapseNode(graph, collapsedNode, collapsed);
        isUndone = true;
    }
}
