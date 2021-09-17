package game.rules.play.moves.nonDecision.effect.set.direction;

import java.util.BitSet;

import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import annotations.Or;
import game.Game;
import game.functions.booleans.BooleanConstant;
import game.functions.booleans.BooleanFunction;
import game.functions.ints.IntFunction;
import game.functions.ints.iterator.From;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import game.rules.play.moves.nonDecision.effect.Effect;
import game.rules.play.moves.nonDecision.effect.Then;
import game.types.board.SiteType;
import game.types.state.GameType;
import main.Constants;
import other.action.state.ActionSetRotation;
import other.concept.Concept;
import other.context.Context;
import other.move.Move;

/**
 * Changes the direction of a piece.
 * 
 * @author Eric.Piette and cambolbro
 * 
 * @remarks This ludeme applies to games with oriented pieces, e.g. Ploy.
 */
@Hide
public final class SetRotation extends Effect
{
	private static final long serialVersionUID = 1L;

	// -------------------------------------------------------------------------

	/** Which Direction. */
	private final IntFunction siteFn;

	/** Which Set of direction. */
	private final IntFunction[] directionsFn;

	/** Previous Direction. */
	private final BooleanFunction previous;

	/** Next Direction. */
	private final BooleanFunction next;

	/** Add on Cell/Edge/Vertex. */
	private SiteType type;

	//-------------------------------------------------------------------------

	/**
	 * @param to         Description of the ``to'' location [(to (from))].
	 * @param directions The index of the possible new rotations.
	 * @param direction  The index of the possible new rotation.
	 * @param previous   True to allow movement to the left [True].
	 * @param next       True to allow movement to the right [True].
	 * @param then       The moves applied after that move is applied.
	 */
	public SetRotation
	(
		@Opt           final game.util.moves.To to, 
		@Opt @Or 	   final IntFunction[]      directions,
		@Opt @Or 	   final IntFunction        direction,
		@Opt     @Name final BooleanFunction    previous,
		@Opt     @Name final BooleanFunction    next,
		@Opt 	 	   final Then               then
	)
	{
		super(then);

		int numNonNull = 0;
		if (directions != null)
			numNonNull++;
		if (direction != null)
			numNonNull++;

		if (numNonNull > 1)
			throw new IllegalArgumentException("Zero or one Or parameter must be non-null.");

		siteFn = (to == null) ? new From(null) : (to.loc() != null) ? to.loc() : new From(null);
		if (directions != null)
			directionsFn = directions;
		else
			directionsFn = (direction == null) ? null : new IntFunction[]
			{ direction };
				
		this.previous = (previous == null) ? new BooleanConstant(true) : previous;
		this.next = (next == null) ? new BooleanConstant(true) : next;
		type = (to == null) ? null : to.type();
	}

	//-------------------------------------------------------------------------

