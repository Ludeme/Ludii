package training.feature_discovery;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
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
import gnu.trove.map.hash.TObjectDoubleHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import main.collections.FVector;
import main.collections.FastArrayList;
import main.collections.ListUtils;
import other.move.Move;
import policies.softmax.SoftmaxPolicy;
import training.ExperienceSample;
import training.expert_iteration.params.FeatureDiscoveryParams;
import training.expert_iteration.params.ObjectiveParams;
import utils.experiments.InterruptableExperiment;

/**
 * Correlation-based Feature Set Expander. Mostly the same as the one proposed in our
 * CEC 2019 paper (https://arxiv.org/abs/1903.08942), possibly with some small
 * changes implemented since then.
 *
 * @author Dennis Soemers
 */
public class CorrelationBasedExpander implements FeatureSetExpander
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
		// we need a matrix C_f with, at every entry (i, j), 
		// the sum of cases in which features i and j are both active
		//
		// we also need a similar matrix C_e with, at every entry (i, j),
		// the sum of errors in cases where features i and j are both active
		//
		// NOTE: both of the above are symmetric matrices
		//
		// we also need a vector X_f with, for every feature i, the sum of
		// cases where feature i is active. (we would need a similar vector
		// with sums of squares, but that can be omitted because our features
		// are binary)
		//
		// NOTE: in writing it is easier to mention X_f as described above as
		// a separate vector, but actually we don't need it; we can simply
		// use the main diagonal of C_f instead
		//
		// we need a scalar S which is the sum of all errors,
		// and a scalar SS which is the sum of all squared errors
		//
		// Given a candidate pair of features (i, j), we can compute the
		// correlation of that pair of features with the errors (an
		// indicator of the candidate pair's potential to be useful) as
		// (where n = the number of cases = number of state-action pairs):
		//
		//
		//                 n * C_e(i, j) - C_f(i, j) * S
		//------------------------------------------------------------
		//  SQRT( n * C_f(i, j) - C_f(i, j)^2 ) * SQRT( n * SS - S^2 )
		//
		//
		// For numeric stability with extremely large batch sizes, 
		// it is better to compute this as:
		// 
		//                 n * C_e(i, j) - C_f(i, j) * S
		//------------------------------------------------------------
		//  SQRT( C_f(i, j) * (n - C_f(i, j)) ) * SQRT( n * SS - S^2 )
		//
		//
		// We can similarly compare the correlation between any candidate pair 
		// of features (i, j) and either of its constituents i (an indicator
		// of redundancy of the candidate pair) as:
		//
		//
		//                 n * C_f(i, j) - C_f(i, j) * X_f(i)
		//--------------------------------------------------------------------
		//  SQRT( n * C_f(i, j) - C_f(i, j)^2 ) * SQRT( n * X_f(i) - X_f(i)^2 )
		//
		//
		// For numeric stability with extremely large batch sizes, 
		// it is better to compute this as:
		//
		//                C_f(i, j) * (n - X_f(i))
		//--------------------------------------------------------------------
		//  SQRT( C_f(i, j) * (n - C_f(i, j)) ) * SQRT( X_f(i) * (n - X_f(i)) )
		//
		//
		// (note; even better would be if we could compute correlation between
		// candidate pair and ANY other feature, rather than just its
		// constituents, but that would probably become too expensive)
		//
		// We want to maximise the absolute value of the first (correlation
		// between candidate feature pair and distribution error), but minimise
		// the worst-case absolute value of the second (correlation between 
		// candidate feature pair and either of its constituents).
		//
		//
		// NOTE: in all of the above, when we say "feature", we actually
		// refer to a particular instantiation of a feature. This is important,
		// because otherwise we wouldn't be able to merge different
		// instantiations of the same feature into a more complex new feature
		//
		// Due to the large amount of possible pairings when considering
		// feature instances, we implement our "matrices" using hash tables,
		// and automatically ignore entries that would be 0 (they won't
		// be created if such pairs are never observed activating together)

