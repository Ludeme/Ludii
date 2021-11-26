package training.feature_discovery;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import features.FeatureVector;
import features.feature_sets.BaseFeatureSet;
import features.spatial.FeatureUtils;
import features.spatial.SpatialFeature;
import features.spatial.instances.FeatureInstance;
import game.Game;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;
import main.collections.FVector;
import main.collections.FastArrayList;
import main.collections.ListUtils;
import main.math.MathRoutines;
import main.math.statistics.KolmogorovSmirnov;
import main.math.statistics.Sampling;
import other.move.Move;
import policies.softmax.SoftmaxPolicy;
import training.ExperienceSample;
import training.expert_iteration.gradients.Gradients;
import training.expert_iteration.params.FeatureDiscoveryParams;
import training.expert_iteration.params.ObjectiveParams;
import utils.experiments.InterruptableExperiment;

/**
 * Expands feature sets based on Kolmogorov-Smirnov statistics between distributions
 * of errors.
 *
 * @author Dennis Soemers
 */
public class KolmogorovSmirnovExpander implements FeatureSetExpander
{

	@Override
	public BaseFeatureSet expandFeatureSet
	(
		final List<? extends ExperienceSample> batch,
		final BaseFeatureSet featureSet,
		final SoftmaxPolicy policy,
		final Game game,
		final int featureDiscoveryMaxNumFeatureInstances,
		final ObjectiveParams objectiveParams,
		final FeatureDiscoveryParams featureDiscoveryParams,
		final TDoubleArrayList featureActiveRatios,
		final PrintWriter logWriter,
		final InterruptableExperiment experiment
	)
	{
		int numCases = 0;	// we'll increment  this as we go
		
		final Map<CombinableFeatureInstancePair, TDoubleArrayList> errorLists = 
				new HashMap<CombinableFeatureInstancePair, TDoubleArrayList>();

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
		
		// For every feature, compute sample correlation coefficient between its activity level (0 or 1)
		// and errors
		final double[] featureErrorCorrelations = new double[featureSet.getNumSpatialFeatures()];
		
		// For every feature, compute expectation of absolute value of error given that feature is active
		final double[] expectedAbsErrorGivenFeature = new double[featureSet.getNumSpatialFeatures()];
		
		// For every feature, computed expected value of feature activity times absolute error
		final double[] expectedFeatureTimesAbsError = new double[featureSet.getNumSpatialFeatures()];
		
		for (int fIdx = 0; fIdx < featureSet.getNumSpatialFeatures(); ++fIdx)
		{
			final TDoubleArrayList errorsWhenActive = errorsPerActiveFeature[fIdx];
			final TDoubleArrayList errorsWhenInactive = errorsPerInactiveFeature[fIdx];
			
			final double avgFeatureVal = 
					(double) errorsWhenActive.size() 
					/ 
					(errorsWhenActive.size() + errorsPerInactiveFeature[fIdx].size());
			
			double dErrorSquaresSum = 0.0;
			double numerator = 0.0;
			
			for (int i = 0; i < errorsWhenActive.size(); ++i)
			{
				final double error = errorsWhenActive.getQuick(i);
				final double dError = error - avgActionError;
				numerator += (1.0 - avgFeatureVal) * dError;
				dErrorSquaresSum += (dError * dError);
				
				expectedAbsErrorGivenFeature[fIdx] += (Math.abs(error) - expectedAbsErrorGivenFeature[fIdx]) / (i + 1);
				expectedFeatureTimesAbsError[fIdx] += (Math.abs(error) - expectedFeatureTimesAbsError[fIdx]) / (i + 1);
			}
			
			for (int i = 0; i < errorsWhenInactive.size(); ++i)
			{
				final double error = errorsWhenInactive.getQuick(i);
				final double dError = error - avgActionError;
				numerator += (0.0 - avgFeatureVal) * dError;
				dErrorSquaresSum += (dError * dError);
				
				expectedFeatureTimesAbsError[fIdx] += (0.0 - expectedFeatureTimesAbsError[fIdx]) / (i + 1);
			}
			
			final double dFeatureSquaresSum = 
					errorsWhenActive.size() * ((1.0 - avgFeatureVal) * (1.0 - avgFeatureVal))
					+
					errorsWhenInactive.size() * ((0.0 - avgFeatureVal) * (0.0 - avgFeatureVal));
			
			final double denominator = Math.sqrt(dFeatureSquaresSum * dErrorSquaresSum);
			featureErrorCorrelations[fIdx] = numerator / denominator;
			if (Double.isNaN(featureErrorCorrelations[fIdx]))
				featureErrorCorrelations[fIdx] = 0.f;
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
			final FVector errors = errorVectors[batchIndex];
			final float minError = errors.min();
			final float maxError = errors.max();
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
						activeInstances.remove(i);
					}
					else if (discardedInstances.contains(combinedSelf))
					{
						activeInstances.remove(i);
					}
					else if (featureActiveRatios.getQuick(instance.feature().spatialFeatureSetIndex()) == 1.0)
					{
						activeInstances.remove(i);
					}
					else
					{
						activeInstancesCombinedSelfs.add(combinedSelf);
						++i;
					}
				}

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
								featureErrorCorrelations[fIdx] + 
								expectedAbsErrorGivenFeature[fIdx] + 
								expectedFeatureTimesAbsError[fIdx] +
								Math.abs
								(
									policy.linearFunction
									(
										sample.gameState().mover()
									).effectiveParams().allWeights().get(fIdx + featureSet.getNumAspatialFeatures())
								)
							)
						);
					}
					distr.softmax(1.0);
					
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
					
