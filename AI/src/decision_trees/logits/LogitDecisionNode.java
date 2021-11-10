package decision_trees.logits;

import features.Feature;

/**
 * Decision node in a feature-based logit tree
 * 
 * @author Dennis Soemers
 */
public class LogitDecisionNode extends LogitTreeNode
{
	
	//-------------------------------------------------------------------------
	
	/** The feature we want to evaluate (our condition) */
	protected final Feature feature;
	
	/** Node we should traverse to if feature is true */
	protected final LogitDecisionNode trueNode;
	
	/** Node we should traverse to if feature is false */
	protected final LogitDecisionNode falseNode;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * @param feature
	 * @param trueNode Node we should traverse to if feature is true
	 * @param falseNode Node we should traverse to if feature is false
	 */
	public LogitDecisionNode
	(
		final Feature feature, 
		final LogitDecisionNode trueNode, 
		final LogitDecisionNode falseNode
	)
	{
		this.feature = feature;
		this.trueNode = trueNode;
		this.falseNode = falseNode;
	}
	
	//-------------------------------------------------------------------------

}
