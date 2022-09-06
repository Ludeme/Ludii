package game.rules.play.moves.nonDecision.effect;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import annotations.Name;
import annotations.Opt;
import game.Game;
import game.equipment.container.board.Track;
import game.functions.booleans.BooleanConstant;
import game.functions.booleans.BooleanFunction;
import game.functions.booleans.is.in.IsIn;
import game.functions.directions.Directions;
import game.functions.directions.DirectionsFunction;
import game.functions.ints.IntConstant;
import game.functions.ints.IntFunction;
import game.functions.ints.iterator.Between;
import game.functions.ints.iterator.From;
import game.functions.region.sites.index.SitesEmpty;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import game.types.board.SiteType;
import game.types.state.GameType;
import game.util.directions.AbsoluteDirection;
import game.util.graph.Radial;
import game.util.moves.To;
import main.Constants;
import main.collections.FastTIntArrayList;
import other.action.Action;
import other.action.move.ActionAdd;
import other.action.move.move.ActionMove;
import other.concept.Concept;
import other.context.Context;
import other.context.EvalContextData;
import other.move.Move;
import other.move.MoveUtilities;
import other.topology.Topology;
import other.topology.TopologyElement;

/**
 * Slides a piece in a direction through a number of sites.
 * 
 * @author Eric.Piette
 * 
 * @remarks The rook in Chess is an example of a piece that slides in Orthogonal
 *          directions. Pieces can be constrained to slide along predefined
 *          tracks, e.g. see Surakarta. Note that we extend the standard
 *          definition of ``slide'' to allow pieces to slide through other
 *          pieces if a specific condition is given, but that pieces are assumed
 *          to slide through empty sites only by default.
 */
