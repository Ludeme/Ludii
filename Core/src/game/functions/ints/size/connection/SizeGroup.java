package game.functions.ints.size.connection;

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
import game.types.board.SiteType;
import game.util.directions.AbsoluteDirection;
import game.util.directions.Direction;
import gnu.trove.list.array.TIntArrayList;
import other.context.Context;
import other.state.container.ContainerState;
import other.topology.Topology;
import other.topology.TopologyElement;

/**
 * Returns the size of the group from a site.
 *
 * @author Eric.Piette
 */
@Hide
public final class SizeGroup extends BaseIntFunction
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

	//-------------------------------------------------------------------------

	/**
	 * @param type       The type of the graph elements of the group.
	 * @param at         The specific starting position needs to connect.
	 * @param directions The directions of the connection between elements in the
	 *                   group [Adjacent].
	 * @param If         The condition on the pieces to include in the group.
	 */
	public SizeGroup
	(
			@Opt	   final SiteType        type,
				 @Name final IntFunction     at,
			@Opt       final Direction       directions,
			@Opt @Name final BooleanFunction If
	)
	{
		startLocationFn = at;
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

		final TIntArrayList groupSites = new TIntArrayList();

		context.setTo(from);

		if (condition == null || condition.eval(context))
				groupSites.add(from);

		final int what = cs.what(from, type);

		if (groupSites.size() > 0)
		{
			context.setFrom(from);
			final TIntArrayList sitesExplored = new TIntArrayList();

			int i = 0;
			while (sitesExplored.size() != groupSites.size())
			{
				final int site = groupSites.get(i);
				final TopologyElement siteElement = topology.getGraphElements(type).get(site);
				final List<AbsoluteDirection> directions = dirnChoice.convertToAbsolute(type, siteElement, null, null,
						null,
						context);

				for (final AbsoluteDirection direction : directions)
				{
					final List<game.util.graph.Step> steps = topology.trajectories().steps(type, siteElement.index(),
							type,
							direction);

					for (final game.util.graph.Step step : steps)
					{
						final int to = step.to().id();

						// If we already have it we continue to look the others.
						if (groupSites.contains(to))
							continue;
						
						context.setTo(to);
						if ((condition == null && what == cs.what(to, type)
								|| (condition != null && condition.eval(context))))
						{
							groupSites.add(to);
						}
					}
				}

				sitesExplored.add(site);
				i++;
			}
		}

		context.setTo(origTo);
		context.setFrom(origFrom);

		return groupSites.size();
	}
	
	//--------------------------------------------------------------------------	
	
	@Override
	public boolean isStatic()
	{
		return false;
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
		concepts.or(SiteType.concepts(type));
		concepts.or(startLocationFn.concepts(game));
		if (condition != null)
			concepts.or(condition.concepts(game));

		if (dirnChoice != null)
			concepts.or(dirnChoice.concepts(game));

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(startLocationFn.writesEvalContextRecursive());
		if (condition != null)
			writeEvalContext.or(condition.writesEvalContextRecursive());

		if (dirnChoice != null)
			writeEvalContext.or(dirnChoice.writesEvalContextRecursive());
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
		
		String directionString = "";
		if (dirnChoice != null)
			directionString = " in direction " + dirnChoice.toEnglish(game);
		
		String typeString = "";
		if (type != null)
			typeString = " of type " + type.name();
		
		return "the size of the group at " + startLocationFn.toEnglish(game) + directionString + typeString + conditionString;
	}
	
	//-------------------------------------------------------------------------
}
