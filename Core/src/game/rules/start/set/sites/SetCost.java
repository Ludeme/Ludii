package game.rules.start.set.sites;

import java.util.BitSet;

import annotations.Hide;
import annotations.Opt;
import annotations.Or;
import game.Game;
import game.functions.ints.IntFunction;
import game.functions.region.RegionFunction;
import game.rules.start.StartRule;
import game.types.board.SiteType;
import main.StringRoutines;
import other.IntArrayFromRegion;
import other.action.graph.ActionSetCost;
import other.concept.Concept;
import other.context.Context;
import other.move.Move;

/**
 * Sets the cost of graph element(s).
 * 
 * @author Eric.Piette
 */
@Hide
public final class SetCost extends StartRule
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The region to set the weight. */
	private final IntArrayFromRegion region;

	/** The cost. */
	private final IntFunction costFn;

	/** The type of the graph element. */
	private SiteType type;

	//-------------------------------------------------------------------------

	/**
	 * @param type   The type of the graph element.
	 * @param site   The site to set.
	 * @param region The region to set.
	 * @param cost   The new cost.
	 */
	public SetCost
	(
			     final IntFunction    cost,
		    @Opt final SiteType       type,
		@Or      final IntFunction    site,
		@Or      final RegionFunction region
	)
	{
		this.type = type;
		this.region = new IntArrayFromRegion(site, region);
		costFn = cost;
	}

	//-------------------------------------------------------------------------

	@Override
	public void eval(final Context context)
	{
		final int[] locs = region.eval(context);
		for (final int loc : locs)
		{
			final ActionSetCost actionSetCost = new ActionSetCost(type, loc, costFn.eval(context));
			actionSetCost.apply(context, true);
			context.trial().addMove(new Move(actionSetCost));
			context.trial().addInitPlacement();
		}
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
		long gameFlags = 0L;

		gameFlags |= SiteType.gameFlags(type);
		gameFlags |= costFn.gameFlags(game);

		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		if (region != null)
			concepts.or(region.concepts(game));
		concepts.or(SiteType.concepts(type));
		concepts.set(Concept.InitialCost.id(), true);
		concepts.or(costFn.concepts(game));
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		if (region != null)
			writeEvalContext.or(region.writesEvalContextRecursive());
		writeEvalContext.or(costFn.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		if (region != null)
			readEvalContext.or(region.readsEvalContextRecursive());
		readEvalContext.or(costFn.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		type = SiteType.use(type, game);
		costFn.preprocess(game);
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		final String str = "(setCost)";
		return str;
	}
	
	@Override
	public String toEnglish(final Game game)
	{
		String englishString = "set the cost of";
		
		if (type != null)
			englishString += " " + type.name() + StringRoutines.getPlural(type.name());
		else
			englishString += " sites";
		
		if (region != null)
			englishString += " in " + region.toEnglish(game);
		
		englishString = " to " + costFn.toEnglish(game);
		
		return englishString;
	}
}
