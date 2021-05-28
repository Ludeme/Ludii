package metadata.info.database;

import metadata.info.InfoItem;

//-----------------------------------------------------------------------------

/**
 * Specifies the publisher of the game.
 * 
 * @author Matthew.Stephenson
 */
public class Publisher implements InfoItem
{
	
	/** Game Publisher. */
	private final String publisher;

	//-------------------------------------------------------------------------
	
	/**
	 * @param publisher	The publisher of the game.
	 * 
	 * @example (publisher "Games Inc.")
	 */
	public Publisher(final String publisher)
	{
		this.publisher = publisher;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		
		sb.append("    (publisher \"" + publisher + "\")\n");
		
		return sb.toString();
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Game publisher.
	 */
	public String publisher()
	{
		return publisher;
	}
	
	//-------------------------------------------------------------------------

}
