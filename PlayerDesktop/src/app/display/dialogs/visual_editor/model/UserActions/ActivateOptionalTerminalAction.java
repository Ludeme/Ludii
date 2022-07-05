package app.display.dialogs.visual_editor.model.UserActions;

import app.display.dialogs.visual_editor.model.DescriptionGraph;
import app.display.dialogs.visual_editor.model.LudemeNode;
import app.display.dialogs.visual_editor.model.NodeArgument;
import app.display.dialogs.visual_editor.view.panels.IGraphPanel;

/**
 * Created when an optional terminal inputfield gets activated/deactivated
 */

public class ActivateOptionalTerminalAction implements IUserAction
{
    private final IGraphPanel graphPanel;
    private final DescriptionGraph graph;
    private final LudemeNode node;
    private final NodeArgument argument;
    private final boolean wasActivated;

    public ActivateOptionalTerminalAction(IGraphPanel graphPanel, LudemeNode node, NodeArgument argument, boolean wasActivated)
    {
        this.graphPanel = graphPanel;
        this.graph = graphPanel.graph();
        this.node = node;
        this.argument = argument;
        this.wasActivated = wasActivated;
    }

    /**
     * @return The type of the action
     */
    @Override
    public ActionType actionType() {
        return ActionType.ACTIVATE_OPTIONAL_TERMINAL;
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
        graphPanel.notifyTerminalActivated(graphPanel.nodeComponent(node), argument, !wasActivated);
    }

    /**
     * Redoes the action
     */
    @Override
    public void redo() {
        graphPanel.notifyTerminalActivated(graphPanel.nodeComponent(node), argument, wasActivated);
    }
}
