package game.rules.play.moves.nonDecision.effect;

import java.util.BitSet;

import annotations.Name;
import annotations.Opt;
import game.Game;
import game.equipment.component.Component;
import game.functions.booleans.BooleanConstant;
import game.functions.booleans.BooleanFunction;
import game.functions.ints.IntConstant;
import game.functions.ints.IntFunction;
import game.functions.ints.iterator.From;
import game.functions.region.RegionFunction;
import game.functions.region.sites.Sites;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import game.types.board.SiteType;
import game.types.board.StepType;
import game.types.state.GameType;
import game.util.directions.CompassDirection;
import game.util.moves.To;
import main.Constants;
import other.ContainerId;
import other.action.move.move.ActionMove;
import other.concept.Concept;
import other.context.Context;
import other.context.EvalContextData;
import other.move.Move;
import other.move.MoveUtilities;
import other.topology.TopologyElement;

/**
 * Allows a player to leap a piece to sites defined by walks through the board graph.
 * 
 * @author Eric.Piette and cambolbro
 * 
 * @remarks Use this ludeme to make leaping moves to pre-defined destination sites 
 *          that do not care about intervening pieces, such as knight moves in Chess.
 */
public final class Leap extends Effect
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Location of the piece. */
	private final IntFunction startLocationFn;

	/** From Condition Rule. */
	private final BooleanFunction fromCondition;

	/** The specific walk of the move. */
	private final RegionFunction walk;

	/** If the leap has to be only in a forward direction. */
	private final BooleanFunction forward;

	/** The rule to continue the move */
	private final BooleanFunction goRule;

	/** The Move applied on the location reached. */
	private final Moves sideEffect;

	/** Cell/Edge/Vertex. */
	private SiteType type;

	//-------------------------------------------------------------------------

	/**
	 * @param from      The site to leap from [(from)].
	 * @param walk      The walk that defines the landing site(s).
	 * @param forward   Whether to only keep moves that land forwards [False].
	 * @param rotations Whether to apply the leap to all rotations [True].
	 * @param to        Details about the site to move to.
	 * @param then      Moves to apply after the leap.
	 * 
	 * @example (leap { {F F R F} {F F L F} } (to if:(or (is Empty (to)) (is Enemy
	 *          (who at:(to)))) (apply (if (is Enemy (who at:(to))) (remove (to) ) )
	 *          ) ) )
	 */
	public Leap
	(
		@Opt       final game.util.moves.From from,
			       final StepType[][]         walk,
		@Opt @Name final BooleanFunction      forward,
		@Opt @Name final BooleanFunction      rotations,
	 		   	   final To                   to,
		@Opt 	   final Then                 then
	)
	{
		super(then);
		startLocationFn = (from == null) ? new From(null) : from.loc();
		fromCondition = (from == null) ? null : from.cond();
		type = (from == null) ? null : from.type();

		this.walk = Sites.construct(null, startLocationFn, walk, rotations);
		this.forward = (forward == null) ? new BooleanConstant(false) : forward;
		goRule = to.cond();
		sideEffect = to.effect();
	}

	//-------------------------------------------------------------------------

	@Override
	public Moves eval(final Context context)
	{
		final Moves moves = new BaseMoves(super.then());

		final int from = startLocationFn.eval(context);
		final int cid = new ContainerId(null, null, null, null, new IntConstant(from)).eval(context);
		final other.topology.Topology graph = context.containers()[cid].topology();
		final SiteType realType = (type != null) ? type : context.game().board().defaultSite();
		
		CompassDirection facing = null;
		if (forward.eval(context))
		{
			final int pieceIndex = context.state().containerStates()[cid].what(from, type);
			if (pieceIndex != 0)
			{
				final Component piece = context.game().equipment().components()[pieceIndex];
				facing = (CompassDirection) piece.getDirn();
			}
		}

		if (from == Constants.OFF)
			return moves;

		final int origFrom = context.from();
		final int origTo = context.to();

		context.setFrom(from);

		if (fromCondition != null && !fromCondition.eval(context))
			return moves;

		final int[] sitesAfterWalk = walk.eval(context).sites();

		for (final int to : sitesAfterWalk)
		{
			final TopologyElement fromV = graph.getGraphElement(realType, from);
			final TopologyElement toV = graph.getGraphElement(realType, to);
			
			if (facing == null || checkForward(facing, fromV, toV))
			{
				context.setTo(to);
				if (!goRule.eval(context))
					continue;

				final ActionMove actionMove = new ActionMove(realType, from, Constants.UNDEFINED, realType,
						to, Constants.OFF, Constants.UNDEFINED, Constants.OFF, Constants.OFF, false);
				if (isDecision())
					actionMove.setDecision(true);
				Move thisAction = new Move(actionMove);

				thisAction = MoveUtilities.chainRuleWithAction(context, sideEffect, thisAction, true,
							false);

				MoveUtilities.chainRuleCrossProduct(context, moves, null, thisAction, false);
				thisAction.setFromNonDecision(from);
				thisAction.setToNonDecision(to);
			}
		}

		context.setTo(origTo);
		context.setFrom(origFrom);

		for (final Move m : moves.moves())
			m.setMover(context.state().mover());

		if (then() != null)
			for (int j = 0; j < moves.moves().size(); j++)
				moves.moves().get(j).then().add(then().moves());

		// Store the Moves in the computed moves.
		for (int j = 0; j < moves.moves().size(); j++)
			moves.moves().get(j).setMovesLudeme(this);

		return moves;
	}

	//-------------------------------------------------------------------------

	/**
	 * @param facing
	 * @param from
	 * @param to
	 * @return True only if the forward condition is respecting according to the
	 *         facing direction.
	 */
	private static boolean checkForward(final CompassDirection facing, final TopologyElement from, final TopologyElement to)
	{
		switch (facing)
		{
		case N:
			return from.row() < to.row();
		case NE:
			return from.row() < to.row() && from.col() < to.col();
		case E:
			return from.col() < to.col();
		case SE:
			return from.row() > to.row() && from.col() < to.col();
		case S:
			return from.row() > to.row();
		case SW:
			return from.row() > to.row() && from.col() > to.col();
		case W:
			return from.col() > to.col();
		case NW:
			return from.row() < to.row() && from.col() > to.col();
		case ENE:
		case ESE:
		case NNE:
		case NNW:
		case SSE:
		case SSW:
		case WNW:
		case WSW:
		default:
			return false;
		}
	}

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = super.gameFlags(game);
		gameFlags |= GameType.UsesFromPositions;

		if (startLocationFn != null)
			gameFlags |= startLocationFn.gameFlags(game);

		if (sideEffect != null)
			gameFlags |= sideEffect.gameFlags(game);

		if (walk != null)
			gameFlags |= walk.gameFlags(game);

		if (fromCondition != null)
			gameFlags |= fromCondition.gameFlags(game);

		if (goRule != null)
			gameFlags |= goRule.gameFlags(game);

		gameFlags |= SiteType.gameFlags(type);

		if (then() != null)
			gameFlags |= then().gameFlags(game);

		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(SiteType.concepts(type));
		concepts.or(super.concepts(game));
		if (isDecision())
		{
			concepts.set(Concept.LeapDecision.id(), true);

			if (goRule != null)
			{
				if (goRule.concepts(game).get(Concept.IsEmpty.id()))
					concepts.set(Concept.LeapDecisionToEmpty.id(), true);
				if (goRule.concepts(game).get(Concept.IsFriend.id()))
					concepts.set(Concept.LeapDecisionToFriend.id(), true);
				if (goRule.concepts(game).get(Concept.IsEnemy.id()))
					concepts.set(Concept.LeapDecisionToEnemy.id(), true);
				if (goRule instanceof BooleanConstant.TrueConstant)
				{
					concepts.set(Concept.LeapDecisionToEmpty.id(), true);
					concepts.set(Concept.LeapDecisionToFriend.id(), true);
					concepts.set(Concept.LeapDecisionToEnemy.id(), true);
				}
			}
		}
		else
			concepts.set(Concept.LeapEffect.id(), true);
			

		if (startLocationFn != null)
			concepts.or(startLocationFn.concepts(game));

		if (sideEffect != null)
			concepts.or(sideEffect.concepts(game));

		if (walk != null)
			concepts.or(walk.concepts(game));

		if (fromCondition != null)
			concepts.or(fromCondition.concepts(game));

		if (goRule != null)
			concepts.or(goRule.concepts(game));

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

		if (startLocationFn != null)
			writeEvalContext.or(startLocationFn.writesEvalContextRecursive());

		if (sideEffect != null)
			writeEvalContext.or(sideEffect.writesEvalContextRecursive());

		if (walk != null)
			writeEvalContext.or(walk.writesEvalContextRecursive());

		if (fromCondition != null)
			writeEvalContext.or(fromCondition.writesEvalContextRecursive());

		if (goRule != null)
			writeEvalContext.or(goRule.writesEvalContextRecursive());

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

		if (startLocationFn != null)
			readEvalContext.or(startLocationFn.readsEvalContextRecursive());

		if (sideEffect != null)
			readEvalContext.or(sideEffect.readsEvalContextRecursive());

		if (walk != null)
			readEvalContext.or(walk.readsEvalContextRecursive());

		if (fromCondition != null)
			readEvalContext.or(fromCondition.readsEvalContextRecursive());

		if (goRule != null)
			readEvalContext.or(goRule.readsEvalContextRecursive());

		if (then() != null)
			readEvalContext.or(then().readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= super.missingRequirement(game);

		if (fromCondition != null)
			missingRequirement |= fromCondition.missingRequirement(game);

		if (startLocationFn != null)
			missingRequirement |= startLocationFn.missingRequirement(game);

		if (sideEffect != null)
			missingRequirement |= sideEffect.missingRequirement(game);

		if (walk != null)
			missingRequirement |= walk.missingRequirement(game);

		if (goRule != null)
			missingRequirement |= goRule.missingRequirement(game);

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

		if (startLocationFn != null)
			willCrash |= startLocationFn.willCrash(game);

		if (sideEffect != null)
			willCrash |= sideEffect.willCrash(game);

		if (walk != null)
			willCrash |= walk.willCrash(game);

		if (goRule != null)
			willCrash |= goRule.willCrash(game);

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

		if (startLocationFn != null)
			startLocationFn.preprocess(game);

		if (walk != null)
			walk.preprocess(game);

		if (goRule != null)
			goRule.preprocess(game);

		if (sideEffect != null)
			sideEffect.preprocess(game);

		if (fromCondition != null)
			fromCondition.preprocess(game);
	}

	//-------------------------------------------------------------------------

	/**
	 * @return Start locations
	 */
	public IntFunction startLocationFn()
	{
		return startLocationFn;
	}

	/**
	 * @return Our walk function
	 */
	public RegionFunction walk()
	{
		return walk;
	}

	/**
	 * @return Our go rule
	 */
	public BooleanFunction goRule()
	{
		return goRule;
	}
	
	@Override
	public String toEnglish(final Game game)
	{
		String thenString = "";
		if (then() != null)
			thenString = " then " + then().toEnglish(game);
		
		return "leap a piece to "+ goRule.toEnglish(game) + thenString;
	}
}
