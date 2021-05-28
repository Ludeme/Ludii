package game.types.play;

/**
 * Defines the possible types of repetition that can occur in a game.
 * 
 * @author cambolbro
 */
public enum RepetitionType
{
	/** Situational State repeated within a turn. */
	SituationalInTurn,

	/** Positional State repeated within a turn. */
	PositionalInTurn, 
	
	/** State repeated within a game (pieces on the board only). */
	Positional,
	
	/** State repeated within a game (all data in the state). */
	Situational
}
