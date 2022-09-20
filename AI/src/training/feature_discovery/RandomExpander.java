package training.feature_discovery;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import features.FeatureVector;
import features.feature_sets.BaseFeatureSet;
import features.spatial.FeatureUtils;
import features.spatial.SpatialFeature;
import features.spatial.instances.FeatureInstance;
import game.Game;
import gnu.trove.impl.Constants;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TObjectIntHashMap;
import main.collections.FVector;
import main.collections.FastArrayList;
import main.collections.ListUtils;
import other.move.Move;
import policies.softmax.SoftmaxPolicyLinear;
import training.ExperienceSample;
import training.expert_iteration.gradients.Gradients;
import training.expert_iteration.params.FeatureDiscoveryParams;
import training.expert_iteration.params.ObjectiveParams;
import utils.experiments.InterruptableExperiment;

/**
 * Random Feature Set Expander.
 *
 * @author Dennis Soemers
 */
public class RandomExpander implements FeatureSetExpander
{

	@Override
	public BaseFeatureSet expandFeatureSet
	(
		final List<? extends ExperienceSample> batch,
		final BaseFeatureSet featureSet,
		final SoftmaxPolicyLinear policy,
		final Game game,
		final int featureDiscoveryMaxNumFeatureInstances,
		final ObjectiveParams objectiveParams,
		final FeatureDiscoveryParams featureDiscoveryParams,
		final TDoubleArrayList featureActiveRatios,
		final PrintWriter logWriter,
		final InterruptableExperiment experiment
	)
	{
//		System.out.println("-------------------------------------------------------------------");
		int numCases = 0;	// we'll increment  this as we go

		// this is our C_f matrix
		final TObjectIntHashMap<CombinableFeatureInstancePair> featurePairActivations = 
				new TObjectIntHashMap<CombinableFeatureInstancePair>(Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, 0);

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

		// For every sample in batch, first compute apprentice policies, errors, and sum of absolute errors
		final FVector[] apprenticePolicies = new FVector[batch.size()];
		final FVector[] errorVectors = new FVector[batch.size()];
		final float[] absErrorSums = new float[batch.size()];
		
		final TDoubleArrayList[] errorsPerActiveFeature = new TDoubleArrayList[featureSet.getNumSpatialFeatures()];
		final TDoubleArrayList[] errorsPerInactiveFeature = new TDoubleArrayList[featureSet.getNumSpatialFeatures()];
		for (int i = 0; i < errorsPerActiveFeature.length; ++i)
		{
			errorsPerActiveFeature[i] = new TDoubleArrayList();
			errorsPerInactiveFeature[i] = new TDoubleArrayList();
		}
		double avgActionError = 0.0;

		for (int i = 0; i < batch.size(); ++i)
		{
			final ExperienceSample sample = batch.get(i);

			final FeatureVector[] featureVectors = sample.generateFeatureVectors(featureSet);

			final FVector apprenticePolicy = 
					policy.computeDistribution(featureVectors, sample.gameState().mover());
			final FVector errors = 
					Gradients.computeDistributionErrors
					(
						apprenticePolicy,
						sample.expertDistribution()
					);
			
			for (int a = 0; a < featureVectors.length; ++a)
			{
				final float actionError = errors.get(a);
				final TIntArrayList sparseFeatureVector = featureVectors[a].activeSpatialFeatureIndices();
				sparseFeatureVector.sort();
				int sparseIdx = 0;
				
				for (int featureIdx = 0; featureIdx < featureSet.getNumSpatialFeatures(); ++featureIdx)
				{
					if (sparseIdx < sparseFeatureVector.size() && sparseFeatureVector.getQuick(sparseIdx) == featureIdx)
					{
						// This spatial feature is active
						errorsPerActiveFeature[featureIdx].add(actionError);
						
						// We've used the sparse index, so increment it
						++sparseIdx;
					}
					else
					{
						// This spatial feature is not active
						errorsPerInactiveFeature[featureIdx].add(actionError);
					}
				}
				
				avgActionError += (actionError - avgActionError) / (numCases + 1);
				++numCases;
			}

			final FVector absErrors = errors.copy();
			absErrors.abs();

			apprenticePolicies[i] = apprenticePolicy;
			errorVectors[i] = errors;
			absErrorSums[i] = absErrors.sum();
		}
		
		// For every feature, compute expectation of absolute value of error given that feature is active
		final double[] expectedAbsErrorGivenFeature = new double[featureSet.getNumSpatialFeatures()];
		
		// For every feature, computed expected value of feature activity times absolute error
//		final double[] expectedFeatureTimesAbsError = new double[featureSet.getNumSpatialFeatures()];
		
		for (int fIdx = 0; fIdx < featureSet.getNumSpatialFeatures(); ++fIdx)
		{
			final TDoubleArrayList errorsWhenActive = errorsPerActiveFeature[fIdx];
			
			for (int i = 0; i < errorsWhenActive.size(); ++i)
			{
				final double error = errorsWhenActive.getQuick(i);
				expectedAbsErrorGivenFeature[fIdx] += (Math.abs(error) - expectedAbsErrorGivenFeature[fIdx]) / (i + 1);
			}
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
		
		// Set of feature instances that we have already preserved (and hence must continue to preserve)
		final Set<CombinableFeatureInstancePair> preservedInstances = new HashSet<CombinableFeatureInstancePair>();

		// Set of feature instances that we have already chosen to discard once (and hence must continue to discard)
		final Set<CombinableFeatureInstancePair> discardedInstances = new HashSet<CombinableFeatureInstancePair>();
		
		// Loop through all samples in batch
		for (int bi = 0; bi < batchIndices.size(); ++bi)
		{
			final int batchIndex = batchIndices.get(bi).intValue();
			final ExperienceSample sample = batch.get(batchIndex);
			final FastArrayList<Move> moves = sample.moves();
			
			final TIntArrayList sortedActionIndices = new TIntArrayList();
			
			// Want to start looking at winning moves
			final BitSet winningMoves = sample.winningMoves();
			for (int i = winningMoves.nextSetBit(0); i >= 0; i = winningMoves.nextSetBit(i + 1))
			{
				sortedActionIndices.add(i);
			}
			
			// Look at losing moves next
			final BitSet losingMoves = sample.losingMoves();
			for (int i = losingMoves.nextSetBit(0); i >= 0; i = losingMoves.nextSetBit(i + 1))
			{
				sortedActionIndices.add(i);
			}
			
			// And finally anti-defeating moves
			final BitSet antiDefeatingMoves = sample.antiDefeatingMoves();
			for (int i = antiDefeatingMoves.nextSetBit(0); i >= 0; i = antiDefeatingMoves.nextSetBit(i + 1))
			{
				sortedActionIndices.add(i);
			}
			
			final TIntArrayList unsortedActionIndices = new TIntArrayList();
			for (int a = 0; a < moves.size(); ++a)
			{
				if (!winningMoves.get(a) && !losingMoves.get(a) && !antiDefeatingMoves.get(a))
					unsortedActionIndices.add(a);
			}
			
			// Finally, randomly fill up with the remaining actions
			while (!unsortedActionIndices.isEmpty())
			{
				final int r = ThreadLocalRandom.current().nextInt(unsortedActionIndices.size());
				final int a = unsortedActionIndices.getQuick(r);
				ListUtils.removeSwap(unsortedActionIndices, r);
				sortedActionIndices.add(a);
			}
			
			// Every action in the sample is a new "case" (state-action pair)
			for (int aIdx = 0; aIdx < sortedActionIndices.size(); ++aIdx)
			{
				final int a = sortedActionIndices.getQuick(aIdx);
				
				// keep track of pairs we've already seen in this "case"
				final Set<CombinableFeatureInstancePair> observedCasePairs = 
						new HashSet<CombinableFeatureInstancePair>(256, .75f);

				// list --> set --> list to get rid of duplicates
				final List<FeatureInstance> activeInstances = new ArrayList<FeatureInstance>(new HashSet<FeatureInstance>(
						featureSet.getActiveSpatialFeatureInstances
						(
							sample.gameState(), 
							sample.lastFromPos(), 
							sample.lastToPos(), 
							FeatureUtils.fromPos(moves.get(a)), 
							FeatureUtils.toPos(moves.get(a)),
							moves.get(a).mover()
						)));
				
				// Save a copy of the above list, which we leave unmodified
				final List<FeatureInstance> origActiveInstances = new ArrayList<FeatureInstance>(activeInstances);

				// Start out by keeping all feature instances that have already been marked as having to be
				// preserved, and discarding those that have already been discarded before
				final List<FeatureInstance> instancesToKeep = new ArrayList<FeatureInstance>();
				
				// For every instance in activeInstances, also keep track of a combined-self version
				final List<CombinableFeatureInstancePair> activeInstancesCombinedSelfs = 
						new ArrayList<CombinableFeatureInstancePair>();
				// And same for instancesToKeep
				final List<CombinableFeatureInstancePair> instancesToKeepCombinedSelfs = 
						new ArrayList<CombinableFeatureInstancePair>();

//				int numRelevantConstituents = 0;
				for (int i = 0; i < activeInstances.size(); /**/)
				{
					final FeatureInstance instance = activeInstances.get(i);
					final CombinableFeatureInstancePair combinedSelf = new CombinableFeatureInstancePair(game, instance, instance);
					
					if (preservedInstances.contains(combinedSelf))
					{
//						if (instancesToKeepCombinedSelfs.contains(combinedSelf))
//						{
//							System.out.println("already contains: " + combinedSelf);
//							System.out.println("instance: " + instance);
//						}
						instancesToKeepCombinedSelfs.add(combinedSelf);
						instancesToKeep.add(instance);
						ListUtils.removeSwap(activeInstances, i);
					}
					else if (discardedInstances.contains(combinedSelf))
					{
						ListUtils.removeSwap(activeInstances, i);
					}
					else if (featureActiveRatios.getQuick(instance.feature().spatialFeatureSetIndex()) == 1.0)
					{
						ListUtils.removeSwap(activeInstances, i);
					}
					else
					{
						activeInstancesCombinedSelfs.add(combinedSelf);
						++i;
					}
				}
				
//				if (activeInstances.size() > 0)
//					System.out.println(activeInstances.size() + "/" + origActiveInstances.size() + " left");

				// This action is allowed to pick at most this many extra instances
				int numInstancesAllowedThisAction = 
						Math.min
						(
							Math.min
							(
								50,
								featureDiscoveryMaxNumFeatureInstances - preservedInstances.size()
							),
							activeInstances.size()
						);
				
				if (numInstancesAllowedThisAction > 0)
				{
//					System.out.println("allowed to add " + numInstancesAllowedThisAction + " instances");
					
					// Create distribution over active instances proportional to scores that reward
					// features that correlate strongly with errors, as well as features that, when active,
					// imply expectations of high absolute errors, as well as features that are often active
					// when absolute errors are high, as well as features that have high absolute weights
					final FVector distr = new FVector(activeInstances.size());
					for (int i = 0; i < activeInstances.size(); ++i)
					{
						final int fIdx = activeInstances.get(i).feature().spatialFeatureSetIndex();
						distr.set
						(
							i, 
							(float) 
							(
								expectedAbsErrorGivenFeature[fIdx]
							)
						);
					}
					distr.softmax(2.0);
					
					// For every instance, divide its probability by the number of active instances for the same
					// feature (because that feature is sort of "overrepresented")
					for (int i = 0; i < activeInstances.size(); ++i)
					{
						final int fIdx = activeInstances.get(i).feature().spatialFeatureSetIndex();
						int featureCount = 0;
						
						for (int j = 0; j < origActiveInstances.size(); ++j)
						{
							if (origActiveInstances.get(j).feature().spatialFeatureSetIndex() == fIdx)
								++featureCount;
						}
						
						distr.set(i, distr.get(i) / featureCount);
					}
					distr.normalise();
	
					while (numInstancesAllowedThisAction > 0)
					{
						// Sample another instance
						final int sampledIdx = distr.sampleFromDistribution();
						final CombinableFeatureInstancePair combinedSelf = activeInstancesCombinedSelfs.get(sampledIdx);
						final FeatureInstance keepInstance = activeInstances.get(sampledIdx);
						instancesToKeep.add(keepInstance);
						instancesToKeepCombinedSelfs.add(combinedSelf);
						preservedInstances.add(combinedSelf);				// Remember to preserve this one forever now
						distr.updateSoftmaxInvalidate(sampledIdx);			// Don't want to pick the same index again
						--numInstancesAllowedThisAction;
						
						// Maybe now have to auto-pick several other instances if they lead to equal combinedSelf
						for (int i = 0; i < distr.dim(); ++i)
						{
							if (distr.get(0) != 0.f)
							{
								if (combinedSelf.equals(activeInstancesCombinedSelfs.get(i)))
								{
									//System.out.println("auto-picking " + activeInstances.get(i) + " after " + keepInstance);
									instancesToKeep.add(activeInstances.get(i));
									instancesToKeepCombinedSelfs.add(activeInstancesCombinedSelfs.get(i));
									distr.updateSoftmaxInvalidate(i);			// Don't want to pick the same index again
									--numInstancesAllowedThisAction;
								}
							}
						}
					}
				}

				// Mark all the instances that haven't been marked as preserved yet as discarded instead
				for (int i = 0; i < activeInstances.size(); ++i)
				{
					final CombinableFeatureInstancePair combinedSelf = new CombinableFeatureInstancePair(game, activeInstances.get(i), activeInstances.get(i));
					if (!preservedInstances.contains(combinedSelf))
						discardedInstances.add(combinedSelf);
				}

				final int numActiveInstances = instancesToKeep.size();
				//System.out.println("numActiveInstances = " + numActiveInstances);

				for (int i = 0; i < numActiveInstances; ++i)
				{
					final FeatureInstance instanceI = instancesToKeep.get(i);

					// increment entries on ''main diagonals''
					final CombinableFeatureInstancePair combinedSelf = instancesToKeepCombinedSelfs.get(i);

					if (observedCasePairs.add(combinedSelf))
					{
						featurePairActivations.adjustOrPutValue(combinedSelf, 1, 1);
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
							}
						}
					}
				}
			}
		}

