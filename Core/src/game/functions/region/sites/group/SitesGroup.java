package game.functions.region.sites.group;

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
import game.functions.ints.IntFunction;
import game.functions.region.BaseRegionFunction;
import game.functions.region.RegionFunction;
import game.types.board.SiteType;
import game.util.directions.AbsoluteDirection;
import game.util.directions.Direction;
import game.util.equipment.Region;
import gnu.trove.list.array.TIntArrayList;
import other.IntArrayFromRegion;
import other.concept.Concept;
import other.context.Context;
import other.context.EvalContextData;
import other.state.container.ContainerState;
import other.topology.Topology;
import other.topology.TopologyElement;

/**
 * Is used to return group items from a specific group.
 *
 * @author Eric.Piette
 */
@Hide
public final class SitesGroup extends BaseRegionFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The starting locations of the groups. */
	private final IntArrayFromRegion startLocationFn;
		
	/** The condition */
	private final BooleanFunction condition;
	
	/** Direction chosen. */
	private final DirectionsFunction dirnChoice;

	/**
	 * @param type       The type of the graph elements of the group.
	 * @param at         The specific starting position of the group.
	 * @param From       The specific starting positions of the groups.
	 * @param directions The directions of the connection between elements in the
	 *                   group [Adjacent].
	 * @param If         The condition on the pieces to include in the group.
	 */
	public SitesGroup
	(
		@Opt	       final SiteType        type,
		@Or  @Name     final IntFunction     at,
	    @Or	 @Name     final RegionFunction  From,
		@Opt           final Direction       directions,
		@Opt @Name     final BooleanFunction If
	)
	{ 
		startLocationFn = new IntArrayFromRegion(at, From);
		this.type = type;
		condition = If;
		dirnChoice = (directions != null) ? directions.directionsFunctions()
				: new Directions(AbsoluteDirection.Adjacent, null);
	}

	//-------------------------------------------------------------------------

	@Override
	public Region eval(final Context context)
	{
		final Topology topology = context.topology();
		final int[] froms = startLocationFn.eval(context);
		final ContainerState cs = context.containerState(0);
		final int origFrom = context.from();
		final int origTo = context.to();

		final TIntArrayList groupsSites = new TIntArrayList();
		
		for(final int from : froms)
		{
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
			
			if(froms.length == 1)
				groupsSites.addAll(groupSites);
			else
			{
				for(int i = 0; i < groupSites.size();i++)
					if(!groupsSites.contains(groupSites.get(i)))
						groupsSites.add(groupSites.get(i));
			}
		}
		
		return new Region(groupsSites.toArray());
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
		concepts.set(Concept.Group.id(), true);
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

	@Override
	public void preprocess(final Game game)
	{
		type = SiteType.use(type, game);
		if (condition != null)
			condition.preprocess(game);
		startLocationFn.preprocess(game);
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
		
		return "the sites of the group at " + startLocationFn.toEnglish(game) + directionString + typeString + conditionString;
	}
	
	//-------------------------------------------------------------------------
		
}
