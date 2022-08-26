package policies.softmax;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import features.Feature;
import features.FeatureVector;
import features.WeightVector;
import features.aspatial.AspatialFeature;
import features.feature_sets.BaseFeatureSet;
import features.feature_sets.network.JITSPatterNetFeatureSet;
import features.spatial.SpatialFeature;
import function_approx.BoostedLinearFunction;
import function_approx.LinearFunction;
import game.Game;
import game.rules.play.moves.Moves;
import game.types.play.RoleType;
import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.list.array.TIntArrayList;
import main.Constants;
import main.collections.FVector;
import main.collections.FastArrayList;
import metadata.ai.features.Features;
import other.context.Context;
import other.move.Move;
import other.playout.PlayoutMoveSelector;
import other.trial.Trial;
import playout_move_selectors.EpsilonGreedyWrapper;
import playout_move_selectors.FeaturesSoftmaxMoveSelector;
import search.mcts.MCTS;
import utils.ExperimentFileUtils;

/**
 * A policy which:
 * 	- Uses a linear function approximator to compute one logit per action.
 * 	- Uses softmax to compute a probability distribution from those logits.
 * 	- Selects actions according to the softmax distribution.
 * 
 * We extend the AI abstract class, which means this policy can also be used as 
 * a full agent (even though that's not the primary intended use).
 * 
 * Similarly, we implement the PlayoutStrategy interface, so the policy can also
 * be plugged into MCTS directly as a playout strategy.
 * 
 * @author Dennis Soemers
 */
