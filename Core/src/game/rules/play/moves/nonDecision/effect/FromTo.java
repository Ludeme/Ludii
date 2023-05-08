package game.rules.play.moves.nonDecision.effect;

import java.util.BitSet;

import annotations.Name;
import annotations.Opt;
import game.Game;
import game.equipment.component.Component;
import game.functions.booleans.BooleanConstant;
import game.functions.booleans.BooleanFunction;
import game.functions.intArray.state.Rotations;
import game.functions.ints.IntFunction;
import game.functions.ints.board.Id;
import game.functions.region.RegionFunction;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import game.types.board.SiteType;
import game.types.play.RoleType;
import game.types.state.GameType;
import game.util.moves.From;
import game.util.moves.To;
import gnu.trove.list.array.TIntArrayList;
import main.Constants;
import other.action.Action;
import other.action.move.ActionCopy;
import other.action.move.ActionMoveN;
import other.action.move.ActionSubStackMove;
import other.action.move.move.ActionMove;
import other.action.state.ActionSetRotation;
import other.concept.Concept;
import other.context.Context;
import other.context.EvalContextData;
import other.move.Move;
import other.move.MoveUtilities;
import other.state.container.ContainerState;

/**
 * Moves a piece from one site to another, possibly in another container, with
 * no direction link between the ``from'' and ``to'' sites.
 * 
 * @author Eric.Piette
 * 
 * @remarks If the ``copy'' parameter is set, then a copy of the piece is
 *          duplicated at the ``to'' site rather than actually moving there.
 */
