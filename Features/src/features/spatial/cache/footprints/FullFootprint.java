package features.spatial.cache.footprints;

import main.collections.ChunkSet;

/**
 * Footprint implementation with support for a mix of cell/vertex/edge stuff.
 *
 * @author Dennis Soemers
 */
public class FullFootprint extends BaseFootprint
{
	
	//-------------------------------------------------------------------------
	
	/** Mask for all chunks that we run at least one "empty" cell test on */
	protected final ChunkSet emptyCell;
	/** Mask for all chunks that we run at least one "empty" vertex test on */
	protected final ChunkSet emptyVertex;
	/** Mask for all chunks that we run at least one "empty" edge test on */
	protected final ChunkSet emptyEdge;

	/** Mask for all chunks that we run at least one "who" cell test on */
	protected final ChunkSet whoCell;
	/** Mask for all chunks that we run at least one "who" vertex test on */
	protected final ChunkSet whoVertex;
	/** Mask for all chunks that we run at least one "who" edge test on */
	protected final ChunkSet whoEdge;

	/** Mask for all chunks that we run at least one "what" cell test on */
	protected final ChunkSet whatCell;
	/** Mask for all chunks that we run at least one "what" vertex test on */
	protected final ChunkSet whatVertex;
	/** Mask for all chunks that we run at least one "what" edge test on */
	protected final ChunkSet whatEdge;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * @param emptyCell 
	 * @param emptyVertex 
	 * @param emptyEdge 
	 * @param whoCell 
	 * @param whoVertex 
	 * @param whoEdge 
	 * @param whatCell 
	 * @param whatVertex 
	 * @param whatEdge 
	 */
	public FullFootprint
	(
		final ChunkSet emptyCell,
		final ChunkSet emptyVertex,
		final ChunkSet emptyEdge,
		final ChunkSet whoCell, 
		final ChunkSet whoVertex,
		final ChunkSet whoEdge,
		final ChunkSet whatCell,
		final ChunkSet whatVertex,
		final ChunkSet whatEdge
	)
	{
		this.emptyCell = emptyCell;
		this.emptyVertex = emptyVertex;
		this.emptyEdge = emptyEdge;
		this.whoCell = whoCell;
		this.whoVertex = whoVertex;
		this.whoEdge = whoEdge;
		this.whatCell = whatCell;
		this.whatVertex = whatVertex;
		this.whatEdge = whatEdge;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public ChunkSet emptyCell()
	{
		return emptyCell;
	}
	
	@Override
	public ChunkSet emptyVertex()
	{
		return emptyVertex;
	}
	
	@Override
	public ChunkSet emptyEdge()
	{
		return emptyEdge;
	}
	
	@Override
	public ChunkSet whoCell()
	{
		return whoCell;
	}
	
	@Override
	public ChunkSet whoVertex()
	{
		return whoVertex;
	}
	
	@Override
	public ChunkSet whoEdge()
	{
		return whoEdge;
	}
	
	@Override
	public ChunkSet whatCell()
	{
		return whatCell;
	}
	
	@Override
	public ChunkSet whatVertex()
	{
		return whatVertex;
	}
	
	@Override
	public ChunkSet whatEdge()
	{
		return whatEdge;
	}
	
	//-------------------------------------------------------------------------

}