		final List<CombinableFeatureInstancePair> proactivePairs = new ArrayList<CombinableFeatureInstancePair>();
		final List<CombinableFeatureInstancePair> reactivePairs = new ArrayList<CombinableFeatureInstancePair>();
		
		// Randomly pick a minimum required sample size in [3, 5]
		final int requiredSampleSize = 3 + ThreadLocalRandom.current().nextInt(3);

		for (final CombinableFeatureInstancePair pair : featurePairActivations.keySet())
		{
			if (!pair.a.equals(pair.b))	// Only interested in combinations of different instances
			{
				final int pairActs = featurePairActivations.get(pair);
				if (pairActs == numCases || numCases < 4)
				{
					// Perfect correlation, so we should just skip this one
					continue;
				}
				
				if (pairActs < requiredSampleSize)
				{
					// Need a bigger sample size
					continue;	
				}

				final int actsI = featurePairActivations.get(new CombinableFeatureInstancePair(game, pair.a, pair.a));
				final int actsJ = featurePairActivations.get(new CombinableFeatureInstancePair(game, pair.b, pair.b));

				if (actsI == numCases || actsJ == numCases || pairActs == actsI || pairActs == actsJ)
				{
					// Perfect correlation, so we should just skip this one
					continue;
				}

				if (pair.combinedFeature.isReactive())
					reactivePairs.add(pair);
				else
					proactivePairs.add(pair);
			}
		}
		
