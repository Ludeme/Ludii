package app.boardMaker.display.panels.polygonView;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

import app.boardMaker.handlers.Displayer;

public class PolygonView extends JPanel
{
	private Displayer displayer;
	
	private int incX = 50;
	private int incY = 50;
	
	private int lastX;
	private int lastY;
	
	private int offX;
	private int offY;
	
	public PolygonView(Displayer displayer) {
		this.displayer = displayer;
		
		PolygonMouseAdapter pl = new PolygonMouseAdapter(this);
		addMouseListener(pl);
		addMouseMotionListener(pl);
	}
	
	public void setLastPoint(int x, int y) {
		lastX = x;
		lastY = y;
	}
	
	public int getLastX() {
		return lastX;
	}
	
	public int getLastY() {
		return lastY;
	}
	
	public void addOffset(int x, int y) {
		offX += x;
		offY += y;
	}

	@Override
	protected void paintComponent(Graphics g)
	{
		Graphics2D g2d = (Graphics2D) g;
		
		g2d.setBackground(Color.white);
		g2d.clearRect(0, 0, getWidth(), getHeight());
		
		g2d.setColor(Color.lightGray);
		for (int x = 0 - incX; x <= getWidth() + incX; x += incX) {
			for (int y = 0 - incY; y <= getHeight() + incY; y += incY) {
				g2d.fillOval(x + (offX % incX), getHeight() - (y + (offY % incY)), 10, 10);
			}
		}
	}
}
