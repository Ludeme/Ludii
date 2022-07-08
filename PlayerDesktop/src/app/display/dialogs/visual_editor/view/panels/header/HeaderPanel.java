package app.display.dialogs.visual_editor.view.panels.header;

import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.view.VisualEditorPanel;
import app.display.dialogs.visual_editor.view.designPalettes.DesignPalette;

import javax.swing.*;
import java.awt.*;

public class HeaderPanel extends JPanel
{

    private final EditorPickerPanel editorPickerPanel;
    private final ToolsPanel toolsPanel;

    public HeaderPanel(VisualEditorPanel visualEditorPanel)
    {
        setLayout(new BorderLayout());
        editorPickerPanel = new EditorPickerPanel(visualEditorPanel);
        add(editorPickerPanel, BorderLayout.LINE_START);
        toolsPanel = new ToolsPanel();
        Handler.toolsPanel = toolsPanel;
        add(toolsPanel, BorderLayout.LINE_END);
        setOpaque(true);
        setBackground(DesignPalette.BACKGROUND_HEADER_PANEL());

        int preferredHeight = getPreferredSize().height;
        setPreferredSize(new Dimension(getPreferredSize().width, preferredHeight+20));
    }

    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        if(getBackground() != DesignPalette.BACKGROUND_HEADER_PANEL())
        {
            setBackground(DesignPalette.BACKGROUND_HEADER_PANEL());
            editorPickerPanel.repaint();
            toolsPanel.repaint();
        }
    }

}
