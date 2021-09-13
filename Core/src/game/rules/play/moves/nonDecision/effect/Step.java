package game.rules.play.moves.nonDecision.effect;

import java.util.BitSet;
import java.util.List;

import annotations.Name;
import annotations.Opt;
import game.Game;
import game.functions.booleans.BooleanConstant;
import game.functions.booleans.BooleanFunction;
import game.functions.directions.Directions;
import game.functions.directions.DirectionsFunction;
import game.functions.ints.IntFunction;
import game.functions.ints.iterator.From;
import game.functions.region.RegionFunction;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import game.types.board.SiteType;
import game.types.state.GameType;
import game.util.directions.AbsoluteDirection;
import main.Constants;
import other.action.move.ActionMove;
import other.concept.Concept;
import other.context.Context;
import other.context.EvalContextData;
import other.move.Move;
import other.move.MoveUtilities;
import other.state.stacking.BaseContainerStateStacking;
import other.topology.Topology;
import other.topology.TopologyElement;

/**
 * Moves to a connected site.
 * 
 * @author Eric.Piette
 * 
 * @remarks The (to ...) parameter is used to specify a condition on the site to
 *          step and on the effect to apply. But the region function in entry of
 *          that parameter is ignored because the region to move is implied by a
 *          step move.
 */
