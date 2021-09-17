package game.functions.ints.count.simple;

import java.util.BitSet;

import annotations.Hide;
import game.Game;
import game.functions.ints.BaseIntFunction;
import other.context.Context;

/**
 * Returns the number of turns.
 * 
 * @author Eric.Piette
 */
@Hide
public final class CountTurns extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * 
	 */
	public CountTurns()
	{
	}

	//-------------------------------------------------------------------------

	@Override
	public int eval(final Context context)
	{
		return context.state().numTurn();
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		return false;
	}

	@Override
	public String toString()
	{
		return "Turns()";
	}

	@Override
	public long gameFlags(final Game game)
	{
		return 0L;
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
	public String toEnglish(final Game game) 
	{
		return "the number of turns";
	}
	
	//-------------------------------------------------------------------------
		
}
