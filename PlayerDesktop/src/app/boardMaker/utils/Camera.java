package app.boardMaker.utils;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;

/**
 * Class acting as a camera for a panel using classic coordinates (not Java)
 */
public class Camera extends MouseAdapter
{
	private JPanel view;
	
	private int lastX;
	private int lastY;
	private int offX = 0;
	private int offY = 0;
	private int dx = 0;
	private int dy = 0;
	
	private boolean pressed = false;
	
	public Camera(JPanel view) {
		this.view = view;
	}
	
	public int offX() {
		return offX;
	}
	
	public int offY() {
		return offY;
	}
	
	@Override
	public void mousePressed(MouseEvent e)
	{
		if (e.getButton() == 2) {
			lastX = e.getX();
			lastY = view.getHeight() - e.getY();
			pressed = true;
		}
	}

	@Override
	public void mouseDragged(MouseEvent e)
	{
		if (pressed) {
			dx = (e.getX() - lastX);
			dy = (view.getHeight() - e.getY() - lastY);

			offX += dx;
			offY += dy;

			lastX = e.getX();
			lastY = view.getHeight() - e.getY();
			
			view.revalidate();
			view.repaint();
		}
	}

	@Override
	public void mouseReleased(MouseEvent e)
	{
		if (e.getButton() == 2) {
			pressed = false;
		}
	}

	public int dx() {
		return dx;
	}

	public int dy() {
		return dy;
	}
}
