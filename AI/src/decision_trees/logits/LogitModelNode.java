package decision_trees.logits;

import features.Feature;
import metadata.ai.features.trees.logits.Leaf;
import metadata.ai.features.trees.logits.LogitNode;
import metadata.ai.misc.Pair;

/**
 * Leaf node in a feature-based logit tree, with a linear model.
 * 
 * @author Dennis Soemers
 */
public class LogitModelNode extends LogitTreeNode
{
	
	//-------------------------------------------------------------------------
	
	/** Array of remaining features */
	protected final Feature[] features;
	
	/** Array of weights for the remaining features */
	protected final float[] weights;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * @param features
	 * @param weights
	 */
	public LogitModelNode
	(
		final Feature[] features, 
		final float[] weights
	)
	{
		this.features = features;
		this.weights = weights;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public LogitNode toMetadataNode()
	{
		final Pair[] pairs = new Pair[features.length];
		for (int i= 0; i < pairs.length; ++i)
		{
			pairs[i] = new Pair(features[i].toString(), Float.valueOf(weights[i]));
		}
		return new Leaf(pairs);
	}
	
	//-------------------------------------------------------------------------

}
