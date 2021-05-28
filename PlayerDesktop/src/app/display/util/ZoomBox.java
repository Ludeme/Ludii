package app.display.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JWindow;

import app.DesktopApp;
import app.PlayerApp;
import app.display.MainWindowDesktop;

/**
 * Magnifying glass like zoom view.
 * Only useful for screens with a very high pixel density.
 * https://stackoverflow.com/questions/18158550/zoom-box-for-area-around-mouse-location-on-screen
 * 
 * @author Matthew.Stephenson
 */
public class ZoomBox extends JPanel 
{
	private static final long serialVersionUID = 1L;
	
	//-------------------------------------------------------------------------
	
	final JWindow popup;
	private final JComponent parent;
	private BufferedImage buffer;
	
    private static final int ZOOM_AREA = 200;
    private static final float zoomLevel = 2.0f;

    //-------------------------------------------------------------------------

    /**
     * Create ZoomBox window.
     */
    public ZoomBox(final PlayerApp app, final MainWindowDesktop parent) 
    {
    	this.parent = parent;
    	popup = new JWindow();
    	popup.setLayout(new BorderLayout());
    	popup.add(this);
    	popup.pack();
    	final MouseAdapter ma = new MouseAdapter() 
    	{
    		@Override
    		public void mouseMoved(final MouseEvent e) 
    		{
	        	if 
	        	(
	        		app.settingsPlayer().showZoomBox()
	        		&& 
	        		DesktopApp.view().getBoardPanel().placement().contains(e.getPoint())
	        	)
	        	{
	        		popup.setVisible(true);
	        		final Point p = e.getPoint();
	        		final Point pos = e.getLocationOnScreen();
	        		updateBuffer(p);
	        		popup.setLocation(pos.x + 16, pos.y + 16);
	        		repaint();
	        	}
	        	else
	        	{
	        		popup.setVisible(false);
	        	}
	        }

	        @Override
	        public void mouseExited(final MouseEvent e) 
	        {
	        	popup.setVisible(false);
	        }
        
    	};
      
    	parent.addMouseListener(ma);
    	parent.addMouseMotionListener(ma);
    }
    
    //-------------------------------------------------------------------------

    /**
     * Update displayed visuals.
     */
    protected void updateBuffer(final Point p) 
    {
    	final int width = Math.round(ZOOM_AREA);
    	final int height = Math.round(ZOOM_AREA);
    	buffer = new BufferedImage(width-2, height-2, BufferedImage.TYPE_INT_ARGB);
    	final Graphics2D g2d = buffer.createGraphics();
    	final AffineTransform at = new AffineTransform();

    	int xPos = (ZOOM_AREA / 2) - p.x;
    	int yPos = (ZOOM_AREA / 2) - p.y;

    	if (xPos > 0) 
    	{
    		xPos = 0;
    	}
    	if (yPos > 0) 
    	{
    		yPos = 0;
    	}

    	if ((xPos * -1) + ZOOM_AREA > parent.getWidth()) 
    	{
    		xPos = (parent.getWidth() - ZOOM_AREA) * -1;
    	}
    	if ((yPos * -1) + ZOOM_AREA > parent.getHeight()) 
    	{
    		yPos = (parent.getHeight()- ZOOM_AREA) * -1;
    	}

    	at.translate(xPos, yPos);
    	g2d.setTransform(at);
    	parent.paint(g2d);
    	g2d.dispose();
    }
    
    //-------------------------------------------------------------------------

    @Override
    public Dimension getPreferredSize() 
    {
    	return new Dimension(Math.round(ZOOM_AREA * zoomLevel), Math.round(ZOOM_AREA * zoomLevel));
    }
    
    //-------------------------------------------------------------------------

    @Override
    protected void paintComponent(final Graphics g) 
    {
    	super.paintComponent(g);
    	final Graphics2D g2d = (Graphics2D) g.create();
    	g2d.setColor(Color.BLACK);
    	g2d.fillRect(0, 0, getWidth(), getHeight());
    	g2d.setColor(Color.WHITE);
    	g2d.fillRect(1, 1, getWidth()-2, getHeight()-2);
    	if (buffer != null) 
    	{
    		final AffineTransform at = g2d.getTransform();
    		g2d.setTransform(AffineTransform.getScaleInstance(zoomLevel, zoomLevel));
    		g2d.drawImage(buffer, 1, 1, this);
    		g2d.setTransform(at);
    	}
    	g2d.dispose();
    }
    
    //-------------------------------------------------------------------------
}