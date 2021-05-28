package game.types.play;

/**
 * Defines when to perform certain tests or actions within a game.
 * 
 * @author cambolbro
 */
public enum WhenType
{
	/** Start of a move. */
	StartOfMove, 
	/** End of a move. */
	EndOfMove,
	
	/** Start of a turn. */
	StartOfTurn, 
	/** End of a turn. */
	EndOfTurn, 
	
	/** Start of a round. */
	StartOfRound, 
	/** End of a round. */
	EndOfRound, 
	
	/** Start of a phase. */
	StartOfPhase, 
	/** End of a phase. */
	EndOfPhase, 
	
	/** Start of a game. */
	StartOfGame, 
	/** End of a game. */
	EndOfGame, 
	
	/** Start of a match. */
	StartOfMatch, 
	/** End of a match. */
	EndOfMatch, 
	
	/** Start of a session. */
	StartOfSession,
	/** End of a session. */
	EndOfSession

}
