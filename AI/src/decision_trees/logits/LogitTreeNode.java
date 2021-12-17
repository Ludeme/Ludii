package decision_trees.logits;

import features.Feature;
import features.FeatureVector;
import features.feature_sets.BaseFeatureSet;
import metadata.ai.features.trees.logits.LogitNode;

/**
 * Abstract class for a node in a feature-based regression tree
 * that should output logits.
 * 
 * @author Dennis Soemers
 */
public abstract class LogitTreeNode
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param featureVector
	 * @return Predicted logit for given feature vector
	 */
	public abstract float predict(final FeatureVector featureVector);
	
	//-------------------------------------------------------------------------
	
	/**
	 * Convert to tree in metadata format.
	 * @return logit node.
	 */
	public abstract LogitNode toMetadataNode();
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructs a node (and hence, tree) from the given metadata node.
	 * @param metadataNode
	 * @param featureSet
	 * @return Constructed node
	 */
	public static LogitTreeNode fromMetadataNode(final LogitNode metadataNode, final BaseFeatureSet featureSet)
	{
		if (metadataNode instanceof metadata.ai.features.trees.logits.If)
		{
			final metadata.ai.features.trees.logits.If ifNode = (metadata.ai.features.trees.logits.If) metadataNode;
			final LogitTreeNode thenBranch = fromMetadataNode(ifNode.thenNode(), featureSet);
			final LogitTreeNode elseBranch = fromMetadataNode(ifNode.elseNode(), featureSet);
			
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
			
			return new LogitDecisionNode(feature, thenBranch, elseBranch, featureIdx);
		}
		else
		{
			final metadata.ai.features.trees.logits.Leaf leafNode = (metadata.ai.features.trees.logits.Leaf) metadataNode;
			
			final String[] featureStrings = leafNode.featureStrings();
			final float[] weights = leafNode.weights();
			final int[] featureIndices = new int[featureStrings.length];
			
			final Feature[] features = new Feature[featureStrings.length];
			for (int i = 0; i < features.length; ++i)
			{
				final String featureString = featureStrings[i];
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

				features[i] = feature;
				featureIndices[i] = featureIdx;
			}
			
			return new LogitModelNode(features, weights, featureIndices);
		}
	}
	
	//-------------------------------------------------------------------------

}
