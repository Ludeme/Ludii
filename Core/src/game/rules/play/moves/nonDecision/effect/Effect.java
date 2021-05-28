package game.rules.play.moves.nonDecision.effect;

import game.rules.play.moves.Moves;
import game.rules.play.moves.nonDecision.NonDecision;
import other.context.Context;

//-----------------------------------------------------------------------------

/**
 * Defines moves which do not involve a player decision.
 * 
 * @author Eric.Piette and cambolbro
 * 
 * Effect moves are typically applied in response to player decision moves, 
 * e.g. the capture of a piece following the move of another piece.
 */
public abstract class Effect extends NonDecision
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * @param then The effect of the moves.
	 */
	public Effect
	(
		final Then then
	)
	{
		super(then);
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Trick ludeme into joining the grammar.
	 */
	@Override
	public Moves eval(final Context context)
	{
		return null;
	}

	//-------------------------------------------------------------------------

}
