package decision_trees.classifiers;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Comparator;
import java.util.List;

import features.FeatureVector;
import features.WeightVector;
import features.feature_sets.BaseFeatureSet;
import function_approx.LinearFunction;
import main.collections.ArrayUtils;
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
	 * Builds an exact logit tree node for given feature set and experience buffer
	 * @param featureSet
	 * @param linFunc
	 * @param buffer
	 * @param maxDepth
	 * @return Root node of the generated tree
	 */
	public static DecisionTreeNode buildTree
	(
		final BaseFeatureSet featureSet, 
		final LinearFunction linFunc, 
		final ExperienceBuffer buffer, 
		final int maxDepth
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
					allFeatureVectors.add(featureVector);
					logits[i] = oracleWeightVector.dot(featureVector);
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
				
				final IQRClass[] classes = new IQRClass[sortedIndices.size()];
				for (int i = 0; i < sortedIndices.size(); ++i)
				{
					if (i < numBottom25)
					{
						classes[sortedIndices.get(i).intValue()] = IQRClass.Bottom25;
					}
					else if (i < numBottom25 + numIQR)
					{
						classes[sortedIndices.get(i).intValue()] = IQRClass.IQR;
					}
					else
					{
						classes[sortedIndices.get(i).intValue()] = IQRClass.Top25;
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
					maxDepth
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
		final int allowedDepth
	)
	{
		if (remainingFeatureVectors.isEmpty())
		{
			// This should probably never happen
			System.err.println("Empty list of remaining feature vectors!");
			return new DecisionLeafNode(1.f / 3, 1.f / 3, 1.f / 3);
		}
		
		return null;
		
//		if (allowedDepth == 0)
//		{
//			// Have to create leaf node here		TODO could in theory use remaining features to compute a model again
//			final float meanLogit = remainingTargetLogits.sum() / remainingTargetLogits.size();
//			return new LogitModelNode(new Feature[] {new InterceptFeature()}, new float[] {meanLogit});
//		}
//		
//		// For every aspatial and every spatial feature, if not already picked, compute mean logits for true and false branches
//		final double[] sumLogitsIfFalseAspatial = new double[numAspatialFeatures];
//		final int[] numFalseAspatial = new int[numAspatialFeatures];
//		final double[] sumLogitsIfTrueAspatial = new double[numAspatialFeatures];
//		final int[] numTrueAspatial = new int[numAspatialFeatures];
//		
//		for (int i = 0; i < numAspatialFeatures; ++i)
//		{
//			if (alreadyPickedAspatials.get(i))
//				continue;
//			
//			for (int j = 0; j < remainingFeatureVectors.size(); ++j)
//			{
//				final FeatureVector featureVector = remainingFeatureVectors.get(j);
//				final float targetLogit = remainingTargetLogits.getQuick(j);
//				
//				if (featureVector.aspatialFeatureValues().get(i) != 0.f)
//				{
//					sumLogitsIfTrueAspatial[i] += targetLogit;
//					++numTrueAspatial[i];
//				}
//				else
//				{
//					sumLogitsIfFalseAspatial[i] += targetLogit;
//					++numFalseAspatial[i];
//				}
//			}
//		}
//		
//		final double[] sumLogitsIfFalseSpatial = new double[numSpatialFeatures];
//		final int[] numFalseSpatial = new int[numSpatialFeatures];
//		final double[] sumLogitsIfTrueSpatial = new double[numSpatialFeatures];
//		final int[] numTrueSpatial = new int[numSpatialFeatures];
//		
//		for (int i = 0; i < remainingFeatureVectors.size(); ++i)
//		{
//			final FeatureVector featureVector = remainingFeatureVectors.get(i);
//			final float targetLogit = remainingTargetLogits.getQuick(i);
//			
//			final boolean[] active = new boolean[numSpatialFeatures];
//			final TIntArrayList sparseSpatials = featureVector.activeSpatialFeatureIndices();
//			
//			for (int j = 0; j < sparseSpatials.size(); ++j)
//			{
//				active[sparseSpatials.getQuick(j)] = true;
//			}
//			
//			for (int j = 0; j < active.length; ++j)
//			{
//				if (alreadyPickedSpatials.get(j))
//					continue;
//				
//				if (active[j])
//				{
//					sumLogitsIfTrueSpatial[j] += targetLogit;
//					++numTrueSpatial[j];
//				}
//				else
//				{
//					sumLogitsIfFalseSpatial[j] += targetLogit;
//					++numFalseSpatial[j];
//				}
//			}
//		}
//		
//		final double[] meanLogitsIfFalseAspatial = new double[numAspatialFeatures];
//		final double[] meanLogitsIfTrueAspatial = new double[numAspatialFeatures];
//		final double[] meanLogitsIfFalseSpatial = new double[numSpatialFeatures];
//		final double[] meanLogitsIfTrueSpatial = new double[numSpatialFeatures];
//		
//		for (int i = 0; i < numAspatialFeatures; ++i)
//		{
//			if (numFalseAspatial[i] > 0)
//				meanLogitsIfFalseAspatial[i] = sumLogitsIfFalseAspatial[i] / numFalseAspatial[i];
//			
//			if (numTrueAspatial[i] > 0)
//				meanLogitsIfTrueAspatial[i] = sumLogitsIfTrueAspatial[i] / numTrueAspatial[i];
//		}
//		
//		for (int i = 0; i < numSpatialFeatures; ++i)
//		{
//			if (numFalseSpatial[i] > 0)
//				meanLogitsIfFalseSpatial[i] = sumLogitsIfFalseSpatial[i] / numFalseSpatial[i];
//			
//			if (numTrueSpatial[i] > 0)
//				meanLogitsIfTrueSpatial[i] = sumLogitsIfTrueSpatial[i] / numTrueSpatial[i];
//		}
//		
//		// Find feature that maximally reduces sum of squared errors
//		double minSumSquaredErrors = Double.POSITIVE_INFINITY;
//		int bestIdx = -1;
//		boolean bestFeatureIsAspatial = true;
//		
//		for (int i = 0; i < numAspatialFeatures; ++i)
//		{
//			if (numFalseAspatial[i] == 0 || numTrueAspatial[i] == 0)
//				continue;
//			
//			double sumSquaredErrors = 0.0;
//			for (int j = 0; j < remainingFeatureVectors.size(); ++j)
//			{
//				final FeatureVector featureVector = remainingFeatureVectors.get(j);
//				final float targetLogit = remainingTargetLogits.getQuick(j);
//				final double error;
//				
//				if (featureVector.aspatialFeatureValues().get(i) != 0.f)
//					error = targetLogit - meanLogitsIfTrueAspatial[i];
//				else
//					error = targetLogit - meanLogitsIfFalseAspatial[i];
//				
//				sumSquaredErrors += (error * error);
//			}
//			
//			if (sumSquaredErrors < minSumSquaredErrors)
//			{
//				minSumSquaredErrors = sumSquaredErrors;
//				bestIdx = i;
//			}
//		}
//		
//		for (int i = 0; i < numSpatialFeatures; ++i)
//		{
//			if (numFalseSpatial[i] == 0 || numTrueSpatial[i] == 0)
//				continue;
//			
//			double sumSquaredErrors = 0.0;
//			for (int j = 0; j < remainingFeatureVectors.size(); ++j)
//			{
//				final FeatureVector featureVector = remainingFeatureVectors.get(j);
//				final float targetLogit = remainingTargetLogits.getQuick(j);
//				final double error;
//				
//				if (featureVector.activeSpatialFeatureIndices().contains(i))
//					error = targetLogit - meanLogitsIfTrueSpatial[i];
//				else
//					error = targetLogit - meanLogitsIfFalseSpatial[i];
//				
//				sumSquaredErrors += (error * error);
//			}
//						
//			if (sumSquaredErrors < minSumSquaredErrors)
//			{
//				minSumSquaredErrors = sumSquaredErrors;
//				bestIdx = i;
//				bestFeatureIsAspatial = false;
//			}
//		}
//		
//		if (bestIdx == -1)
//		{
//			// No point in making any split at all, so just make leaf		TODO could in theory use remaining features to compute a model again
//			final float meanLogit = remainingTargetLogits.sum() / remainingTargetLogits.size();
//			return new LogitModelNode(new Feature[] {new InterceptFeature()}, new float[] {meanLogit});
//		}
//		
//		final Feature splittingFeature;
//		if (bestFeatureIsAspatial)
//			splittingFeature = featureSet.aspatialFeatures()[bestIdx];
//		else
//			splittingFeature = featureSet.spatialFeatures()[bestIdx];
//		
//		final BitSet newAlreadyPickedAspatials;
//		final BitSet newAlreadyPickedSpatials;
//		
//		if (bestFeatureIsAspatial)
//		{
//			newAlreadyPickedAspatials = (BitSet) alreadyPickedAspatials.clone();
//			newAlreadyPickedAspatials.set(bestIdx);
//			newAlreadyPickedSpatials = alreadyPickedSpatials;
//		}
//		else
//		{
//			newAlreadyPickedSpatials = (BitSet) alreadyPickedSpatials.clone();
//			newAlreadyPickedSpatials.set(bestIdx);
//			newAlreadyPickedAspatials = alreadyPickedAspatials;
//		}
//		
//		// Split remaining data for the two branches
//		final List<FeatureVector> remainingFeatureVectorsTrue = new ArrayList<FeatureVector>();
//		final TFloatArrayList remainingTargetLogitsTrue = new TFloatArrayList();
//		
//		final List<FeatureVector> remainingFeatureVectorsFalse = new ArrayList<FeatureVector>();
//		final TFloatArrayList remainingTargetLogitsFalse = new TFloatArrayList();
//		
//		if (bestFeatureIsAspatial)
//		{
//			for (int i = 0; i < remainingFeatureVectors.size(); ++i)
//			{
//				if (remainingFeatureVectors.get(i).aspatialFeatureValues().get(bestIdx) != 0.f)
//				{
//					remainingFeatureVectorsTrue.add(remainingFeatureVectors.get(i));
//					remainingTargetLogitsTrue.add(remainingTargetLogits.getQuick(i));
//				}
//				else
//				{
//					remainingFeatureVectorsFalse.add(remainingFeatureVectors.get(i));
//					remainingTargetLogitsFalse.add(remainingTargetLogits.getQuick(i));
//				}
//			}
//		}
//		else
//		{
//			for (int i = 0; i < remainingFeatureVectors.size(); ++i)
//			{
//				if (remainingFeatureVectors.get(i).activeSpatialFeatureIndices().contains(bestIdx))
//				{
//					remainingFeatureVectorsTrue.add(remainingFeatureVectors.get(i));
//					remainingTargetLogitsTrue.add(remainingTargetLogits.getQuick(i));
//				}
//				else
//				{
//					remainingFeatureVectorsFalse.add(remainingFeatureVectors.get(i));
//					remainingTargetLogitsFalse.add(remainingTargetLogits.getQuick(i));
//				}
//			}
//		}
//		
//		// Create the node for case where splitting feature is true
//		final DecisionTreeNode trueBranch;
//		{
//			trueBranch = 
//					buildNode
//					(
//						featureSet,
//						remainingFeatureVectorsTrue,
//						remainingTargetLogitsTrue,
//						newAlreadyPickedAspatials,
//						newAlreadyPickedSpatials,
//						numAspatialFeatures,
//						numSpatialFeatures,
//						allowedDepth - 1
//					);
//		}
//		
//		// Create the node for case where splitting feature is false
//		final DecisionTreeNode falseBranch;
//		{
//			falseBranch = 
//					buildNode
//					(
//						featureSet,
//						remainingFeatureVectorsFalse,
//						remainingTargetLogitsFalse,
//						newAlreadyPickedAspatials,
//						newAlreadyPickedSpatials,
//						numAspatialFeatures,
//						numSpatialFeatures,
//						allowedDepth - 1
//					);
//		}
//		
//		return new LogitDecisionNode(splittingFeature, trueBranch, falseBranch);
	}
	
	//-------------------------------------------------------------------------

}
