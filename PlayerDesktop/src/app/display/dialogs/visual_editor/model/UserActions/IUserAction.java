package app.display.dialogs.visual_editor.model.UserActions;

import app.display.dialogs.visual_editor.model.DescriptionGraph;
import app.display.dialogs.visual_editor.view.panels.IGraphPanel;

/**
 * Describes an undoable action that was performed by the user
 * @author Filipp Dokienko
 */

public interface IUserAction
{
    /**
     * Describes the action that was performed
     */
    enum ActionType
    {
        ADDED_NODE, // User added a new Ludeme
        REMOVED_NODE, // User removed a Ludeme
        CHANGED_CLAUSE, // User changed a clause
        COLLAPSED, // User collapsed a node
        ADDED_INPUT, // User added a new input to a node
        REMOVED_INPUT, // User removed an input from a node
        ADDED_COLLECTION, // User added a new collection to a node
        REMOVED_COLLECTION, // User removed a collection from a node
        ACTIVATE_OPTIONAL_TERMINAL, // User activated an optional terminal
        PASTED // User pasted one or more nodes
    }

    /**
     *
     * @return The type of the action
     */
    ActionType actionType();

    /**
     *
     * @return The graph panel that was affected by the action
     */
    IGraphPanel graphPanel();

    /**
     *
     * @return The description graph that was affected by the action
     */
    DescriptionGraph graph();

    /**
     * Undoes the action
     */
    void undo();

    /**
     * Redoes the action
     */
    void redo();

}