//					for (int i = 0; i < activeInstances.size(); ++i)
//					{
//						System.out.println("prob = " + distr.get(i) + " for " + activeInstances.get(i));
//					}
	
					while (numInstancesAllowedThisAction > 0)
					{
						// Sample another instance
						final int sampledIdx = distr.sampleFromDistribution();
						final CombinableFeatureInstancePair combinedSelf = activeInstancesCombinedSelfs.get(sampledIdx);
						final FeatureInstance keepInstance = activeInstances.get(sampledIdx);
						instancesToKeep.add(keepInstance);
						instancesToKeepCombinedSelfs.add(combinedSelf);
						preservedInstances.add(combinedSelf);	// Remember to preserve this one forever now
						distr.updateSoftmaxInvalidate(sampledIdx);			// Don't want to pick the same index again
						--numInstancesAllowedThisAction;
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

				float error = errors.get(a);
				if (winningMoves.get(a))
				{
					error = minError;	// Reward correlation with winning moves
				}
				else if (losingMoves.get(a))
				{
					error = maxError;	// Reward correlation with losing moves
				}
				else if (antiDefeatingMoves.get(a))
				{
					error = Math.min(error, minError + 0.1f);	// Reward correlation with anti-defeating moves	
				}

				for (int i = 0; i < numActiveInstances; ++i)
				{
					final FeatureInstance instanceI = instancesToKeep.get(i);

					// increment entries on ''main diagonals''
					final CombinableFeatureInstancePair combinedSelf = instancesToKeepCombinedSelfs.get(i);

					if (observedCasePairs.add(combinedSelf))
					{
						TDoubleArrayList errorsList = errorLists.get(combinedSelf);
						if (errorsList == null)
						{
							errorsList = new TDoubleArrayList();
							errorLists.put(combinedSelf, errorsList);
						}
						
						errorsList.add(error);
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
								TDoubleArrayList errorsList = errorLists.get(combined);
								if (errorsList == null)
								{
									errorsList = new TDoubleArrayList();
									errorLists.put(combined, errorsList);
								}
								
								errorsList.add(error);
							}
						}
					}
				}
			}
		}

		// Construct all possible pairs and scores; one priority queue for proactive features,
		// and one for reactive features
		//
		// In priority queues, we want highest scores at the head, hence "reversed" implementation
		// of comparator
		final Comparator<ScoredFeatureInstancePair> comparator = new Comparator<ScoredFeatureInstancePair>()
		{
			@Override
			public int compare(final ScoredFeatureInstancePair o1, final ScoredFeatureInstancePair o2)
			{
				if (o1.score < o2.score)
					return 1;
				else if (o1.score > o2.score)
					return -1;
				else
					return 0;
			}
		};
		
		final PriorityQueue<ScoredFeatureInstancePair> proactivePairs = new PriorityQueue<ScoredFeatureInstancePair>(comparator);
		final PriorityQueue<ScoredFeatureInstancePair> reactivePairs = new PriorityQueue<ScoredFeatureInstancePair>(comparator);

		for (final CombinableFeatureInstancePair pair : errorLists.keySet())
		{
//			int numFriendElements = 0;
//			for (final FeatureElement element : ((RelativeFeature) pair.combinedFeature).pattern().featureElements())
//			{
//				if (element.type() == ElementType.Friend && !element.not())
//					++numFriendElements;
//			}
//			final boolean couldBeWinFeature = (numFriendElements >= 3);
			
			if (!pair.a.equals(pair.b))	// Only interested in combinations of different instances
			{
				final TDoubleArrayList pairErrors = errorLists.get(pair);
				final TDoubleArrayList errorsA = errorLists.get(new CombinableFeatureInstancePair(game, pair.a, pair.a));
				final TDoubleArrayList errorsB = errorLists.get(new CombinableFeatureInstancePair(game, pair.b, pair.b));
				
				pairErrors.sort();
				errorsA.sort();
				errorsB.sort();
				
				if (pairErrors.equals(errorsA) || pairErrors.equals(errorsB))
					continue;
				
				final int minSampleSize = 50;	// Synthetically create more samples if we don't have at least this many
				
				final int numRealPairSamples = pairErrors.size();
				while (pairErrors.size() < minSampleSize)
				{
					// Add a new sample drawn from a gaussian around a real sample
					final double realSample = pairErrors.getQuick(ThreadLocalRandom.current().nextInt(numRealPairSamples));
					final double syntheticSample = MathRoutines.clip(ThreadLocalRandom.current().nextGaussian() * 0.1 + realSample, -1.0, 1.0);
					pairErrors.add(syntheticSample);
				}
				
				final int numRealSamplesA = errorsA.size();
				while (errorsA.size() < minSampleSize)
				{
					// Add a new sample drawn from a gaussian around a real sample
					final double realSample = errorsA.getQuick(ThreadLocalRandom.current().nextInt(numRealSamplesA));
					final double syntheticSample = MathRoutines.clip(ThreadLocalRandom.current().nextGaussian() * 0.1 + realSample, -1.0, 1.0);
					errorsA.add(syntheticSample);
				}
				
				final int numRealSamplesB = errorsB.size();
				while (errorsB.size() < minSampleSize)
				{
					// Add a new sample drawn from a gaussian around a real sample
					final double realSample = errorsB.getQuick(ThreadLocalRandom.current().nextInt(numRealSamplesB));
					final double syntheticSample = MathRoutines.clip(ThreadLocalRandom.current().nextGaussian() * 0.1 + realSample, -1.0, 1.0);
					errorsB.add(syntheticSample);
				}
				
				final int numBootstrapsPerConstituent = 10;
				double minKolmogorovSmirnovDistance = 0.0;
				
				for (int i = 0; i < numBootstrapsPerConstituent; ++i)
				{
					final TDoubleArrayList bootstrapA = Sampling.sampleWithReplacement(Math.min(150, pairErrors.size()), errorsA);
					final TDoubleArrayList bootstrapB = Sampling.sampleWithReplacement(Math.min(150, pairErrors.size()), errorsB);
					final TDoubleArrayList bootstrapPair = Sampling.sampleWithReplacement(Math.min(150, pairErrors.size()), pairErrors);
					
					final double ksA = KolmogorovSmirnov.kolmogorovSmirnovStatistic(bootstrapPair, bootstrapA);
					final double ksB = KolmogorovSmirnov.kolmogorovSmirnovStatistic(bootstrapPair, bootstrapB);
					
					if (ksA < minKolmogorovSmirnovDistance)
						minKolmogorovSmirnovDistance = ksA;
					if (ksB < minKolmogorovSmirnovDistance)
						minKolmogorovSmirnovDistance = ksB;
				}
				
				final double score = minKolmogorovSmirnovDistance;
				
//				if (couldBeWinFeature)
//				{
//					System.out.println("Might be win feature: " + pair);
//					System.out.println("errorCorr = " + errorCorr);
//					System.out.println("lbErrorCorr = " + lbErrorCorr);
//					System.out.println("ubErrorCorr = " + ubErrorCorr);
//					System.out.println("numCases = " + numCases);
//					System.out.println("pairActs = " + pairActs);
//					System.out.println("actsI = " + actsI);
//					System.out.println("actsJ = " + actsJ);
//					System.out.println("featureCorrI = " + featureCorrI);
//					System.out.println("featureCorrJ = " + featureCorrJ);
//					System.out.println("score = " + score);
//				}

				if (pair.combinedFeature.isReactive())
					reactivePairs.add(new ScoredFeatureInstancePair(pair, score));
				else
					proactivePairs.add(new ScoredFeatureInstancePair(pair, score));
			}
		}

