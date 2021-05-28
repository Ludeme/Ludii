package metadata.ai.features;

import annotations.Opt;
import metadata.ai.AIItem;

/**
 * Describes one or more sets of features (local, geometric patterns) to be used by Biased MCTS agents.
 * 
 * @remarks The basic format of these features is described in: Browne, C., Soemers, D. J. N. J., and Piette, E. (2019). 
 * ``Strategic features for general games.'' In Proceedings of the 2nd Workshop on Knowledge Extraction from Games (KEG)
 * (pp. 70–75).
 *
 * @author Dennis Soemers
 */
public class Features implements AIItem
{
	
	//-------------------------------------------------------------------------
	
	/** Our array of feature sets */
	protected final FeatureSet[] featureSets;
	
	//-------------------------------------------------------------------------
	
	/**
	 * For just a single feature set shared among players.
	 * @param featureSet A single feature set.
	 * 
	 * @example (features (featureSet All { 
	 * (pair "rel:to=<{}>:pat=<refl=true,rots=all,els=[-{}]>" 1.0) }))
	 */
	public Features(@Opt final FeatureSet featureSet)
	{
		if (featureSet == null)
			this.featureSets = new FeatureSet[]{};
		else
			this.featureSets = new FeatureSet[]{featureSet};
	}
	
	/**
	 * For multiple feature sets (one per player).
	 * @param featureSets A sequence of multiple feature sets (typically each
	 * applying to a different player).
	 * 
	 * @example (features { 
	 * 	(featureSet P1 { (pair "rel:to=<{}>:pat=<els=[-{}]>" 1.0) })
	 * 	(featureSet P2 { (pair "rel:to=<{}>:pat=<els=[-{}]>" -1.0) })
	 * })
	 */
	public Features(@Opt final FeatureSet[] featureSets)
	{
		if (featureSets == null)
			this.featureSets = new FeatureSet[]{};
		else
			this.featureSets = featureSets;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Our array of feature sets
	 */
	public FeatureSet[] featureSets()
	{
		return featureSets;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		
		sb.append("(features {\n");
		
		for (final FeatureSet featureSet : featureSets)
		{
			sb.append(featureSet.toString());
		}
			
		sb.append("})\n");

		return sb.toString();
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param threshold
	 * @return A string representation of these features, retaining only those for
	 * which the absolute weights exceed the given threshold.
	 */
	public String toStringThresholded(final float threshold)
	{
		final StringBuilder sb = new StringBuilder();
		
		sb.append("(features {\n");
		
		for (final FeatureSet featureSet : featureSets)
		{
			sb.append(featureSet.toStringThresholded(threshold));
		}
			
		sb.append("})\n");

		return sb.toString();
	}
	
	//-------------------------------------------------------------------------

}
