package app.display.dialogs.visual_editor.view.panels.editor.defineEditor;

import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.view.panels.editor.GraphPanel;
import javax.swing.*;

public class DefineGraphPanel extends GraphPanel
{

    private String name;

    public DefineGraphPanel(String name, int width, int height)
    {
        super(width, height);
        this.name = name;
        Handler.addGraphPanel(graph(), this);
    }

    public void initialize(JScrollPane scrollPane)
    {
        super.initialize(scrollPane);
    }
}
