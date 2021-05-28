package game.functions.ints.iterator;

import java.util.BitSet;

import game.Game;
import game.functions.ints.BaseIntFunction;
import other.context.Context;
import other.context.EvalContextData;

/**
 * Returns the ``player'' value of the context.
 * 
 * @author Eric.Piette
 * 
 * @remarks This ludeme corresponds to the index of a player. It is used to
 *          iterate through the players with a (forEach Player ...) ludeme.
 */
public final class Player extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	// -------------------------------------------------------------------------

	/**
	 * @example (player)
	 */
	public Player()
	{
	}

	@Override
	public int eval(final Context context)
	{
		return context.player();
	}

	// -------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		return false;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		return readsEvalContextFlat();
	}
	
	@Override
	public BitSet readsEvalContextFlat()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.set(EvalContextData.Player.id(), true);
		return readEvalContext;
	}

	@Override
	public long gameFlags(final Game game)
	{
		return 0L;
	}

	@Override
	public void preprocess(final Game game)
	{
		// nothing to do
	}
}
