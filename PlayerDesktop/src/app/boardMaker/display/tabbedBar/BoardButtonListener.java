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
		case "Brick" :
			maker.setMancala(false);
			maker.setSurakarta(false);
			displayer.getParamPanel().setPanel(new BrickPanel(displayer));
			displayer.creationView();
			break;
		case "Celtic" :
			maker.setMancala(false);
			maker.setSurakarta(false);
			displayer.getParamPanel().setPanel(new CelticPanel(displayer));
			displayer.creationView();
			break;
		case "Concentric" :
			maker.setMancala(false);
			maker.setSurakarta(false);
			displayer.getParamPanel().setPanel(new ConcentricPanel(displayer));
			displayer.creationView();
			break;
		case "Hex" :
			maker.setMancala(false);
			maker.setSurakarta(false);
			displayer.getParamPanel().setPanel(new HexPanel(displayer));
			displayer.creationView();
			break;
		case "Quadhex" :
			maker.setMancala(false);
			maker.setSurakarta(false);
			displayer.getParamPanel().setPanel(new QuadhexPanel(displayer));
			displayer.creationView();
			break;
		case "Rectangle":
			maker.setMancala(false);
			maker.setSurakarta(false);
			displayer.getParamPanel().setPanel(new RectanglePanel(displayer));
			displayer.creationView();
			break;
		case "Spiral" :
			maker.setMancala(false);
			maker.setSurakarta(false);
			displayer.getParamPanel().setPanel(new SpiralPanel(displayer));
			displayer.creationView();
			break;
		case "Square":
			maker.setMancala(false);
			maker.setSurakarta(false);
			displayer.getParamPanel().setPanel(new SquarePanel(displayer));
			displayer.creationView();
			break;
		case "Tiling" :
			maker.setMancala(false);
			maker.setSurakarta(false);
			displayer.getParamPanel().setPanel(new TilingPanel(displayer));
			displayer.creationView();
			break;
		case "Triangle":
			maker.setMancala(false);
			maker.setSurakarta(false);
			displayer.getParamPanel().setPanel(new TrianglePanel(displayer));
			displayer.creationView();
			break;
		case "Wedge" :
			maker.setMancala(false);
			maker.setSurakarta(false);
			displayer.getParamPanel().setPanel(new WedgePanel(displayer));
			displayer.creationView();
			break;
		case "New" :
			displayer.getBoardPanel().newTab();
			break;
		case "Remove" :
			displayer.getBoardPanel().removeTab();
			break;
		case "Mancala" :
			maker.setMancala(true);
			maker.setSurakarta(false);
			displayer.getParamPanel().setPanel(new MancalaPanel(maker));
			displayer.creationView();
			break;
		case "SurakartaR" :
			maker.setMancala(false);
			maker.setSurakarta(true);
			displayer.getParamPanel().setPanel(new SurakartaRPanel(maker));
			displayer.creationView();
			break;
		case "SurakartaT" :
			maker.setMancala(false);
			maker.setSurakarta(true);
			displayer.getParamPanel().setPanel(new SurakartaTPanel(maker));
			displayer.creationView();
			break;
		default:
			displayer.creationView();
			break;
		}
	}

}
