package utils.data_structures.transposition_table;

import other.move.Move;

/**
 * Transposition table for Alpha-Beta search.
 * 
 * @author Dennis Soemers
 */
public class TranspositionTable
{
	
	//-------------------------------------------------------------------------
	
	/** An invalid value stored in Transposition Table */
	public final static byte INVALID_VALUE			= (byte) 0x0;
	/** An exact (maybe heuristic) value stored in Transposition Table */
	public final static byte EXACT_VALUE			= (byte) 0x1;
	/** A lower bound stored in Transposition Table */
	public final static byte LOWER_BOUND			= (byte) (0x1 << 1);
	/** A upper bound stored in Transposition Table */
	public final static byte UPPER_BOUND			= (byte) (0x1 << 2);
	
	//-------------------------------------------------------------------------
	
	/** Number of bits from hashes to use as primary code */
	private final int numBitsPrimaryCode;
	
	/** Max number of entries for which we've allocated space */
	private final int maxNumEntries;
	
	/** Our table of entries */
	private ABTTEntry[] table;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor.
	 * 
	 * NOTE: does not yet allocate memory!
	 * 
	 * @param numBitsPrimaryCode Number of bits from hashes to use as primary code.
	 */
	public TranspositionTable(final int numBitsPrimaryCode)
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
		table = new ABTTEntry[maxNumEntries];
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
	public ABTTData retrieve(final long fullHash)
	{
		final ABTTEntry entry = table[(int) (fullHash >>> (Long.SIZE - numBitsPrimaryCode))];
		if (entry == null)
			return null;
		if (entry.data1 != null && entry.data1.fullHash == fullHash)
			return entry.data1;
		if (entry.data2 != null && entry.data2.fullHash == fullHash)
			return entry.data2;
		return null;
	}
	
