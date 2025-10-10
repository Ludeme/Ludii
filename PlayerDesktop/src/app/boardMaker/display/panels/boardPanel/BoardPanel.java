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
import app.boardMaker.res.Transformations;
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

	private BoardDrawSpace view;

	public BoardPanel(Maker maker) {
		this.displayer = maker.getDisplayer();
		this.maker = maker;

		displayer.setBoardPanel(this);

		view = new BoardDrawSpace(maker);
		addTab("View",view);
	}
	
	public void visibility(boolean b) {
		visible = b;
	}
	
	public boolean visible() {
		return visible;
	}

	public void setCreatePoly(boolean b) {
		view.setCreatePoly(b);
	}

	public void setTransformation(Transformations transformations) {
		view.setTransformation(transformations);
	}

	public void newPolygon() {
		view.newPolygon();
	}
}
