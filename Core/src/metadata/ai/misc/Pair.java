package metadata.ai.misc;

import main.StringRoutines;
import metadata.MetadataItem;

/**
 * Defines a pair of a String and a floating point value. Typically used
 * in AI metadata to assign a numeric value (such as a heuristic score, or
 * some other weight) to a specific piece name.
 *
 * @author Dennis Soemers
 */
public class Pair implements MetadataItem
{
	
	//-------------------------------------------------------------------------
	
	/** Our key */
	protected final String key;
	
	/** Our float value */
	protected final float floatVal;
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param key The String value.
	 * @param floatVal The floating point value.
	 * 
	 * @example (pair "Pawn" 1.0)
	 */
	public Pair
	(
		final String key, 
		final Float floatVal
	)
	{
		this.key = key;
		this.floatVal = floatVal.floatValue();
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Our key
	 */
	public final String key()
	{
		return key;
	}
	
	/**
	 * @return Our float value
	 */
	public final float floatVal()
	{
		return floatVal;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toString()
	{
		return "(pair " + StringRoutines.quote(key) + " " + floatVal + ")";
	}
	
	//-------------------------------------------------------------------------

}
