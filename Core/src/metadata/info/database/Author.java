package metadata.info.database;

import metadata.info.InfoItem;

//-----------------------------------------------------------------------------

/**
 * Specifies the author of the game or ruleset.
 * 
 * @author Matthew.Stephenson
 */
public class Author implements InfoItem
{
	
	/** Game author. */
	private final String author;

	//-------------------------------------------------------------------------
	
	/**
	 * @param author	The author of the game.
	 * 
	 * @example (author "John Doe")
	 */
	public Author(final String author)
	{
		this.author = author;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		
		sb.append("    (author \"" + author + "\")\n");
		
		return sb.toString();
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Game author.
	 */
	public String author()
	{
		return author;
	}

	//-------------------------------------------------------------------------

}
