package decision_trees.classifiers;

import features.FeatureVector;

/**
 * Leaf node in a feature-based decision tree, with probabilities for classes.
 * 
 * @author Dennis Soemers
 */
public class BinaryLeafNode extends DecisionTreeNode
{
	
	//-------------------------------------------------------------------------
	
	/** Predicted probability of being a top move */
	protected final float prob;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * @param prob
	 */
	public BinaryLeafNode
	(
		final float prob
	)
	{
		this.prob = prob;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public float predict(final FeatureVector featureVector)
	{
		return prob;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public metadata.ai.features.trees.classifiers.DecisionTreeNode toMetadataNode()
	{
		return new metadata.ai.features.trees.classifiers.BinaryLeaf(Float.valueOf(prob));
	}
	
	//-------------------------------------------------------------------------

}
