package utils.data_structures;

import other.move.Move;

/**
 * 
 * Simple wrapper for score + move, used for sorting moves based on scores.
 * Copied from the AB-Code, but put in an external class so that it can be used by Transposition Tables.
 * 
 * @author cyprien
 *
 */

public class ScoredMove implements Comparable<ScoredMove>
{
	
	/** The move */
	public final Move move;
	/** The move's score */
	public final float score;
	/**  The number of times this move was explored */
	public int nbVisits;
	
	/**
	 * Constructor
	 * @param move
	 * @param score
	 */
	public ScoredMove(final Move move, final float score, final int nbVisits)
	{
		this.move = move;
		this.score = score;
		this.nbVisits = nbVisits;
	}

	@Override
	public int compareTo(final ScoredMove other)
	{
		//nbVisits is not taken into account for the moment
		final float delta = other.score - score;
		if (delta < 0.f)
			return -1;
		else if (delta > 0.f)
			return 1;
		else
			return 0;
	}
}