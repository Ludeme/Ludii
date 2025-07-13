package app.boardMaker.display.panels.boardPanel;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import app.boardMaker.handlers.Displayer;
import app.boardMaker.handlers.Maker;
import app.utils.SVGUtil;
import game.equipment.container.board.Board;
import game.functions.graph.GraphFunction;

/**
 * Class representing the panel where the board is displayed
 */

public class BoardPanel extends JTabbedPane
{
	private Displayer displayer;
	private Maker maker;

	private boolean visible;

	public BoardPanel(Maker maker) {
		this.displayer = maker.getDisplayer();
		this.maker = maker;

		displayer.setBoardPanel(this);
		
		addTab("View",new BoardDrawSpace(maker));
	}
	
	public void visibility(boolean b) {
		visible = b;
	}
	
	public boolean visible() {
		return visible;
	}
}
