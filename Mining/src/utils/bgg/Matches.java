package utils.bgg;

import java.util.ArrayList;
import java.util.List;

/**
 * Record of matches for user scores.
 * @author cambolbro
 */
public class Matches
{
	private final BggGame game;
	private final List<Double> scores = new ArrayList<Double>();
	private double score = 0;
	private int numberMatches = 0;
	
	//-------------------------------------------------------------------------
	
	public Matches(final BggGame game)
	{
		this.game = game;
	}

	//-------------------------------------------------------------------------

	public BggGame game()
	{
		return game;
	}
	
	public List<Double> scores()
	{
		return scores;
	}
	
	public double score()
	{
		return score;
	}

	public void add(final double value)
	{
		scores.add(Double.valueOf(value));
		score += value;
	}

	public void setScore(final double value)
	{
		score = value;
	}

	public void normalise()
	{
		if (scores.size() == 0)
			score = 0;
		else
			score /= scores.size();
	}

	public int getNumberMatches()
	{
		return numberMatches;
	}

	public void setNumberMatches(final int numberMatches)
	{
		this.numberMatches = numberMatches;
	}
	
}