public class SoftmaxPolicyLinear extends SoftmaxPolicy 
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
	
	//-------------------------------------------------------------------------
	
	/**
	 * Default constructor. Will initialise important parts to null and break 
	 * down if used directly. Should customise() it first!
	 */
	public SoftmaxPolicyLinear()
	{
		linearFunctions = null;
		featureSets = null;
	}
	
	/**
	 * Constructs a softmax policy with a linear function approximator
	 * @param linearFunctions
	 * @param featureSets
	 */
	public SoftmaxPolicyLinear
	(
		final LinearFunction[] linearFunctions, 
		final BaseFeatureSet[] featureSets
	)
	{
		this.linearFunctions = linearFunctions;
		this.featureSets = Arrays.copyOf(featureSets, featureSets.length);
	}
	
	/**
	 * Constructs a softmax policy with a linear function approximator,
	 * and a limit on the number of play-out actions to run with this policy
	 * plus a fallback Play-out strategy to use afterwards.
	 * 
	 * @param linearFunctions
	 * @param featureSets
	 * @param playoutActionLimit
	 */
	public SoftmaxPolicyLinear
	(
		final LinearFunction[] linearFunctions, 
		final BaseFeatureSet[] featureSets,
		final int playoutActionLimit
	)
	{
		this.linearFunctions = linearFunctions;
		this.featureSets = Arrays.copyOf(featureSets, featureSets.length);
		this.playoutActionLimit = playoutActionLimit;
	}
	
	/**
	 * Constructs a softmax policy from a given set of features as created
	 * by the compiler, using the Selection weights.
	 * 
	 * @param features
	 * @param epsilon Epsilon for epsilon-greedy playouts (should be irrelevant for Selection policy)
	 */
	public static SoftmaxPolicyLinear constructSelectionPolicy(final Features features, final double epsilon)
	{
		final SoftmaxPolicyLinear softmax = new SoftmaxPolicyLinear();
		
		final List<BaseFeatureSet> featureSetsList = new ArrayList<BaseFeatureSet>();
		final List<LinearFunction> linFuncs = new ArrayList<LinearFunction>();
				
		for (final metadata.ai.features.FeatureSet featureSet : features.featureSets())
		{
			if (featureSet.role() == RoleType.Shared || featureSet.role() == RoleType.Neutral)
				addFeatureSetWeights(0, featureSet.featureStrings(), featureSet.selectionWeights(), featureSetsList, linFuncs);
			else
				addFeatureSetWeights(featureSet.role().owner(), featureSet.featureStrings(), featureSet.selectionWeights(), featureSetsList, linFuncs);
		}
		
		softmax.featureSets = featureSetsList.toArray(new BaseFeatureSet[featureSetsList.size()]);
		softmax.linearFunctions = linFuncs.toArray(new LinearFunction[linFuncs.size()]);
		softmax.epsilon = epsilon;
		
		return softmax;
	}
	
	/**
	 * Constructs a softmax policy from a given set of features as created
	 * by the compiler, using the Playout weights.
	 * 
	 * @param features
	 * @param epsilon Epsilon for epsilon-greedy playouts
	 */
	public static SoftmaxPolicyLinear constructPlayoutPolicy(final Features features, final double epsilon)
	{
		final SoftmaxPolicyLinear softmax = new SoftmaxPolicyLinear();
		
		final List<BaseFeatureSet> featureSetsList = new ArrayList<BaseFeatureSet>();
		final List<LinearFunction> linFuncs = new ArrayList<LinearFunction>();
				
		for (final metadata.ai.features.FeatureSet featureSet : features.featureSets())
		{
			if (featureSet.role() == RoleType.Shared || featureSet.role() == RoleType.Neutral)
				addFeatureSetWeights(0, featureSet.featureStrings(), featureSet.playoutWeights(), featureSetsList, linFuncs);
			else
				addFeatureSetWeights(featureSet.role().owner(), featureSet.featureStrings(), featureSet.playoutWeights(), featureSetsList, linFuncs);
		}
		
		softmax.featureSets = featureSetsList.toArray(new BaseFeatureSet[featureSetsList.size()]);
		softmax.linearFunctions = linFuncs.toArray(new LinearFunction[linFuncs.size()]);
		softmax.epsilon = epsilon;
		
		return softmax;
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
			featureSet = featureSets[0];
		else
			featureSet = featureSets[context.state().mover()];
		
		return computeDistribution(featureSet.computeFeatureVectors(context, actions, thresholded), context.state().mover());
	}
	
	@Override
	public float computeLogit(final Context context, final Move move)
	{
		final BaseFeatureSet featureSet;
		
		if (featureSets.length == 1)
			featureSet = featureSets[0];
		else
			featureSet = featureSets[context.state().mover()];
		
		final LinearFunction linearFunction;
		
		if (linearFunctions.length == 1)
			linearFunction = linearFunctions[0];
		else
			linearFunction = linearFunctions[context.state().mover()];
		
		return linearFunction.predict(featureSet.computeFeatureVector(context, move, true));
	}
	
	/**
	 * @param featureVectors
	 * @param player
	 * @return Probability distribution over actions implied by a list of sparse 
	 * feature vectors
	 */
	public FVector computeDistribution
	(
		final FeatureVector[] featureVectors,
		final int player
	)
	{
		final float[] logits = new float[featureVectors.length];
		final LinearFunction linearFunction;
		
		if (linearFunctions.length == 1)
			linearFunction = linearFunctions[0];
		else
			linearFunction = linearFunctions[player];
		
		for (int i = 0; i < featureVectors.length; ++i)
		{
			logits[i] = linearFunction.predict(featureVectors[i]);
		}
		
		final FVector distribution = FVector.wrap(logits);
		distribution.softmax();
		
		return distribution;
	}
	
	/**
	 * @param errors Vector of errors in distributions
	 * @param featureVectors One feature vector for every element 
	 * (action) in the distributions.
	 * @param player The player whose parameters we want to compute gradients of
	 * @return Vector of gradients of the loss function (assumed to be 
	 * cross-entropy loss) with respect to our linear function's vector of 
	 * parameters.
	 */
	public FVector computeParamGradients
	(
		final FVector errors,
		final FeatureVector[] featureVectors,
		final int player
	)
	{
		final LinearFunction linearFunction;
		
		if (linearFunctions.length == 1)
			linearFunction = linearFunctions[0];
		else
			linearFunction = linearFunctions[player];
		
		// now compute gradients w.r.t. parameters
		final FVector grads = new FVector(linearFunction.trainableParams().allWeights().dim());
		final int numActions = errors.dim();
		
		for (int i = 0; i < numActions; ++i)
		{
			// error for this action
			final float error = errors.get(i);
			
			// feature vector for this action
			final FeatureVector featureVector = featureVectors[i];
			
			// Feature values for aspatial features (dense representation); gradients for these first
			final FVector aspatialFeatureValues = featureVector.aspatialFeatureValues();
			final int numAspatialFeatures = aspatialFeatureValues.dim();
			
			for (int j = 0; j < numAspatialFeatures; ++j)
			{
				grads.addToEntry(j, error * featureVector.aspatialFeatureValues().get(j));
			}
			
			// Saprse representation of active spatial features; gradients for these second,
			// with offset for indexing based on number of aspatial features
			final TIntArrayList activeSpatialFeatureIndices = featureVector.activeSpatialFeatureIndices();
			
			for (int j = 0; j < activeSpatialFeatureIndices.size(); ++j)
			{
				final int featureIdx = activeSpatialFeatureIndices.getQuick(j);
				grads.addToEntry(featureIdx + numAspatialFeatures, error);
			}
		}
		
		//System.out.println("est. distr. = " + estimatedDistribution);
		//System.out.println("tar. distr. = " + targetDistribution);
		//System.out.println("errors      = " + errors);
		//System.out.println("grads = " + grads);
		
		return grads;
	}
	
	/**
	 * @param distribution
	 * @return Samples an action index from a previously-computed distribution
	 */
	@SuppressWarnings("static-method")
	public int selectActionFromDistribution(final FVector distribution)
	{
		return distribution.sampleFromDistribution();
	}
	
	/**
	 * Updates this policy to use a new array of Feature Sets.
	 * For now, this method assumes that Feature Sets can only grow, and that newly-added
	 * features are always new spatial features.
	 * 
	 * @param newFeatureSets
	 */
	public void updateFeatureSets(final BaseFeatureSet[] newFeatureSets)
	{
		for (int i = 0; i < linearFunctions.length; ++i)
		{
			if (newFeatureSets[i] != null)
			{
				final int numExtraFeatures = newFeatureSets[i].getNumSpatialFeatures() - featureSets[i].getNumSpatialFeatures();

				for (int j = 0; j < numExtraFeatures; ++j)
				{
					linearFunctions[i].setTheta(new WeightVector(linearFunctions[i].trainableParams().allWeights().append(0.f)));
				}
				
				featureSets[i] = newFeatureSets[i];
			}
			else if (newFeatureSets[0] != null)
			{
				// Handle the case where all players have different functions but share the 0th feature set
				final int numExtraFeatures = newFeatureSets[0].getNumSpatialFeatures() - featureSets[0].getNumSpatialFeatures();
				
				for (int j = 0; j < numExtraFeatures; ++j)
				{
					linearFunctions[i].setTheta(new WeightVector(linearFunctions[i].trainableParams().allWeights().append(0.f)));
				}
			}
		}
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
		
		final PlayoutMoveSelector playoutMoveSelector;
		if (epsilon < 1.0)
		{
			if (epsilon <= 0.0)
				playoutMoveSelector = new FeaturesSoftmaxMoveSelector(featureSets, params, true);
			else
				playoutMoveSelector = new EpsilonGreedyWrapper(new FeaturesSoftmaxMoveSelector(featureSets, params, true), epsilon);
		}
		else
		{
			playoutMoveSelector = null;
		}
		
		return context.game().playout
				(
					context, 
					null, 
					1.0, 
					playoutMoveSelector,
					playoutActionLimit,
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
			else if (input.toLowerCase().startsWith("playoutactionlimit="))
			{
				playoutActionLimit = 
						Integer.parseInt(input.substring(
								"playoutactionlimit=".length()));
			}
			else if (input.toLowerCase().startsWith("playoutturnlimit="))
			{
				playoutTurnLimit = 
						Integer.parseInt
						(
							input.substring("playoutturnlimit=".length())
						);
			}
			else if (input.toLowerCase().startsWith("friendly_name="))
			{
				friendlyName = input.substring("friendly_name=".length());
			}
			else if (input.toLowerCase().startsWith("boosted="))
			{
				if (input.toLowerCase().endsWith("true"))
				{
					boosted = true;
				}
			}
			else if (input.toLowerCase().startsWith("epsilon="))
			{
				epsilon = Double.parseDouble(input.substring("epsilon=".length()));
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
			System.err.println("Cannot construct linear Softmax Policy from: " + Arrays.toString(inputs));
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

		return actions.moves().get
				(
					selectActionFromDistribution
					(
						computeDistribution
						(
							featureSet.computeFeatureVectors
							(
								context, 
								actions.moves(), 
								true
							), 
							context.state().mover()
						)
					)
				);
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
	
	@Override
	public void closeAI()
	{
		if (featureSets == null)
			return;
		
		if (featureSets.length == 1)
		{
			featureSets[0].closeCache();
		}
		else
		{
			for (int i = 1; i < featureSets.length; ++i)
			{
				featureSets[i].closeCache();
			}
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param player
	 * @return Linear function corresponding to given player
	 */
	public LinearFunction linearFunction(final int player)
	{
		if (linearFunctions.length == 1)
			return linearFunctions[0];
		else
			return linearFunctions[player];
	}
	
	/**
	 * @return The linear functions used to compute logits
	 */
	public LinearFunction[] linearFunctions()
	{
		return linearFunctions;
	}
	
	/**
	 * @return Feature Sets used by this policy
	 */
	public BaseFeatureSet[] featureSets()
	{
		return featureSets;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return A metadata Features item describing the features + weights for this policy
	 */
//	public metadata.ai.features.Features generateFeaturesMetadata()
//	{
//		final Features features;
//		
//		if (featureSets.length == 1)
//		{
//			// Just a single featureset for all players
//			final BaseFeatureSet featureSet = featureSets[0];
//			final LinearFunction linFunc = linearFunctions[0];
//			final Pair[] pairs = new Pair[featureSet.spatialFeatures().length];
//			
//			for (int i = 0; i < pairs.length; ++i)
//			{
//				final float weight = linFunc.effectiveParams().allWeights().get(i);
//				pairs[i] = new Pair(featureSet.spatialFeatures()[i].toString(), Float.valueOf(weight));
//				
//				if (Float.isNaN(weight))
//					System.err.println("WARNING: writing NaN weight");
//				else if (Float.isInfinite(weight))
//					System.err.println("WARNING: writing infinity weight");
//			}
//			
//			features = new Features(new metadata.ai.features.FeatureSet(RoleType.Shared, pairs));
//		}
//		else
//		{
//			// One featureset per player
//			final metadata.ai.features.FeatureSet[] metadataFeatureSets = new metadata.ai.features.FeatureSet[featureSets.length - 1];
//			
//			for (int p = 0; p < featureSets.length; ++p)
//			{
//				final BaseFeatureSet featureSet = featureSets[p];
//				if (featureSet == null)
//					continue;
//				
//				final LinearFunction linFunc = linearFunctions[p];
//				final Pair[] pairs = new Pair[featureSet.spatialFeatures().length];
//				
//				for (int i = 0; i < pairs.length; ++i)
//				{
//					final float weight = linFunc.effectiveParams().allWeights().get(i);
//					pairs[i] = new Pair(featureSet.spatialFeatures()[i].toString(), Float.valueOf(weight));
//					
//					if (Float.isNaN(weight))
//						System.err.println("WARNING: writing NaN weight");
//					else if (Float.isInfinite(weight))
//						System.err.println("WARNING: writing infinity weight");
//				}
//				
//				metadataFeatureSets[p - 1] = new metadata.ai.features.FeatureSet(RoleType.roleForPlayerId(p), pairs);
//			}
//			
//			features = new Features(metadataFeatureSets);
//		}
//		
//		return features;
//	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param lines
	 * @return A softmax policy constructed from a given array of input lines
	 */
	public static SoftmaxPolicy fromLines(final String[] lines)
	{
		SoftmaxPolicy policy = null;
		
		for (final String line : lines)
		{
			if (line.equalsIgnoreCase("features=from_metadata"))
			{
				policy = new SoftmaxFromMetadataSelection(0.0);
				break;
			}
		}
		
		if (policy == null)
			policy = new SoftmaxPolicyLinear();

		policy.customise(lines);
		return policy;
	}
	
	/**
	 * @param weightsFile
	 * @return A Softmax policy constructed from a given file
	 */
	public static SoftmaxPolicyLinear fromFile(final File weightsFile)
	{
		final SoftmaxPolicyLinear policy = new SoftmaxPolicyLinear();
		boolean boosted = false;
		
		try (final BufferedReader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream(weightsFile.getAbsolutePath()), "UTF-8")))
		{
			String line = reader.readLine();
			String lastLine = null;
			
			while (line != null)
			{
				lastLine = line;
				line = reader.readLine();
			}
			
			if (!lastLine.startsWith("FeatureSet="))
			{
				boosted = true;
			}
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}
		
		policy.customise(new String[]{
				"softmax",
				"policyweights=" + weightsFile.getAbsolutePath(),
				"boosted=" + boosted
		});
		return policy;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Helper method that adds a Feature Set and a Linear Function for the 
	 * given player index
	 * 
	 * @param playerIdx
	 * @param featureStrings
	 * @param featureWeights
	 * @param outFeatureSets
	 * @param outLinFuncs
	 */
	protected static void addFeatureSetWeights
	(
		final int playerIdx, 
		final String[] featureStrings,
		final float[] featureWeights,
		final List<BaseFeatureSet> outFeatureSets, 
		final List<LinearFunction> outLinFuncs
	)
	{
		while (outFeatureSets.size() <= playerIdx)
		{
			outFeatureSets.add(null);
		}
		
		while (outLinFuncs.size() <= playerIdx)
		{
			outLinFuncs.add(null);
		}
		
		final List<AspatialFeature> aspatialFeatures = new ArrayList<AspatialFeature>();
		final List<SpatialFeature> spatialFeatures = new ArrayList<SpatialFeature>();
		final TFloatArrayList weights = new TFloatArrayList();
		
		for (int i = 0; i < featureStrings.length; ++i)
		{
			final Feature feature = Feature.fromString(featureStrings[i]);
			
			if (feature instanceof AspatialFeature)
				aspatialFeatures.add((AspatialFeature)feature);
			else
				spatialFeatures.add((SpatialFeature)feature);
			
			weights.add(featureWeights[i]);
		}
		
		outFeatureSets.set(playerIdx, JITSPatterNetFeatureSet.construct(aspatialFeatures, spatialFeatures));
		outLinFuncs.set(playerIdx, new LinearFunction(new WeightVector(new FVector(weights.toArray()))));
	}

	//-------------------------------------------------------------------------
	
}
