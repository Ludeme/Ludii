package utils.data_structures;

/**
 * 
 * Simple wrapper for score + index, used for sorting indices (usually move indices) based on scores.
 * Almost identical to ScoredMove.
 * 
 * @author cyprien
 *
 */

public class ScoredIndex implements Comparable<ScoredIndex>
{
	
	/** The move */
	public final Integer index;
	/** The move's score */
	public final float score;
	
	/**
	 * Constructor
	 * @param move
	 * @param score
	 */
	public ScoredIndex(final Integer index, final float score)
	{
		this.index = index;
		this.score = score;
	}

	@Override
	public int compareTo(final ScoredIndex other)
	{
		final float delta = other.score - score;
		if (delta < 0.f)
			return -1;
		else if (delta > 0.f)
			return 1;
		else
			return 0;
	}
}