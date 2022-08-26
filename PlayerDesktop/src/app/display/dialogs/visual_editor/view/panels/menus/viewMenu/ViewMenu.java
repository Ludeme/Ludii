package app.display.dialogs.visual_editor.view.panels.menus.viewMenu;

import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.view.panels.menus.EditorMenuBar;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.util.List;

public class ViewMenu extends JMenu
{
    /**
	 * 
	 */
	private static final long serialVersionUID = -5378448276139884202L;
	public ViewMenu(EditorMenuBar menuBar)
    {
        super("View");

        JMenu appearance = new JMenu("Colour Scheme");
        JMenu background = new JMenu("Background");
        JMenu fontSize = new JMenu("Font Size");

        add(appearance);
        add(background);
        add(fontSize);

        List<String> paletteNames = Handler.palettes();
        for (String paletteName : paletteNames)
            EditorMenuBar.addJMenuItem(appearance, paletteName, e -> Handler.setPalette(paletteName));

        EditorMenuBar.addJMenuItem(background, "Dot Grid", dotGrid);
        EditorMenuBar.addJMenuItem(background, "Cartesian Grid", cartesianGrid);
        EditorMenuBar.addJMenuItem(background, "No Grid", noGrid);

        EditorMenuBar.addJMenuItem(fontSize, "Small", e->Handler.setFont("Small"));
        EditorMenuBar.addJMenuItem(fontSize, "Medium", e->Handler.setFont("Medium"));
        EditorMenuBar.addJMenuItem(fontSize, "Large", e->Handler.setFont("Large"));

    }

    final ActionListener dotGrid = e -> Handler.setBackground(Handler.DotGridBackground);
    final ActionListener cartesianGrid = e -> Handler.setBackground(Handler.CartesianGridBackground);
    final ActionListener noGrid = e -> Handler.setBackground(Handler.EmptyBackground);
}
