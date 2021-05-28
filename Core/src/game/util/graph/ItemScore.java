package game.util.graph;

/**
 * Helper class for reordering graph elements.
 * @author cambolbro
 */
public class ItemScore implements Comparable<ItemScore>
{
	private final int    id;
	private final double score;
	
	//-------------------------------------------------
	
	/**
	 * @param id    The index.
	 * @param score The score.
	 */
	public ItemScore(final int id, final double score)
	{
		this.id    = id;
		this.score = score;
	}
	
	//-------------------------------------------------

	/**
	 * @return The index.
	 */
	public int id()
	{
		return id;
	}
	
	/**
	 * @return The score.
	 */
	public double score()
	{
		return score;
	}

	//-------------------------------------------------

	@Override
	public int compareTo(final ItemScore other)
	{
		return (score == other.score)
        		? 0
        		: (score < other.score) ? - 1 : 1;
	}

	//-------------------------------------------------

}
