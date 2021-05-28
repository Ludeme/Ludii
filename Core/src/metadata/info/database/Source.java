package metadata.info.database;

import metadata.info.InfoItem;

//-----------------------------------------------------------------------------

/**
 * Specifies the reference for the game, or its currently chosen ruleset.
 * 
 * @author Matthew.Stephenson
 */
public class Source implements InfoItem
{
	
	/** Rules source. */
	private final String source;

	//-------------------------------------------------------------------------

	/**
	 * @param source The source of the game's rules.
	 * 
	 * @example (source "Murray 1969")
	 */
	public Source(final String source)
	{
		this.source = source;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		
		sb.append("    (source \"" + source + "\")\n");
		
		return sb.toString();
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return The source of the game's rules.
	 */
	public String source()
	{
		return source;
	}
	
	//-------------------------------------------------------------------------

}
