package game.types.board;

/**
 * Defines known (predefined) regions of the board.
 * 
 * @author Eric.Piette and cambolbro
 */
public enum RegionTypeStatic
{
	/** Row areas. */
	Rows, 
	/** Column areas. */
	Columns, 
	/** All direction areas. */
	AllDirections, 
	/** Hint areas. */
	HintRegions,
	/** Layers areas. */
	Layers,
	/** diagonal areas. */
	Diagonals,
	/** SubGrid areas. */
	SubGrids, 
	/** Region areas. */
	Regions, 
	/** Vertex areas. */
	Vertices, 
	/** Corner areas. */
	Corners,
	/** Side areas. */
	Sides,
	/** Side areas that are not corners. */
	SidesNoCorners,
	/** All site areas. */
	AllSites, 
	/** Touching areas. */
	Touching
}
