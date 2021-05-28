package game.rules.play.moves.nonDecision.effect.requirement.max;

/**
 * Defines the types of properties which can be used for the Max super ludeme
 * with only a move ludeme in entry.
 * 
 * @author Eric.Piette
 */
public enum MaxMovesType
{
	/**
	 * To ilter a list of legal moves to keep only the moves allowing the maximum
	 * number of moves in a turn.
	 */
	Moves,

	/**
	 * To filter a list of moves to keep only the moves doing the maximum possible
	 * number of captures.
	 */
	Captures,
}
