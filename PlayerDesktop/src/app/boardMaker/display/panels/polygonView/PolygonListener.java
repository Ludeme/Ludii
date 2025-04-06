package app.boardMaker.display.panels.polygonView;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import app.boardMaker.utils.Coordinates;

public class PolygonListener extends MouseAdapter
{
	private PolygonView view;
	
	public PolygonListener(PolygonView view) {
		this.view = view;
	}

	@Override
	public void mouseClicked(MouseEvent e)
	{
		int x = e.getX();
		int y = view.getHeight() - e.getY();
		
		// Coordinates on base grid, without offset
		int correctedX = x - (view.getCamera().offX() % view.incX());
		int correctedY = y - (view.getCamera().offY() % view.incY());
		
		if ((correctedX % view.incX() < view.dotSize()) && (correctedY % view.incY() > view.incY() - view.dotSize())) {
			int vx = correctedX / view.incX() - view.getCamera().offX() / view.incX();
			int vy = correctedY / view.incY() - view.getCamera().offY() / view.incY();
			
			if (e.getButton() == 1) {
				view.addVertex(new Coordinates(vx, vy));
			} else if (e.getButton() == 3) {
				view.removeVertex(new Coordinates(vx, vy));
			} 
		} 
		
		view.revalidate();
		view.repaint();
	}
	
	
}
