package app.boardMaker.display.tabbedBar;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import app.boardMaker.display.panels.boardPanel.BoardDisplay;
import app.boardMaker.display.panels.paramPanel.tilings.BrickPanel;
import app.boardMaker.display.panels.paramPanel.tilings.CelticPanel;
import app.boardMaker.display.panels.paramPanel.tilings.ConcentricPanel;
import app.boardMaker.display.panels.paramPanel.tilings.HexPanel;
import app.boardMaker.display.panels.paramPanel.tilings.MancalaPanel;
import app.boardMaker.display.panels.paramPanel.tilings.QuadhexPanel;
import app.boardMaker.display.panels.paramPanel.tilings.RectanglePanel;
import app.boardMaker.display.panels.paramPanel.tilings.SpiralPanel;
import app.boardMaker.display.panels.paramPanel.tilings.SquarePanel;
import app.boardMaker.display.panels.paramPanel.tilings.SurakartaRPanel;
import app.boardMaker.display.panels.paramPanel.tilings.SurakartaTPanel;
import app.boardMaker.display.panels.paramPanel.tilings.TilingPanel;
import app.boardMaker.display.panels.paramPanel.tilings.TrianglePanel;
import app.boardMaker.display.panels.paramPanel.tilings.WedgePanel;
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
			displayer.getBoardPanel().newTab();
			break;
		case "Remove" :
			displayer.getBoardPanel().removeTab();
			break;
		default:
			break;
		}
	}

}
