package app.boardMaker.utils;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;

/**
 * Class acting as a camera for a panel using classic coordinates (not Java)
 * Controlled using mouse wheel click (button 2)
 */
public class Camera extends MouseAdapter
{
	private JPanel view;
	/** Last x position of the camera */
	private int lastX;
	/** Last y position of the camera */
	private int lastY;
	/** Total x offset **/
	private int offX = 0;
	/** Total y offset **/
	private int offY = 0;
	/** X displacement **/
	private int dx = 0;
	/** Y displacement **/
	private int dy = 0;
	/** Mouse pressed **/
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
}
