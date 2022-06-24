package app.display.dialogs.visual_editor.model.UserActions;

import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.model.DescriptionGraph;
import app.display.dialogs.visual_editor.model.LudemeNode;
import app.display.dialogs.visual_editor.view.panels.IGraphPanel;
import main.grammar.Clause;

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