//		System.out.println("--------------------------------------------------------");
//		final PriorityQueue<ScoredFeatureInstancePair> allPairs = new PriorityQueue<ScoredFeatureInstancePair>(comparator);
//		allPairs.addAll(proactivePairs);
//		allPairs.addAll(reactivePairs);
//		while (!allPairs.isEmpty())
//		{
//			final ScoredFeatureInstancePair pair = allPairs.poll();
//			
//			final int actsI = 
//					featurePairActivations.get
//					(
//						new CombinableFeatureInstancePair(game, pair.pair.a, pair.pair.a)
//					);
//
//			final int actsJ = 
//					featurePairActivations.get
//					(
//						new CombinableFeatureInstancePair(game, pair.pair.b, pair.pair.b)
//					);
//
//			final int pairActs = 
//					featurePairActivations.get
//					(
//						new CombinableFeatureInstancePair(game, pair.pair.a, pair.pair.b)
//					);
//
//			final double pairErrorSum = 
//					errorSums.get
//					(
//						new CombinableFeatureInstancePair(game, pair.pair.a, pair.pair.b)
//					);
//
//			final double errorCorr = 
//					(
//						(numCases * pairErrorSum - pairActs * sumErrors) 
//						/ 
//						(
//							Math.sqrt(numCases * pairActs - pairActs * pairActs) * 
//							Math.sqrt(numCases * sumSquaredErrors - sumErrors * sumErrors)
//						)
//					);
//			// Fisher's r-to-z transformation
//			final double errorCorrZ = 0.5 * Math.log((1.0 + errorCorr) / (1.0 - errorCorr));
//			// Standard deviation of the z
//			final double stdErrorCorrZ = Math.sqrt(1.0 / (numCases - 3));
//			// Lower bound of 90% confidence interval on z
//			final double lbErrorCorrZ = errorCorrZ - 1.64 * stdErrorCorrZ;
//			// Transform lower bound on z back to r
//			final double lbErrorCorr = (Math.exp(2.0 * lbErrorCorrZ) - 1.0) / (Math.exp(2.0 * lbErrorCorrZ) + 1.0);
//			// Upper bound of 90% confidence interval on z
//			final double ubErrorCorrZ = errorCorrZ + 1.64 * stdErrorCorrZ;
//			// Transform upper bound on z back to r
//			final double ubErrorCorr = (Math.exp(2.0 * ubErrorCorrZ) - 1.0) / (Math.exp(2.0 * ubErrorCorrZ) + 1.0);
//
//			final double featureCorrI = 
//					(
//						(numCases * pairActs - pairActs * actsI) 
//						/ 
//						(
//							Math.sqrt(numCases * pairActs - pairActs * pairActs) * 
//							Math.sqrt(numCases * actsI - actsI * actsI)
//						)
//					);
//
//			final double featureCorrJ = 
//					(
//						(numCases * pairActs - pairActs * actsJ) 
//						/ 
//						(
//							Math.sqrt(numCases * pairActs - pairActs * pairActs) * 
//							Math.sqrt(numCases * actsJ - actsJ * actsJ)
//						)
//					);
//
//			System.out.println("score = " + pair.score);
//			System.out.println("correlation with errors = " + errorCorr);
//			System.out.println("lower bound correlation with errors = " + lbErrorCorr);
//			System.out.println("upper bound correlation with errors = " + ubErrorCorr);
//			System.out.println("correlation with first constituent = " + featureCorrI);
//			System.out.println("correlation with second constituent = " + featureCorrJ);
//			System.out.println("active feature A = " + pair.pair.a.feature());
//			System.out.println("rot A = " + pair.pair.a.rotation());
//			System.out.println("ref A = " + pair.pair.a.reflection());
//			System.out.println("anchor A = " + pair.pair.a.anchorSite());
//			System.out.println("active feature B = " + pair.pair.b.feature());
//			System.out.println("rot B = " + pair.pair.b.rotation());
//			System.out.println("ref B = " + pair.pair.b.reflection());
//			System.out.println("anchor B = " + pair.pair.b.anchorSite());
//			System.out.println("observed pair of instances " + pairActs + " times");
//			System.out.println("observed first constituent " + actsI + " times");
//			System.out.println("observed second constituent " + actsJ + " times");
//			System.out.println();
//		}
//		System.out.println("--------------------------------------------------------");

		// Keep trying to add a proactive feature, until we succeed (almost always 
		// this should be on the very first iteration)
		BaseFeatureSet currFeatureSet = featureSet;
		
		// TODO pretty much duplicate code of block below, should refactor into method
		while (!proactivePairs.isEmpty())
		{
			// extract pair of feature instances we want to try combining
			final ScoredFeatureInstancePair bestPair = proactivePairs.poll();

			final BaseFeatureSet newFeatureSet = 
					currFeatureSet.createExpandedFeatureSet(game, bestPair.pair.combinedFeature);

			if (newFeatureSet != null)
			{
//				final int actsI = 
//						featurePairActivations.get
//						(
//							new CombinableFeatureInstancePair(game, bestPair.pair.a, bestPair.pair.a)
//						);
//
//				final int actsJ = 
//						featurePairActivations.get
//						(
//							new CombinableFeatureInstancePair(game, bestPair.pair.b, bestPair.pair.b)
//						);
//				
//				final CombinableFeatureInstancePair pair = 
//						new CombinableFeatureInstancePair(game, bestPair.pair.a, bestPair.pair.b);
//				
//				final int pairActs = featurePairActivations.get(pair);
//				final double pairErrorSum = errorSums.get(pair);
//
//				final double errorCorr = 
//						(
//							(numCases * pairErrorSum - pairActs * sumErrors) 
//							/ 
//							(
//								Math.sqrt(numCases * pairActs - pairActs * pairActs) * 
//								Math.sqrt(numCases * sumSquaredErrors - sumErrors * sumErrors)
//							)
//						);
//				
//				// Fisher's r-to-z transformation
//				final double errorCorrZ = 0.5 * Math.log((1.0 + errorCorr) / (1.0 - errorCorr));
//				// Standard deviation of the z
//				final double stdErrorCorrZ = Math.sqrt(1.0 / (numCases - 3));
//				// Lower bound of 90% confidence interval on z
//				final double lbErrorCorrZ = errorCorrZ - 1.64 * stdErrorCorrZ;
//				// Transform lower bound on z back to r
//				final double lbErrorCorr = (Math.exp(2.0 * lbErrorCorrZ) - 1.0) / (Math.exp(2.0 * lbErrorCorrZ) + 1.0);
//				// Upper bound of 90% confidence interval on z
//				final double ubErrorCorrZ = errorCorrZ + 1.64 * stdErrorCorrZ;
//				// Transform upper bound on z back to r
//				final double ubErrorCorr = (Math.exp(2.0 * ubErrorCorrZ) - 1.0) / (Math.exp(2.0 * ubErrorCorrZ) + 1.0);
//
//				final double featureCorrI = 
//						(
//							(numCases * pairActs - pairActs * actsI) 
//							/ 
//							(
//								Math.sqrt(numCases * pairActs - pairActs * pairActs) * 
//								Math.sqrt(numCases * actsI - actsI * actsI)
//							)
//						);
//
//				final double featureCorrJ = 
//						(
//							(numCases * pairActs - pairActs * actsJ) 
//							/ 
//							(
//								Math.sqrt(numCases * pairActs - pairActs * pairActs) * 
//								Math.sqrt(numCases * actsJ - actsJ * actsJ)
//							)
//						);
//
//				experiment.logLine(logWriter, "New proactive feature added!");
//				experiment.logLine(logWriter, "new feature = " + newFeatureSet.spatialFeatures()[newFeatureSet.getNumSpatialFeatures() - 1]);
//				experiment.logLine(logWriter, "active feature A = " + bestPair.pair.a.feature());
//				experiment.logLine(logWriter, "rot A = " + bestPair.pair.a.rotation());
//				experiment.logLine(logWriter, "ref A = " + bestPair.pair.a.reflection());
//				experiment.logLine(logWriter, "anchor A = " + bestPair.pair.a.anchorSite());
//				experiment.logLine(logWriter, "active feature B = " + bestPair.pair.b.feature());
//				experiment.logLine(logWriter, "rot B = " + bestPair.pair.b.rotation());
//				experiment.logLine(logWriter, "ref B = " + bestPair.pair.b.reflection());
//				experiment.logLine(logWriter, "anchor B = " + bestPair.pair.b.anchorSite());
//				experiment.logLine(logWriter, "avg error = " + sumErrors / numCases);
//				experiment.logLine(logWriter, "avg error for pair = " + pairErrorSum / pairActs);
//				experiment.logLine(logWriter, "score = " + bestPair.score);
//				experiment.logLine(logWriter, "correlation with errors = " + errorCorr);
//				experiment.logLine(logWriter, "lower bound correlation with errors = " + lbErrorCorr);
//				experiment.logLine(logWriter, "upper bound correlation with errors = " + ubErrorCorr);
//				experiment.logLine(logWriter, "correlation with first constituent = " + featureCorrI);
//				experiment.logLine(logWriter, "correlation with second constituent = " + featureCorrJ);
//				experiment.logLine(logWriter, "observed pair of instances " + pairActs + " times");
//				experiment.logLine(logWriter, "observed first constituent " + actsI + " times");
//				experiment.logLine(logWriter, "observed second constituent " + actsJ + " times");

				currFeatureSet = newFeatureSet;
				break;
			}
		}
		
		// Keep trying to add a reactive feature, until we succeed (almost always 
		// this should be on the very first iteration)
		while (!reactivePairs.isEmpty())
		{
			// extract pair of feature instances we want to try combining
			final ScoredFeatureInstancePair bestPair = reactivePairs.poll();

			final BaseFeatureSet newFeatureSet = 
					currFeatureSet.createExpandedFeatureSet(game, bestPair.pair.combinedFeature);

			if (newFeatureSet != null)
			{
//				final int actsI = 
//						featurePairActivations.get
//						(
//							new CombinableFeatureInstancePair(game, bestPair.pair.a, bestPair.pair.a)
//						);
//
//				final int actsJ = 
//						featurePairActivations.get
//						(
//							new CombinableFeatureInstancePair(game, bestPair.pair.b, bestPair.pair.b)
//						);
//
//				final CombinableFeatureInstancePair pair = 
//						new CombinableFeatureInstancePair(game, bestPair.pair.a, bestPair.pair.b);
//				
//				final int pairActs = featurePairActivations.get(pair);
//				final double pairErrorSum = errorSums.get(pair);
//
//				final double errorCorr = 
//						(
//							(numCases * pairErrorSum - pairActs * sumErrors) 
//							/ 
//							(
//								Math.sqrt(numCases * pairActs - pairActs * pairActs) * 
//								Math.sqrt(numCases * sumSquaredErrors - sumErrors * sumErrors)
//							)
//						);
//				
//				// Fisher's r-to-z transformation
//				final double errorCorrZ = 0.5 * Math.log((1.0 + errorCorr) / (1.0 - errorCorr));
//				// Standard deviation of the z
//				final double stdErrorCorrZ = Math.sqrt(1.0 / (numCases - 3));
//				// Lower bound of 90% confidence interval on z
//				final double lbErrorCorrZ = errorCorrZ - 1.64 * stdErrorCorrZ;
//				// Transform lower bound on z back to r
//				final double lbErrorCorr = (Math.exp(2.0 * lbErrorCorrZ) - 1.0) / (Math.exp(2.0 * lbErrorCorrZ) + 1.0);
//				// Upper bound of 90% confidence interval on z
//				final double ubErrorCorrZ = errorCorrZ + 1.64 * stdErrorCorrZ;
//				// Transform upper bound on z back to r
//				final double ubErrorCorr = (Math.exp(2.0 * ubErrorCorrZ) - 1.0) / (Math.exp(2.0 * ubErrorCorrZ) + 1.0);
//
//				final double featureCorrI = 
//						(
//							(numCases * pairActs - pairActs * actsI) 
//							/ 
//							(
//								Math.sqrt(numCases * pairActs - pairActs * pairActs) * 
//								Math.sqrt(numCases * actsI - actsI * actsI)
//							)
//						);
//
//				final double featureCorrJ = 
//						(
//							(numCases * pairActs - pairActs * actsJ) 
//							/ 
//							(
//								Math.sqrt(numCases * pairActs - pairActs * pairActs) * 
//								Math.sqrt(numCases * actsJ - actsJ * actsJ)
//							)
//						);
//
//				experiment.logLine(logWriter, "New reactive feature added!");
//				experiment.logLine(logWriter, "new feature = " + newFeatureSet.spatialFeatures()[newFeatureSet.getNumSpatialFeatures() - 1]);
//				experiment.logLine(logWriter, "active feature A = " + bestPair.pair.a.feature());
//				experiment.logLine(logWriter, "rot A = " + bestPair.pair.a.rotation());
//				experiment.logLine(logWriter, "ref A = " + bestPair.pair.a.reflection());
//				experiment.logLine(logWriter, "anchor A = " + bestPair.pair.a.anchorSite());
//				experiment.logLine(logWriter, "active feature B = " + bestPair.pair.b.feature());
//				experiment.logLine(logWriter, "rot B = " + bestPair.pair.b.rotation());
//				experiment.logLine(logWriter, "ref B = " + bestPair.pair.b.reflection());
//				experiment.logLine(logWriter, "anchor B = " + bestPair.pair.b.anchorSite());
//				experiment.logLine(logWriter, "avg error = " + sumErrors / numCases);
//				experiment.logLine(logWriter, "avg error for pair = " + pairErrorSum / pairActs);
//				experiment.logLine(logWriter, "score = " + bestPair.score);
//				experiment.logLine(logWriter, "correlation with errors = " + errorCorr);
//				experiment.logLine(logWriter, "lower bound correlation with errors = " + lbErrorCorr);
//				experiment.logLine(logWriter, "upper bound correlation with errors = " + ubErrorCorr);
//				experiment.logLine(logWriter, "correlation with first constituent = " + featureCorrI);
//				experiment.logLine(logWriter, "correlation with second constituent = " + featureCorrJ);
//				experiment.logLine(logWriter, "observed pair of instances " + pairActs + " times");
//				experiment.logLine(logWriter, "observed first constituent " + actsI + " times");
//				experiment.logLine(logWriter, "observed second constituent " + actsJ + " times");

				currFeatureSet = newFeatureSet;
				break;
			}
		}

		return currFeatureSet;
	}

}
