package metadata.info.database;

import metadata.info.InfoItem;

/**
 * Specifies the (approximate) date that the game was created.
 * 
 * @author Matthew.Stephenson
 * 
 * @remarks Date is specified in the format (YYYY-MM-DD).
 */
public class Date implements InfoItem
{
	
	/** The date the game was created. */
	private final String date;

	//-------------------------------------------------------------------------
	
	/**
	 * @param date The date the game was created.
	 * 
	 * @example (date "2015-10-05")
	 */
	public Date(final String date)
	{
		this.date = date;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		
		sb.append("    (date \"" + date + "\")\n");
		
		return sb.toString();
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return The date the game was created
	 */
	public String date()
	{
		return date;
	}

}
