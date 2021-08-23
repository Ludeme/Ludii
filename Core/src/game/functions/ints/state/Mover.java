package game.functions.ints.state;

import java.util.BitSet;

import game.Game;
import game.functions.ints.BaseIntFunction;
import other.context.Context;

/**
 * Returns the index of the current player.
 * 
 * @author mrraow
 * @remarks To apply some specific condition/rules to the current player.
 */
public final class Mover extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * @example (mover)
	 */
	public Mover()
	{
		// Nothing to do.
	}
	
	//-------------------------------------------------------------------------

	@Override
	public final int eval(final Context context)
	{
		return context.state().mover();
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
		return 0;
	}
	
	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		return concepts;
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
		final BitSet readEvalContext = new BitSet();
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		// nothing to do
	}
	
	@Override
	public String toEnglish(final Game game)
	{
		return "current moving player";
	}
}
