package app.display.dialogs.visual_editor.model.UserActions;

import app.display.dialogs.visual_editor.model.DescriptionGraph;
import app.display.dialogs.visual_editor.model.LudemeNode;
import app.display.dialogs.visual_editor.model.NodeArgument;
import app.display.dialogs.visual_editor.view.panels.IGraphPanel;

public class RemovedCollectionAction implements IUserAction
{


    /**
     * @return The type of the action
     */
    @Override
    public ActionType actionType() {
        return null;
    }

    /**
     * @return The graph panel that was affected by the action
     */
    @Override
    public IGraphPanel graphPanel() {
        return null;
    }

    /**
     * @return The description graph that was affected by the action
     */
    @Override
    public DescriptionGraph graph() {
        return null;
    }

    /**
     * @return Whether the action was undone
     */
    @Override
    public boolean isUndone() {
        return false;
    }

    /**
     * Undoes the action
     */
    @Override
    public void undo() {

    }

    /**
     * Redoes the action
     */
    @Override
    public void redo() {

    }
}
