package game.rules.play.moves.nonDecision;

import game.rules.play.moves.Moves;
import game.rules.play.moves.nonDecision.effect.Then;
import other.context.Context;

/**
 * Defines moves that do not involve an immediate decision by the player.
 * 
 * @author Eric.Piette and cambolbro
 * 
 * @remarks Non-decision moves include move operators that might combine
 *          further decision moves.
 */
public abstract class NonDecision extends Moves
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * @param then The subsequents of the moves.
	 */
	public NonDecision(final Then then)
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
