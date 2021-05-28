package game.rules.play.moves.nonDecision.operators.foreach.direction;

import java.util.BitSet;
import java.util.List;

import annotations.Hide;
import annotations.Opt;
import annotations.Or;
import game.Game;
import game.equipment.component.Component;
import game.functions.booleans.BooleanFunction;
import game.functions.directions.Directions;
import game.functions.directions.DirectionsFunction;
import game.functions.ints.IntConstant;
import game.functions.ints.IntFunction;
import game.functions.ints.iterator.From;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import game.rules.play.moves.nonDecision.effect.Effect;
import game.rules.play.moves.nonDecision.effect.Then;
import game.types.board.SiteType;
import game.util.directions.AbsoluteDirection;
import game.util.directions.DirectionFacing;
import game.util.graph.Radial;
import game.util.graph.Step;
import game.util.moves.To;
import main.Constants;
import other.concept.Concept;
import other.context.Context;
import other.context.EvalContextData;
import other.move.Move;
import other.move.MoveUtilities;
import other.topology.Topology;
import other.topology.TopologyElement;

/**
 * Applies a move for each site reached according to a direction.
 * 
 * @author Eric.Piette
 * 
 * @remarks In case of different directions and conditions to follow before
 *          applying a move (e.g. Xiangqi).
 */
