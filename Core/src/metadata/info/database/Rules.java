package metadata.info.database;

import metadata.info.InfoItem;

//-----------------------------------------------------------------------------

/**
 * Specifies an English description of the rules of a game.
 * 
 * @author Matthew.Stephenson and cambolbro
 */
public class Rules implements InfoItem
{
	private final String rules;

	//-------------------------------------------------------------------------
	
	/**
	 * @param rules	An English description of the game's rules.
	 * 
	 * @example (rules "Try to make a line of four.")
	 */
	public Rules(final String rules)
	{
		this.rules = rules;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		
		sb.append("    (rules \"" + rules + "\")\n");
		
		return sb.toString();
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return English description of the rules.
	 */
	public String rules()
	{
		return rules;
	}
	
	//-------------------------------------------------------------------------

}
