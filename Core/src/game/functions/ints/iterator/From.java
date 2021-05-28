package game.functions.ints.iterator;

import java.util.BitSet;

import annotations.Name;
import annotations.Opt;
import game.Game;
import game.functions.ints.BaseIntFunction;
import game.types.play.WhenType;
import other.context.Context;
import other.context.EvalContextData;

/**
 * Returns the ``from'' value of the context.
 * 
 * @author mrraow and cambolbro
 * 
 * @remarks This ludeme identifies the current position of a specified component. 
 *          It is used for the component's move generator and for all the decision moves.
 */
public final class From extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	final WhenType at;
	
	/**
	 * @param at To return the ``from'' location at a specific time within the game.
	 * @example (from)
	 */
	public From
	(
		@Opt @Name WhenType at
	)
	{
		this.at = at;
	}

	@Override
	public int eval(final Context context)
	{
		if (at == WhenType.StartOfTurn)
			return context.fromStartOfTurn();

		return context.from();
	}

	//-------------------------------------------------------------------------

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
		readEvalContext.set(EvalContextData.From.id(), true);
		return readEvalContext;
	}

	@Override
	public boolean isStatic()
	{
		return false;
	}

	@Override
	public long gameFlags(final Game game)
	{
		return 0L;
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
		return "From()";
	}
}
