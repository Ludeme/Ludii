package policies;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import features.FeatureVector;
import features.WeightVector;
import features.feature_sets.BaseFeatureSet;
import features.feature_sets.network.JITSPatterNetFeatureSet;
import function_approx.BoostedLinearFunction;
import function_approx.LinearFunction;
import game.Game;
import game.rules.play.moves.Moves;
import gnu.trove.list.array.TIntArrayList;
import main.Constants;
import main.collections.FVector;
import main.collections.FastArrayList;
import other.context.Context;
import other.move.Move;
import other.trial.Trial;
import playout_move_selectors.FeaturesSoftmaxMoveSelector;
import search.mcts.MCTS;
import utils.ExperimentFileUtils;

/**
 * A greedy policy (plays greedily according to estimates by a linear function
 * approximator).
 * 
 * @author Dennis Soemers and cambolbro
 */
public class GreedyPolicy extends Policy 
{
	
	//-------------------------------------------------------------------------
	
	/** 
	 * Linear function approximators (can output one logit per action) 
	 * 
	 * If it contains only one function, it will be shared across all
	 * players. Otherwise, it will contain one function per player.
	 */
	protected LinearFunction[] linearFunctions;
	
	/** 
	 * Feature Sets to use to generate feature vectors for state+action pairs.
	 * 
	 * If it contains only one feature set, it will be shared across all
	 * players. Otherwise, it will contain one Feature Set per player.
	 */
	protected BaseFeatureSet[] featureSets;
	
	/** Auto-end playouts in a draw if they take more turns than this */
	protected int playoutTurnLimit = 200;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Default constructor. Will initialize important parts to null and break 
	 * down if used directly. Should customize() it first!
	 */
	public GreedyPolicy()
	{
		linearFunctions = null;
		featureSets = null;
	}
	