//		System.out.println("-------------------------------------------------------------------");
		int numCases = 0;	// we'll increment  this as we go

		// this is our C_f matrix
		final TObjectIntHashMap<CombinableFeatureInstancePair> featurePairActivations = 
				new TObjectIntHashMap<CombinableFeatureInstancePair>(Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, 0);

		// this is our C_e matrix
		final TObjectDoubleHashMap<CombinableFeatureInstancePair> errorSums = 
				new TObjectDoubleHashMap<CombinableFeatureInstancePair>(Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, 0.0);

		// these are our S and SS scalars
		double sumErrors = 0.0;
		double sumSquaredErrors = 0.0;

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
					policy.computeDistributionErrors
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
					if (sparseFeatureVector.getQuick(sparseIdx) == featureIdx)
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
							FeatureUtils.fromPos(sample.lastDecisionMove()), 
							FeatureUtils.toPos(sample.lastDecisionMove()), 
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
//					for (int i = 0; i < activeInstances.size(); ++i)
//					{
//						final int fIdx = activeInstances.get(i).feature().spatialFeatureSetIndex();
//						distr.set
//						(
//							i, 
//							(float) 
//							(
//								featureErrorCorrelations[fIdx] + 
//								expectedAbsErrorGivenFeature[fIdx] + 
//								expectedFeatureTimesAbsError[fIdx] +
//								Math.abs
//								(
//									policy.linearFunction
//									(
//										sample.gameState().mover()
//									).effectiveParams().allWeights().get(fIdx + featureSet.getNumAspatialFeatures())
//								)
//							)
//						);
//					}
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

				sumErrors += error;
				sumSquaredErrors += error * error;

				for (int i = 0; i < numActiveInstances; ++i)
				{
					final FeatureInstance instanceI = instancesToKeep.get(i);

					// increment entries on ''main diagonals''
					final CombinableFeatureInstancePair combinedSelf = instancesToKeepCombinedSelfs.get(i);
					
//					boolean relevantConstituent = false;
//					if (!combinedSelf.combinedFeature.isReactive())
//					{
//						if (combinedSelf.combinedFeature.pattern().featureElements().length == 1)
//						{
//							relevantConstituent = true;
//							final FeatureElement element = combinedSelf.combinedFeature.pattern().featureElements()[0];
//							if 
//							(
//								element.not() 
//								|| 
//								element.type() != ElementType.Friend
//								||
//								((RelativeFeatureElement)element).walk().steps().size() != 2
//								||
//								((RelativeFeatureElement)element).walk().equals(new Walk(0.f, 0.f))
//								||
//								((RelativeFeatureElement)element).walk().equals(new Walk(1.f/6, 1.f/3))
//								||
//								((RelativeFeatureElement)element).walk().equals(new Walk(0.f, 1.f/3))
//								||
//								((RelativeFeatureElement)element).walk().equals(new Walk(0.5f, 1.f/3))
//								||
//								((RelativeFeatureElement)element).walk().equals(new Walk(0.5f, -1.f/3))
//								||
//								((RelativeFeatureElement)element).walk().equals(new Walk(0.f, 1.f/3))
//								||
//								((RelativeFeatureElement)element).walk().equals(new Walk(1.f/6, 0.f))
//								||
//								((RelativeFeatureElement)element).walk().equals(new Walk(1.f/3, 0.f))
//								||
//								((RelativeFeatureElement)element).walk().equals(new Walk(0.5f, 0.f))
//								||
//								((RelativeFeatureElement)element).walk().equals(new Walk(1.f/3, 1.f/3))
//								||
//								((RelativeFeatureElement)element).walk().equals(new Walk(-1.f/3, 1.f/3))
//								||
//								((RelativeFeatureElement)element).walk().equals(new Walk(0.f, -1.f/3))
//								||
//								((RelativeFeatureElement)element).walk().equals(new Walk(1.f/3, -1.f/3))
//								||
//								((RelativeFeatureElement)element).walk().equals(new Walk(-1.f/6, 1.f/3))
//								||
//								((RelativeFeatureElement)element).walk().equals(new Walk(1.f/6, -1.f/3))
//							)
//							{
//								relevantConstituent = false;
//							}
//						}
//					}
//					if (relevantConstituent)
//					{
//						System.out.println("relevant constituent " + i + ": " + instanceI);
//						++numRelevantConstituents;
//					}

					if (observedCasePairs.add(combinedSelf))
					{
						featurePairActivations.adjustOrPutValue(combinedSelf, 1, 1);
						errorSums.adjustOrPutValue(combinedSelf, error, error);
						
//						if (relevantConstituent)
//							System.out.println("incremented for constituent " + i);
					}
//					else if (relevantConstituent)
//					{
//						System.out.println("Already observed for this action, so no increment for constituent " + i); 
//					}

					for (int j = i + 1; j < numActiveInstances; ++j)
					{
						final FeatureInstance instanceJ = instancesToKeep.get(j);

						// increment off-diagonal entries
						final CombinableFeatureInstancePair combined = 
								new CombinableFeatureInstancePair(game, instanceI, instanceJ);
						
//						boolean relevantCombined = false;
//						if (!combined.combinedFeature.isReactive() && combined.combinedFeature.pattern().featureElements().length == 2)
//						{
//							boolean onlyFriendElements = true;
//							for (final FeatureElement element : combined.combinedFeature.pattern().featureElements())
//							{
//								if 
//								(
//									element.not() 
//									|| 
//									element.type() != ElementType.Friend
//									||
//									((RelativeFeatureElement)element).walk().steps().size() != 2
//									||
//									((RelativeFeatureElement)element).walk().equals(new Walk(0.f, 0.f))
//									||
//									((RelativeFeatureElement)element).walk().equals(new Walk(1.f/6, 1.f/3))
//									||
//									((RelativeFeatureElement)element).walk().equals(new Walk(0.f, 1.f/3))
//									||
//									((RelativeFeatureElement)element).walk().equals(new Walk(0.5f, 1.f/3))
//									||
//									((RelativeFeatureElement)element).walk().equals(new Walk(0.5f, -1.f/3))
//									||
//									((RelativeFeatureElement)element).walk().equals(new Walk(0.f, 1.f/3))
//									||
//									((RelativeFeatureElement)element).walk().equals(new Walk(1.f/6, 0.f))
//									||
//									((RelativeFeatureElement)element).walk().equals(new Walk(1.f/3, 0.f))
//									||
//									((RelativeFeatureElement)element).walk().equals(new Walk(0.5f, 0.f))
//									||
//									((RelativeFeatureElement)element).walk().equals(new Walk(1.f/3, 1.f/3))
//									||
//									((RelativeFeatureElement)element).walk().equals(new Walk(-1.f/3, 1.f/3))
//									||
//									((RelativeFeatureElement)element).walk().equals(new Walk(0.f, -1.f/3))
//									||
//									((RelativeFeatureElement)element).walk().equals(new Walk(1.f/3, -1.f/3))
//									||
//									((RelativeFeatureElement)element).walk().equals(new Walk(-1.f/6, 1.f/3))
//									||
//									((RelativeFeatureElement)element).walk().equals(new Walk(1.f/6, -1.f/3))
//								)
//								{
//									onlyFriendElements = false;
//									break;
//								}
//							}
//							
//							if (onlyFriendElements)
//								relevantCombined = true;
//						}
						
//						if (relevantCombined)
//						{
//							System.out.println("relevant combined feature: " + combined.combinedFeature);
//							System.out.println("from constituent " + i + " = " + instanceI);
//							System.out.println("from constituent " + j + " = " + instanceJ);
//						}

						if (!existingFeatures.contains(combined.combinedFeature))
						{
							if (observedCasePairs.add(combined))
							{
								featurePairActivations.adjustOrPutValue(combined, 1, 1);
								errorSums.adjustOrPutValue(combined, error, error);
//								
//								if (relevantCombined)
//									System.out.println("incremented for combined");
							}
//							else if (relevantCombined)
//							{
//								System.out.println("didn't add combined because already observed for this action ");
//							}
						}
//						else if (relevantCombined)
//						{
//							System.out.println("didn't add combined because feature already exists");
//						}
					}
				}
				
