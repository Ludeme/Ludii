package app.boardMaker.display.panels.previewPanel;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import app.boardMaker.handlers.Displayer;

/**
 * A class for the preview of the board during its creation
 */

public class PreviewPanel extends JTabbedPane
{
	private Displayer displayer;
	
	private boolean visible;
	
	public PreviewPanel(Displayer displayer) {
		this.displayer = displayer;
		
		JPanel placeholder = new JPanel();
		placeholder.add(new JLabel("Placeholder"));
		
		addTab("Preview", placeholder);
	}
	
	public void visibility(boolean b) {
		visible = b;
	}
	
	public boolean visible() {
		return visible;
	}
}