		// Shuffle lists for random feature combination
		Collections.shuffle(proactivePairs);
		Collections.shuffle(reactivePairs);

		// Keep trying to add a proactive feature, until we succeed (almost always 
		// this should be on the very first iteration)
		BaseFeatureSet currFeatureSet = featureSet;
		
		// TODO pretty much duplicate code of block below, should refactor into method
		while (!proactivePairs.isEmpty())
		{
			// extract pair of feature instances we want to try combining
			final CombinableFeatureInstancePair bestPair = proactivePairs.remove(proactivePairs.size() - 1);

			final BaseFeatureSet newFeatureSet = 
					currFeatureSet.createExpandedFeatureSet(game, bestPair.combinedFeature);

			if (newFeatureSet != null)
			{
				final int actsI = 
						featurePairActivations.get
						(
							new CombinableFeatureInstancePair(game, bestPair.a, bestPair.a)
						);

				final int actsJ = 
						featurePairActivations.get
						(
							new CombinableFeatureInstancePair(game, bestPair.b, bestPair.b)
						);
				
				final CombinableFeatureInstancePair pair = 
						new CombinableFeatureInstancePair(game, bestPair.a, bestPair.b);
				
				final int pairActs = featurePairActivations.get(pair);
				
				experiment.logLine(logWriter, "New proactive feature added!");
				experiment.logLine(logWriter, "new feature = " + newFeatureSet.spatialFeatures()[newFeatureSet.getNumSpatialFeatures() - 1]);
				experiment.logLine(logWriter, "active feature A = " + bestPair.a.feature());
				experiment.logLine(logWriter, "rot A = " + bestPair.a.rotation());
				experiment.logLine(logWriter, "ref A = " + bestPair.a.reflection());
				experiment.logLine(logWriter, "anchor A = " + bestPair.a.anchorSite());
				experiment.logLine(logWriter, "active feature B = " + bestPair.b.feature());
				experiment.logLine(logWriter, "rot B = " + bestPair.b.rotation());
				experiment.logLine(logWriter, "ref B = " + bestPair.b.reflection());
				experiment.logLine(logWriter, "anchor B = " + bestPair.b.anchorSite());
				experiment.logLine(logWriter, "observed pair of instances " + pairActs + " times");
				experiment.logLine(logWriter, "observed first constituent " + actsI + " times");
				experiment.logLine(logWriter, "observed second constituent " + actsJ + " times");

				currFeatureSet = newFeatureSet;
				break;
			}
		}
		
