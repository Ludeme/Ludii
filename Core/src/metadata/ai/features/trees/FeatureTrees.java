package metadata.ai.features.trees;

import annotations.Name;
import annotations.Opt;
import metadata.ai.features.trees.classifiers.DecisionTree;
import metadata.ai.features.trees.logits.LogitTree;

/**
 * Describes one or more sets of features (local, geometric patterns), 
 * represented as decision / regression trees.
 *
 * @author Dennis Soemers
 */
public class FeatureTrees 
{
	
	//-------------------------------------------------------------------------
	
	/** Exact logit trees */
	protected LogitTree[] exactLogitTrees;
	
	/** Decision trees for predicting bottom 25% / IQR / top 25% */
	protected DecisionTree[] decisionTrees;
	
	//-------------------------------------------------------------------------
	
	/**
	 * For a variety of different types of trees, each for one or more roles.
	 * 
	 * @param exactLogitTrees One or more exact logit trees (each for the All
	 * role or for a specific player).
	 * @param decisionTrees One or more decision trees (each for the All
	 * role or for a specific player).
	 * 
	 * @example (featureTrees exactLogitTrees:{ 
	 * (logitTree P1 (if "rel:to=<{}>:pat=<els=[f{0}]>" then:(leaf { (pair "Intercept" 1.0) }) else:(leaf { (pair "Intercept" -1.0) }))) 
	 * })
	 */
	public FeatureTrees
	(
		@Name @Opt final LogitTree[] exactLogitTrees,
		@Name @Opt final DecisionTree[] decisionTrees
	)
	{
		this.exactLogitTrees = exactLogitTrees;
		this.decisionTrees = decisionTrees;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		
		sb.append("(featureTrees \n");
		
		if (exactLogitTrees != null)
		{
			sb.append("exactLogitTrees:{\n");
			for (final LogitTree tree : exactLogitTrees)
			{
				sb.append(tree.toString() + "\n");
			}
			sb.append("}\n");
		}
		
		if (decisionTrees != null)
		{
			sb.append("decisionTrees:{\n");
			for (final DecisionTree tree : decisionTrees)
			{
				sb.append(tree.toString() + "\n");
			}
			sb.append("}\n");
		}
		
		sb.append(")");
		
		return sb.toString();
	}
	
	//-------------------------------------------------------------------------

}
