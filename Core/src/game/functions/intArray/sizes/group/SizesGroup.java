package game.functions.intArray.sizes.group;

import java.util.BitSet;
import java.util.List;

import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import annotations.Or;
import game.Game;
import game.functions.booleans.BooleanFunction;
import game.functions.directions.Directions;
import game.functions.directions.DirectionsFunction;
import game.functions.intArray.BaseIntArrayFunction;
import game.functions.ints.IntConstant;
import game.functions.ints.IntFunction;
import game.functions.ints.board.Id;
import game.types.board.SiteType;
import game.types.play.RoleType;
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
 * Returns an array of the sizes of all the groups.
 * 
 * @author Eric.Piette
 */
@Hide
public final class SizesGroup extends BaseIntArrayFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The graph element type. */
	private SiteType type;

	/** The index of the player. */
	private final IntFunction whoFn;

	/** The minimum size of a group. */
	private final IntFunction minFn;

	/** The condition */
	private final BooleanFunction condition;

	/** Direction chosen. */
	private final DirectionsFunction dirnChoice;

	/** Variable to know if all the pieces have to be check */
	private final boolean allPieces;

	//-------------------------------------------------------------------------

	/**
	 * @param type       The graph element type [default SiteType of the board].
	 * @param directions The directions of the connection between elements in the
	 *                   group [Adjacent].
	 * @param role       The role of the player [All].
	 * @param of         The index of the player.
	 * @param If         The condition on the pieces to include in the group.
	 * @param min        Minimum size of each group [0].
	 */
	public SizesGroup
	(       
			@Opt 	        final SiteType          type,
			@Opt            final Direction         directions,
			@Opt @Or	    final RoleType          role,
			@Opt @Or  @Name final IntFunction       of,
			@Opt @Or  @Name final BooleanFunction   If,
			@Opt      @Name final IntFunction       min
	)
	{

		this.type = type;
		whoFn = (of != null) ? of : (role != null) ? RoleType.toIntFunction(role) : new Id(null, RoleType.All);
		dirnChoice = (directions != null) ? directions.directionsFunctions()
				: new Directions(AbsoluteDirection.Adjacent, null);
		minFn = (min == null) ? new IntConstant(0) : min;
		condition = If;
		allPieces = (If == null && of == null && role == null)
				|| (role != null && (role.equals(RoleType.All) || role.equals(RoleType.Shared)));
	}

	//-------------------------------------------------------------------------

	@Override
	public int[] eval(final Context context)
	{
		final Topology topology = context.topology();
		final int maxIndexElement = context.topology().getGraphElements(type).size();
		final ContainerState cs = context.containerState(0);
		final int origFrom = context.from();
		final int origTo = context.to();
		final int who = whoFn.eval(context);
		final int min = minFn.eval(context);

		final TIntArrayList sizes = new TIntArrayList();

		final TIntArrayList sitesChecked = new TIntArrayList();
		final TIntArrayList sitesToCheck = new TIntArrayList();

		if (allPieces)
		{
			for (int i = 1; i < context.game().players().size(); i++)
			{
				final TIntArrayList allSites = context.state().owned().sites(i);
				for (int j = 0; j < allSites.size(); j++)
				{
					final int site = allSites.get(j);
					if (site < maxIndexElement)
						sitesToCheck.add(site);
				}
			}
		}
		else
		{
			for (int j = 0; j < context.state().owned().sites(who).size(); j++)
			{
				final int site = context.state().owned().sites(who).get(j);
				if (site < maxIndexElement)
					sitesToCheck.add(site);
			}
		}

		for (int k = 0; k < sitesToCheck.size(); k++)
		{
			final int from = sitesToCheck.get(k);

			if (sitesChecked.contains(from))
				continue;

			final TIntArrayList groupSites = new TIntArrayList();

			context.setFrom(from);
			if ((who == cs.who(from, type) && condition == null) || (condition != null && condition.eval(context)))
				groupSites.add(from);
			else if (allPieces && cs.what(from, type) != 0)
				groupSites.add(from);

			if (groupSites.size() > 0)
			{
				context.setFrom(from);
				final TIntArrayList sitesExplored = new TIntArrayList();
				int i = 0;
				while (sitesExplored.size() != groupSites.size())
				{
					final int site = groupSites.get(i);
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
							if (groupSites.contains(to))
								continue;

							context.setTo(to);
							if ((condition == null && who == cs.who(to, type)
									|| (condition != null && condition.eval(context))))
								groupSites.add(to);
							else if (allPieces && cs.what(to, type) != 0)
								groupSites.add(to);
						}
					}

					sitesExplored.add(site);
					i++;
				}

				if (groupSites.size() >= min)
					sizes.add(groupSites.size());

				sitesChecked.addAll(groupSites);
			}
		}

		context.setTo(origTo);
		context.setFrom(origFrom);

		return sizes.toArray();
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
		long gameFlags = whoFn.gameFlags(game) | minFn.gameFlags(game);
		if (condition != null)
			gameFlags |= condition.gameFlags(game);
		gameFlags |= SiteType.gameFlags(type);
		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(whoFn.concepts(game));
		concepts.or(minFn.concepts(game));
		concepts.or(SiteType.concepts(type));
		concepts.set(Concept.Group.id(), true);
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
		writeEvalContext.or(whoFn.writesEvalContextRecursive());
		writeEvalContext.or(minFn.writesEvalContextRecursive());
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
		readEvalContext.or(whoFn.readsEvalContextRecursive());
		readEvalContext.or(minFn.readsEvalContextRecursive());
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
		whoFn.preprocess(game);
		minFn.preprocess(game);
		if (condition != null)
			condition.preprocess(game);
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= whoFn.missingRequirement(game);
		missingRequirement |= minFn.missingRequirement(game);
		if (condition != null)
			missingRequirement |= condition.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= whoFn.willCrash(game);
		willCrash |= minFn.willCrash(game);
		if (condition != null)
			willCrash |= condition.willCrash(game);
		return willCrash;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{
		return "the sizes of all groups";
	}
	
	//-------------------------------------------------------------------------
	
}
