package completer;

import java.util.ArrayList;
import java.util.List;

import gnu.trove.list.array.TIntArrayList;

/**
 * Record of a reconstruction completion, which will be a raw *.lud description.
 * @author cambolbro and Eric.Piette
 */
public class Completion
{
	private String raw;  // completed game description
	private double score = 0;  // confidence in enumeration (0..1)
	private double similarityScore = 0;  // (0..1)
	private double commonTrueConceptsScore = 0;  // (0..1)
	private double geographicalScore = 0;  // (0..1)
	private TIntArrayList idsUsed = new TIntArrayList(); // The ruleset ids used to make the completion.
	private List<TIntArrayList> otherIdsUSed = new ArrayList<TIntArrayList>(); // The other possible combinations of rulesets used to obtain the same completion.

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
	
	public void setRaw(final String raw)
	{
		this.raw = raw;
	}
	
	public double score()
	{
		return score;
	}
	
	public void setScore(final double value)
	{
		score = value;
	}
	
	public double similarityScore()
	{
		return similarityScore;
	}
	
	public void setSimilarityScore(final double value)
	{
		similarityScore = value;
	}
	
	public double geographicalScore()
	{
		return geographicalScore;
	}
	
	public void setGeographicalScore(final double value)
	{
		geographicalScore = value;
	}
	
	public double commonExpectedConceptsScore()
	{
		return commonTrueConceptsScore;
	}
	
	public void setCommonTrueConceptsScore(final double value)
	{
		commonTrueConceptsScore = value;
	}
	
	public TIntArrayList idsUsed()
	{
		return idsUsed;
	}
	
	public void setIdsUsed(final TIntArrayList ids)
	{
		idsUsed = new TIntArrayList(ids);
	}
	
	public void addId(final int id)
	{
		idsUsed.add(id);
	}
	
	public List<TIntArrayList> otherIdsUsed()
	{
		return otherIdsUSed;
	}
	
	
	public void addOtherIds(final TIntArrayList ids)
	{
		otherIdsUSed.add(ids);
	}
	
	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		return raw;
	}
}
