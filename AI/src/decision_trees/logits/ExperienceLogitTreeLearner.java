package decision_trees.logits;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import features.Feature;
import features.FeatureVector;
import features.WeightVector;
import features.aspatial.InterceptFeature;
import features.feature_sets.BaseFeatureSet;
import function_approx.LinearFunction;
import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.list.array.TIntArrayList;
import training.expert_iteration.ExItExperience;
import utils.data_structures.experience_buffers.ExperienceBuffer;

/**
 * Class with methods for learning logit trees from experience.
 * 
 * @author Dennis Soemers
 */
public class ExperienceLogitTreeLearner
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * Builds an exact logit tree node for given feature set and experience buffer
	 * @param featureSet
	 * @param linFunc
	 * @param buffer
	 * @param maxDepth
	 * @param minSamplesPerLeaf
	 * @return Root node of the generated tree
	 */
	public static LogitTreeNode buildTree
	(
		final BaseFeatureSet featureSet, 
		final LinearFunction linFunc, 
		final ExperienceBuffer buffer, 
		final int maxDepth,
		final int minSamplesPerLeaf
	)
	{
		final WeightVector oracleWeightVector = linFunc.effectiveParams();
		final ExItExperience[] samples = buffer.allExperience();
		final List<FeatureVector> allFeatureVectors = new ArrayList<FeatureVector>();
		final TFloatArrayList allTargetLogits = new TFloatArrayList();
		
		for (final ExItExperience sample : samples)
		{
			if (sample != null && sample.moves().size() > 1)
			{
				final FeatureVector[] featureVectors = sample.generateFeatureVectors(featureSet);
				
				for (final FeatureVector featureVector : featureVectors)
				{
					allFeatureVectors.add(featureVector);
					allTargetLogits.add(oracleWeightVector.dot(featureVector));
				}
			}
		}
		
		return buildNode
				(
					featureSet,
					allFeatureVectors, 
					allTargetLogits, 
					new BitSet(), new BitSet(), 
					featureSet.getNumAspatialFeatures(), featureSet.getNumSpatialFeatures(),
					maxDepth,
					minSamplesPerLeaf
				);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param featureSet
	 * @param remainingFeatureVectors
	 * @param remainingTargetLogits
	 * @param alreadyPickedAspatials
	 * @param alreadyPickedSpatials
	 * @param numAspatialFeatures
	 * @param numSpatialFeatures
	 * @param allowedDepth
	 * @param minSamplesPerLeaf
	 * @return Newly built node for logit tree, for given data
	 */
	private static LogitTreeNode buildNode
	(
		final BaseFeatureSet featureSet,
		final List<FeatureVector> remainingFeatureVectors,
		final TFloatArrayList remainingTargetLogits,
		final BitSet alreadyPickedAspatials,
		final BitSet alreadyPickedSpatials,
		final int numAspatialFeatures,
		final int numSpatialFeatures,
		final int allowedDepth,
		final int minSamplesPerLeaf
	)
	{
		if (minSamplesPerLeaf <= 0)
			throw new IllegalArgumentException("minSamplesPerLeaf must be greater than 0");
		
		if (remainingFeatureVectors.isEmpty())
		{
			return new LogitModelNode(new Feature[] {InterceptFeature.instance()}, new float[] {0.f});
		}
		
		if (allowedDepth == 0)
		{
			// Have to create leaf node here		TODO could in theory use remaining features to compute a model again
			final float meanLogit = remainingTargetLogits.sum() / remainingTargetLogits.size();
			return new LogitModelNode(new Feature[] {InterceptFeature.instance()}, new float[] {meanLogit});
		}
		
		// For every aspatial and every spatial feature, if not already picked, compute mean logits for true and false branches
		final double[] sumLogitsIfFalseAspatial = new double[numAspatialFeatures];
		final int[] numFalseAspatial = new int[numAspatialFeatures];
		final double[] sumLogitsIfTrueAspatial = new double[numAspatialFeatures];
		final int[] numTrueAspatial = new int[numAspatialFeatures];
		
		for (int i = 0; i < numAspatialFeatures; ++i)
		{
			if (alreadyPickedAspatials.get(i))
				continue;
			
			for (int j = 0; j < remainingFeatureVectors.size(); ++j)
			{
				final FeatureVector featureVector = remainingFeatureVectors.get(j);
				final float targetLogit = remainingTargetLogits.getQuick(j);
				
				if (featureVector.aspatialFeatureValues().get(i) != 0.f)
				{
					sumLogitsIfTrueAspatial[i] += targetLogit;
					++numTrueAspatial[i];
				}
				else
				{
					sumLogitsIfFalseAspatial[i] += targetLogit;
					++numFalseAspatial[i];
				}
			}
		}
		
		final double[] sumLogitsIfFalseSpatial = new double[numSpatialFeatures];
		final int[] numFalseSpatial = new int[numSpatialFeatures];
		final double[] sumLogitsIfTrueSpatial = new double[numSpatialFeatures];
		final int[] numTrueSpatial = new int[numSpatialFeatures];
		
		for (int i = 0; i < remainingFeatureVectors.size(); ++i)
		{
			final FeatureVector featureVector = remainingFeatureVectors.get(i);
			final float targetLogit = remainingTargetLogits.getQuick(i);
			
			final boolean[] active = new boolean[numSpatialFeatures];
			final TIntArrayList sparseSpatials = featureVector.activeSpatialFeatureIndices();
			
			for (int j = 0; j < sparseSpatials.size(); ++j)
			{
				active[sparseSpatials.getQuick(j)] = true;
			}
			
			for (int j = 0; j < active.length; ++j)
			{
				if (alreadyPickedSpatials.get(j))
					continue;
				
				if (active[j])
				{
					sumLogitsIfTrueSpatial[j] += targetLogit;
					++numTrueSpatial[j];
				}
				else
				{
					sumLogitsIfFalseSpatial[j] += targetLogit;
					++numFalseSpatial[j];
				}
			}
		}
		
		final double[] meanLogitsIfFalseAspatial = new double[numAspatialFeatures];
		final double[] meanLogitsIfTrueAspatial = new double[numAspatialFeatures];
		final double[] meanLogitsIfFalseSpatial = new double[numSpatialFeatures];
		final double[] meanLogitsIfTrueSpatial = new double[numSpatialFeatures];
		
		for (int i = 0; i < numAspatialFeatures; ++i)
		{
			if (numFalseAspatial[i] > 0)
				meanLogitsIfFalseAspatial[i] = sumLogitsIfFalseAspatial[i] / numFalseAspatial[i];
			
			if (numTrueAspatial[i] > 0)
				meanLogitsIfTrueAspatial[i] = sumLogitsIfTrueAspatial[i] / numTrueAspatial[i];
		}
		
		for (int i = 0; i < numSpatialFeatures; ++i)
		{
			if (numFalseSpatial[i] > 0)
				meanLogitsIfFalseSpatial[i] = sumLogitsIfFalseSpatial[i] / numFalseSpatial[i];
			
			if (numTrueSpatial[i] > 0)
				meanLogitsIfTrueSpatial[i] = sumLogitsIfTrueSpatial[i] / numTrueSpatial[i];
		}
		
		// Find feature that maximally reduces sum of squared errors
		double minSumSquaredErrors = Double.POSITIVE_INFINITY;
		double maxSumSquaredErrors = Double.NEGATIVE_INFINITY;
		int bestIdx = -1;
		boolean bestFeatureIsAspatial = true;
		
		for (int i = 0; i < numAspatialFeatures; ++i)
		{
			if (numFalseAspatial[i] < minSamplesPerLeaf || numTrueAspatial[i] < minSamplesPerLeaf)
				continue;
			
			double sumSquaredErrors = 0.0;
			for (int j = 0; j < remainingFeatureVectors.size(); ++j)
			{
				final FeatureVector featureVector = remainingFeatureVectors.get(j);
				final float targetLogit = remainingTargetLogits.getQuick(j);
				final double error;
				
				if (featureVector.aspatialFeatureValues().get(i) != 0.f)
					error = targetLogit - meanLogitsIfTrueAspatial[i];
				else
					error = targetLogit - meanLogitsIfFalseAspatial[i];
				
				sumSquaredErrors += (error * error);
			}
			
			if (sumSquaredErrors < minSumSquaredErrors)
			{
				minSumSquaredErrors = sumSquaredErrors;
				bestIdx = i;
			}
			
			if (sumSquaredErrors > maxSumSquaredErrors)
			{
				maxSumSquaredErrors = sumSquaredErrors;
			}
		}
		
		for (int i = 0; i < numSpatialFeatures; ++i)
		{
			if (numFalseSpatial[i] < minSamplesPerLeaf || numTrueSpatial[i] < minSamplesPerLeaf)
				continue;
			
			double sumSquaredErrors = 0.0;
			for (int j = 0; j < remainingFeatureVectors.size(); ++j)
			{
				final FeatureVector featureVector = remainingFeatureVectors.get(j);
				final float targetLogit = remainingTargetLogits.getQuick(j);
				final double error;
				
				if (featureVector.activeSpatialFeatureIndices().contains(i))
					error = targetLogit - meanLogitsIfTrueSpatial[i];
				else
					error = targetLogit - meanLogitsIfFalseSpatial[i];
				
				sumSquaredErrors += (error * error);
			}
						
			if (sumSquaredErrors < minSumSquaredErrors)
			{
				minSumSquaredErrors = sumSquaredErrors;
				bestIdx = i;
				bestFeatureIsAspatial = false;
			}
			
			if (sumSquaredErrors > maxSumSquaredErrors)
			{
				maxSumSquaredErrors = sumSquaredErrors;
			}
		}
		
		if (bestIdx == -1 || minSumSquaredErrors == 0.0 || minSumSquaredErrors == maxSumSquaredErrors)
		{
			// No point in making any split at all, so just make leaf		TODO could in theory use remaining features to compute a model again
			final float meanLogit = remainingTargetLogits.sum() / remainingTargetLogits.size();
			return new LogitModelNode(new Feature[] {InterceptFeature.instance()}, new float[] {meanLogit});
		}
		
		final Feature splittingFeature;
		if (bestFeatureIsAspatial)
			splittingFeature = featureSet.aspatialFeatures()[bestIdx];
		else
			splittingFeature = featureSet.spatialFeatures()[bestIdx];
		
		final BitSet newAlreadyPickedAspatials;
		final BitSet newAlreadyPickedSpatials;
		
		if (bestFeatureIsAspatial)
		{
			newAlreadyPickedAspatials = (BitSet) alreadyPickedAspatials.clone();
			newAlreadyPickedAspatials.set(bestIdx);
			newAlreadyPickedSpatials = alreadyPickedSpatials;
		}
		else
		{
			newAlreadyPickedSpatials = (BitSet) alreadyPickedSpatials.clone();
			newAlreadyPickedSpatials.set(bestIdx);
			newAlreadyPickedAspatials = alreadyPickedAspatials;
		}
		
		// Split remaining data for the two branches
		final List<FeatureVector> remainingFeatureVectorsTrue = new ArrayList<FeatureVector>();
		final TFloatArrayList remainingTargetLogitsTrue = new TFloatArrayList();
		
		final List<FeatureVector> remainingFeatureVectorsFalse = new ArrayList<FeatureVector>();
		final TFloatArrayList remainingTargetLogitsFalse = new TFloatArrayList();
		
		if (bestFeatureIsAspatial)
		{
			for (int i = 0; i < remainingFeatureVectors.size(); ++i)
			{
				if (remainingFeatureVectors.get(i).aspatialFeatureValues().get(bestIdx) != 0.f)
				{
					remainingFeatureVectorsTrue.add(remainingFeatureVectors.get(i));
					remainingTargetLogitsTrue.add(remainingTargetLogits.getQuick(i));
				}
				else
				{
					remainingFeatureVectorsFalse.add(remainingFeatureVectors.get(i));
					remainingTargetLogitsFalse.add(remainingTargetLogits.getQuick(i));
				}
			}
		}
		else
		{
			for (int i = 0; i < remainingFeatureVectors.size(); ++i)
			{
				if (remainingFeatureVectors.get(i).activeSpatialFeatureIndices().contains(bestIdx))
				{
					remainingFeatureVectorsTrue.add(remainingFeatureVectors.get(i));
					remainingTargetLogitsTrue.add(remainingTargetLogits.getQuick(i));
				}
				else
				{
					remainingFeatureVectorsFalse.add(remainingFeatureVectors.get(i));
					remainingTargetLogitsFalse.add(remainingTargetLogits.getQuick(i));
				}
			}
		}
		
		// Create the node for case where splitting feature is true
		final LogitTreeNode trueBranch;
		{
			trueBranch = 
					buildNode
					(
						featureSet,
						remainingFeatureVectorsTrue,
						remainingTargetLogitsTrue,
						newAlreadyPickedAspatials,
						newAlreadyPickedSpatials,
						numAspatialFeatures,
						numSpatialFeatures,
						allowedDepth - 1,
						minSamplesPerLeaf
					);
		}
		
		// Create the node for case where splitting feature is false
		final LogitTreeNode falseBranch;
		{
			falseBranch = 
					buildNode
					(
						featureSet,
						remainingFeatureVectorsFalse,
						remainingTargetLogitsFalse,
						newAlreadyPickedAspatials,
						newAlreadyPickedSpatials,
						numAspatialFeatures,
						numSpatialFeatures,
						allowedDepth - 1,
						minSamplesPerLeaf
					);
		}
		
		return new LogitDecisionNode(splittingFeature, trueBranch, falseBranch);
	}
	
	//-------------------------------------------------------------------------

}
