package app.display.dialogs.visual_editor.view.panels.editor;

import app.display.dialogs.visual_editor.LayoutManagement.LayoutHandler;
import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.model.LudemeNode;
import app.display.dialogs.visual_editor.view.DesignPalette;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.LudemeNodeComponent;
import app.display.dialogs.visual_editor.view.panels.IGraphPanel;
import app.display.dialogs.visual_editor.view.panels.editor.tabPanels.LayoutSettingsPanel;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

public class EditorPopupMenu extends JPopupMenu {

    public EditorPopupMenu(IGraphPanel graphPanel, int x, int y) {
        JMenuItem newLudeme = new JMenuItem("New Ludeme");
        JMenuItem paste = new JMenuItem("Paste");



        paste.addActionListener(e -> {
            Handler.paste(graphPanel.graph(), x, y);
            // deselect all previously selected nodes
            graphPanel.deselectEverything();
        });

        JMenu lmMenu = new JMenu("Graph Layout");
        JMenuItem compact = new JMenuItem("Arrange graph");
        JMenuItem settings = new JMenuItem("Layout Settings");

        newLudeme.addActionListener(e -> {
            graphPanel.showAllAvailableLudemes(getX(), getY());
        });

        compact.addActionListener(e -> {
            LayoutHandler.applyOnPanel(graphPanel);
        });

        settings.addActionListener(e -> {
            LayoutSettingsPanel.getSettingsFrame(graphPanel);
        });

        lmMenu.add(compact);
        lmMenu.add(settings);

        JMenuItem collapse = new JMenuItem("Collapse");
        collapse.addActionListener(e -> {
            Handler.collapse(graphPanel.graph());
        });

        int iconHeight = (int)(newLudeme.getPreferredSize().getHeight()*0.75);

        ImageIcon newLudemeIcon = new ImageIcon(DesignPalette.ADD_ICON.getImage().getScaledInstance(iconHeight, iconHeight, Image.SCALE_SMOOTH));
        newLudeme.setIcon(newLudemeIcon);
        ImageIcon pasteIcon = new ImageIcon(DesignPalette.PASTE_ICON.getImage().getScaledInstance(iconHeight, iconHeight, Image.SCALE_SMOOTH));
        paste.setIcon(pasteIcon);
        ImageIcon collapseIcon = new ImageIcon(DesignPalette.COLLAPSE_ICON.getImage().getScaledInstance(iconHeight, iconHeight, Image.SCALE_SMOOTH));
        collapse.setIcon(collapseIcon);

        if(Handler.copyList().isEmpty()) paste.setEnabled(false);
        else paste.setEnabled(true);

        if(Handler.selectedNodes(graphPanel.graph()).isEmpty()) collapse.setEnabled(false);
        else collapse.setEnabled(true);

        add(newLudeme);
        add(paste);
        add(lmMenu);


        JMenuItem undo = new JMenuItem("Undo");
        undo.addActionListener(e -> {
            Handler.undo();
        });
        add(undo);

        JMenuItem redo = new JMenuItem("Redo");
        redo.addActionListener(e -> Handler.redo());
        add(redo);
    }

}
