package other.state.zhash;

import org.apache.commons.rng.core.source64.SplitMix64;

/**
 * Generates sequences for Zobrist hashing
 * For now, just a wrapper around SplitMix64, but easy to change if that proves inadequate
 * 
 * @author mrraow
 *
 */
public class ZobristHashGenerator extends SplitMix64 {

	private static final long RNG_SEED = 3544273448235996400L;	// TODO: consider tuning the seed to minimise collisions

	private int counter;
	
	/**
	 * Base constructor
	 */
	public ZobristHashGenerator() {
		super(Long.valueOf(RNG_SEED));
	}
	
	/**
	 * Creates a generator at the specified position - useful if you want to serialise/deserialise and trade file size for CPU time
	 * @param pos
	 */
	public ZobristHashGenerator(final int pos) {
		super(Long.valueOf(RNG_SEED));
		while (counter < pos) next();
	}
	
	/**
	 * Next 64 bit number in sequence
	 */
	@Override
	public long next() {
		counter++;
		return super.next();
	}

	/**
	 * @return The sequence position.
	 */
	public int getSequencePosition() {
		return counter;
	}
}
