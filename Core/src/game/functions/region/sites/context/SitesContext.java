package game.functions.region.sites.context;

import java.util.BitSet;

import annotations.Hide;
import game.Game;
import game.functions.region.BaseRegionFunction;
import game.util.equipment.Region;
import other.context.Context;
import other.context.EvalContextData;

/**
 * Returns the sites iterated in ForEach Moves.
 * 
 * @author Eric Piette
 */
@Hide
public final class SitesContext extends BaseRegionFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * @example (sites)
	 */
	public SitesContext()
	{
	}

	//-------------------------------------------------------------------------

	@Override
	public Region eval(final Context context)
	{
		return context.region();
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
		return readsEvalContextFlat();
	}
	
	@Override
	public BitSet readsEvalContextFlat()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.set(EvalContextData.Region.id(), true);
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		// Nothing to do.
	}
}
