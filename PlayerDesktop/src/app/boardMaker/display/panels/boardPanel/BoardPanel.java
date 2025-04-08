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
		
		addTab("Board",displays.getFirst());
	}
	
	public void visibility(boolean b) {
		visible = b;
	}
	
	public boolean visible() {
		return visible;
	}
	
	public boolean hasBoard() {
		return displays.getFirst().hasBoard();
	}
	
	public void setBoard(Board board) {
		if (!displays.get(getSelectedIndex()).hasBoard()) {
			displays.get(getSelectedIndex()).setBoard(board);
		} else {
			displays.add(new BoardDisplay(maker));
			displays.getLast().setBoard(board);
			addTab("board"+getTabCount(), displays.getLast());
		}
	}
}