	@Override
	public Moves eval(final Context context)
	{
		final BaseMoves moves = new BaseMoves(super.then());

		final int site = siteFn.eval(context);

		if (site == Constants.OFF)
			return moves;
		
		if (directionsFn != null)
		{
			for (final IntFunction directionFn : directionsFn)
			{
				final int direction = directionFn.eval(context);
				final ActionSetRotation actionRotation = new ActionSetRotation(type, site, direction);
				if (isDecision())
					actionRotation.setDecision(true);
				final Move action = new Move(actionRotation);
				action.setFromNonDecision(site);
				action.setToNonDecision(site);
				action.setMover(context.state().mover());
				moves.moves().add(action);
			}
		}

		if (previous != null || next != null)
		{
			final int currentRotation = context.containerState(context.containerId()[site]).rotation(site, type);
			final int maxRotation = context.game().maximalRotationStates() - 1;

			if (previous != null && previous.eval(context))
			{
				final int newRotation = (currentRotation > 0) ? currentRotation - 1 : maxRotation;
				final ActionSetRotation actionRotation = new ActionSetRotation(type, site, newRotation);
				if (isDecision())
					actionRotation.setDecision(true);
				final Move action = new Move(actionRotation);
				action.setFromNonDecision(site);
				action.setToNonDecision(site);
				action.setMover(context.state().mover());
				moves.moves().add(action);
			}

			if (next != null && next.eval(context))
			{
				final int newRotation = (currentRotation < maxRotation) ? currentRotation + 1 : 0;
				final ActionSetRotation actionRotation = new ActionSetRotation(type, site, newRotation);
				if (isDecision())
					actionRotation.setDecision(true);
				final Move action = new Move(actionRotation);
				action.setFromNonDecision(site);
				action.setToNonDecision(site);
				action.setMover(context.state().mover());
				moves.moves().add(action);
			}
		}

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
	public boolean canMoveTo(final Context context, final int target)
	{
		return false;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = super.gameFlags(game) | siteFn.gameFlags(game) | previous.gameFlags(game)
				| next.gameFlags(game)
				| GameType.Rotation;
		
		if (directionsFn != null)
			for (final IntFunction direction : directionsFn)
				gameFlags |= direction.gameFlags(game);

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
		concepts.or(siteFn.concepts(game));
		concepts.or(previous.concepts(game));
		concepts.or(next.concepts(game));
		concepts.set(Concept.PieceRotation.id(), true);
		
		if(isDecision())
			concepts.set(Concept.RotationDecision.id(), true);
		else
			concepts.set(Concept.SetRotation.id(), true);

		if (directionsFn != null)
			for (final IntFunction direction : directionsFn)
				concepts.or(direction.concepts(game));

		if (then() != null)
			concepts.or(then().concepts(game));

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(super.writesEvalContextRecursive());
		writeEvalContext.or(siteFn.writesEvalContextRecursive());
		writeEvalContext.or(previous.writesEvalContextRecursive());
		writeEvalContext.or(next.writesEvalContextRecursive());

		if (directionsFn != null)
			for (final IntFunction direction : directionsFn)
				writeEvalContext.or(direction.writesEvalContextRecursive());

		if (then() != null)
			writeEvalContext.or(then().writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(super.readsEvalContextRecursive());
		readEvalContext.or(siteFn.readsEvalContextRecursive());
		readEvalContext.or(previous.readsEvalContextRecursive());
		readEvalContext.or(next.readsEvalContextRecursive());

		if (directionsFn != null)
			for (final IntFunction direction : directionsFn)
				readEvalContext.or(direction.readsEvalContextRecursive());

		if (then() != null)
			readEvalContext.or(then().readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= super.missingRequirement(game);
		missingRequirement |= siteFn.missingRequirement(game);
		missingRequirement |= previous.missingRequirement(game);
		missingRequirement |= next.missingRequirement(game);

		if (directionsFn != null)
			for (final IntFunction direction : directionsFn)
				missingRequirement |= direction.missingRequirement(game);

		if (then() != null)
			missingRequirement |= then().missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= super.willCrash(game);
		willCrash |= siteFn.willCrash(game);
		willCrash |= previous.willCrash(game);
		willCrash |= next.willCrash(game);

		if (directionsFn != null)
			for (final IntFunction direction : directionsFn)
				willCrash |= direction.willCrash(game);

		if (then() != null)
			willCrash |= then().willCrash(game);
		return willCrash;
	}

	@Override
	public boolean isStatic()
	{
		boolean isStatic = siteFn.isStatic() | previous.isStatic() | next.isStatic();
		if (directionsFn != null)
			for (final IntFunction direction : directionsFn)
				isStatic |= direction.isStatic();
		return isStatic;
	}

	@Override
	public void preprocess(final Game game)
	{
		type = SiteType.use(type, game);
		super.preprocess(game);
		siteFn.preprocess(game);
		previous.preprocess(game);
		next.preprocess(game);
		if (directionsFn != null)
			for (final IntFunction direction : directionsFn)
				direction.preprocess(game);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{		
		String directionsString = "all directions";
		if (directionsFn != null)
		{
			directionsString = "[";
			for (final IntFunction i : directionsFn)
				directionsString += i.toEnglish(game) + ",";
			directionsString = directionsString.substring(0,directionsString.length()-1) + "]";
		}
		
		String thenString = "";
		if (then() != null)
			thenString = " then " + then().toEnglish(game);
		
		return "set the rotation of " + type.name().toLowerCase() + " " + siteFn.toEnglish(game) + " to " + directionsString + thenString;
	}
	
	//-------------------------------------------------------------------------

}
