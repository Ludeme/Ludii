package metadata.info.database;

import metadata.info.InfoItem;

//-----------------------------------------------------------------------------

/**
 * Specifies the location of this game within the Ludii classification scheme.
 * 
 * @author cambolbro
 * 
 * @remarks The Ludii classification is a combination of the schemes used in 
 *          H. J. R. Murray's {\it A History of Board Games other than 
 *          Chess} and David Parlett's {\it The Oxford History of Board Games}, 
 *          with additional categories to reflect the wider range of games
 *          supported by Ludii.
 */
public class Classification implements InfoItem
{
	private final String classification;

	//-------------------------------------------------------------------------
	
	/**
	 * @param classification The game's location within the Ludii classification scheme.
	 * 
	 * @example (classification "games/board/war/chess")
	 */
	public Classification(final String classification)
	{
		this.classification = classification;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		
		sb.append("    (classification \"" + classification + "\")\n");
		
		return sb.toString();
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return The classification.
	 */
	public String classification()
	{
		return classification;
	}
	
	//-------------------------------------------------------------------------

}
