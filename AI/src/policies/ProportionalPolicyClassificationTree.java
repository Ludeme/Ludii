package policies;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import decision_trees.classifiers.DecisionTreeNode;
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
import metadata.ai.features.trees.classifiers.DecisionTree;
import other.context.Context;
import other.move.Move;
import other.playout.PlayoutMoveSelector;
import other.trial.Trial;
import playout_move_selectors.DecisionTreeMoveSelector;
import playout_move_selectors.EpsilonGreedyWrapper;
import search.mcts.MCTS;

/**
 * A policy that uses a Classification Tree to compute probabilities per move.
 * 
 * @author Dennis Soemers
 */
public class ProportionalPolicyClassificationTree extends Policy 
{

	//-------------------------------------------------------------------------
	
	/** 
	 * Roots of decision trees that can output probability estimates (one per legal move). 
	 * 
	 * If it contains only one root, it will be shared across all
	 * players. Otherwise, it will contain one root per player.
	 */
	protected DecisionTreeNode[] decisionTreeRoots;
	
	/** 
	 * Feature Sets to use to generate feature vectors for state+action pairs.
	 * 
	 * If it contains only one feature set, it will be shared across all
	 * players. Otherwise, it will contain one Feature Set per player.
	 */
	protected BaseFeatureSet[] featureSets;
	
	/** 
	 * If >= 0, we'll only actually use this policy in MCTS play-outs
	 * for up to this many actions. If a play-out still did not terminate
	 * after this many play-out actions, we revert to a random play-out
	 * strategy as fallback
	 */
	protected int playoutActionLimit = -1;
	
	/** Auto-end playouts in a draw if they take more turns than this */
	protected int playoutTurnLimit = -1;
	
	/** Epsilon for epsilon-greedy playouts */
	protected double epsilon = 0.0;
	
	/** If true, we play greedily instead of sampling proportional to probabilities */
	protected boolean greedy = false;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Default constructor. Will initialise important parts to null and break 
	 * down if used directly. Should customise() it first!
	 */
	public ProportionalPolicyClassificationTree()
	{
		decisionTreeRoots = null;
		featureSets = null;
	}
	
	/**
	 * Constructs a policy with classification tree(s) for probabilities
	 * @param decisionTreeRoots
	 * @param featureSets
	 */
	public ProportionalPolicyClassificationTree
	(
		final DecisionTreeNode[] decisionTreeRoots, 
		final BaseFeatureSet[] featureSets
	)
	{
		this.decisionTreeRoots = decisionTreeRoots;
		this.featureSets = Arrays.copyOf(featureSets, featureSets.length);
	}
	
	/**
	 * Constructs a policy with classification tree(s) for probabilities,
	 * and a limit on the number of play-out actions to run with this policy
	 * plus a fallback Play-out strategy to use afterwards.
	 * 
	 * @param featureSets
	 * @param playoutActionLimit
	 */
	public ProportionalPolicyClassificationTree
	(
		final DecisionTreeNode[] decisionTreeRoots, 
		final BaseFeatureSet[] featureSets,
		final int playoutActionLimit
	)
	{
		this.decisionTreeRoots = decisionTreeRoots;
		this.featureSets = Arrays.copyOf(featureSets, featureSets.length);
		this.playoutActionLimit = playoutActionLimit;
	}
	
