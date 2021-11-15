package game.functions.booleans.all.groups;

import java.util.BitSet;
import java.util.List;

import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import game.Game;
import game.functions.booleans.BaseBooleanFunction;
import game.functions.booleans.BooleanFunction;
import game.functions.directions.Directions;
import game.functions.directions.DirectionsFunction;
import game.types.board.SiteType;
import game.util.directions.AbsoluteDirection;
import game.util.directions.Direction;
import game.util.equipment.Region;
import gnu.trove.list.array.TIntArrayList;
import other.concept.Concept;
import other.context.Context;
import other.context.EvalContextData;
import other.state.container.ContainerState;
import other.topology.Topology;
import other.topology.TopologyElement;

/**
 * Returns true if all the groups of the board verify a condition.
 * 
 * @author Eric.Piette
 */
@Hide
public final class AllGroups extends BaseBooleanFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The condition for each element of the group. */
	private final BooleanFunction groupElementConditionFn;
	
	/** The condition for each group to check. */
	private final BooleanFunction groupCondition;
	
	/** Direction chosen. */
	private final DirectionsFunction dirnChoice;

	/** The graph element type. */
	private SiteType type;
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param type        The type of the graph elements of the group.
	 * @param directions  The directions of the connection between elements in the
	 *                    group [Adjacent].
	 * @param of          The condition on the pieces to include in the group [(= (to) (mover))].
	 * @param If          The condition for each group to verify.
	 */
	public AllGroups
	(
			@Opt	   final SiteType type,
			@Opt       final Direction    directions,
			@Opt @Name final BooleanFunction of,
			     @Name final BooleanFunction If
	)
	{ 
		this.type = type;
		groupCondition = If;
		groupElementConditionFn = of;
		dirnChoice = (directions != null) ? directions.directionsFunctions()
				: new Directions(AbsoluteDirection.Adjacent, null);
	} 

	//-------------------------------------------------------------------------

	@Override
	public boolean eval(final Context context)
	{
		final Topology topology = context.topology();
		final int maxIndexElement = context.topology().getGraphElements(type).size();
		final ContainerState cs = context.containerState(0);
		final int origFrom = context.from();
		final int origTo = context.to();
		final Region origRegion = context.region();
		final int who = context.state().mover();

		// We get the minimum set of sites to look.
		final TIntArrayList sitesToCheck = new TIntArrayList();
		if (groupElementConditionFn != null)
		{
			for (int i = 0; i <= context.game().players().size(); i++)
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

		// We get each group.
		final TIntArrayList sitesChecked = new TIntArrayList();
		for (int k = 0; k < sitesToCheck.size(); k++)
		{
			final int from = sitesToCheck.get(k);

			if (sitesChecked.contains(from))
				continue;

			final TIntArrayList groupSites = new TIntArrayList();

			context.setFrom(from);
			context.setTo(from);
			if ((who == cs.who(from, type) && groupElementConditionFn == null) || (groupElementConditionFn != null && groupElementConditionFn.eval(context)))
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
							if ((groupElementConditionFn == null && who == cs.who(to, type)
									|| (groupElementConditionFn != null && groupElementConditionFn.eval(context))))
								groupSites.add(to);
						}
					}

					sitesExplored.add(site);
					i++;
				}

				context.setRegion(new Region(groupSites.toArray()));
				if(!groupCondition.eval(context))
					return false;

				sitesChecked.addAll(groupSites);
			}
		}

		context.setTo(origTo);
		context.setFrom(origFrom);
		context.setRegion(origRegion);

		return true;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		return "AllGroups()";
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

		if (groupElementConditionFn != null)
			gameFlags |= groupElementConditionFn.gameFlags(game);

		if (groupCondition != null)
			gameFlags |= groupCondition.gameFlags(game);
		
		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(SiteType.concepts(type));
		concepts.set(Concept.Group.id(), true);

		if (groupElementConditionFn != null)
			concepts.or(groupElementConditionFn.concepts(game));

		if (groupCondition != null)
			concepts.or(groupCondition.concepts(game));

		if (dirnChoice != null)
			concepts.or(dirnChoice.concepts(game));

		return concepts;
	}

	@Override
	public BitSet writesEvalContextFlat()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.set(EvalContextData.To.id(), true);
		writeEvalContext.set(EvalContextData.From.id(), true);
		writeEvalContext.set(EvalContextData.Region.id(), true);
		return writeEvalContext;
	}
	
	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		
		if (groupElementConditionFn != null)
			writeEvalContext.or(groupElementConditionFn.writesEvalContextRecursive());
		
		if (groupCondition != null)
			writeEvalContext.or(groupCondition.writesEvalContextRecursive());

		if (dirnChoice != null)
			writeEvalContext.or(dirnChoice.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		
		if (groupElementConditionFn != null)
			readEvalContext.or(groupElementConditionFn.readsEvalContextRecursive());
		
		if (groupCondition != null)
			readEvalContext.or(groupCondition.readsEvalContextRecursive());

		if (dirnChoice != null)
			readEvalContext.or(dirnChoice.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		type = SiteType.use(type, game);

		if (groupElementConditionFn != null)
			groupElementConditionFn.preprocess(game);

		if (groupCondition != null)
			groupCondition.preprocess(game);
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;

		if (groupElementConditionFn != null)
			missingRequirement |= groupElementConditionFn.missingRequirement(game);
		
		if (groupCondition != null)
			missingRequirement |= groupCondition.missingRequirement(game);
		
		return missingRequirement;
	}
	
	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;

		if (groupElementConditionFn != null)
			willCrash |= groupElementConditionFn.willCrash(game);
		
		if (groupCondition != null)
			willCrash |= groupCondition.willCrash(game);
		
		return willCrash;
	}
	
	@Override
	public String toEnglish(final Game game) 
	{
		return "all groups satisfy the condition " + groupCondition.toEnglish(game);
	}
}