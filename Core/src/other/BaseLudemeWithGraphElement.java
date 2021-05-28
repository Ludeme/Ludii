package other;

import game.Game;
import game.types.board.SiteType;

/**
 * Base ludeme class for ludemes with a GraphElementType.
 * 
 * @author cambolbro
 */
public class BaseLudemeWithGraphElement extends BaseLudeme
{
	private SiteType type = null;
	
	//-------------------------------------------------------------------------

	/**
	 * @return The type of the element.
	 */
	public SiteType graphElementType()
	{
		return type;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Set the graph element type.
	 * 
	 * @param preferred The preferred graph type.
	 * @param game      The game.
	 */
	public void setGraphElementType(final SiteType preferred, final Game game)
	{
		if (preferred == null)
			type = game.board().defaultSite();
		else 
			type = preferred;
	}
}
