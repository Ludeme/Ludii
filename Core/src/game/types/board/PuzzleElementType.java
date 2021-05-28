package game.types.board;

/**
 * Defines the possible types of variables that can be used in deduction
 * puzzles.
 * 
 * @author Eric.Piette
 */
public enum PuzzleElementType
{
	/** A variable corresponding to a cell. */
	Cell, 
	
	/** A variable corresponding to an edge. */
	Edge, 
	
	/** A variable corresponding to a vertex. */
	Vertex,
	
	/** A variable corresponding to a hint. */
	Hint,
	;

	/**
	 * @param puzzleElement The puzzle type to convert.
	 * @return The corresponding SiteType.
	 */
	public static SiteType convert(final PuzzleElementType puzzleElement)
	{
		switch (puzzleElement)
		{
		case Cell:
			return SiteType.Cell;
		case Edge:
			return SiteType.Edge;
		case Vertex:
			return SiteType.Vertex;
		default:
			return null;
		}
	}
}
