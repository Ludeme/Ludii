package app.display.dialogs.visual_editor.view.panels.editor;

import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.inputs.LConnectionComponent;

public interface IEditorPanel {
    public void startNewConnection(LConnectionComponent source);
    public void cancelNewConnection();
    public void finishNewConnection();
}
