package app.display.dialogs.visual_editor.view.panels.menus.viewMenu;

import app.display.dialogs.visual_editor.view.panels.menus.EditorMenuBar;

import javax.swing.*;

public class ViewMenu extends JMenu
{
    public ViewMenu(EditorMenuBar menuBar)
    {
        super("View");

        JMenu appearance = new JMenu("Colour Scheme");
        JMenu background = new JMenu("Background");

        add(appearance);
        add(background);

        menuBar.addJMenuItem(appearance, "Light", null);
        menuBar.addJMenuItem(appearance, "Dark", null);
        menuBar.addJMenuItem(appearance, "High Contrast", null);

        menuBar.addJMenuItem(background, "Dot Grid", null);
        menuBar.addJMenuItem(background, "Cartesian Grid", null);
        menuBar.addJMenuItem(background, "No Grid", null);
    }
}
