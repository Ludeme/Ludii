package game.functions.booleans.is;

/**
 * Defines the types of Is test according to an integer.
 * 
 * @author Eric.Piette
 */
public enum IsIntegerType
{
	/** To check if a value is odd. */
	Odd,

	/** To check if a value is even. */
	Even,

	/** To check if a site was already visited by a piece in the same turn. */
	Visited,

	/** To detect whether the terminus of a tile matches with its neighbors. */
	SidesMatch,

	/** To detect whether the pips of a domino match its neighbours. */
	PipsMatch,

	/**
	 * To Ensures that in a 3D board, all the pieces in the bottom layer must be
	 * placed so that they do not fall.
	 */
	Flat,

	/**
	 * To check if any current die is equal to a specific value.
	 */
	AnyDie,
}
