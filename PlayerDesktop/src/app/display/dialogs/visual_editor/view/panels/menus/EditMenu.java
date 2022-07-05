package app.display.dialogs.visual_editor.view.panels.menus;

import app.display.dialogs.visual_editor.handler.Handler;

import javax.swing.*;
import java.awt.event.ActionListener;

public class EditMenu extends JMenu
{
    public EditMenu(EditorMenuBar menuBar)
    {
        super("Edit");
        menuBar.addJMenuItem(this, "Undo", undo, KeyStroke.getKeyStroke("control Z"));
        menuBar.addJMenuItem(this, "Redo", redo, KeyStroke.getKeyStroke("control Y"));
        add(new JSeparator());
        menuBar.addJMenuItem(this, "Copy", copy, KeyStroke.getKeyStroke("control C"));
        menuBar.addJMenuItem(this, "Paste", paste, KeyStroke.getKeyStroke("control V"));
        menuBar.addJMenuItem(this, "Duplicate", duplicate, KeyStroke.getKeyStroke("control shift D"));
        menuBar.addJMenuItem(this, "Delete", delete, KeyStroke.getKeyStroke("control D"));
        add(new JSeparator());
        menuBar.addJMenuItem(this, "Select All", selectAll, KeyStroke.getKeyStroke("control A"));
        menuBar.addJMenuItem(this, "Unselect All", unselectAll);
        menuBar.addJMenuItem(this, "Collapse", collapse, KeyStroke.getKeyStroke("control W"));
        menuBar.addJMenuItem(this, "Expand", expand, KeyStroke.getKeyStroke("control E"));
        menuBar.addJMenuItem(this, "Expand All", expandAll);
    }

    ActionListener undo = e -> Handler.undo();

    ActionListener redo = e -> Handler.redo();

    ActionListener copy = e -> Handler.copy();

    ActionListener paste = e -> Handler.paste(-1, -1);

    ActionListener duplicate = e -> Handler.duplicate();

    ActionListener delete = e -> Handler.remove();

    ActionListener selectAll = e -> Handler.selectAll();

    ActionListener unselectAll = e -> Handler.unselectAll();

    ActionListener collapse = e -> Handler.collapse();

    ActionListener expand = e -> Handler.expand();

    ActionListener expandAll = e -> {
        Handler.selectAll();
        Handler.expand();
    };
}
