package app.display.dialogs.visual_editor.view;

import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.view.panels.editor.EditorSidebar;
import app.display.dialogs.visual_editor.view.panels.editor.defineEditor.DefineEditor;
import app.display.dialogs.visual_editor.view.panels.editor.gameEditor.GameEditor;
import app.display.dialogs.visual_editor.view.panels.editor.textEditor.TextEditor;
import app.display.dialogs.visual_editor.view.panels.header.HeaderPanel;

import javax.swing.*;
import java.awt.*;

public class VisualEditorPanel extends JPanel
{

    // Game Editor
    private final GameEditor gameEditor = new GameEditor();
    // Define Editor
    private final DefineEditor defineEditor = new DefineEditor();
    // Text "Editor"
    private final TextEditor textEditor = new TextEditor();
    // Whether the game, define, text, ... editor is currently active/selected
    private JPanel ACTIVE_EDITOR = gameEditor;

    public VisualEditorPanel()
    {
        setLayout(new BorderLayout());

        add(new HeaderPanel(this), BorderLayout.NORTH);

        add(ACTIVE_EDITOR, BorderLayout.CENTER);
        // Layout Sidebar
        EditorSidebar layoutSidebar = EditorSidebar.getEditorSidebar();
        add(layoutSidebar, BorderLayout.EAST);

        setFocusable(true);
    }


    public void openGameEditor()
    {
        remove(ACTIVE_EDITOR);
        ACTIVE_EDITOR = gameEditor;
        add(ACTIVE_EDITOR, BorderLayout.CENTER);
        repaint();
        revalidate();
        Handler.updateCurrentGraphPanel(gameEditor.graphPanel());
    }

    public void openDefineEditor()
    {
        remove(ACTIVE_EDITOR);
        ACTIVE_EDITOR = defineEditor;
        add(ACTIVE_EDITOR, BorderLayout.CENTER);
        repaint();
        revalidate();
        Handler.updateCurrentGraphPanel(defineEditor.currentGraphPanel());
    }

    public void openTextEditor()
    {
        remove(ACTIVE_EDITOR);
        ACTIVE_EDITOR = textEditor;
        textEditor.updateLud();
        add(ACTIVE_EDITOR, BorderLayout.CENTER);
        repaint();
        revalidate();
    }

}
