package decision_trees.classifiers;

import features.Feature;
import metadata.ai.features.trees.classifiers.If;

/**
 * Decision node in a feature-based logit tree
 * 
 * @author Dennis Soemers
 */
public class DecisionConditionNode extends DecisionTreeNode
{
	
	//-------------------------------------------------------------------------
	
	/** The feature we want to evaluate (our condition) */
	protected final Feature feature;
	
	/** Node we should traverse to if feature is true */
	protected final DecisionTreeNode trueNode;
	
	/** Node we should traverse to if feature is false */
	protected final DecisionTreeNode falseNode;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * @param feature
	 * @param trueNode Node we should traverse to if feature is true
	 * @param falseNode Node we should traverse to if feature is false
	 */
	public DecisionConditionNode
	(
		final Feature feature, 
		final DecisionTreeNode trueNode, 
		final DecisionTreeNode falseNode
	)
	{
		this.feature = feature;
		this.trueNode = trueNode;
		this.falseNode = falseNode;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public metadata.ai.features.trees.classifiers.DecisionTreeNode toMetadataNode()
	{
		return new If(feature.toString(), trueNode.toMetadataNode(), falseNode.toMetadataNode());
	}
	
	//-------------------------------------------------------------------------

}
