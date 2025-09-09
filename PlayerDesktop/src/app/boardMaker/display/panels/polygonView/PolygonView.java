package app.boardMaker.display.panels.polygonView;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import app.boardMaker.display.panels.paramPanel.tilings.OptionPanel;
import app.boardMaker.handlers.Displayer;
import app.boardMaker.utils.Camera;
import app.boardMaker.utils.Vertex;
import game.util.graph.Poly;

public class PolygonView extends JPanel
{
	private Displayer displayer;
	private Camera camera;
	private PolygonListener pl;
	private OptionPanel op;
	
	private int incX = 50;
	private int incY = 50;
	
	private int dotSize = 10;
	
	private List<Vertex> vertexes;
	private Vertex current;
	
	public PolygonView(Displayer displayer, OptionPanel op) {
		this.displayer = displayer;
		this.op = op;
		
		camera = new Camera(this);
		pl = new PolygonListener(this);
		
		vertexes = new ArrayList<Vertex>();
		
		addMouseListener(camera);
		addMouseListener(pl);
		addMouseMotionListener(camera);
	}
	
	public Poly makePoly() {
		Float[][] pts = new Float[vertexes.size()][2];
		
		for (int i = 0; i < vertexes.size(); i++) {
			pts[i][0] = (float) vertexes.get(i).getX();
			pts[i][1] = (float) vertexes.get(i).getY();
		}
			
		return new Poly(pts, null);
	}
	
	public Camera getCamera() {
		return camera;
	}
	
	public List<Vertex> getPoly() {
		return vertexes;
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
	
	public void addVertex(Vertex v) {
		if (!vertexes.contains(v)) {
			vertexes.add(v);
			current = v;
			op.createBoard();
			displayer.getPreviewPanel().setBoard(op.board());
			displayer.getPreviewPanel().repaint();
		}
	}
	
	public void removeVertex(Vertex v) {
		if (vertexes.contains(v)) {
			vertexes.remove(v);
			current = vertexes.get(vertexes.size() - 1);
			op.createBoard();
			displayer.getPreviewPanel().setBoard(op.board());
			displayer.getPreviewPanel().repaint();
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
					
					int x1 = (int) (vertexes.get(i).getX() * incX + camera.offX());
					int y1 = (int) (getHeight() - (vertexes.get(i).getY() + 1) * incY - camera.offY());
					int x2 = (int) (vertexes.get((i + 1) % vertexes.size()).getX() * incX + camera.offX());
					int y2 = (int) (getHeight() - (vertexes.get((i + 1) % vertexes.size()).getY() + 1) * incY - camera.offY());
					
					g2d.setStroke(new BasicStroke(5));
					g2d.drawLine(x1 + dotSize / 2, y1 + dotSize / 2, x2 + dotSize / 2, y2 + dotSize / 2);
				}
			}
			
			for (Vertex p : vertexes) {
				if (p.equals(current)) {
					g2d.setColor(Color.red);
				} else {
					g2d.setColor(Color.blue);
				}
				
				int x = (int) (p.getX() * incX + camera.offX());
				int y = (int) (getHeight() - (p.getY() + 1) * incY - camera.offY());
				
				g2d.fillOval(x, y, dotSize, dotSize);
			}
		}
	}
}
