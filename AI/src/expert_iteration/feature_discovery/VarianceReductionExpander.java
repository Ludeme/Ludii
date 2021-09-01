package expert_iteration.feature_discovery;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import expert_iteration.ExItExperience;
import expert_iteration.params.ObjectiveParams;
import features.FeatureVector;
import features.feature_sets.BaseFeatureSet;
import features.spatial.FeatureUtils;
import features.spatial.SpatialFeature;
import features.spatial.instances.FeatureInstance;
import game.Game;
import gnu.trove.impl.Constants;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import main.collections.FVector;
import main.collections.FastArrayList;
import other.move.Move;
import policies.softmax.SoftmaxPolicy;
import utils.experiments.InterruptableExperiment;

/**
 * Expands feature sets by adding features that maximally reduce variance in
 * at least one of the "splits", based on the upper bound of a confidence interval
 * on the split's variance.
 *
 * @author Dennis Soemers
 */
public class VarianceReductionExpander implements FeatureSetExpander
{

	@Override
	public BaseFeatureSet expandFeatureSet
	(
		final List<ExItExperience> batch,
		final BaseFeatureSet featureSet,
		final SoftmaxPolicy policy,
		final Game game,
		final int featureDiscoveryMaxNumFeatureInstances,
		final TDoubleArrayList fActiveRatios,
		final ObjectiveParams objectiveParams,
		final PrintWriter logWriter,
		final InterruptableExperiment experiment
	)
	{
		// For every pair (i, j) of feature instances i and j, we need:
		// 
		//	n(i, j): the number of times that i and j are active at the same time
		//	S(i, j): sum of errors in cases where i and j are both active
		//	SS(i, j): sum of squares of errors in cases where i and j are both active
		//	M(i, j): mean error in cases where i and j are both active
		// 
		//	Inverses of all four of the above (for cases where i and j are NOT both active),
		//	but these do not need to be explicit.
		//
		// We similarly also need n, S, SS, and M values for all data (regardless of whether or
		// not i and j are active).
		//
		// For the set of cases where (i, j) are active, we can compute the sample variance in 
		// errors using:
		//
		// SS(i, j) - 2*M(i, j)*S(i, j) + n(i, j)*M(i, j)*M(i, j)
		// ------------------------------------------------------
		//                        n(i, j) - 1   
		//
		// Similar computations can be used for the cases where i and j are NOT both active, and
		// for the complete dataset.

		// This is our n(i, j) matrix
		final TObjectIntHashMap<CombinableFeatureInstancePair> featurePairActivations = 
				new TObjectIntHashMap<CombinableFeatureInstancePair>(Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, 0);

		// This is our S(i, j) matrix
		final TObjectDoubleHashMap<CombinableFeatureInstancePair> errorSums = 
				new TObjectDoubleHashMap<CombinableFeatureInstancePair>(Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, 0.0);
		
		// This is our SS(i, j) matrix
		final TObjectDoubleHashMap<CombinableFeatureInstancePair> squaredErrorSums = 
				new TObjectDoubleHashMap<CombinableFeatureInstancePair>(Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, 0.0);
		
		// This is our M(i, j) matrix
		final TObjectDoubleHashMap<CombinableFeatureInstancePair> meanErrors = 
				new TObjectDoubleHashMap<CombinableFeatureInstancePair>(Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, 0.0);

		// These are our n, S, SS, and M scalars for the complete dataset
		int numCases = 0;
		double sumErrors = 0.0;
		double sumSquaredErrors = 0.0;
		double meanError = 0.0;

		// Create a Hash Set of features already in Feature Set; we won't
		// have to consider combinations that are already in
		final Set<SpatialFeature> existingFeatures = 
				new HashSet<SpatialFeature>
				(
					(int) Math.ceil(featureSet.getNumSpatialFeatures() / 0.75f), 
					0.75f
				);

		for (final SpatialFeature feature : featureSet.spatialFeatures())
		{
			existingFeatures.add(feature);
		}

		// Set of feature instances that we have already preserved (and hence must continue to preserve)
		final Set<CombinableFeatureInstancePair> preservedInstances = new HashSet<CombinableFeatureInstancePair>();

		// Set of feature instances that we have already chosen to discard once (and hence must continue to discard)
		final Set<CombinableFeatureInstancePair> discardedInstances = new HashSet<CombinableFeatureInstancePair>();

		// For every sample in batch, first compute apprentice policies, errors, and sum of absolute errors
		final FVector[] apprenticePolicies = new FVector[batch.size()];
		final FVector[] errorVectors = new FVector[batch.size()];
		final float[] absErrorSums = new float[batch.size()];

		for (int i = 0; i < batch.size(); ++i)
		{
			final ExItExperience sample = batch.get(i);

			final FeatureVector[] featureVectors = 
					featureSet.computeFeatureVectors
					(
						sample.state().state(), 
						sample.state().lastDecisionMove(), 
						sample.moves(), 
						false
					);

			final FVector apprenticePolicy = 
					policy.computeDistribution(featureVectors, sample.state().state().mover());
			final FVector errors = 
					policy.computeDistributionErrors
					(
						apprenticePolicy,
						sample.expertDistribution()
					);
			
			if (objectiveParams.expDeltaValWeighting)
			{
				// Compute expected values of expert and apprentice policies
				double expValueExpert = 0.0;
				double expValueApprentice = 0.0;
				final FVector expertQs = sample.expertValueEstimates();

				for (int a = 0; a < expertQs.dim(); ++a)
				{
					expValueExpert += expertQs.get(a) * sample.expertDistribution().get(a);
					expValueApprentice += expertQs.get(a) * apprenticePolicy.get(a);
				}

				// Scale the errors
				final double expDeltaValWeight = Math.max(
						objectiveParams.expDeltaValWeightingLowerClip, 
						(expValueExpert - expValueApprentice));
				errors.mult((float)expDeltaValWeight);
			}

			final FVector absErrors = errors.copy();
			absErrors.abs();

			apprenticePolicies[i] = apprenticePolicy;
			errorVectors[i] = errors;
			absErrorSums[i] = absErrors.sum();
		}

		// Create list of indices that we can use to index into batch, sorted in descending order
		// of sums of absolute errors.
		// This means that we prioritise looking at samples in the batch for which we have big policy
		// errors, and hence also focus on them when dealing with a cap in the number of active feature
		// instances we can look at
		final List<Integer> batchIndices = new ArrayList<Integer>(batch.size());
		for (int i = 0; i < batch.size(); ++i)
		{
			batchIndices.add(Integer.valueOf(i));
		}
		Collections.sort(batchIndices, new Comparator<Integer>()
		{

			@Override
			public int compare(final Integer o1, final Integer o2)
			{
				final float deltaAbsErrorSums = absErrorSums[o1.intValue()] - absErrorSums[o2.intValue()];
				if (deltaAbsErrorSums > 0.f)
					return -1;
				else if (deltaAbsErrorSums < 0.f)
					return 1;
				else
					return 0;
			}

		});

		// Loop through all samples in batch
		for (int bi = 0; bi < batchIndices.size(); ++bi)
		{
			final int batchIndex = batchIndices.get(bi).intValue();
			final ExItExperience sample = batch.get(batchIndex);
			final FVector errors = errorVectors[batchIndex];
			final FastArrayList<Move> moves = sample.moves();

			// Every action in the sample is a new "case" (state-action pair)
			for (int a = 0; a < moves.size(); ++a)
			{
				++numCases;

				// keep track of pairs we've already seen in this "case"
				final Set<CombinableFeatureInstancePair> observedCasePairs = 
						new HashSet<CombinableFeatureInstancePair>(256, .75f);

				// list --> set --> list to get rid of duplicates
				final List<FeatureInstance> activeInstances = new ArrayList<FeatureInstance>(new HashSet<FeatureInstance>(
						featureSet.getActiveSpatialFeatureInstances
						(
							sample.state().state(), 
							FeatureUtils.fromPos(sample.state().lastDecisionMove()), 
							FeatureUtils.toPos(sample.state().lastDecisionMove()), 
							FeatureUtils.fromPos(moves.get(a)), 
							FeatureUtils.toPos(moves.get(a)),
							moves.get(a).mover()
						)));

				// Start out by keeping all feature instances that have already been marked as having to be
				// preserved, and discarding those that have already been discarded before
				final List<FeatureInstance> instancesToKeep = new ArrayList<FeatureInstance>();

				for (int i = 0; i < activeInstances.size(); /**/)
				{
					final FeatureInstance instance = activeInstances.get(i);
					final CombinableFeatureInstancePair combinedSelf = new CombinableFeatureInstancePair(game, instance, instance);
					if (preservedInstances.contains(combinedSelf))
					{
						instancesToKeep.add(instance);
						activeInstances.remove(i);
					}
					else if (discardedInstances.contains(combinedSelf))
					{
						activeInstances.remove(i);
					}
					else
					{
						++i;
					}
				}

				// This action is allowed to pick at most this many extra instances
				int numInstancesAllowedThisAction = Math.min(Math.min(Math.max(
						5, 		// TODO make this a param
						featureDiscoveryMaxNumFeatureInstances - preservedInstances.size() / (moves.size() - a)),
						featureDiscoveryMaxNumFeatureInstances - preservedInstances.size()),
						activeInstances.size());

				// Create distribution over active instances using softmax over logits inversely proportional to
				// how commonly the instances' features are active
				final FVector distr = new FVector(activeInstances.size());
				for (int i = 0; i < activeInstances.size(); ++i)
				{
					distr.set(i, (float) (2.0 * (1.0 - fActiveRatios.getQuick(activeInstances.get(i).feature().spatialFeatureSetIndex()))));
				}
				distr.softmax();

				while (numInstancesAllowedThisAction > 0)
				{
					// Sample another instance
					final int sampledIdx = distr.sampleFromDistribution();
					final FeatureInstance keepInstance = activeInstances.get(sampledIdx);
					instancesToKeep.add(keepInstance);
					final CombinableFeatureInstancePair combinedSelf = new CombinableFeatureInstancePair(game, keepInstance, keepInstance);
					preservedInstances.add(combinedSelf);		// Remember to preserve this one forever now
					distr.updateSoftmaxInvalidate(sampledIdx);	// Don't want to pick the same index again
					--numInstancesAllowedThisAction;
				}

				// Mark all the instances that haven't been marked as preserved yet as discarded instead
				for (final FeatureInstance instance : activeInstances)
				{
					final CombinableFeatureInstancePair combinedSelf = new CombinableFeatureInstancePair(game, instance, instance);
					if (!preservedInstances.contains(combinedSelf))
						discardedInstances.add(combinedSelf);
				}

				final int numActiveInstances = instancesToKeep.size();
				final float error = errors.get(a);

				sumErrors += error;
				sumSquaredErrors += error * error;
				meanError += (error - meanError) / numCases;

				for (int i = 0; i < numActiveInstances; ++i)
				{
					final FeatureInstance instanceI = instancesToKeep.get(i);

					// increment entries on ''main diagonals''
					final CombinableFeatureInstancePair combinedSelf = 
							new CombinableFeatureInstancePair(game, instanceI, instanceI);

					if (observedCasePairs.add(combinedSelf))
					{
						featurePairActivations.adjustOrPutValue(combinedSelf, 1, 1);
						errorSums.adjustOrPutValue(combinedSelf, error, error);
						squaredErrorSums.adjustOrPutValue(combinedSelf, error*error, error*error);
						
						final double deltaMean = (error - meanErrors.get(combinedSelf)) / featurePairActivations.get(combinedSelf);
						meanErrors.adjustOrPutValue(combinedSelf, deltaMean, deltaMean);
					}

					for (int j = i + 1; j < numActiveInstances; ++j)
					{
						final FeatureInstance instanceJ = instancesToKeep.get(j);

						// increment off-diagonal entries
						final CombinableFeatureInstancePair combined = 
								new CombinableFeatureInstancePair(game, instanceI, instanceJ);

						if (!existingFeatures.contains(combined.combinedFeature))
						{
							if (observedCasePairs.add(combined))
							{
								featurePairActivations.adjustOrPutValue(combined, 1, 1);
								errorSums.adjustOrPutValue(combined, error, error);
								squaredErrorSums.adjustOrPutValue(combined, error*error, error*error);
						
								final double deltaMean = (error - meanErrors.get(combined)) / featurePairActivations.get(combined);
								meanErrors.adjustOrPutValue(combined, deltaMean, deltaMean);
							}
						}
					}
				}
			}
		}
		
		if (sumErrors == 0.0 || sumSquaredErrors == 0.0)
		{
			// incredibly rare case but appears to be possible sometimes
			// we have nothing to guide our feature growing, so let's 
			// just refuse to add a feature
			return null;
		}
		
		// Construct all possible pairs and scores
		// as we go, keep track of best score and index at which we can find it
		final List<ScoredFeatureInstancePair> scoredPairs = new ArrayList<ScoredFeatureInstancePair>(featurePairActivations.size());
		double bestScore = Double.NEGATIVE_INFINITY;
		int bestPairIdx = -1;

		for (final CombinableFeatureInstancePair pair : featurePairActivations.keySet())
		{
			if (! pair.a.equals(pair.b))	// Only interested in combinations of different instances
			{
				final int nij = featurePairActivations.get(pair);
				if (nij == numCases)
				{
					// No variance reduction, so we should just skip this one
					continue;
				}
				
				if (nij < 2)
				{
					// Not enough cases to estimate sample variance in errors, so skip
					continue;
				}

				final int ni = featurePairActivations.get(new CombinableFeatureInstancePair(game, pair.a, pair.a));
				final int nj = featurePairActivations.get(new CombinableFeatureInstancePair(game, pair.b, pair.b));

				if (ni == numCases || nj == numCases)
				{
					// Perfect correlation with one of the constituents, we'll skip this
					continue;
				}
				
				// Compute sample variance for all cases
				final double varComplete = (sumSquaredErrors - 2*meanError*sumErrors + numCases*meanError*meanError) / (numCases - 1);

				// Compute sample variance for the "split" that includes the feature pair
				final double Sij = errorSums.get(pair);
				final double SSij = squaredErrorSums.get(pair);
				final double Mij = meanErrors.get(pair);
				final double varWithPair = (SSij - 2*Mij*Sij + nij*Mij*Mij) / (nij - 1);
				
				// Compute sample variance for the "split" that excludes the feature pair
				final int invNij = numCases - nij;
				final double invSij = sumErrors - Sij;
				final double invSSij = sumSquaredErrors - SSij;
				final double invMij = (meanError*numCases - Mij*nij) / invNij;
				final double varWithoutPair = (invSSij - 2*invMij*invSij + invNij*invMij*invMij) / (nij - 1);

				final double varReduction = varComplete - (varWithPair + varWithoutPair);
				
				final double featureCorrI = 
						(
							(nij * (numCases - ni)) 
							/ 
							(
								Math.sqrt(nij * (numCases - nij)) * 
								Math.sqrt(ni * (numCases - ni))
							)
						);

				final double featureCorrJ = 
						(
							(nij * (numCases - nj)) 
							/ 
							(
								Math.sqrt(nij * (numCases - nij)) * 
								Math.sqrt(nj * (numCases - nj))
							)
						);

				final double worstFeatureCorr = 
						Math.max
						(
							Math.abs(featureCorrI), 
							Math.abs(featureCorrJ)
						);
				
				if (worstFeatureCorr == 1.0)
					continue;

				if (Double.isNaN(varReduction))
					continue;
				
				// continue if worst feature corr == 1.0
				
				final double score = varReduction/* * (1.0 - worstFeatureCorr)*/;

				scoredPairs.add(new ScoredFeatureInstancePair(pair, score));
				if (varReduction > bestScore)
				{
					bestScore = varReduction;
					bestPairIdx = scoredPairs.size() - 1;
				}
			}
		}

		// keep trying to generate an expanded (by one) feature set, until
		// we succeed (almost always this should be on the very first iteration)
		while (scoredPairs.size() > 0)
		{
			// extract pair of feature instances we want to try combining
			final ScoredFeatureInstancePair bestPair = scoredPairs.remove(bestPairIdx);

			final BaseFeatureSet newFeatureSet = 
					featureSet.createExpandedFeatureSet(game, bestPair.pair.combinedFeature);

			if (newFeatureSet != null)
			{
				final int actsI = 
						featurePairActivations.get
						(
							new CombinableFeatureInstancePair(game, bestPair.pair.a, bestPair.pair.a)
						);

				final int actsJ = 
						featurePairActivations.get
						(
							new CombinableFeatureInstancePair(game, bestPair.pair.b, bestPair.pair.b)
						);

				final int pairActs = 
						featurePairActivations.get
						(
							new CombinableFeatureInstancePair(game, bestPair.pair.a, bestPair.pair.b)
						);

				final double pairErrorSum = 
						errorSums.get
						(
							new CombinableFeatureInstancePair(game, bestPair.pair.a, bestPair.pair.b)
						);

				final double errorCorr = 
						(
							(numCases * pairErrorSum - pairActs * sumErrors) 
							/ 
							(
								Math.sqrt(numCases * pairActs - pairActs * pairActs) * 
								Math.sqrt(numCases * sumSquaredErrors - sumErrors * sumErrors)
							)
						);

				final double featureCorrI = 
						(
							(numCases * pairActs - pairActs * actsI) 
							/ 
							(
								Math.sqrt(numCases * pairActs - pairActs * pairActs) * 
								Math.sqrt(numCases * actsI - actsI * actsI)
							)
						);

				final double featureCorrJ = 
						(
							(numCases * pairActs - pairActs * actsJ) 
							/ 
							(
								Math.sqrt(numCases * pairActs - pairActs * pairActs) * 
								Math.sqrt(numCases * actsJ - actsJ * actsJ)
							)
						);
				
				// Compute sample variance for all cases
				final double varComplete = (sumSquaredErrors - 2*meanError*sumErrors + numCases*meanError*meanError) / (numCases - 1);

				// Compute sample variance for the "split" that includes the feature pair
				final double Sij = errorSums.get(bestPair.pair);
				final double SSij = squaredErrorSums.get(bestPair.pair);
				final double Mij = meanErrors.get(bestPair.pair);
				final double varWithPair = (SSij - 2*Mij*Sij + pairActs*Mij*Mij) / (pairActs - 1);
				
				// Compute sample variance for the "split" that excludes the feature pair
				final int invNij = numCases - pairActs;
				final double invSij = sumErrors - Sij;
				final double invSSij = sumSquaredErrors - SSij;
				final double invMij = (meanError*numCases - Mij*pairActs) / invNij;
				final double varWithoutPair = (invSSij - 2*invMij*invSij + invNij*invMij*invMij) / (pairActs - 1);
				
				final double varReduction = varComplete - (varWithPair + varWithoutPair);
				
				experiment.logLine(logWriter, "New feature added!");
				experiment.logLine(logWriter, "new feature = " + newFeatureSet.spatialFeatures()[newFeatureSet.getNumSpatialFeatures() - 1]);
				experiment.logLine(logWriter, "active feature A = " + bestPair.pair.a.feature());
				experiment.logLine(logWriter, "rot A = " + bestPair.pair.a.rotation());
				experiment.logLine(logWriter, "ref A = " + bestPair.pair.a.reflection());
				experiment.logLine(logWriter, "anchor A = " + bestPair.pair.a.anchorSite());
				experiment.logLine(logWriter, "active feature B = " + bestPair.pair.b.feature());
				experiment.logLine(logWriter, "rot B = " + bestPair.pair.b.rotation());
				experiment.logLine(logWriter, "ref B = " + bestPair.pair.b.reflection());
				experiment.logLine(logWriter, "anchor B = " + bestPair.pair.b.anchorSite());
				experiment.logLine(logWriter, "score = " + bestPair.score);
				experiment.logLine(logWriter, "correlation with errors = " + errorCorr);
				experiment.logLine(logWriter, "correlation with first constituent = " + featureCorrI);
				experiment.logLine(logWriter, "correlation with second constituent = " + featureCorrJ);
				experiment.logLine(logWriter, "varComplete = " + varComplete);
				experiment.logLine(logWriter, "varWithPair = " + varWithPair);
				experiment.logLine(logWriter, "varWithoutPair = " + varWithoutPair);
				experiment.logLine(logWriter, "varReduction = " + varReduction);

				return newFeatureSet;
			}

			// if we reach this point, it means we failed to create an
			// expanded feature set with our top-score pair.
			// so, we should search again for the next best pair
			bestScore = Double.NEGATIVE_INFINITY;
			bestPairIdx = -1;

			for (int i = 0; i < scoredPairs.size(); ++i)
			{
				if (scoredPairs.get(i).score > bestScore)
				{
					bestScore = scoredPairs.get(i).score;
					bestPairIdx = i;
				}
			}
		}
		
		return null;
	}

}
