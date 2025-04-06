package app.boardMaker.display.panels.polygonView;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import app.boardMaker.handlers.Displayer;
import app.boardMaker.utils.Camera;
import app.boardMaker.utils.Coordinates;

public class PolygonView extends JPanel
{
	private Displayer displayer;
	private Camera camera;
	private PolygonListener pl;
	
	private int incX = 50;
	private int incY = 50;
	
	private int dotSize = 10;
	
	private List<Coordinates> vertexes;
	private Coordinates current;
	
	public PolygonView(Displayer displayer) {
		this.displayer = displayer;
		
		camera = new Camera(this);
		pl = new PolygonListener(this);
		
		vertexes = new ArrayList<Coordinates>();
		
		addMouseListener(camera);
		addMouseListener(pl);
		addMouseMotionListener(camera);
	}
	
	public Camera getCamera() {
		return camera;
	}
	
	public int incX() {
		return incX;
	}
	
	public int incY() {
		return incY;
	}
	
	public int dotSize() {
		return dotSize;
	}
	
	public void addVertex(Coordinates v) {
		if (!vertexes.contains(v)) {
			vertexes.add(v);
			current = v;
		}
	}
	
	public void removeVertex(Coordinates v) {
		if (vertexes.contains(v)) {
			vertexes.remove(v);
			current = vertexes.getLast();
		}
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
				g2d.fillOval(x + (camera.offX() % incX), getHeight() - (y + (camera.offY() % incY)), dotSize, dotSize);
			}
		}
		
		if (vertexes.size() > 0) {
			if (vertexes.size() >= 2) {
				for (int i = 0; i < vertexes.size(); i++) {
					g2d.setColor(Color.black);
					
					int x1 = vertexes.get(i).getX() * incX + camera.offX();
					int y1 = getHeight() - (vertexes.get(i).getY() + 1) * incY - camera.offY();
					int x2 = vertexes.get((i + 1) % vertexes.size()).getX() * incX + camera.offX();
					int y2 = getHeight() - (vertexes.get((i + 1) % vertexes.size()).getY() + 1) * incY - camera.offY();
					
					g2d.setStroke(new BasicStroke(5));
					g2d.drawLine(x1 + dotSize / 2, y1 + dotSize / 2, x2 + dotSize / 2, y2 + dotSize / 2);
				}
			}
			
			for (Coordinates p : vertexes) {
				if (p.equals(current)) {
					g2d.setColor(Color.red);
				} else {
					g2d.setColor(Color.blue);
				}
				
				int x = p.getX() * incX + camera.offX();
				int y = getHeight() - (p.getY() + 1) * incY - camera.offY();
				
				g2d.fillOval(x, y, dotSize, dotSize);
			}
		}
	}
}
