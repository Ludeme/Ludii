package app.display.dialogs.visual_editor.view.panels.editor.defineEditor;

import app.display.dialogs.visual_editor.view.panels.IGraphPanel;

import javax.swing.*;
import java.awt.*;

public class DefineEditor extends JPanel
{
    private final JScrollPane SCROLL_PANE;
    private final DefineGraphPanel GRAPH_PANEL;

    public DefineEditor()
    {
        setLayout(new BorderLayout());
        GRAPH_PANEL = new DefineGraphPanel("Test", 10000, 10000);
        SCROLL_PANE = new JScrollPane(GRAPH_PANEL);
        add(SCROLL_PANE, BorderLayout.CENTER);
        GRAPH_PANEL.initialize(SCROLL_PANE);
        setVisible(true);
    }

    public IGraphPanel currentGraphPanel()
    {
        return GRAPH_PANEL;
    }


}
