package features.spatial.instances;

import game.Game;
import game.types.board.SiteType;
import main.collections.ChunkSet;
import other.state.State;

/**
 * A test that check for a single specific cell that must be owned by a specific player
 *
 * @author Dennis Soemers
 */
public class SingleMustWhoCell extends AtomicProposition
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
	 * @param mustWhoSite
	 * @param mustWhoValue
	 * @param chunkSize
	 */
	public SingleMustWhoCell(final int mustWhoSite, final int mustWhoValue, final int chunkSize)
	{
		// Using same logic as ChunkSet.setChunk() here to determine wordIdx, mask, and matchingWord
		final int bitIndex  = mustWhoSite * chunkSize;
		wordIdx = bitIndex >> 6;
		
		final int up = bitIndex & 63;
		mask = ((0x1L << chunkSize) - 1) << up;
		matchingWord = (((long)mustWhoValue) << up);
		
		this.site = mustWhoSite;
		this.value = mustWhoValue;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public boolean matches(final State state)
	{
		return state.containerStates()[0].matchesWhoCell(wordIdx, mask, matchingWord);
	}

	@Override
	public boolean onlyRequiresSingleMustEmpty()
	{
		return false;
	}

	@Override
	public boolean onlyRequiresSingleMustWho()
	{
		return true;
	}

	@Override
	public boolean onlyRequiresSingleMustWhat()
	{
		return false;
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
		return StateVectorTypes.Who;
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
		
		// True means we DO contain player we look for
		if (other.stateVectorType() == StateVectorTypes.Who)
			return (!other.negated() && value() == other.value());
		
		// We prove not-what for any piece not owned by player, and also prove what if this player only owns a single piece type
		if (other.stateVectorType() == StateVectorTypes.What)
		{
			if (other.negated())
				return (game.equipment().components()[other.value()].owner() != value());
			else
				return AtomicProposition.playerOnlyOwns(game, value(), other.value());
		}
		
		// True means we DO contain a specific player, so we prove not empty
		return (value() > 0 && other.stateVectorType() == StateVectorTypes.Empty && other.negated());
	}

	@Override
	public boolean disprovesIfTrue(final AtomicProposition other, final Game game)
	{
		if (graphElementType() != other.graphElementType())
			return false;
		
		if (testedSite() != other.testedSite())
			return false;
		
		// True means we DO contain player we look for
		if (other.stateVectorType() == StateVectorTypes.Who)
			return (other.negated() && value() == other.value());
		
		// We disprove what for any piece not owned by player, and also disprove not-what for piece if player only owns a single piece type
		if (other.stateVectorType() == StateVectorTypes.What)
		{
			if (other.negated())
				return AtomicProposition.playerOnlyOwns(game, value(), other.value());
			else
				return (game.equipment().components()[other.value()].owner() != value());
		}
		
		// True means we DO contain a specific player, so we disprove empty
		return (value() > 0 && other.stateVectorType() == StateVectorTypes.Empty && !other.negated());
	}

	@Override
	public boolean provesIfFalse(final AtomicProposition other, final Game game)
	{
		if (graphElementType() != other.graphElementType())
			return false;
		
		if (testedSite() != other.testedSite())
			return false;
		
		// We prove not-who
		if (other.stateVectorType() == StateVectorTypes.Who)
			return (other.negated() && other.value() == value());
		
		// We prove not-what for any what owned by this player
		if (other.stateVectorType() == StateVectorTypes.What && other.negated())
			return (AtomicProposition.ownedComponentIDs(game, value()).contains(other.value()));
		
		return false;
	}

	@Override
	public boolean disprovesIfFalse(final AtomicProposition other, final Game game)
	{
		if (graphElementType() != other.graphElementType())
			return false;
		
		if (testedSite() != other.testedSite())
			return false;
		
		// We disprove what for any what owned by this player
		if (other.stateVectorType() == StateVectorTypes.What && !other.negated())
			return (AtomicProposition.ownedComponentIDs(game, value()).contains(other.value()));
		
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

		if (!(obj instanceof SingleMustWhoCell))
			return false;
		
		final SingleMustWhoCell other = (SingleMustWhoCell) obj;
		return (mask == other.mask && matchingWord == other.matchingWord && wordIdx == other.wordIdx);
	}
	
	@Override
	public String toString()
	{
		return "[Cell " + site + " must be owned by Player " + value + "]";
	}
	
	//-------------------------------------------------------------------------

}
