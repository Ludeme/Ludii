package app.display.dialogs.visual_editor.view.panels.editor.defineEditor;

import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.model.DescriptionGraph;
import app.display.dialogs.visual_editor.model.LudemeNode;
import app.display.dialogs.visual_editor.view.panels.editor.GraphPanel;

import javax.swing.*;

public class DefineGraphPanel extends GraphPanel
{

    private final DescriptionGraph GRAPH;

    public DefineGraphPanel(String name, int width, int height)
    {
        super(width, height);
        this.GRAPH = new DescriptionGraph(name, true);
        Handler.addGraphPanel(graph(), this);
    }

    @Override
    public DescriptionGraph graph()
    {
        return GRAPH;
    }

    public void initialize(JScrollPane scrollPane)
    {
        super.initialize(scrollPane);

        // Create a "define" root node
        LudemeNode defineRoot = new LudemeNode(scrollPane.getViewport().getViewRect().x + (int)(scrollPane.getViewport().getViewRect().getWidth()/2),
                scrollPane.getViewport().getViewRect().y + (int)(scrollPane.getViewport().getViewRect().getHeight()/2), GraphPanel.symbolsWithoutConnection, true);
        defineRoot.setProvidedInput(defineRoot.currentNodeArguments().get(0),graph().title());

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
