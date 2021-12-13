package metadata.ai.features.trees.classifiers;

/**
 * Describes a leaf node in a binary classification tree for features; it contains
 * only a predicted probability for "top move".
 * 
 * @author Dennis Soemers
 */
public class BinaryLeaf extends DecisionTreeNode
{
	
	//-------------------------------------------------------------------------
	
	/** Predicted probability of being the/a top move */
	protected final float prob;
	
	//-------------------------------------------------------------------------

	/**
	 * Defines the feature (condition) and the predicted probability of being a top move.
	 * @param prob Predicted probability of being a top move
	 * 
	 * @example (binaryLeaf 0.6)
	 */
	public BinaryLeaf
	(
		final Float prob
	)
	{
		this.prob = prob.floatValue();
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toString()
	{
		return toString(0);
	}
	
	@Override
	public String toString(final int indent)
	{
		return "(binaryLeaf " + prob + ")";
	}
	
	//-------------------------------------------------------------------------
	
}
