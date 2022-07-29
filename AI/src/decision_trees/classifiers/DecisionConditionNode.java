package decision_trees.classifiers;

import features.Feature;
import features.FeatureVector;
import features.aspatial.AspatialFeature;
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
	
	/** Index of the feature we look at in our feature set (may index into either aspatial or spatial features list) */
	protected int featureIdx = -1;
	
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
	
	/**
	 * Constructor
	 * @param feature
	 * @param trueNode Node we should traverse to if feature is true
	 * @param falseNode Node we should traverse to if feature is false
	 * @param featureIdx Index of the feature
	 */
	public DecisionConditionNode
	(
		final Feature feature, 
		final DecisionTreeNode trueNode, 
		final DecisionTreeNode falseNode,
		final int featureIdx
	)
	{
		this.feature = feature;
		this.trueNode = trueNode;
		this.falseNode = falseNode;
		this.featureIdx = featureIdx;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public float predict(final FeatureVector featureVector)
	{
		if (feature instanceof AspatialFeature)
		{
			if (featureVector.aspatialFeatureValues().get(featureIdx) != 0.f)
				return trueNode.predict(featureVector);
			else
				return falseNode.predict(featureVector);
		}
		else
		{
			if (featureVector.activeSpatialFeatureIndices().contains(featureIdx))
				return trueNode.predict(featureVector);
			else
				return falseNode.predict(featureVector);
		}
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public metadata.ai.features.trees.classifiers.DecisionTreeNode toMetadataNode()
	{
		return new If(feature.toString(), trueNode.toMetadataNode(), falseNode.toMetadataNode());
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return The feature we check in this node
	 */
	public Feature feature()
	{
		return feature;
	}
	
	/**
	 * @return The node we go to when the feature is active
	 */
	public DecisionTreeNode trueNode()
	{
		return trueNode;
	}
	
	/**
	 * @return The node we go to when the feature is not active
	 */
	public DecisionTreeNode falseNode()
	{
		return falseNode;
	}
	
	//-------------------------------------------------------------------------

}
