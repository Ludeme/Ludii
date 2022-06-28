package app.utils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Arc2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Timer;

/**
 * Functions for the spinner graphic
 * 
 * @author cambolbro and matthew.stephenson
 */
public class Spinner
{
	//-------------------------------------------------------------------------
	// parameters relating to the spinning loading icon.
	
	private int numPts = 12;
	private double dotRadius = 1.5;
	
	private Rectangle2D.Double originalRect = new Rectangle2D.Double();
	private final Rectangle spinRect = new Rectangle();
	private final List<Point2D.Double> spinPts = new ArrayList<>();

	private Timer spinTimer = null;
	int spinTicks = -1;
	
	//-------------------------------------------------------------------------
	
	public Spinner(final Rectangle2D.Double bounds)
	{
		originalRect = bounds;
		
		final int r = (int) (bounds.getWidth()/2);
		final int cx = (int) (bounds.getX() + r);
		final int cy = (int) (bounds.getY() + r);

		for (int n = 0; n < numPts-1; n++)
		{
			final double t = n / (double)(numPts - 1);
			final double x = cx + r * Math.sin(t * 2 * Math.PI);
			final double y = cy - r * Math.cos(t * 2 * Math.PI);
			spinPts.add(new Point2D.Double(x, y));
		}

		spinRect.setBounds(cx - 2*r, cy - 2*r, 4 * r ,  4 * r);
	}
	
	//-------------------------------------------------------------------------

	public void startSpinner()
	{
		if (spinTimer != null)
			return;

		spinTicks = 0;
		
		final int ms = (int)(1.0 / numPts * 1000);
		spinTimer = new Timer(ms, spinTimerAction);
	   	spinTimer.start();
	}
	
	//-------------------------------------------------------------------------

	public void stopSpinner()
	{
		if (spinTimer != null)
		{
			spinTimer.stop();
			spinTimer = null;
		}
		
		spinTicks = -1;
	}
	
	//-------------------------------------------------------------------------

	public void drawSpinner(final Graphics2D g2d)
	{
		if (spinTicks < 0)
			return;

		for (int n = 0; n < numPts; n++)
		{
			final int dotAt = spinTicks - n; 
			if (dotAt < 0)
				continue;

			final Point2D.Double pt = spinPts.get(dotAt % spinPts.size());
			final java.awt.Shape dot = new Arc2D.Double(pt.x-dotRadius, pt.y-dotRadius, 2*dotRadius+1, 2*dotRadius+1, 0, 360, 0);
			
			final double t = Math.pow((numPts - n) / (double)numPts, 3);
			final int alpha = (int)(t * 255); 
			final Color dotColour = new Color(160, 160, 160, alpha);
			
			g2d.setColor(dotColour);
			g2d.fill(dot);
		}
	}
	
	//-------------------------------------------------------------------------

	ActionListener spinTimerAction = new ActionListener()
    {
    	@Override
		public void actionPerformed(final ActionEvent e)
    	{
    		spinTicks++;
     	}
    };
    
    //-------------------------------------------------------------------------
    
    public Rectangle2D.Double originalRect() 
    {
		return originalRect;
	}

	public void setDotRadius(double dotRadius) 
	{
		this.dotRadius = dotRadius;
	}

	public void setNumPts(int numPts) 
	{
		this.numPts = numPts;
	}
    
  //-------------------------------------------------------------------------
	
}
