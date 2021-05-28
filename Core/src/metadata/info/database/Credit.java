package metadata.info.database;

import metadata.info.InfoItem;

//-----------------------------------------------------------------------------

/**
 * Specifies the author of the .lud file and any relevant credit information.
 * 
 * @author cambolbro
 * 
 * @remarks The is *not* for the author of the game or ruleset.
 *          The "author" info item should be used for that.
 */
public class Credit implements InfoItem
{
	/** .lud author, date, publication details, etc. */
	private final String credit;

	//-------------------------------------------------------------------------
	
	/**
	 * @param credit The author of the .lud file.
	 * 
	 * @example (credit "A. Fool, April Fool Games, 1/4/2020")
	 */
	public Credit(final String credit)
	{
		this.credit = credit;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		
		sb.append("    (credit \"" + credit + "\")\n");
		
		return sb.toString();
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return .lud author.
	 */
	public String credit()
	{
		return credit;
	}

	//-------------------------------------------------------------------------

}
