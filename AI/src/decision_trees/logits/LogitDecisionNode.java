package decision_trees.logits;

import features.Feature;
import features.FeatureVector;
import features.aspatial.AspatialFeature;
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
	
	/** Index of the feature we look at in our feature set (may index into either aspatial or spatial features list) */
	protected int featureIdx = -1;
	
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
	
	/**
	 * Constructor
	 * @param feature
	 * @param trueNode Node we should traverse to if feature is true
	 * @param falseNode Node we should traverse to if feature is false
	 * @param featureIdx Index of the feature
	 */
	public LogitDecisionNode
	(
		final Feature feature, 
		final LogitTreeNode trueNode, 
		final LogitTreeNode falseNode,
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
	public LogitNode toMetadataNode()
	{
		return new If(feature.toString(), trueNode.toMetadataNode(), falseNode.toMetadataNode());
	}
	
	//-------------------------------------------------------------------------

}
