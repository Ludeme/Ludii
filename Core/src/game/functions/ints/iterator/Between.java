package game.functions.ints.iterator;

import java.util.BitSet;

import game.Game;
import game.functions.ints.BaseIntFunction;
import other.context.Context;
import other.context.EvalContextData;

/**
 * Returns the ``between'' value of the context.
 * 
 * @author Eric.Piette and cambolbro
 * 
 * @remarks This ludeme identifies the location(s) between the current position 
 *          of a component and its destination location of a move. 
 *          It can also represent each site (iteratively) surrounded by other sites 
 *          or inside a loop. 
 *          This ludeme is typically used to test a condition or apply an
 *          effect to each site ``between'' other specified sites.
 */
public final class Between extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Our singleton instance */
	private static final Between INSTANCE = new Between();

	//-------------------------------------------------------------------------

	/**
	 * @example (between)
	 */
	private Between()
	{
		// Private constructor; singleton!
	}

	//-------------------------------------------------------------------------

	/**
	 * @return Singleton instance
	 */
	public static Between instance()
	{
		return INSTANCE;
	}

	/**
	 * @return The between value.
	 * @example (between)
	 */
	public static Between construct()
	{
		return INSTANCE;
	}

	//-------------------------------------------------------------------------

	@Override
	public int eval(final Context context)
	{
		return context.between();
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
		readEvalContext.set(EvalContextData.Between.id(), true);
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
		return 0;
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
		return "Between()";
	}
	
	@Override
	public String toEnglish(final Game game)
	{
		return "between";
	}
}
