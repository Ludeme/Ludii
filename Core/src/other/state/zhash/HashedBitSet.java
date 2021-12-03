package other.state.zhash;

import java.io.Serializable;
import java.util.BitSet;

import other.state.State;

/**
 * Wrapper around ChunkSet, to make sure it is managed correctly
 * If ChunkSet were an interface, I'd use the decorator pattern...
 * 
 * @author mrraow
 */
public class HashedBitSet implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	/** Which sites are hidden to which players. */
	private final BitSet internalState;
	private final long[] hashes;
	
	/**
	 * @param generator
	 * @param numSites
	 */
	public HashedBitSet(final ZobristHashGenerator generator, final int numSites) 
	{
		internalState = new BitSet(numSites);
		hashes = ZobristHashUtilities.getSequence(generator, numSites);
	}

	/**
	 * Copy constructor, used by clone()
	 * @param that
	 */
	private HashedBitSet(final HashedBitSet that) 
	{
		this.internalState = (BitSet) that.internalState.clone();
		this.hashes = that.hashes;	// Safe to just store a reference
	}

	/* ----------------------------------------------------------------------------------------------------
	 * The following methods change state, and therefore need to manage the hash value
	 * ---------------------------------------------------------------------------------------------------- */

	/**
	 * Performance warning - this method will iterate through all sites to update
	 * the hash, so avoid at runtime
	 * 
	 * @param trialState
	 */
	public void clear(final State trialState) 
	{
		long hashDelta = 0L;
		for (int site = internalState.nextSetBit(0); site >= 0; site = internalState.nextSetBit(site + 1))
		{
			hashDelta ^= hashes[site];
		}

		internalState.clear();
				
		trialState.updateStateHash(hashDelta);
	}

	/**
	 * Calculates the hash of this object after the specified remapping. Either the
	 * sites, or the values, will be remapped and the new hash calculated. Intended
	 * for calculating canonical hashes.
	 * 
	 * Performance Warning: this is slow; do not call during a search.
	 * 
	 * @param siteRemap may be null
	 * @param invert
	 * @return the hash of this chunk, when subjected to the specified
	 *         transformation
	 */
	public long calculateHashAfterRemap (final int[] siteRemap, final boolean invert)
	{
		long hashDelta = 0L;
		
		if (siteRemap == null)
		{
			for (int site = 0; site < hashes.length; site++) 
			{
				final boolean siteValue = internalState.get(site);
				final boolean newValue = invert ? !siteValue : siteValue;
				if (newValue) hashDelta ^= hashes[site];
			}

			return hashDelta;
		}

		for (int site = 0; site < hashes.length; site++) 
		{
			final int newSite = siteRemap[site];
			final boolean siteValue = internalState.get(site);
			final boolean newValue = invert ? !siteValue : siteValue;
			if (newValue) hashDelta ^= hashes[newSite];
		}

		return hashDelta;
	}
	
	/**
	 * Makes this a copy of src - equivalent to clear followed by or
	 * Performance warning - this method will iterate through all sites to update the hash, so avoid at runtime
	 * @param trialState
	 * @param src
	 */
	public void setTo(final State trialState, final HashedBitSet src) 
	{
		// Possible performance improvement: calculate xor of two bitsets, iterate through all 1s in resultant set
		long hashDelta = 0L;
		for (int site = 0; site < hashes.length; site++)
			if (internalState.get(site) != src.internalState.get(site))
				hashDelta ^=  hashes[site];
		
		internalState.clear();
		internalState.or(src.internalState);
		
		trialState.updateStateHash(hashDelta);
	}
	
	/**
	 * To set the bits
	 * 
	 * @param trialState
	 * @param bitIndex
	 * @param on
	 */
	public void set(final State trialState, final int bitIndex, final boolean on) 
	{
		if (on != internalState.get(bitIndex)) trialState.updateStateHash(hashes[bitIndex]);		
		internalState.set(bitIndex, on);
	}
	
/* ----------------------------------------------------------------------------------------------------
 * The following methods are read-only, and do not need to manage their internal states
 * ---------------------------------------------------------------------------------------------------- */

	@Override
	public HashedBitSet clone()
	{
		return new HashedBitSet(this);
	}
	
	/**
	 * @return copy of the internal state of this hashed bitset
	 */
	public BitSet internalStateCopy() { return (BitSet) internalState.clone(); }
	

	/**
	 * @return The internal state of this hashed bitset
	 */
	public BitSet internalState() { return internalState; }

	/**
	 * @param bitIndex The index of the bit.
	 * @return The bit value.
	 * @see #util.ChunkSet.get(int)
	 */
	public boolean get (final int bitIndex) { return internalState.get(bitIndex); }	
	
	/**
	 * @param fromIndex
	 * @return The index of the first bit that is set to true
	 * 	that occurs on or after the specified starting index. 
	 *  If no such bit exists then -1 is returned. 
	 */
	public int nextSetBit(final int fromIndex)
	{
		return internalState.nextSetBit(fromIndex);
	}
}
