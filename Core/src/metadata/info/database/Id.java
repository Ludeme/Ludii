package metadata.info.database;

import metadata.info.InfoItem;

//-----------------------------------------------------------------------------

/**
 * Specifies the database Id for the currently chosen ruleset.
 * 
 * @author Matthew.Stephenson
 */
public class Id implements InfoItem
{
	
	/** Ruleset Database Id. */
	private final String id;

	//-------------------------------------------------------------------------

	/**
	 * @param id The ruleset database table Id.
	 * 
	 * @example (id "35")
	 */
	public Id(final String id)
	{
		this.id = id;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		
		sb.append("    (id \"" + id + "\")\n");
		
		return sb.toString();
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return The ruleset database table Id.
	 */
	public String id()
	{
		return id;
	}
	
	//-------------------------------------------------------------------------

}
