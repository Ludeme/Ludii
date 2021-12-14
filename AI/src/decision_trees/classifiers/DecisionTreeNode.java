package decision_trees.classifiers;

import features.Feature;
import features.FeatureVector;
import features.feature_sets.BaseFeatureSet;

/**
 * Abstract class for a node in a feature-based decision tree
 * that should output class probabilities.
 * 
 * @author Dennis Soemers
 */
public abstract class DecisionTreeNode
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param featureVector
	 * @return Predicted (unnormalised) probability estimate for playing given feature vector
	 */
	public abstract float predict(final FeatureVector featureVector);
	
	//-------------------------------------------------------------------------
	
	/**
	 * Convert to tree in metadata format.
	 * @return Decision tree node.
	 */
	public abstract metadata.ai.features.trees.classifiers.DecisionTreeNode toMetadataNode();
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructs a node (and hence, tree) from the given metadata node.
	 * @param metadataNode
	 * @param featureSet
	 * @return Constructed node
	 */
	public static DecisionTreeNode fromMetadataNode
	(
		final metadata.ai.features.trees.classifiers.DecisionTreeNode metadataNode, 
		final BaseFeatureSet featureSet
	)
	{
		if (metadataNode instanceof metadata.ai.features.trees.classifiers.If)
		{
			final metadata.ai.features.trees.classifiers.If ifNode = (metadata.ai.features.trees.classifiers.If) metadataNode;
			final DecisionTreeNode thenBranch = fromMetadataNode(ifNode.thenNode(), featureSet);
			final DecisionTreeNode elseBranch = fromMetadataNode(ifNode.elseNode(), featureSet);
			
			final String featureString = ifNode.featureString();
			final int featureIdx = featureSet.findFeatureIndexForString(featureString);
			final Feature feature;
			if (featureIdx < featureSet.aspatialFeatures().length)
			{
				if (featureSet.aspatialFeatures()[featureIdx].toString().equals(featureString))
					feature = featureSet.aspatialFeatures()[featureIdx];
				else
					feature = featureSet.spatialFeatures()[featureIdx];
			}
			else
			{
				feature = featureSet.spatialFeatures()[featureIdx];
			}
			
			return new DecisionConditionNode(feature, thenBranch, elseBranch);
		}
		else if (metadataNode instanceof metadata.ai.features.trees.classifiers.BinaryLeaf)
		{
			final metadata.ai.features.trees.classifiers.BinaryLeaf leafNode = (metadata.ai.features.trees.classifiers.BinaryLeaf) metadataNode;
			return new BinaryLeafNode(leafNode.prob());
		}
		else
		{
			final metadata.ai.features.trees.classifiers.Leaf leafNode = (metadata.ai.features.trees.classifiers.Leaf) metadataNode;
			return new DecisionLeafNode(leafNode.bottom25Prob(), leafNode.iqrProb(), leafNode.top25Prob());
		}
	}
	
	//-------------------------------------------------------------------------

}
