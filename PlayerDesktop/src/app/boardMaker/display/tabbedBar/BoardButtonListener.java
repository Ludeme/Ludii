package app.boardMaker.display.tabbedBar;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import app.boardMaker.display.panels.paramPanel.tilings.BrickPanel;
import app.boardMaker.display.panels.paramPanel.tilings.CelticPanel;
import app.boardMaker.display.panels.paramPanel.tilings.ConcentricPanel;
import app.boardMaker.display.panels.paramPanel.tilings.HexPanel;
import app.boardMaker.display.panels.paramPanel.tilings.QuadhexPanel;
import app.boardMaker.display.panels.paramPanel.tilings.SpiralPanel;
import app.boardMaker.display.panels.paramPanel.tilings.SquarePanel;
import app.boardMaker.display.panels.paramPanel.tilings.TilingPanel;
import app.boardMaker.display.panels.paramPanel.tilings.TrianglePanel;
import app.boardMaker.display.panels.paramPanel.tilings.WedgePanel;
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
			displayer.getParamPanel().setPanel(new ConcentricPanel(displayer));
			displayer.creationView();
			break;
		case "Hex" :
			displayer.getParamPanel().setPanel(new HexPanel(displayer));
			displayer.creationView();
			break;
		case "Quadhex" :
			displayer.getParamPanel().setPanel(new QuadhexPanel(displayer));
			displayer.creationView();
			break;
		case "Spiral" :
			displayer.getParamPanel().setPanel(new SpiralPanel(displayer));
			displayer.creationView();
			break;
		case "Square":
			displayer.getParamPanel().setPanel(new SquarePanel(displayer));
			displayer.creationView();
			break;
		case "Tiling" :
			displayer.getParamPanel().setPanel(new TilingPanel(displayer));
			displayer.creationView();
			break;
		case "Triangle":
			displayer.getParamPanel().setPanel(new TrianglePanel(displayer));
			displayer.creationView();
			break;
		case "Wedge" :
			displayer.getParamPanel().setPanel(new WedgePanel(displayer));
			displayer.creationView();
			break;
		default:
			displayer.creationView();
			break;
		}
	}

}
