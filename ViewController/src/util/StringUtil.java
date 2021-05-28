package util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import metadata.graphics.util.colour.ColourRoutines;
import other.topology.TopologyElement;

/**
 * Functions relating strings visuals and manipulation.
 * 
 * @author Matthew.Stephenson
 */
public class StringUtil 
{
	/**
	 * Draws a string at a specified point (graphElement can be null if not important).
	 */
	public static void drawStringAtPoint(final Graphics2D g2d, final String string, final TopologyElement graphElement, final Point2D drawPosn, final boolean withOutline)
	{
		final Rectangle2D rect = g2d.getFont().getStringBounds(string, g2d.getFontRenderContext());
		
		int posnX = 0;
		int posnY = 0;
		
		if (graphElement != null && graphElement.layer() > 1)
		{
			posnX = (int) ((drawPosn.getX() - rect.getWidth() / 2) + (graphElement.layer() / 2) * rect.getWidth() + 5);
			posnY = (int) (drawPosn.getY() + rect.getHeight() / 2.7);
		}
		else
		{
			posnX = (int) (drawPosn.getX() - rect.getWidth() / 2);
			posnY = (int) (drawPosn.getY() + rect.getHeight() / 2.7);		// No idea why 2.7 is needed to center vertically properly.
		}
		
		if (!withOutline)
			g2d.drawString(string, posnX, posnY);
		else
			drawStringWithOutline(g2d, string, posnX, posnY);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Draws a string at a specified position with a contrasting outline.
	 */
	private static void drawStringWithOutline(final Graphics2D g2d, final String string, final int posnX, final int posnY)
	{
		final Color originalFontColour = g2d.getColor();
		final Graphics2D g2dNew = (Graphics2D) g2d.create();
		g2dNew.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2dNew.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2dNew.translate(posnX, posnY);
        g2dNew.setColor(ColourRoutines.getContrastColorFavourDark(g2d.getColor()));
        final FontRenderContext frc = g2d.getFontRenderContext();
        final TextLayout tl = new TextLayout(string, g2d.getFont(), frc);
        final Shape shape = tl.getOutline(null);
        g2dNew.setStroke(new BasicStroke(g2d.getFont().getSize()/5));
        g2dNew.draw(shape);
        g2dNew.draw(shape);
        g2dNew.setColor(originalFontColour);
        g2dNew.fill(shape);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Calculate a consistent hashcode of a String.
	 */
	public static int hashCode(final String string) 
	{
	    int h = 0;
	    final int len = string.length();
	    if (len > 0) 
	    {
	        int off = 0;
	        final char val[] = string.toCharArray();
	        for (int i = 0; i < len; i++) 
	            h = 31*h + val[off++];
	    }
	    return h;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Returns true if a string can be parsed to an integer.
	 */
	public static boolean isInteger(final String strNum) 
	{
	    if (strNum == null) 
	    {
	        return false;
	    }
	    try 
	    {
	        Integer.parseInt(strNum);
	    } 
	    catch (final NumberFormatException nfe) 
	    {
	        return false;
	    }
	    return true;
	}
	
	//-------------------------------------------------------------------------
	
}
