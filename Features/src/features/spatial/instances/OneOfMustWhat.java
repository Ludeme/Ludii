package features.spatial.instances;

import game.types.board.SiteType;
import main.collections.ChunkSet;
import other.state.State;
import other.state.container.ContainerState;

/**
 * Simultaneously tests multiple chunks of the "what" ChunkSet.
 * 
 * TODO could make special cases of this class for cells, vertices, and edges
 * 
 * @author Dennis Soemers
 */
public final class OneOfMustWhat implements BitwiseTest 
{
	
	//-------------------------------------------------------------------------
	
	/** 
	 * Set of chunks of which at least one must match game state's What 
	 * ChunkSet for test to succeed.
	 */
	protected final ChunkSet mustWhats;
	
	/** Mask for must-what tests  */
	protected final ChunkSet mustWhatsMask;
	
	/** The first non-zero word in the mustWhatsMask ChunkSet */
	protected final int firstUsedWord;
	
	/** Graph element type we want to test on */
	protected final SiteType graphElementType;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * @param mustWhats
	 * @param mustWhatsMask
	 * @param graphElementType 
	 */
	public OneOfMustWhat(final ChunkSet mustWhats, final ChunkSet mustWhatsMask, final SiteType graphElementType)
	{
		this.mustWhats = mustWhats;
		this.mustWhatsMask = mustWhatsMask;
		this.graphElementType = graphElementType;
		firstUsedWord = mustWhatsMask.nextSetBit(0) / Long.SIZE;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public final boolean matches(final State state) 
	{
		final ContainerState container = state.containerStates()[0];
		switch (graphElementType)
		{
		case Cell:
			return (container.violatesNotWhatCell(mustWhatsMask, mustWhats, firstUsedWord));
		case Vertex:
			return (container.violatesNotWhatVertex(mustWhatsMask, mustWhats, firstUsedWord));
		case Edge:
			return (container.violatesNotWhatEdge(mustWhatsMask, mustWhats, firstUsedWord));
			//$CASES-OMITTED$ Hint
		default:
			break;
		}
		
		return false;
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
		return false;
	}

	@Override
	public final boolean onlyRequiresSingleMustWho() 
	{
		return true;
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
	 * @return mustWhats ChunkSet
	 */
	public final ChunkSet mustWhats()
	{
		return mustWhats;
	}
	
	/**
	 * @return mustWhatsMask ChunkSet
	 */
	public final ChunkSet mustWhatsMask()
	{
		return mustWhatsMask;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toString()
	{
		String requirementsStr = "";
		
		for (int i = 0; i < mustWhats.numChunks(); ++i)
		{
			if (mustWhatsMask.getChunk(i) != 0)
			{
				requirementsStr += 
						i + " must contain " + mustWhats.getChunk(i) + ", ";
			}
		}
		
		return String.format(
				"One of these what-conditions must hold: [%s]", 
				requirementsStr);
	}
	
	//-------------------------------------------------------------------------

}
