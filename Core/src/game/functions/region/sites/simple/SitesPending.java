package game.functions.region.sites.simple;

import java.util.BitSet;

import annotations.Hide;
import game.Game;
import game.functions.region.BaseRegionFunction;
import game.types.state.GameType;
import game.util.equipment.Region;
import other.context.Context;

/**
 * Returns all the sites currently in pending from the previous state.
 * 
 * @author Eric.Piette
 */
@Hide
public final class SitesPending extends BaseRegionFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * 
	 */
	public SitesPending()
	{
		// Nothing to do.
	}

	//-------------------------------------------------------------------------

	@Override
	public Region eval(final Context context)
	{
		return new Region(context.state().pendingValues().toArray());
	}
	
	@Override
	public boolean contains(final Context context, final int location)
	{
		return context.state().pendingValues().contains(location);
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
		return GameType.PendingValues;
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
			return "the pending sites";
		}
		
		//-------------------------------------------------------------------------
}
