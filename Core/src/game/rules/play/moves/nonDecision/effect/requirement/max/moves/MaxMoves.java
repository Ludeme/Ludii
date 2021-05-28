package game.rules.play.moves.nonDecision.effect.requirement.max.moves;

import java.util.BitSet;

import annotations.Hide;
import annotations.Opt;
import game.Game;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import game.rules.play.moves.nonDecision.effect.Effect;
import game.rules.play.moves.nonDecision.effect.Then;
import other.concept.Concept;
import other.context.Context;
import other.context.TempContext;
import other.move.Move;

/**
 * Filters a list of legal moves to keep only the moves allowing the maximum number of
 * moves in a turn.
 * 
 * @author Eric.Piette
 * 
 * @remarks For games like International Draughts.
 */
@Hide
public final class MaxMoves extends Effect
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The moves to maximise. */
	private final Moves moves;

	/**
	 * @param moves The moves to filter.
	 * @param then  The moves applied after that move is applied.
	 */
	public MaxMoves
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

		final int[] replayCount = new int[movesToEval.moves().size()];
		
		// We store evalled moves because they'll include already-evalled consequents, more efficient to keep them
		final Move[] evalledMoves = new Move[movesToEval.moves().size()];

		for (int i = 0; i < movesToEval.moves().size(); i++)
		{
			final Move m = movesToEval.moves().get(i);
			// System.out.println(m);
			
			final Context newContext = new TempContext(context);
			evalledMoves[i] = newContext.game().apply(newContext, m);
			replayCount[i] = getReplayCount(newContext, 1);
		}

		int max = 0;

		// Get the max of the replayCount.
		for (final int count : replayCount)
		{
			if (count > max)
				max = count;
		}

		// Keep only the longest moves.
		for (int i = 0; i < evalledMoves.length; i++)
			if (replayCount[i] == max)
				returnMoves.moves().add(evalledMoves[i]);

		// Store the Moves in the computed moves.
		for (int j = 0; j < returnMoves.moves().size(); j++)
			returnMoves.moves().get(j).setMovesLudeme(returnMoves);

		return returnMoves;
	}

	//-------------------------------------------------------------------------

	/**
	 * @param contextCopy
	 * @return the count of the replay of this move.
	 */
	private int getReplayCount(final Context contextCopy, final int count)
	{
		if (contextCopy.state().prev() != contextCopy.state().mover())
			return count;

		final Moves legalMoves = contextCopy.game().moves(contextCopy);

		final int[] replayCount = new int[legalMoves.moves().size()];

		for (int i = 0; i < legalMoves.moves().size(); i++)
		{
			final Move newMove = legalMoves.moves().get(i);
			final Context newContext = new TempContext(contextCopy);
			newContext.game().apply(newContext, newMove);
			replayCount[i] = getReplayCount(newContext, count + 1);
		}

		int max = 0;

		// Get the max of the replayCount.
		for (final int nbReplay : replayCount)
		{
			if (nbReplay > max)
				max = nbReplay;
		}

		return max;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public boolean canMove(final Context context)
	{
		// Don't care about max moves here; as soon as we have at least 1 move,
		// we know that we can move (even if that one may not be the max move!)
		return moves.canMove(context);
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
		concepts.set(Concept.MaxMovesInTurn.id(), true);
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
		return "MaxMove";
	}
}
