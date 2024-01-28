package other.state.zhash;

import java.io.Serializable;

import main.collections.ChunkSet;
import main.math.BitTwiddling;
import other.state.State;

//-----------------------------------------------------------------------------

/**
 * Wrapper around ChunkSet, to make sure it is managed correctly
 * If ChunkSet were an interface, I'd use the decorator pattern...
 * 
 * @author mrraow
 */
public class HashedChunkSet implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	/** Which sites are hidden to which players. */
	private final ChunkSet internalState;
	private final long[][] hashes;
	
	/**
	 * @param generator
	 * @param maxChunkVal
	 * @param numChunks
	 */
	public HashedChunkSet(final ZobristHashGenerator generator, final int maxChunkVal, final int numChunks) 
	{
		internalState = new ChunkSet(BitTwiddling.nextPowerOf2(BitTwiddling.bitsRequired(maxChunkVal)), numChunks);
		hashes = ZobristHashUtilities.getSequence(generator, numChunks, maxChunkVal + 1);
	}

	/**
	 * @param hashes
	 * @param maxChunkVal
	 * @param numSites
	 */
	public HashedChunkSet(final long[][] hashes, final int maxChunkVal, final int numSites) 
	{
		internalState = new ChunkSet(BitTwiddling.nextPowerOf2(BitTwiddling.bitsRequired(maxChunkVal)), numSites);
		this.hashes = hashes;
	}
	
	/**
	 * Copy constructor, used by clone()
	 * @param that
	 */
	private HashedChunkSet(final HashedChunkSet that) 
	{
		this.internalState = that.internalState.clone();
		this.hashes = that.hashes;	// Safe to just store a reference
	}

	/* ----------------------------------------------------------------------------------------------------
	 * The following methods change state, and therefore need to manage the hash value
	 * ---------------------------------------------------------------------------------------------------- */

	/** 
	 * Performance warning - this method will iterate through all sites to update the hash, so avoid at runtime
	 * @param trialState 
	 */
	public void clear(final State trialState) 
	{
		long hashDelta = 0L;
		for (int site = 0; site < hashes.length; site++)
			hashDelta ^= hashes[site][internalState.getChunk(site)];
		
		internalState.clear();
		
		trialState.updateStateHash(hashDelta);
	}

	/**
	 * Calculates the hash of this object after the specified remapping.
	 * Either the sites, or the values, or possibly both will be remapped and the new hash calculated.
	 * Intended for calculating canonical hashes.
	 * BUGFIX: the length of the set may be longer than the remap values.  This occurs when connections have been modified, and the graph compacted.
	 * 
	 * Performance Warning: this is slow; do not call during a search.
	 * 
	 * @param siteRemap may be null
	 * @param valueRemap may be null
	 * @return the hash of this chunk, when subjected to the specified transformation
	 */
	public long calculateHashAfterRemap (final int[] siteRemap, final int[] valueRemap)
	{
		long hashDelta = 0L;
		
		if (valueRemap == null)
		{
			for (int site = 0; site < hashes.length && site < siteRemap.length; site++) 
			{
				final int newSite = siteRemap[site];
				final int siteValue = internalState.getChunk(site);
				hashDelta ^= hashes[newSite][siteValue] ^ hashes[newSite][0];
			}
			
			return hashDelta;
		}
		
		if (siteRemap == null)
		{
			for (int site = 0; site < hashes.length; site++) 
			{
				final int siteValue = internalState.getChunk(site);
				final int newValue = valueRemap[siteValue];
				hashDelta ^= hashes[site][newValue] ^ hashes[site][0];
			}

			return hashDelta;
		}
		
		for (int site = 0; site < hashes.length && site < siteRemap.length; site++) 
		{
			final int siteValue = internalState.getChunk(site);
			final int newValue = valueRemap[siteValue];
			final int newSite = siteRemap[site];
			hashDelta ^= hashes[newSite][newValue] ^ hashes[newSite][0];
		}

		return hashDelta;
	}
	
	/**
	 * Makes this a copy of src - equivalent to clear followed by or
	 * Performance warning - this method will iterate through all sites to update the hash, so avoid at runtime
	 * @param trialState
	 * @param src
	 */
	public void setTo(final State trialState, final HashedChunkSet src) 
	{
		long hashDelta = 0L;
		for (int site = 0; site < hashes.length; site++)
			hashDelta ^= hashes[site][internalState.getChunk(site)] ^ hashes[site][src.internalState.getChunk(site)];
		
		internalState.clear();
		internalState.or(src.internalState);
		
		trialState.updateStateHash(hashDelta);
	}
	
	/**
	 * @param trialState
	 * @param chunk
	 * @param bit
	 * @param value
	 */
	public void setBit(final State trialState, final int chunk, final int bit, final boolean value) 
	{
		long hashDelta = hashes[chunk][internalState.getChunk(chunk)];		
		internalState.setBit(chunk, bit, value);
		hashDelta ^= hashes[chunk][internalState.getChunk(chunk)];

		trialState.updateStateHash(hashDelta);
	}

	/** 
	 * @param trialState 
	 * @param site 
	 * @param val 
	 */
	public void setChunk(final State trialState, final int site, final int val)
	{
		long hashDelta = hashes[site][internalState.getAndSetChunk(site, val)];		
		hashDelta ^= hashes[site][val];

		trialState.updateStateHash(hashDelta);
	}

