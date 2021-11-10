package decision_trees.logits;

import java.util.ArrayList;
import java.util.List;

import features.Feature;
import features.aspatial.AspatialFeature;
import features.aspatial.InterceptFeature;
import features.feature_sets.BaseFeatureSet;
import features.spatial.SpatialFeature;
import function_approx.LinearFunction;
import gnu.trove.list.array.TFloatArrayList;
import main.collections.FVector;

/**
 * Class with methods for learning an exact logit tree.
 * 
 * @author Dennis Soemers
 */
public class ExactLogitTreeLearner
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * Builds an exact logit tree node for given feature set and linear function of weights
	 * @param featureSet
	 * @param linFunc
	 * @return Root node of the generated tree
	 */
	public static LogitTreeNode buildTree(final BaseFeatureSet featureSet, final LinearFunction linFunc)
	{
		final List<AspatialFeature> aspatialFeatures = new ArrayList<AspatialFeature>(featureSet.aspatialFeatures().length);
		for (final AspatialFeature aspatial : featureSet.aspatialFeatures())
		{
			aspatialFeatures.add(aspatial);
		}
		
		final List<SpatialFeature> spatialFeatures = new ArrayList<SpatialFeature>(featureSet.spatialFeatures().length);
		for (final SpatialFeature spatial : featureSet.spatialFeatures())
		{
			spatialFeatures.add(spatial);
		}
		
		final FVector allWeights = linFunc.effectiveParams().allWeights();
		final TFloatArrayList aspatialWeights = new TFloatArrayList(aspatialFeatures.size());
		final TFloatArrayList spatialWeights = new TFloatArrayList(spatialFeatures.size());
		
		for (int i = 0; i < allWeights.dim(); ++i)
		{
			if (i < aspatialFeatures.size())
				aspatialWeights.add(allWeights.get(i));
			else
				spatialWeights.add(allWeights.get(i));
		}
		
		// Remove intercept features and collect accumulated intercept
		float accumInterceptWeight = 0.f;
		for (int i = aspatialFeatures.size() - 1; i >= 0; --i)
		{
			if (aspatialFeatures.get(i) instanceof InterceptFeature)
			{
				accumInterceptWeight += aspatialWeights.removeAt(i);
				aspatialFeatures.remove(i);
			}
		}
		
		return buildNode(aspatialFeatures, aspatialWeights, spatialFeatures, spatialWeights, accumInterceptWeight);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param remainingAspatialFeatures
	 * @param remainingAspatialWeights
	 * @param remainingSpatialFeatures
	 * @param remainingSpatialWeights
	 * @param accumInterceptWeight
	 * @return Newly built node for logit tree, for given data
	 */
	private static LogitTreeNode buildNode
	(
		final List<AspatialFeature> remainingAspatialFeatures,
		final TFloatArrayList remainingAspatialWeights,
		final List<SpatialFeature> remainingSpatialFeatures,
		final TFloatArrayList remainingSpatialWeights,
		final float accumInterceptWeight
	)
	{
		if (remainingAspatialFeatures.isEmpty() && remainingSpatialFeatures.isEmpty())
		{
			// Time to create leaf node: a model with just a single intercept feature
			return new LogitModelNode(new Feature[] {new InterceptFeature()}, new float[] {accumInterceptWeight});
		}
		
		// Find optimal splitting feature. As optimal splitting criterion, we try to
		// get the lowest average (between our two children) sum of absolute weight values
		// in remaining non-intercept features.
//		float lowestScore = Float.POSITIVE_INFINITY;
//		int bestIdx = -1;
//		boolean bestFeatureIsAspatial = true;
//		
//		float sumAllAbsWeights = 0.f;
//		for (int i = 0; i < remainingAspatialWeights.size(); ++i)
//		{
//			
//		}
//		
//		for (int i = 0; i < remainingAspatialFeatures.size(); ++i)
//		{
//			final AspatialFeature aspatial = remainingAspatialFeatures.get(i);
//		}
		
		return null; // TODO
	}
	
	//-------------------------------------------------------------------------

}
