package game.functions.ints.count;

/**
 * Defines the types of sites that can be counted within a game.
 * 
 * @author Eric.Piette and cambolbro
 */
public enum CountSiteType
{
	/** Number of playable sites within a region or container. */
	Sites,
	
	/** Number of adjacent (connected) elements. */
	Adjacent,
	
	/** Number of neighbours (not necessarily connected). */
	Neighbours,
	
	/** Number of orthogonal elements. */
	Orthogonal,
	
	/** Number of diagonal elements. */
	Diagonal,
	
	/** Number of off-diagonal elements. */
	Off,

	/** Number of sites occupied by an item of a given player on the sites (platform) bellow another site. */
	SitesPlatformBelow,
}
