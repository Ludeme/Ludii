package utils.bgg;

import java.util.ArrayList;
import java.util.List;

public class User
{
	private final String name;
	private final List<Rating> ratings = new ArrayList<Rating>();
	private double match = 0;
	//private BitSet gamesRated = new BitSet();
	
	//-------------------------------------------------------------------------
	
	public User(final String name)
	{
		this.name = name;
	}

	//-------------------------------------------------------------------------

	public String name()
	{
		return name;
	}
	
	public List<Rating> ratings()
	{
		return ratings;
	}
	
	public void add(final Rating rating)
	{
		//gamesRated.set(rating.game().id());
		ratings.add(rating);
	}
	
	public double match()
	{
		return match;
	}
	
	public void setMatch(final double value)
	{
		match = value;
	}
	
}