public final class FromTo extends Effect
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------
	
	/** Loc from. */
	private final IntFunction locFrom;

	/** Level. */
	private final IntFunction levelFrom;

	/** Count. */
	private final IntFunction countFn;

	/** Loc to. */
	private final IntFunction locTo;

	/** Rotation to. */
	private final Rotations rotationTo;

	/** Level. */
	private final IntFunction levelTo;

	/** Region from. */
	private final RegionFunction regionFrom;

	/** Region to. */
	private final RegionFunction regionTo;

	/** From Condition Rule. */
	private final BooleanFunction fromCondition;
	
	/** Move Rule. */
	private final BooleanFunction moveRule;

	/** Capture Rule. */
	private final BooleanFunction captureRule;

	/** Capture Effect. */
	private final Moves captureEffect;

	/** The mover of this moves for simultaneous game (e.g. Rock-Paper-Scissors). */
	private final RoleType mover;

	/** To move a complete stack. */
	private final boolean stack;

	/** Cell/Edge/Vertex for the origin. */
	private SiteType typeFrom;

	/** Cell/Edge/Vertex for the target. */
	private SiteType typeTo;

	/** If true, we do not move the piece, we copy it. */
	private final BooleanFunction copy;

	//-------------------------------------------------------------------------

	/** True if the to type was defined explicitly */
	private final boolean typeToDefined;

	/**
	 * @param from  The data of the ``from'' location [(from)].
	 * @param to    The data of the ``to'' location.
	 * @param count The number of pieces to move.
	 * @param copy  Whether to duplicate the piece rather than moving it [False].
	 * @param stack To move a complete stack [False].
	 * @param mover The mover of the move.
	 * @param then  The moves applied after that move is applied.
	 * 
	 * @example (fromTo (from (last To)) (to (last From)))
	 *
	 * @example (fromTo (from (handSite Mover)) (to (sites Empty)))
	 * 
	 * @example (fromTo (from (to)) (to (sites Empty)) count:(count at:(to)))
	 * 
	 * @example (fromTo (from (handSite Shared)) (to (sites Empty)) copy:True )
	 */
	public FromTo
	(
		 			final From            from,
		 			final To              to,
		@Opt @Name  final IntFunction     count,
		@Opt @Name  final BooleanFunction copy,
		@Opt @Name  final Boolean         stack,
		@Opt 		final RoleType        mover,
		@Opt		final Then            then
	) 
	{ 
		super(then);

		locFrom = from.loc();
		fromCondition = from.cond();
		levelFrom = from.level();
		countFn = count;
		locTo = to.loc();
		levelTo = to.level();
		regionFrom = from.region();
		regionTo = to.region();
		moveRule = (to.cond() == null) ? new BooleanConstant(true) : to.cond();
		captureRule = (to.effect() == null) ? null : to.effect().condition();
		captureEffect = (to.effect() == null) ? null : to.effect().effect();
		this.mover = mover;
		rotationTo = to.rotations();
		this.stack = (stack == null) ? false : stack.booleanValue();
		typeFrom = from.type();
		typeTo = to.type();
		typeToDefined = (to.type() != null);
		this.copy = (copy == null) ? new BooleanConstant(false) : copy;
	}

	//-------------------------------------------------------------------------

	@Override
	public Moves eval(final Context context)
	{
		final int[] sitesFrom = (regionFrom == null) ? new int[] { locFrom.eval(context) } : regionFrom.eval(context).sites();

		final int origFrom = context.from();
		final int origTo = context.to();

		final BaseMoves moves = new BaseMoves(super.then());
		final boolean stackingGame = context.currentInstanceContext().game().isStacking();

		for (final int from : sitesFrom)
		{
			if (from > Constants.OFF)
			{
				final int cidFrom = from >= context.containerId().length ? 0 : context.containerId()[from];
				
				SiteType realTypeFrom = typeFrom;
				if (cidFrom > 0)
					realTypeFrom = SiteType.Cell;
				else if (realTypeFrom == null)
					realTypeFrom = context.board().defaultSite();

				final ContainerState cs = context.containerState(cidFrom);
				final int what = cs.what(from, realTypeFrom);

				if (what <= 0)
					continue;

				context.setFrom(from);
				
				final boolean copyTo = copy.eval(context);
				if (fromCondition != null && !fromCondition.eval(context))
					continue;

				final int[] sitesTo = (regionTo == null) ? new int[] { locTo.eval(context) } : regionTo.eval(context).sites();
				final int count = (countFn == null) ? 1 : countFn.eval(context);
				context.setFrom(origFrom);

				final Component component = context.components()[what];
				
				// Special case for LargePiece.
				if (component != null && component.isLargePiece())
				{
					final BaseMoves movesLargePiece = evalLargePiece(context, from, sitesTo);
					for (final Move m : movesLargePiece.moves())
						moves.moves().add(m);
					continue;
				}

				for (final int to : sitesTo)
				{					
					if (to > Constants.OFF)
					{
						// Get the right container id for 'to' and the right site type of the 'to'.
						int cidTo;
						SiteType realTypeTo = typeTo;
						if (typeToDefined)
						{
							cidTo = (!typeTo.equals(SiteType.Cell)) ? 0 : context.containerId()[to];
						}
						else
						{
							cidTo = to >= context.containerId().length ? 0 : context.containerId()[to];
							if (cidTo > 0)
								realTypeTo = SiteType.Cell;
							else if (realTypeTo == null)
								realTypeTo = context.board().defaultSite();
						}
						final ContainerState csTo = context.containerState(cidTo);

						// Compute the right action to move the piece(s).
						final Action actionMove;
						if (levelTo != null)
						{
							if (!stack)
							{
								if (levelFrom == null)
								{
									actionMove = ActionMove.construct
											(
												realTypeFrom, 
												from, 
												Constants.UNDEFINED, 
												realTypeTo, 
												to,
												levelTo.eval(context), 
												Constants.UNDEFINED,
												Constants.UNDEFINED,
												Constants.OFF, 
												false
											);
									actionMove.setLevelFrom(cs.sizeStack(from, typeFrom) - 1);
								}
								else
								{
									actionMove = ActionMove.construct
											(
												realTypeFrom, 
												from, 
												levelFrom.eval(context),
												realTypeTo,
												to, 
												levelTo.eval(context), 
												Constants.UNDEFINED,
												Constants.UNDEFINED, 
												Constants.OFF, 
												false
											);
								}
							}
							else
							{
								actionMove = ActionMove.construct
										(
											realTypeFrom, 
											from, 
											Constants.UNDEFINED, 
											realTypeTo, 
											to,
											levelTo.eval(context), 
											Constants.UNDEFINED,
											Constants.UNDEFINED, 
											Constants.OFF, 
											true
										);
								actionMove.setLevelFrom(0);
							}
						}
						else if (levelFrom == null && countFn == null)
						{
							if (copyTo) {
								actionMove = new ActionCopy
										(
											realTypeFrom, 
											from, 
											Constants.UNDEFINED, 
											realTypeTo,
											to, 
											Constants.OFF, 
											Constants.UNDEFINED, 
											Constants.OFF, 
											Constants.OFF, 
											false
										);
							}
							else 
							{
								actionMove = ActionMove.construct
									(
										realTypeFrom, 
										from, 
										Constants.UNDEFINED, 
										realTypeTo, 
										to,
										Constants.OFF, 
										Constants.UNDEFINED, 
										Constants.OFF, 
										Constants.OFF,
										stack
									);
							}
							
							if(stack)
								actionMove.setLevelFrom(0);
							else
								actionMove.setLevelFrom(cs.sizeStack(from, typeFrom) - 1);
						}
						else if (levelFrom != null)
						{
							actionMove = ActionMove.construct
									(
										realTypeFrom, 
										from, 
										levelFrom.eval(context), 
										realTypeTo, 
										to,
										Constants.UNDEFINED, 
										Constants.UNDEFINED, 
										Constants.UNDEFINED, 
										Constants.OFF,
										false
									);
						}
						else
						{
							if (!stackingGame && !stack)
							{
								actionMove = new ActionMoveN(realTypeFrom, from, realTypeTo, to, count);
								actionMove.setLevelFrom(cs.sizeStack(from, typeFrom) - 1);
							}
							// Move a sub stack.
							else
							{
								actionMove = new ActionSubStackMove(realTypeFrom, from, realTypeTo, to, count);
								actionMove.setLevelFrom(cs.sizeStack(from, realTypeFrom) - (count));
								actionMove.setLevelTo(csTo.sizeStack(to, realTypeTo));
							}
						}

						if (isDecision())
							actionMove.setDecision(true);
						
						context.setFrom(from);
						context.setTo(to);
						
						if (moveRule.eval(context))
						{
							context.setFrom(origFrom);
							Move move = new Move(actionMove);
							move.setFromNonDecision(from);
							move.setToNonDecision(to);
							
							// to add the levels to move a stack on the Move class (only for GUI)
							if (context.game().isStacking()) 
							{
								if (levelFrom == null)
								{
									move.setLevelMinNonDecision(cs.sizeStack(from, realTypeFrom) - 1);
									move.setLevelMaxNonDecision(cs.sizeStack(from, realTypeFrom) - 1);
								}
								else
								{
									move.setLevelMinNonDecision(levelFrom.eval(context));
									move.setLevelMaxNonDecision(levelFrom.eval(context));
								}
								
								// To add the levels to move a stack on the Move class (only for GUI)
								if (stack)
								{
									move.setLevelMinNonDecision(0);
									move.setLevelMaxNonDecision(cs.sizeStack(from, realTypeFrom) - 1);
									move.setLevelFrom(0);
								}
							}
						
							if (rotationTo != null)
							{
								final int[] rotations = rotationTo.eval(context);
								for (final int rotation : rotations)
								{
									final Move moveWithRotation = new Move(move);
									final Action actionRotation = new ActionSetRotation(typeTo, to, rotation);
									moveWithRotation.actions().add(actionRotation);
									moves.moves().add(moveWithRotation);
								}
							}
							else if (captureRule == null || (captureRule != null && (captureRule.eval(context))))
							{
								context.setFrom(from);
								context.setTo(to);
								move = MoveUtilities.chainRuleWithAction(context, captureEffect, move, true, false);

								move.setFromNonDecision(from);
								move.setToNonDecision(to);
								MoveUtilities.chainRuleCrossProduct(context, moves, null, move, false);
							}
							else
							{
								moves.moves().add(move);
							}
						}
					}
				}
			}
		}

		context.setTo(origTo);
		context.setFrom(origFrom);

		// We set the mover to the move.
		final int moverToSet = (mover == null) ? context.state().mover() : new Id(null, mover).eval(context);
		
		MoveUtilities.setGeneratedMovesData(moves.moves(), this, moverToSet);

		return moves;
	}

	//-------------------------------------------------------------------------

	private BaseMoves evalLargePiece(final Context context, final int from, final int[] sitesTo)
	{
		final int origFrom = context.from();
		final int origTo = context.to();

		final BaseMoves moves = new BaseMoves(super.then());

		final ContainerState cs = context.containerState(context.containerId()[from]);
		final int what = cs.what(from, typeFrom);
		final int localState = cs.state(from, typeFrom);
		final Component largePiece = context.components()[what];
		final int nbPossibleStates = largePiece.walk().length * 4;
		final TIntArrayList currentLocs = largePiece.locs(context, from, localState, context.topology());
		
		final TIntArrayList newSitesTo = new TIntArrayList();
		for (int i = 0; i < sitesTo.length; i++)
			newSitesTo.add(sitesTo[i]);
		for (int i = 1; i < currentLocs.size(); i++)
			newSitesTo.add(currentLocs.getQuick(i));

		for (int index = 0; index < newSitesTo.size(); index++)
		{
			final int to = newSitesTo.getQuick(index);
			for (int state = 0; state < nbPossibleStates; state++)
			{
				final TIntArrayList locs = largePiece.locs(context, to, state, context.topology());
				if (locs == null || locs.size() <= 0)
					continue;
				final ContainerState csTo = context.containerState(context.containerId()[locs.getQuick(0)]);
				boolean valid = true;
				for (int i = 0; i < locs.size(); i++)
				{
					if (!largePiece.isDomino())
					{
						if (!newSitesTo.contains(locs.getQuick(i)) && locs.getQuick(i) != from)
						{
							valid = false;
							break;
						}
					}
					else if (!csTo.isPlayable(locs.getQuick(i)) && context.trial().moveNumber() > 0)
					{
						valid = false;
						break;
					}
				}
				if (valid && (from != to || (from == to) && localState != state))
				{
					final Action actionMove = ActionMove.construct
						(
							typeFrom, 
							from, 
							Constants.UNDEFINED, 
							typeTo, 
							to,
							Constants.OFF,
							state, 
							Constants.OFF, 
							Constants.OFF,
							false
						);
					
					if (isDecision())
						actionMove.setDecision(true);
					
					Move move = new Move(actionMove);
					move = MoveUtilities.chainRuleWithAction(context, captureEffect, move, true, false);

					move.setFromNonDecision(from);
					move.setToNonDecision(to);
					move.setStateNonDecision(state);
					MoveUtilities.chainRuleCrossProduct(context, moves, null, move, false);
				}
			}
		}

		context.setTo(origTo);
		context.setFrom(origFrom);

		// Store the Moves in the computed moves.
		for (int j = 0; j < moves.moves().size(); j++)
			moves.moves().get(j).setMovesLudeme(this);

		return moves;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = super.gameFlags(game);
		gameFlags |= GameType.UsesFromPositions;
		
		gameFlags |= SiteType.gameFlags(typeFrom);
		gameFlags |= SiteType.gameFlags(typeTo);
		
		if (locFrom != null)
			gameFlags |= locFrom.gameFlags(game);

		if (locTo != null)
			gameFlags |= locTo.gameFlags(game);

		if (fromCondition != null)
			gameFlags |= fromCondition.gameFlags(game);

		if (regionFrom != null)
			gameFlags |= regionFrom.gameFlags(game);

		if (captureRule != null)
			gameFlags |= captureRule.gameFlags(game);

		if (moveRule != null)
			gameFlags |= moveRule.gameFlags(game);

		if (levelTo != null)
			gameFlags |= levelTo.gameFlags(game);

		if (countFn != null)
			gameFlags |= countFn.gameFlags(game);

		if (captureEffect != null)
			gameFlags |= captureEffect.gameFlags(game);

		if (regionTo != null)
			gameFlags |= regionTo.gameFlags(game);
		
		if (levelFrom != null || stack)
			gameFlags |= GameType.Stacking;

		if (rotationTo != null)
			gameFlags |= rotationTo.gameFlags(game) | GameType.Rotation;
		
		gameFlags |= copy.gameFlags(game);

		if (then() != null)
			gameFlags |= then().gameFlags(game);

		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(super.concepts(game));
		concepts.or(SiteType.concepts(typeFrom));
		concepts.or(SiteType.concepts(typeTo));
		
		if(isDecision())
		{
			concepts.set(Concept.FromToDecision.id(), true);
			if (moveRule.concepts(game).get(Concept.IsEmpty.id()))
				concepts.set(Concept.FromToDecisionEmpty.id(), true);
			if (moveRule.concepts(game).get(Concept.IsFriend.id()))
				concepts.set(Concept.FromToDecisionFriend.id(), true);
			if (moveRule.concepts(game).get(Concept.IsEnemy.id()))
				concepts.set(Concept.FromToDecisionEnemy.id(), true);
			if (moveRule instanceof BooleanConstant.TrueConstant)
			{
				concepts.set(Concept.FromToDecisionEmpty.id(), true);
				concepts.set(Concept.FromToDecisionFriend.id(), true);
				concepts.set(Concept.FromToDecisionEnemy.id(), true);
			}
		}
		else
			concepts.set(Concept.FromToEffect.id(), true);

		if (isDecision())
			concepts.set(Concept.FromToDecision.id(), true);

		if (locFrom != null)
			concepts.or(locFrom.concepts(game));

		if (fromCondition != null)
			concepts.or(fromCondition.concepts(game));

		if (locTo != null)
			concepts.or(locTo.concepts(game));

		if (regionFrom != null)
			concepts.or(regionFrom.concepts(game));

		if (captureRule != null)
			concepts.or(captureRule.concepts(game));

		if (levelTo != null)
			concepts.or(levelTo.concepts(game));

		if (countFn != null)
			concepts.or(countFn.concepts(game));

		if (captureEffect != null)
			concepts.or(captureEffect.concepts(game));

		if (regionTo != null)
			concepts.or(regionTo.concepts(game));

		if (rotationTo != null)
			concepts.or(rotationTo.concepts(game));

		concepts.or(copy.concepts(game));

		if (then() != null)
			concepts.or(then().concepts(game));

		// We check if that's effectively a capture (remove or fromTo).
		if (captureEffect != null)
			if (captureEffect.concepts(game).get(Concept.RemoveEffect.id())
					|| captureEffect.concepts(game).get(Concept.FromToEffect.id()))
				concepts.set(Concept.ReplacementCapture.id(), true);

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = writesEvalContextFlat();
		writeEvalContext.or(super.writesEvalContextRecursive());

		if (locFrom != null)
			writeEvalContext.or(locFrom.writesEvalContextRecursive());

		if (fromCondition != null)
			writeEvalContext.or(fromCondition.writesEvalContextRecursive());

		if (locTo != null)
			writeEvalContext.or(locTo.writesEvalContextRecursive());

		if (regionFrom != null)
			writeEvalContext.or(regionFrom.writesEvalContextRecursive());

		if (captureRule != null)
			writeEvalContext.or(captureRule.writesEvalContextRecursive());

		if (moveRule != null)
			writeEvalContext.or(moveRule.writesEvalContextRecursive());

		if (levelTo != null)
			writeEvalContext.or(levelTo.writesEvalContextRecursive());

		if (countFn != null)
			writeEvalContext.or(countFn.writesEvalContextRecursive());

		if (captureEffect != null)
			writeEvalContext.or(captureEffect.writesEvalContextRecursive());

		if (regionTo != null)
			writeEvalContext.or(regionTo.writesEvalContextRecursive());

		if (rotationTo != null)
			writeEvalContext.or(rotationTo.writesEvalContextRecursive());

		writeEvalContext.or(copy.writesEvalContextRecursive());

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

		if (locFrom != null)
			readEvalContext.or(locFrom.readsEvalContextRecursive());

		if (fromCondition != null)
			readEvalContext.or(fromCondition.readsEvalContextRecursive());

		if (locTo != null)
			readEvalContext.or(locTo.readsEvalContextRecursive());

		if (regionFrom != null)
			readEvalContext.or(regionFrom.readsEvalContextRecursive());

		if (captureRule != null)
			readEvalContext.or(captureRule.readsEvalContextRecursive());

		if (moveRule != null)
			readEvalContext.or(moveRule.readsEvalContextRecursive());

		if (levelTo != null)
			readEvalContext.or(levelTo.readsEvalContextRecursive());

		if (countFn != null)
			readEvalContext.or(countFn.readsEvalContextRecursive());

		if (captureEffect != null)
			readEvalContext.or(captureEffect.readsEvalContextRecursive());

		if (regionTo != null)
			readEvalContext.or(regionTo.readsEvalContextRecursive());

		if (rotationTo != null)
			readEvalContext.or(rotationTo.readsEvalContextRecursive());

		readEvalContext.or(copy.readsEvalContextRecursive());

		if (then() != null)
			readEvalContext.or(then().readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= super.missingRequirement(game);

		if (locFrom != null)
			missingRequirement |= locFrom.missingRequirement(game);

		if (fromCondition != null)
			missingRequirement |= fromCondition.missingRequirement(game);

		if (locTo != null)
		missingRequirement |= locTo.missingRequirement(game);

		if (regionFrom != null)
			missingRequirement |= regionFrom.missingRequirement(game);

		if (captureRule != null)
			missingRequirement |= captureRule.missingRequirement(game);

		if (moveRule != null)
			missingRequirement |= moveRule.missingRequirement(game);

		if (levelTo != null)
			missingRequirement |= levelTo.missingRequirement(game);

		if (countFn != null)
			missingRequirement |= countFn.missingRequirement(game);

		if (captureEffect != null)
			missingRequirement |= captureEffect.missingRequirement(game);

		if (regionTo != null)
			missingRequirement |= regionTo.missingRequirement(game);

		if (rotationTo != null)
			missingRequirement |= rotationTo.missingRequirement(game);

		missingRequirement |= copy.missingRequirement(game);

		if (then() != null)
			missingRequirement |= then().missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= super.willCrash(game);

		if (locFrom != null)
			willCrash |= locFrom.willCrash(game);

		if (fromCondition != null)
			willCrash |= fromCondition.willCrash(game);

		if (locTo != null)
			willCrash |= locTo.willCrash(game);

		if (regionFrom != null)
			willCrash |= regionFrom.willCrash(game);

		if (captureRule != null)
			willCrash |= captureRule.willCrash(game);

		if (moveRule != null)
			willCrash |= moveRule.willCrash(game);

		if (levelTo != null)
			willCrash |= levelTo.willCrash(game);

		if (countFn != null)
			willCrash |= countFn.willCrash(game);

		if (captureEffect != null)
			willCrash |= captureEffect.willCrash(game);

		if (regionTo != null)
			willCrash |= regionTo.willCrash(game);

		if (rotationTo != null)
			willCrash |= rotationTo.willCrash(game);

		willCrash |= copy.willCrash(game);

		if (then() != null)
			willCrash |= then().willCrash(game);
		return willCrash;
	}

	@Override
	public boolean isStatic()
	{
		boolean isStatic = true;
		if (locFrom != null)
			isStatic = isStatic && locFrom.isStatic();

		if (fromCondition != null)
			isStatic = isStatic && fromCondition.isStatic();

		if (locTo != null)
			isStatic = isStatic && locTo.isStatic();

		if (regionFrom != null)
			isStatic = isStatic && regionFrom.isStatic();

		if (regionTo != null)
			isStatic = isStatic && regionTo.isStatic();
		
		isStatic = isStatic && copy.isStatic();
		
		return isStatic;
	}
	
	@Override
	public void preprocess(final Game game)
	{
		if (typeFrom == null)
			typeFrom = game.board().defaultSite();

		if (typeTo == null)
			typeTo = game.board().defaultSite();

		super.preprocess(game);
		
		if (locFrom != null)
			locFrom.preprocess(game);
		
		if (fromCondition != null)
			fromCondition.preprocess(game);

		if (locTo != null)
			locTo.preprocess(game);
		
		if (regionFrom != null)
			regionFrom.preprocess(game);
		
		if (regionTo != null)
			regionTo.preprocess(game);

		if (rotationTo != null)
			rotationTo.preprocess(game);

		if (levelFrom != null)
			levelFrom.preprocess(game);

		if (countFn != null)
			countFn.preprocess(game);

		if (levelTo != null)
			levelTo.preprocess(game);

		if (moveRule != null)
			moveRule.preprocess(game);

		if (captureRule != null)
			captureRule.preprocess(game);

		if (captureEffect != null)
			captureEffect.preprocess(game);
		
		copy.preprocess(game);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Location to move from
	 */
	public IntFunction locFrom()
	{
		return locFrom;
	}
	
	/**
	 * @return Location to move to
	 */
	public IntFunction locTo()
	{
		return locTo;
	}
	
	/**
	 * @return Region to move from
	 */
	public RegionFunction regionFrom()
	{
		return regionFrom;
	}
	
	/**
	 * @return Region to move to
	 */
	public RegionFunction regionTo()
	{
		return regionTo;
	}
	
	/**
	 * @return Move rule
	 */
	public BooleanFunction moveRule()
	{
		return moveRule;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{
		final SiteType realTypeFrom = (typeFrom != null) ? typeFrom : game.board().defaultSite();
		String englishString = "from " + realTypeFrom.name().toLowerCase() + 
								(regionFrom == null ? "" : " in " + regionFrom.toEnglish(game)) +
								(locFrom == null ? "" : " in " + locFrom.toEnglish(game)) +
								(levelFrom == null ? "" : " " + levelFrom.toEnglish(game)) + 
								(fromCondition == null ? "" : " if " + fromCondition.toEnglish(game));
		

		final SiteType realTypeTo = (typeTo != null) ? typeTo : game.board().defaultSite();
		if (regionTo != null)
			englishString += " to " + realTypeTo.name().toLowerCase() + 
								" in " + regionTo.toEnglish(game) + 
								(levelTo == null ? "" : " " + levelTo.toEnglish(game));
		
		if (locTo != null)
			englishString += " to " + realTypeTo.name().toLowerCase() + 
								" " + locTo.toEnglish(game) + 
								(levelTo == null ? "" : " " + levelTo.toEnglish(game));
		
		if (moveRule != null)
			englishString += " moveRule: " + moveRule.toEnglish(game);
		
		if (captureRule != null)
			englishString += " captureRule: " + captureRule.toEnglish(game);
		
		if (captureEffect != null)
			englishString += " captureEffect: " + captureEffect.toEnglish(game);
		
		String thenString = "";
		if (then() != null)
			thenString = " then " + then().toEnglish(game);
		
		englishString += thenString;
		
		return englishString;
	}
	
	//-------------------------------------------------------------------------
}
