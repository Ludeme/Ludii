package utils.data_structures.transposition_table;

import java.util.ArrayList;
import java.util.List;

import other.move.Move;
import utils.data_structures.ScoredMove;

/**
 * Transposition table for Best-First Search.
 * Copied from AB-Transposition tables but adding a field for the sorted list of Scored Moves.
 * 
 * @author cyprien
 */

public class TranspositionTableUBFM
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
	/** An exact value marked (only used by the heuristic learning code) */
	public final static byte MARKED			= (byte) (0x1 << 3);
	/** An exact value validated (only used by the heuristic learning code) */
	public final static byte VALIDATED			= (byte) (0x1 << 4);
	
	//-------------------------------------------------------------------------
	
	/** Number of bits from hashes to use as primary code */
	private final int numBitsPrimaryCode;
	
	/** Max number of entries for which we've allocated space */
	private final int maxNumEntries;
	
	/** Our table of entries */
	private UBFMTTEntry[] table;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor.
	 * 
	 * NOTE: does not yet allocate memory!
	 * 
	 * @param numBitsPrimaryCode Number of bits from hashes to use as primary code.
	 */
	public TranspositionTableUBFM(final int numBitsPrimaryCode)
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
		table = new UBFMTTEntry[maxNumEntries];
	}
	
	/**
	 * Clears up all memory of our table
	 */
	public void deallocate()
	{
		table = null;
	}
	
	/**
	 * Return true if and only if the table is allocated
	 */
	public boolean isAllocated()
	{
		return (table != null);
	}
	
	
	/**
	 * @param fullHash
	 * @return Stored data for given full hash (full 64bits code), or null if not found
	 */
	public UBFMTTData retrieve(final long fullHash)
	{
		final UBFMTTEntry entry = table[(int) (fullHash >>> (Long.SIZE - numBitsPrimaryCode))];
		if (entry == null)
			return null;
		else
			for (UBFMTTData data : entry.data)
			{
				if (data.fullHash == fullHash)
					return data;
			}
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
		final byte valueType,
		final List<ScoredMove> sortedScoredMoves
	)
	{
		final int idx = (int) (fullHash >>> (Long.SIZE - numBitsPrimaryCode));
		UBFMTTEntry entry = table[idx];
		
		if (entry == null)
		{
			entry = new UBFMTTEntry();
			entry.data.add(new UBFMTTData(bestMove, fullHash, value, depth, valueType, sortedScoredMoves));
			table[idx] = entry;
		}
		else
		{
			UBFMTTData dataToSave =  new UBFMTTData(bestMove, fullHash, value, depth, valueType, sortedScoredMoves);
			
			// We erase a previous entry if it has the same fullHash
			for (int i =0; i<entry.data.size(); i++)
			{
				UBFMTTData data = entry.data.get(i);
				if (data.fullHash == fullHash)
				{
					// is the previous entry properly erased from memory?
					entry.data.set(i, dataToSave);
					return;
				}
			};
			
			// If we arrive to this point it means that we had no previous data about this fullHash
			entry.data.add(dataToSave);
			
		}
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Data we wish to store in TT entries for Alpha-Beta Search
	 * 
	 * @author cyprien
	 */
	public static final class UBFMTTData
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
		
		/** The list of possible moves at this position, with their scores, sorted (can be null if we don't know) */
		public List<ScoredMove> sortedScoredMoves = null;
		
		/**
		 * Constructor
		 * @param bestMove
		 * @param fullHash
		 * @param value
		 * @param depth
		 * @param valueType
		 */
		public UBFMTTData
		(
			final Move bestMove, 
			final long fullHash,
			final float value,
			final int depth,
			final byte valueType,
			final List<ScoredMove> sortedScoredMoves
		)
		{
			this.bestMove = bestMove;
			this.fullHash = fullHash;
			this.value = value;
			this.depth = depth;
			this.valueType = valueType;
			this.sortedScoredMoves = sortedScoredMoves;
		}
		
	}
	
	//-------------------------------------------------------------------------
	
	public int nbEntries()
	{
		int res = 0;
		for (int i=0; i<maxNumEntries; i++)
		{
			if (table[i] != null)
				res += table[i].data.size();
		}
		return res;
	}
	
	public int nbMarkedEntries()
	{
		int res = 0;
		for (int i=0; i<maxNumEntries; i++)
			if (table[i] != null)
				for (UBFMTTData entry : table[i].data)
					if (entry.valueType == MARKED)
						res += 1;
		return res;
	}
	
	public void dispValueStats()
	// (counts entries with double data as 1 entry)
	{
		
		System.out.println("Number of entries:"+Integer.toString(nbEntries()));
		
		int maxDepth = 0;
		for (int i=0; i<maxNumEntries; i++)
		{
			if (table[i]!=null)
			{
				for (UBFMTTData data : table[i].data)
				{
					if (data.depth>maxDepth)
						maxDepth = data.depth;
				}
			}
		}
		
		int[][] counters = new int[maxDepth+1][7];
		for (int i=0; i<maxNumEntries; i++)
		{
			if (table[i]!=null)
			{
				for (UBFMTTData data : table[i].data)
				{
					int index = data.valueType;
					switch (index)
					{
					case 0: break;
					case 1: break;
					case 2: break;
					case 4: index = 3; break;
					case 8: index = 4; break;
					case 16: index = 5; break;
					default: index = 6;
					}
					counters[data.depth][index] += 1;
				}
			}
		}
		
		System.out.println("Search tree analysis:");
		for (int i=0; i<maxDepth; i++)
		{
			System.out.print("At depth "+i+": ");
			for (int k : new int[]{0,1,2,4,5,6})
			{
				System.out.print("value "+k+": "+counters[i][k]+", ");
			}
			System.out.println();
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * An entry in a Transposition Table for Alpha-Beta. Every entry can contain any number 
	 * of slots (BFSTTData).
	 *
	 * @author cyprien
	 */
	public static final class UBFMTTEntry
	{
		/** Data in our entry's first slot */
		public List<UBFMTTData> data = new ArrayList<UBFMTTData>(3);
	}

}
