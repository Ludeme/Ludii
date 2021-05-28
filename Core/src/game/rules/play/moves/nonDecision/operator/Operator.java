package game.rules.play.moves.nonDecision.operator;

import game.rules.play.moves.Moves;
import game.rules.play.moves.nonDecision.NonDecision;
import game.rules.play.moves.nonDecision.effect.Then;
import other.context.Context;

/**
 * Defines operations that combine lists of moves, then optionally perform some additional effects.
 * 
 * @author Eric.Piette and cambolbro
 * 
 * The input moves can be decision moves or effect moves (or both).
 */
public abstract class Operator extends NonDecision
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * @param then The effect of the moves.
	 */
	public Operator
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
