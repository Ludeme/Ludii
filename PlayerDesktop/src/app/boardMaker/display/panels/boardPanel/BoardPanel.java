package app.boardMaker.display.panels.boardPanel;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import app.boardMaker.handlers.Displayer;

/**
 * Class representing the panel where the board is displayed
 */

public class BoardPanel extends JTabbedPane
{
	private Displayer displayer;
	
	private boolean visible;
	private boolean hasBoard = false;
	
	public BoardPanel(Displayer displayer) {
		this.displayer = displayer;
		
		JPanel placeholder = new JPanel();
		placeholder.add(new JLabel("Placeholder"));
		
		addTab("Board",placeholder);
	}
	
	public void visibility(boolean b) {
		visible = b;
	}
	
	public boolean visible() {
		return visible;
	}
	
	public void setHasBoard(boolean b) {
		hasBoard = b;
	}
	
	public boolean hasBoard() {
		return hasBoard;
	}
}
