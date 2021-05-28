package features.spatial.instances;

import game.Game;
import game.types.board.SiteType;
import main.collections.ChunkSet;
import other.state.State;

/**
 * A test that check for a single specific edge that must be not owned by a specific player
 *
 * @author Dennis Soemers
 */
public class SingleMustNotWhoEdge extends AtomicProposition
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
	 * @param mustNotWhoSite
	 * @param mustNotWhoValue
	 * @param chunkSize
	 */
	public SingleMustNotWhoEdge(final int mustNotWhoSite, final int mustNotWhoValue, final int chunkSize)
	{
		// Using same logic as ChunkSet.setChunk() here to determine wordIdx, mask, and matchingWord
		final int bitIndex  = mustNotWhoSite * chunkSize;
		wordIdx = bitIndex >> 6;
		
		final int up = bitIndex & 63;
		mask = ((0x1L << chunkSize) - 1) << up;
		matchingWord = (((long)mustNotWhoValue) << up);
		
		this.site = mustNotWhoSite;
		this.value = mustNotWhoValue;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public boolean matches(final State state)
	{
		return !state.containerStates()[0].matchesWhoEdge(wordIdx, mask, matchingWord);
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
		return false;
	}

	@Override
	public SiteType graphElementType()
	{
		return SiteType.Edge;
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
		return true;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public boolean provesIfTrue(final AtomicProposition other, final Game game)
	{
		if (graphElementType() != other.graphElementType())
			return false;
		
		if (testedSite() != other.testedSite())
			return false;
		
		// If not who is true, we also prove not what for any what owned by the player
		if (other.stateVectorType() == StateVectorTypes.What && other.negated())
			return (AtomicProposition.ownedComponentIDs(game, value()).contains(other.value()));
		
		return false;
	}

	@Override
	public boolean disprovesIfTrue(final AtomicProposition other, final Game game)
	{
		if (graphElementType() != other.graphElementType())
			return false;
		
		if (testedSite() != other.testedSite())
			return false;
		
		// If not who is true, we disprove what for any what owned by the player
		if (other.stateVectorType() == StateVectorTypes.What && !other.negated())
			return (AtomicProposition.ownedComponentIDs(game, value()).contains(other.value()));
		
		// Not containing a specific player disproves that we contain that same specific player
		return (other.stateVectorType() == StateVectorTypes.Who && other.value() == value());
	}

	@Override
	public boolean provesIfFalse(final AtomicProposition other, final Game game)
	{
		if (graphElementType() != other.graphElementType())
			return false;
		
		if (testedSite() != other.testedSite())
			return false;
		
		// False means we DO contain player we look for
		if (other.stateVectorType() == StateVectorTypes.Who)
			return (!other.negated() && value() == other.value());
		
		// If the tested player only owns a single piece type, we also prove what for that piece type
		if (other.stateVectorType() == StateVectorTypes.What && !other.negated())
			return (AtomicProposition.playerOnlyOwns(game, value(), other.value()));
		
		// We prove not-what for any what not owned by player
		if (other.stateVectorType() == StateVectorTypes.What && other.negated())
			return (!AtomicProposition.ownedComponentIDs(game, value()).contains(other.value()));
		
		// False means we DO contain a specific player, so we prove not empty
		return (value() > 0 && other.stateVectorType() == StateVectorTypes.Empty && other.negated());
	}

	@Override
	public boolean disprovesIfFalse(final AtomicProposition other, final Game game)
	{
		if (graphElementType() != other.graphElementType())
			return false;
		
		if (testedSite() != other.testedSite())
			return false;
		
		// False means we DO contain player we look for
		if (other.stateVectorType() == StateVectorTypes.Who)
			return (other.negated() && value() == other.value());
		
		// If player owns only a single piece type, we disprove not-what for that piece type
		if (other.stateVectorType() == StateVectorTypes.What && other.negated())
			return (AtomicProposition.playerOnlyOwns(game, value(), other.value()));
		
		// We disprove what for any what not owned by this player
		if (other.stateVectorType() == StateVectorTypes.What && !other.negated())
			return (!AtomicProposition.ownedComponentIDs(game, value()).contains(other.value()));
		
		// False means we DO contain a specific player, so we disprove empty
		return (value() > 0 && other.stateVectorType() == StateVectorTypes.Empty && !other.negated());
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

		if (!(obj instanceof SingleMustNotWhoEdge))
			return false;
		
		final SingleMustNotWhoEdge other = (SingleMustNotWhoEdge) obj;
		return (mask == other.mask && matchingWord == other.matchingWord && wordIdx == other.wordIdx);
	}
	
	@Override
	public String toString()
	{
		return "[Edge " + site + " must NOT be owned by Player " + value + "]";
	}
	
	//-------------------------------------------------------------------------

}
