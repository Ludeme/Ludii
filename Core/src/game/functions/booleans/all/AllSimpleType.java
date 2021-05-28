package game.functions.booleans.all;

/**
 * Defines the query types that can be used for an {\tt (all ...)} test with no
 * parameter.
 * 
 * @author Eric.Piette
 */
public enum AllSimpleType
{
	/** Returns whether all the dice have been used in the current turn. */
	DiceUsed,

	/** Returns whether all the dice are equal when they are rolled. */
	DiceEqual,

	/** Returns whether all players have passed in succession. */
	Passed,
}
