package app.display.dialogs.visual_editor.view.panels.menus;

import app.display.dialogs.visual_editor.handler.Handler;

import javax.swing.*;
import java.awt.event.ActionListener;

public class RunMenu extends JMenu
{
    /**
	 * 
	 */
	private static final long serialVersionUID = -7766009187364586583L;
	public RunMenu(EditorMenuBar menuBar)
    {
        super("Run");
        menuBar.addJMenuItem(this, "Compile", compile, KeyStroke.getKeyStroke("control shift R"));
        menuBar.addJMenuItem(this, "Play Game", play, KeyStroke.getKeyStroke("control P"));
        add(new JSeparator());
        menuBar.addJCheckBoxMenuItem(this, "Auto Compile", Handler.liveCompile, autocompile);
    }

    final ActionListener autocompile = e -> Handler.liveCompile = ((JCheckBoxMenuItem) e.getSource()).isSelected();
    final ActionListener compile = e -> Handler.compile(true);
    final ActionListener play = e -> Handler.play();

}
