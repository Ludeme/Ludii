package game.rules.play.moves.nonDecision.operators.foreach.group;

import java.util.BitSet;
import java.util.List;

import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import game.Game;
import game.functions.booleans.BooleanFunction;
import game.functions.directions.Directions;
import game.functions.directions.DirectionsFunction;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import game.rules.play.moves.nonDecision.effect.Effect;
import game.rules.play.moves.nonDecision.effect.Then;
import game.types.board.SiteType;
import game.util.directions.AbsoluteDirection;
import game.util.directions.Direction;
import game.util.equipment.Region;
import gnu.trove.list.array.TIntArrayList;
import main.Constants;
import other.concept.Concept;
import other.context.Context;
import other.context.EvalContextData;
import other.move.Move;
import other.move.MoveUtilities;
import other.state.container.ContainerState;
import other.topology.Topology;
import other.topology.TopologyElement;

/**
 * Applies a move for each group.
 * 
 * @author Eric.Piette
 */
@Hide
public final class ForEachGroup extends Effect
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The condition */
	private final BooleanFunction condition;
	
	/** Direction chosen. */
	private final DirectionsFunction dirnChoice;

	/** Moves to apply from each direction respecting the direction */
	private final Moves movesToApply;

	/** The graph element type. */
	private SiteType type;

	//-------------------------------------------------------------------------
	
	/**
	 * @param type        The type of the graph elements of the group.
	 * @param directions  The directions of the connection between elements in the
	 *                    group [Adjacent].
	 * @param If          The condition on the pieces to include in the group.
	 * @param moves       The moves to apply.
	 * @param then        The moves applied after that move is applied.
	 */
	public ForEachGroup
	(
			@Opt	   final SiteType type,
			@Opt       final Direction    directions,
			@Opt @Name final BooleanFunction If,
	    	           final Moves moves,
			@Opt 	   final Then then
	)
	{ 
		super(then);

		movesToApply = moves;
		this.type = type;
		condition = If;
		dirnChoice = (directions != null) ? directions.directionsFunctions()
				: new Directions(AbsoluteDirection.Adjacent, null);
	} 

	//-------------------------------------------------------------------------

	@Override
	public Moves eval(final Context context)
	{
		final Moves moves = new BaseMoves(super.then());
		final Topology topology = context.topology();
		final int maxIndexElement = context.topology().getGraphElements(type).size();
		final ContainerState cs = context.containerState(0);
		final int origFrom = context.from();
		final int origTo = context.to();
		final Region origRegion = context.region();
		final int who = context.state().mover();

		// We get the minimum set of sites to look.
		final TIntArrayList sitesToCheck = new TIntArrayList();
		if (condition != null)
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
			if ((who == cs.who(from, type) && condition == null) || (condition != null && condition.eval(context)))
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
						}
					}

					sitesExplored.add(site);
					i++;
				}

				context.setRegion(new Region(groupSites.toArray()));
				final Moves movesApplied = movesToApply.eval(context);

				for (final Move m : movesApplied.moves())
				{
					final int saveFrom = context.from();
					final int saveTo = context.to();
					context.setFrom(Constants.OFF);
					context.setTo(Constants.OFF);
					MoveUtilities.chainRuleCrossProduct(context, moves, null, m, false);
					context.setTo(saveTo);
					context.setFrom(saveFrom);
				}

				sitesChecked.addAll(groupSites);
			}
		}

		context.setTo(origTo);
		context.setFrom(origFrom);
		context.setRegion(origRegion);

		if (then() != null)
			for (int j = 0; j < moves.moves().size(); j++)
				moves.moves().get(j).then().add(then().moves());

		return moves;
	}

	//-------------------------------------------------------------------------

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = super.gameFlags(game);

		gameFlags |= SiteType.gameFlags(type);

		if (then() != null)
			gameFlags |= then().gameFlags(game);

		gameFlags |= movesToApply.gameFlags(game);

		if (condition != null)
			gameFlags |= condition.gameFlags(game);

		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(SiteType.concepts(type));
		concepts.set(Concept.Group.id(), true);

		if (then() != null)
			concepts.or(then().concepts(game));

		concepts.or(movesToApply.concepts(game));

		if (condition != null)
			concepts.or(condition.concepts(game));

		if (dirnChoice != null)
			concepts.or(dirnChoice.concepts(game));

		concepts.set(Concept.ControlFlowStatement.id(), true);

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = writesEvalContextFlat();
		if (then() != null)
			writeEvalContext.or(then().writesEvalContextRecursive());

		writeEvalContext.or(movesToApply.writesEvalContextRecursive());

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
		writeEvalContext.set(EvalContextData.Region.id(), true);
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		if (then() != null)
			readEvalContext.or(then().readsEvalContextRecursive());

		readEvalContext.or(movesToApply.readsEvalContextRecursive());

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

		if (then() != null)
			missingRequirement |= then().missingRequirement(game);

		missingRequirement |= movesToApply.missingRequirement(game);

		if (condition != null)
			missingRequirement |= condition.missingRequirement(game);
		
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		if (then() != null)
			willCrash |= then().willCrash(game);

		willCrash |= movesToApply.willCrash(game);

		if (condition != null)
			willCrash |= condition.willCrash(game);
		
		return willCrash;
	}

	@Override
	public boolean isStatic()
	{
		return false;
	}

	@Override
	public void preprocess(final Game game)
	{
		super.preprocess(game);
		movesToApply.preprocess(game);
		type = SiteType.use(type, game);

		if (condition != null)
			condition.preprocess(game);
	}
	
	//------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{		
		String thenString = "";
		if (then() != null)
			thenString = " then " + then().toEnglish(game);
		
		return "for all groups on a " + type.name() + " if " + condition + " (" + dirnChoice.toEnglish(game) + ") " + movesToApply.toEnglish(game) + thenString;
	}
	
	//--------------------------------------------------------------------------

}
