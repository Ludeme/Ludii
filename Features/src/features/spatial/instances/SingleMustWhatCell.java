package features.spatial.instances;

import game.Game;
import game.types.board.SiteType;
import main.collections.ChunkSet;
import other.state.State;

/**
 * A test that check for a single specific cell that must contain a specific value
 *
 * @author Dennis Soemers
 */
public class SingleMustWhatCell extends AtomicProposition
{
	
	//-------------------------------------------------------------------------
	
	/** The index of the word that we want to match */
	protected final int wordIdx;
	
	/** The mask that we want to apply to the word when matching */
	protected final long mask;
	
	/** The word that we should match after masking */
	protected final long matchingWord;
	
	/** The site we look at */
	protected final int site;
	
	/** The value we look for in chunkset */
	protected final int value;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * @param mustWhatSite
	 * @param mustWhatValue
	 * @param chunkSize
	 */
	public SingleMustWhatCell(final int mustWhatSite, final int mustWhatValue, final int chunkSize)
	{
		// Using same logic as ChunkSet.setChunk() here to determine wordIdx, mask, and matchingWord
		final int bitIndex  = mustWhatSite * chunkSize;
		wordIdx = bitIndex >> 6;
		
		final int up = bitIndex & 63;
		mask = ((0x1L << chunkSize) - 1) << up;
		matchingWord = (((long)mustWhatValue) << up);
		
		this.site = mustWhatSite;
		this.value = mustWhatValue;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public boolean matches(final State state)
	{
		return state.containerStates()[0].matchesWhatCell(wordIdx, mask, matchingWord);
	}

	@Override
	public boolean onlyRequiresSingleMustEmpty()
	{
		return false;
	}

	@Override
	public boolean onlyRequiresSingleMustWho()
	{
		return false;
	}

	@Override
	public boolean onlyRequiresSingleMustWhat()
	{
		return true;
	}

	@Override
	public SiteType graphElementType()
	{
		return SiteType.Cell;
	}
	
	@Override
	public void addMaskTo(final ChunkSet chunkSet)
	{
		chunkSet.addMask(wordIdx, mask);
	}
	
	@Override
	public StateVectorTypes stateVectorType()
	{
		return StateVectorTypes.What;
	}
	
	@Override
	public int testedSite()
	{
		return site;
	}
	
	@Override
	public int value()
	{
		return value;
	}
	
	@Override
	public boolean negated()
	{
		return false;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public boolean provesIfTrue(final AtomicProposition other, final Game game)
	{
		if (graphElementType() != other.graphElementType())
			return false;
		
		if (testedSite() != other.testedSite())
			return false;
		
		// True means we DO contain a specific piece, so we prove that we contain it and prove that we do NOT contain something else
		if (other.stateVectorType() == StateVectorTypes.What)
		{
			if (other.negated())
				return (value() != other.value());
			else
				return (value() == other.value());
		}
		
		// We prove who for owner of piece type, and not who for any other player
		if (other.stateVectorType() == StateVectorTypes.Who)
		{
			if (other.negated())
				return (other.value() != game.equipment().components()[value()].owner());
			else
				return (other.value() == game.equipment().components()[value()].owner());
		}
		
		// True means we DO contain a specific piece, so we prove not empty
		return (value() > 0 && other.stateVectorType() == StateVectorTypes.Empty && other.negated());
	}

	@Override
	public boolean disprovesIfTrue(final AtomicProposition other, final Game game)
	{
		if (graphElementType() != other.graphElementType())
			return false;
		
		if (testedSite() != other.testedSite())
			return false;
		
		// True means we DO contain a specific piece, so we disprove that we don't contain it, and disprove containing any other piece
		if (other.stateVectorType() == StateVectorTypes.What)
		{
			if (other.negated())
				return (value() == other.value());
			else
				return (value() != other.value());
		}
		
		// We disprove not who for owner, and who for any other player
		if (other.stateVectorType() == StateVectorTypes.Who)
		{
			if (other.negated())
				return (other.value() == game.equipment().components()[value()].owner());
			else
				return (other.value() != game.equipment().components()[value()].owner());
		}
		
		// True means we DO contain a specific piece, so we disprove empty
		return (value() > 0 && other.stateVectorType() == StateVectorTypes.Empty && !other.negated());
	}

	@Override
	public boolean provesIfFalse(final AtomicProposition other, final Game game)
	{
		if (graphElementType() != other.graphElementType())
			return false;
		
		if (testedSite() != other.testedSite())
			return false;
		
		// If this is the only piece type owned by its owner, we prove not-who for that owner
		if (AtomicProposition.ownerOnlyOwns(game, value()))
			return (other.stateVectorType() == StateVectorTypes.Who && other.negated() && other.value() == game.equipment().components()[value()].owner());
		
		return false;
	}

	@Override
	public boolean disprovesIfFalse(final AtomicProposition other, final Game game)
	{
		if (graphElementType() != other.graphElementType())
			return false;
		
		if (testedSite() != other.testedSite())
			return false;
		
		// If this is the only piece type owned by its owner, we disprove who for that owner
		if (AtomicProposition.ownerOnlyOwns(game, value()))
			return (other.stateVectorType() == StateVectorTypes.Who && !other.negated() && other.value() == game.equipment().components()[value()].owner());
		
		return false;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (mask ^ (mask >>> 32));
		result = prime * result + (int) (matchingWord ^ (matchingWord >>> 32));
		result = prime * result + wordIdx;
		return result;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
			return true;

		if (!(obj instanceof SingleMustWhatCell))
			return false;
		
		final SingleMustWhatCell other = (SingleMustWhatCell) obj;
		return (mask == other.mask && matchingWord == other.matchingWord && wordIdx == other.wordIdx);
	}
	
	@Override
	public String toString()
	{
		return "[Cell " + site + " must contain " + value + "]";
	}
	
	//-------------------------------------------------------------------------

}
