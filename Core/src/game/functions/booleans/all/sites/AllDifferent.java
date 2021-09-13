package game.functions.booleans.all.sites;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import annotations.Hide;
import annotations.Name;
import game.Game;
import game.functions.booleans.BaseBooleanFunction;
import game.functions.booleans.BooleanFunction;
import game.functions.region.RegionFunction;
import game.types.board.SiteType;
import gnu.trove.list.array.TIntArrayList;
import other.context.Context;
import other.context.EvalContextData;
import other.location.FullLocation;
import other.location.Location;
import other.state.container.ContainerState;

/**
 * Returns true if all the sites are different.
 * 
 * @author Eric.Piette
 */
@Hide
public final class AllDifferent extends BaseBooleanFunction
{
	private static final long serialVersionUID = 1L;

	// -------------------------------------------------------------------------

	/** Region to check. */
	private final RegionFunction region;

	/** Condition to check. */
	private final BooleanFunction condition;

	/**
	 * @param region The region.
	 * @param If     The condition to satisfy.
	 */
	public AllDifferent
	(
			  final RegionFunction  region,
		@Name final BooleanFunction If
	)
	{
		this.region = region;
		condition = If;
	}

	// -------------------------------------------------------------------------

	@Override
	public boolean eval(final Context context)
	{
		final int[] sites = region.eval(context).sites();
		final int originSiteValue = context.site();
		final SiteType type = context.board().defaultSite();
		final ContainerState cs = context.containerState(0);
		final TIntArrayList whats = new TIntArrayList();

		for (final int site : sites)
		{
			context.setSite(site);
			if (!condition.eval(context))
			{
				context.setSite(originSiteValue);
				return false;
			}
			else
			{
				final int what = cs.what(site, type);
				if (whats.contains(what))
				{

					context.setSite(originSiteValue);
					return false;
				}
				else
				{
					whats.add(what);
				}
			}
		}

		context.setSite(originSiteValue);
		return true;
	}

	// -------------------------------------------------------------------------

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
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = writesEvalContextFlat();
		writeEvalContext.or(condition.writesEvalContextRecursive());
		writeEvalContext.or(region.writesEvalContextRecursive());
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
		missingRequirement |= region.missingRequirement(game);
		missingRequirement |= condition.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= region.willCrash(game);
		willCrash |= condition.willCrash(game);
		return willCrash;
	}

	// -------------------------------------------------------------------------

	@Override
	public List<Location> satisfyingSites(final Context context)
	{
		if (!eval(context))
			return new ArrayList<Location>();

		final List<Location> winningSites = new ArrayList<Location>();
		final int[] sites = region.eval(context).sites();

		final SiteType type = context.board().defaultSite();
		for (final int site : sites)
			winningSites.add(new FullLocation(site, 0, type));
		return winningSites;
	}
	
	@Override
	public String toEnglish(final Game game) 
	{
		return "all sites in " + region.toEnglish(game) + " have different results for the condition " + condition.toEnglish(game);
	}
}