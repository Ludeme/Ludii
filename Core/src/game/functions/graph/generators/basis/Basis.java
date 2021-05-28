package game.functions.graph.generators.basis;

import game.Game;
import game.functions.graph.BaseGraphFunction;
import game.types.board.SiteType;
import game.util.graph.Graph;
import other.context.Context;

//-----------------------------------------------------------------------------

/**
 * Defines known basis types (i.e. tilings) for board graphs.
 * 
 * @author cambolbro
 */
public abstract class Basis extends BaseGraphFunction
{
	private static final long serialVersionUID = 1L;
		
	//-------------------------------------------------------------------------

	@Override
	public Graph eval(final Context context, final SiteType siteType)
	{
		// Null placeholder to make the grammar recognise Basis
		return null;
	}
	
	//-------------------------------------------------------------------------
	// Ludeme overrides
	
	@Override
	public String toEnglish(final Game game)
	{
		return "<Shape>";
	}
	
	//-------------------------------------------------------------------------
	
}
