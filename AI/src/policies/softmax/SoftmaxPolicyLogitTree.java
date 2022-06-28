package policies.softmax;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import decision_trees.logits.LogitTreeNode;
import features.Feature;
import features.FeatureVector;
import features.aspatial.AspatialFeature;
import features.feature_sets.BaseFeatureSet;
import features.feature_sets.network.JITSPatterNetFeatureSet;
import features.spatial.SpatialFeature;
import game.Game;
import game.rules.play.moves.Moves;
import game.types.play.RoleType;
import main.FileHandling;
import main.collections.FVector;
import main.collections.FastArrayList;
import main.grammar.Report;
import metadata.ai.features.trees.FeatureTrees;
import metadata.ai.features.trees.logits.LogitNode;
import metadata.ai.features.trees.logits.LogitTree;
import other.context.Context;
import other.move.Move;
import other.playout.PlayoutMoveSelector;
import other.trial.Trial;
import playout_move_selectors.EpsilonGreedyWrapper;
import playout_move_selectors.LogitTreeMoveSelector;
import search.mcts.MCTS;

/**
 * A policy that uses a Logit (Regression) Tree to compute logits per move,
 * and then a probabilitity distribution over those moves using a softmax.
 * 
 * @author Dennis Soemers
 */
public class SoftmaxPolicyLogitTree extends SoftmaxPolicy 
{

	//-------------------------------------------------------------------------
	
	/** 
	 * Roots of regression trees that can output logits (one per legal move). 
	 * 
	 * If it contains only one root, it will be shared across all
	 * players. Otherwise, it will contain one root per player.
	 */
	protected LogitTreeNode[] regressionTreeRoots;
	
	/** 
	 * Feature Sets to use to generate feature vectors for state+action pairs.
	 * 
	 * If it contains only one feature set, it will be shared across all
	 * players. Otherwise, it will contain one Feature Set per player.
	 */
	protected BaseFeatureSet[] featureSets;
	
	/** Temperature for distribution */
	protected double temperature = 1.0;
	
	/** Do we want to play greedily? */
	protected boolean greedy = false;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Default constructor. Will initialise important parts to null and break 
	 * down if used directly. Should customise() it first!
	 */
	public SoftmaxPolicyLogitTree()
	{
		regressionTreeRoots = null;
		featureSets = null;
	}
	
	/**
	 * Constructs a softmax policy with regression tree(s) for logits
	 * @param regressionTreeRoots
	 * @param featureSets
	 */
	public SoftmaxPolicyLogitTree
	(
		final LogitTreeNode[] regressionTreeRoots, 
		final BaseFeatureSet[] featureSets
	)
	{
		this.regressionTreeRoots = regressionTreeRoots;
		this.featureSets = Arrays.copyOf(featureSets, featureSets.length);
	}
	
	/**
	 * Constructs a softmax policy with regression tree(s) for logits,
	 * and a limit on the number of play-out actions to run with this policy
	 * plus a fallback Play-out strategy to use afterwards.
	 * 
	 * @param regressionTreeRoots
	 * @param featureSets
	 * @param playoutActionLimit
	 */
	public SoftmaxPolicyLogitTree
	(
		final LogitTreeNode[] regressionTreeRoots, 
		final BaseFeatureSet[] featureSets,
		final int playoutActionLimit
	)
	{
		this.regressionTreeRoots = regressionTreeRoots;
		this.featureSets = Arrays.copyOf(featureSets, featureSets.length);
		this.playoutActionLimit = playoutActionLimit;
	}
	
