package app.boardMaker.display.panels.polygonView;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class PolygonMouseAdapter extends MouseAdapter
{
	private PolygonView polyView;
	
	public PolygonMouseAdapter(PolygonView pv) {
		polyView = pv;
	}

	@Override
	public void mouseDragged(MouseEvent e)
	{
		int x = e.getX();
		int y = polyView.getHeight() - e.getY();
		
		int dx = x - polyView.getLastX();
		int dy = y - polyView.getLastY();
		
		polyView.setLastPoint(x,y);
		polyView.addOffset(dx, dy);
		
		polyView.revalidate();
		polyView.repaint();
	}

	@Override
	public void mouseClicked(MouseEvent e)
	{
		
	}

	@Override
	public void mousePressed(MouseEvent e)
	{
		System.out.println(e.getButton());
		polyView.setLastPoint(e.getX(),polyView.getHeight() - e.getY());
	}
}
