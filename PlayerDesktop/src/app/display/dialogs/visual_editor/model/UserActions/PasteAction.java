package app.display.dialogs.visual_editor.model.UserActions;

import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.model.DescriptionGraph;
import app.display.dialogs.visual_editor.model.LudemeNode;
import app.display.dialogs.visual_editor.view.panels.IGraphPanel;

import java.util.List;

/**
 * Created when the user pastes node(s).
 */

public class PasteAction implements IUserAction
{

    private final IGraphPanel graphPanel;
    private final DescriptionGraph graph;
    private final List<LudemeNode> pastedNodes;
    private boolean isUndone = false;
    private  RemovedNodesAction removedNodesAction;

    public PasteAction(IGraphPanel graphPanel, List<LudemeNode> pastedNodes)
    {
        this.graphPanel = graphPanel;
        this.graph = graphPanel.graph();
        this.pastedNodes = pastedNodes;
    }

    /**
     * @return The type of the action
     */
    @Override
    public ActionType actionType() {
        return ActionType.PASTED;
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
        removedNodesAction = new RemovedNodesAction(graphPanel, pastedNodes);
        Handler.removeNodes(graph, pastedNodes);
        isUndone = false;
    }

    /**
     * Redoes the action
     */
    @Override
    public void redo() {
        removedNodesAction.undo();
        isUndone = true;
    }
}
