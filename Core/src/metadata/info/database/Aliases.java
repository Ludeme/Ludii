package metadata.info.database;

import metadata.info.InfoItem;

//-----------------------------------------------------------------------------

/**
 * Specifies a list of additional aliases for the game's name.
 * 
 * @author Matthew.Stephenson
 */
public class Aliases implements InfoItem
{
	
	/** Array of aliases. */
	private final String[] aliases;

	//-------------------------------------------------------------------------
		
	/**
	 * @param aliases	Set of additional aliases for the name of this game.
	 * 
	 * @example (aliases {"Caturanga" "Catur"})
	 */
	public Aliases(final String[] aliases)
	{
		this.aliases = aliases;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		
		sb.append("    (aliases \"" + aliases + "\")\n");
		
		return sb.toString();
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Additional aliases.
	 */
	public String[] aliases()
	{
		return aliases;
	}

	//-------------------------------------------------------------------------
	
}
