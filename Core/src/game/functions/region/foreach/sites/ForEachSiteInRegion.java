package game.functions.region.foreach.sites;

import java.util.BitSet;

import annotations.Hide;
import annotations.Name;
import game.Game;
import game.functions.region.BaseRegionFunction;
import game.functions.region.RegionFunction;
import game.util.equipment.Region;
import gnu.trove.list.array.TIntArrayList;
import other.concept.Concept;
import other.context.Context;
import other.context.EvalContextData;

/**
 * Returns the sites of a region in iterating another region.
 * 
 * @author Eric.Piette
 */
@Hide
public final class ForEachSiteInRegion extends BaseRegionFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Region to iterate. */
	private final RegionFunction ofRegion;

	/** Region to use the iterator. */
	private final RegionFunction region;

	//-------------------------------------------------------------------------

	/**
	 * @param of     The region of sites.
	 * @param region The region to compute with each site of the first region.
	 * @example (forEach (sites Occupied by:P1) if:(= (what at:(site)) (id
	 *          "Pawn1")))
	 */
	public ForEachSiteInRegion
	(
		 @Name final RegionFunction of, 
		       final RegionFunction region
	)
	{
		this.ofRegion = of;
		this.region = region;
	}

	//-------------------------------------------------------------------------

	@Override
	public final Region eval(final Context context)
	{
		final TIntArrayList iteratedSites = new TIntArrayList(ofRegion.eval(context).sites());
		final TIntArrayList returnSites = new TIntArrayList();
		final int originSiteValue = context.site();
		
		for (int i = 0; i < iteratedSites.size(); i++)
		{
			final int iteratedSite = iteratedSites.getQuick(i);
			context.setSite(iteratedSite);
			final TIntArrayList sites = new TIntArrayList(region.eval(context).sites());
			for(int j = 0; j < sites.size() ; j ++)
			{
				final int site = sites.get(j);
				if(!returnSites.contains(site))
					returnSites.add(site);
			}
		}

		context.setSite(originSiteValue);
		return new Region(returnSites.toArray());
	}

	@Override
	public boolean isStatic()
	{
		return ofRegion.isStatic() && region.isStatic();
	}

	@Override
	public long gameFlags(final Game game)
	{
		return ofRegion.gameFlags(game) | region.gameFlags(game);
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(ofRegion.concepts(game));
		concepts.or(region.concepts(game));

		concepts.set(Concept.ControlFlowStatement.id(), true);

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = writesEvalContextFlat();
		writeEvalContext.set(EvalContextData.Site.id(), true);
		return writeEvalContext;
	}
	
	@Override
	public BitSet writesEvalContextFlat()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.set(EvalContextData.Site.id(), true);
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(ofRegion.readsEvalContextRecursive());
		readEvalContext.or(region.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		ofRegion.preprocess(game);
		region.preprocess(game);
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= ofRegion.missingRequirement(game);
		missingRequirement |= region.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= ofRegion.willCrash(game);
		willCrash |= region.willCrash(game);
		return willCrash;
	}
}
