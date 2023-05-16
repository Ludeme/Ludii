package game.functions.region.sites.between;

import java.util.BitSet;
import java.util.List;

import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import game.Game;
import game.functions.booleans.BooleanConstant;
import game.functions.booleans.BooleanFunction;
import game.functions.directions.Directions;
import game.functions.directions.DirectionsFunction;
import game.functions.ints.IntFunction;
import game.functions.region.BaseRegionFunction;
import game.types.board.SiteType;
import game.util.directions.AbsoluteDirection;
import game.util.equipment.Region;
import game.util.graph.Radial;
import gnu.trove.list.array.TIntArrayList;
import main.Constants;
import other.context.Context;
import other.context.EvalContextData;
import other.topology.Topology;
import other.topology.TopologyElement;

/**
 * For getting the sites (in the same radial) between two others sites.
 * 
 * @author Eric Piette
 */
@Hide
public final class SitesBetween extends BaseRegionFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The 'from' site. */
	private final IntFunction fromFn;

	/** True if the 'from' site is included in the result. */
	private final BooleanFunction fromIncludedFn;

	/** The 'to' site. */
	private final IntFunction toFn;

	/** True if the 'to' site is included in the result. */
	private final BooleanFunction toIncludedFn;
	
	/** The condition to include a site in between */
	private final BooleanFunction betweenCond;
	
	/** Direction chosen. */
	private final DirectionsFunction dirnChoice;
	
	/**
	 * @param directions The directions of the move [Adjacent].
	 * @param type         The type of the graph element [Default SiteType].
	 * @param from         The 'from' site.
	 * @param fromIncluded True if the 'from' site is included in the result
	 *                     [False].
	 * @param to           The 'to' site.
	 * @param toIncluded   True if the 'to' site is included in the result [False].
	 * @param cond         The condition to include the site in between [True].
	 */
	public SitesBetween
	(
		   @Opt        final game.util.directions.Direction directions,
		   @Opt        final SiteType                       type,
			     @Name final IntFunction                    from,
		   @Opt  @Name final BooleanFunction                fromIncluded,
			     @Name final IntFunction                    to,
		   @Opt  @Name final BooleanFunction                toIncluded,
		   @Opt  @Name final BooleanFunction                cond
	)
	{
		this.fromFn = from;
		this.toFn = to;
		this.fromIncludedFn = (fromIncluded == null) ? new BooleanConstant(false) : fromIncluded;
		this.toIncludedFn = (toIncluded == null) ? new BooleanConstant(false) : toIncluded;
		this.type = type;
		this.betweenCond = (cond == null) ? new BooleanConstant(true) : cond;
		this.dirnChoice = (directions != null) ? directions.directionsFunctions()
				: new Directions(AbsoluteDirection.Adjacent, null);
	}

	//-------------------------------------------------------------------------

	@Override
	public Region eval(final Context context)
	{
		final int from = fromFn.eval(context);

		if (from <= Constants.OFF)
			return new Region();

		final int to = toFn.eval(context);
		
		if (to <= Constants.OFF)
			return new Region();
		
		final Topology topology = context.topology();
		
		final SiteType realType = (type != null) ? type : context.game().board().defaultSite();
		
		if (from >= topology.getGraphElements(realType).size())
			return new Region();
		
		if (to >= topology.getGraphElements(realType).size())
			return new Region();

		final TopologyElement fromV = topology.getGraphElements(realType).get(from);
		
		final int origFrom = context.from();
		final int origTo = context.to();
		final int origBetween = context.between();
		
		final TIntArrayList sites = new TIntArrayList();

		context.setFrom(from);
		context.setTo(to);
		
		if (fromIncludedFn.eval(context))
			sites.add(from);

		if (toIncludedFn.eval(context))
			sites.add(to);

		final List<AbsoluteDirection> directions = dirnChoice.convertToAbsolute(realType, fromV, null, null, null,
				context);
		
		boolean toFound = false;
		for (final AbsoluteDirection direction : directions)
		{
			final List<Radial> radials = topology.trajectories().radials(type, fromV.index(), direction);

			for (final Radial radial : radials)
			{
				context.setBetween(origBetween);
				for (int toIdx = 1; toIdx < radial.steps().length; toIdx++)
				{
					final int site = radial.steps()[toIdx].id();
					if (site == to)
					{
						for (int betweenIdx = toIdx - 1; betweenIdx >= 1; betweenIdx--)
						{
							final int between = radial.steps()[betweenIdx].id();
							context.setBetween(between);
							if (betweenCond.eval(context))
								sites.add(between);
						}
						toFound = true;
						break;
					}
				}

				if (toFound)
					break;
			}

			if (toFound)
				break;
		}

		context.setTo(origTo);
		context.setFrom(origFrom);
		context.setBetween(origBetween);
		
		return new Region(sites.toArray());
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		return fromFn.isStatic() && fromIncludedFn.isStatic() && toFn.isStatic() && toIncludedFn.isStatic() && betweenCond.isStatic();
	}

	@Override
	public long gameFlags(final Game game)
	{
		long flags = 0l;
		flags |= SiteType.gameFlags(type);
		flags |= fromFn.gameFlags(game);
		flags |= toFn.gameFlags(game);
		flags |= fromIncludedFn.gameFlags(game);
		flags |= toIncludedFn.gameFlags(game);
		flags |= betweenCond.gameFlags(game);
		return flags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(SiteType.concepts(type));
		concepts.or(fromFn.concepts(game));
		concepts.or(toFn.concepts(game));
		concepts.or(fromIncludedFn.concepts(game));
		concepts.or(toIncludedFn.concepts(game));
		concepts.or(betweenCond.concepts(game));

		if (dirnChoice != null)
			concepts.or(dirnChoice.concepts(game));
		return concepts;
	}
	

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = writesEvalContextFlat();
		writeEvalContext.or(fromFn.writesEvalContextRecursive());
		writeEvalContext.or(toFn.writesEvalContextRecursive());
		writeEvalContext.or(fromIncludedFn.writesEvalContextRecursive());
		writeEvalContext.or(toIncludedFn.writesEvalContextRecursive());
		writeEvalContext.or(betweenCond.writesEvalContextRecursive());

		if (dirnChoice != null)
			writeEvalContext.or(dirnChoice.writesEvalContextRecursive());
		return writeEvalContext;
	}
	
	@Override
	public BitSet writesEvalContextFlat()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.set(EvalContextData.To.id(), true);
		writeEvalContext.set(EvalContextData.From.id(), true);
		writeEvalContext.set(EvalContextData.Between.id(), true);
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(fromFn.readsEvalContextRecursive());
		readEvalContext.or(toFn.readsEvalContextRecursive());
		readEvalContext.or(fromIncludedFn.readsEvalContextRecursive());
		readEvalContext.or(toIncludedFn.readsEvalContextRecursive());
		readEvalContext.or(betweenCond.readsEvalContextRecursive());

		if (dirnChoice != null)
			readEvalContext.or(dirnChoice.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= fromFn.missingRequirement(game);
		missingRequirement |= toFn.missingRequirement(game);
		missingRequirement |= fromIncludedFn.missingRequirement(game);
		missingRequirement |= toIncludedFn.missingRequirement(game);
		missingRequirement |= betweenCond.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= fromFn.willCrash(game);
		willCrash |= toFn.willCrash(game);
		willCrash |= fromIncludedFn.willCrash(game);
		willCrash |= toIncludedFn.willCrash(game);
		willCrash |= betweenCond.willCrash(game);
		return willCrash;
	}

	@Override
	public void preprocess(final Game game)
	{
		type = SiteType.use(type, game);
		fromFn.preprocess(game);
		toFn.preprocess(game);
		fromIncludedFn.preprocess(game);
		toIncludedFn.preprocess(game);
		betweenCond.preprocess(game);
	}
}
