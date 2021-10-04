package training.policy_gradients;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import features.FeatureVector;
import features.feature_sets.BaseFeatureSet;
import game.Game;
import gnu.trove.list.array.TIntArrayList;
import main.collections.FVector;
import main.collections.FastArrayList;
import optimisers.Optimiser;
import other.RankUtils;
import other.context.Context;
import other.move.Move;
import other.state.State;
import other.trial.Trial;
import policies.softmax.SoftmaxPolicy;
import training.ExperienceSample;
import training.expert_iteration.feature_discovery.FeatureSetExpander;
import training.expert_iteration.params.FeatureDiscoveryParams;
import training.expert_iteration.params.ObjectiveParams;
import training.expert_iteration.params.TrainingParams;
import utils.ExponentialMovingAverage;

/**
 * Self-play feature (pre-)training and discovery with REINFORCE
 * 
 * @author Dennis Soemers
 */
public class Reinforce
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * Runs self-play with Policy Gradient training of features
	 * 
	 * @param game
	 * @param policy
	 * @param featureSets
	 * @param featureSetExpander
	 * @param optimisers
	 * @param objectiveParams
	 * @param featureDiscoveryParams
	 * @param trainingParams
	 * @param numEpochs
	 * @param numTrialsPerEpoch
	 * @param logWriter
	 */
	@SuppressWarnings("unchecked")
	public static void runSelfPlayPG
	(
		final Game game,
		final SoftmaxPolicy policy,
		final BaseFeatureSet[] featureSets,
		final FeatureSetExpander featureSetExpander,
		final Optimiser[] optimisers,
		final ObjectiveParams objectiveParams,
		final FeatureDiscoveryParams featureDiscoveryParams,
		final TrainingParams trainingParams,
		final int numEpochs,
		final int numTrialsPerEpoch,
		final PrintWriter logWriter
	)
	{
		final int numPlayers = game.players().count();
		final ExponentialMovingAverage[] avgGameDurations = new ExponentialMovingAverage[numPlayers + 1];
		for (int p = 1; p <= numPlayers; ++p)
		{
			avgGameDurations[p] = new ExponentialMovingAverage();
		}
		
		final Trial trial = new Trial(game);
		final Context context = new Context(game, trial);
		
		for (int epoch = 0; epoch < numEpochs; ++epoch)
		{
			// Collect all experience (per player) for this epoch here
			final List<PGExperience>[] epochExperiences = new List[numPlayers + 1];
			for (int p = 1; p <= numPlayers; ++p)
			{
				epochExperiences[p] = new ArrayList<PGExperience>();
			}
			
			for (int epochTrial = 0; epochTrial < numTrialsPerEpoch; ++epochTrial)
			{
				final List<State>[] encounteredGameStates = new List[numPlayers + 1];
				final List<Move>[] lastDecisionMoves = new List[numPlayers + 1];
				final List<FastArrayList<Move>>[] legalMovesLists = new List[numPlayers + 1];
				final List<FeatureVector[]>[] featureVectorArrays = new List[numPlayers + 1];
				final TIntArrayList[] playedMoveIndices = new TIntArrayList[numPlayers + 1];
				
				for (int p = 1; p <= numPlayers; ++p)
				{
					encounteredGameStates[p] = new ArrayList<State>();
					lastDecisionMoves[p] = new ArrayList<Move>();
					legalMovesLists[p] = new ArrayList<FastArrayList<Move>>();
					featureVectorArrays[p] = new ArrayList<FeatureVector[]>();
					playedMoveIndices[p] = new TIntArrayList();
				}
				
				// We can make a single SoftmaxPolicy object control all players at the same time as a
				// single AI object, but do still need to init and close it once per trial
				//
				// Since our object will play as all players at once, we pass -1 for the player ID
				// This is fine since SoftmaxPolicy doesn't actually care about that argument
				policy.initAI(game, -1);
				
				// TODO when adding parallelisation, will probably want a separate SoftmaxPolicy object per thread...
				
				// Play trial
				while (!trial.over())
				{
					final int mover = context.state().mover();
					final FastArrayList<Move> moves = game.moves(context).moves();
					final BaseFeatureSet featureSet = featureSets[mover];

					final FeatureVector[] featureVectors = featureSet.computeFeatureVectors(context, moves, false);
					final FVector distribution = policy.computeDistribution(featureVectors, mover);
					
					final int moveIdx = policy.selectActionFromDistribution(distribution);
					final Move move = moves.get(moveIdx);
					
					encounteredGameStates[mover].add(new Context(context).state());
					lastDecisionMoves[mover].add(context.trial().lastMove());
					legalMovesLists[mover].add(new FastArrayList<Move>(moves));
					featureVectorArrays[mover].add(featureVectors);
					playedMoveIndices[mover].add(moveIdx);
					
					game.apply(context, move);
				}
				
				final double[] utilities = RankUtils.agentUtilities(context);
				
				// Store all experiences
				for (int p = 1; p <= numPlayers; ++p)
				{
					final List<State> gameStatesList = encounteredGameStates[p];
					final List<Move> lastDecisionMovesList = lastDecisionMoves[p];
					final List<FastArrayList<Move>> legalMovesList = legalMovesLists[p];
					final List<FeatureVector[]> featureVectorsList = featureVectorArrays[p];
					final TIntArrayList moveIndicesList = playedMoveIndices[p];
					
					for (int i = 0; i < featureVectorsList.size(); ++i)
					{
						epochExperiences[p].add
						(
							new PGExperience
							(
								gameStatesList.get(i),
								lastDecisionMovesList.get(i),
								legalMovesList.get(i),
								featureVectorsList.get(i), 
								moveIndicesList.getQuick(i), 
								(float)utilities[p]
							)
						);
					}
				}
				
				policy.closeAI();
			}
			
			for (int p = 1; p <= numPlayers; ++p)
			{
				final List<PGExperience> experiences = epochExperiences[p];
				final int numExperiences = experiences.size();
				final FVector grads = new FVector(policy.linearFunction(p).trainableParams().allWeights().dim());
				
				for (int i = 0; i < numExperiences; ++i)
				{
					final PGExperience exp = experiences.get(i);
					final FVector policyGradients = computePolicyGradients(exp, grads.dim());
					
					// Now just need to divide gradients by the number of experiences we have and then we can
					// add them to the average gradients (averaged over all experiences)
					policyGradients.div(numExperiences);
					grads.add(policyGradients);
				}

				// Take gradient step
				optimisers[p].maximiseObjective(policy.linearFunction(p).trainableParams().allWeights(), grads);
			}

			// Now we want to try growing our feature set
//			if (!featureDiscoveryParams.noGrowFeatureSet)
//			{
//				final ExecutorService threadPool = Executors.newFixedThreadPool(featureDiscoveryParams.numFeatureDiscoveryThreads);
//				final CountDownLatch latch = new CountDownLatch(numPlayers);
//				for (int pIdx = 1; pIdx <= numPlayers; ++pIdx)
//				{
//					final int p = pIdx;
//					final BaseFeatureSet featureSetP = featureSets[p];
//					threadPool.submit
//					(
//						() ->
//						{
//							// We'll sample a batch from our replay buffer, and grow feature set
//							final int batchSize = trainingParams.batchSize;
//							final List<ExItExperience> batch = experienceBuffers[p].sampleExperienceBatch(batchSize);
//
//							if (batch.size() > 0)
//							{
//								final long startTime = System.currentTimeMillis();
//								final BaseFeatureSet expandedFeatureSet = 
//										featureSetExpander.expandFeatureSet
//										(
//											batch,
//											featureSetP,
//											cePolicy,
//											game,
//											featureDiscoveryParams.combiningFeatureInstanceThreshold,
//											null,
//											objectiveParams, 
//											featureDiscoveryParams,
//											logWriter,
//											this
//										);
//
//								if (expandedFeatureSet != null)
//								{
//									expandedFeatureSets[p] = expandedFeatureSet;
//									expandedFeatureSet.init(game, new int[]{p}, null);
//								}
//								else
//								{
//									expandedFeatureSets[p] = featureSetP;
//								}
//								
//								logLine
//								(
//									logWriter,
//									"Expanded feature set in " + (System.currentTimeMillis() - startTime) + " ms for P" + p + "."
//								);
//							}
//							else
//							{
//								expandedFeatureSets[p] = featureSetP;
//							}
//							
//							latch.countDown();
//						}
//					);
//							
//				}
//				
//				try
//				{
//					latch.await();
//				} 
//				catch (final InterruptedException e)
//				{
//					e.printStackTrace();
//				}
//				threadPool.shutdown();
//
//				cePolicy.updateFeatureSets(expandedFeatureSets);
//				
//				featureSets = expandedFeatureSets;
//			}
			// TODO above will be lots of duplication with code in ExpertIteration, should refactor
			
			// TODO save checkpoints
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param exp
	 * @param dim Dimensionality we want for output vector
	 * @return Computes vector of policy gradients for given sample of experience
	 */
	public static FVector computePolicyGradients(final PGExperience exp, final int dim)
	{
		// Policy gradient giving the direction in which we should update parameters theta
		// can be estimated as:
		//
		// AVERAGE OVER ALL EXPERIENCE SAMPLES i with returns G_i:
		//	\nabla_{\theta} \log ( \pi_{\theta} (a_i | s_i) ) * G_i
		//
		// Assuming that \pi_{\theta} (a_i | s_i) is given by a softmax over the logits of
		// all the actions legal in s_i, we have:
		//
		// \nabla_{\theta} \log ( \pi_{\theta} (a_i | s_i) ) = \phi(s_i, a_i) - E_{\pi_{\theta}} [\phi(s_i, \cdot)]
		
		final FeatureVector[] featureVectors = exp.featureVectors();
		
		final FVector expectedPhi = new FVector(dim);
		final FVector gradLogPi = new FVector(dim);
		
		for (int moveIdx = 0; moveIdx < featureVectors.length; ++moveIdx)
		{
			final FeatureVector featureVector = featureVectors[moveIdx];
			
			// Dense representation for aspatial features
			final FVector aspatialFeatureVals = featureVector.aspatialFeatureValues();
			final int numAspatialFeatures = aspatialFeatureVals.dim();
			
			for (int k = 0; k < numAspatialFeatures; ++k)
			{
				expectedPhi.addToEntry(k, aspatialFeatureVals.get(k));
			}
			
			if (moveIdx == exp.movePlayedIdx())
			{
				for (int k = 0; k < numAspatialFeatures; ++k)
				{
					gradLogPi.addToEntry(k, aspatialFeatureVals.get(k));
				}
			}
			
			// Sparse representation for spatial features (num aspatial features as offset for indexing)
			final TIntArrayList sparseSpatialFeatures = featureVector.activeSpatialFeatureIndices();
			
			for (int k = 0; k < sparseSpatialFeatures.size(); ++k)
			{
				final int feature = sparseSpatialFeatures.getQuick(k);
				expectedPhi.addToEntry(feature + numAspatialFeatures, 1.f);
			}
			
			if (moveIdx == exp.movePlayedIdx())
			{
				for (int k = 0; k < sparseSpatialFeatures.size(); ++k)
				{
					final int feature = sparseSpatialFeatures.getQuick(k);
					gradLogPi.addToEntry(feature + numAspatialFeatures, 1.f);
				}
			}
		}
		
		expectedPhi.div(featureVectors.length);
		gradLogPi.subtract(expectedPhi);

		// Now we have the gradients of the log-probability of the action we played
		// We want to weight these by the returns of the episode
		gradLogPi.mult(exp.returns());		// TODO subtract per-player expected score as baseline
		
		return gradLogPi;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Sample of experience for policy gradients
	 * 
	 * NOTE: since our experiences just collect feature vectors rather than contexts,
	 * we cannot reuse the same experiences after growing our feature sets
	 * 
	 * @author Dennis Soemers
	 */
	private static class PGExperience extends ExperienceSample
	{
		
		/** Game state */
		protected final State state;
		
		/** Last move (which transitioned us into the stored game state) */
		protected final Move lastDecisionMove;
		
		/** List of legal moves */
		protected final FastArrayList<Move> legalMoves;
		
		/** Array of feature vectors (one per legal move) */
		protected final FeatureVector[] featureVectors;
		
		/** Index of move that we ended up playing */
		protected final int movePlayedIdx;
		
		/** Returns we got at the end of the trial that this experience was a part of */
		protected final float returns;
		
		/**
		 * Constructor
		 * @param state
		 * @param lastDecisionMove
		 * @param legalMoves
		 * @param featureVectors
		 * @param movePlayedIdx
		 * @param returns
		 */
		public PGExperience
		(
			final State state,
			final Move lastDecisionMove,
			final FastArrayList<Move> legalMoves,
			final FeatureVector[] featureVectors, 
			final int movePlayedIdx, 
			final float returns
		)
		{
			this.state = state;
			this.lastDecisionMove = lastDecisionMove;
			this.legalMoves = legalMoves;
			this.featureVectors = featureVectors;
			this.movePlayedIdx = movePlayedIdx;
			this.returns = returns;
		}
		
		/**
		 * @return Array of feature vectors (one per legal move)
		 */
		public FeatureVector[] featureVectors()
		{
			return featureVectors;
		}
		
		/**
		 * @return The index of the move we played
		 */
		public int movePlayedIdx()
		{
			return movePlayedIdx;
		}
		
		/**
		 * @return The returns we got at end of trial that this experience was a part of
		 */
		public float returns()
		{
			return returns;
		}
		
		@Override
		public FeatureVector[] generateFeatureVectors(final BaseFeatureSet featureSet)
		{
			return featureVectors;
		}
		
		@Override
		public FVector expertDistribution()
		{
			// As an estimation of a good expert distribution, we'll use the
			// returns as logit for the played action, with logits of 0
			// everywhere else, and then use a softmax to turn it into
			// a distribution
			final FVector distribution = new FVector(featureVectors.length);
			distribution.set(movePlayedIdx, returns);
			distribution.softmax();
			return distribution;
		}
		
		@Override
		public State gameState()
		{
			return state;
		}
		
		@Override
		public Move lastDecisionMove()
		{
			return lastDecisionMove;
		}
		
		@Override
		public FastArrayList<Move> moves()
		{
			return legalMoves;
		}
		
	}
	
	//-------------------------------------------------------------------------

}
