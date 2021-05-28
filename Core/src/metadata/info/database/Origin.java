package metadata.info.database;

import metadata.info.InfoItem;

//-----------------------------------------------------------------------------

/**
 * Specifies the location of the earliest known origin for this game.
 * 
 * @author cambolbro
 */
public class Origin implements InfoItem
{
	private final String origin;

	//-------------------------------------------------------------------------
	
	/**
	 * @param origin Earliest known origin for this game.
	 * 
	 * @example (origin "1953")
	 */
	public Origin(final String origin)
	{
		this.origin = origin;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		
		sb.append("    (origin \"" + origin + "\")\n");
		
		return sb.toString();
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return The origin in a string.
	 */
	public String origin()
	{
		return origin;
	}
	
	//-------------------------------------------------------------------------

}
