package game.functions.ints.value;

/**
 * Defines the types of properties than can be returned by the super ludeme
 * value with no parameter.
 * 
 * @author Eric.Piette
 */
public enum ValueSimpleType
{
	/**
	 * To get the pending value if the previous state causes the current state to be
	 * pending with a specific value.
	 */
	Pending,
	
	/**
	 * To get the move limit of a game.
	 */
	MoveLimit,
	
	/**
	 * To get the turn limit of a game.
	 */
	TurnLimit,
}
