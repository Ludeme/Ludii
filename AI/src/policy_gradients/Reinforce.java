package policy_gradients;

import java.util.ArrayList;
import java.util.List;

import expert_iteration.feature_discovery.FeatureSetExpander;
import expert_iteration.params.FeatureDiscoveryParams;
import expert_iteration.params.ObjectiveParams;
import features.FeatureVector;
import features.feature_sets.BaseFeatureSet;
import game.Game;
import main.collections.FVector;
import main.collections.FastArrayList;
import optimisers.Optimiser;
import other.RankUtils;
import other.context.Context;
import other.move.Move;
import other.trial.Trial;
import policies.softmax.SoftmaxPolicy;
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
	 * @param numEpochs
	 * @param numTrialsPerEpoch
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
		final int numEpochs,
		final int numTrialsPerEpoch
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
				final List<Context>[] encounteredStates = new List[numPlayers + 1];
				final List<Move>[] playedMoves = new List[numPlayers + 1];
				
				for (int p = 1; p <= numPlayers; ++p)
				{
					encounteredStates[p] = new ArrayList<Context>();
					playedMoves[p] = new ArrayList<Move>();
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
					final Move move = policy.selectAction(game, context);
					
					encounteredStates[mover].add(new Context(context));
					playedMoves[mover].add(move);
					
					game.apply(context, move);
				}
				
				final double[] utilities = RankUtils.agentUtilities(context);
				
				// Store all experiences
				for (int p = 1; p <= numPlayers; ++p)
				{
					final List<Context> contextsList = encounteredStates[p];
					final List<Move> movesList = playedMoves[p];
					
					for (int i = 0; i < contextsList.size(); ++i)
					{
						epochExperiences[p].add(new PGExperience(contextsList.get(i), movesList.get(i), utilities[p]));
					}
				}
				
				policy.closeAI();
			}
			
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
			
			for (int p = 1; p <= numPlayers; ++p)
			{
				final List<PGExperience> experiences = epochExperiences[p];
				final int numExperiences = experiences.size();
				final FVector grads = new FVector(policy.linearFunction(p).trainableParams().allWeights().dim());
				
				for (int i = 0; i < numExperiences; ++i)
				{
					final PGExperience exp = experiences.get(i);
					final FastArrayList<Move> moves = game.moves(exp.context()).moves();
					
					final FeatureVector[] featureVectors = 
							featureSets[p].computeFeatureVectors
							(
								exp.context().state(),
								exp.context().trial().lastMove(),
								moves, 
								false
							);
					
					for (int moveIdx = 0; moveIdx < moves.size(); ++moveIdx)
					{
						// Dense representation for aspatial features
//						final FVector aspatialFeatureVals = featureVector.aspatialFeatureValues();
//						final int numAspatialFeatures = aspatialFeatureVals.dim();
//						
//						for (int k = 0; k < numAspatialFeatures; ++k)
//						{
//							if (i == j)
//								grads.addToEntry(k, aspatialFeatureVals.get(k) * expertQ * pi_sa * (1.f - pi_sa));
//							else
//								grads.addToEntry(k, aspatialFeatureVals.get(k) * expertQ * pi_sa * (0.f - pi.get(j)));
//						}
						
						// Sparse representation for spatial features (num aspatial features as offset for indexing)
//						final TIntArrayList sparseSpatialFeatures = featureVector.activeSpatialFeatureIndices();
//						
//						for (int k = 0; k < sparseSpatialFeatures.size(); ++k)
//						{
//							final int feature = sparseSpatialFeatures.getQuick(k);
//							
//							if (i == j)
//								grads.addToEntry(feature + numAspatialFeatures, expertQ * pi_sa * (1.f - pi_sa));
//							else
//								grads.addToEntry(feature + numAspatialFeatures, expertQ * pi_sa * (0.f - pi.get(j)));
//						}
					}
				}
			}
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Sample of experience for policy gradients
	 * 
	 * @author Dennis Soemers
	 */
	private static class PGExperience
	{
		
		/** Copy of the context we encountered */
		protected final Context context;
		
		/** Move that we chose to play in context */
		protected final Move movePlayed;
		
		/** Returns we got at the end of the trial that this experience was a part of */
		protected final double returns;
		
		/**
		 * Constructor
		 * @param context
		 * @param movePlayed
		 * @param returns
		 */
		public PGExperience(final Context context, final Move movePlayed, final double returns)
		{
			this.context = context;
			this.movePlayed = movePlayed;
			this.returns = returns;
		}
		
		/**
		 * @return Our context
		 */
		public Context context()
		{
			return context;
		}
		
		/**
		 * @return The move we played
		 */
		public Move movePlayed()
		{
			return movePlayed;
		}
		
		/**
		 * @return The returns we got at end of trial that this experience was a part of
		 */
		public double returns()
		{
			return returns;
		}
		
	}
	
	//-------------------------------------------------------------------------

}
