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

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(floatVal);
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
			return true;

		if (!(obj instanceof Pair))
			return false;
		
		final Pair other = (Pair) obj;
		if (floatVal != other.floatVal)
			return false;
		
		if (key == null)
		{
			if (other.key != null)
				return false;
		}
		else if (!key.equals(other.key))
		{
			return false;
		}
		
		return true;
	}
	
	//-------------------------------------------------------------------------

}
