package game.functions.region.sites.simple;

import java.util.BitSet;

import annotations.Hide;
import game.Game;
import game.functions.booleans.is.in.IsIn;
import game.functions.ints.IntFunction;
import game.functions.ints.iterator.To;
import game.functions.region.BaseRegionFunction;
import game.functions.region.RegionConstant;
import game.functions.region.RegionFunction;
import game.functions.region.sites.Sites;
import game.functions.region.sites.SitesSimpleType;
import game.functions.region.sites.around.SitesAround;
import game.functions.region.sites.index.SitesEmpty;
import game.types.board.SiteType;
import game.util.equipment.Region;
import gnu.trove.list.array.TIntArrayList;
import other.context.Context;
import other.state.container.ContainerState;

/**
 * Returns the playable sites of any boardless game.
 * 
 * @author Eric.Piette and cambolbro
 * 
 * @remarks For non-boardless games, it returns the site around the current
 *          occupied sites of the board.
 *          Used on any boardless game to play all the pieces in a unique group.
 */
@Hide
public final class SitesPlayable extends BaseRegionFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * 
	 */
	public SitesPlayable()
	{
		// Nothing to do.
	}

	//-------------------------------------------------------------------------

	@Override
	public Region eval(final Context context)
	{
		final TIntArrayList sites = new TIntArrayList();
		
		if(!context.game().isBoardless())
		{
			if (context.trial().moveNumber() == 0)
			{
				return Sites.construct(SitesSimpleType.Centre, null)
						.eval(context);
			}
			else
			{
				final TIntArrayList occupiedSite = new TIntArrayList();
				for (int i = 0; i < context.containers()[0].numSites(); i++)
					if (!context.containerState(0).isEmpty(i, SiteType.Cell))
						occupiedSite.add(i); 
				final RegionFunction occupiedRegion = new RegionConstant(new Region(occupiedSite.toArray()));
				return new SitesAround(null, null,
						occupiedRegion,
						null, null, null, IsIn.construct(null, new IntFunction[]
						{ To.instance() }, SitesEmpty.construct(null, null), null), null).eval(context);
			}
		}

		final ContainerState cs = context.state().containerStates()[0];
		
		final int numSite = context.game().equipment().containers()[0].numSites();
		for (int index = 0; index < numSite; index++)
			if (cs.isPlayable(index))
				sites.add(index);
		
		return new Region(sites.toArray());
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		return false;
	}

	@Override
	public String toString()
	{
		return "Playable()";
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
		// Do nothing
	}
}
