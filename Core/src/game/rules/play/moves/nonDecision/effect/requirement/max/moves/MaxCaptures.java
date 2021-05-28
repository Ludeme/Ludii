package game.rules.play.moves.nonDecision.effect.requirement.max.moves;

import java.util.BitSet;
import java.util.List;

import annotations.Hide;
import annotations.Opt;
import game.Game;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import game.rules.play.moves.nonDecision.effect.Effect;
import game.rules.play.moves.nonDecision.effect.Then;
import gnu.trove.list.array.TIntArrayList;
import other.action.Action;
import other.action.move.ActionRemove;
import other.concept.Concept;
import other.context.Context;
import other.move.Move;

/**
 * Filters a list of moves to keep only the moves doing the maximum possible number of captures.
 * 
 * @author Eric.Piette
 * 
 * @remarks For games allowing only the maximum possible number of captures in one move (e.g.
 *          Triad).
 */
@Hide
public final class MaxCaptures extends Effect
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The moves to maximise. */
	private final Moves moves;

	/**
	 * @param moves The moves to filter.
	 * @param then  The moves applied after that move is applied.
	 */
	public MaxCaptures
	(
			 final Moves moves, 
		@Opt final Then  then
	)
	{
		super(then);
		this.moves = moves;
	}

	//-------------------------------------------------------------------------

	@Override
	public Moves eval(final Context context)
	{
		final Moves returnMoves = new BaseMoves(super.then());

		final Moves movesToEval = moves.eval(context);

		// Compute the number of capture for each move.
		final TIntArrayList numCaptureByMove = new TIntArrayList();
		for (final Move m : movesToEval.moves())
		{
			int numCapture = 0;
			final List<Action> actions = m.getActionsWithConsequences(context);

			for (final Action action : actions)
				if (action instanceof ActionRemove)
					numCapture++;

			numCaptureByMove.add(numCapture);
		}

		// Compute the maximum of capture.
		int maxCapture = 0;
		for (int i = 0; i < numCaptureByMove.size(); i++)
			if (numCaptureByMove.getQuick(i) > maxCapture)
				maxCapture = numCaptureByMove.getQuick(i);

		// Keep only the maximum one.
		for (int i = 0; i < numCaptureByMove.size(); i++)
			if (numCaptureByMove.getQuick(i) == maxCapture)
				returnMoves.moves().add(movesToEval.get(i));

		if (then() != null)
			for (int j = 0; j < moves.moves().size(); j++)
				moves.moves().get(j).then().add(then().moves());

		// Store the Moves in the computed moves.
		for (int j = 0; j < returnMoves.moves().size(); j++)
			returnMoves.moves().get(j).setMovesLudeme(returnMoves);

		return returnMoves;
	}

	//-------------------------------------------------------------------------

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = moves.gameFlags(game) | super.gameFlags(game);

		if (then() != null)
			gameFlags |= then().gameFlags(game);

		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(moves.concepts(game));
		concepts.or(super.concepts(game));
		concepts.set(Concept.MaxCapture.id(), true);
		concepts.set(Concept.CopyContext.id(), true);

		if (then() != null)
			concepts.or(then().concepts(game));
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(moves.writesEvalContextRecursive());
		writeEvalContext.or(super.writesEvalContextRecursive());

		if (then() != null)
			writeEvalContext.or(then().writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(moves.readsEvalContextRecursive());
		readEvalContext.or(super.readsEvalContextRecursive());

		if (then() != null)
			readEvalContext.or(then().readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= moves.missingRequirement(game);
		missingRequirement |= super.missingRequirement(game);

		if (then() != null)
			missingRequirement |= then().missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= moves.willCrash(game);
		willCrash |= super.willCrash(game);

		if (then() != null)
			willCrash |= then().willCrash(game);
		return willCrash;
	}

	@Override
	public boolean isStatic()
	{
		final boolean isStatic = moves.isStatic();
		return isStatic;
	}

	@Override
	public void preprocess(final Game game)
	{
		moves.preprocess(game);
	}
	
	//-------------------------------------------------------------------------

	@Override
	public String toEnglish(final Game game)
	{
		return "MaxCaptures";
	}
}
