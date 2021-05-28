package game.functions.region.sites.random;

import java.util.BitSet;

import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import game.Game;
import game.functions.ints.IntConstant;
import game.functions.ints.IntFunction;
import game.functions.region.BaseRegionFunction;
import game.functions.region.RegionFunction;
import game.functions.region.sites.index.SitesEmpty;
import game.types.board.SiteType;
import game.types.state.GameType;
import game.util.equipment.Region;
import gnu.trove.list.array.TIntArrayList;
import other.concept.Concept;
import other.context.Context;

/**
 * Returns a list of random sites in a region.
 * 
 * @author Eric.Piette
 */
@Hide
public final class SitesRandom extends BaseRegionFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The region. */
	private final RegionFunction region;
	
	/** The number of sites to get randomly from the region. */
	private final IntFunction numSitesFn;

	/**
	 * @param region The region to get.
	 * @param num    The number of sites to return.
	 */
	public SitesRandom
	(
		@Opt       final RegionFunction region,
		@Opt @Name final IntFunction    num
    )
	{
		this.region = (region == null) ? SitesEmpty.construct(SiteType.Cell, new IntConstant(0)) :region;
		this.numSitesFn = (num == null) ? new IntConstant(1) : num;
	}

	//-------------------------------------------------------------------------

	@Override
	public Region eval(final Context context)
	{
		final TIntArrayList sites = new TIntArrayList();
		final int[] regionSites = region.eval(context).sites();
		int numSites = numSitesFn.eval(context);

		if (numSites > regionSites.length)
			numSites = regionSites.length;
		
		while (sites.size() != numSites)
		{
			if (regionSites.length == 0)
				break;
			final int site = regionSites[context.rng().nextInt(regionSites.length)];
			if (!sites.contains(site))
				sites.add(site);
		}
		
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
		return "SitesRandom()";
	}

	@Override
	public long gameFlags(final Game game)
	{
		final long flags = region.gameFlags(game) | numSitesFn.gameFlags(game) | GameType.Stochastic;

		return flags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(region.concepts(game));
		concepts.or(numSitesFn.concepts(game));
		concepts.set(Concept.Stochastic.id(), true);
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(region.writesEvalContextRecursive());
		writeEvalContext.or(numSitesFn.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(region.readsEvalContextRecursive());
		readEvalContext.or(numSitesFn.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= region.missingRequirement(game);
		missingRequirement |= numSitesFn.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= region.willCrash(game);
		willCrash |= numSitesFn.willCrash(game);
		return willCrash;
	}

	@Override
	public void preprocess(final Game game)
	{
		region.preprocess(game);
		numSitesFn.preprocess(game);
	}
}
