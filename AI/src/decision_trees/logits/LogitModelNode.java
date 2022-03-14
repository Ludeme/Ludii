package decision_trees.logits;

import features.Feature;
import features.FeatureVector;
import features.aspatial.AspatialFeature;
import metadata.ai.features.trees.logits.Leaf;
import metadata.ai.features.trees.logits.LogitNode;
import metadata.ai.misc.Pair;

/**
 * Leaf node in a feature-based logit tree, with a linear model.
 * 
 * @author Dennis Soemers
 */
public class LogitModelNode extends LogitTreeNode
{
	
	//-------------------------------------------------------------------------
	
	/** Array of remaining features */
	protected final Feature[] features;
	
	/** Array of weights for the remaining features */
	protected final float[] weights;
	
	/** Array of feature indices */
	protected final int[] featureIndices;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * @param features
	 * @param weights
	 */
	public LogitModelNode
	(
		final Feature[] features, 
		final float[] weights
	)
	{
		this.features = features;
		this.weights = weights;
		featureIndices = null;
	}
	
	/**
	 * Constructor
	 * @param features
	 * @param weights
	 * @param featureIndices
	 */
	public LogitModelNode
	(
		final Feature[] features, 
		final float[] weights,
		final int[] featureIndices
	)
	{
		this.features = features;
		this.weights = weights;
		this.featureIndices = featureIndices;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public float predict(final FeatureVector featureVector)
	{
		float dotProduct = 0.f;
		
		for (int i = 0; i < features.length; ++i)
		{
			final Feature feature = features[i];
			final int featureIdx = featureIndices[i];
			
			if (feature instanceof AspatialFeature)
			{
				dotProduct += featureVector.aspatialFeatureValues().get(featureIdx) * weights[i];
			}
			else
			{
				if (featureVector.activeSpatialFeatureIndices().contains(featureIdx))
					dotProduct += weights[i];
			}
		}
		
		return dotProduct;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public LogitNode toMetadataNode()
	{
		final Pair[] pairs = new Pair[features.length];
		for (int i= 0; i < pairs.length; ++i)
		{
			pairs[i] = new Pair(features[i].toString(), Float.valueOf(weights[i]));
		}
		return new Leaf(pairs);
	}
	
	//-------------------------------------------------------------------------

}