	/**
	 * Constructs a greedy policy with linear function approximators
	 * @param linearFunctions
	 * @param featureSets
	 */
	public GreedyPolicy
	(
		final LinearFunction[] linearFunctions, 
		final BaseFeatureSet[] featureSets
	)
	{
		this.linearFunctions = linearFunctions;
		this.featureSets = featureSets;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public FVector computeDistribution
	(
		final Context context, 
		final FastArrayList<Move> actions,
		final boolean thresholded
	)
	{
		final BaseFeatureSet featureSet;
		
		if (featureSets.length == 1)
		{
			featureSet = featureSets[0];
		}
		else
		{
			featureSet = featureSets[context.state().mover()];
		}
		
		return computeDistribution
				(
					featureSet.computeFeatureVectors(context, actions, thresholded),
					context.state().mover()
				);
	}
	
	/**
	 * @param featureVectors
	 * @param player
	 * @return Logits for the actions implied by a list of feature vectors.
	 */
	public float[] computeLogits
	(
		final FeatureVector[] featureVectors,
		final int player
	)
	{
		final float[] logits = new float[featureVectors.length];
		final LinearFunction linearFunction;
		
		if (linearFunctions.length == 1)
		{
			linearFunction = linearFunctions[0];
		}
		else
		{
			linearFunction = linearFunctions[player];
		}
		
		for (int i = 0; i < featureVectors.length; ++i)
		{
			logits[i] = linearFunction.predict(featureVectors[i]);
		}
		
		return logits;
	}
	
	@Override
	public float computeLogit(final Context context, final Move move)
	{
		final LinearFunction linearFunction;
		
		if (linearFunctions.length == 1)
			linearFunction = linearFunctions[0];
		else
			linearFunction = linearFunctions[context.state().mover()];
		
		final BaseFeatureSet featureSet;
		
		if (featureSets.length == 1)
			featureSet = featureSets[0];
		else
			featureSet = featureSets[context.state().mover()];
		
		return linearFunction.predict(featureSet.computeFeatureVector(context, move, true));
	}
	
	/**
	 * @param featureVectors One feature vector per action
	 * @param player Player for which to use features
	 * 
	 * @return Probability distribution over actions implied by a list of sparse 
	 * feature vectors
	 */
	public FVector computeDistribution
	(
		final FeatureVector[] featureVectors,
		final int player
	)
	{
		final float[] logits = computeLogits(featureVectors, player);
		
		float maxLogit = Float.NEGATIVE_INFINITY;
		final TIntArrayList maxLogitIndices = new TIntArrayList();
		
		for (int i = 0; i < logits.length; ++i)
		{
			final float logit = logits[i];
			
			if (logit > maxLogit)
			{
				maxLogit = logit;
				maxLogitIndices.reset();
				maxLogitIndices.add(i);
			}
			else if (logit == maxLogit)
			{
				maxLogitIndices.add(i);
			}
		}
		
		// this is the probability we assign to all max logits
		final float maxProb = 1.f / maxLogitIndices.size();
		
		// now create the distribution
		final FVector distribution = new FVector(logits.length);
		for (int i = 0; i < maxLogitIndices.size(); ++i)
		{
			distribution.set(maxLogitIndices.getQuick(i), maxProb);
		}

		return distribution;
	}

	//-------------------------------------------------------------------------
	
	@Override
	public Trial runPlayout(final MCTS mcts, final Context context) 
	{
		final WeightVector[] params = new WeightVector[linearFunctions.length];
		for (int i = 0; i < linearFunctions.length; ++i)
		{
			if (linearFunctions[i] == null)
			{
				params[i] = null;
			}
			else
			{
				params[i] = linearFunctions[i].effectiveParams();
			}
		}
		
		return context.game().playout
				(
					context, 
					null, 
					1.0, 
					new FeaturesSoftmaxMoveSelector(featureSets, params, true), 
					-1,
					playoutTurnLimit,
					ThreadLocalRandom.current()
				);
	}
	
	@Override
	public boolean playoutSupportsGame(final Game game)
	{
		return supportsGame(game);
	}
	
	@Override
	public int backpropFlags()
	{
		return 0;
	}

	@Override
	public void customise(final String[] inputs) 
	{
		final List<String> policyWeightsFilepaths = new ArrayList<String>();
		boolean boosted = false;
		
		for (int i = 1; i < inputs.length; ++i)
		{
			final String input = inputs[i];
			
			if (input.toLowerCase().startsWith("policyweights="))
			{
				if (policyWeightsFilepaths.size() > 0)
					policyWeightsFilepaths.clear();
				
				policyWeightsFilepaths.add(input.substring("policyweights=".length()));
			}
			else if (input.toLowerCase().startsWith("policyweights"))
			{
				for (int p = 1; p <= Constants.MAX_PLAYERS; ++p)
				{
					if (input.toLowerCase().startsWith("policyweights" + p + "="))
					{
						while (policyWeightsFilepaths.size() <= p)
						{
							policyWeightsFilepaths.add(null);
						}
						
						if (p < 10)
							policyWeightsFilepaths.set(p, input.substring("policyweightsX=".length()));
						else		// Doubt we'll ever have more than 99 players
							policyWeightsFilepaths.set(p, input.substring("policyweightsXX=".length()));
					}
				}
			}
			else if (input.toLowerCase().startsWith("playoutturnlimit="))
			{
				playoutTurnLimit = 
						Integer.parseInt(
								input.substring("playoutturnlimit=".length()));
			}
			else if (input.toLowerCase().startsWith("friendly_name="))
			{
				friendlyName = 
						input.substring("friendly_name=".length());
			}
			else if (input.toLowerCase().startsWith("boosted="))
			{
				if (input.toLowerCase().endsWith("true"))
				{
					boosted = true;
				}
			}
		}
		
		if (!policyWeightsFilepaths.isEmpty())
		{
			this.linearFunctions = new LinearFunction[policyWeightsFilepaths.size()];
			this.featureSets = new BaseFeatureSet[linearFunctions.length];
			
			for (int i = 0; i < policyWeightsFilepaths.size(); ++i)
			{
				String policyWeightsFilepath = policyWeightsFilepaths.get(i);
				
				if (policyWeightsFilepath != null)
				{
					final String parentDir = new File(policyWeightsFilepath).getParent();
					
					if (!new File(policyWeightsFilepath).exists())
					{
						// Replace with whatever is the latest file we have
						if (policyWeightsFilepath.contains("Selection"))
						{
							policyWeightsFilepath = 
								ExperimentFileUtils.getLastFilepath(parentDir + "/PolicyWeightsSelection_P" + i, "txt");
						}
						else if (policyWeightsFilepath.contains("Playout"))
						{
							policyWeightsFilepath = 
								ExperimentFileUtils.getLastFilepath(parentDir + "/PolicyWeightsPlayout_P" + i, "txt");
						}
						else if (policyWeightsFilepath.contains("TSPG"))
						{
							policyWeightsFilepath = 
								ExperimentFileUtils.getLastFilepath(parentDir + "/PolicyWeightsTSPG_P" + i, "txt");
						}
						else
						{
							policyWeightsFilepath = null;
						}
					}
					
					if (boosted)
						linearFunctions[i] = BoostedLinearFunction.boostedFromFile(policyWeightsFilepath, null);
					else
						linearFunctions[i] = LinearFunction.fromFile(policyWeightsFilepath);
					
					featureSets[i] = JITSPatterNetFeatureSet.construct(parentDir + File.separator + linearFunctions[i].featureSetFile());
				}
			}
		}
		else
		{
			System.err.println("Cannot construct Greedy Policy from: " + Arrays.toString(inputs));
		}
	}
	
	//-------------------------------------------------------------------------

	@Override
	public Move selectAction
	(
		final Game game, 
		final Context context,
		final double maxSeconds,
		final int maxIterations,
		final int maxDepth
	) 
	{
		final Moves actions = game.moves(context);
		final BaseFeatureSet featureSet;
		
		if (featureSets.length == 1)
		{
			featureSet = featureSets[0];
		}
		else
		{
			featureSet = featureSets[context.state().mover()];
		}
		
		return actions.moves().get(FVector.wrap
				(
					computeLogits
					(
						featureSet.computeFeatureVectors
						(
							context, 
							actions.moves(),
							true
						),
						context.state().mover()
					)
				).argMaxRand());
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param lines
	 * @return A greedy policy constructed from a given array of input lines
	 */
	public static GreedyPolicy fromLines(final String[] lines)
	{
		final GreedyPolicy policy = new GreedyPolicy();
		policy.customise(lines);
		return policy;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public void initAI(final Game game, final int playerID)
	{
		if (featureSets.length == 1)
		{
			final int[] supportedPlayers = new int[game.players().count()];
			for (int i = 0; i < supportedPlayers.length; ++i)
			{
				supportedPlayers[i] = i + 1;
			}
			
			featureSets[0].init(game, supportedPlayers, linearFunctions[0].effectiveParams());
		}
		else
		{
			for (int i = 1; i < featureSets.length; ++i)
			{
				featureSets[i].init(game, new int[] {i}, linearFunctions[i].effectiveParams());
			}
		}
	}
	
	//-------------------------------------------------------------------------

}
