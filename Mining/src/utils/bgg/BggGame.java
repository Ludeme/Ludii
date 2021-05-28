package utils.bgg;

import java.util.ArrayList;
import java.util.List;

/**
 * Record of a BGG game entry.
 * @author cambolbro
 */
public class BggGame
{
	private final int      index;
	private final int      bggId;
	private final String   name;
	private final String   date;
	private final String[] details;
	private final List<Rating> ratings = new ArrayList<Rating>();
	
	public BggGame
	(
		final int index, final int bggId, final String name, 
		final String date, final String[] details
	)
	{
		this.index   = index;
		this.bggId   = bggId;
		this.name    = name;
		this.date    = date;
		this.details = details;
	}
	
	public int index()
	{
		return index;
	}
	
	public int bggId()
	{
		return bggId;
	}
	
	public String name()
	{
		return name;
	}
	
	public String date()
	{
		return date;
	}
	
	public String[] details()
	{
		return details;
	}
	
	public List<Rating> ratings()
	{
		return ratings;
	}
	
	public double averageRating()
	{
		// If no ratings yet, use score of -1
		if (ratings.size() == 0)
			return -1;
		
		double averageScore = 0.0;
		for (final Rating rating : ratings)
			averageScore += rating.score();
		return averageScore / ratings.size();
	}

	public void add(final Rating rating)
	{
		ratings.add(rating);
	}

}
