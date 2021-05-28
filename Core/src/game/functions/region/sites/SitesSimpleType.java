package game.functions.region.sites;

/**
 * Specifies set of sites that do not require any parameters (apart from the graph element type).
 * 
 * @author Eric.Piette and cambolbro
 */
public enum SitesSimpleType
{
	/** All board sites. */
	Board,
	
	/** Sites on the top side of the board. */
	Top,

	/** Sites on the bottom side of the board. */
	Bottom,
	
	/** Sites on the left side of the board. */
	Left,
	
	/** Sites on the right side of the board. */
	Right,
	
	/** Interior board sites. */
	Inner,
	
	/** Outer board sites. */
	Outer,
	
	/** Perimeter board sites. */
	Perimeter,

	/** Corner board sites. */
	Corners,
	
	/** Concave corner board sites. */
	ConcaveCorners,

	/** Convex corner board sites. */
	ConvexCorners,

	/** Major generator board sites. */
	Major,

	/** Minor generator board sites. */
	Minor,

	/** Centre board site(s). */
	Centre,
	
	/** Sites that contain a puzzle hint. */
	Hint,
	
	/** Sites to remove at the end of a capture sequence. */
	ToClear,
	
	/**
	 * Sites in the line of play. Applies to domino game 
	 * (returns an empty region for other games).
	 */
	LineOfPlay,
	
	/** Sites with a non-zero ``pending'' value in the game state. */
	Pending,
	
	/**
	 * Playable sites of a boardless game. For other games, returns the set of 
	 * empty sites adjacent to occupied sites.
	 */
	Playable,
	
	/**
	 * The set of ``to'' sites of the last move.
	 */
	LastTo,

	/**
	 * The set of ``from'' sites of the last move.
	 */
	LastFrom,
}
