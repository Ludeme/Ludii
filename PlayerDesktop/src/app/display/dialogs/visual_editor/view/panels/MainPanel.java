package app.display.dialogs.visual_editor.view.panels;

import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.view.panels.editor.gameEditor.GameGraphPanel;
import app.display.dialogs.visual_editor.view.panels.header.HeaderPanel;
import app.display.dialogs.visual_editor.view.panels.editor.EditorSidebar;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MainPanel extends JPanel {

    //JPanel editor_panel = new EditorPanel(5000, 5000);
    private final JScrollPane panel;

    public MainPanel(GameGraphPanel editor_panel)
    {
        setLayout(new BorderLayout());

        add(new HeaderPanel(null), BorderLayout.NORTH);
        panel = new JScrollPane(editor_panel);

        Rectangle rect = panel.getViewport().getViewRect();
        int centerX = (panel.getViewport().getViewSize().width - rect.width) / 2;
        int centerY = (panel.getViewport().getViewSize().height - rect.height) / 2;

        panel.getViewport().setViewPosition(new Point(centerX, centerY));

        panel.getVerticalScrollBar().setOpaque(true);
        panel.getHorizontalScrollBar().setOpaque(true);
        panel.getHorizontalScrollBar().setBackground(Handler.currentPalette().BACKGROUND_EDITOR());
        panel.getHorizontalScrollBar().setBackground(Handler.currentPalette().BACKGROUND_EDITOR());

        JPanel splitPanel = new JPanel();
        splitPanel.setLayout(new BorderLayout());
        splitPanel.add(panel, BorderLayout.CENTER);
        splitPanel.add(EditorSidebar.getEditorSidebar(), BorderLayout.EAST);
        add(splitPanel, BorderLayout.CENTER);

        editor_panel.initialize(panel);

        setFocusable(true);
    }

    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        if(panel.getVerticalScrollBar().getBackground() != Handler.currentPalette().BACKGROUND_EDITOR())
        {
            panel.getVerticalScrollBar().setBackground(Handler.currentPalette().BACKGROUND_EDITOR());
            panel.getHorizontalScrollBar().setBackground(Handler.currentPalette().BACKGROUND_EDITOR());
        }
    }

    public Dimension getViewPort()
    {
        return panel.getViewport().getSize();
    }

}
