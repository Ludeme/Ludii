package metadata.info.database;

import metadata.info.InfoItem;

//-----------------------------------------------------------------------------

/**
 * Specifies a description of the game.
 * 
 * @author Matthew.Stephenson
 */
public class Description implements InfoItem
{
	
	/** English description of the game. */
	private final String description;

	//-------------------------------------------------------------------------
	
	/**
	 * @param description	An English description of the game.
	 * 
	 * @example (description "A traditional game that comes from Egypt.")
	 */
	public Description(final String description)
	{
		this.description = description;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		
		sb.append("    (description \"" + description + "\")\n");
		
		return sb.toString();
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return English description of the game.
	 */
	public String description()
	{
		return description;
	}
	
	//-------------------------------------------------------------------------

}
