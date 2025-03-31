package app.boardMaker.display.panels.welcomePanel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

import app.boardMaker.handlers.Displayer;

public class WelcomePanel extends JPanel
{
	private Displayer displayer;
	
	private boolean visible;
	
	public WelcomePanel(Displayer displayer) {
		this.displayer = displayer;
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		add(Box.createVerticalGlue());
		
		JButton button = new JButton("New Game");
		button.setAlignmentX(CENTER_ALIGNMENT);
		button.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				displayer.mainView();
			}
		});
		add(button);
		
		add(Box.createVerticalGlue());
		
		setOpaque(true);
	}
	
	public void visibility(boolean b) {
		visible = b;
	}
	
	
	public boolean visible() {
		return visible;
	}
}
