package game.functions.booleans.all.simple;

import java.util.BitSet;

import annotations.Hide;
import game.Game;
import game.functions.booleans.BaseBooleanFunction;
import game.types.state.GameType;
import other.context.Context;

/**
 * Returns whether all the players have passed in the previous turns.
 * 
 * @author Eric.Piette
 * 
 * @remarks For any game in which the game can end by all players passing.
 */
@Hide
public final class AllPassed extends BaseBooleanFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * 
	 */
	public AllPassed()
	{
		// Nothing to do.
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game) 
	{
		return "all players has passed in the previous turns";
	}
	
	//-------------------------------------------------------------------------

	@Override
	public boolean eval(final Context context)
	{
		if (context.trial().moveNumber() < context.game().players().count())
			return false;

		return context.allPass();
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
		return GameType.NotAllPass;
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
}
