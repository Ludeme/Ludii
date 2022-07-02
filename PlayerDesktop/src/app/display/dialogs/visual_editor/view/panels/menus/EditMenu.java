package app.display.dialogs.visual_editor.view.panels.menus;

import app.display.dialogs.visual_editor.handler.Handler;

import javax.swing.*;
import java.awt.event.ActionListener;

public class EditMenu extends JMenu
{
    public EditMenu(EditorMenuBar menuBar)
    {
        super("Edit");
        menuBar.addJMenuItem(this, "Undo", undo);
        menuBar.addJMenuItem(this, "Redo", redo);
        add(new JSeparator());
        menuBar.addJMenuItem(this, "Copy", copy);
        menuBar.addJMenuItem(this, "Paste", paste);
        menuBar.addJMenuItem(this, "Duplicate", duplicate);
        menuBar.addJMenuItem(this, "Delete", delete);
        add(new JSeparator());
        menuBar.addJMenuItem(this, "Select All", selectAll);
        menuBar.addJMenuItem(this, "Unselect All", unselectAll);
        menuBar.addJMenuItem(this, "Collapse", collapse);
        menuBar.addJMenuItem(this, "Expand", expand);
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
