package completer;

/**
 * Record of a reconstruction completion, which will be a raw *.lud description.
 * @author cambolbro
 */
public class Completion
{
	private final String raw;  // completed game description
	private double score = 1;  // confidence in enumeration (0..1)

	//-------------------------------------------------------------------------

	public Completion(final String raw)
	{
		this.raw = raw;
	}

	//-------------------------------------------------------------------------

	public String raw()
	{
		return raw;
	}
	
	public double score()
	{
		return score;
	}
	
	public void setScore(final double value)
	{
		score = value;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		return raw;
	}
	
	//-------------------------------------------------------------------------
	
}
