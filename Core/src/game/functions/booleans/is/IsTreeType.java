package game.functions.booleans.is;

/**
 * Defines the types of Is test for a regular graph.
 * 
 * @author Eric.Piette
 */
public enum IsTreeType
{
	/** To check if the induced graph (by adding or deleting edges) is a tree or not. */
	Tree,
	
	/**
	 * To check if the induced graph (by adding or deleting edges) is a spanning
	 * tree or not.
	 */
	SpanningTree,

	/**
	 * To check if the induced graph (by adding or deleting edges) is the largest
	 * caterpillar Tree or not.
	 */
	CaterpillarTree,

	/** To check whether the last vertex is the centre of the tree (or sub tree). */
	TreeCentre,
}