	/**
	 * Constructs a policy from a given set of feature trees as created
	 * by the compiler, using classification trees
	 * 
	 * @param featureTrees
	 * @param epsilon Epsilon for epsilon-greedy playouts
	 */
	public static ProportionalPolicyClassificationTree constructPolicy(final FeatureTrees featureTrees, final double epsilon)
	{
		final ProportionalPolicyClassificationTree softmax = new ProportionalPolicyClassificationTree();
		
		final List<BaseFeatureSet> featureSetsList = new ArrayList<BaseFeatureSet>();
		final List<DecisionTreeNode> roots = new ArrayList<DecisionTreeNode>();
				
		for (final DecisionTree classificationTree : featureTrees.decisionTrees())
		{
			if (classificationTree.role() == RoleType.Shared || classificationTree.role() == RoleType.Neutral)
				addFeatureSetRoot(0, classificationTree.root(), featureSetsList, roots);
			else
				addFeatureSetRoot(classificationTree.role().owner(), classificationTree.root(), featureSetsList, roots);
		}
		
		softmax.featureSets = featureSetsList.toArray(new BaseFeatureSet[featureSetsList.size()]);
		softmax.decisionTreeRoots = roots.toArray(new DecisionTreeNode[roots.size()]);
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
		final DecisionTreeNode decisionTreeRoot;
		
		if (decisionTreeRoots.length == 1)
			decisionTreeRoot = decisionTreeRoots[0];
		else
			decisionTreeRoot = decisionTreeRoots[player];
		
		for (int i = 0; i < featureVectors.length; ++i)
		{
			logits[i] = decisionTreeRoot.predict(featureVectors[i]);
		}
		
		final FVector distribution = FVector.wrap(logits);
		distribution.normalise();
		
		return distribution;
	}
	
	@Override
	public float computeLogit(final Context context, final Move move)
	{
		final DecisionTreeNode decisionTreeRoot;
		
		if (decisionTreeRoots.length == 1)
			decisionTreeRoot = decisionTreeRoots[0];
		else
			decisionTreeRoot = decisionTreeRoots[context.state().mover()];
		
		final BaseFeatureSet featureSet;
		
		if (featureSets.length == 1)
			featureSet = featureSets[0];
		else
			featureSet = featureSets[context.state().mover()];
		
		return decisionTreeRoot.predict(featureSet.computeFeatureVector(context, move, true));
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public Trial runPlayout(final MCTS mcts, final Context context) 
	{		
		final PlayoutMoveSelector playoutMoveSelector;
		if (epsilon < 1.0)
		{
			if (epsilon <= 0.0)
				playoutMoveSelector = new DecisionTreeMoveSelector(featureSets, decisionTreeRoots, greedy);
			else
				playoutMoveSelector = new EpsilonGreedyWrapper(new DecisionTreeMoveSelector(featureSets, decisionTreeRoots, greedy), epsilon);
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
		}
		
		if (policyTreesFilepath != null)
		{
			final List<BaseFeatureSet> featureSetsList = new ArrayList<BaseFeatureSet>();
			final List<DecisionTreeNode> roots = new ArrayList<DecisionTreeNode>();
			
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
						
				for (final DecisionTree decisionTree : featureTrees.decisionTrees())
				{
					if (decisionTree.role() == RoleType.Shared || decisionTree.role() == RoleType.Neutral)
						addFeatureSetRoot(0, decisionTree.root(), featureSetsList, roots);
					else
						addFeatureSetRoot(decisionTree.role().owner(), decisionTree.root(), featureSetsList, roots);
				}
				
				this.featureSets = featureSetsList.toArray(new BaseFeatureSet[featureSetsList.size()]);
				this.decisionTreeRoots = roots.toArray(new DecisionTreeNode[roots.size()]);
			} 
			catch (final IOException e) 
			{
				e.printStackTrace();
			}
				
		}
		else
		{
			System.err.println("Cannot construct Proportional Policy Classification Tree from: " + Arrays.toString(inputs));
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
		{
			return actions.moves().get(distribution.argMaxRand());
		}
		else
		{
			return actions.moves().get(distribution.sampleFromDistribution());
		}
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
	 * @return A classification tree policy constructed from a given array of input lines
	 */
	public static ProportionalPolicyClassificationTree fromLines(final String[] lines)
	{
		ProportionalPolicyClassificationTree policy = null;
		
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
			policy = new ProportionalPolicyClassificationTree();

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
		final metadata.ai.features.trees.classifiers.DecisionTreeNode rootNode,
		final List<BaseFeatureSet> outFeatureSets, 
		final List<DecisionTreeNode> outRoots
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
		outRoots.set(playerIdx, DecisionTreeNode.fromMetadataNode(rootNode, featureSet));
	}

	//-------------------------------------------------------------------------
	
}
