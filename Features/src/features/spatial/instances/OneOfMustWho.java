package features.spatial.instances;

import game.types.board.SiteType;
import main.collections.ChunkSet;
import other.state.State;
import other.state.container.ContainerState;

/**
 * Simultaneously tests multiple chunks of the "who" ChunkSet.
 * 
 * TODO could make special cases of this class for cells, vertices, and edges
 * 
 * @author Dennis Soemers
 */
public final class OneOfMustWho implements BitwiseTest 
{
	
	//-------------------------------------------------------------------------
	
	/** 
	 * Set of chunks of which at least one must match game state's Who 
	 * ChunkSet for test to succeed.
	 */
	protected final ChunkSet mustWhos;
	
	/** Mask for must-who tests  */
	protected final ChunkSet mustWhosMask;
	
	/** The first non-zero word in the mustWhosMask ChunkSet */
	protected final int firstUsedWord;
	
	/** Graph element type we want to test on */
	protected final SiteType graphElementType;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * @param mustWhos
	 * @param mustWhosMask
	 * @param graphElementType 
	 */
	public OneOfMustWho(final ChunkSet mustWhos, final ChunkSet mustWhosMask, final SiteType graphElementType)
	{
		this.mustWhos = mustWhos;
		this.mustWhosMask = mustWhosMask;
		this.graphElementType = graphElementType;
		firstUsedWord = mustWhosMask.nextSetBit(0) / Long.SIZE;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public final boolean matches(final State state) 
	{
		final ContainerState container = state.containerStates()[0];
		switch (graphElementType)
		{
		case Cell:
			return (container.violatesNotWhoCell(mustWhosMask, mustWhos, firstUsedWord));
		case Vertex:
			return (container.violatesNotWhoVertex(mustWhosMask, mustWhos, firstUsedWord));
		case Edge:
			return (container.violatesNotWhoEdge(mustWhosMask, mustWhos, firstUsedWord));
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
	 * @return mustWhos ChunkSet
	 */
	public final ChunkSet mustWhos()
	{
		return mustWhos;
	}
	
	/**
	 * @return mustWhosMask ChunkSet
	 */
	public final ChunkSet mustWhosMask()
	{
		return mustWhosMask;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toString()
	{
		String requirementsStr = "";
		
		for (int i = 0; i < mustWhos.numChunks(); ++i)
		{
			if (mustWhosMask.getChunk(i) != 0)
			{
				requirementsStr += 
						i + " must belong to " + mustWhos.getChunk(i) + ", ";
			}
		}
		
		return String.format(
				"One of these who-conditions must hold: [%s]", 
				requirementsStr);
	}
	
	//-------------------------------------------------------------------------

}
