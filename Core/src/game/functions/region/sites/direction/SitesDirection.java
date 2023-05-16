package game.functions.region.sites.direction;

import java.util.BitSet;
import java.util.List;

import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import annotations.Or;
import game.Game;
import game.functions.booleans.BooleanConstant;
import game.functions.booleans.BooleanFunction;
import game.functions.directions.Directions;
import game.functions.directions.DirectionsFunction;
import game.functions.ints.IntConstant;
import game.functions.ints.IntFunction;
import game.functions.region.BaseRegionFunction;
import game.functions.region.RegionFunction;
import game.types.board.SiteType;
import game.util.directions.AbsoluteDirection;
import game.util.equipment.Region;
import game.util.graph.GraphElement;
import game.util.graph.Radial;
import gnu.trove.list.array.TIntArrayList;
import main.Constants;
import other.ContainerId;
import other.IntArrayFromRegion;
import other.context.Context;
import other.context.EvalContextData;
import other.topology.TopologyElement;

/**
 * All the sites in a direction from a site.
 * 
 * @author Eric.Piette
 */
@Hide
public final class SitesDirection extends BaseRegionFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------
	
	/** Which site. */
	private final IntArrayFromRegion regionFn;

	/** Site included or not. */
	private final BooleanFunction included;

	/** Distance. */
	private final IntFunction distanceFn;

	/** Direction chosen. */
	private final DirectionsFunction dirnChoice;

	/** Condition to stop on this direction. */
	private final BooleanFunction stopRule;

	/** To include or not the site stopping each direction. */
	private final BooleanFunction stopIncludedRule;

	//-------------------------------------------------------------------------

	/**
	 * @param from         The origin location.
	 * @param From         The origin region location.
	 * @param directions   The directions of the move [Adjacent].
	 * @param included     True if the origin is included in the result [False].
	 * @param stop         When the condition is true in one specific direction,
	 *                     sites are no longer added to the result [False].
	 * @param stopIncluded True if the site stopping the radial in each direction is
	 *                     included in the result [False].
	 * @param distance     The distance around which to check [Infinite].
	 * @param type         The graph element type [default SiteType of the board].
	 */
	public SitesDirection
	(
			  @Or  @Name  final IntFunction                    from,
			  @Or  @Name  final RegionFunction                 From,
		@Opt              final game.util.directions.Direction directions,
		@Opt @Name        final BooleanFunction                included,
		@Opt @Name        final BooleanFunction                stop, 
		@Opt @Name        final BooleanFunction                stopIncluded, 
		@Opt @Name        final IntFunction                    distance,
		@Opt 	          final SiteType                       type
	)
	{

		regionFn = new IntArrayFromRegion(from, From);

		// Directions
		dirnChoice = (directions != null) 
						  ? directions.directionsFunctions()
						  : new Directions(AbsoluteDirection.Adjacent, null);

		this.included = (included == null) ? new BooleanConstant(false) : included;
		stopRule = (stop == null) ? new BooleanConstant(false) : stop;
		this.type = type;
		distanceFn = (distance == null) ? new IntConstant(Constants.MAX_DISTANCE) : distance;
		stopIncludedRule = (stopIncluded == null) ? new BooleanConstant(false) : stopIncluded;
	}

	//-------------------------------------------------------------------------

	@Override
	public Region eval(final Context context)
	{
		final int[] region = regionFn.eval(context);
		final TIntArrayList sites = new TIntArrayList();
		final int distance = distanceFn.eval(context);
		final SiteType realType = (type != null) ? type : context.game().board().defaultSite();
		
		for (final int loc : region)
		{
			final int cid = new ContainerId(null, null, null, null, new IntConstant(loc)).eval(context);
			final other.topology.Topology topology = context.containers()[cid].topology();

			if (loc == Constants.UNDEFINED)
				return new Region(sites.toArray());

			final TopologyElement element = topology.getGraphElements(realType).get(loc);
			final int originTo = context.to();

			if (included.eval(context))
				sites.add(loc);

			final List<AbsoluteDirection> directions = dirnChoice.convertToAbsolute(realType, element, null, null, null,
					context);

			for (final AbsoluteDirection direction : directions)
			{
				final List<Radial> radialList = topology.trajectories().radials(realType, loc, direction);

				for (final Radial radial : radialList)
				{
					final GraphElement[] steps = radial.steps();
					final int limit = Math.min(steps.length, distance + 1);
					for (int toIdx = 1; toIdx < limit; toIdx++)
					{
						final int to = steps[toIdx].id();
						context.setTo(to);
						if (stopRule.eval(context))
						{
							if (stopIncludedRule.eval(context))
								sites.add(to);
							break;
						}
						sites.add(to);
					}
				}
			}
			context.setTo(originTo);
		}

		return new Region(sites.toArray());
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		return regionFn.isStatic() && included.isStatic() && stopRule.isStatic() && distanceFn.isStatic()
				&& stopIncludedRule.isStatic() && dirnChoice.isStatic();
	}

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = regionFn.gameFlags(game) | included.gameFlags(game) | stopIncludedRule.gameFlags(game)
				| stopRule.gameFlags(game)
				| distanceFn.gameFlags(game) | dirnChoice.gameFlags(game);

		gameFlags |= SiteType.gameFlags(type);

		return gameFlags;
	}
	
	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(stopIncludedRule.concepts(game));
		concepts.or(regionFn.concepts(game));
		concepts.or(included.concepts(game));
		concepts.or(SiteType.concepts(type));
		concepts.or(stopRule.concepts(game));
		concepts.or(distanceFn.concepts(game));
		concepts.or(dirnChoice.concepts(game));
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = writesEvalContextFlat();
		writeEvalContext.or(stopIncludedRule.writesEvalContextRecursive());
		writeEvalContext.or(regionFn.writesEvalContextRecursive());
		writeEvalContext.or(included.writesEvalContextRecursive());
		writeEvalContext.or(stopRule.writesEvalContextRecursive());
		writeEvalContext.or(distanceFn.writesEvalContextRecursive());
		writeEvalContext.or(dirnChoice.writesEvalContextRecursive());
		return writeEvalContext;
	}
	
	@Override
	public BitSet writesEvalContextFlat()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.set(EvalContextData.To.id(), true);
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(stopIncludedRule.readsEvalContextRecursive());
		readEvalContext.or(regionFn.readsEvalContextRecursive());
		readEvalContext.or(included.readsEvalContextRecursive());
		readEvalContext.or(stopRule.readsEvalContextRecursive());
		readEvalContext.or(distanceFn.readsEvalContextRecursive());
		readEvalContext.or(dirnChoice.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= stopIncludedRule.missingRequirement(game);
		missingRequirement |= regionFn.missingRequirement(game);
		missingRequirement |= included.missingRequirement(game);
		missingRequirement |= stopRule.missingRequirement(game);
		missingRequirement |= distanceFn.missingRequirement(game);
		missingRequirement |= dirnChoice.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= stopIncludedRule.willCrash(game);
		willCrash |= regionFn.willCrash(game);
		willCrash |= included.willCrash(game);
		willCrash |= stopRule.willCrash(game);
		willCrash |= distanceFn.willCrash(game);
		willCrash |= dirnChoice.willCrash(game);
		return willCrash;
	}

	@Override
	public void preprocess(final Game game)
	{
		type = SiteType.use(type, game);

		regionFn.preprocess(game);
		included.preprocess(game);
		stopRule.preprocess(game);
		distanceFn.preprocess(game);
		stopIncludedRule.preprocess(game);
		dirnChoice.preprocess(game);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game) 
	{
		String directionString = "";
		if (dirnChoice != null)
			directionString = " in the direction " + dirnChoice.toEnglish(game);
		
		return "all sites " + distanceFn.toEnglish(game) + " spaces away from " + regionFn.toEnglish(game) + directionString;
	}
	
	//-------------------------------------------------------------------------
		
}
