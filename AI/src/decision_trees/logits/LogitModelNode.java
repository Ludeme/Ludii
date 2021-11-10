package decision_trees.logits;

import features.Feature;

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

}
