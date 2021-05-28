package game.functions.booleans.was;

import java.util.BitSet;

import annotations.Hide;
import game.Game;
import game.functions.booleans.BaseBooleanFunction;
import other.context.Context;

/**
 * Checks if the last move was a pass move.
 * 
 * @author Eric.Piette
 */
@Hide
public final class WasPass extends BaseBooleanFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * 
	 */
	public WasPass()
	{
		// Nothing to do.
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean eval(final Context context)
	{
		return context.trial().lastMove().isPass();
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
		return 0l;
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
		// Nothing to do.
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		final String str = "(WasPass)";
		return str;
	}
}
