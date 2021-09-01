package game.rules.play.moves.nonDecision.effect;

import java.io.Serializable;
import java.util.BitSet;

import annotations.Name;
import annotations.Opt;
import game.Game;
import game.rules.play.moves.Moves;
import game.rules.play.moves.nonDecision.NonDecision;
import other.BaseLudeme;
import other.concept.Concept;

/**
 * Defines the subsequents of a move, to be applied after the move.
 * 
 * @author Eric.Piette and cambolbro
 * 
 * @remarks This is used to define subsequent moves by the same player in a 
 *          turn after a move is made.
 */
public class Then extends BaseLudeme implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	//-------------------------------------------------------------------------

	private final Moves moves;

	//-------------------------------------------------------------------------

	/**
	 * @param moves              The moves to apply afterwards.
	 * @param applyAfterAllMoves For simultaneous game, to apply the subsequents
	 *                           when all the moves of all the players are applied.
	 * 
	 * @example (then (moveAgain))
	 */
	public Then
	(
	              final NonDecision moves,
	   @Opt @Name final Boolean     applyAfterAllMoves
	)
	{
		this.moves = moves;
		this.moves.setApplyAfterAllMoves((applyAfterAllMoves == null) ? false : applyAfterAllMoves.booleanValue());
	}

	//-------------------------------------------------------------------------
	// Ludeme overrides
	
	@Override
	public String toString()
	{
		return "[Then: " + moves + "]";
	}

	//-------------------------------------------------------------------------

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((moves == null) ? 0 : moves.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
			return true;

		if (!(obj instanceof Then))
			return false;
		
		final Then other = (Then) obj;
		
		if (moves == null)
			if (other.moves != null)
				return false;
		else if (!moves.equals(other.moves))
			return false;
		
		return true;
	}

	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{
		return "then " + moves.toEnglish(game);
	}

	//-------------------------------------------------------------------------

	/**
	 * @return Moves in the consequence.
	 */
	public Moves moves()
	{
		return moves;
	}

	/**
	 * @param game
	 * @return gameFlags of the csq.
	 */
	public long gameFlags(final Game game)
	{
		long gameFlags = 0;
		if (moves != null)
			gameFlags |= moves.gameFlags(game);
		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.set(Concept.Then.id(), true);
		if (moves != null)
			concepts.or(moves.concepts(game));
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		if (moves != null)
			writeEvalContext.or(moves.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		if (moves != null)
			readEvalContext.or(moves.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		if (moves != null)
			missingRequirement |= moves.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		if (moves != null)
			willCrash |= moves.willCrash(game);
		return willCrash;
	}
}
