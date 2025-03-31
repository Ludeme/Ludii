package app.boardMaker.display.tabbedBar;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 * Class representing the main tabbed bar of the board maker
 */

public class BoardMakerTabbedBar extends JTabbedPane
{
	public BoardMakerTabbedBar() {
		JPanel placeholder = new JPanel();
		placeholder.add(new JLabel("Placeholder"));
		
		addTab("Game", placeholder);
		addTab("Boards", placeholder);
		addTab("Functions", placeholder);
	}
}
