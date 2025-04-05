package app.boardMaker.display.panels.paramPanel.tilings;

import java.awt.BorderLayout;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import app.boardMaker.handlers.Displayer;

public class TrianglePanel extends JPanel
{
	private Displayer displayer;
	
	public TrianglePanel(Displayer displayer)
	{
		super();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));	
		
		this.displayer = displayer;
		
		JLabel label = new JLabel("Triangle");
		add(label);
	}
}
