package common;

/**
 * Distance estimate between two games.
 * 
 * @author cambolbro
 */
public class Score
{
	// Distance estimate in range 0..1:
	// 0 = identical .. 1 = maximally different
	private final double score;

	// -------------------------------------------------------------------------

	public Score(final double score)
	{
		this.score = score;
	}

	// -------------------------------------------------------------------------

	public double score()
	{
		return score;
	}

	// -------------------------------------------------------------------------

}