	/**
	 * Stores new data for given full hash (full 64bits code)
	 * @param bestMove 
	 * @param fullHash
	 * @param value 
	 * @param depth 
	 * @param valueType 
	 */
	public void store
	(
		final Move bestMove, 
		final long fullHash,
		final float value,
		final int depth,
		final byte valueType
	)
	{
		final int idx = (int) (fullHash >>> (Long.SIZE - numBitsPrimaryCode));
		ABTTEntry entry = table[idx];
		
		if (entry == null)
		{
			entry = new ABTTEntry();
			entry.data1 = new ABTTData(bestMove, fullHash, value, depth, valueType);
			table[idx] = entry;
		}
		else
		{
			// See if we have empty slots in data
			if (entry.data1 == null)
			{
				entry.data1 = new ABTTData(bestMove, fullHash, value, depth, valueType);
				return;
			}
			else if (entry.data2 == null)
			{
				entry.data2 = new ABTTData(bestMove, fullHash, value, depth, valueType);
				return;
			}
			
			// Check if one of them has an identical full hash value, and if so, 
			// prefer data with largest search depth
			if (entry.data1.fullHash == fullHash)
			{
				if (depth > entry.data1.depth)	// Only change data if we've searched to a deeper depth now
				{
					entry.data1.bestMove = bestMove;
					entry.data1.depth = depth;
					entry.data1.fullHash = fullHash;
					entry.data1.value = value;
					entry.data1.valueType = valueType;
				}
		
				return;
			}
			else if (entry.data2.fullHash == fullHash)
			{
				if (depth > entry.data2.depth)	// Only change data if we've searched to a deeper depth now
				{
					entry.data2.bestMove = bestMove;
					entry.data2.depth = depth;
					entry.data2.fullHash = fullHash;
					entry.data2.value = value;
					entry.data2.valueType = valueType;
				}
		
				return;
			}
			
			// Both slots already filled, so replace whichever has the lowest depth
			if (entry.data1.depth < entry.data2.depth)				// Slot 1 searched less deep than slot 2, so replace that
			{
				entry.data1.bestMove = bestMove;
				entry.data1.depth = depth;
				entry.data1.fullHash = fullHash;
				entry.data1.value = value;
				entry.data1.valueType = valueType;
			}
			else if (entry.data2.depth < entry.data1.depth)			// Slot 2 searched less deep than slot 1, so replace that
			{
				entry.data2.bestMove = bestMove;
				entry.data2.depth = depth;
				entry.data2.fullHash = fullHash;
				entry.data2.value = value;
				entry.data2.valueType = valueType;
			}
			else			// Both existing slots have equal search depth, move data from 1 to 2 and then replace 1
			{
				entry.data2.bestMove = entry.data1.bestMove;
				entry.data2.depth = entry.data1.depth;
				entry.data2.fullHash = entry.data1.fullHash;
				entry.data2.value = entry.data1.value;
				entry.data2.valueType = entry.data1.valueType;
		
				entry.data1.bestMove = bestMove;
				entry.data1.depth = depth;
				entry.data1.fullHash = fullHash;
				entry.data1.value = value;
				entry.data1.valueType = valueType;
			}
		}
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Data we wish to store in TT entries for Alpha-Beta Search
	 * 
	 * @author Dennis Soemers
	 */
	public static final class ABTTData
	{
		
		/** The best move according to previous AB search */
		public Move bestMove = null;
		
		/** Full 64bits hash code for which this data was stored */
		public long fullHash = -1L;
		
		/** The (maybe heuristic) value stored in Table */
		public float value = Float.NaN;
		
		/** The depth to which the search tree was explored below the node corresponding to this data */
		public int depth = -1;
		
		/** The type of value stored */
		public byte valueType = INVALID_VALUE;
		
		/**
		 * Constructor
		 * @param bestMove
		 * @param fullHash
		 * @param value
		 * @param depth
		 * @param valueType
		 */
		public ABTTData
		(
			final Move bestMove, 
			final long fullHash,
			final float value,
			final int depth,
			final byte valueType
		)
		{
			this.bestMove = bestMove;
			this.fullHash = fullHash;
			this.value = value;
			this.depth = depth;
			this.valueType = valueType;
		}
		
	}
	
	//-------------------------------------------------------------------------
	
	public int nbEntries()
	// (counts entries with double data as 1 entry)
	{
		int res = 0;
		for (int i=0; i<maxNumEntries; i++)
		{
			if (table[i]!=null)
				res += 1;
		}
		return res;
	}
	
	public void dispValueStats()
	// (counts entries with double data as 1 entry)
	{
		int maxDepth = 0;
		for (int i=0; i<maxNumEntries; i++)
		{
			if (table[i]!=null)
			{
				if (table[i].data1!=null)
				{
					if (table[i].data1.depth>maxDepth)
						maxDepth = table[i].data1.depth;
				}
				if (table[i].data2!=null)
				{
					if (table[i].data2.depth>maxDepth)
						maxDepth = table[i].data2.depth;
				}
			}
		}
		
		int[][] counters = new int[maxDepth+1][6];
		for (int i=0; i<maxNumEntries; i++)
		{
			if (table[i]!=null)
			{
				if (table[i].data1!=null)
				{
					int index = table[i].data1.valueType;
					switch (index)
					{
					case 0: break;
					case 1: break;
					case 2: break;
					case 4: break;
					default: index = 5;
					}
					counters[table[i].data1.depth][index] += 1;
				}
				if (table[i].data2!=null)
				{
					int index = table[i].data2.valueType;
					switch (index)
					{
					case 0: break;
					case 1: break;
					case 2: break;
					case 4: break;
					default: index = 5;
					}
					counters[table[i].data2.depth][index] += 1;
				}
			}
		}
		
		System.out.println("Search tree analysis:");
		for (int i=1; i<maxDepth; i++)
		{
			System.out.print("At depth "+i+": ");
			for (int k : new int[]{0,1,2,4})
			{
				System.out.print("value "+k+": "+counters[i][k]+", ");
			}
			System.out.println();
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * An entry in a Transposition Table for Alpha-Beta. Every entry contains
	 * two slots.
	 *
	 * @author Dennis Soemers
	 */
	public static final class ABTTEntry
	{
		
		/** Data in our entry's first slot */
		public ABTTData data1 = null;
		/** Data in our entry's second slot */
		public ABTTData data2 = null;
		
	}
	
	//-------------------------------------------------------------------------

}
