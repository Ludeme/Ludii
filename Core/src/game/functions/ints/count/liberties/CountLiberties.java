package game.functions.ints.count.liberties;

import java.util.BitSet;
import java.util.List;

import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import game.Game;
import game.functions.booleans.BooleanFunction;
import game.functions.directions.Directions;
import game.functions.directions.DirectionsFunction;
import game.functions.ints.BaseIntFunction;
import game.functions.ints.IntFunction;
import game.functions.ints.last.LastTo;
import game.types.board.SiteType;
import game.util.directions.AbsoluteDirection;
import game.util.directions.Direction;
import gnu.trove.list.array.TIntArrayList;
import other.context.Context;
import other.context.EvalContextData;
import other.state.container.ContainerState;
import other.topology.Topology;
import other.topology.TopologyElement;

/**
 * Returns the number of liberties of a region.
 * 
 * @author Eric.Piette
 */
@Hide
public final class CountLiberties extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The starting location of the Group. */
	private final IntFunction startLocationFn;

	/** The condition */
	private final BooleanFunction condition;

	/** Direction chosen. */
	private final DirectionsFunction dirnChoice;
	
	/** The graph element type. */
	private SiteType type;

	/**
	 * @param type       The type of the graph elements of the group.
	 * @param at         The specific starting position needs to connect.
	 * @param directions The directions of the connection between elements in the
	 *                   group [Adjacent].
	 * @param If         The condition on the pieces to include in the group.
	 */
	public CountLiberties
	(
		@Opt       final SiteType        type, 
		@Opt @Name final IntFunction     at, 
		@Opt       final Direction       directions,
		@Opt @Name final BooleanFunction If
	)
	{
		startLocationFn = (at != null) ? at : new LastTo(null);
		this.type = type;
		condition = If;
		dirnChoice = (directions != null) ? directions.directionsFunctions()
				: new Directions(AbsoluteDirection.Adjacent, null);
	}

	//-------------------------------------------------------------------------

	@Override
	public int eval(final Context context)
	{
		final Topology topology = context.topology();
		final int from = startLocationFn.eval(context);
		final ContainerState cs = context.containerState(0);
		final int origFrom = context.from();
		final int origTo = context.to();
		
		final List<? extends TopologyElement> graphElements = topology.getGraphElements(type);

		final BitSet groupSites = new BitSet(graphElements.size());
		final TIntArrayList groupSitesList = new TIntArrayList();

		context.setTo(from);

		if (condition == null || condition.eval(context))
		{
			groupSites.set(from);
			groupSitesList.add(from);
		}

		final int what = cs.what(from, type);

		if (groupSitesList.size() > 0)
		{
			context.setFrom(from);

			int i = 0;
			while (i != groupSitesList.size())
			{
				final int site = groupSitesList.get(i);
				final TopologyElement siteElement = graphElements.get(site);
				final List<AbsoluteDirection> directions = dirnChoice.convertToAbsolute(type, siteElement, null, null,
						null, context);

				for (final AbsoluteDirection direction : directions)
				{
					final List<game.util.graph.Step> steps = topology.trajectories().steps(type, site,
							type, direction);

					for (final game.util.graph.Step step : steps)
					{
						final int to = step.to().id();

						// If we already have it we continue to look the others.
						if (groupSites.get(to))
							continue;

						context.setTo(to);
						if (what == cs.what(to, type) && (condition == null
								|| (condition != null && condition.eval(context))))
						{
							groupSites.set(to);
							groupSitesList.add(to);
						}
					}
				}

				i++;
			}
		}

		context.setTo(origTo);
		context.setFrom(origFrom);

		// We get all the empty sites around the group (the liberties).
		final BitSet liberties = new BitSet(graphElements.size());
		for (int indexGroup = 0; indexGroup < groupSitesList.size(); indexGroup++)
		{
			final int siteGroup = groupSitesList.get(indexGroup);
			final TopologyElement element = graphElements.get(siteGroup);
			final List<AbsoluteDirection> directionsElement = dirnChoice
					.convertToAbsolute(type, element,
					null, null, null, context);
			for (final AbsoluteDirection direction : directionsElement)
			{
				final List<game.util.graph.Step> steps = topology.trajectories().steps(type,
						siteGroup, type, direction);

				for (final game.util.graph.Step step : steps)
				{
					final int to = step.to().id();
					if (!groupSites.get(to) && cs.what(to, type) == 0)
						liberties.set(to);
				}
			}
		}
		
		return liberties.cardinality();
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public boolean exceeds(final Context context, final IntFunction other)
	{
		final int valToExceed = other.eval(context);
		
		final Topology topology = context.topology();
		final int from = startLocationFn.eval(context);
		final ContainerState cs = context.containerState(0);
		final int origFrom = context.from();
		final int origTo = context.to();
		
		final List<? extends TopologyElement> graphElements = topology.getGraphElements(type);

		final BitSet groupSites = new BitSet(graphElements.size());
		final TIntArrayList groupSitesList = new TIntArrayList();

		context.setTo(from);

		if (condition == null || condition.eval(context))
		{
			groupSites.set(from);
			groupSitesList.add(from);
		}

		final int what = cs.what(from, type);

		if (groupSitesList.size() > 0)
		{
			context.setFrom(from);

			int i = 0;
			while (i != groupSitesList.size())
			{
				final int site = groupSitesList.get(i);
				final TopologyElement siteElement = graphElements.get(site);
				final List<AbsoluteDirection> directions = dirnChoice.convertToAbsolute(type, siteElement, null, null,
						null, context);

				for (final AbsoluteDirection direction : directions)
				{
					final List<game.util.graph.Step> steps = topology.trajectories().steps(type, site,
							type, direction);

					for (final game.util.graph.Step step : steps)
					{
						final int to = step.to().id();

						// If we already have it we continue to look the others.
						if (groupSites.get(to))
							continue;

						context.setTo(to);
						final int whatTo = cs.what(to, type);
						if (what == whatTo)
						{
							if (condition == null || (condition != null && condition.eval(context)))
							{
								groupSites.set(to);
								groupSitesList.add(to);
							}
						}
						else if (whatTo == 0 && valToExceed <= 0)
						{
							// Already found at least one liberty, which is enough for early return
							context.setTo(origTo);
							context.setFrom(origFrom);
							return true;
						}
					}
				}

				i++;
			}
		}

		context.setTo(origTo);
		context.setFrom(origFrom);

		// We get all the empty sites around the group (the liberties).
		final BitSet liberties = new BitSet(graphElements.size());
		for (int indexGroup = 0; indexGroup < groupSitesList.size(); indexGroup++)
		{
			final int siteGroup = groupSitesList.get(indexGroup);
			final TopologyElement element = graphElements.get(siteGroup);
			final List<AbsoluteDirection> directionsElement = dirnChoice
					.convertToAbsolute(type, element,
					null, null, null, context);
			for (final AbsoluteDirection direction : directionsElement)
			{
				final List<game.util.graph.Step> steps = topology.trajectories().steps(type,
						siteGroup, type, direction);

				for (final game.util.graph.Step step : steps)
				{
					final int to = step.to().id();
					if (!groupSites.get(to) && cs.what(to, type) == 0)
					{
						liberties.set(to);
						if (liberties.cardinality() > valToExceed)
							return true;
					}
				}
			}
		}
		
		return false;
	}

//	//-------------------------------------------------------------------------
//
//	/**
//	 * 
//	 * @param position  A cell number.
//	 * @param uf        Object of union-find.
//	 * 
//	 * @return The root of the position.
//	 */
//	private static int find(final int position, final UnionInfoD uf)
//	{
//		final int parentId = uf.getParent(position);
//
//		if (parentId == Constants.UNUSED)
//			return position;
//
//		if (parentId == position)
//			return position;
//		else
//			return find(parentId, uf);
//	}
//
//	//-------------------------------------------------------------------------
//
//	/**
//	 * @param elements List of graph elements.
//	 * @return return List of indices of the given graph elements.
//	 */
//	private static TIntArrayList elementIndices(final List<? extends TopologyElement> elements)
//	{
//		final int verticesListSz = elements.size();
//		final TIntArrayList integerVerticesList = new TIntArrayList(verticesListSz);
//
//		for (int i = 0; i < verticesListSz; i++)
//		{
//			integerVerticesList.add(elements.get(i).index());
//		}
//
//		return integerVerticesList;
//	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		return false;
	}

	@Override
	public String toString()
	{
		return "Liberties()";
	}

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = 0l;

		gameFlags |= SiteType.gameFlags(type);

		gameFlags |= startLocationFn.gameFlags(game);
		if (condition != null)
			gameFlags |= condition.gameFlags(game);

		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(startLocationFn.concepts(game));
		concepts.or(SiteType.concepts(type));

		if (condition != null)
			concepts.or(condition.concepts(game));

		if (dirnChoice != null)
			concepts.or(dirnChoice.concepts(game));

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = writesEvalContextFlat();
		writeEvalContext.or(startLocationFn.writesEvalContextRecursive());
		if (condition != null)
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
		writeEvalContext.set(EvalContextData.From.id(), true);
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(startLocationFn.readsEvalContextRecursive());
		if (condition != null)
			readEvalContext.or(condition.readsEvalContextRecursive());
		if (dirnChoice != null)
			readEvalContext.or(dirnChoice.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		type = SiteType.use(type, game);
		if (condition != null)
			condition.preprocess(game);
		startLocationFn.preprocess(game);
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= startLocationFn.missingRequirement(game);

		if (condition != null)
			missingRequirement |= condition.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= startLocationFn.willCrash(game);

		if (condition != null)
			willCrash |= condition.willCrash(game);
		return willCrash;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{		
		String conditionString = "";
		if (condition != null)
			conditionString = " if " + condition.toEnglish(game);
		
		return "the number of liberties from " + type.name() + " " + startLocationFn.toEnglish(game) + " in the direction " + dirnChoice.toEnglish(game) + conditionString;
	}
	
	//-------------------------------------------------------------------------
		
}
