package app.boardMaker.display.panels.boardPanel;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

import app.boardMaker.handlers.Maker;
import app.utils.SVGUtil;
import game.equipment.container.board.Board;
import game.functions.graph.GraphFunction;

public class BoardDisplay extends JPanel
{
	private Maker maker;
	
	private boolean hasBoard = false;
	private boolean hasChanged = false;
	private double boardRatio = 1.0;
	
	private GraphFunction graph;
	private Board board;
	private String svg;
	
	public BoardDisplay(Maker maker) {
		this.maker = maker;
		
		setOpaque(true);
	}

	@Override
	protected void paintComponent(Graphics g)
	{		
		Graphics2D g2d = (Graphics2D) g;
		
		g2d.setColor(Color.white);
		g2d.fillRect(0, 0, getWidth(), getHeight());
		
		int boardSize = Math.min(getHeight(), (int)(getWidth() * boardRatio));
		
		if (graph != null || board != null) {
			// Need this to avoid a bug where redrawing the window makes the board smaller
			if (hasChanged) {
				maker.drawBoard(g2d,graph,board,boardSize,boardSize);
				hasChanged = false;
			} else {
				g2d.drawImage(SVGUtil.createSVGImage(svg, getWidth(), getHeight()), 0, 0, null);
			}
		}
	}
	
	public void switchStyle() {
		hasChanged = true;
	}
	
	public boolean hasBoard() {
		return hasBoard;
	}
	
	public void setBoard(GraphFunction graph) {
		hasBoard = true;
		hasChanged = true;
		this.graph = graph;
	}
	
	public void setBoard(Board board) {
		hasBoard = true;
		hasChanged = true;
		this.board = board;
	}
	
	public void setSVG(String svg) {
		this.svg = svg;
	}
}
