package game.rules.meta;

import game.Game;
import game.rules.Rule;
import other.BaseLudeme;

/**
 * Metarule defined before play that supercedes all other rules.
 * 
 * @author cambolbro
 */
public abstract class MetaRule extends BaseLudeme implements Rule
{
	private static final long serialVersionUID = 1L;
	
	//-------------------------------------------------------------------------
	// Ludeme overrides
	
	@Override
	public String toEnglish(final Game game)
	{
		return "<MetaRule>";
	}
}
