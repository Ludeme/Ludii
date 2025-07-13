package app.boardMaker.display.tabbedBar;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import app.boardMaker.handlers.Displayer;
import app.boardMaker.handlers.Maker;

public class BoardButtonListener implements ActionListener
{

	private Maker maker;
	private Displayer displayer;
	
	public BoardButtonListener(Maker maker) {
		this.displayer = maker.getDisplayer();
		this.maker = maker;
	}
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		String command = e.getActionCommand();
		System.out.println(command);
		
		switch (command)
		{
		case "New" :
			break;
		case "Remove" :
			break;
		default:
			break;
		}
	}

}
