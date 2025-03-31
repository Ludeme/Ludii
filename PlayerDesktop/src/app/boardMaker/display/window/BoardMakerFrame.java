package app.boardMaker.display.window;

import java.awt.Dimension;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

import app.boardMaker.handlers.Displayer;
import app.boardMaker.menu.BoardMakerMenu;
import app.display.dialogs.visual_editor.view.designPalettes.DesignPalette;
import app.util.SettingsDesktop;

/**
 * Frame of the board maker
 */
public class BoardMakerFrame extends JFrame
{
	/** App title */
	private final String title = "Ludii Board Maker";
	
	/** Ludii icon */
	private final ImageIcon icon = DesignPalette.LUDII_ICON;
	
	private Displayer displayer;
	
	public BoardMakerFrame(Displayer displayer) {
		this.displayer = displayer;
		
		setTitle(title);
		setIconImage(icon.getImage());
		setPreferredSize(new Dimension(SettingsDesktop.defaultWidth,SettingsDesktop.defaultHeight));
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		BoardMakerMenu menu = new BoardMakerMenu();
		setJMenuBar(menu);
		setContentPane(new BoardMakerPane(displayer));
		
		pack();
		setVisible(true);
	}
	
}
