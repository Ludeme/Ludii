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
import game.functions.ints.IntConstant;
import game.functions.ints.IntFunction;
import game.functions.ints.iterator.From;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import game.types.board.SiteType;
import game.types.state.GameType;
import game.util.directions.AbsoluteDirection;
import game.util.graph.GraphElement;
import game.util.graph.Radial;
import game.util.moves.To;
import gnu.trove.list.array.TIntArrayList;
import main.Constants;
import main.collections.FastTIntArrayList;
import other.action.Action;
import other.action.move.move.ActionMove;
import other.concept.Concept;
import other.context.Context;
import other.context.EvalContextData;
import other.move.Move;
import other.move.MoveUtilities;
import other.state.stacking.BaseContainerStateStacking;
import other.topology.Topology;
import other.topology.TopologyElement;
import other.trial.Trial;

/**
 * Defines a hop in which a piece hops over a hurdle (the {\it pivot}) in a direction.
 * 
 * @author Eric.Piette and cambolbro
 * 
 * @remarks Capture moves in Draughts are typical hop moves. 
 *          Note that we extend the standard definition of ``hop'' to include cases
 *          where the pivot is empty, for example in games such as Lines of Action
 *          and Quantum Leap.
 */
public final class Hop extends Effect
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Location of the piece. */
	private final IntFunction startLocationFn;

	/** Direction chosen. */
	private final DirectionsFunction dirnChoice;

	/** Move without to reach the hurdle. */
	private final BooleanFunction goRule;

	/** To detect the hurdle. */
	private final BooleanFunction hurdleRule;

	/** Rule to stop the move after to jump the hurdle. */
	private final BooleanFunction stopRule;

	/** Effect on the stop component corresponding to the stopRule. */
	private final Moves stopEffect;

	/** Maximum distance between from and the hurdle. */
	private final IntFunction maxDistanceFromHurdleFn;

	/** Minimum length of the hurdle. */
	private final IntFunction minLengthHurdleFn;

	/** Maximum length of the hurdle. */
	private final IntFunction maxLengthHurdleFn;

	/** Maximum distance between the hurdle and to. */
	private final IntFunction maxDistanceHurdleToFn;

	/** From Condition Rule. */
	private final BooleanFunction fromCondition;

	/** effect on the Hurdle. */
	private final Moves sideEffect;

	/** To Hop with a complete stack. */
	private final boolean stack;

	/** Add on Cell/Edge/Vertex. */
	protected SiteType type;
	
	//-------------------------------------------------------------------------

	/**
	 * @param from       The data of the from location [(from)].
	 * @param directions The directions of the move [Adjacent].
	 * @param between    The information about the locations between ``from'' and
	 *                   ``to'' [(between if:true)].
	 * @param to         The condition on the location to move.
	 * @param stack      True if the move has to be applied for stack [False].
	 * @param then       The moves applied after that move is applied.
	 * 
	 * @example (hop (between if:(is Enemy (who at:(between))) (apply (remove
	 *          (between))) ) (to if:(is Empty (to))) )
	 * 
	 * @example (hop Orthogonal (between if:(is Friend (who at:(between))) (apply
	 *          (remove (between))) ) (to if:(is Empty (to))) )
	 */
	public Hop
	(
		@Opt       final game.util.moves.From           from,
		@Opt       final game.util.directions.Direction directions,
		@Opt       final game.util.moves.Between        between,
				   final To                             to,
		@Opt @Name final Boolean                        stack,
		@Opt 	   final Then                           then
	)
	{
		super(then);

		startLocationFn = (from == null || from.loc() == null) ? new From(null) : from.loc();
		fromCondition = (from == null) ? null : from.cond();
		type = (from == null) ? null : from.type();

		maxDistanceFromHurdleFn = (between == null || between.before() == null ? new IntConstant(0) : between.before());
		minLengthHurdleFn = (between == null || between.range() == null) ? new IntConstant(1) : between.range().minFn();
		maxLengthHurdleFn = (between == null || between.range() == null) ? new IntConstant(1) : between.range().maxFn();
		maxDistanceHurdleToFn = (between == null || between.after() == null ? new IntConstant(0) : between.after());
		sideEffect = between == null ? null : between.effect();
		goRule = to.cond();
		hurdleRule = (between == null) ?  new BooleanConstant(true): between.condition();
		dirnChoice = (directions != null) ? directions.directionsFunctions()
				: new Directions(AbsoluteDirection.Adjacent, null);

		// Stop effect
		stopRule = (to.effect() == null) ? null : to.effect().condition();
		stopEffect = (to.effect() == null) ? null : to.effect().effect();

		// Stack
		this.stack = (stack == null) ? false : stack.booleanValue();
	}

	//-------------------------------------------------------------------------

	@Override
	public final Moves eval(final Context context)
	{
		final Moves moves = new BaseMoves(super.then());

		final int from = startLocationFn.eval(context);

		if (from == Constants.OFF)
			return moves;

		final int origFrom = context.from();
		final int origTo = context.to();
		final int origBetween = context.between();

		final Topology graph = context.topology();

		final SiteType realType = (type != null) ? type : context.game().board().defaultSite();
		final TopologyElement fromV = graph.getGraphElements(realType).get(from);
		
		final List<AbsoluteDirection> directions = dirnChoice.convertToAbsolute(realType, fromV, null, null, null,
				context);

		final int maxDistanceFromHurdle = maxDistanceFromHurdleFn.eval(context);
		final int minLengthHurdle = minLengthHurdleFn.eval(context);
		final int maxLengthHurdle = (minLengthHurdleFn == maxLengthHurdleFn) ? minLengthHurdle : maxLengthHurdleFn.eval(context);
		final int maxDistanceHurdleTo = maxDistanceHurdleToFn.eval(context);
		
		context.setFrom(from);

		// If the min length is 0 we can step.
		if (minLengthHurdle == 0)
		{
			for (final AbsoluteDirection direction : directions)
			{
				final List<game.util.graph.Step> steps = graph.trajectories().steps(realType, from, realType, direction);

				for (final game.util.graph.Step step : steps)
				{
					final int to = step.to().id();

					// context.setFrom(from);

					if (fromCondition != null && !fromCondition.eval(context))
						continue;
					
					context.setTo(to);

					if (!goRule.eval(context))
						continue;

					if (!alreadyCompute(moves, from, to))
					{
						final Action action = ActionMove.construct(type, from, Constants.UNDEFINED, type, to, Constants.OFF, Constants.UNDEFINED, Constants.OFF, Constants.OFF, stack);

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

						thisAction = MoveUtilities.chainRuleWithAction(context, sideEffect, thisAction, true, false);

						MoveUtilities.chainRuleCrossProduct(context, moves, null, thisAction, false);
						thisAction.setFromNonDecision(from);
						thisAction.setToNonDecision(to);
					}
				}
			}

			// context.setTo(origTo);
			// context.setFrom(origFrom);

		}

		// From here the code to hop at least a site.
		if (maxLengthHurdle > 0)
			for (final AbsoluteDirection direction : directions)
			{
				final List<Radial> radialList = graph.trajectories().radials(type, from, direction);

				for (final Radial radial : radialList)
				{
					final GraphElement[] steps = radial.steps();
					for (int toIdx = 1; toIdx < steps.length; toIdx++)
					{
						final FastTIntArrayList betweenSites = new FastTIntArrayList();
						final int between = steps[toIdx].id();

						//context.setFrom(from);
						
						if (fromCondition != null && !fromCondition.eval(context))
							continue;
						
						context.setBetween(between);

						if (hurdleRule != null && (hurdleRule.eval(context)))
						{
							final TIntArrayList hurdleLocs = new TIntArrayList();
							hurdleLocs.add(between);
							int lengthHurdle = 1;
							int hurdleIdx = toIdx + 1;
							boolean hurdleWrong = false;
							for (/**/; hurdleIdx < steps.length && lengthHurdle < maxLengthHurdle; hurdleIdx++)
							{
								final int hurdleLoc = steps[hurdleIdx].id();

								context.setBetween(hurdleLoc);
								if (!hurdleRule.eval(context))
								{
									hurdleWrong = true;
									break;
								}
								hurdleLocs.add(hurdleLoc);
								lengthHurdle++;
							}


							if (lengthHurdle < minLengthHurdle || (hurdleWrong && lengthHurdle == maxLengthHurdle))
								break;

							betweenSites.addAll(hurdleLocs);

							// Jump between the min size of the hurdle to the length of the hurdle detected.
							for (int fromMinHurdle = lengthHurdle
									- minLengthHurdle; fromMinHurdle >= 0; fromMinHurdle--)
							{
								int afterHurdleToIdx = hurdleIdx - fromMinHurdle;
								for (/**/; afterHurdleToIdx < steps.length; afterHurdleToIdx++)
								{
									final int afterHurdleTo = steps[afterHurdleToIdx].id();

									//context.setFrom(from);
									context.setTo(afterHurdleTo);

									if (!goRule.eval(context))
									{
										if (stopRule != null && stopRule.eval(context))
										{
											final Action action = ActionMove.construct(type, from, Constants.UNDEFINED, type, afterHurdleTo, Constants.OFF, Constants.UNDEFINED, Constants.OFF, Constants.OFF, stack);
											if (isDecision())
												action.setDecision(true);
											Move move = new Move(action);

											if (stopEffect != null)
												move = MoveUtilities.chainRuleWithAction(context, stopEffect,
														move, true, false);

											MoveUtilities.chainRuleCrossProduct(context, moves, null, move,
													false);
											move.setFromNonDecision(from);
											move.setBetweenNonDecision(new FastTIntArrayList(betweenSites));
											move.setToNonDecision(afterHurdleTo);
										}
										break;
									}
	
									if (stopRule == null)
									{
										final Action action = ActionMove.construct(type, from, Constants.UNDEFINED, type, afterHurdleTo, Constants.OFF, Constants.UNDEFINED, Constants.OFF, Constants.OFF, stack);
										if (isDecision())
											action.setDecision(true);

										Move move = new Move(action);

										if (stopEffect != null)
											move = MoveUtilities.chainRuleWithAction(context, stopEffect,
													move, true, false);

										for (int hurdleLocIndex = 0; hurdleLocIndex < hurdleLocs.size()
												- fromMinHurdle; hurdleLocIndex++)
										{
											final int hurdleLoc = hurdleLocs.getQuick(hurdleLocIndex);
											context.setBetween(hurdleLoc);
											if (sideEffect != null)
												move = MoveUtilities.chainRuleWithAction(context, sideEffect,
														move, true, false);
										}

										MoveUtilities.chainRuleCrossProduct(context, moves, null,
												move, false);
										move.setFromNonDecision(from);
										move.setBetweenNonDecision(new FastTIntArrayList(betweenSites));
										move.setToNonDecision(afterHurdleTo);
										// to add the levels to move a stack on the Move class (only for GUI)
										if (stack)
										{
											move.setLevelMinNonDecision(0);
											move.setLevelMaxNonDecision(
													((BaseContainerStateStacking) context.state().containerStates()[0])
															.sizeStack(from, type) - 1);
										}
									}

									if ((afterHurdleToIdx - hurdleIdx + 1) > (maxDistanceHurdleTo - fromMinHurdle))
										break;
								}
							}
							break;
						}

						context.setTo(between);
						if (toIdx > maxDistanceFromHurdle || (!goRule.eval(context)))
							break;

					}
				}
			}

		context.setTo(origTo);
		context.setFrom(origFrom);
		context.setBetween(origBetween);
		
		MoveUtilities.setGeneratedMovesData(moves.moves(), this, context.state().mover());

		return moves;
	}

	//-------------------------------------------------------------------------

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = super.gameFlags(game);
		gameFlags |= GameType.UsesFromPositions;
		
		if (stack) 
			gameFlags |= GameType.Stacking;
		
		if (goRule != null)
			gameFlags |= goRule.gameFlags(game);
		
		if (startLocationFn != null)
			gameFlags |= startLocationFn.gameFlags(game);

		if (fromCondition != null)
			gameFlags |= fromCondition.gameFlags(game);
		
		if (hurdleRule != null)
			gameFlags |= hurdleRule.gameFlags(game);
		
		if (maxDistanceFromHurdleFn != null)
			gameFlags = gameFlags | maxDistanceFromHurdleFn.gameFlags(game);

		if (maxDistanceHurdleToFn != null)
			gameFlags = gameFlags | maxDistanceHurdleToFn.gameFlags(game);
		
		if (maxLengthHurdleFn != null)
			gameFlags = gameFlags | maxLengthHurdleFn.gameFlags(game);

		if (minLengthHurdleFn != null)
			gameFlags = gameFlags | minLengthHurdleFn.gameFlags(game);

		if (sideEffect != null)
			gameFlags = gameFlags | sideEffect.gameFlags(game);

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
		if(isDecision())
		{
			concepts.set(Concept.HopDecision.id(), true);
			if (hurdleRule != null && goRule != null)
			{
				if ((goRule.concepts(game).get(Concept.IsEmpty.id()) || goRule instanceof BooleanConstant.TrueConstant)
						&& (hurdleRule.concepts(game).get(Concept.IsEnemy.id()) || hurdleRule instanceof BooleanConstant.TrueConstant))
					concepts.set(Concept.HopDecisionEnemyToEmpty.id(), true);

				if ((goRule.concepts(game).get(Concept.IsEmpty.id()) || goRule instanceof BooleanConstant.TrueConstant)
						&& (hurdleRule.concepts(game).get(Concept.IsFriend.id()) || hurdleRule instanceof BooleanConstant.TrueConstant))
					concepts.set(Concept.HopDecisionFriendToEmpty.id(), true);

				if ((goRule.concepts(game).get(Concept.IsFriend.id()) || goRule instanceof BooleanConstant.TrueConstant)
						&& (hurdleRule.concepts(game).get(Concept.IsEnemy.id()) || hurdleRule instanceof BooleanConstant.TrueConstant))
					concepts.set(Concept.HopDecisionEnemyToFriend.id(), true);

				if ((goRule.concepts(game).get(Concept.IsFriend.id()) || goRule instanceof BooleanConstant.TrueConstant)
						&& (hurdleRule.concepts(game).get(Concept.IsFriend.id()) || hurdleRule instanceof BooleanConstant.TrueConstant))
					concepts.set(Concept.HopDecisionFriendToFriend.id(), true);

				if ((goRule.concepts(game).get(Concept.IsEnemy.id()) || goRule instanceof BooleanConstant.TrueConstant)
						&& (hurdleRule.concepts(game).get(Concept.IsEnemy.id()) || hurdleRule instanceof BooleanConstant.TrueConstant))
					concepts.set(Concept.HopDecisionEnemyToEnemy.id(), true);

				if ((goRule.concepts(game).get(Concept.IsEnemy.id()) || goRule instanceof BooleanConstant.TrueConstant)
						&& (hurdleRule.concepts(game).get(Concept.IsFriend.id()) || hurdleRule instanceof BooleanConstant.TrueConstant))
					concepts.set(Concept.HopDecisionFriendToEnemy.id(), true);
			}
		}
		else
			concepts.set(Concept.HopEffect.id(), true);
		
		if (goRule != null)
			concepts.or(goRule.concepts(game));

		if (fromCondition != null)
			concepts.or(fromCondition.concepts(game));
		
		if (startLocationFn != null)
			concepts.or(startLocationFn.concepts(game));
		
		if (hurdleRule != null)
			concepts.or(hurdleRule.concepts(game));
		
		if (maxDistanceFromHurdleFn != null)
			concepts.or(maxDistanceFromHurdleFn.concepts(game));

		if (maxDistanceHurdleToFn != null)
			concepts.or(maxDistanceHurdleToFn.concepts(game));
		
		if (maxLengthHurdleFn != null)
			concepts.or(maxLengthHurdleFn.concepts(game));

		if (minLengthHurdleFn != null)
			concepts.or(minLengthHurdleFn.concepts(game));

		if (sideEffect != null)
			concepts.or(sideEffect.concepts(game));

		if (then() != null)
			concepts.or(then().concepts(game));

		if (dirnChoice != null)
			concepts.or(dirnChoice.concepts(game));

		// We check if that's effectively a replacement capture (remove or fromTo).
		if (stopEffect != null)
			if (stopEffect.concepts(game).get(Concept.RemoveEffect.id())
					|| stopEffect.concepts(game).get(Concept.FromToEffect.id()))
				concepts.set(Concept.ReplacementCapture.id(), true);

		// We check if that's effectively a hop capture (remove or fromTo).
		if (sideEffect != null)
			if (sideEffect.concepts(game).get(Concept.RemoveEffect.id())
					|| sideEffect.concepts(game).get(Concept.FromToEffect.id()))
				concepts.set(Concept.HopCapture.id(), true);

		// We check if some pieces can jump more than one site.
		if (isDecision())
		{
			if (minLengthHurdleFn instanceof IntConstant)
			{
				if (((IntConstant) minLengthHurdleFn).eval(new Context(game, new Trial(game))) > 1)
					concepts.set(Concept.HopDecisionMoreThanOne.id(), true);
			}
			else
				concepts.set(Concept.HopDecisionMoreThanOne.id(), true);
			if (maxLengthHurdleFn instanceof IntConstant)
			{
				if (((IntConstant) maxLengthHurdleFn).eval(new Context(game, new Trial(game))) > 1)
					concepts.set(Concept.HopDecisionMoreThanOne.id(), true);
			}
			else
				concepts.set(Concept.HopDecisionMoreThanOne.id(), true);

			// If the pieces jumped are potentially more than one and capture is
			// activated we active the HopCaptureMoreThanOne too.
			if (concepts.get(Concept.HopDecisionMoreThanOne.id()) && concepts.get(Concept.HopCapture.id()))
				concepts.set(Concept.HopCaptureMoreThanOne.id());

			if (maxDistanceHurdleToFn instanceof IntConstant)
			{
				if (((IntConstant) maxDistanceHurdleToFn).eval(new Context(game, new Trial(game))) > 1)
					concepts.set(Concept.HopDecisionMoreThanOne.id(), true);
			}
			else
				concepts.set(Concept.HopDecisionMoreThanOne.id(), true);
			if (maxDistanceFromHurdleFn instanceof IntConstant)
			{
				if (((IntConstant) maxDistanceFromHurdleFn).eval(new Context(game, new Trial(game))) > 1)
					concepts.set(Concept.HopDecisionMoreThanOne.id(), true);
			}
			else
				concepts.set(Concept.HopDecisionMoreThanOne.id(), true);
		}

		return concepts;
	}
	
	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = writesEvalContextFlat();
		writeEvalContext.or(super.writesEvalContextRecursive());
		if (goRule != null)
			writeEvalContext.or(goRule.writesEvalContextRecursive());

		if (fromCondition != null)
			writeEvalContext.or(fromCondition.writesEvalContextRecursive());
		
		if (startLocationFn != null)
			writeEvalContext.or(startLocationFn.writesEvalContextRecursive());
		
		if (hurdleRule != null)
			writeEvalContext.or(hurdleRule.writesEvalContextRecursive());
		
		if (maxDistanceFromHurdleFn != null)
			writeEvalContext.or(maxDistanceFromHurdleFn.writesEvalContextRecursive());

		if (maxDistanceHurdleToFn != null)
			writeEvalContext.or(maxDistanceHurdleToFn.writesEvalContextRecursive());
		
		if (maxLengthHurdleFn != null)
			writeEvalContext.or(maxLengthHurdleFn.writesEvalContextRecursive());

		if (minLengthHurdleFn != null)
			writeEvalContext.or(minLengthHurdleFn.writesEvalContextRecursive());

		if (sideEffect != null)
			writeEvalContext.or(sideEffect.writesEvalContextRecursive());

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
		if (goRule != null)
			readEvalContext.or(goRule.readsEvalContextRecursive());

		if (fromCondition != null)
			readEvalContext.or(fromCondition.readsEvalContextRecursive());
		
		if (startLocationFn != null)
			readEvalContext.or(startLocationFn.readsEvalContextRecursive());
		
		if (hurdleRule != null)
			readEvalContext.or(hurdleRule.readsEvalContextRecursive());
		
		if (maxDistanceFromHurdleFn != null)
			readEvalContext.or(maxDistanceFromHurdleFn.readsEvalContextRecursive());

		if (maxDistanceHurdleToFn != null)
			readEvalContext.or(maxDistanceHurdleToFn.readsEvalContextRecursive());
		
		if (maxLengthHurdleFn != null)
			readEvalContext.or(maxLengthHurdleFn.readsEvalContextRecursive());

		if (minLengthHurdleFn != null)
			readEvalContext.or(minLengthHurdleFn.readsEvalContextRecursive());

		if (sideEffect != null)
			readEvalContext.or(sideEffect.readsEvalContextRecursive());

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

		if (goRule != null)
			missingRequirement |= goRule.missingRequirement(game);

		if (fromCondition != null)
			missingRequirement |= fromCondition.missingRequirement(game);

		if (startLocationFn != null)
			missingRequirement |= startLocationFn.missingRequirement(game);

		if (hurdleRule != null)
			missingRequirement |= hurdleRule.missingRequirement(game);

		if (maxDistanceFromHurdleFn != null)
			missingRequirement |= maxDistanceFromHurdleFn.missingRequirement(game);

		if (maxDistanceHurdleToFn != null)
			missingRequirement |= maxDistanceHurdleToFn.missingRequirement(game);

		if (maxLengthHurdleFn != null)
			missingRequirement |= maxLengthHurdleFn.missingRequirement(game);

		if (minLengthHurdleFn != null)
			missingRequirement |= minLengthHurdleFn.missingRequirement(game);

		if (sideEffect != null)
			missingRequirement |= sideEffect.missingRequirement(game);

		if (then() != null)
			missingRequirement |= then().missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= super.willCrash(game);

		if (fromCondition != null)
			willCrash |= fromCondition.willCrash(game);

		if (goRule != null)
			willCrash |= goRule.willCrash(game);

		if (startLocationFn != null)
			willCrash |= startLocationFn.willCrash(game);

		if (hurdleRule != null)
			willCrash |= hurdleRule.willCrash(game);

		if (maxDistanceFromHurdleFn != null)
			willCrash |= maxDistanceFromHurdleFn.willCrash(game);

		if (maxDistanceHurdleToFn != null)
			willCrash |= maxDistanceHurdleToFn.willCrash(game);

		if (maxLengthHurdleFn != null)
			willCrash |= maxLengthHurdleFn.willCrash(game);

		if (minLengthHurdleFn != null)
			willCrash |= minLengthHurdleFn.willCrash(game);

		if (sideEffect != null)
			willCrash |= sideEffect.willCrash(game);

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
		
		if (goRule != null)
			goRule.preprocess(game);
		
		if (stopRule != null)
			stopRule.preprocess(game);

		if (fromCondition != null)
			fromCondition.preprocess(game);

		if (stopEffect != null)
			stopEffect.preprocess(game);

		if (startLocationFn != null)
			startLocationFn.preprocess(game);
		
		if (hurdleRule != null)
			hurdleRule.preprocess(game);
		
		if (maxDistanceFromHurdleFn != null)
			maxDistanceFromHurdleFn.preprocess(game);

		if (maxDistanceHurdleToFn != null)
			maxDistanceHurdleToFn.preprocess(game);
		
		if (maxLengthHurdleFn != null)
			maxLengthHurdleFn.preprocess(game);

		if (minLengthHurdleFn != null)
			minLengthHurdleFn.preprocess(game);

		if (sideEffect != null)
			sideEffect.preprocess(game);
	}

	//-------------------------------------------------------------------------
	
	/**
	 * @return Rule that tells us the kinds of cells we're allowed to traverse
	 * on the way to the hurdle.
	 */
	public BooleanFunction goRule()
	{
		return goRule;
	}
	
	/**
	 * @return Rule that tells us what kinds of cells are recognised as hurdle
	 */
	public BooleanFunction hurdleRule()
	{
		return hurdleRule;
	}
	
	/**
	 * @return Maximum number of steps we're allowed to take to reach a hurdle
	 */
	public IntFunction maxDistanceFromHurdleFn()
	{
		return maxDistanceFromHurdleFn;
	}
	
	/**
	 * @return Minimum number of steps that must be occupied by hurdle
	 */
	public IntFunction minLengthHurdleFn()
	{
		return minLengthHurdleFn;
	}
	
	/**
	 * @return Maximum number of steps that must be occupied by hurdle
	 */
	public IntFunction maxLengthHurdleFn()
	{
		return maxLengthHurdleFn;
	}
	
	/**
	 * @return Maximum number of steps we're allowed to take "behind" hurdle
	 */
	public IntFunction maxDistanceHurdleToFn()
	{
		return maxDistanceHurdleToFn;
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
	
	@Override
	public String toEnglish(final Game game)
	{
		String thenString = "";
		if (then() != null)
			thenString = " then " + then().toEnglish(game);
		
		return "hop " + dirnChoice.toEnglish(game) + thenString;
	}
}
