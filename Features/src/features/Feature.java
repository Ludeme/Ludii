package features;

import features.aspatial.AspatialFeature;
import features.aspatial.InterceptFeature;
import features.aspatial.PassMoveFeature;
import features.aspatial.SwapMoveFeature;
import features.spatial.AbsoluteFeature;
import features.spatial.RelativeFeature;

/**
 * Abstract class for features; can be spatial or aspatial.
 *
 * @author Dennis Soemers
 */
public abstract class Feature
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param string
	 * @return Feature constructed from given stirng
	 */
	public static Feature fromString(final String string)
	{
		if (string.contains("abs:"))
			return new AbsoluteFeature(string);
		else if (string.contains("rel:"))
			return new RelativeFeature(string);
		else
			return aspatialFromString(string);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param string
	 * @return Aspatial feature constructed from given string
	 */
	private static AspatialFeature aspatialFromString(final String string)
	{
		if (string.equals("PassMove"))
			return new PassMoveFeature();
		else if (string.equals("SwapMove"))
			return new SwapMoveFeature();
		else if (string.equals("Intercept"))
			return new InterceptFeature();
		else
			System.err.println("Cannot construct aspatial feature from string: " + string);
		
		return null;
	}
	
	//-------------------------------------------------------------------------

}
