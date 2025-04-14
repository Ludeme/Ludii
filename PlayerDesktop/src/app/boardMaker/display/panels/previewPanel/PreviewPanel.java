package app.boardMaker.display.panels.previewPanel;

import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import app.boardMaker.display.panels.boardPanel.BoardDisplay;
import app.boardMaker.handlers.Displayer;
import app.boardMaker.handlers.Maker;
import game.equipment.container.board.Board;
import game.functions.graph.GraphFunction;

/**
 * A class for the preview of the board during its creation
 */

public class PreviewPanel extends JTabbedPane
{
	private Displayer displayer;
	private Maker maker;
	
	private BoardDisplay display;
	
	private boolean visible;
	
	public PreviewPanel(Maker maker) {
		this.displayer = maker.getDisplayer();
		this.maker = maker;
		
		displayer.setPreviewPanel(this);
		
		display = new BoardDisplay(maker);
		addTab("Preview", display);
	}
	
	public void switchStyle() {
		display.switchStyle();
	}
	
	public void visibility(boolean b) {
		visible = b;
	}
	
	public boolean visible() {
		return visible;
	}
	
	public void setBoard(GraphFunction graph) {
		display.setBoard(graph);
	}
	
	public void setBoard(Board board) {
		display.setBoard(board);
	}
	
	public void setSVG(String svg) {
		display.setSVG(svg);
	}
}
