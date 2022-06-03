package metadata.ai.features.trees;

import annotations.Name;
import annotations.Opt;
import metadata.ai.AIItem;
import metadata.ai.features.trees.classifiers.DecisionTree;
import metadata.ai.features.trees.logits.LogitTree;

/**
 * Describes one or more sets of features (local, geometric patterns), 
 * represented as decision / regression trees.
 *
 * @author Dennis Soemers
 */
public class FeatureTrees implements AIItem
{
	
	//-------------------------------------------------------------------------
	
	/** Logit trees */
	protected LogitTree[] logitTrees;
	
	/** Decision trees for predicting bottom 25% / IQR / top 25% */
	protected DecisionTree[] decisionTrees;
	
	//-------------------------------------------------------------------------
	
	/**
	 * For a variety of different types of trees, each for one or more roles.
	 * 
	 * @param logitTrees One or more logit trees (each for the All
	 * role or for a specific player).
	 * @param decisionTrees One or more decision trees (each for the All
	 * role or for a specific player).
	 * 
	 * @example (featureTrees logitTrees:{ 
	 * (logitTree P1 (if "rel:to=<{}>:pat=<els=[f{0}]>" then:(leaf { (pair "Intercept" 1.0) }) else:(leaf { (pair "Intercept" -1.0) }))) 
	 * })
	 */
	public FeatureTrees
	(
		@Name @Opt final LogitTree[] logitTrees,
		@Name @Opt final DecisionTree[] decisionTrees
	)
	{
		this.logitTrees = logitTrees;
		this.decisionTrees = decisionTrees;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Array of logit trees.
	 */
	public LogitTree[] logitTrees()
	{
		return logitTrees;
	}
	
	/**
	 * @return Array of decision trees.
	 */
	public DecisionTree[] decisionTrees()
	{
		return decisionTrees;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		
		sb.append("(featureTrees \n");
		
		if (logitTrees != null)
		{
			sb.append("logitTrees:{\n");
			for (final LogitTree tree : logitTrees)
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
