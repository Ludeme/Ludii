package game.functions.ints.count.groups;

import java.util.BitSet;
import java.util.List;

import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import annotations.Or;
import game.Game;
import game.functions.booleans.BooleanFunction;
import game.functions.booleans.is.site.IsOccupied;
import game.functions.directions.Directions;
import game.functions.directions.DirectionsFunction;
import game.functions.ints.BaseIntFunction;
import game.functions.ints.IntConstant;
import game.functions.ints.IntFunction;
import game.functions.ints.iterator.To;
import game.types.board.SiteType;
import game.util.directions.AbsoluteDirection;
import game.util.directions.Direction;
import gnu.trove.list.array.TIntArrayList;
import other.concept.Concept;
import other.context.Context;
import other.context.EvalContextData;
import other.state.container.ContainerState;
import other.topology.Topology;
import other.topology.TopologyElement;

/**
 * Returns the number of groups.
 * 
 * @author Eric.Piette
 */
@Hide
public final class CountGroups extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The graph element type. */
	private SiteType type;

	/** The minimum size of a group. */
	private final IntFunction minFn;

	/** The condition */
	private final BooleanFunction condition;

	/** Direction chosen. */
	private final DirectionsFunction dirnChoice;

	//-------------------------------------------------------------------------

	/**
	 * @param type       The graph element type [default SiteType of the board].
	 * @param directions The directions of the connection between elements in the
	 *                   group [Adjacent].
	 * @param If         The condition on the pieces to include in the group.
	 * @param min        Minimum size of each group [0].
	 */
	public CountGroups
	(
		@Opt 	        final SiteType        type,
		@Opt            final Direction       directions,
		@Opt @Or @Name  final BooleanFunction If,
		@Opt     @Name  final IntFunction     min
	)
	{
		this.type = type;
		this.dirnChoice = (directions != null) ? directions.directionsFunctions()
				: new Directions(AbsoluteDirection.Adjacent, null);
		this.minFn = (min == null) ? new IntConstant(0) : min;
		this.condition = (If != null) ? If : new IsOccupied(type, To.construct());
	}

	//-------------------------------------------------------------------------

	@Override
	public int eval(final Context context)
	{
		final Topology topology = context.topology();
		final List<? extends TopologyElement> sites = context.topology().getGraphElements(type);
		final ContainerState cs = context.containerState(0);
		final int origTo = context.to();
		final int min = minFn.eval(context);

		int count = 0;

		final BitSet sitesChecked = new BitSet(sites.size());
		final TIntArrayList sitesToCheck = new TIntArrayList();

		if (context.game().isDeductionPuzzle())
		{
			for (int site = 0; site < sites.size(); site++)
				if (cs.what(site, type) != 0)
					sitesToCheck.add(site);
		}
		else
		{
			for(TopologyElement element: sites)
			{
				context.setTo(element.index());
				if(condition.eval(context))
					sitesToCheck.add(element.index());
			}
		}

		for (int k = 0; k < sitesToCheck.size(); k++)
		{
			final int from = sitesToCheck.getQuick(k);
			
			if (sitesChecked.get(from))
				continue;
			
			// Good to use both list and BitSet here at the same time for different advantages
			final TIntArrayList groupSites = new TIntArrayList();
			final BitSet groupSitesBS = new BitSet(sites.size());
			
			context.setTo(from);
			if (condition.eval(context))
			{
				groupSites.add(from);
				groupSitesBS.set(from);
			}
			
			if (groupSites.size() > 0)
			{
				int i = 0;
				while (i != groupSites.size())
				{
					final int site = groupSites.getQuick(i);
					final TopologyElement siteElement = topology.getGraphElements(type).get(site);
					final List<AbsoluteDirection> directions = dirnChoice.convertToAbsolute(type, siteElement, null,
							null, null, context);

					for (final AbsoluteDirection direction : directions)
					{
						final List<game.util.graph.Step> steps = topology.trajectories().steps(type,
								siteElement.index(), type, direction);

						for (final game.util.graph.Step step : steps)
						{
							final int to = step.to().id();

							// If we already have it we continue to look the others.
							if (groupSitesBS.get(to))
								continue;

							context.setTo(to);
							if (condition.eval(context))
							{
								groupSites.add(to);
								groupSitesBS.set(to);
							}
						}
					}

					++i;
				}

				if (groupSites.size() >= min)
					count++;

				sitesChecked.or(groupSitesBS);
			}
		}

		context.setTo(origTo);

		return count;
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
		return "Groups()";
	}

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = minFn.gameFlags(game);
		gameFlags |= condition.gameFlags(game);
		gameFlags |= SiteType.gameFlags(type);
		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(SiteType.concepts(type));
		concepts.or(minFn.concepts(game));
		concepts.set(Concept.Group.id(), true);
		concepts.or(condition.concepts(game));
		if (dirnChoice != null)
			concepts.or(dirnChoice.concepts(game));
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = writesEvalContextFlat();
		writeEvalContext.or(minFn.writesEvalContextRecursive());
		writeEvalContext.or(condition.writesEvalContextRecursive());
		if (dirnChoice != null)
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
		readEvalContext.or(minFn.readsEvalContextRecursive());
		readEvalContext.or(condition.readsEvalContextRecursive());
		if (dirnChoice != null)
			readEvalContext.or(dirnChoice.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		type = SiteType.use(type, game);
		minFn.preprocess(game);
		condition.preprocess(game);
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= minFn.missingRequirement(game);
		missingRequirement |= condition.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= minFn.willCrash(game);
		if (condition != null)
			willCrash |= condition.willCrash(game);
		return willCrash;
	}
}
