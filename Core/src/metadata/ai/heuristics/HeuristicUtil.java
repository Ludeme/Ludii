package metadata.ai.heuristics;

import annotations.Hide;
import metadata.ai.heuristics.terms.HeuristicTerm;

/**
 * Utility functions for heuristic manipulation.
 * 
 * @author Matthew.Stephenson
 */
@Hide
public class HeuristicUtil 
{

	//-------------------------------------------------------------------------
	
	/**
	 * @param heuristic 
	 * @return Normalises all weights on heuristic between -1 and 1.
	 */
	public static Heuristics normaliseHeuristic(final Heuristics heuristic)
	{
		double maxWeight = 0.0;
		for (final HeuristicTerm term : heuristic.heuristicTerms())
			maxWeight = Math.max(maxWeight, term.maxAbsWeight());
		return new Heuristics(multiplyHeuristicTerms(heuristic.heuristicTerms(), 1.0/maxWeight));
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param heuristicTerms 
	 * @param multiplier 
	 * @return Multiplies the weights for an array of heuristicTerms by the specified multiplier.
	 */
	public static HeuristicTerm[] multiplyHeuristicTerms(final HeuristicTerm[] heuristicTerms, final double multiplier)
	{
		final HeuristicTerm[] heuristicTermsMultiplied = new HeuristicTerm[heuristicTerms.length];
		for (int i = 0; i < heuristicTermsMultiplied.length; i++)
		{
			final HeuristicTerm halvedHeuristicTerm = heuristicTerms[i].copy();
			halvedHeuristicTerm.setWeight((float) (heuristicTerms[i].weight()*multiplier));
			heuristicTermsMultiplied[i] = halvedHeuristicTerm;
		}
		return heuristicTermsMultiplied;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param weight 
	 * @return Converts a (normalised) weight to a string by binning it.
	 */
	public static String convertWeightToString(final double weight)
	{
		if (weight < 0.2)
			return "very low importance";
		if (weight < 0.4)
			return "low importance";
		if (weight < 0.6)
			return "moderate importance";
		if (weight < 0.8)
			return "high importance";
		
		return "very high importance";
	}
	
	//-------------------------------------------------------------------------
	
}
