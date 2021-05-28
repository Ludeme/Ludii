package metrics.support;

/**
 * 
 * @author markus
 */
public class EditCost 
{
	private final int HIT_VALUE = 3;
	private final int MISS_VALUE = -3;
	
	public int gapPenalty()
	{		
		return 0;
	}

	public int hit()
	{
		return HIT_VALUE;
	}

	public int miss()
	{
		
		return MISS_VALUE;
	}
}
