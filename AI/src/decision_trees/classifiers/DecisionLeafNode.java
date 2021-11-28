package decision_trees.classifiers;

/**
 * Leaf node in a feature-based decision tree, with probabilities for classes.
 * 
 * @author Dennis Soemers
 */
public class DecisionLeafNode extends DecisionTreeNode
{
	
	//-------------------------------------------------------------------------
	
	/** Predicted probability of being a bottom-25% move */
	protected final float bottom25Prob;
	
	/** Predicted probability of being a move in the Interquartile Range */
	protected final float iqrProb;
	
	/** Predicted probability of being a top-25% move */
	protected final float top25Prob;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * @param bottom25Prob
	 * @param iqrProb
	 * @param top25Prob
	 */
	public DecisionLeafNode
	(
		final float bottom25Prob, 
		final float iqrProb, 
		final float top25Prob
	)
	{
		this.bottom25Prob = bottom25Prob;
		this.iqrProb = iqrProb;
		this.top25Prob = top25Prob;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public metadata.ai.features.trees.classifiers.DecisionTreeNode toMetadataNode()
	{
		return new metadata.ai.features.trees.classifiers.Leaf(Float.valueOf(bottom25Prob), Float.valueOf(iqrProb), Float.valueOf(top25Prob));
	}
	
	//-------------------------------------------------------------------------

}
