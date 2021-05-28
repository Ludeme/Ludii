package game.types.board;

/**
 * Defines certain landmarks that can be used to specify individual sites on the board.
 * 
 * @author Eric.Piette
 */
public enum LandmarkType
{
	/** The central site of the board. */
	CentreSite,
	
	/** The site that is furthest to the left. */
	LeftSite,
	
	/** The site that is furthest to the right */
	RightSite,
	
	/** The site that is furthest to the top. */
	Topsite,
	
	/** The site that is furthest to the bottom. */
	BottomSite,
	
	/** The first site indexed in the graph. */
	FirstSite,
	
	/** The last site indexed in the graph. */
	LastSite,
}