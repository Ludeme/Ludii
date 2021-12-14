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
 * Class with methods for learning imbalanced binary classification trees from experience,
 * where the "True" branch must always directly end in a leaf node.
 * 
 * @author Dennis Soemers
 */
public class ExperienceImbalancedBinaryClassificationTreeLearner
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

		final double[] meanProbsIfTrueAspatial = new double[numAspatialFeatures];
		final double[] meanProbsIfTrueSpatial = new double[numSpatialFeatures];

		for (int i = 0; i < numAspatialFeatures; ++i)
		{
			if (numTrueAspatial[i] > 0)
				meanProbsIfTrueAspatial[i] = sumProbsIfTrueAspatial[i] / numTrueAspatial[i];
		}

		for (int i = 0; i < numSpatialFeatures; ++i)
		{
			if (numTrueSpatial[i] > 0)
				meanProbsIfTrueSpatial[i] = sumProbsIfTrueSpatial[i] / numTrueSpatial[i];
		}

		// Find feature that maximally reduces squared errors for true branch
		double minTrueBranchSquaredErrors = Double.POSITIVE_INFINITY;
		double maxTrueBranchSquaredErrors = Double.NEGATIVE_INFINITY;
		int bestIdx = -1;
		boolean bestFeatureIsAspatial = true;

		for (int i = 0; i < numAspatialFeatures; ++i)
		{
			if (numFalseAspatial[i] < minSamplesPerLeaf || numTrueAspatial[i] < minSamplesPerLeaf)
				continue;

			double trueBranchSquaredErrors = 0.0;
			for (int j = 0; j < remainingFeatureVectors.size(); ++j)
			{
				final FeatureVector featureVector = remainingFeatureVectors.get(j);
				final float targetProb = remainingTargetLabels.getQuick(j);
				final double error;

				if (featureVector.aspatialFeatureValues().get(i) != 0.f)
					error = targetProb - meanProbsIfTrueAspatial[i];
				else
					error = 0.0;	// Ignore false branch

				trueBranchSquaredErrors += (error * error);
			}

			if (trueBranchSquaredErrors < minTrueBranchSquaredErrors)
			{
				minTrueBranchSquaredErrors = trueBranchSquaredErrors;
				bestIdx = i;
			}
			
			if (trueBranchSquaredErrors > maxTrueBranchSquaredErrors)
			{
				maxTrueBranchSquaredErrors = trueBranchSquaredErrors;
			}
		}

		for (int i = 0; i < numSpatialFeatures; ++i)
		{
			if (numFalseSpatial[i] < minSamplesPerLeaf || numTrueSpatial[i] < minSamplesPerLeaf)
				continue;

			double trueBranchSquaredErrors = 0.0;
			for (int j = 0; j < remainingFeatureVectors.size(); ++j)
			{
				final FeatureVector featureVector = remainingFeatureVectors.get(j);
				final float targetProb = remainingTargetLabels.getQuick(j);
				final double error;

				if (featureVector.activeSpatialFeatureIndices().contains(i))
					error = targetProb - meanProbsIfTrueSpatial[i];
				else
					error = 0.0;		// Ignore false branch

				trueBranchSquaredErrors += (error * error);
			}

			if (trueBranchSquaredErrors < minTrueBranchSquaredErrors)
			{
				minTrueBranchSquaredErrors = trueBranchSquaredErrors;
				bestIdx = i;
				bestFeatureIsAspatial = false;
			}
			
			if (trueBranchSquaredErrors > maxTrueBranchSquaredErrors)
			{
				maxTrueBranchSquaredErrors = trueBranchSquaredErrors;
			}
		}

		if (bestIdx == -1 || minTrueBranchSquaredErrors == maxTrueBranchSquaredErrors)
		{
			// No point in making any split at all, so just make leaf		TODO could in theory use remaining features to compute a model again
			return new BinaryLeafNode(remainingTargetLabels.sum() / remainingTargetLabels.size());
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
						0,		// Force immediately making a leaf
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
