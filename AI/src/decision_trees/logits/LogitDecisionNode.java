package decision_trees.logits;

import features.Feature;
import metadata.ai.features.trees.logits.If;
import metadata.ai.features.trees.logits.LogitNode;

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
	protected final LogitTreeNode trueNode;
	
	/** Node we should traverse to if feature is false */
	protected final LogitTreeNode falseNode;
	
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
		final LogitTreeNode trueNode, 
		final LogitTreeNode falseNode
	)
	{
		this.feature = feature;
		this.trueNode = trueNode;
		this.falseNode = falseNode;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public LogitNode toMetadataNode()
	{
		return new If(feature.toString(), trueNode.toMetadataNode(), falseNode.toMetadataNode());
	}
	
	//-------------------------------------------------------------------------

}
