package metadata.graphics.util;

import metadata.MetadataItem;

//-----------------------------------------------------------------------------

/**
 * Supported style types for drawing curves.
 * 
 * @author matthew.stephenson
 */
public enum CurveType implements MetadataItem
{
	/** Spline curve based on relative distances. */
	Spline,
	
	/** Bezier curve based on absolute distances. */ 
	Bezier;
	
	//-----------------------------------------------------------------------------
	
	/**
	 * @param name
	 * @return The curve type from its name.
	 */
	public static CurveType fromName(final String name)
	{
		try
		{
			return valueOf(name);
		}
		catch (final Exception e)
		{
			return Spline;
		}
	}
	
}
