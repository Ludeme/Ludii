package game.functions.ints.count;

/**
 * Defines the types of components that can be counted within a game.
 * 
 * @author Eric.Piette and cambolbro
 */
public enum CountComponentType
{
	/** Number of pieces on the board (or in hand), per player or over all players. */
	Pieces,
	
	/** The number of pips showing on all dice, or dice owned by a specified player. */
	Pips,
}