		// Keep trying to add a reactive feature, until we succeed (almost always 
		// this should be on the very first iteration)
		while (!reactivePairs.isEmpty())
		{
			// extract pair of feature instances we want to try combining
			final CombinableFeatureInstancePair bestPair = reactivePairs.remove(reactivePairs.size() - 1);

			final BaseFeatureSet newFeatureSet = 
					currFeatureSet.createExpandedFeatureSet(game, bestPair.combinedFeature);

			if (newFeatureSet != null)
			{
				final int actsI = 
						featurePairActivations.get
						(
							new CombinableFeatureInstancePair(game, bestPair.a, bestPair.a)
						);

				final int actsJ = 
						featurePairActivations.get
						(
							new CombinableFeatureInstancePair(game, bestPair.b, bestPair.b)
						);

				final CombinableFeatureInstancePair pair = 
						new CombinableFeatureInstancePair(game, bestPair.a, bestPair.b);
				
				final int pairActs = featurePairActivations.get(pair);

				experiment.logLine(logWriter, "New reactive feature added!");
				experiment.logLine(logWriter, "new feature = " + newFeatureSet.spatialFeatures()[newFeatureSet.getNumSpatialFeatures() - 1]);
				experiment.logLine(logWriter, "active feature A = " + bestPair.a.feature());
				experiment.logLine(logWriter, "rot A = " + bestPair.a.rotation());
				experiment.logLine(logWriter, "ref A = " + bestPair.a.reflection());
				experiment.logLine(logWriter, "anchor A = " + bestPair.a.anchorSite());
				experiment.logLine(logWriter, "active feature B = " + bestPair.b.feature());
				experiment.logLine(logWriter, "rot B = " + bestPair.b.rotation());
				experiment.logLine(logWriter, "ref B = " + bestPair.b.reflection());
				experiment.logLine(logWriter, "anchor B = " + bestPair.b.anchorSite());
				experiment.logLine(logWriter, "observed pair of instances " + pairActs + " times");
				experiment.logLine(logWriter, "observed first constituent " + actsI + " times");
				experiment.logLine(logWriter, "observed second constituent " + actsJ + " times");

				currFeatureSet = newFeatureSet;
				break;
			}
		}

		return currFeatureSet;
	}

}
