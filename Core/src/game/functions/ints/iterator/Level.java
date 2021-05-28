package game.functions.ints.iterator;

import java.util.BitSet;

import game.Game;
import game.functions.ints.BaseIntFunction;
import game.types.state.GameType;
import other.context.Context;
import other.context.EvalContextData;

/**
 * Returns the ``level'' value of the context.
 * 
 * @author Eric.Piette and cambolbro
 * 
 * @remarks This ludeme identifies the level of the current position of a component on a
 *          site that is stored in the context. It is used for stacking games and to 
 *          generate the moves of the components and for all decision moves.
 */
public final class Level extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * @example (level)
	 */
	public Level()
	{
		// Nothing to do.
	}

	//-------------------------------------------------------------------------

	@Override
	public int eval(final Context context)
	{
		return context.level();
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		return false;
	}

	@Override
	public long gameFlags(final Game game)
	{
		return GameType.Stacking;
	}

	@Override
	public void preprocess(final Game game)
	{
		// nothing to do
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
		readEvalContext.set(EvalContextData.Level.id(), true);
		return readEvalContext;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		return "Level()";
	}
}
