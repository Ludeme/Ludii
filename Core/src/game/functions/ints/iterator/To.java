package game.functions.ints.iterator;

import java.util.BitSet;

import game.Game;
import game.functions.ints.BaseIntFunction;
import other.context.Context;
import other.context.EvalContextData;

/**
 * Returns the ``to'' value of the context.
 * 
 * @author mrraow and cambolbro
 * 
 * @remarks This ludeme returns the destination location the current component is moving to. 
 *          It is used to generate component moves and for all decision moves.
 */
public final class To extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;
	
	//-------------------------------------------------------------------------

	/** Our singleton instance */
	private static final To INSTANCE = new To();

	//-------------------------------------------------------------------------

	/**
	 * Returns the "to" value of the context.
	 */
	private To()
	{
		// Private constructor; singleton!
	}

	//-------------------------------------------------------------------------

	/**
	 * @return Singleton instance
	 */
	public static To instance()
	{
		return INSTANCE;
	}
	
	/**
	 * @return Returns our singleton instance as a ludeme
	 * @example (to)
	 */
	public static To construct()
	{
		return INSTANCE;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public int eval(final Context context)
	{
		return context.to();
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
		readEvalContext.set(EvalContextData.To.id(), true);
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
		return "To()";
	}
}
