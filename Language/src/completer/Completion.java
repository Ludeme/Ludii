package completer;

/**
 * Record of a reconstruction completion, which will be a raw *.lud description.
 * @author cambolbro and Eric.Piette
 */
public class Completion
{
	private final String raw;  // completed game description
	private double score = 0;  // confidence in enumeration (0..1)
	private int numCompletionPoints = 0;  // number of completions points

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
	
	public int numCompletionPoints()
	{
		return numCompletionPoints;
	}
	
	public void addCompletionPoints()
	{
		numCompletionPoints++;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		return raw;
	}
	
	//-------------------------------------------------------------------------
	
}
