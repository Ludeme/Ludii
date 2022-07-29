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
import main.collections.ListUtils;

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
	 * @param maxDepth
	 * @return Root node of the generated tree
	 */
	public static LogitTreeNode buildTree(final BaseFeatureSet featureSet, final LinearFunction linFunc, final int maxDepth)
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
		
		// Remove all 0-weight features
		for (int i = aspatialFeatures.size() - 1; i >= 0; --i)
		{
			if (aspatialWeights.getQuick(i) == 0.f)
			{
				ListUtils.removeSwap(aspatialWeights, i);
				ListUtils.removeSwap(aspatialFeatures, i);
			}
		}
		for (int i = spatialFeatures.size() - 1; i >= 0; --i)
		{
			if (spatialWeights.getQuick(i) == 0.f)
			{
				ListUtils.removeSwap(spatialWeights, i);
				ListUtils.removeSwap(spatialFeatures, i);
			}
		}
		
		return buildNode(aspatialFeatures, aspatialWeights, spatialFeatures, spatialWeights, accumInterceptWeight, maxDepth);
	}
	
	/**
	 * Builds an exact logit tree node for given feature set and linear function of weights,
	 * using a naive approach that simply splits on the feature with the maximum absolute weight
	 * @param featureSet
	 * @param linFunc
	 * @param maxDepth
	 * @return Root node of the generated tree
	 */
	public static LogitTreeNode buildTreeNaiveMaxAbs(final BaseFeatureSet featureSet, final LinearFunction linFunc, final int maxDepth)
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
		
		// Remove all 0-weight features
		for (int i = aspatialFeatures.size() - 1; i >= 0; --i)
		{
			if (aspatialWeights.getQuick(i) == 0.f)
			{
				ListUtils.removeSwap(aspatialWeights, i);
				ListUtils.removeSwap(aspatialFeatures, i);
			}
		}
		for (int i = spatialFeatures.size() - 1; i >= 0; --i)
		{
			if (spatialWeights.getQuick(i) == 0.f)
			{
				ListUtils.removeSwap(spatialWeights, i);
				ListUtils.removeSwap(spatialFeatures, i);
			}
		}
		
		return buildNodeNaiveMaxAbs(aspatialFeatures, aspatialWeights, spatialFeatures, spatialWeights, accumInterceptWeight, maxDepth);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param remainingAspatialFeatures
	 * @param remainingAspatialWeights
	 * @param remainingSpatialFeatures
	 * @param remainingSpatialWeights
	 * @param accumInterceptWeight
	 * @param allowedDepth
	 * @return Newly built node for logit tree, for given data
	 */
	private static LogitTreeNode buildNode
	(
		final List<AspatialFeature> remainingAspatialFeatures,
		final TFloatArrayList remainingAspatialWeights,
		final List<SpatialFeature> remainingSpatialFeatures,
		final TFloatArrayList remainingSpatialWeights,
		final float accumInterceptWeight,
		final int allowedDepth
	)
	{
		if (remainingAspatialFeatures.isEmpty() && remainingSpatialFeatures.isEmpty())
		{
			// Time to create leaf node: a model with just a single intercept feature
			return new LogitModelNode(new Feature[] {InterceptFeature.instance()}, new float[] {accumInterceptWeight});
		}
		
		if (allowedDepth == 0)
		{
			// Have to create leaf node with remaining features
			final int numModelFeatures = remainingAspatialFeatures.size() + remainingSpatialFeatures.size() + 1;
			final Feature[] featuresArray = new Feature[numModelFeatures];
			final float[] weightsArray = new float[numModelFeatures];
			
			int nextIdx = 0;
			
			// Start with intercept
			featuresArray[nextIdx] = InterceptFeature.instance();
			weightsArray[nextIdx++] = accumInterceptWeight;
			
			// Now aspatial features
			for (int i = 0; i < remainingAspatialFeatures.size(); ++i)
			{
				featuresArray[nextIdx] = remainingAspatialFeatures.get(i);
				weightsArray[nextIdx++] = remainingAspatialWeights.getQuick(i);
			}
			
			// And finally spatial features
			for (int i = 0; i < remainingSpatialFeatures.size(); ++i)
			{
				featuresArray[nextIdx] = remainingSpatialFeatures.get(i);
				weightsArray[nextIdx++] = remainingSpatialWeights.getQuick(i);
			}
			
			return new LogitModelNode(featuresArray, weightsArray);
		}
		
		// Find optimal splitting feature. As optimal splitting criterion, we try to
		// get the lowest average (between our two children) sum of absolute weight values
		// in remaining non-intercept features.
		float lowestScore = Float.POSITIVE_INFINITY;
		int bestIdx = -1;
		boolean bestFeatureIsAspatial = true;
		
		float sumAllAbsWeights = 0.f;
		for (int i = 0; i < remainingAspatialWeights.size(); ++i)
		{
			sumAllAbsWeights += Math.abs(remainingAspatialWeights.getQuick(i));
		}
		for (int i = 0; i < remainingSpatialWeights.size(); ++i)
		{
			sumAllAbsWeights += Math.abs(remainingSpatialWeights.getQuick(i));
		}
		
		for (int i = 0; i < remainingAspatialFeatures.size(); ++i)
		{
			// Since we already filtered out intercept terms, we know that whenever any other
			// aspatial feature is true, all the spatial features must be false (so then we
			// can absorb all their weights at once). If an aspatial feature is false, we
			// do not lose any other weights.
			final float absFeatureWeight = Math.abs(remainingAspatialWeights.getQuick(i));
			
			float falseScore = sumAllAbsWeights - absFeatureWeight;
			float trueScore = sumAllAbsWeights - absFeatureWeight;
			for (int j = 0; j < remainingSpatialWeights.size(); ++j)
			{
				trueScore -= Math.abs(remainingSpatialWeights.getQuick(j));
			}
			
			final float splitScore = (falseScore + trueScore) / 2.f;
			
			if (splitScore < lowestScore)
			{
				lowestScore = splitScore;
				bestIdx = i;
			}
		}
		
		for (int i = 0; i < remainingSpatialFeatures.size(); ++i)
		{
			final SpatialFeature spatial = remainingSpatialFeatures.get(i);
			final float absFeatureWeight = Math.abs(remainingSpatialWeights.getQuick(i));
			
			float falseScore = sumAllAbsWeights - absFeatureWeight;
			float trueScore = sumAllAbsWeights - absFeatureWeight;
			
			// If a spatial feature is true, we lose all the aspatial weights (none of them can be true)
			for (int j = 0; j < remainingAspatialWeights.size(); ++j)
			{
				trueScore -= Math.abs(remainingAspatialWeights.getQuick(j));
			}
			
			for (int j = 0; j < remainingSpatialFeatures.size(); ++j)
			{
				if (i == j)
					continue;
				
				final SpatialFeature otherFeature = remainingSpatialFeatures.get(j);
				
				if (otherFeature.generalises(spatial))
				{
					// The other feature generalises the splitting candidate. This means
					// that in the branch where the splitting candidate is true, this 
					// feature must also be true and we can therefore also absorb its
					// weight.
					final float otherAbsWeight = Math.abs(remainingSpatialWeights.getQuick(j));
					trueScore -= otherAbsWeight;
				}
				
				if (spatial.generalises(otherFeature))
				{
					// The splitting candidate generalises the other feature. This means
					// that in the branch where the splitting candidate is false, the other 
					// feature must also be false and we can therefore also absorb its
					// weight.
					final float otherAbsWeight = Math.abs(remainingSpatialWeights.getQuick(j));
					falseScore -= otherAbsWeight;
				}
			}
			
			final float splitScore = (falseScore + trueScore) / 2.f;
			
			if (splitScore < lowestScore)
			{
				lowestScore = splitScore;
				bestIdx = i;
				bestFeatureIsAspatial = false;
			}
		}
		
		final Feature splittingFeature;
		if (bestFeatureIsAspatial)
			splittingFeature = remainingAspatialFeatures.get(bestIdx);
		else
			splittingFeature = remainingSpatialFeatures.get(bestIdx);
		
		// Create the node for case where splitting feature is true
		final LogitTreeNode trueBranch;
		{
			final List<AspatialFeature> remainingAspatialsWhenTrue;
			final TFloatArrayList remainingAspatialWeightsWhenTrue;
			final List<SpatialFeature> remainingSpatialsWhenTrue;
			final TFloatArrayList remainingSpatialWeightsWhenTrue;
			float accumInterceptWhenTrue = accumInterceptWeight;
			
			if (bestFeatureIsAspatial)
			{
				// Remove the aspatial feature that we split on
				remainingAspatialsWhenTrue = new ArrayList<AspatialFeature>(remainingAspatialFeatures);
				remainingAspatialWeightsWhenTrue = new TFloatArrayList(remainingAspatialWeights);
				ListUtils.removeSwap(remainingAspatialsWhenTrue, bestIdx);
				accumInterceptWhenTrue += remainingAspatialWeightsWhenTrue.getQuick(bestIdx);
				ListUtils.removeSwap(remainingAspatialWeightsWhenTrue, bestIdx);
				
				// Remove all spatial features when an aspatial feature is true
				remainingSpatialsWhenTrue = new ArrayList<SpatialFeature>();
				remainingSpatialWeightsWhenTrue = new TFloatArrayList();
			}
			else
			{
				// Remove all the aspatial features if a spatial feature is true
				remainingAspatialsWhenTrue = new ArrayList<AspatialFeature>();
				remainingAspatialWeightsWhenTrue = new TFloatArrayList();
				
				// Remove all spatial features that are more general than our splitting feature + the splitting feature
				remainingSpatialsWhenTrue = new ArrayList<SpatialFeature>(remainingSpatialFeatures);
				remainingSpatialWeightsWhenTrue = new TFloatArrayList(remainingSpatialWeights);
				
				for (int i = remainingSpatialsWhenTrue.size() - 1; i >= 0; --i)
				{
					if (i == bestIdx)
					{
						ListUtils.removeSwap(remainingSpatialsWhenTrue, i);
						accumInterceptWhenTrue += remainingSpatialWeightsWhenTrue.getQuick(i);
						ListUtils.removeSwap(remainingSpatialWeightsWhenTrue, i);
					}
					else
					{
						final SpatialFeature other = remainingSpatialsWhenTrue.get(i);
						if (other.generalises((SpatialFeature)splittingFeature))
						{
							ListUtils.removeSwap(remainingSpatialsWhenTrue, i);
							accumInterceptWhenTrue += remainingSpatialWeightsWhenTrue.getQuick(i);
							ListUtils.removeSwap(remainingSpatialWeightsWhenTrue, i);
						}
					}
				}
			}
			
			trueBranch = 
					buildNode
					(
						remainingAspatialsWhenTrue, 
						remainingAspatialWeightsWhenTrue, 
						remainingSpatialsWhenTrue, 
						remainingSpatialWeightsWhenTrue, 
						accumInterceptWhenTrue,
						allowedDepth - 1
					);
		}
		
		// Create the node for case where splitting feature is false
		final LogitTreeNode falseBranch;
		{
			final List<AspatialFeature> remainingAspatialsWhenFalse;
			final TFloatArrayList remainingAspatialWeightsWhenFalse;
			final List<SpatialFeature> remainingSpatialsWhenFalse;
			final TFloatArrayList remainingSpatialWeightsWhenFalse;
			float accumInterceptWhenFalse = accumInterceptWeight;

			if (bestFeatureIsAspatial)
			{
				// Remove the aspatial feature that we split on
				remainingAspatialsWhenFalse = new ArrayList<AspatialFeature>(remainingAspatialFeatures);
				remainingAspatialWeightsWhenFalse = new TFloatArrayList(remainingAspatialWeights);
				ListUtils.removeSwap(remainingAspatialsWhenFalse, bestIdx);
				ListUtils.removeSwap(remainingAspatialWeightsWhenFalse, bestIdx);

				// Keep all spatial features when an aspatial feature is false
				remainingSpatialsWhenFalse = new ArrayList<SpatialFeature>(remainingSpatialFeatures);
				remainingSpatialWeightsWhenFalse = new TFloatArrayList(remainingSpatialWeights);
			}
			else
			{
				// Keep all the aspatial features if a spatial feature is false
				remainingAspatialsWhenFalse = new ArrayList<AspatialFeature>(remainingAspatialFeatures);
				remainingAspatialWeightsWhenFalse = new TFloatArrayList(remainingAspatialWeights);

				// Remove all spatial features that are generalised by our splitting feature + the splitting feature
				remainingSpatialsWhenFalse = new ArrayList<SpatialFeature>(remainingSpatialFeatures);
				remainingSpatialWeightsWhenFalse = new TFloatArrayList(remainingSpatialWeights);

				for (int i = remainingSpatialsWhenFalse.size() - 1; i >= 0; --i)
				{
					if (i == bestIdx)
					{
						ListUtils.removeSwap(remainingSpatialsWhenFalse, i);
						ListUtils.removeSwap(remainingSpatialWeightsWhenFalse, i);
					}
					else
					{
						final SpatialFeature other = remainingSpatialsWhenFalse.get(i);
						if (((SpatialFeature)splittingFeature).generalises(other))
						{
							ListUtils.removeSwap(remainingSpatialsWhenFalse, i);
							ListUtils.removeSwap(remainingSpatialWeightsWhenFalse, i);
						}
					}
				}
			}

			falseBranch = 
					buildNode
					(
						remainingAspatialsWhenFalse, 
						remainingAspatialWeightsWhenFalse, 
						remainingSpatialsWhenFalse, 
						remainingSpatialWeightsWhenFalse, 
						accumInterceptWhenFalse,
						allowedDepth - 1
					);
		}
		
		return new LogitDecisionNode(splittingFeature, trueBranch, falseBranch);
	}
	
	/**
	 * Uses naive approach of splitting on features with max absolute weight.
	 * 
	 * @param remainingAspatialFeatures
	 * @param remainingAspatialWeights
	 * @param remainingSpatialFeatures
	 * @param remainingSpatialWeights
	 * @param accumInterceptWeight
	 * @param allowedDepth
	 * @return Newly built node for logit tree, for given data
	 */
	private static LogitTreeNode buildNodeNaiveMaxAbs
	(
		final List<AspatialFeature> remainingAspatialFeatures,
		final TFloatArrayList remainingAspatialWeights,
		final List<SpatialFeature> remainingSpatialFeatures,
		final TFloatArrayList remainingSpatialWeights,
		final float accumInterceptWeight,
		final int allowedDepth
	)
	{
		if (remainingAspatialFeatures.isEmpty() && remainingSpatialFeatures.isEmpty())
		{
			// Time to create leaf node: a model with just a single intercept feature
			return new LogitModelNode(new Feature[] {InterceptFeature.instance()}, new float[] {accumInterceptWeight});
		}
		
		if (allowedDepth == 0)
		{
			// Have to create leaf node with remaining features
			final int numModelFeatures = remainingAspatialFeatures.size() + remainingSpatialFeatures.size() + 1;
			final Feature[] featuresArray = new Feature[numModelFeatures];
			final float[] weightsArray = new float[numModelFeatures];
			
			int nextIdx = 0;
			
			// Start with intercept
			featuresArray[nextIdx] = InterceptFeature.instance();
			weightsArray[nextIdx++] = accumInterceptWeight;
			
			// Now aspatial features
			for (int i = 0; i < remainingAspatialFeatures.size(); ++i)
			{
				featuresArray[nextIdx] = remainingAspatialFeatures.get(i);
				weightsArray[nextIdx++] = remainingAspatialWeights.getQuick(i);
			}
			
			// And finally spatial features
			for (int i = 0; i < remainingSpatialFeatures.size(); ++i)
			{
				featuresArray[nextIdx] = remainingSpatialFeatures.get(i);
				weightsArray[nextIdx++] = remainingSpatialWeights.getQuick(i);
			}
			
			return new LogitModelNode(featuresArray, weightsArray);
		}
		
		// Find optimal splitting feature. As optimal splitting criterion, we try to
		// get the lowest average (between our two children) sum of absolute weight values
		// in remaining non-intercept features.
		float lowestScore = Float.POSITIVE_INFINITY;
		int bestIdx = -1;
		boolean bestFeatureIsAspatial = true;
		
		float sumAllAbsWeights = 0.f;
		for (int i = 0; i < remainingAspatialWeights.size(); ++i)
		{
			sumAllAbsWeights += Math.abs(remainingAspatialWeights.getQuick(i));
		}
		for (int i = 0; i < remainingSpatialWeights.size(); ++i)
		{
			sumAllAbsWeights += Math.abs(remainingSpatialWeights.getQuick(i));
		}
		
		for (int i = 0; i < remainingAspatialFeatures.size(); ++i)
		{
			final float absFeatureWeight = Math.abs(remainingAspatialWeights.getQuick(i));
			
			float falseScore = sumAllAbsWeights - absFeatureWeight;
			float trueScore = sumAllAbsWeights - absFeatureWeight;
			final float splitScore = (falseScore + trueScore) / 2.f;
			
			if (splitScore < lowestScore)
			{
				lowestScore = splitScore;
				bestIdx = i;
			}
		}
		
		for (int i = 0; i < remainingSpatialFeatures.size(); ++i)
		{
			final float absFeatureWeight = Math.abs(remainingSpatialWeights.getQuick(i));
			
			float falseScore = sumAllAbsWeights - absFeatureWeight;
			float trueScore = sumAllAbsWeights - absFeatureWeight;
			
			final float splitScore = (falseScore + trueScore) / 2.f;
			
			if (splitScore < lowestScore)
			{
				lowestScore = splitScore;
				bestIdx = i;
				bestFeatureIsAspatial = false;
			}
		}
		
		final Feature splittingFeature;
		if (bestFeatureIsAspatial)
			splittingFeature = remainingAspatialFeatures.get(bestIdx);
		else
			splittingFeature = remainingSpatialFeatures.get(bestIdx);
		
		// Create the node for case where splitting feature is true
		final LogitTreeNode trueBranch;
		{
			final List<AspatialFeature> remainingAspatialsWhenTrue;
			final TFloatArrayList remainingAspatialWeightsWhenTrue;
			final List<SpatialFeature> remainingSpatialsWhenTrue;
			final TFloatArrayList remainingSpatialWeightsWhenTrue;
			float accumInterceptWhenTrue = accumInterceptWeight;
			
			if (bestFeatureIsAspatial)
			{
				// Remove the aspatial feature that we split on
				remainingAspatialsWhenTrue = new ArrayList<AspatialFeature>(remainingAspatialFeatures);
				remainingAspatialWeightsWhenTrue = new TFloatArrayList(remainingAspatialWeights);
				ListUtils.removeSwap(remainingAspatialsWhenTrue, bestIdx);
				accumInterceptWhenTrue += remainingAspatialWeightsWhenTrue.getQuick(bestIdx);
				ListUtils.removeSwap(remainingAspatialWeightsWhenTrue, bestIdx);
				
				// Remove all spatial features when an aspatial feature is true
				remainingSpatialsWhenTrue = new ArrayList<SpatialFeature>();
				remainingSpatialWeightsWhenTrue = new TFloatArrayList();
			}
			else
			{
				// Remove all the aspatial features if a spatial feature is true
				remainingAspatialsWhenTrue = new ArrayList<AspatialFeature>();
				remainingAspatialWeightsWhenTrue = new TFloatArrayList();
				
				// Remove all spatial features that are more general than our splitting feature + the splitting feature
				remainingSpatialsWhenTrue = new ArrayList<SpatialFeature>(remainingSpatialFeatures);
				remainingSpatialWeightsWhenTrue = new TFloatArrayList(remainingSpatialWeights);
				
				for (int i = remainingSpatialsWhenTrue.size() - 1; i >= 0; --i)
				{
					if (i == bestIdx)
					{
						ListUtils.removeSwap(remainingSpatialsWhenTrue, i);
						accumInterceptWhenTrue += remainingSpatialWeightsWhenTrue.getQuick(i);
						ListUtils.removeSwap(remainingSpatialWeightsWhenTrue, i);
					}
					else
					{
						final SpatialFeature other = remainingSpatialsWhenTrue.get(i);
						if (other.generalises((SpatialFeature)splittingFeature))
						{
							ListUtils.removeSwap(remainingSpatialsWhenTrue, i);
							accumInterceptWhenTrue += remainingSpatialWeightsWhenTrue.getQuick(i);
							ListUtils.removeSwap(remainingSpatialWeightsWhenTrue, i);
						}
					}
				}
			}
			
			trueBranch = 
					buildNodeNaiveMaxAbs
					(
						remainingAspatialsWhenTrue, 
						remainingAspatialWeightsWhenTrue, 
						remainingSpatialsWhenTrue, 
						remainingSpatialWeightsWhenTrue, 
						accumInterceptWhenTrue,
						allowedDepth - 1
					);
		}
		
		// Create the node for case where splitting feature is false
		final LogitTreeNode falseBranch;
		{
			final List<AspatialFeature> remainingAspatialsWhenFalse;
			final TFloatArrayList remainingAspatialWeightsWhenFalse;
			final List<SpatialFeature> remainingSpatialsWhenFalse;
			final TFloatArrayList remainingSpatialWeightsWhenFalse;
			float accumInterceptWhenFalse = accumInterceptWeight;

			if (bestFeatureIsAspatial)
			{
				// Remove the aspatial feature that we split on
				remainingAspatialsWhenFalse = new ArrayList<AspatialFeature>(remainingAspatialFeatures);
				remainingAspatialWeightsWhenFalse = new TFloatArrayList(remainingAspatialWeights);
				ListUtils.removeSwap(remainingAspatialsWhenFalse, bestIdx);
				ListUtils.removeSwap(remainingAspatialWeightsWhenFalse, bestIdx);

				// Keep all spatial features when an aspatial feature is false
				remainingSpatialsWhenFalse = new ArrayList<SpatialFeature>(remainingSpatialFeatures);
				remainingSpatialWeightsWhenFalse = new TFloatArrayList(remainingSpatialWeights);
			}
			else
			{
				// Keep all the aspatial features if a spatial feature is false
				remainingAspatialsWhenFalse = new ArrayList<AspatialFeature>(remainingAspatialFeatures);
				remainingAspatialWeightsWhenFalse = new TFloatArrayList(remainingAspatialWeights);

				// Remove all spatial features that are generalised by our splitting feature + the splitting feature
				remainingSpatialsWhenFalse = new ArrayList<SpatialFeature>(remainingSpatialFeatures);
				remainingSpatialWeightsWhenFalse = new TFloatArrayList(remainingSpatialWeights);

				for (int i = remainingSpatialsWhenFalse.size() - 1; i >= 0; --i)
				{
					if (i == bestIdx)
					{
						ListUtils.removeSwap(remainingSpatialsWhenFalse, i);
						ListUtils.removeSwap(remainingSpatialWeightsWhenFalse, i);
					}
					else
					{
						final SpatialFeature other = remainingSpatialsWhenFalse.get(i);
						if (((SpatialFeature)splittingFeature).generalises(other))
						{
							ListUtils.removeSwap(remainingSpatialsWhenFalse, i);
							ListUtils.removeSwap(remainingSpatialWeightsWhenFalse, i);
						}
					}
				}
			}

			falseBranch = 
					buildNodeNaiveMaxAbs
					(
						remainingAspatialsWhenFalse, 
						remainingAspatialWeightsWhenFalse, 
						remainingSpatialsWhenFalse, 
						remainingSpatialWeightsWhenFalse, 
						accumInterceptWhenFalse,
						allowedDepth - 1
					);
		}
		
		return new LogitDecisionNode(splittingFeature, trueBranch, falseBranch);
	}
	
	//-------------------------------------------------------------------------

}
