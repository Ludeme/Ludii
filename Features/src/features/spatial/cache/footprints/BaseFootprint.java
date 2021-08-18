package features.spatial.cache.footprints;

import main.collections.ChunkSet;

/**
 * Wrapper class for masks that represent the key-specific (specific to
 * player index / from-pos / to-pos) footprint of a complete Feature Set.
 * 
 * @author Dennis Soemers
 */
public abstract class BaseFootprint
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Footprint on "empty" ChunkSet for cells
	 */
	public abstract ChunkSet emptyCell();
	
	/**
	 * @return Footprint on "empty" ChunkSet for vertices
	 */
	public abstract ChunkSet emptyVertex();
	
	/**
	 * @return Footprint on "empty" ChunkSet for edges
	 */
	public abstract ChunkSet emptyEdge();
	
	/**
	 * @return Footprint on "who" ChunkSet for cells
	 */
	public abstract ChunkSet whoCell();
	
	/**
	 * @return Footprint on "who" ChunkSet for vertices
	 */
	public abstract ChunkSet whoVertex();
	
	/**
	 * @return Footprint on "who" ChunkSet for edges
	 */
	public abstract ChunkSet whoEdge();
	
	/**
	 * @return Footprint on "what" ChunkSet for cells
	 */
	public abstract ChunkSet whatCell();
	
	/**
	 * @return Footprint on "what" ChunkSet for vertices
	 */
	public abstract ChunkSet whatVertex();
	
	/**
	 * @return Footprint on "what" ChunkSet for edges
	 */
	public abstract ChunkSet whatEdge();
	
	//-------------------------------------------------------------------------
	
	/**
	 * Adds the given other footprint to this one
	 * @param other
	 */
	public void union(final BaseFootprint other)
	{
		if (other.emptyCell() != null)
			emptyCell().or(other.emptyCell());
		if (other.emptyVertex() != null)
			emptyVertex().or(other.emptyVertex());
		if (other.emptyEdge() != null)
			emptyEdge().or(other.emptyEdge());
		
		if (other.whoCell() != null)
			whoCell().or(other.whoCell());
		if (other.whoVertex() != null)
			whoVertex().or(other.whoVertex());
		if (other.whoEdge() != null)
			whoEdge().or(other.whoEdge());
		
		if (other.whatCell() != null)
			whatCell().or(other.whatCell());
		if (other.whatVertex() != null)
			whatVertex().or(other.whatVertex());
		if (other.whatEdge() != null)
			whatEdge().or(other.whatEdge());
	}

	//-------------------------------------------------------------------------
	
}
