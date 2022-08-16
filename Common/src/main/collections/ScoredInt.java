package main.collections;

import java.util.Comparator;

/**
 * A pair of a score (double) and an object (of type int).
 * 
 * The ASCENDING or DESCENDING comparator objects from this class can be used for sorting based on scores.
 * 
 * @author Dennis Soemers
 */
public class ScoredInt
{
	
	private final int object;
	private final double score;
	
	/**
	 * Constructor
	 * 
	 * @param object
	 * @param score
	 */
	public ScoredInt(final int object, final double score)
	{
		this.object = object;
		this.score = score;
	}
	
	/**
	 * @return The object
	 */
	public int object()
	{
		return object;
	}
	
	/**
	 * @return The score
	 */
	public double score()
	{
		return score;
	}
	
	/**
	 * Can be used for sorting in ascending order (with respect to scores)
	 */
	public static Comparator<ScoredInt> ASCENDING = 
			new Comparator<ScoredInt>()
			{

				@Override
				public int compare(final ScoredInt o1, final ScoredInt o2) 
				{
					if (o1.score < o2.score)
						return -1;
					
					if (o1.score > o2.score)
						return 1;
					
					return 0;
				}
		
			};
			
	/**
	 * Can be used for sorting in ascending order (with respect to scores)
	 */
	public static Comparator<ScoredInt> DESCENDING = 
			new Comparator<ScoredInt>()
			{

				@Override
				public int compare(final ScoredInt o1, final ScoredInt o2) 
				{
					if (o1.score < o2.score)
						return 1;

					if (o1.score > o2.score)
						return -1;

					return 0;
				}

			};

}
