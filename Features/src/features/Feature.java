package features;

import features.aspatial.AspatialFeature;
import features.aspatial.InterceptFeature;
import features.aspatial.PassMoveFeature;
import features.aspatial.SwapMoveFeature;
import features.spatial.AbsoluteFeature;
import features.spatial.RelativeFeature;
import game.Game;

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
			return PassMoveFeature.instance();
		else if (string.equals("SwapMove"))
			return SwapMoveFeature.instance();
		else if (string.equals("Intercept"))
			return InterceptFeature.instance();
		else
			System.err.println("Cannot construct aspatial feature from string: " + string);
		
		return null;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param game Game to visualise for
	 * @return Tikz code to visualise this feature in a Tikz environment in LaTeX.
	 */
	public abstract String generateTikzCode(final Game game);
	
	//-------------------------------------------------------------------------

}
