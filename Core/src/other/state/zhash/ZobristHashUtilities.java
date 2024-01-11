package other.state.zhash;

/**
 * Zobrist hashing - attempts to reduce a board state to a 64 bit number
 * 
 * Basic contract:
 * - same position in different parts of the tree should have the same hash
 * - different positions will have different hashes _most_ of the time
 * - fast to calculate via incremental updates
 * 
 * Note 1: that collisions can largely be ignored: http://www.craftychess.com/hyatt/collisions.html
 * 
 * Note 2: actions can be identified as oldPosition^newPosition - useful for RAVE, LGM, etc 
 * 
 * Note 3: the seed is fixed, and we are using a deterministic RNG, so the hashes can also be 
 * used to generate opening libraries and endgame libraries
 *
 * @author mrraow
 *
 */
public final class ZobristHashUtilities 
{
	/** Initial value of a hash (0) */
	public static final long INITIAL_VALUE = 0L;

	/** Potentially useful value for signalling unknown hash */
	public static final long UNKNOWN = -1L;

	/**
	 * @return a new instance of a generator which will return a consistent set of hashes across different runs of the code
	 */
	public static final ZobristHashGenerator getHashGenerator()
	{
		return new ZobristHashGenerator();
	}
	
	/**
	 * @param generator - sequence generator
	 * @return next long in sequence
	 */
	public static final long getNext (final ZobristHashGenerator generator) 
	{
		return generator.next();
	}
	
	/**
	 * @param generator - sequence generator
	 * @param dim - dimension of array
	 * @return 1d array of longs from sequence
	 */
	public static final long[] getSequence(final ZobristHashGenerator generator, final int dim) 
	{
		final long[] results = new long[dim];
		for (int i = 0; i < dim; i++) results[i] = generator.next();
		return results;
	}
	
	/**
	 * @param generator - sequence generator
	 * @param dim1 first dimension of array
	 * @param dim2 second dimension of array 
	 * @return 2d array of longs from sequence
	 */
	public static final long[][] getSequence(final ZobristHashGenerator generator, final int dim1, final int dim2) 
	{
		final long[][] results = new long[dim1][];
		for (int i = 0; i < dim1; i++) results[i] = getSequence(generator, dim2);
		return results;
	}
	
	/**
	 * @param generator - sequence generator
	 * @param dim1 first dimension of array
	 * @param dim2 second dimension of array 
	 * @param dim3 third dimension of array
	 * @return 2d array of longs from sequence
	 */
	public static final long[][][] getSequence(final ZobristHashGenerator generator, final int dim1, final int dim2, final int dim3) 
	{
		final long[][][] results = new long[dim1][][];
		for (int i = 0; i < dim1; i++) results[i] = getSequence(generator, dim2, dim3);
		return results;
	}
}