@Hide
public final class ForEachDirection extends Effect
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Location of the piece. */
	private final IntFunction startLocationFn;

	/** Min limit of the move. */
	private final IntFunction min;

	/** Limit to apply the moves. */
	private final IntFunction limit;

	/** Direction chosen. */
	private final DirectionsFunction dirnChoice;

	/** The rule to respect on the location to go. */
	private final BooleanFunction rule;
	
	/** The rule to respect on the sites to cross. */
	private final BooleanFunction betweenRule;

	/** Moves to apply from each direction respecting the direction */
	private final Moves movesToApply;

	/** Cell/Edge/Vertex. */
	private SiteType type;

	//-------------------------------------------------------------------------
	
	/**
	 * @param from       Description of the ``from'' location [(from)].
	 * @param directions The directions of the move [Adjacent].
	 * @param between    Description of location(s) between ``from'' and ``to''
	 *                   [(between (exact 1))].
	 * @param to         Description of the ``to'' location.
	 * @param moves      Description of the decision moves to apply.
	 * @param then       The moves applied after that move is applied.
	 */
	public ForEachDirection
	(
			@Opt final game.util.moves.From           from,
			@Opt final game.util.directions.Direction directions,
			@Opt final game.util.moves.Between        between,
		@Or      final To 	                          to,
		@Or      final Moves                          moves,
			@Opt final Then                           then
	)
	{ 
		super(then);
		// From
		this.startLocationFn = (from == null) ? new From(null) : from.loc();
		this.type = (from == null) ? null : from.type();

		// Directions
		this.dirnChoice = (directions != null) ? directions.directionsFunctions()
				: new Directions(AbsoluteDirection.Adjacent, null);

		// Between
		this.limit = (between == null || between.range() == null) ? new IntConstant(1) : between.range().maxFn();
		this.min = (between == null || between.range() == null) ? new IntConstant(1) : between.range().minFn();
		this.betweenRule = (between != null) ? between.condition() : null;

		// To
		this.rule = (to != null) ? to.cond() : null;
		this.movesToApply = (to != null && to.effect() != null) ? to.effect().effect() : moves;
	} 

	//-------------------------------------------------------------------------

	@Override
	public Moves eval(final Context context)
	{
		final Moves moves = new BaseMoves(super.then());

		final int from = startLocationFn.eval(context);

		if (from <= Constants.OFF)
			return moves;

		final Topology graph = context.topology();
		final SiteType realType = (type != null) ? type : context.game().board().defaultSite();

		if (from >= graph.getGraphElements(realType).size())
			return moves;

		final List<DirectionFacing> directionsSupported = graph.supportedDirections(realType);
		final int minPathLength = min.eval(context);
		final int maxPathLength = limit.eval(context);

		final int contextFrom = context.from();
		final int contextTo = context.to();

		final Component component = context.components()
				[context.containerState(context.containerId()[contextFrom]).what(contextFrom, type)];

		if (component == null)
			return moves;
		
		DirectionFacing newDirection = null;

		if (contextTo != Constants.UNDEFINED)
		{
			for (final DirectionFacing direction : directionsSupported)
			{
				final AbsoluteDirection absoluteDirection = direction.toAbsolute();
				final List<game.util.graph.Step> steps = graph.trajectories().steps(realType, context.from(), realType,
						absoluteDirection);
				for (final Step step : steps)
				{
					if (step.to().id() == contextTo)
					{
						newDirection = direction;
						break;
					}
				}
			}
		}

		final int origFrom = context.from();
		final int origBetween = context.between();
		final int origTo = context.to();

		final TopologyElement fromV = graph.getGraphElements(realType).get(from);
		final List<AbsoluteDirection> directions = dirnChoice.convertToAbsolute(realType, fromV, component,
				newDirection, null, context);

		for (final AbsoluteDirection direction : directions)
		{
			final List<Radial> radials = graph.trajectories().radials(type, fromV.index(), direction);

			for (final Radial radial : radials)
			{
				for (int toIdx = 1; toIdx < radial.steps().length && toIdx <= maxPathLength; toIdx++)
				{
					final int to = radial.steps()[toIdx].id();

					// Check the middle rule
					if (betweenRule != null && minPathLength > 1 && toIdx < minPathLength)
					{
						context.setBetween(to);
						if (!betweenRule.eval(context))
							break;
						context.setBetween(origBetween);
					}

					context.setTo(to);
					if (rule == null || rule.eval(context))
					{
						if (toIdx >= minPathLength)
						{
							final Moves movesApplied = movesToApply.eval(context);

							for (final Move m : movesApplied.moves())
							{
								final int saveFrom = context.from();
								final int saveTo = context.to();
								context.setFrom(to);
								context.setTo(Constants.OFF);
								MoveUtilities.chainRuleCrossProduct(context, moves, null, m, false);
								context.setTo(saveTo);
								context.setFrom(saveFrom);
							}
						}
					}
					else
					{
						break;
					}
				}
			}
		}

		context.setTo(origTo);
		context.setBetween(origBetween);
		context.setFrom(origFrom);

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

		gameFlags |= movesToApply.gameFlags(game);

		if (startLocationFn != null)
			gameFlags |= startLocationFn.gameFlags(game);

		if (min != null)
			gameFlags |= min.gameFlags(game);

		if (limit != null)
			gameFlags |= limit.gameFlags(game);

		if (rule != null)
			gameFlags |= rule.gameFlags(game);

		if (betweenRule != null)
			gameFlags |= betweenRule.gameFlags(game);

		if (then() != null)
			gameFlags |= then().gameFlags(game);

		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(super.concepts(game));
		concepts.or(SiteType.concepts(type));
		concepts.or(movesToApply.concepts(game));

		if (startLocationFn != null)
			concepts.or(startLocationFn.concepts(game));

		if (min != null)
			concepts.or(min.concepts(game));

		if (limit != null)
			concepts.or(limit.concepts(game));

		if (rule != null)
			concepts.or(rule.concepts(game));

		if (betweenRule != null)
			concepts.or(betweenRule.concepts(game));

		if (then() != null)
			concepts.or(then().concepts(game));

		if (dirnChoice != null)
			concepts.or(dirnChoice.concepts(game));

		concepts.set(Concept.ControlFlowStatement.id(), true);

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = writesEvalContextFlat();
		writeEvalContext.or(super.writesEvalContextRecursive());
		writeEvalContext.or(movesToApply.writesEvalContextRecursive());

		if (startLocationFn != null)
			writeEvalContext.or(startLocationFn.writesEvalContextRecursive());

		if (min != null)
			writeEvalContext.or(min.writesEvalContextRecursive());

		if (limit != null)
			writeEvalContext.or(limit.writesEvalContextRecursive());

		if (rule != null)
			writeEvalContext.or(rule.writesEvalContextRecursive());

		if (betweenRule != null)
			writeEvalContext.or(betweenRule.writesEvalContextRecursive());

		if (then() != null)
			writeEvalContext.or(then().writesEvalContextRecursive());

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
		readEvalContext.or(super.readsEvalContextRecursive());
		readEvalContext.or(movesToApply.readsEvalContextRecursive());

		if (startLocationFn != null)
			readEvalContext.or(startLocationFn.readsEvalContextRecursive());

		if (min != null)
			readEvalContext.or(min.readsEvalContextRecursive());

		if (limit != null)
			readEvalContext.or(limit.readsEvalContextRecursive());

		if (rule != null)
			readEvalContext.or(rule.readsEvalContextRecursive());

		if (betweenRule != null)
			readEvalContext.or(betweenRule.readsEvalContextRecursive());

		if (then() != null)
			readEvalContext.or(then().readsEvalContextRecursive());

		if (dirnChoice != null)
			readEvalContext.or(dirnChoice.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= super.missingRequirement(game);
		missingRequirement |= movesToApply.missingRequirement(game);

		if (startLocationFn != null)
			missingRequirement |= startLocationFn.missingRequirement(game);

		if (min != null)
			missingRequirement |= min.missingRequirement(game);

		if (limit != null)
			missingRequirement |= limit.missingRequirement(game);

		if (rule != null)
			missingRequirement |= rule.missingRequirement(game);

		if (betweenRule != null)
			missingRequirement |= betweenRule.missingRequirement(game);

		if (then() != null)
			missingRequirement |= then().missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= super.willCrash(game);
		willCrash |= movesToApply.willCrash(game);

		if (startLocationFn != null)
			willCrash |= startLocationFn.willCrash(game);

		if (min != null)
			willCrash |= min.willCrash(game);

		if (limit != null)
			willCrash |= limit.willCrash(game);

		if (rule != null)
			willCrash |= rule.willCrash(game);

		if (betweenRule != null)
			willCrash |= betweenRule.willCrash(game);

		if (then() != null)
			willCrash |= then().willCrash(game);
		return willCrash;
	}

	@Override
	public boolean isStatic()
	{
		if (startLocationFn != null && !startLocationFn.isStatic())
			return false;

		if (rule != null && !rule.isStatic())
			return false;

		if (betweenRule != null && !betweenRule.isStatic())
			return false;

		return movesToApply.isStatic();
	}

	@Override
	public void preprocess(final Game game)
	{
		type = SiteType.use(type, game);

		super.preprocess(game);

		if (rule != null)
			rule.preprocess(game);

		if (betweenRule != null)
			betweenRule.preprocess(game);

		movesToApply.preprocess(game);

		if (startLocationFn != null)
			startLocationFn.preprocess(game);

		if (min != null)
			min.preprocess(game);

		if (limit != null)
			limit.preprocess(game);
	}
	
	//-------------------------------------------------------------------------

	@Override
	public String toEnglish(final Game game)
	{
		return "ForDirn";
	}
}
