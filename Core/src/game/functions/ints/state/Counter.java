package game.functions.ints.state;

import java.util.BitSet;

import game.Game;
import game.functions.ints.BaseIntFunction;
import other.concept.Concept;
import other.context.Context;

/**
 * Returns the automatic counter of the game.
 * 
 * @author Eric.Piette
 * @remarks To use a counter automatically incremented at each move done, this
 *          can be set to another value by the move (setCounter).
 */
public final class Counter extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * @example (counter)
	 */
	public Counter()
	{
	}
	
	//-------------------------------------------------------------------------

	@Override
	public int eval(final Context context)
	{
		return context.state().counter();
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
		concepts.set(Concept.InternalCounter.id(), true);
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

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		return "Counter()";
	}
	
	@Override
	public String toEnglish(final Game game)
	{
		return "Counter";
	}
}