/* ----------------------------------------------------------------------------------------------------
 * The following methods are read-only, and do not need to manage their internal states
 * ---------------------------------------------------------------------------------------------------- */

	@Override
	public HashedChunkSet clone()
	{
		return new HashedChunkSet(this);
	}

	/**
	 * @return copy of the internal state of this hashed chunkset
	 */
	public ChunkSet internalStateCopy() { return internalState.clone(); }

	/**
	 * @param site     The site.
	 * @param location The location.
	 * @return The bit.
	 * @see #util.ChunkSet.getBit(int, int)
	 */
	public int getBit (final int site, final int location) { return internalState.getBit(site, location); }
	
	/**
	 * @param site The site.
	 * @return The chunk.
	 * @see #util.ChunkSet.getChunk(int)
	 */
	public int getChunk (final int site) { return internalState.getChunk(site); }

	/**
	 * @return The number of chunks.
	 * @see #util.ChunkSet.numChunks()
	 */
	public int numChunks() { return internalState.numChunks(); }

	/**
	 * @param mask    The mask.
	 * @param pattern The pattern.
	 * @return True if the pattern matched.
	 * @see #util.ChunkSet.matches(ChunkSet, ChunkSet)
	 */
	public boolean matches(final ChunkSet mask, final ChunkSet pattern) { return internalState.matches(mask, pattern); }
	
	/**
	 * @param wordIdx
	 * @param mask
	 * @param matchingWord
	 * @return True if the word at wordIdx, after masking by mask, matches the given word
	 */
	public boolean matches(final int wordIdx, final long mask, final long matchingWord) 
	{ 
		return internalState.matchesWord(wordIdx, mask, matchingWord); 
	}
	
	/**
	 * @return The size of the chunk.
	 * @see #util.ChunkSet.chunkSize()
	 */
	public int chunkSize() { return internalState.chunkSize(); }
	
	/**
	 * @param mask    The chunkset mask.
	 * @param pattern The chunkset pattern
	 * @return True if this is not violated.
	 * @see #util.ChunkSet.violatesNot(ChunkSet, ChunkSet)
	 */
	public boolean violatesNot(final ChunkSet mask, final ChunkSet pattern) { return internalState.violatesNot(mask, pattern); }
	
	/**
	 * @param mask      The mask chunkset.
	 * @param pattern   The pattern chunkset.
	 * @param startWord The word to start.
	 * @return True if this is not violated.
	 * @see #util.ChunkSet.violatesNot(ChunkSet, ChunkSet, int)
	 */
	public boolean violatesNot(final ChunkSet mask, final ChunkSet pattern, final int startWord) { return internalState.violatesNot(mask, pattern, startWord); }

}
