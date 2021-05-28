package features.spatial.instances;

import game.types.board.SiteType;
import main.collections.ChunkSet;
import other.state.State;
import other.state.container.ContainerState;

/**
 * Simultaneously tests multiple chunks of the "empty" ChunkSet, returning
 * true if at least one of them is indeed empty.
 * 
 * TODO could make special cases of this class for cells, vertices, and edges
 * 
 * @author Dennis Soemers
 */
public final class OneOfMustEmpty implements BitwiseTest 
{
	
	//-------------------------------------------------------------------------
	
	/** Set of chunks of which at least one must be empty for test to succeed */
	protected final ChunkSet mustEmpties;
	
	/** The first non-zero word in the mustEmpties ChunkSet */
	protected final int firstUsedWord;
	
	/** Graph element type we want to test on */
	protected final SiteType graphElementType;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * @param mustEmpties
	 * @param graphElementType 
	 */
	public OneOfMustEmpty(final ChunkSet mustEmpties, final SiteType graphElementType)
	{
		this.mustEmpties = mustEmpties;
		this.graphElementType = graphElementType;
		firstUsedWord = mustEmpties.nextSetBit(0) / Long.SIZE;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public final boolean matches(final State state) 
	{
		final ContainerState container = state.containerStates()[0];
		final ChunkSet chunkSet;
		switch (graphElementType)
		{
		case Cell:
			chunkSet = container.emptyChunkSetCell();
			break;
		case Vertex:
			chunkSet = container.emptyChunkSetVertex();
			break;
		case Edge:
			chunkSet = container.emptyChunkSetEdge();
			break;
			//$CASES-OMITTED$ Hint
		default:
			chunkSet = null;
			break;
		}
		
		return chunkSet.violatesNot(mustEmpties, mustEmpties, firstUsedWord);
	}
	
	//-------------------------------------------------------------------------

	@Override
	public final boolean hasNoTests() 
	{
		return false;
	}

	@Override
	public final boolean onlyRequiresSingleMustEmpty() 
	{
		return true;
	}

	@Override
	public final boolean onlyRequiresSingleMustWho() 
	{
		return false;
	}

	@Override
	public final boolean onlyRequiresSingleMustWhat() 
	{
		return false;
	}
	
	@Override
	public final SiteType graphElementType()
	{
		return graphElementType;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Chunks of which at least one must be empty
	 */
	public final ChunkSet mustEmpties()
	{
		return mustEmpties;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toString()
	{
		String requirementsStr = "";
		
		for (int i = mustEmpties.nextSetBit(0); 
				i >= 0; i = mustEmpties.nextSetBit(i + 1)) 
		{
			requirementsStr += i + ", ";
		}
		
		return String.format(
				"One of these must be empty: [%s]", 
				requirementsStr);
	}
	
	//-------------------------------------------------------------------------

}
