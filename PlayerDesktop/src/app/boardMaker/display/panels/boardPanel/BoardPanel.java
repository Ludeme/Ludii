package app.boardMaker.display.panels.boardPanel;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import app.boardMaker.handlers.Displayer;
import app.boardMaker.handlers.Maker;
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
	
	private List<BoardDisplay> displays;
	
	public BoardPanel(Maker maker) {
		this.displayer = maker.getDisplayer();
		this.maker = maker;
		
		displayer.setBoardPanel(this);
		
		displays = new ArrayList<BoardDisplay>();
		displays.add(new BoardDisplay(maker));
		
		addTab("View",displays.get(0));
	}
	
	/**
	 * Adds a new tab to display another board.
	 */
	public void newTab() {
		displays.add(new BoardDisplay(maker));
		addTab("Board"+getTabCount(), displays.get(displays.size() - 1));
	}
	
	/**
	 * Removes the currently selected tab
	 */
	public void removeTab() {
		if (getTabCount() > 1) {
			int idx = getSelectedIndex();
			remove(idx);
			displays.remove(idx);
		}
	}
	
	public void switchStyle() {
		for (BoardDisplay display : displays) {
			display.switchStyle();
		}
	}
	
	public void visibility(boolean b) {
		visible = b;
	}
	
	public boolean visible() {
		return visible;
	}
	
	public boolean hasBoard() {
		return displays.get(getSelectedIndex()).hasBoard();
	}
	
	public void setBoard(GraphFunction graph) {
		displays.get(getSelectedIndex()).setBoard(graph);
	}
	
	public void setBoard(Board board) {
		displays.get(getSelectedIndex()).setBoard(board);
	}
	
	public void setSVG(String svg) {
		displays.get(getSelectedIndex()).setSVG(svg);
	}
}
