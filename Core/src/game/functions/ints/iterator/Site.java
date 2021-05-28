package game.functions.ints.iterator;

import java.util.BitSet;

import game.Game;
import game.functions.ints.BaseIntFunction;
import other.context.Context;
import other.context.EvalContextData;

/**
 * Returns the ``site'' value stored in the context.
 * 
 * @author Eric.Piette
 * 
 * @remarks This ludeme is used by {\tt (forEach Site ...)} to iterate over a
 *          set of sites.
 */
public final class Site extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * @example (site)
	 */
	public Site()
	{
		// Nothing to do.
	}

	//-------------------------------------------------------------------------

	@Override
	public int eval(final Context context)
	{
		return context.site();
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		return false;
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
		readEvalContext.set(EvalContextData.Site.id(), true);
		return readEvalContext;
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
		return "site";
	}
}
