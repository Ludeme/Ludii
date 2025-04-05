package app.boardMaker.display.panels.pawnPanel;

import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import app.boardMaker.handlers.Displayer;

/**
 * A panel listing all possible pawns
 */

public class PawnPanel extends JTabbedPane
{
	private Displayer displayer;
	
	private boolean visible;
	
	public PawnPanel(Displayer displayer) {
		this.displayer = displayer;
		
		JPanel placeholder = new JPanel();
		placeholder.add(new JLabel("Placeholder"));
		
		addTab("Pawns", placeholder);
	}
	
	public void visibility(boolean b) {
		visible = b;
	}
	
	public boolean visible() {
		return visible;
	}
}
