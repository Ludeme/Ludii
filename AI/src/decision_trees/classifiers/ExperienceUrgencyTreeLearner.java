package decision_trees.classifiers;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import features.Feature;
import features.FeatureVector;
import features.WeightVector;
import features.feature_sets.BaseFeatureSet;
import function_approx.LinearFunction;
import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.list.array.TIntArrayList;
import main.collections.ArrayUtils;
import main.collections.FVector;
import training.expert_iteration.ExItExperience;
import utils.data_structures.experience_buffers.ExperienceBuffer;

/**
 * Class with methods for learning urgency trees from experience.
 * 
 * @author Dennis Soemers
 */
public class ExperienceUrgencyTreeLearner
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * Builds an urgency tree node for given feature set and experience buffer
	 * @param featureSet
	 * @param linFunc
	 * @param buffer
	 * @param maxDepth
	 * @param minSamplesPerLeaf
	 * @return Root node of the generated tree
	 */
	public static DecisionTreeNode buildTree
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
		final TFloatArrayList allTargetLabels = new TFloatArrayList();
		
		for (final ExItExperience sample : samples)
		{
			if (sample != null && sample.moves().size() > 1)
			{
				final FeatureVector[] featureVectors = sample.generateFeatureVectors(featureSet);
				final float[] logits = new float[featureVectors.length];

				for (int i = 0; i < featureVectors.length; ++i)
				{
					final FeatureVector featureVector = featureVectors[i];
					logits[i] = oracleWeightVector.dot(featureVector);
				}
				
				final float maxLogit = ArrayUtils.max(logits);
				final float minLogit = ArrayUtils.min(logits);
				
				if (maxLogit == minLogit)
					continue;		// Nothing to learn from this, just skip it
				
				for (int i = 0; i < featureVectors.length; ++i)
				{
					final FeatureVector featureVector = featureVectors[i];
					allFeatureVectors.add(featureVector);
				}
				
				// Maximise logits for winning moves and minimise for losing moves
				for (int i = sample.winningMoves().nextSetBit(0); i >= 0; i = sample.winningMoves().nextSetBit(i + 1))
				{
					logits[i] = maxLogit;
				}
				
				for (int i = sample.losingMoves().nextSetBit(0); i >= 0; i = sample.losingMoves().nextSetBit(i + 1))
				{
					logits[i] = minLogit;
				}
				
				final FVector policy = new FVector(logits);
				policy.softmax();
				
				final float maxProb = policy.max();
				
				final float[] targets = new float[logits.length];
				for (int i = 0; i < targets.length; ++i)
				{
					targets[i] = policy.get(i) / maxProb;
				}
				
				for (final float target : targets)
				{
					allTargetLabels.add(target);
				}
			}
		}
		
		return buildNode
				(
					featureSet,
					allFeatureVectors, 
					allTargetLabels, 
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
	 * @param remainingTargetLabels
	 * @param alreadyPickedAspatials
	 * @param alreadyPickedSpatials
	 * @param numAspatialFeatures
	 * @param numSpatialFeatures
	 * @param allowedDepth
	 * @param minSamplesPerLeaf
	 * @return Newly built node for decision tree, for given data
	 */
	private static DecisionTreeNode buildNode
	(
		final BaseFeatureSet featureSet,
		final List<FeatureVector> remainingFeatureVectors,
		final TFloatArrayList remainingTargetLabels,
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
			return new BinaryLeafNode(0.5f);
		}

		if (allowedDepth == 0)
		{
			// Have to create leaf node here
			return new BinaryLeafNode(remainingTargetLabels.sum() / remainingTargetLabels.size());
		}
		
		// Compute baseline prob (mean for the full node that we want to split)
		final double baselineProb = remainingTargetLabels.sum() / remainingTargetLabels.size();
		
		// For every aspatial and every spatial feature, if not already picked, compute mean prob for true and false branches
		final double[] sumProbsIfFalseAspatial = new double[numAspatialFeatures];
		final int[] numFalseAspatial = new int[numAspatialFeatures];
		final double[] sumProbsIfTrueAspatial = new double[numAspatialFeatures];
		final int[] numTrueAspatial = new int[numAspatialFeatures];

		for (int i = 0; i < numAspatialFeatures; ++i)
		{
			if (alreadyPickedAspatials.get(i))
				continue;

			for (int j = 0; j < remainingFeatureVectors.size(); ++j)
			{
				final FeatureVector featureVector = remainingFeatureVectors.get(j);
				final float targetProb = remainingTargetLabels.getQuick(j);

				if (featureVector.aspatialFeatureValues().get(i) != 0.f)
				{
					sumProbsIfTrueAspatial[i] += targetProb;
					++numTrueAspatial[i];
				}
				else
				{
					sumProbsIfFalseAspatial[i] += targetProb;
					++numFalseAspatial[i];
				}
			}
		}

		final double[] sumProbsIfFalseSpatial = new double[numSpatialFeatures];
		final int[] numFalseSpatial = new int[numSpatialFeatures];
		final double[] sumProbsIfTrueSpatial = new double[numSpatialFeatures];
		final int[] numTrueSpatial = new int[numSpatialFeatures];

		for (int i = 0; i < remainingFeatureVectors.size(); ++i)
		{
			final FeatureVector featureVector = remainingFeatureVectors.get(i);
			final float targetProb = remainingTargetLabels.getQuick(i);

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
					sumProbsIfTrueSpatial[j] += targetProb;
					++numTrueSpatial[j];
				}
				else
				{
					sumProbsIfFalseSpatial[j] += targetProb;
					++numFalseSpatial[j];
				}
			}
		}

		final double[] meanProbsIfFalseAspatial = new double[numAspatialFeatures];
		final double[] meanProbsIfTrueAspatial = new double[numAspatialFeatures];
		final double[] meanProbsIfFalseSpatial = new double[numSpatialFeatures];
		final double[] meanProbsIfTrueSpatial = new double[numSpatialFeatures];

		for (int i = 0; i < numAspatialFeatures; ++i)
		{
			if (numFalseAspatial[i] > 0)
				meanProbsIfFalseAspatial[i] = sumProbsIfFalseAspatial[i] / numFalseAspatial[i];

			if (numTrueAspatial[i] > 0)
				meanProbsIfTrueAspatial[i] = sumProbsIfTrueAspatial[i] / numTrueAspatial[i];
		}

		for (int i = 0; i < numSpatialFeatures; ++i)
		{
			if (numFalseSpatial[i] > 0)
				meanProbsIfFalseSpatial[i] = sumProbsIfFalseSpatial[i] / numFalseSpatial[i];

			if (numTrueSpatial[i] > 0)
				meanProbsIfTrueSpatial[i] = sumProbsIfTrueSpatial[i] / numTrueSpatial[i];
		}

		// Find features with maximum urgency
		double maxUrgency = 0.0;
		final TIntArrayList bestIndices = new TIntArrayList();

		for (int i = 0; i < numAspatialFeatures; ++i)
		{
			if (numFalseAspatial[i] < minSamplesPerLeaf || numTrueAspatial[i] < minSamplesPerLeaf)
				continue;
			
			final double falseUrgency = Math.max(meanProbsIfFalseAspatial[i] / baselineProb, baselineProb / meanProbsIfFalseAspatial[i]);
			final double trueUrgency = Math.max(meanProbsIfTrueAspatial[i] / baselineProb, baselineProb / meanProbsIfTrueAspatial[i]);
			final double urgency = Math.max(falseUrgency, trueUrgency);
			
			if (urgency > maxUrgency)
			{
				bestIndices.reset();
				bestIndices.add(i);
				maxUrgency = urgency;
			}
			else if (urgency == maxUrgency)
			{
				bestIndices.add(i);
			}
		}

		for (int i = 0; i < numSpatialFeatures; ++i)
		{
			if (numFalseSpatial[i] < minSamplesPerLeaf || numTrueSpatial[i] < minSamplesPerLeaf)
				continue;
			
			final double falseUrgency = Math.max(meanProbsIfFalseSpatial[i] / baselineProb, baselineProb / meanProbsIfFalseSpatial[i]);
			final double trueUrgency = Math.max(meanProbsIfTrueSpatial[i] / baselineProb, baselineProb / meanProbsIfTrueSpatial[i]);
			final double urgency = Math.max(falseUrgency, trueUrgency);
			
			if (urgency > maxUrgency)
			{
				bestIndices.reset();
				bestIndices.add(i + numAspatialFeatures);
				maxUrgency = urgency;
			}
			else if (urgency == maxUrgency)
			{
				bestIndices.add(i + numAspatialFeatures);
			}
		}

		if (bestIndices.isEmpty() || maxUrgency == 1.0)
		{
			// No point in making any split at all, so just make leaf		TODO could in theory use remaining features to compute a model again
			return new BinaryLeafNode((float) baselineProb);
		}
		
		// Use sample size as tie-breaker
		int bestSampleSize = 0;
		Feature splittingFeature = null;
		boolean bestFeatureIsAspatial = false;
		int bestIdx = -1;
		
		for (int i = 0; i < bestIndices.size(); ++i)
		{
			final int rawIdx = bestIndices.getQuick(i);
			final boolean isAspatial = (rawIdx < numAspatialFeatures);
			final int adjustedIdx = isAspatial ? rawIdx : rawIdx - numAspatialFeatures;
			
			final int sampleSize;
			if (isAspatial)
				sampleSize = Math.min(numFalseAspatial[adjustedIdx], numTrueAspatial[adjustedIdx]);
			else
				sampleSize = Math.min(numFalseSpatial[adjustedIdx], numTrueSpatial[adjustedIdx]);
			
			if (sampleSize > bestSampleSize)
			{
				bestSampleSize = sampleSize;
				splittingFeature = isAspatial ? featureSet.aspatialFeatures()[adjustedIdx] : featureSet.spatialFeatures()[adjustedIdx];
				bestFeatureIsAspatial = isAspatial;
				bestIdx = adjustedIdx;
			}
		}

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
		final TFloatArrayList remainingTargetProbsTrue = new TFloatArrayList();

		final List<FeatureVector> remainingFeatureVectorsFalse = new ArrayList<FeatureVector>();
		final TFloatArrayList remainingTargetProbsFalse = new TFloatArrayList();

		if (bestFeatureIsAspatial)
		{
			for (int i = 0; i < remainingFeatureVectors.size(); ++i)
			{
				if (remainingFeatureVectors.get(i).aspatialFeatureValues().get(bestIdx) != 0.f)
				{
					remainingFeatureVectorsTrue.add(remainingFeatureVectors.get(i));
					remainingTargetProbsTrue.add(remainingTargetLabels.getQuick(i));
				}
				else
				{
					remainingFeatureVectorsFalse.add(remainingFeatureVectors.get(i));
					remainingTargetProbsFalse.add(remainingTargetLabels.getQuick(i));
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
					remainingTargetProbsTrue.add(remainingTargetLabels.getQuick(i));
				}
				else
				{
					remainingFeatureVectorsFalse.add(remainingFeatureVectors.get(i));
					remainingTargetProbsFalse.add(remainingTargetLabels.getQuick(i));
				}
			}
		}

		// Create the node for case where splitting feature is true
		final DecisionTreeNode trueBranch;
		{
			trueBranch = 
					buildNode
					(
						featureSet,
						remainingFeatureVectorsTrue,
						remainingTargetProbsTrue,
						newAlreadyPickedAspatials,
						newAlreadyPickedSpatials,
						numAspatialFeatures,
						numSpatialFeatures,
						allowedDepth - 1,
						minSamplesPerLeaf
					);
		}

		// Create the node for case where splitting feature is false
		final DecisionTreeNode falseBranch;
		{
			falseBranch = 
					buildNode
					(
						featureSet,
						remainingFeatureVectorsFalse,
						remainingTargetProbsFalse,
						newAlreadyPickedAspatials,
						newAlreadyPickedSpatials,
						numAspatialFeatures,
						numSpatialFeatures,
						allowedDepth - 1,
						minSamplesPerLeaf
					);
		}
		
		return new DecisionConditionNode(splittingFeature, trueBranch, falseBranch);
	}
	
	//-------------------------------------------------------------------------

}
