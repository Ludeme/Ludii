package game.functions.region.foreach.sites;

import java.util.BitSet;

import annotations.Hide;
import annotations.Name;
import game.Game;
import game.functions.booleans.BooleanFunction;
import game.functions.region.BaseRegionFunction;
import game.functions.region.RegionFunction;
import game.util.equipment.Region;
import gnu.trove.list.array.TIntArrayList;
import other.concept.Concept;
import other.context.Context;
import other.context.EvalContextData;

/**
 * Returns the sites satisfying a constraint from a given region.
 * 
 * @author Eric.Piette
 */
@Hide
public final class ForEachSite extends BaseRegionFunction
{
	private static final long serialVersionUID = 1L;

	// -------------------------------------------------------------------------

	/** Region to filter. */
	private final RegionFunction region;

	/** Condition to check. */
	private final BooleanFunction condition;

	//-------------------------------------------------------------------------

	/**
	 * @param region The original region.
	 * @param If     The condition to satisfy.
	 * @example (forEach (sites Occupied by:P1) if:(= (what at:(site)) (id
	 *          "Pawn1")))
	 */
	public ForEachSite
	(
		      final RegionFunction  region, 
		@Name final BooleanFunction If
	)
	{
		this.region = region;
		condition = If;
	}

	//-------------------------------------------------------------------------

	@Override
	public Region eval(final Context context)
	{
		final TIntArrayList originalSites = new TIntArrayList(region.eval(context).sites());
		final TIntArrayList returnSites = new TIntArrayList();
		final int originSiteValue = context.site();
		
		for (int i = 0; i < originalSites.size(); i++)
		{
			final int site = originalSites.getQuick(i);
			context.setSite(site);
			if (condition.eval(context))
				returnSites.add(site);
		}

		context.setSite(originSiteValue);
		return new Region(returnSites.toArray());
	}

	@Override
	public boolean isStatic()
	{
		return condition.isStatic() && region.isStatic();
	}

	@Override
	public long gameFlags(final Game game)
	{
		return condition.gameFlags(game) | region.gameFlags(game);
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(condition.concepts(game));
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
		readEvalContext.or(condition.readsEvalContextRecursive());
		readEvalContext.or(region.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		condition.preprocess(game);
		region.preprocess(game);
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= condition.missingRequirement(game);
		missingRequirement |= region.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= condition.willCrash(game);
		willCrash |= region.willCrash(game);
		return willCrash;
	}
	
	@Override
	public String toEnglish(final Game game) 
	{
		if(condition == null)
			return region.toEnglish(game);
		else
			return condition.toEnglish(game) + " " + region.toEnglish(game);
	}
}
