package game.functions.ints.value.iterated;

import java.util.BitSet;

import annotations.Hide;
import game.Game;
import game.functions.ints.BaseIntFunction;
import other.context.Context;
import other.context.EvalContextData;

/**
 * Returns the ``value'' value stored in the context.
 * 
 * @author Eric.Piette
 * 
 * @remarks This ludeme is used by {\tt (forEach Value ...)} to iterate over a
 *          set of values.
 */
@Hide
public final class ValueIterated extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * @example (value)
	 */
	public ValueIterated()
	{
		// Nothing to do.
	}

	//-------------------------------------------------------------------------

	@Override
	public int eval(final Context context)
	{
		return context.value();
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
		return readsEvalContextFlat();
	}
	
	@Override
	public BitSet readsEvalContextFlat()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.set(EvalContextData.Value.id(), true);
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		// nothing to do
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		return "value";
	}
	
	@Override
	public String toEnglish(final Game game) 
	{
		return "current value";
	}
}
