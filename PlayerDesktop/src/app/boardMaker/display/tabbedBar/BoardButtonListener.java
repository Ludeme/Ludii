package app.boardMaker.display.tabbedBar;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import app.boardMaker.display.panels.paramPanel.tilings.SquarePanel;
import app.boardMaker.display.panels.paramPanel.tilings.TrianglePanel;
import app.boardMaker.handlers.Displayer;

public class BoardButtonListener implements ActionListener
{

	private Displayer displayer;
	
	public BoardButtonListener(Displayer displayer) {
		this.displayer = displayer;
	}
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		String command = e.getActionCommand();
		System.out.println(command);
		
		switch (command)
		{
		case "Square":
			displayer.getParamPanel().setPanel(new SquarePanel(displayer));
			displayer.creationView();
			break;
			
		case "Triangle":
			displayer.getParamPanel().setPanel(new TrianglePanel(displayer));
			displayer.creationView();
			break;
		default:
			displayer.creationView();
			break;
		}
	}

}
