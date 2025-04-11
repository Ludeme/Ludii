package app.boardMaker.display.tabbedBar;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import app.boardMaker.display.panels.paramPanel.tilings.BrickPanel;
import app.boardMaker.display.panels.paramPanel.tilings.CelticPanel;
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
		
		switch (command)
		{
		case "Brick" :
			displayer.getParamPanel().setPanel(new BrickPanel(displayer));
			displayer.creationView();
			break;
		case "Celtic" :
			displayer.getParamPanel().setPanel(new CelticPanel(displayer));
			displayer.creationView();
			break;
		case "Concentric" :
			break;
		case "Hex" :
			break;
		case "Quadhex" :
			break;
		case "Spiral" :
			break;
		case "Square":
			displayer.getParamPanel().setPanel(new SquarePanel(displayer));
			displayer.creationView();
			break;
		case "Tiling" :
			break;
		case "Triangle":
			displayer.getParamPanel().setPanel(new TrianglePanel(displayer));
			displayer.creationView();
			break;
		case "Wedge" :
			break;
		default:
			displayer.creationView();
			break;
		}
	}

}