public final class Slide extends Effect
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Location of the piece. */
	private final IntFunction startLocationFn;

	/** Level from. */
	private final IntFunction levelFromFn;
	
	/** From Condition Rule. */
	private final BooleanFunction fromCondition;

	/** Limit to slide. */
	private final IntFunction limit;

	/** Minimal distance. */
	private final IntFunction minFn;

	/** The rule to continue the move. */
	private final BooleanFunction goRule;

	/** The rule to stop the move. */
	private final BooleanFunction stopRule;
	
	/** If not null, the site has to go has to follow this condition. */
	private final BooleanFunction toRule;

	/** The piece let on each site during the movement. */
	private final IntFunction let;

	/** The Move applied on the location crossed. */
	private final Moves betweenEffect;

	/** The Move applied on the location reached. */
	private final Moves sideEffect;
	
	/** Direction choosen. */
	private final DirectionsFunction dirnChoice;
	
	/** Track choosen */
	private final String trackName;

	/** Add on Cell/Edge/Vertex. */
	protected SiteType type;

	/** To step a complete stack. */
	private final boolean stack;

	//-------------------------------------------------------------------------

	/** Pre-computed tracks in case of slide move on the tracks. */
	private List<Track> preComputedTracks = new ArrayList<Track>();
	
	//-------------------------------------------------------------------------

	/**
	 * @param from       Description of the ``from'' location [(from)].
	 * @param track      The track on which to slide.
	 * @param directions The directions of the move [Adjacent].
	 * @param between    Description of the location(s) between ``from'' and ``to''.
	 * @param to         Description of the ``to'' location.
	 * @param stack      True if the move is applied to a stack [False].
	 * @param then       Moves to apply after this one.
	 * 
	 * @example (slide)
	 * 
	 * @example (slide Orthogonal)
	 * 
	 * @example (slide "AllTracks" (between if:(or (= (between) (from)) (is In
	 *          (between) (sites Empty)) ) ) (to if:(is Enemy (who at:(to))) (apply
	 *          (remove (to))) ) (then (set Counter)) )
	 */
	public Slide
	(
			@Opt       final game.util.moves.From           from, 
			@Opt       final String                         track,
			@Opt       final game.util.directions.Direction directions, 
			@Opt       final game.util.moves.Between        between,
			@Opt       final To                             to, 
			@Opt @Name final Boolean                        stack, 
			@Opt       final Then                           then
	) 
	{
		super(then);

		// From
		if (from != null)
		{
			startLocationFn = from.loc();
			levelFromFn = from.level();
			fromCondition = from.cond();
		}
		else
		{
			startLocationFn = new From(null);
			levelFromFn = null;
			fromCondition = null;
		}
		type = (from == null) ? null : from.type();

		minFn = (between == null || between.range() == null) ? new IntConstant(Constants.UNDEFINED)
				: between.range().minFn();
		limit = (between == null || between.range() == null) ? new IntConstant(Constants.MAX_DISTANCE)
				: between.range().maxFn();
		sideEffect = (to == null || to.effect() == null) ? null : to.effect().effect();
		goRule = (between == null || between.condition() == null) ? IsIn.construct(null, new IntFunction[]
		{ Between.instance() }, SitesEmpty.construct(null, null), null) : between.condition();
		stopRule = (to == null) ? null : to.cond();
		let = (between == null) ? null : between.trail();
		betweenEffect = (between == null) ? null : between.effect();
		dirnChoice = (directions != null) ? directions.directionsFunctions()
				: new Directions(AbsoluteDirection.Adjacent, null);

		// Stack
		this.stack = (stack == null) ? false : stack.booleanValue();

		trackName = track;
		toRule = (to == null || to.effect() == null) ? null : to.effect().condition();
	}

	//-------------------------------------------------------------------------

	@Override
	public Moves eval(final Context context)
	{
		final Moves moves = new BaseMoves(super.then());

		final int from = startLocationFn.eval(context);
		final int min = minFn.eval(context);
		if (from == Constants.OFF)
			return moves;

		if (trackName != null) 
			return slideByTrack(context);
		
		final int origFrom = context.from();
		final int origTo = context.to();
		final int origBetween = context.between();

		final Topology topology = context.topology();

		final TopologyElement fromV = topology.getGraphElements(type).get(from);
		context.setFrom(from);
		final List<AbsoluteDirection> directions = dirnChoice.convertToAbsolute(type, fromV, null, null, null,
				context);

		final int levelFrom = (levelFromFn == null) ? Constants.UNDEFINED : levelFromFn.eval(context);
		
		// If the level specified is less than 0, no move can be computed.
		if (levelFrom < Constants.UNDEFINED && levelFromFn != null)
			return moves;

		final int maxPathLength = limit.eval(context);

		if (fromCondition != null && !fromCondition.eval(context))
			return moves;

		for (final AbsoluteDirection direction : directions)
		{
			final List<Radial> radials = topology.trajectories().radials(type, from, direction);
			
			for (final Radial radial : radials)
			{
				final FastTIntArrayList betweenSites = new FastTIntArrayList();
				context.setBetween(origBetween);
				for (int toIdx = 1; toIdx < radial.steps().length && toIdx <= maxPathLength; toIdx++)
				{
					final int to = radial.steps()[toIdx].id();
					
					context.setTo(to);
					if (stopRule != null && stopRule.eval(context))
					{
						if (min <= toIdx)
						{
							Action action = null;
							Move move = null;
							// Check stack move.
							if (levelFrom == Constants.UNDEFINED && stack == false)
							{ 
								action = ActionMove.construct(type, from, Constants.UNDEFINED, type, to, Constants.OFF,Constants.UNDEFINED, Constants.OFF, Constants.OFF, false);
								action.setDecision(isDecision());
								move = new Move(action);
								move = MoveUtilities.chainRuleWithAction(context, sideEffect, move, true, false);
							}
							else if (levelFrom == Constants.UNDEFINED || stack == true)
							{
								final int level = (context.game().isStacking())
										? (stack) ? Constants.UNDEFINED
												: context.state().containerStates()[0].sizeStack(from, type) - 1
										: Constants.UNDEFINED;

								action = ActionMove.construct(type, from, level, type, to, Constants.OFF, Constants.UNDEFINED, Constants.OFF, Constants.OFF, stack);
								action.setDecision(true);
								move = new Move(action);

								// To add the levels to move a stack on the Move class (only for GUI)
								if (stack)
								{
									move.setLevelMinNonDecision(0);
									move.setLevelMaxNonDecision(context.state().containerStates()[0].sizeStack(from, type) - 1);
								}
								move = MoveUtilities.chainRuleWithAction(context, sideEffect, move, true, false);
							}
							else // Slide a piece at a specific level.
							{
								action = ActionMove.construct(type, from, levelFrom, type, to, Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED, Constants.OFF, stack);
								action.setDecision(true);
								move = new Move(action);
								move.setLevelMinNonDecision(levelFrom);
								move.setLevelMaxNonDecision(levelFrom);
								move = MoveUtilities.chainRuleWithAction(context, sideEffect, move, true, false);
							}
							
							// Trail piece
							if (let != null)
							{
								final int pieceToLet = let.eval(context);
								for (int i = 0; i < toIdx; i++)
								{
									final Action add = new ActionAdd(type, radial.steps()[i].id(), pieceToLet, 1, Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED, null);
									move.actions().add(add);
								}
							}

							// Moves on between sites
							if (betweenEffect != null)
								for (int i = 0; i < betweenSites.size(); i++)
								{
									context.setBetween(betweenSites.get(i));
									move = MoveUtilities.chainRuleWithAction(context, betweenEffect, move, true, false);
								}

							// Check to rule.
							if (toRule == null || toRule.eval(context))
							{
								MoveUtilities.chainRuleCrossProduct(context, moves, null, move, false);
								move.setFromNonDecision(from);
								move.setBetweenNonDecision(new FastTIntArrayList(betweenSites));
								move.setToNonDecision(to);
							}
							break;
						}
					}
					
					context.setBetween(to);
					if (!goRule.eval(context))
						break;

					if (min <= toIdx)
					{
						Action action = null;
						Move move = null;
						// Check stack move.
						if (levelFrom == Constants.UNDEFINED && stack == false)
						{
							action = ActionMove.construct(type, from, Constants.UNDEFINED, type, to, Constants.OFF, Constants.UNDEFINED, Constants.OFF, Constants.OFF, false);
							action.setDecision(isDecision());
							move = new Move(action);
							move = MoveUtilities.chainRuleWithAction(context, sideEffect, move, true, false);
						}
						else if (levelFrom == Constants.UNDEFINED || stack == true)
						{
							final int level = (context.game().isStacking())
									? (stack) ? Constants.UNDEFINED
											: context.state().containerStates()[0].sizeStack(from, type) - 1
									: Constants.UNDEFINED;

							action = ActionMove.construct(type, from, level, type, to, Constants.OFF, Constants.UNDEFINED, Constants.OFF, Constants.OFF, stack);
							action.setDecision(true);
							move = new Move(action);

							// to add the levels to move a stack on the Move class (only for GUI)
							if (stack)
							{
								move.setLevelMinNonDecision(0);
								move.setLevelMaxNonDecision(
										context.state().containerStates()[0].sizeStack(from, type) - 1);
							}
						}
						else // Step a piece at a specific level.
						{
							action = ActionMove.construct(type, from, levelFrom, type, to, Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED, Constants.OFF, stack);
							action.setDecision(true);
							move = new Move(action);
							move.setLevelMinNonDecision(levelFrom);
							move.setLevelMaxNonDecision(levelFrom);
						}

						// Trail piece.
						if (let != null)
						{
							final int pieceToLet = let.eval(context);
							for (int i = 0; i < toIdx; i++)
							{
								final Action add = new ActionAdd(type, radial.steps()[i].id(), pieceToLet, 1, Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED,
										null);
								move.actions().add(add);
							}
						}

						// Moves on between sites.
						if (betweenEffect != null)
							for (int i = 0; i < betweenSites.size(); i++)
							{
								context.setBetween(betweenSites.get(i));
								move = MoveUtilities.chainRuleWithAction(context, betweenEffect, move, true, false);
							}

						// Check to rule.
						if (toRule == null || (toRule.eval(context)))
						{
							MoveUtilities.chainRuleCrossProduct(context, moves, null, move, false);
							move.setFromNonDecision(from);
							move.setBetweenNonDecision(new FastTIntArrayList(betweenSites));
							move.setToNonDecision(to);
						}
					}

					betweenSites.add(to);
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

	private Moves slideByTrack(final Context context)
	{
		final Moves moves = new BaseMoves(super.then());

		// this is not a track
		if (preComputedTracks.size() == 0)
			return moves;
		
		final int from = startLocationFn.eval(context);
		
		final int origFrom = context.from();

		context.setFrom(from);

		if (fromCondition != null && !fromCondition.eval(context))
			return moves;

		final int origTo = context.to();
		final int origStep = context.between();

		final int min = minFn.eval(context);
		final int maxPathLength = limit.eval(context);

		for (final Track track : preComputedTracks)
		{
			for (int i = 0; i < track.elems().length; i++)
			{
				if (track.elems()[i].site == from)
				{
					int index = i;
					int nbBump = track.elems()[index].bump;
					int to = track.elems()[index].site;
					int nbElem = 1;
					while (track.elems()[index].next != Constants.OFF && nbElem < track.elems().length && nbElem <= maxPathLength)
					{
						to = track.elems()[index].next;

						context.setTo(to);

						if (nbBump > 0 || !trackName.equals("AllTracks"))
						{
							if (stopRule != null && stopRule.eval(context))
							{
								if (min <= nbElem)
								{
									final Action actionMove = ActionMove.construct(type, from, Constants.UNDEFINED, type, to, Constants.OFF, Constants.UNDEFINED, Constants.OFF, Constants.OFF, false);
									actionMove.setDecision(isDecision());
									Move thisAction = new Move(actionMove);
	
									thisAction = MoveUtilities.chainRuleWithAction(context, sideEffect, thisAction, true,
											false);
									MoveUtilities.chainRuleCrossProduct(context, moves, null, thisAction, false);
									thisAction.setFromNonDecision(from);
									thisAction.setToNonDecision(to);
									break;
								}
							}
							else
							{
								if (min <= nbElem)
								{
									context.setTo(to);
									if (toRule == null || (toRule.eval(context)))
									{
										final Action actionMove = ActionMove.construct(type, from, Constants.UNDEFINED, type, to, Constants.OFF, Constants.UNDEFINED, Constants.OFF, Constants.OFF, false);
										actionMove.setDecision(isDecision());
										final Move thisAction = new Move(actionMove);
										thisAction.setFromNonDecision(from);
										thisAction.setToNonDecision(to);
										MoveUtilities.chainRuleCrossProduct(context, moves, null, thisAction, false);
										thisAction.setFromNonDecision(from);
										thisAction.setToNonDecision(to);
									}
								}
							}
						}

						nbElem++;
						context.setBetween(to);
						if (!goRule.eval(context))
							break;

						index = track.elems()[index].nextIndex;
						nbBump += track.elems()[index].bump;
					}
				}
			}
		}
		
		context.setTo(origTo);
		context.setFrom(origFrom);
		context.setBetween(origStep);

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
		long gameFlags = super.gameFlags(game) | goRule.gameFlags(game);
		gameFlags |= GameType.UsesFromPositions;
		
		if (startLocationFn != null)
			gameFlags = gameFlags | startLocationFn.gameFlags(game);
		
		if (fromCondition != null)
			gameFlags |= fromCondition.gameFlags(game);

		if (limit != null)
			gameFlags = gameFlags | limit.gameFlags(game);
		
		if (stopRule != null)
			gameFlags = gameFlags | stopRule.gameFlags(game);
		
		if (toRule != null)
			gameFlags = gameFlags | toRule.gameFlags(game);
		
		if (let != null)
			gameFlags = gameFlags | let.gameFlags(game);
		
		if (sideEffect != null)
			gameFlags = gameFlags | sideEffect.gameFlags(game);

		if (betweenEffect != null)
			gameFlags = gameFlags | betweenEffect.gameFlags(game);

		if (levelFromFn != null)
			gameFlags |= levelFromFn.gameFlags(game);

		gameFlags |= SiteType.gameFlags(type);

		if (stack)
			gameFlags |= GameType.Stacking;

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

		concepts.set(Concept.LineOfSight.id(), true);

		concepts.or(goRule.concepts(game));

		if (isDecision())
		{
			concepts.set(Concept.SlideDecision.id(), true);
			if (goRule.concepts(game).get(Concept.IsEmpty.id()))
				concepts.set(Concept.SlideDecisionToEmpty.id(), true);
			if (goRule.concepts(game).get(Concept.IsFriend.id()))
				concepts.set(Concept.SlideDecisionToFriend.id(), true);
			if (goRule.concepts(game).get(Concept.IsEnemy.id()))
				concepts.set(Concept.SlideDecisionToEnemy.id(), true);
			if (goRule instanceof BooleanConstant.TrueConstant)
			{
				concepts.set(Concept.SlideDecisionToEmpty.id(), true);
				concepts.set(Concept.SlideDecisionToFriend.id(), true);
				concepts.set(Concept.SlideDecisionToEnemy.id(), true);
			}
		}
		else
			concepts.set(Concept.SlideEffect.id(), true);
			

		if (startLocationFn != null)
			concepts.or(startLocationFn.concepts(game));

		if (fromCondition != null)
			concepts.or(fromCondition.concepts(game));

		if (limit != null)
			concepts.or(limit.concepts(game));

		if (stopRule != null)
			concepts.or(stopRule.concepts(game));

		if (toRule != null)
		{
			concepts.or(toRule.concepts(game));
			if (isDecision())
			{
				if (toRule.concepts(game).get(Concept.IsEmpty.id()))
					concepts.set(Concept.SlideDecisionToEmpty.id(), true);
				if (toRule.concepts(game).get(Concept.IsFriend.id()))
					concepts.set(Concept.SlideDecisionToFriend.id(), true);
				if (toRule.concepts(game).get(Concept.IsEnemy.id()))
					concepts.set(Concept.SlideDecisionToEnemy.id(), true);
				if (toRule instanceof BooleanConstant.TrueConstant)
				{
					concepts.set(Concept.SlideDecisionToEmpty.id(), true);
					concepts.set(Concept.SlideDecisionToFriend.id(), true);
					concepts.set(Concept.SlideDecisionToEnemy.id(), true);
				}
			}
		}

		if (let != null)
			concepts.or(let.concepts(game));

		if (sideEffect != null)
			concepts.or(sideEffect.concepts(game));

		if (then() != null)
			concepts.or(then().concepts(game));

		if (dirnChoice != null)
			concepts.or(dirnChoice.concepts(game));

		if (betweenEffect != null)
			concepts.or(betweenEffect.concepts(game));

		if (levelFromFn != null)
			concepts.or(levelFromFn.concepts(game));

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
		writeEvalContext.or(goRule.writesEvalContextRecursive());

		if (startLocationFn != null)
			writeEvalContext.or(startLocationFn.writesEvalContextRecursive());

		if (fromCondition != null)
			writeEvalContext.or(fromCondition.writesEvalContextRecursive());

		if (limit != null)
			writeEvalContext.or(limit.writesEvalContextRecursive());

		if (stopRule != null)
			writeEvalContext.or(stopRule.writesEvalContextRecursive());

		if (toRule != null)
			writeEvalContext.or(toRule.writesEvalContextRecursive());

		if (let != null)
			writeEvalContext.or(let.writesEvalContextRecursive());

		if (sideEffect != null)
			writeEvalContext.or(sideEffect.writesEvalContextRecursive());

		if (then() != null)
			writeEvalContext.or(then().writesEvalContextRecursive());

		if (betweenEffect != null)
			writeEvalContext.or(betweenEffect.writesEvalContextRecursive());

		if (levelFromFn != null)
			writeEvalContext.or(levelFromFn.writesEvalContextRecursive());

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
		readEvalContext.or(goRule.readsEvalContextRecursive());

		if (startLocationFn != null)
			readEvalContext.or(startLocationFn.readsEvalContextRecursive());

		if (fromCondition != null)
			readEvalContext.or(fromCondition.readsEvalContextRecursive());

		if (limit != null)
			readEvalContext.or(limit.readsEvalContextRecursive());

		if (stopRule != null)
			readEvalContext.or(stopRule.readsEvalContextRecursive());

		if (toRule != null)
			readEvalContext.or(toRule.readsEvalContextRecursive());

		if (let != null)
			readEvalContext.or(let.readsEvalContextRecursive());

		if (sideEffect != null)
			readEvalContext.or(sideEffect.readsEvalContextRecursive());

		if (betweenEffect != null)
			readEvalContext.or(betweenEffect.readsEvalContextRecursive());

		if (levelFromFn != null)
			readEvalContext.or(levelFromFn.readsEvalContextRecursive());

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

		if (fromCondition != null)
			missingRequirement |= fromCondition.missingRequirement(game);

		missingRequirement |= goRule.missingRequirement(game);

		if (startLocationFn != null)
			missingRequirement |= startLocationFn.missingRequirement(game);

		if (limit != null)
			missingRequirement |= limit.missingRequirement(game);

		if (stopRule != null)
			missingRequirement |= stopRule.missingRequirement(game);

		if (toRule != null)
			missingRequirement |= toRule.missingRequirement(game);

		if (let != null)
			missingRequirement |= let.missingRequirement(game);

		if (sideEffect != null)
			missingRequirement |= sideEffect.missingRequirement(game);

		if (betweenEffect != null)
			missingRequirement |= betweenEffect.missingRequirement(game);

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

		willCrash |= goRule.willCrash(game);

		if (fromCondition != null)
			willCrash |= fromCondition.willCrash(game);

		if (startLocationFn != null)
			willCrash |= startLocationFn.willCrash(game);

		if (limit != null)
			willCrash |= limit.willCrash(game);

		if (stopRule != null)
			willCrash |= stopRule.willCrash(game);

		if (toRule != null)
			willCrash |= toRule.willCrash(game);

		if (let != null)
			willCrash |= let.willCrash(game);

		if (sideEffect != null)
			willCrash |= sideEffect.willCrash(game);

		if (betweenEffect != null)
			willCrash |= betweenEffect.willCrash(game);

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
		
		goRule.preprocess(game);
		if (startLocationFn != null)
			startLocationFn.preprocess(game);
		if (fromCondition != null)
			fromCondition.preprocess(game);
		if (limit != null)
			limit.preprocess(game);
		if (let != null)
			let.preprocess(game);
		if (stopRule != null)
			stopRule.preprocess(game);
		if (sideEffect != null)
			sideEffect.preprocess(game);
		if (toRule != null)
			toRule.preprocess(game);
		if (betweenEffect != null)
			betweenEffect.preprocess(game);
		if (levelFromFn != null)
			levelFromFn.preprocess(game);
		if (dirnChoice != null)
			dirnChoice.preprocess(game);

		if (trackName != null)
		{
			preComputedTracks = new ArrayList<Track>();
			for (final Track t : game.board().tracks())
				if (t.name().equals(trackName) || trackName.equals("AllTracks"))
					preComputedTracks.add(t);
		}
	}

	//-------------------------------------------------------------------------
	
	/**
	 * @return The rule that tells us we're allowed to continue sliding
	 */
	public BooleanFunction goRule()
	{
		return goRule;
	}
	
	/**
	 * @return The rule that tells us we have to stop sliding
	 */
	public BooleanFunction stopRule()
	{
		return stopRule;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{
		String text = "";
		if(startLocationFn.toEnglish(game).equals(""))
			text = "slide in the " + dirnChoice.toEnglish(game) + " direction through " + goRule.toEnglish(game);
		else 
			text = "slide from "+ startLocationFn.toEnglish(game) + " in the " + dirnChoice.toEnglish(game) + " direction through " + goRule.toEnglish(game);
		
		String thenString = "";
		if (then() != null)
			thenString = " then " + then().toEnglish(game);
		
		text += thenString;
				
		return text;
	}

	//-------------------------------------------------------------------------

}
