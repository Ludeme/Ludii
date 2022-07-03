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
        centerScrollPane(); // center scroll pane position
        add(SCROLL_PANE, BorderLayout.CENTER);
        GRAPH_PANEL.initialize(SCROLL_PANE);
        setVisible(true);
    }

    private void centerScrollPane()
    {
        Rectangle rect = SCROLL_PANE.getViewport().getViewRect();
        int centerX = (SCROLL_PANE.getViewport().getViewSize().width - rect.width) / 2;
        int centerY = (SCROLL_PANE.getViewport().getViewSize().height - rect.height) / 2;

        SCROLL_PANE.getViewport().setViewPosition(new Point(centerX, centerY));
    }

    public IGraphPanel currentGraphPanel()
    {
        return GRAPH_PANEL;
    }


}
