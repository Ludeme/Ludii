package app.display.dialogs.visual_editor.view;

import javax.swing.*;
import java.awt.event.ActionListener;

public class EditorMenuBar extends JMenuBar {

    public EditorMenuBar() {
        JMenu file = new JMenu("File"); // operations with file being edited
        // adding file menu items
        addJMenuItem(file, "New", null);
        addJMenuItem(file, "Open...", null);
        addJMenuItem(file, "Open recent", null);
        addJMenuItem(file, "Close file", null);
        addJMenuItem(file, "Save", null);
        addJMenuItem(file, "Save as...", null);
        addJMenuItem(file, "Compile", null);
        addJMenuItem(file, "Exit", null);

        JMenu settings = new JMenu("Settings"); // adjust editor settings e.g. font size, colors ect.
        // adding settings menu items
        addJMenuItem(settings, "Open settings...", null);

        JMenu about = new JMenu("About"); // read about the editor: documentation, research report, DLP
        // adding about menu items
        addJMenuItem(about, "Open documentation", null);
        addJMenuItem(about, "Learn more about the editor", null); // opens research paper
        addJMenuItem(about, "Learn more about DLP", null);

        add(file);
        add(settings);
        add(about);
    }

    public static void addJMenuItem(JMenu menu, String itemName, ActionListener actionListener)
    {
        JMenuItem jMenuItem = new JMenuItem(itemName);
        jMenuItem.addActionListener(actionListener);
        menu.add(jMenuItem);
    }
}

