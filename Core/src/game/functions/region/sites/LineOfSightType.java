package game.functions.region.sites;

/**
 * Specifies the expected types of line of sight tests.
 * 
 * @author cambolbro
 */
public enum LineOfSightType
{
	/** Empty sites in line of sight along each direction. */
	Empty,

	/** Farthest empty site in line of sight along each direction. */
	Farthest,
	
	/** First piece (of any type) in line of sight along each direction. */
	Piece,
}