//				if (numRelevantConstituents == 1)
//					System.out.println("origActiveInstances = " + origActiveInstances);
			}
		}

		if (sumErrors == 0.0 || sumSquaredErrors == 0.0)
		{
			// incredibly rare case but appears to be possible sometimes
			// we have nothing to guide our feature growing, so let's 
			// just refuse to add a feature
			return null;
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
		
		// Randomly pick a minimum required sample size in [3, 5]
		final int requiredSampleSize = 3 + ThreadLocalRandom.current().nextInt(3);

		for (final CombinableFeatureInstancePair pair : featurePairActivations.keySet())
		{
//			int numFriendElements = 0;
//			for (final FeatureElement element : ((RelativeFeature) pair.combinedFeature).pattern().featureElements())
//			{
//				if (element.type() == ElementType.Friend && !element.not())
//					++numFriendElements;
//			}
//			final boolean couldBeWinFeature = (numFriendElements >= 3);
//			final boolean couldBeLossFeature = (numFriendElements == 2);
			
			if (!pair.a.equals(pair.b))	// Only interested in combinations of different instances
			{
//				if (pair.combinedFeature.toString().equals("rel:to=<{}>:pat=<els=[f{-1/6,1/6}, f{0,-1/6}]>"))
//				{
//					final int pairActs = featurePairActivations.get(pair);
//					final int actsI = featurePairActivations.get(new CombinableFeatureInstancePair(game, pair.a, pair.a));
//					final int actsJ = featurePairActivations.get(new CombinableFeatureInstancePair(game, pair.b, pair.b));
//					
//					if (pairActs != actsI || pairActs != actsJ || actsI != actsJ)
//					{
//						System.out.println("pairActs = " + pairActs);
//						System.out.println("actsI = " + actsI);
//						System.out.println("actsJ = " + actsJ);
//						System.out.println("already contains = " + existingFeatures.contains(pair.combinedFeature));
//						System.out.println("pair = " + pair);
//						System.out.println("errorSumsI = " + errorSums.get(new CombinableFeatureInstancePair(game, pair.a, pair.a)));
//						System.out.println("errorSumsJ = " + errorSums.get(new CombinableFeatureInstancePair(game, pair.b, pair.b)));
//						for (final CombinableFeatureInstancePair key : featurePairActivations.keySet())
//						{
//							if (featurePairActivations.get(key) <= actsI && !key.combinedFeature.isReactive())
//							{
//								boolean onlyFriendElements = true;
//								for (final FeatureElement element : key.combinedFeature.pattern().featureElements())
//								{
//									if 
//									(
//										element.not() 
//										|| 
//										element.type() != ElementType.Friend
//										||
//										((RelativeFeatureElement)element).walk().steps().size() != 2
//										||
//										((RelativeFeatureElement)element).walk().equals(new Walk(0.f, 0.f))
//										||
//										((RelativeFeatureElement)element).walk().equals(new Walk(1.f/6, 1.f/3))
//										||
//										((RelativeFeatureElement)element).walk().equals(new Walk(0.f, 1.f/3))
//										||
//										((RelativeFeatureElement)element).walk().equals(new Walk(0.5f, 1.f/3))
//										||
//										((RelativeFeatureElement)element).walk().equals(new Walk(0.5f, -1.f/3))
//										||
//										((RelativeFeatureElement)element).walk().equals(new Walk(0.f, 1.f/3))
//										||
//										((RelativeFeatureElement)element).walk().equals(new Walk(1.f/6, 0.f))
//										||
//										((RelativeFeatureElement)element).walk().equals(new Walk(1.f/3, 0.f))
//										||
//										((RelativeFeatureElement)element).walk().equals(new Walk(0.5f, 0.f))
//										||
//										((RelativeFeatureElement)element).walk().equals(new Walk(1.f/3, 1.f/3))
//										||
//										((RelativeFeatureElement)element).walk().equals(new Walk(-1.f/3, 1.f/3))
//										||
//										((RelativeFeatureElement)element).walk().equals(new Walk(0.f, -1.f/3))
//										||
//										((RelativeFeatureElement)element).walk().equals(new Walk(1.f/3, -1.f/3))
//										||
//										((RelativeFeatureElement)element).walk().equals(new Walk(-1.f/6, 1.f/3))
//										||
//										((RelativeFeatureElement)element).walk().equals(new Walk(1.f/6, -1.f/3))
//									)
//									{
//										onlyFriendElements = false;
//										break;
//									}
//								}
//								
//								if (onlyFriendElements)
//									System.out.println("Num activations for " + key + " = " + featurePairActivations.get(key));
//							}
//						}
//						System.exit(0);
//					}
//				}
				
				final int pairActs = featurePairActivations.get(pair);
				if (pairActs == numCases || numCases < 4)
				{
					// Perfect correlation, so we should just skip this one
//					if (couldBeWinFeature || couldBeLossFeature)
//						System.out.println("Skipping because of correlation: " + pair);
					continue;
				}
				
				if (pairActs < requiredSampleSize)
				{
					// Need a bigger sample size
//					if (couldBeWinFeature || couldBeLossFeature)
//						System.out.println("Skipping because of sample size (" + pairActs + " < " + requiredSampleSize + "): " + pair);
					continue;	
				}

				final int actsI = featurePairActivations.get(new CombinableFeatureInstancePair(game, pair.a, pair.a));
				final int actsJ = featurePairActivations.get(new CombinableFeatureInstancePair(game, pair.b, pair.b));

				if (actsI == numCases || actsJ == numCases || pairActs == actsI || pairActs == actsJ)
				{
					// Perfect correlation, so we should just skip this one
//					if (couldBeWinFeature || couldBeLossFeature)
//						System.out.println("Skipping because of perfect correlation: " + pair);
					continue;
				}

				final double pairErrorSum = errorSums.get(pair);

				final double errorCorr = 
						(
							(numCases * pairErrorSum - pairActs * sumErrors) 
							/ 
							(
								Math.sqrt(pairActs * (numCases - pairActs)) * 
								Math.sqrt(numCases * sumSquaredErrors - sumErrors * sumErrors)
							)
						);
				
				// Fisher's r-to-z transformation
				final double errorCorrZ = 0.5 * Math.log((1.0 + errorCorr) / (1.0 - errorCorr));
				// Standard deviation of the z
				final double stdErrorCorrZ = Math.sqrt(1.0 / (numCases - 3));
				// Lower bound of 90% confidence interval on z
				final double lbErrorCorrZ = errorCorrZ - featureDiscoveryParams.criticalValueCorrConf * stdErrorCorrZ;
				// Transform lower bound on z back to r
				final double lbErrorCorr = (Math.exp(2.0 * lbErrorCorrZ) - 1.0) / (Math.exp(2.0 * lbErrorCorrZ) + 1.0);
				// Upper bound of 90% confidence interval on z
				final double ubErrorCorrZ = errorCorrZ + featureDiscoveryParams.criticalValueCorrConf * stdErrorCorrZ;
				// Transform upper bound on z back to r
				final double ubErrorCorr = (Math.exp(2.0 * ubErrorCorrZ) - 1.0) / (Math.exp(2.0 * ubErrorCorrZ) + 1.0);

				final double featureCorrI = 
						(
							(pairActs * (numCases - actsI)) 
							/ 
							(
								Math.sqrt(pairActs * (numCases - pairActs)) * 
								Math.sqrt(actsI * (numCases - actsI))
							)
						);

				final double featureCorrJ = 
						(
							(pairActs * (numCases - actsJ)) 
							/ 
							(
								Math.sqrt(pairActs * (numCases - pairActs)) * 
								Math.sqrt(actsJ * (numCases - actsJ))
							)
						);

				final double worstFeatureCorr = 
						Math.max
						(
							Math.abs(featureCorrI), 
							Math.abs(featureCorrJ)
						);

				final double score;
				if (errorCorr >= 0.0)
					score = Math.max(0.0, lbErrorCorr) * (1.0 - worstFeatureCorr*worstFeatureCorr);
				else
					score = -Math.min(0.0, ubErrorCorr) * (1.0 - worstFeatureCorr*worstFeatureCorr);

				if (Double.isNaN(score))
				{
					// System.err.println("numCases = " + numCases);
					// System.err.println("pairActs = " + pairActs);
					// System.err.println("actsI = " + actsI);
					// System.err.println("actsJ = " + actsJ);
					// System.err.println("sumErrors = " + sumErrors);
					// System.err.println("sumSquaredErrors = " + sumSquaredErrors);
//					if (couldBeWinFeature)
//						System.out.println("Skipping because of NaN score: " + pair);
					continue;
				}
				
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
//				else if (couldBeLossFeature)
//				{
//					System.out.println("Might be loss feature: " + pair);
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
				
				final CombinableFeatureInstancePair pair = 
						new CombinableFeatureInstancePair(game, bestPair.pair.a, bestPair.pair.b);
				
				final int pairActs = featurePairActivations.get(pair);
				final double pairErrorSum = errorSums.get(pair);

				final double errorCorr = 
						(
							(numCases * pairErrorSum - pairActs * sumErrors) 
							/ 
							(
								Math.sqrt(numCases * pairActs - pairActs * pairActs) * 
								Math.sqrt(numCases * sumSquaredErrors - sumErrors * sumErrors)
							)
						);
				
				// Fisher's r-to-z transformation
				final double errorCorrZ = 0.5 * Math.log((1.0 + errorCorr) / (1.0 - errorCorr));
				// Standard deviation of the z
				final double stdErrorCorrZ = Math.sqrt(1.0 / (numCases - 3));
				// Lower bound of 90% confidence interval on z
				final double lbErrorCorrZ = errorCorrZ - 1.64 * stdErrorCorrZ;
				// Transform lower bound on z back to r
				final double lbErrorCorr = (Math.exp(2.0 * lbErrorCorrZ) - 1.0) / (Math.exp(2.0 * lbErrorCorrZ) + 1.0);
				// Upper bound of 90% confidence interval on z
				final double ubErrorCorrZ = errorCorrZ + 1.64 * stdErrorCorrZ;
				// Transform upper bound on z back to r
				final double ubErrorCorr = (Math.exp(2.0 * ubErrorCorrZ) - 1.0) / (Math.exp(2.0 * ubErrorCorrZ) + 1.0);

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

				experiment.logLine(logWriter, "New proactive feature added!");
				experiment.logLine(logWriter, "new feature = " + newFeatureSet.spatialFeatures()[newFeatureSet.getNumSpatialFeatures() - 1]);
				experiment.logLine(logWriter, "active feature A = " + bestPair.pair.a.feature());
				experiment.logLine(logWriter, "rot A = " + bestPair.pair.a.rotation());
				experiment.logLine(logWriter, "ref A = " + bestPair.pair.a.reflection());
				experiment.logLine(logWriter, "anchor A = " + bestPair.pair.a.anchorSite());
				experiment.logLine(logWriter, "active feature B = " + bestPair.pair.b.feature());
				experiment.logLine(logWriter, "rot B = " + bestPair.pair.b.rotation());
				experiment.logLine(logWriter, "ref B = " + bestPair.pair.b.reflection());
				experiment.logLine(logWriter, "anchor B = " + bestPair.pair.b.anchorSite());
				experiment.logLine(logWriter, "avg error = " + sumErrors / numCases);
				experiment.logLine(logWriter, "avg error for pair = " + pairErrorSum / pairActs);
				experiment.logLine(logWriter, "score = " + bestPair.score);
				experiment.logLine(logWriter, "correlation with errors = " + errorCorr);
				experiment.logLine(logWriter, "lower bound correlation with errors = " + lbErrorCorr);
				experiment.logLine(logWriter, "upper bound correlation with errors = " + ubErrorCorr);
				experiment.logLine(logWriter, "correlation with first constituent = " + featureCorrI);
				experiment.logLine(logWriter, "correlation with second constituent = " + featureCorrJ);
				experiment.logLine(logWriter, "observed pair of instances " + pairActs + " times");
				experiment.logLine(logWriter, "observed first constituent " + actsI + " times");
				experiment.logLine(logWriter, "observed second constituent " + actsJ + " times");

//				System.out.println("BEST SCORE: " + bestPair.score);
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

				final CombinableFeatureInstancePair pair = 
						new CombinableFeatureInstancePair(game, bestPair.pair.a, bestPair.pair.b);
				
				final int pairActs = featurePairActivations.get(pair);
				final double pairErrorSum = errorSums.get(pair);

				final double errorCorr = 
						(
							(numCases * pairErrorSum - pairActs * sumErrors) 
							/ 
							(
								Math.sqrt(numCases * pairActs - pairActs * pairActs) * 
								Math.sqrt(numCases * sumSquaredErrors - sumErrors * sumErrors)
							)
						);
				
				// Fisher's r-to-z transformation
				final double errorCorrZ = 0.5 * Math.log((1.0 + errorCorr) / (1.0 - errorCorr));
				// Standard deviation of the z
				final double stdErrorCorrZ = Math.sqrt(1.0 / (numCases - 3));
				// Lower bound of 90% confidence interval on z
				final double lbErrorCorrZ = errorCorrZ - 1.64 * stdErrorCorrZ;
				// Transform lower bound on z back to r
				final double lbErrorCorr = (Math.exp(2.0 * lbErrorCorrZ) - 1.0) / (Math.exp(2.0 * lbErrorCorrZ) + 1.0);
				// Upper bound of 90% confidence interval on z
				final double ubErrorCorrZ = errorCorrZ + 1.64 * stdErrorCorrZ;
				// Transform upper bound on z back to r
				final double ubErrorCorr = (Math.exp(2.0 * ubErrorCorrZ) - 1.0) / (Math.exp(2.0 * ubErrorCorrZ) + 1.0);

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

				experiment.logLine(logWriter, "New reactive feature added!");
				experiment.logLine(logWriter, "new feature = " + newFeatureSet.spatialFeatures()[newFeatureSet.getNumSpatialFeatures() - 1]);
				experiment.logLine(logWriter, "active feature A = " + bestPair.pair.a.feature());
				experiment.logLine(logWriter, "rot A = " + bestPair.pair.a.rotation());
				experiment.logLine(logWriter, "ref A = " + bestPair.pair.a.reflection());
				experiment.logLine(logWriter, "anchor A = " + bestPair.pair.a.anchorSite());
				experiment.logLine(logWriter, "active feature B = " + bestPair.pair.b.feature());
				experiment.logLine(logWriter, "rot B = " + bestPair.pair.b.rotation());
				experiment.logLine(logWriter, "ref B = " + bestPair.pair.b.reflection());
				experiment.logLine(logWriter, "anchor B = " + bestPair.pair.b.anchorSite());
				experiment.logLine(logWriter, "avg error = " + sumErrors / numCases);
				experiment.logLine(logWriter, "avg error for pair = " + pairErrorSum / pairActs);
				experiment.logLine(logWriter, "score = " + bestPair.score);
				experiment.logLine(logWriter, "correlation with errors = " + errorCorr);
				experiment.logLine(logWriter, "lower bound correlation with errors = " + lbErrorCorr);
				experiment.logLine(logWriter, "upper bound correlation with errors = " + ubErrorCorr);
				experiment.logLine(logWriter, "correlation with first constituent = " + featureCorrI);
				experiment.logLine(logWriter, "correlation with second constituent = " + featureCorrJ);
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
