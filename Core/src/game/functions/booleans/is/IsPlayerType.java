package game.functions.booleans.is;

/**
 * Defines the types of Is test for a player.
 * 
 * @author Eric.Piette
 */
public enum IsPlayerType
{
	/** To check if a player is the mover. */
	Mover,

	/** To check if a player is the next mover. */
	Next,

	/** To check if a player is the previous mover. */
	Prev,

	/** To check if a player is the friend of the mover. */
	Friend,

	/** To check if a player is the enemy of the mover. */
	Enemy,

	/** To check if a player is active. */
	Active
}
