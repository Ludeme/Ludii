package app.boardMaker.display.panels.paramPanel;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import app.boardMaker.handlers.Displayer;

/**
 * A class containing the parameters when creating boards
 */

public class ParamPanel extends JTabbedPane
{
	private Displayer displayer;
	
	public ParamPanel(Displayer displayer) {
		this.displayer = displayer;
		
		JPanel placeholder = new JPanel();
		placeholder.add(new JLabel("Placeholder"));
		
		addTab("Board parameters", placeholder);
	}
}