	/**
	 * Constructs a softmax policy from a given set of feature trees as created
	 * by the compiler, using the Selection weights.
	 * 
	 * @param featureTrees
	 * @param epsilon Epsilon for epsilon-greedy playouts
	 */
	public static SoftmaxPolicyLogitTree constructPolicy(final FeatureTrees featureTrees, final double epsilon)
	{
		final SoftmaxPolicyLogitTree softmax = new SoftmaxPolicyLogitTree();
		
		final List<BaseFeatureSet> featureSetsList = new ArrayList<BaseFeatureSet>();
		final List<LogitTreeNode> roots = new ArrayList<LogitTreeNode>();
				
		for (final LogitTree logitTree : featureTrees.logitTrees())
		{
			if (logitTree.role() == RoleType.Shared || logitTree.role() == RoleType.Neutral)
				addFeatureSetRoot(0, logitTree.root(), featureSetsList, roots);
			else
				addFeatureSetRoot(logitTree.role().owner(), logitTree.root(), featureSetsList, roots);
		}
		
		softmax.featureSets = featureSetsList.toArray(new BaseFeatureSet[featureSetsList.size()]);
		softmax.regressionTreeRoots = roots.toArray(new LogitTreeNode[roots.size()]);
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
		
		final LogitTreeNode regressionTreeRoot;
		
		if (regressionTreeRoots.length == 1)
			regressionTreeRoot = regressionTreeRoots[0];
		else
			regressionTreeRoot = regressionTreeRoots[context.state().mover()];
		
		return regressionTreeRoot.predict(featureSet.computeFeatureVector(context, move, true));
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
		final LogitTreeNode regressionTreeRoot;
		
		if (regressionTreeRoots.length == 1)
			regressionTreeRoot = regressionTreeRoots[0];
		else
			regressionTreeRoot = regressionTreeRoots[player];
		
		for (int i = 0; i < featureVectors.length; ++i)
		{
			logits[i] = regressionTreeRoot.predict(featureVectors[i]);
		}
		
		final FVector distribution = FVector.wrap(logits);
		distribution.softmax(temperature);
		
		return distribution;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public Trial runPlayout(final MCTS mcts, final Context context) 
	{		
		final PlayoutMoveSelector playoutMoveSelector;
		if (epsilon < 1.0)
		{
			if (epsilon <= 0.0)
				playoutMoveSelector = new LogitTreeMoveSelector(featureSets, regressionTreeRoots, greedy, temperature);
			else
				playoutMoveSelector = new EpsilonGreedyWrapper(new LogitTreeMoveSelector(featureSets, regressionTreeRoots, greedy, temperature), epsilon);
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
		String policyTreesFilepath = null;
		
		for (int i = 1; i < inputs.length; ++i)
		{
			final String input = inputs[i];
			
			if (input.toLowerCase().startsWith("policytrees="))
			{
				policyTreesFilepath = input.substring("policytrees=".length());
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
			else if (input.toLowerCase().startsWith("epsilon="))
			{
				epsilon = Double.parseDouble(input.substring("epsilon=".length()));
			}
			else if (input.toLowerCase().startsWith("greedy="))
			{
				greedy = Boolean.parseBoolean(input.substring("greedy=".length()));
			}
			else if (input.toLowerCase().startsWith("temperature="))
			{
				temperature = Double.parseDouble(input.substring("temperature=".length()));
			}
		}
		
		if (policyTreesFilepath != null)
		{
			final List<BaseFeatureSet> featureSetsList = new ArrayList<BaseFeatureSet>();
			final List<LogitTreeNode> roots = new ArrayList<LogitTreeNode>();
			
			try 
			{
				final String featureTreesString = FileHandling.loadTextContentsFromFile(policyTreesFilepath);
				final FeatureTrees featureTrees = 
						(FeatureTrees)compiler.Compiler.compileObject
						(
							featureTreesString, 
							"metadata.ai.features.trees.FeatureTrees",
							new Report()
						);
						
				for (final LogitTree logitTree : featureTrees.logitTrees())
				{
					if (logitTree.role() == RoleType.Shared || logitTree.role() == RoleType.Neutral)
						addFeatureSetRoot(0, logitTree.root(), featureSetsList, roots);
					else
						addFeatureSetRoot(logitTree.role().owner(), logitTree.root(), featureSetsList, roots);
				}
				
				this.featureSets = featureSetsList.toArray(new BaseFeatureSet[featureSetsList.size()]);
				this.regressionTreeRoots = roots.toArray(new LogitTreeNode[roots.size()]);
			} 
			catch (final IOException e) 
			{
				e.printStackTrace();
			}
				
		}
		else
		{
			System.err.println("Cannot construct Softmax Policy Logit Tree from: " + Arrays.toString(inputs));
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
		
		final FVector distribution =
				computeDistribution
				(
					featureSet.computeFeatureVectors
					(
						context, 
						actions.moves(), 
						true
					), 
					context.state().mover()
				);

		if (greedy)
			return actions.moves().get(distribution.argMaxRand());
		else
			return actions.moves().get(distribution.sampleFromDistribution());
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
			
			featureSets[0].init(game, supportedPlayers, null);
		}
		else
		{
			for (int i = 1; i < featureSets.length; ++i)
			{
				featureSets[i].init(game, new int[] {i}, null);
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
	 * @return Feature Sets used by this policy
	 */
	public BaseFeatureSet[] featureSets()
	{
		return featureSets;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param lines
	 * @return A softmax logit tree policy constructed from a given array of input lines
	 */
	public static SoftmaxPolicyLogitTree fromLines(final String[] lines)
	{
		SoftmaxPolicyLogitTree policy = null;
		
//		for (final String line : lines)
//		{
//			if (line.equalsIgnoreCase("features=from_metadata"))
//			{
//				policy = new SoftmaxFromMetadataSelection(0.0);
//				break;
//			}
//		}
//		
//		if (policy == null)
			policy = new SoftmaxPolicyLogitTree();

		policy.customise(lines);
		return policy;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Helper method that adds a Feature Set and a Linear Function for the 
	 * given player index
	 * 
	 * @param playerIdx
	 * @param rootNode
	 * @param outFeatureSets
	 * @param outRoots
	 */
	protected static void addFeatureSetRoot
	(
		final int playerIdx, 
		final LogitNode rootNode,
		final List<BaseFeatureSet> outFeatureSets, 
		final List<LogitTreeNode> outRoots
	)
	{
		while (outFeatureSets.size() <= playerIdx)
		{
			outFeatureSets.add(null);
		}
		
		while (outRoots.size() <= playerIdx)
		{
			outRoots.add(null);
		}
		
		final List<AspatialFeature> aspatialFeatures = new ArrayList<AspatialFeature>();
		final List<SpatialFeature> spatialFeatures = new ArrayList<SpatialFeature>();
		
		final Set<String> featureStrings = new HashSet<String>();
		rootNode.collectFeatureStrings(featureStrings);
		
		for (final String featureString : featureStrings)
		{
			final Feature feature = Feature.fromString(featureString);
			
			if (feature instanceof AspatialFeature)
				aspatialFeatures.add((AspatialFeature)feature);
			else
				spatialFeatures.add((SpatialFeature)feature);
		}
		
		final BaseFeatureSet featureSet = JITSPatterNetFeatureSet.construct(aspatialFeatures, spatialFeatures);
		outFeatureSets.set(playerIdx, featureSet);
		outRoots.set(playerIdx, LogitTreeNode.fromMetadataNode(rootNode, featureSet));
	}

	//-------------------------------------------------------------------------
	
}
