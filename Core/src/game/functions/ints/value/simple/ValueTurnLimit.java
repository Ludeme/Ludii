package game.functions.ints.value.simple;

import java.util.BitSet;

import annotations.Hide;
import game.Game;
import game.functions.ints.BaseIntFunction;
import other.context.Context;

/**
 * Returns the internal move limit of the game.
 * 
 * @author Eric.Piette
 */
@Hide
public final class ValueTurnLimit extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 */
	public ValueTurnLimit()
	{
		// Nothing to do
	}

	//-------------------------------------------------------------------------

	@Override
	public int eval(final Context context)
	{
		return context.game().getMaxTurnLimit();
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
		// Nothing to do
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{
		return "the turn limit";
	}
	
	//-------------------------------------------------------------------------
		
}
