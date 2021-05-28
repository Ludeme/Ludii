package features.spatial.cache;

import features.spatial.cache.footprints.BaseFootprint;
import main.collections.ChunkSet;
import other.state.container.ContainerState;

/**
 * A full version of cached data with support for any mix of cell/edge/vertex
 *
 * @author Dennis Soemers
 */
public class FullCachedData extends BaseCachedData
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * masked "empty" ChunkSet in the game state for which we last cached active
	 * features (for cells)
	 */
	protected final ChunkSet emptyStateCells;

	/**
	 * masked "empty" ChunkSet in the game state for which we last cached active
	 * features (for vertices)
	 */
	protected final ChunkSet emptyStateVertices;

	/**
	 * masked "empty" ChunkSet in the game state for which we last cached active
	 * features (for edges)
	 */
	protected final ChunkSet emptyStateEdges;

	/**
	 * masked "who" ChunkSet in the game state for which we last cached active
	 * features (for cells)
	 */
	protected final ChunkSet whoStateCells;

	/**
	 * masked "who" ChunkSet in the game state for which we last cached active
	 * features (for vertices)
	 */
	protected final ChunkSet whoStateVertices;

	/**
	 * masked "who" ChunkSet in the game state for which we last cached active
	 * features (for edges)
	 */
	protected final ChunkSet whoStateEdges;

	/**
	 * masked "what" ChunkSet in the game state for which we last cached active
	 * features (for cells)
	 */
	protected final ChunkSet whatStateCells;

	/**
	 * masked "what" ChunkSet in the game state for which we last cached active
	 * features (for vertices)
	 */
	protected final ChunkSet whatStateVertices;

	/**
	 * masked "what" ChunkSet in the game state for which we last cached active
	 * features (for edges)
	 */
	protected final ChunkSet whatStateEdges;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * 
	 * @param activeFeatureIndices
	 * @param emptyStateCells 
	 * @param emptyStateVertices 
	 * @param emptyStateEdges 
	 * @param whoStateCells 
	 * @param whoStateVertices 
	 * @param whoStateEdges 
	 * @param whatStateCells 
	 * @param whatStateVertices 
	 * @param whatStateEdges 
	 */
	public FullCachedData
	(
		final int[] activeFeatureIndices, 
		final ChunkSet emptyStateCells,
		final ChunkSet emptyStateVertices,
		final ChunkSet emptyStateEdges,
		final ChunkSet whoStateCells, 
		final ChunkSet whoStateVertices, 
		final ChunkSet whoStateEdges, 
		final ChunkSet whatStateCells,
		final ChunkSet whatStateVertices,
		final ChunkSet whatStateEdges
	)
	{
		super(activeFeatureIndices);
		this.emptyStateCells = emptyStateCells;
		this.emptyStateVertices = emptyStateVertices;
		this.emptyStateEdges = emptyStateEdges;
		this.whoStateCells = whoStateCells;
		this.whoStateVertices = whoStateVertices;
		this.whoStateEdges = whoStateEdges;
		this.whatStateCells = whatStateCells;
		this.whatStateVertices = whatStateVertices;
		this.whatStateEdges = whatStateEdges;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public boolean isDataValid(final ContainerState containerState, final BaseFootprint footprint)
	{
		if 
		(
			footprint.emptyCell() != null && 
			!containerState.emptyChunkSetCell().matches(footprint.emptyCell(), emptyStateCells)
		)
		{
			// part of "empty" state for Cells covered by footprint no longer matches, data invalid
			return false;
		}
		else if 
		(
			footprint.emptyVertex() != null && 
			!containerState.emptyChunkSetVertex().matches(footprint.emptyVertex(), emptyStateVertices)
		)
		{
			// part of "empty" state for Vertices covered by footprint no longer matches, data invalid
			return false;
		}
		else if 
		(
			footprint.emptyEdge() != null && 
			!containerState.emptyChunkSetEdge().matches(footprint.emptyEdge(), emptyStateEdges)
		)
		{
			// part of "empty" state for Edges covered by footprint no longer matches, data invalid
			return false;
		}
		else if 
		(
			footprint.whoCell() != null &&
			!containerState.matchesWhoCell(footprint.whoCell(), whoStateCells)
		)
		{
			// part of "who" state for Cells covered by footprint no longer matches, data invalid
			return false;
		}
		else if 
		(
			footprint.whoVertex() != null &&
			!containerState.matchesWhoVertex(footprint.whoVertex(), whoStateVertices)
		)
		{
			// part of "who" state for Vertices covered by footprint no longer matches, data invalid
			return false;
		}
		else if 
		(
			footprint.whoEdge() != null &&
			!containerState.matchesWhoEdge(footprint.whoEdge(), whoStateEdges)
		)
		{
			// part of "who" state for Edges covered by footprint no longer matches, data invalid
			return false;
		}
		else if 
		(
			footprint.whatCell() != null &&
			!containerState.matchesWhatCell(footprint.whatCell(), whatStateCells)
		)
		{
			// part of "what" state for Cells covered by footprint no longer matches, data invalid
			return false;
		}
		else if 
		(
			footprint.whatVertex() != null &&
			!containerState.matchesWhatVertex(footprint.whatVertex(), whatStateVertices)
		)
		{
			// part of "what" state for Vertices covered by footprint no longer matches, data invalid
			return false;
		}
		else if 
		(
			footprint.whatEdge() != null &&
			!containerState.matchesWhatEdge(footprint.whatEdge(), whatStateEdges)
		)
		{
			// part of "what" state for Edges covered by footprint no longer matches, data invalid
			return false;
		}
		
		return true;
	}
	
	//-------------------------------------------------------------------------

}
