package game.functions.ints.count.simple;

import java.util.BitSet;

import annotations.Hide;
import game.Game;
import game.functions.ints.BaseIntFunction;
import other.context.Context;

/**
 * Returns the number of active players.
 * 
 * @author Eric.Piette
 */
@Hide
public final class CountActive extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	// -------------------------------------------------------------------------

	/**
	 * 
	 */
	public CountActive()
	{
		// Nothing to do.
	}

	// -------------------------------------------------------------------------

	@Override
	public int eval(final Context context)
	{
		int count = 0;
		for (int i = 1; i < context.game().players().size(); i++)
			if(context.active(i))
				count++;
		
		return count;
	}

	// -------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		return false;
	}

	@Override
	public String toString()
	{
		return "Active()";
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
		return "the number of active players";
	}
	
	//-------------------------------------------------------------------------
		
}
