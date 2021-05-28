package game.functions.intArray.players;

/**
 * Defines the types of set of players which can be iterated.
 * 
 * @author Eric.Piette
 */
public enum PlayersManyType
{
	/** All players. */
	All,
	/** Players who are not moving. */
	NonMover,
	/** Enemy players. */
	Enemy,
	/** Friend players (Mover + Allies). */
	Friend,
	/** Ally players. */
	Ally,
}
