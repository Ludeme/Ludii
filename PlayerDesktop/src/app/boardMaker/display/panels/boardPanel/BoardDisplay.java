package app.boardMaker.display.panels.boardPanel;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

import app.boardMaker.handlers.Maker;
import game.equipment.container.board.Board;

public class BoardDisplay extends JPanel
{
	private Maker maker;
	
	private boolean hasBoard = false;
	private double boardRatio = 1.0;
	
	private Board board;
	
	public BoardDisplay(Maker maker) {
		this.maker = maker;
		
		setOpaque(true);
	}

	@Override
	protected void paintComponent(Graphics g)
	{
		Graphics2D g2d = (Graphics2D) g;
		
		g2d.setBackground(Color.white);
		g2d.clearRect(0, 0, getWidth(), getHeight());
		
		if (board != null) {
			int boardSize = Math.min(getHeight(), (int)(getWidth() * boardRatio));
			maker.drawBoard(g2d,board,boardSize,boardSize);
		}
	}
	
	public boolean hasBoard() {
		return hasBoard;
	}
	
	public void setBoard(Board board) {
		hasBoard = true;
		
		this.board = board;
	}
}
