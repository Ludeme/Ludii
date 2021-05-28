package other.state;

import java.io.Serializable;
import java.util.Arrays;

import game.Game;
import game.equipment.component.Component;
import main.Constants;

/**
 * A helper object for Owned structures, which allows us to map from
 * player + component indices into player + array indices, where the
 * array indices are in a smaller range than the component indices
 * (always starting from 0, always contiguous, only have legal mappings
 * for components that are actually owned by the corresponding player index).
 *
 * @author Dennis Soemers
 */
public final class OwnedIndexMapper implements Serializable
{
	
	//-------------------------------------------------------------------------
	
	/** */
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------
	
	/** 
	 * For (playerIdx, componentIdx), gives us the index that Owned should 
	 * use as a replacement for componentIdx 
	 */
	private final int[][] mappedIndices;
	
	/**
	 * A reverse map (giving us original component index back)
	 */
	private final int[][] reverseMap;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * @param game
	 */
	public OwnedIndexMapper(final Game game)
	{
		final Component[] components = game.equipment().components();
		final int fullPlayersDim = game.players().count() + 2;
		final int fullCompsDim = components.length;
		
		mappedIndices = new int[fullPlayersDim][fullCompsDim];
		reverseMap = new int[fullPlayersDim][];
		
		for (int p = 0; p < fullPlayersDim; ++p)
		{
			int nextIndex = 0;
			Arrays.fill(mappedIndices[p], Constants.UNDEFINED);
			
			for (int e = 0; e < fullCompsDim; ++e)
			{
				final Component comp = components[e];
				if (comp != null && comp.owner() == p)
					mappedIndices[p][e] = nextIndex++;
			}
			
			reverseMap[p] = new int[nextIndex];
			for (int i = 0; i < mappedIndices[p].length; ++i)
			{
				if (mappedIndices[p][i] >= 0)
					reverseMap[p][mappedIndices[p][i]] = i;
			}
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param playerIdx
	 * @param origCompIdx
	 * @return Index that we should use for given original comp index, for given player index.
	 */
	public final int compIndex(final int playerIdx, final int origCompIdx)
	{
		return mappedIndices[playerIdx][origCompIdx];
	}
	
	/**
	 * @param playerIdx
	 * @return Array of indices we should use for all possible components for given player index.
	 */
	public final int[] playerCompIndices(final int playerIdx)
	{
		return mappedIndices[playerIdx];
	}
	
	/**
	 * @param playerIdx
	 * @return Number of valid component indices for given player index.
	 */
	public final int numValidIndices(final int playerIdx)
	{
		return reverseMap[playerIdx].length;
	}
	
	/**
	 * @param playerIdx
	 * @param mappedIndex
	 * @return Reverses a mapped index back into a component index.
	 */
	public final int reverseMap(final int playerIdx, final int mappedIndex)
	{
		return reverseMap[playerIdx][mappedIndex];
	}
	
	//-------------------------------------------------------------------------

}
