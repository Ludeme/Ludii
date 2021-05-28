package game.rules.play.moves.decision;

import game.rules.play.moves.Moves;
import game.rules.play.moves.nonDecision.effect.Then;
import other.context.Context;

//-----------------------------------------------------------------------------

/**
 * Defines moves that involve a decision by the player.
 * 
 * @author Eric.Piette and cambolbro
 */
public abstract class Decision extends Moves
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * @param then The subsequents of the moves.
	 */
	public Decision(final Then then)
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
