package game.functions.booleans.is;

/**
 * Defines the types of Is test for a player with no parameter.
 */
public enum IsSimpleType
{
	/**
	 * To check if the game is repeating the same set of states three times with
	 * exactly the same moves during these states.
	 */
	Cycle,

	/** To check if the state is in pending. */
	Pending,

	/** To check if the board is full. */
	Full,

}
