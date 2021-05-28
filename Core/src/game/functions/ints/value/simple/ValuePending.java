package game.functions.ints.value.simple;

import java.util.BitSet;

import annotations.Hide;
import game.Game;
import game.functions.ints.BaseIntFunction;
import game.types.state.GameType;
import other.context.Context;

/**
 * Returns the pending value if the previous state causes the current state to
 * be pending with a specific value.
 * 
 * @author Eric.Piette
 * @remarks To store a temporary value in the state for one move turn. Returns 0
 *          if there are multiple different pending values or no pending value.
 */
@Hide
public final class ValuePending extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 */
	public ValuePending()
	{
		// Nothing to do
	}

	//-------------------------------------------------------------------------

	@Override
	public int eval(final Context context)
	{
		// pendingValues should mathematically be a set, so if it contains
		// more than 1 value we don't know what to return and just return
		// the default of 0 instead
		if (context.state().pendingValues().size() == 1)
			return context.state().pendingValues().iterator().next();
		
		return 0;
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
		return GameType.PendingValues;
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
		// Nothing to do
	}
}
