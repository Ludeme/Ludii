package app.display.dialogs.visual_editor.view.panels.editor;

import app.display.dialogs.visual_editor.LayoutManagement.GraphDrawing.GraphPanel;
import app.display.dialogs.visual_editor.LayoutManagement.LayoutManager.LayoutHandler;
import app.display.dialogs.visual_editor.view.panels.IGraphPanel;
import app.display.dialogs.visual_editor.view.panels.settings.LayoutSettingsPanel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class EditorPopupMenu extends JPopupMenu {

    public EditorPopupMenu(IGraphPanel graphPanel) {
        JMenuItem newLudeme = new JMenuItem("New Ludeme");
        JMenuItem duplicateScreen = new JMenuItem("Duplicate Screen");
        JMenuItem repaintScreen = new JMenuItem("Repaint");

        JMenu lmMenu = new JMenu("Graph Layout");
        JMenuItem compact = new JMenuItem("Arrange graph");
        JMenuItem settings = new JMenuItem("Layout Settings");

        newLudeme.addActionListener(e -> {
            graphPanel.showAllAvailableLudemes(getX(), getY());
        });

        duplicateScreen.addActionListener(e -> {
            JFrame frame = new JFrame("Define Editor");
            EditorPanel editorPanel = new EditorPanel(5000,5000);
            frame.setContentPane(editorPanel);
            editorPanel.drawGraph(graphPanel.getGraph());
            frame.setVisible(true);
            frame.setPreferredSize(frame.getPreferredSize());
            frame.setSize(1200,800);
        });

        compact.addActionListener(e -> {
            LayoutHandler lm = graphPanel.getLayoutHandler();
            lm.evaluateGraphWeights();
            lm.executeLayout();

            graphPanel.drawGraph(graphPanel.getGraph());
        });

        settings.addActionListener(e -> {
            LayoutSettingsPanel.getSettingsFrame(graphPanel);
        });

        lmMenu.add(compact);
        lmMenu.add(settings);

        repaintScreen.addActionListener(e -> {
            graphPanel.repaint();
                });

        add(newLudeme);
        add(lmMenu);
        add(duplicateScreen);
        add(repaintScreen);
    }

}
