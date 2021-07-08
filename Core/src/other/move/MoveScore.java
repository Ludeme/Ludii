package other.move;

/**
 * Record of a move and its estimated score. 
 * @author cambolbro
 */

public class MoveScore
{
	private final Move  move;
	private final float score;
	
	/**
	 * @param move The move.
	 * @param score The score.
	 */
	public MoveScore(final Move move, final float score)
	{
		this.move  = move;
		this.score = score;
	}
	
	public Move move()
	{
		return move;
	}
	
	public float score()
	{
		return score;
	}
}
