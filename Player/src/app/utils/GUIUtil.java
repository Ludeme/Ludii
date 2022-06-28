package app.utils;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;

import app.views.View;


/**
 * Utility functions for the GUI.
 * 
 * @author Matthew Stephenson
 */
public class GUIUtil
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * Checks if point overlaps the rectangle
	 */
	public static boolean pointOverlapsRectangle(final Point p, final Rectangle rectangle)
	{
		return pointOverlapsRectangles(p, new Rectangle[]{rectangle});
	}
	
	/**
	 * Checks if point overlaps any rectangle in the list
	 */
	public static boolean pointOverlapsRectangles(final Point p, final Rectangle[] rectangleList)
	{
		final int bufferDistance = 2;
		
		for (final Rectangle r : rectangleList)
			if (r != null)
				if
				(
					p.x > r.x - bufferDistance
					&&
					p.x < r.x+r.width + bufferDistance
					&&
					p.y > r.y - bufferDistance
					&&
					p.y < r.y+r.height + bufferDistance
				)
					return true;
		
		return false;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Returns the ViewPanel that the point is on
	 */
	public static View calculateClickedPanel(final List<View> panels, final Point pt)
	{
		View clickedPanel = null;
		
		for (final View p : panels)
		{
			final Rectangle placement = p.placement();
			if (placement.contains(pt))
			{
				clickedPanel = p;
				break;
			}
		}
		
		return clickedPanel;
	}	
	
	//-----------------------------------------------------------------------------

}
