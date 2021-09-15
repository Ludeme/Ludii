package game.functions.booleans.is.simple;

import java.util.BitSet;

import annotations.Hide;
import game.Game;
import game.functions.booleans.BaseBooleanFunction;
import other.context.Context;

/**
 * Returns true if the game is repeating the same set of states three times with
 * exactly the same moves during these states.
 * 
 * @author Eric.Piette
 */
@Hide
public final class IsCycle extends BaseBooleanFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * 
	 */
	public IsCycle()
	{
		// Nothing to do.
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean eval(final Context context)
	{
		return false;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		return "AllPass()";
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
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{
		return "we have repeated the same state three times";
	}
	
	//-------------------------------------------------------------------------
	
}
