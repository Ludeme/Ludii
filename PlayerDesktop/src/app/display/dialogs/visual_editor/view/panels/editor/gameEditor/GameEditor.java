package app.display.dialogs.visual_editor.view.panels.editor.gameEditor;

import app.display.dialogs.visual_editor.view.designPalettes.DesignPalette;
import app.display.dialogs.visual_editor.view.panels.IGraphPanel;

import javax.swing.*;
import java.awt.*;

/**
 * Panel for the game graph panel
 */

public class GameEditor extends JPanel
{
    private final JScrollPane SCROLL_PANE;
    private final GameGraphPanel GRAPH_PANEL;

    public GameEditor()
    {
        setLayout(new BorderLayout());
        GRAPH_PANEL = new GameGraphPanel(DesignPalette.DEFAULT_GRAPHPANEL_SIZE.width,  DesignPalette.DEFAULT_GRAPHPANEL_SIZE.height);
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

    public IGraphPanel graphPanel()
    {
        return GRAPH_PANEL;
    }

}
