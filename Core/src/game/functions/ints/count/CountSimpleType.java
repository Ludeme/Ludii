package game.functions.ints.count;

/**
 * Defines the types of properties that can be counted without a parameter 
 * (apart from the graph element type, where relevant).
 * 
 * @author Eric.Piette and cambolbro
 */
public enum CountSimpleType
{
	/** Number of rows on the board. */
	Rows,
	
	/** Number of columns on the board. */
	Columns,
	
	/** Number of turns played so far in this trial. */
	Turns,
	
	/** Number of moves made so far in this trial. */
	Moves,
	
	/** Number of completed games within a match. */
	Trials,
	
	/** Number of moves made so far this turn. */
	MovesThisTurn,
	
	/** Number of phase changes during this trial. */
	Phases,
	
	/** Number of adjacent (connected) elements. */
	Vertices,
	
	/** Number of edges on the board. */
	Edges,
	
	/** Number of cells on the board. */
	Cells,
	
	/** Number of players. */
	Players,

	/** Number of active players. */
	Active,
}
