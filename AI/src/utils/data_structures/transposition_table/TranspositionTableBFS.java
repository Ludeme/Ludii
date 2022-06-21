package utils.data_structures.transposition_table;


/**
 * Transposition table for BFS search.
 * 
 * copied from the classic Transposition table
 * 
 * @author cyprien
 */

public class TranspositionTableBFS
{
	
	//-------------------------------------------------------------------------
	
	/** An invalid value stored in Transposition Table */
	public final static byte INVALID_VALUE			= (byte) 0x0;
	/** An exact (maybe heuristic) value stored in Transposition Table */
	public final static byte EXACT_VALUE			= (byte) 0x1;
	
	// No values for upper or lower bounds
	
	//-------------------------------------------------------------------------
	
	/** Number of bits from hashes to use as primary coed */
	private final int numBitsPrimaryCode;
	
	/** Max number of entries for which we've allocated space */
	private final int maxNumEntries;
	
	/** Our table of entries */
	private BFSTTEntry[] table;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor.
	 * 
	 * NOTE: does not yet allocate memory!
	 * 
	 * @param numBitsPrimaryCode Number of bits from hashes to use as primary code.
	 */
	public TranspositionTableBFS(final int numBitsPrimaryCode)
	{
		this.numBitsPrimaryCode = numBitsPrimaryCode;
		maxNumEntries = 1 << numBitsPrimaryCode;
		table = null;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Allocates a brand new table with space for 2^(numBitsPrimaryCode) entries.
	 */
	public void allocate()
	{
		table = new BFSTTEntry[maxNumEntries];
	}
	
	/**
	 * Clears up all memory of our table
	 */
	public void deallocate()
	{
		table = null;
	}
	
	/**
	 * @param fullHash
	 * @return Stored data for given full hash (full 64bits code), or null if not found
	 */
	public BFSTTData retrieve(final long fullHash)
	{
		final BFSTTEntry entry = table[(int) (fullHash >>> (Long.SIZE - numBitsPrimaryCode))];
		if (entry == null)
			return null;
		if (entry.data != null && entry.data.fullHash == fullHash)
			return entry.data;
		return null;
	}
	
	/**
	 * Stores new data for given full hash (full 64bits code)
	 * @param fullHash
	 * @param value
	 * @param valueType 
	 */
	public void store
	(
		final long fullHash,
		final float value,
		final byte valueType
	)
	{
		final int idx = (int) (fullHash >>> (Long.SIZE - numBitsPrimaryCode));
		BFSTTEntry entry = table[idx];
		
		if (entry == null)
		{
			entry = new BFSTTEntry();
			entry.data = new BFSTTData(fullHash, value, valueType);
			table[idx] = entry;
		}
		else
		{
			// See if we have an empty slot in data
			if (entry.data == null)
			{
				entry.data = new BFSTTData(fullHash, value, valueType);
				return;
			}

			
			// Check if one of them has an identical full hash value
			if (entry.data.fullHash == fullHash)
			{
				// We suppose that the method would only be called if the new value is more accurate than the previous one
				
				entry.data.fullHash = fullHash;
				entry.data.value = value;
				entry.data.valueType = valueType;
				
				return;
			}
			
			// The slot is already filled, so its value is replaced
			entry.data.fullHash = fullHash;
			entry.data.value = value;
			entry.data.valueType = valueType;
		}
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Data we wish to store in TT entries for Alpha-Beta Search
	 * 
	 * @author cyprien
	 */
	public static final class BFSTTData
	{
		
		/** Full 64bits hash code for which this data was stored */
		public long fullHash = -1L;
		
		/** The (maybe heuristic) value stored in Table */
		public float value = Float.NaN;
		
		/** The type of value stored */
		public byte valueType = INVALID_VALUE;
		
		/**
		 * Constructor
		 * @param fullHash
		 * @param value
		 * @param valueType
		 */
		public BFSTTData
		(
			final long fullHash,
			final float value,
			final byte valueType
		)
		{
			this.fullHash = fullHash;
			this.value = value;
			this.valueType = valueType;
		}
		
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * An entry in a Transposition Table for Alpha-Beta. Every entry contains
	 * one slot.
	 * NOTE: useless to distinguish from a data if there is only one slot
	 * but we keep it to make the code similar to the one for alpha-beta
	 *
	 * @author cmicheld
	 */
	public static final class BFSTTEntry
	{
		
		/** Data in our entry's first (and only) slot */
		public BFSTTData data = null;
		
	}
	
	//-------------------------------------------------------------------------

}
