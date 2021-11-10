package app.display.dialogs.visual_editor;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JPanel;

import app.PlayerApp;

//-----------------------------------------------------------------------------

/**
 * Visual editor view.
 * @author cambolbro
 */
public class VisualEditorPanel extends JPanel implements MouseListener, MouseMotionListener
{
	private static final long serialVersionUID = 1L;

	private final PlayerApp app;
	
	//-------------------------------------------------------------------------
	
	public VisualEditorPanel(final PlayerApp app)
	{
		this.app = app;
		
	   	addMouseListener(this);
    	addMouseMotionListener(this);
	}

	//-------------------------------------------------------------------------

 	@Override
    public void paint(final Graphics g) 
    {
       	final Graphics2D g2d = (Graphics2D)g;
       	g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    	g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    	
    	// Clear the view
    	g2d.setPaint(Color.white);
   		g2d.fillRect(0, 0, getWidth(), getHeight());
    }

 	//-------------------------------------------------------------------------
	
	@Override
	public void mouseClicked(MouseEvent arg0)
	{
	}

	@Override
	public void mouseEntered(MouseEvent arg0)
	{
	}

	@Override
	public void mouseExited(MouseEvent arg0)
	{
	}

	@Override
	public void mousePressed(MouseEvent arg0)
	{
		System.out.println("Mouse pressed at: " + arg0.getPoint());
	}

	@Override
	public void mouseReleased(MouseEvent arg0)
	{
		System.out.println("Mouse released at: " + arg0.getPoint());
	}

	@Override
	public void mouseDragged(MouseEvent arg0)
	{
		System.out.println("Mouse dragged to: " + arg0.getPoint());
	}

	@Override
	public void mouseMoved(MouseEvent arg0)
	{
	}

	//-------------------------------------------------------------------------

	
}
