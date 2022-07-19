package main.collections;

import java.util.Comparator;

/**
 * A pair of a score (double) and an object (of any type E).
 * 
 * The ASCENDING or DESCENDING comparator objects from this class can be used for sorting based on scores.
 * 
 * @author Dennis Soemers
 * @param <E> Type of object for which we have a score
 */
public class ScoredObject<E> 
{
	
	private final E object;
	private final double score;
	
	/**
	 * Constructor
	 * 
	 * @param object
	 * @param score
	 */
	public ScoredObject(final E object, final double score)
	{
		this.object = object;
		this.score = score;
	}
	
	/**
	 * @return The object
	 */
	public E object()
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
	public static Comparator<ScoredObject<?>> ASCENDING = 
			new Comparator<ScoredObject<?>>()
			{

				@Override
				public int compare(final ScoredObject<?> o1, final ScoredObject<?> o2) 
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
	public static Comparator<ScoredObject<?>> DESCENDING = 
			new Comparator<ScoredObject<?>>()
			{

				@Override
				public int compare(final ScoredObject<?> o1, final ScoredObject<?> o2) 
				{
					if (o1.score < o2.score)
						return 1;

					if (o1.score > o2.score)
						return -1;

					return 0;
				}

			};

}
