package app.display.dialogs.visual_editor.view.panels.editor.defineEditor;

import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.model.LudemeNode;
import app.display.dialogs.visual_editor.view.panels.editor.GraphPanel;
import grammar.Grammar;
import main.grammar.Clause;

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

        // Create a "define" root node
        LudemeNode defineRoot = new LudemeNode(scrollPane.getViewport().getViewRect().x + (int)(scrollPane.getViewport().getViewRect().getWidth()/2),
                scrollPane.getViewport().getViewRect().y + (int)(scrollPane.getViewport().getViewRect().getHeight()/2), GraphPanel.symbolsWithoutConnection, true);

        Handler.recordUserActions = false;
        Handler.addNode(graph(), defineRoot);
        Handler.recordUserActions = true;

    }

    @Override
    public boolean isDefineGraph()
    {
        return true;
    }
}