public final class Step extends Effect
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Location of the piece. */
	private final IntFunction startLocationFn;

	/** From Condition Rule. */
	private final BooleanFunction fromCondition;

	/** Region start of the piece. */
	private final RegionFunction startRegionFn;

	/** Level from. */
	private final IntFunction levelFromFn;

	/** The rule to be able to the move. */
	private final BooleanFunction rule;

	/** The Move applied on the location reached. */
	private final Moves sideEffect;

	/** To step a complete stack. */
	private final boolean stack;

	/** Direction to move. */
	private final DirectionsFunction dirnChoice;

	/** Direction to move. */
	private final game.util.moves.To toRule;

	/** Add on Cell/Edge/Vertex. */
	private SiteType type;

	//-------------------------------------------------------------------------

	/**
	 * @param from       Description of ``from'' location [(from)].
	 * @param directions The directions of the move [Adjacent].
	 * @param to         Description of the ``to'' location.
	 * @param stack      True if the move is applied to a stack [False].
	 * @param then       Moves to apply after this one.
	 * 
	 * @example (step (to if:(is Empty (to))) )
	 * 
	 * @example (step Forward (to if:(is Empty (to))) )
	 * 
	 * @example (step (directions {FR FL}) (to if:(or (is Empty (to)) (is Enemy (who
	 *          at:(to)))) (apply (remove (to)))) )
	 */
	public Step
	(
		@Opt 	   final game.util.moves.From           from,
		@Opt       final game.util.directions.Direction directions,
			  	   final game.util.moves.To             to,
		@Opt @Name final Boolean                        stack,
		@Opt 	   final Then                           then
	)
	{
		super(then);

		// From
		if (from != null)
		{
			startRegionFn = from.region();
			startLocationFn = from.loc();
			levelFromFn = from.level();
			fromCondition = from.cond();
		}
		else
		{
			startRegionFn = null;
			startLocationFn = new From(null);
			levelFromFn = null;
			fromCondition = null;
		}
		type = (from == null) ? null : from.type();

		// Rule
		rule = (to == null) ? new BooleanConstant(true)
				: (to.cond() == null) ? new BooleanConstant(true) : to.cond();

		// The directions
		dirnChoice = (directions != null) ? directions.directionsFunctions()
				: new Directions(AbsoluteDirection.Adjacent, null);

		// Effect
		sideEffect = (to == null) ? null : to.effect();

		// Stack
		this.stack = (stack == null) ? false : stack.booleanValue();

		// We store the toRule because that can be needed in CountSteps.java
		toRule = to;
	}

	//-------------------------------------------------------------------------

	@Override
	public Moves eval(final Context context)
	{
		if (startRegionFn != null)
			return evalRegion(context);

		final Moves moves = new BaseMoves(super.then());

		final int from = startLocationFn.eval(context);
		if (from == Constants.OFF)
			return moves;

		final int origFrom = context.from();
		final int origTo = context.to();

		final Topology graph = context.topology();

		final SiteType realType = (type != null) ? type : context.game().board().defaultSite();

		if (from >= graph.getGraphElements(realType).size())
			return moves;

		final TopologyElement fromV = graph.getGraphElements(realType).get(from);

		final List<AbsoluteDirection> directions = dirnChoice.convertToAbsolute(realType, fromV, null, null, null,
				context);

		final int levelFrom = (levelFromFn == null) ? Constants.UNDEFINED : levelFromFn.eval(context);

		// If the level specified is less than 0, no move can be computed.
		if (levelFrom < Constants.UNDEFINED && levelFromFn != null)
			return moves;

		context.setFrom(from);

		if (fromCondition != null && !fromCondition.eval(context))
			return moves;

		for (final AbsoluteDirection direction : directions)
		{
			final List<game.util.graph.Step> steps = 
					graph.trajectories().steps(realType, fromV.index(), realType, direction);

			for (final game.util.graph.Step step : steps)
			{
				final int to = step.to().id();

				context.setTo(to);

				if (!rule.eval(context))
					continue;

				if (!alreadyCompute(moves, from, to))
				{
					ActionMove action = null;
					Move move = null;
					if (levelFrom == Constants.UNDEFINED || stack == true)
					{
						final int level = (context.game().isStacking())
								? (stack) ? Constants.UNDEFINED
										: context.state().containerStates()[0].sizeStack(from, type) - 1
								: Constants.UNDEFINED;

						action = new ActionMove(type, from, level, type, to, Constants.OFF, Constants.UNDEFINED,
								Constants.OFF, Constants.OFF, stack);
						if (isDecision())
							action.setDecision(true);
						move = new Move(action);

						// to add the levels to move a stack on the Move class (only for GUI)
						if (stack)
						{
							move.setLevelMinNonDecision(0);
							move.setLevelMaxNonDecision(context.state().containerStates()[0].sizeStack(from, type) - 1);
						}
					}
					else // Step a piece at a specific level.
					{
						action = new ActionMove(type, from, levelFrom, type, to, Constants.UNDEFINED,
								Constants.UNDEFINED, Constants.UNDEFINED, Constants.OFF, stack);

						if (isDecision())
							action.setDecision(true);
						move = new Move(action);
						move.setLevelMinNonDecision(levelFrom);
						move.setLevelMaxNonDecision(levelFrom);
					}


					move = MoveUtilities.chainRuleWithAction(context, sideEffect, move, true, false);

					MoveUtilities.chainRuleCrossProduct(context, moves, null, move, false);
					move.setFromNonDecision(from);
					move.setToNonDecision(to);
				}
			}
		}

		context.setTo(origTo);
		context.setFrom(origFrom);
		
		MoveUtilities.setGeneratedMovesData(moves.moves(), this, context.state().mover());

		return moves;
	}

	//-------------------------------------------------------------------------

	/**
	 * 
	 * Exact same idea of eval(context) but for a starting region
	 * 
	 * @param context
	 * @return
	 */
	private Moves evalRegion(final Context context)
	{
		final Moves moves = new BaseMoves(super.then());

		if (startLocationFn == null)
			return moves;

		final int fromLocforRegion = startLocationFn.eval(context);

		final int[] froms = startRegionFn.eval(context).sites();
		if (froms.length == 0)
			return moves;

		final int origFrom = context.from();
		final int origTo = context.to();
		for (final int from : froms)
		{
			if (from == Constants.OFF)
				continue;

			final Topology graph = context.topology();

			final SiteType realType = (type != null) ? type : context.game().board().defaultSite();

			if (from >= graph.getGraphElements(realType).size())
				return moves;

			final TopologyElement fromV = graph.getGraphElements(realType).get(from);

			context.setFrom(fromV.index());

			if (fromCondition != null && !fromCondition.eval(context))
				continue;

			final List<AbsoluteDirection> directions = dirnChoice.convertToAbsolute(realType, fromV, null, null, null,
					context);

			for (final AbsoluteDirection direction : directions)
			{
				final List<game.util.graph.Step> steps = graph.trajectories().steps(realType, fromV.index(), direction);

				for (final game.util.graph.Step step : steps)
				{
					final int to = step.to().id();

					context.setTo(to);
					if (!rule.eval(context))
						continue;

					final ActionMove action = new ActionMove(SiteType.Cell, from, Constants.UNDEFINED, SiteType.Cell,
							to, Constants.OFF, Constants.UNDEFINED, Constants.OFF, Constants.OFF, stack);

					if (isDecision())
						action.setDecision(true);

					Move thisAction = new Move(action);

					// to add the levels to move a stack on the Move class (only for GUI)
					if (stack)
					{
						thisAction.setLevelMinNonDecision(0);
						thisAction.setLevelMaxNonDecision(((BaseContainerStateStacking) context.state().containerStates()[0])
								.sizeStack(from, type) - 1);
					}

					final int origFrom2 = context.from();
					final int origTo2 = context.to();
					context.setFrom(fromLocforRegion);
					context.setTo(from);
					thisAction = new Move(action, sideEffect.eval(context).moves().get(0));
					context.setFrom(origFrom2);
					context.setTo(origTo2);
					MoveUtilities.chainRuleCrossProduct(context, moves, null, thisAction, false);
					thisAction.setFromNonDecision(from);
					thisAction.setToNonDecision(to);
				}
			}
		}
		context.setTo(origTo);
		context.setFrom(origFrom);

		if (then() != null)
			for (int j = 0; j < moves.moves().size(); j++)
				moves.moves().get(j).then().add(then().moves());

		// Store the Moves in the computed moves.
		for (int j = 0; j < moves.moves().size(); j++)
			moves.moves().get(j).setMovesLudeme(this);

		return moves;
	}

	//-------------------------------------------------------------------------
	
	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = super.gameFlags(game) | rule.gameFlags(game);
		gameFlags |= GameType.UsesFromPositions;
		
		if (stack)
			gameFlags |= GameType.Stacking;
		if (fromCondition != null)
			gameFlags |= fromCondition.gameFlags(game);
		if (startLocationFn != null)
			gameFlags |= startLocationFn.gameFlags(game);
		if (startRegionFn != null)
			gameFlags |= startRegionFn.gameFlags(game);
		if (sideEffect != null)
			gameFlags |= sideEffect.gameFlags(game);
		if (levelFromFn != null)
			gameFlags |= levelFromFn.gameFlags(game);

		gameFlags |= SiteType.gameFlags(type);

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
		concepts.or(rule.concepts(game));

		if (fromCondition != null)
			concepts.or(fromCondition.concepts(game));

		if (isDecision())
		{
			concepts.set(Concept.StepDecision.id(), true);
			if (rule.concepts(game).get(Concept.IsEmpty.id()))
				concepts.set(Concept.StepDecisionToEmpty.id(), true);
			if (rule.concepts(game).get(Concept.IsFriend.id()))
				concepts.set(Concept.StepDecisionToFriend.id(), true);
			if (rule.concepts(game).get(Concept.IsEnemy.id()))
				concepts.set(Concept.StepDecisionToEnemy.id(), true);
			if (rule instanceof BooleanConstant.TrueConstant)
			{
				concepts.set(Concept.StepDecisionToEmpty.id(), true);
				concepts.set(Concept.StepDecisionToFriend.id(), true);
				concepts.set(Concept.StepDecisionToEnemy.id(), true);
			}
		}
		else
			concepts.set(Concept.StepEffect.id(), true);

		if (startLocationFn != null)
			concepts.or(startLocationFn.concepts(game));
		if (startRegionFn != null)
			concepts.or(startRegionFn.concepts(game));
		if (sideEffect != null)
			concepts.or(sideEffect.concepts(game));
		if (levelFromFn != null)
			concepts.or(levelFromFn.concepts(game));

		if (dirnChoice != null)
			concepts.or(dirnChoice.concepts(game));

		if (then() != null)
			concepts.or(then().concepts(game));

		// We check if that's effectively a capture (remove or fromTo).
		if (sideEffect != null)
			if (sideEffect.concepts(game).get(Concept.RemoveEffect.id())
					|| sideEffect.concepts(game).get(Concept.FromToEffect.id()))
				concepts.set(Concept.ReplacementCapture.id(), true);

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = writesEvalContextFlat();
		writeEvalContext.or(super.writesEvalContextRecursive());
		writeEvalContext.or(rule.writesEvalContextRecursive());

		if (fromCondition != null)
			writeEvalContext.or(fromCondition.writesEvalContextRecursive());

		if (startLocationFn != null)
			writeEvalContext.or(startLocationFn.writesEvalContextRecursive());
		if (startRegionFn != null)
			writeEvalContext.or(startRegionFn.writesEvalContextRecursive());
		if (sideEffect != null)
			writeEvalContext.or(sideEffect.writesEvalContextRecursive());
		if (levelFromFn != null)
			writeEvalContext.or(levelFromFn.writesEvalContextRecursive());

		if (dirnChoice != null)
			writeEvalContext.or(dirnChoice.writesEvalContextRecursive());

		if (then() != null)
			writeEvalContext.or(then().writesEvalContextRecursive());
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
		readEvalContext.or(super.readsEvalContextRecursive());
		readEvalContext.or(rule.readsEvalContextRecursive());

		if (fromCondition != null)
			readEvalContext.or(fromCondition.readsEvalContextRecursive());

		if (startLocationFn != null)
			readEvalContext.or(startLocationFn.readsEvalContextRecursive());
		if (startRegionFn != null)
			readEvalContext.or(startRegionFn.readsEvalContextRecursive());
		if (sideEffect != null)
			readEvalContext.or(sideEffect.readsEvalContextRecursive());
		if (levelFromFn != null)
			readEvalContext.or(levelFromFn.readsEvalContextRecursive());

		if (dirnChoice != null)
			readEvalContext.or(dirnChoice.readsEvalContextRecursive());

		if (then() != null)
			readEvalContext.or(then().readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= super.missingRequirement(game);
		missingRequirement |= rule.missingRequirement(game);

		if (fromCondition != null)
			missingRequirement |= fromCondition.missingRequirement(game);
		if (startLocationFn != null)
			missingRequirement |= startLocationFn.missingRequirement(game);
		if (startRegionFn != null)
			missingRequirement |= startRegionFn.missingRequirement(game);
		if (sideEffect != null)
			missingRequirement |= sideEffect.missingRequirement(game);
		if (levelFromFn != null)
			missingRequirement |= levelFromFn.missingRequirement(game);

		if (then() != null)
			missingRequirement |= then().missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= super.willCrash(game);
		willCrash |= rule.willCrash(game);

		if (fromCondition != null)
			willCrash |= fromCondition.willCrash(game);
		if (startLocationFn != null)
			willCrash |= startLocationFn.willCrash(game);
		if (startRegionFn != null)
			willCrash |= startRegionFn.willCrash(game);
		if (sideEffect != null)
			willCrash |= sideEffect.willCrash(game);
		if (levelFromFn != null)
			willCrash |= levelFromFn.willCrash(game);
		if (then() != null)
			willCrash |= then().willCrash(game);
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
		type = SiteType.use(type, game);

		super.preprocess(game);

		rule.preprocess(game);

		if (fromCondition != null)
			fromCondition.preprocess(game);
		if (startLocationFn != null)
			startLocationFn.preprocess(game);
		if (startRegionFn != null)
			startRegionFn.preprocess(game);
		if (sideEffect != null)
			sideEffect.preprocess(game);
		if (rule != null)
			rule.preprocess(game);
		if (sideEffect != null)
			sideEffect.preprocess(game);
		if (dirnChoice != null)
			dirnChoice.preprocess(game);
		if (levelFromFn != null)
			levelFromFn.preprocess(game);
	}

	/**
	 * To know if a move is already compute (for wheel board with the center, this
	 * is possible).
	 * 
	 * @param moves
	 * @param from
	 * @param to
	 * @return
	 */
	private static boolean alreadyCompute(final Moves moves, final int from, final int to)
	{
		for (final Move m : moves.moves())
		{
			if (m.fromNonDecision() == from && m.toNonDecision() == to)
			{
				return true;
			}
		}
		return false;
	}

	//-------------------------------------------------------------------------

	/**
	 * @return The rule telling us what we're allowed to step into
	 */
	public BooleanFunction goRule()
	{
		return rule;
	}

	/**
	 * @return The directions.
	 */
	public DirectionsFunction directions()
	{
		return dirnChoice;
	}

	/**
	 * @return The to rule.
	 */
	public game.util.moves.To toRule()
	{
		return toRule;
	}
	
	@Override
	public String toEnglish(final Game game)
	{
		String sideEffectEnglish = "";
		if (sideEffect != null)
			sideEffectEnglish = sideEffect.toEnglish(game) + " then ";
		
		return sideEffectEnglish + "step " + dirnChoice.toEnglish(game);
	}
	
}

