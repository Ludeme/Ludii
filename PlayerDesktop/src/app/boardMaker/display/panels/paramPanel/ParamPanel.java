package app.boardMaker.display.panels.paramPanel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
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
	
	private boolean visible;
	
	public ParamPanel(Displayer displayer) {
		this.displayer = displayer;
		
		JPanel placeholder = new JPanel();
		placeholder.add(new JLabel("Placeholder"));
		
		JButton button = new JButton("Confirm placeholder");
		button.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				displayer.getBoardPanel().setHasBoard(true);
				displayer.mainView();		
			}
		});
		placeholder.add(button);
		
		addTab("Board parameters", placeholder);
	}
	
	public void visibility(boolean b) {
		visible = b;
	}
	
	public boolean visible() {
		return visible;
	}
}
