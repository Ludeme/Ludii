package metadata.ai.features.trees;

import annotations.Name;
import annotations.Opt;
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
	
	//-------------------------------------------------------------------------
	
	/**
	 * For a variety of different types of trees, each for one or more roles.
	 * 
	 * @param exactLogitTrees One or more exact logit trees (each for the All
	 * role or for a specific player).
	 * 
	 * @example (featureTrees exactLogitTrees:{ 
	 * (logitTree P1 (if "rel:to=<{}>:pat=<els=[f{0}]>" then:(leaf { (pair "Intercept" 1.0) }) else:(leaf { (pair "Intercept" -1.0) }))) 
	 * })
	 */
	public FeatureTrees
	(
		@Name @Opt final LogitTree[] exactLogitTrees
	)
	{
		this.exactLogitTrees = exactLogitTrees;
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
		
		sb.append(")");
		
		return sb.toString();
	}
	
	//-------------------------------------------------------------------------

}
