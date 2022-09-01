package decision_trees.classifiers;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Comparator;
import java.util.List;

import features.Feature;
import features.FeatureVector;
import features.WeightVector;
import features.feature_sets.BaseFeatureSet;
import function_approx.LinearFunction;
import main.collections.ArrayUtils;
import main.math.MathRoutines;
import training.expert_iteration.ExItExperience;
import utils.data_structures.experience_buffers.ExperienceBuffer;

/**
 * Class with methods for learning decision trees (classifiers) from experience.
 * 
 * @author Dennis Soemers
 */
public class ExperienceIQRTreeLearner
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * Classes we distinguish for IQR tree
	 * 
	 * @author Dennis Soemers
	 */
	private static enum IQRClass
	{
		/** Should remain unused */
		UNDEFINED,
		/** Bottom 25% */
		Bottom25,
		/** Interquartile Range */
		IQR,
		/** Top 25% */
		Top25
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Builds an IQR classification tree node for given feature set and experience buffer
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
		final List<IQRClass> allTargetClasses = new ArrayList<IQRClass>();
		
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
				
				final List<Integer> sortedIndices = 
						ArrayUtils.sortedIndices
						(
							featureVectors.length, 
							new Comparator<Integer>()
							{
	
								@Override
								public int compare(final Integer i1, final Integer i2) 
								{
									final float delta = logits[i1.intValue()] - logits[i2.intValue()];
									
									if (delta < 0.f)
										return -1;
									if (delta > 0.f)
										return 1;
									return 0;
								}
						
							}
						);
				
				final int numBottom25 = (int) Math.min(1, Math.round(0.25 * featureVectors.length));
				final int numTop25 = numBottom25;
				final int numIQR = featureVectors.length - numBottom25 - numTop25;
				
				float lowestTop25Logit = Float.POSITIVE_INFINITY;
				float highestBottom25Logit = Float.NEGATIVE_INFINITY;
				
				final IQRClass[] classes = new IQRClass[sortedIndices.size()];
				for (int i = 0; i < numBottom25; ++i)
				{
					final float logit = logits[sortedIndices.get(i).intValue()];
					classes[sortedIndices.get(i).intValue()] = IQRClass.Bottom25;
					highestBottom25Logit = Math.max(highestBottom25Logit, logit);
				}
				
				for (int i = sortedIndices.size() - 1; i >= numBottom25 + numIQR; --i)
				{
					final float logit = logits[sortedIndices.get(i).intValue()];
					classes[sortedIndices.get(i).intValue()] = IQRClass.Top25;
					lowestTop25Logit = Math.min(lowestTop25Logit, logit);
				}
				
				for (int i = numBottom25; i < numBottom25 + numIQR; ++i)
				{
					final float logit = logits[sortedIndices.get(i).intValue()];
					if (logit == lowestTop25Logit)
						classes[sortedIndices.get(i).intValue()] = IQRClass.Top25;
					else if (logit == highestBottom25Logit)
						classes[sortedIndices.get(i).intValue()] = IQRClass.Bottom25;
					else
						classes[sortedIndices.get(i).intValue()] = IQRClass.IQR;
				}
				
				if (lowestTop25Logit == highestBottom25Logit)
				{
					// Top 25% and Bottom 25% logits overlap, so shrink those two buckets
					// and instead have a greater IQR
					for (int i = 0; i < sortedIndices.size(); ++i)
					{
						final float logit = logits[sortedIndices.get(i).intValue()];
						if (logit == lowestTop25Logit)
							classes[sortedIndices.get(i).intValue()] = IQRClass.IQR;
					}
				}
				
				for (final IQRClass targetClass : classes)
				{
					allTargetClasses.add(targetClass);
				}
			}
		}
		
		return buildNode
				(
					featureSet,
					allFeatureVectors, 
					allTargetClasses, 
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
	 * @param remainingTargetClasses
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
		final List<IQRClass> remainingTargetClasses,
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
			// This should probably never happen
			System.err.println("Empty list of remaining feature vectors!");
			return new DecisionLeafNode(1.f / 3, 1.f / 3, 1.f / 3);
		}
		
		int numBottom25 = 0;
		int numTop25 = 0;

		for (final IQRClass iqrClass : remainingTargetClasses)
		{
			if (iqrClass == IQRClass.Bottom25)
				++numBottom25;
			else if (iqrClass == IQRClass.Top25)
				++numTop25;
		}

		final float probBottom25 = ((float)numBottom25) / remainingTargetClasses.size();
		final float probTop25 = ((float)numTop25) / remainingTargetClasses.size();
		final float probIQR = 1.f - probBottom25 - probTop25;
		
		if (allowedDepth == 0)
		{
			// Have to create leaf node here
			return new DecisionLeafNode(probBottom25, probIQR, probTop25);
		}
		
		double entropyBeforeSplit = 0.0;
		if (probBottom25 > 0.f)
			entropyBeforeSplit -= probBottom25 * MathRoutines.log2(probBottom25);
		if (probTop25 > 0.f)
			entropyBeforeSplit -= probTop25 * MathRoutines.log2(probTop25);
		if (probIQR > 0.f)
			entropyBeforeSplit -= probIQR * MathRoutines.log2(probIQR);
		
		// Find feature with maximum information gain
		double maxInformationGain = Double.NEGATIVE_INFINITY;
		double minInformationGain = Double.POSITIVE_INFINITY;
		int bestIdx = -1;
		boolean bestFeatureIsAspatial = true;
		
		for (int i = 0; i < numAspatialFeatures; ++i)
		{
			if (alreadyPickedAspatials.get(i))
				continue;
			
			int numBottom25IfFalse = 0;
			int numIQRIfFalse = 0;
			int numTop25IfFalse = 0;
			
			int numBottom25IfTrue = 0;
			int numIQRIfTrue = 0;
			int numTop25IfTrue = 0;

			for (int j = 0; j < remainingFeatureVectors.size(); ++j)
			{
				final FeatureVector featureVector = remainingFeatureVectors.get(j);
				final IQRClass iqrClass = remainingTargetClasses.get(j);

				if (featureVector.aspatialFeatureValues().get(i) != 0.f)
				{
					switch (iqrClass)
					{
					case Bottom25:
						++numBottom25IfTrue;
						break;
					case IQR:
						++numIQRIfTrue;
						break;
					case Top25:
						++numTop25IfTrue;
						break;
						//$CASES-OMITTED$
					default:
						System.err.println("Unrecognised IQR class!");
					}
				}
				else
				{
					switch (iqrClass)
					{
					case Bottom25:
						++numBottom25IfFalse;
						break;
					case IQR:
						++numIQRIfFalse;
						break;
					case Top25:
						++numTop25IfFalse;
						break;
						//$CASES-OMITTED$
					default:
						System.err.println("Unrecognised IQR class!");
					}
				}
			}
			
			final int totalNumFalse = numBottom25IfFalse + numIQRIfFalse + numTop25IfFalse;
			final int totalNumTrue = numBottom25IfTrue + numIQRIfTrue + numTop25IfTrue;
			
			if (totalNumFalse < minSamplesPerLeaf || totalNumTrue < minSamplesPerLeaf)
				continue;
			
			final double probBottom25IfFalse = ((double)numBottom25IfFalse) / totalNumFalse;
			final double probIQRIfFalse = ((double)numIQRIfFalse) / totalNumFalse;
			final double probTop25IfFalse = ((double)numTop25IfFalse) / totalNumFalse;
			
			final double probBottom25IfTrue = ((double)numBottom25IfTrue) / totalNumTrue;
			final double probIQRIfTrue = ((double)numIQRIfTrue) / totalNumTrue;
			final double probTop25IfTrue = ((double)numTop25IfTrue) / totalNumTrue;
			
			double entropyFalseBranch = 0.0;
			if (probBottom25IfFalse > 0.f)
				entropyFalseBranch -= probBottom25IfFalse * MathRoutines.log2(probBottom25IfFalse);
			if (probIQRIfFalse > 0.f)
				entropyFalseBranch -= probIQRIfFalse * MathRoutines.log2(probIQRIfFalse);
			if (probTop25IfFalse > 0.f)
				entropyFalseBranch -= probTop25IfFalse * MathRoutines.log2(probTop25IfFalse);
			
			double entropyTrueBranch = 0.0;
			if (probBottom25IfTrue > 0.f)
				entropyTrueBranch -= probBottom25IfTrue * MathRoutines.log2(probBottom25IfTrue);
			if (probIQRIfTrue > 0.f)
				entropyTrueBranch -= probIQRIfTrue * MathRoutines.log2(probIQRIfTrue);
			if (probTop25IfTrue > 0.f)
				entropyTrueBranch -= probTop25IfTrue * MathRoutines.log2(probTop25IfTrue);
			
			final double probFalse = ((double)totalNumFalse) / (totalNumFalse + totalNumTrue);
			final double probTrue = 1.0 - probFalse;
			
			final double informationGain = entropyBeforeSplit - probFalse * entropyFalseBranch - probTrue * entropyTrueBranch;
			
			if (informationGain > maxInformationGain)
			{
				maxInformationGain = informationGain;
				bestIdx = i;
			}
			
			if (informationGain < minInformationGain)
			{
				minInformationGain = informationGain;
			}
		}
		
		for (int i = 0; i < numSpatialFeatures; ++i)
		{
			if (alreadyPickedSpatials.get(i))
				continue;
			
			int numBottom25IfFalse = 0;
			int numIQRIfFalse = 0;
			int numTop25IfFalse = 0;
			
			int numBottom25IfTrue = 0;
			int numIQRIfTrue = 0;
			int numTop25IfTrue = 0;
			
			for (int j = 0; j < remainingFeatureVectors.size(); ++j)
			{
				final FeatureVector featureVector = remainingFeatureVectors.get(j);
				final IQRClass iqrClass = remainingTargetClasses.get(j);
				
				if (featureVector.activeSpatialFeatureIndices().contains(i))
				{
					switch (iqrClass)
					{
					case Bottom25:
						++numBottom25IfTrue;
						break;
					case IQR:
						++numIQRIfTrue;
						break;
					case Top25:
						++numTop25IfTrue;
						break;
						//$CASES-OMITTED$
					default:
						System.err.println("Unrecognised IQR class!");
					}
				}
				else
				{
					switch (iqrClass)
					{
					case Bottom25:
						++numBottom25IfFalse;
						break;
					case IQR:
						++numIQRIfFalse;
						break;
					case Top25:
						++numTop25IfFalse;
						break;
						//$CASES-OMITTED$
					default:
						System.err.println("Unrecognised IQR class!");
					}
				}
			}
			
			final int totalNumFalse = numBottom25IfFalse + numIQRIfFalse + numTop25IfFalse;
			final int totalNumTrue = numBottom25IfTrue + numIQRIfTrue + numTop25IfTrue;
			
			if (totalNumFalse < minSamplesPerLeaf || totalNumTrue < minSamplesPerLeaf)
				continue;
			
			final double probBottom25IfFalse = ((double)numBottom25IfFalse) / totalNumFalse;
			final double probIQRIfFalse = ((double)numIQRIfFalse) / totalNumFalse;
			final double probTop25IfFalse = ((double)numTop25IfFalse) / totalNumFalse;
			
			final double probBottom25IfTrue = ((double)numBottom25IfTrue) / totalNumTrue;
			final double probIQRIfTrue = ((double)numIQRIfTrue) / totalNumTrue;
			final double probTop25IfTrue = ((double)numTop25IfTrue) / totalNumTrue;
			
			double entropyFalseBranch = 0.0;
			if (probBottom25IfFalse > 0.f)
				entropyFalseBranch -= probBottom25IfFalse * MathRoutines.log2(probBottom25IfFalse);
			if (probIQRIfFalse > 0.f)
				entropyFalseBranch -= probIQRIfFalse * MathRoutines.log2(probIQRIfFalse);
			if (probTop25IfFalse > 0.f)
				entropyFalseBranch -= probTop25IfFalse * MathRoutines.log2(probTop25IfFalse);
			
			double entropyTrueBranch = 0.0;
			if (probBottom25IfTrue > 0.f)
				entropyTrueBranch -= probBottom25IfTrue * MathRoutines.log2(probBottom25IfTrue);
			if (probIQRIfTrue > 0.f)
				entropyTrueBranch -= probIQRIfTrue * MathRoutines.log2(probIQRIfTrue);
			if (probTop25IfTrue > 0.f)
				entropyTrueBranch -= probTop25IfTrue * MathRoutines.log2(probTop25IfTrue);
			
			final double probFalse = ((double)totalNumFalse) / (totalNumFalse + totalNumTrue);
			final double probTrue = 1.0 - probFalse;
			
			final double informationGain = entropyBeforeSplit - probFalse * entropyFalseBranch - probTrue * entropyTrueBranch;
						
			if (informationGain > maxInformationGain)
			{
				maxInformationGain = informationGain;
				bestIdx = i;
				bestFeatureIsAspatial = false;
			}
			
			if (informationGain < minInformationGain)
			{
				minInformationGain = informationGain;
			}
		}

		if (bestIdx == -1 || maxInformationGain == 0.0 || minInformationGain == maxInformationGain)
		{
			// No point in making any split at all, so just make leaf
			return new DecisionLeafNode(probBottom25, probIQR, probTop25);
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
		final List<IQRClass> remainingTargetClassesTrue = new ArrayList<IQRClass>();
		
		final List<FeatureVector> remainingFeatureVectorsFalse = new ArrayList<FeatureVector>();
		final List<IQRClass> remainingTargetClassesFalse = new ArrayList<IQRClass>();
		
		if (bestFeatureIsAspatial)
		{
			for (int i = 0; i < remainingFeatureVectors.size(); ++i)
			{
				if (remainingFeatureVectors.get(i).aspatialFeatureValues().get(bestIdx) != 0.f)
				{
					remainingFeatureVectorsTrue.add(remainingFeatureVectors.get(i));
					remainingTargetClassesTrue.add(remainingTargetClasses.get(i));
				}
				else
				{
					remainingFeatureVectorsFalse.add(remainingFeatureVectors.get(i));
					remainingTargetClassesFalse.add(remainingTargetClasses.get(i));
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
					remainingTargetClassesTrue.add(remainingTargetClasses.get(i));
				}
				else
				{
					remainingFeatureVectorsFalse.add(remainingFeatureVectors.get(i));
					remainingTargetClassesFalse.add(remainingTargetClasses.get(i));
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
						remainingTargetClassesTrue,
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
						remainingTargetClassesFalse,
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
