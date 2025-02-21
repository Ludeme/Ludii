package game.functions.region.sites;

/**
 * Specifies sets of board sites by some indexed property.
 * 
 * @author Eric.Piette and cambolbro
 */
public enum SitesIndexType
{
	/** Sites in a specified row. */
	Row,

	/** Sites in a specified column. */
	Column,
	
	/** Sites in a specified phase. */
	Phase,
	
	/** Vertices that make up a cell. */
	Cell,
	
	/** End points of an edge. */
	Edge,
	
	/** Sites with a specified state value. */
	State,
	
	/** Empty (i.e. unoccupied) sites of a container. */
	Empty,

	/** Sites in a specified layer. */
	Layer,
	
	/** Sites which are supporting other pieces on sites top of them . */
	Support,
}
