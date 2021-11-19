package game.rules.play.moves.nonDecision.effect.state.swap.sites;

import java.util.BitSet;

import annotations.And;
import annotations.Hide;
import annotations.Opt;
import game.Game;
import game.functions.ints.IntFunction;
import game.functions.ints.last.LastFrom;
import game.functions.ints.last.LastTo;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import game.rules.play.moves.nonDecision.effect.Effect;
import game.rules.play.moves.nonDecision.effect.Then;
import game.types.board.SiteType;
import game.types.state.GameType;
import main.Constants;
import other.action.Action;
import other.action.move.ActionAdd;
import other.action.move.move.ActionMove;
import other.concept.Concept;
import other.context.Context;
import other.move.Move;
import other.state.container.ContainerState;

/**
 * Swaps two pieces on the board.
 * 
 * @author Eric.Piette
 */
@Hide
public final class SwapPieces extends Effect
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The locA to swap */
	private final IntFunction locAFn;
	
	/** The locB to swap */
	private final IntFunction locBFn;

	//-------------------------------------------------------------------------

	/**
	 * @param locA The first location [(lastFrom)].
	 * @param locB The second location [(lastTo)].
	 * @param then The moves applied after that move is applied.
	 */
	public SwapPieces
	(
		@And @Opt final IntFunction locA,
		@And @Opt final IntFunction locB,
		     @Opt final Then        then
	)
	{
		super(then);
		locAFn = (locA == null) ? new LastFrom(null) : locA;
		locBFn = (locB == null) ? new LastTo(null) : locB;
	}

	//-------------------------------------------------------------------------

	@Override
	public Moves eval(final Context context)
	{
		final Moves moves = new BaseMoves(super.then());

		final int locA = locAFn.eval(context);
		final int locB = locBFn.eval(context);
		final ContainerState cs = context.containerState(context.containerId()[locB]);
		final int whatB = cs.whatCell(locB);
		
		final ActionMove actionMove = new ActionMove(SiteType.Cell, locA, Constants.UNDEFINED, SiteType.Cell, locB,
				Constants.OFF, Constants.UNDEFINED, Constants.OFF, Constants.OFF, false);
		if (isDecision())
			actionMove.setDecision(true);
		final Move swapMove = new Move(actionMove);

		final Action actionAdd = new ActionAdd(null, locA, whatB, 1, Constants.UNDEFINED, Constants.UNDEFINED,
				Constants.UNDEFINED, null);
		swapMove.actions().add(actionAdd);

		swapMove.setFromNonDecision(locA);
		swapMove.setToNonDecision(locB);
		swapMove.setMover(context.state().mover());
		moves.moves().add(swapMove);
		
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
		long gameFlags = GameType.UsesFromPositions | locAFn.gameFlags(game) | locBFn.gameFlags(game)
				| super.gameFlags(game);

		if (then() != null)
			gameFlags |= then().gameFlags(game);

		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();

		if (isDecision())
			concepts.set(Concept.SwapPiecesDecision.id(), true);

		concepts.or(super.concepts(game));
		concepts.or(locAFn.concepts(game));
		concepts.or(locBFn.concepts(game));

		if (then() != null)
			concepts.or(then().concepts(game));

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(super.writesEvalContextRecursive());
		writeEvalContext.or(locAFn.writesEvalContextRecursive());
		writeEvalContext.or(locBFn.writesEvalContextRecursive());

		if (then() != null)
			writeEvalContext.or(then().writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(super.readsEvalContextRecursive());
		readEvalContext.or(locAFn.readsEvalContextRecursive());
		readEvalContext.or(locBFn.readsEvalContextRecursive());

		if (then() != null)
			readEvalContext.or(then().readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= super.missingRequirement(game);
		missingRequirement |= locAFn.missingRequirement(game);
		missingRequirement |= locBFn.missingRequirement(game);

		if (then() != null)
			missingRequirement |= then().missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= super.willCrash(game);
		willCrash |= locAFn.willCrash(game);
		willCrash |= locBFn.willCrash(game);

		if (then() != null)
			willCrash |= then().willCrash(game);
		return willCrash;
	}

	@Override
	public boolean isStatic()
	{
		return locAFn.isStatic() && locBFn.isStatic();
	}

	@Override
	public void preprocess(final Game game)
	{
		super.preprocess(game);
		locAFn.preprocess(game);
		locBFn.preprocess(game);
	}

	//-------------------------------------------------------------------------

	@Override
	public String toEnglish(final Game game)
	{
		String thenString = "";
		if (then() != null)
			thenString = " then " + then().toEnglish(game);
		
		return "Swap the pieces at " + locAFn.toEnglish(game) + "and" + locBFn.toEnglish(game) + thenString;
	}

	@Override
	public boolean canMoveTo(final Context context, final int target)
	{
		return false;
	}
}
