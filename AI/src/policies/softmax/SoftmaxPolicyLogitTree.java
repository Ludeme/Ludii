package policies.softmax;

import game.Game;
import main.collections.FVector;
import main.collections.FastArrayList;
import other.context.Context;
import other.move.Move;
import other.trial.Trial;
import search.mcts.MCTS;

/**
 * A policy that uses a Logit (Regression) Tree to compute logits per move,
 * and then a probabilitity distribution over those moves using a softmax.
 * 
 * @author Dennis Soemers
 */
public class SoftmaxPolicyLogitTree extends SoftmaxPolicy 
{

	@Override
	public Trial runPlayout(MCTS mcts, Context context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean playoutSupportsGame(Game game) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int backpropFlags() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void customise(String[] inputs) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public float computeLogit(Context context, Move move) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public FVector computeDistribution(Context context, FastArrayList<Move> actions, boolean thresholded) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Move selectAction(Game game, Context context, double maxSeconds, int maxIterations, int maxDepth) {
		// TODO Auto-generated method stub
		return null;
	}

//	//-------------------------------------------------------------------------
//	
//	/** 
//	 * Roots of regression trees that can output logits (one per legal move). 
//	 * 
//	 * If it contains only one root, it will be shared across all
//	 * players. Otherwise, it will contain one root per player.
//	 */
//	protected LogitTreeNode[] regressionTreeRoots;
//	
//	/** 
//	 * Feature Sets to use to generate feature vectors for state+action pairs.
//	 * 
//	 * If it contains only one feature set, it will be shared across all
//	 * players. Otherwise, it will contain one Feature Set per player.
//	 */
//	protected BaseFeatureSet[] featureSets;
//	
//	/** 
//	 * If >= 0, we'll only actually use this softmax policy in MCTS play-outs
//	 * for up to this many actions. If a play-out still did not terminate
//	 * after this many play-out actions, we revert to a random play-out
//	 * strategy as fallback
//	 */
//	protected int playoutActionLimit = -1;
//	
//	/** Auto-end playouts in a draw if they take more turns than this */
//	protected int playoutTurnLimit = -1;
//	
//	/** Epsilon for epsilon-greedy playouts */
//	protected double epsilon = 0.0;
//	
//	//-------------------------------------------------------------------------
//	
//	/**
//	 * Default constructor. Will initialise important parts to null and break 
//	 * down if used directly. Should customise() it first!
//	 */
//	public SoftmaxPolicyLogitTree()
//	{
//		regressionTreeRoots = null;
//		featureSets = null;
//	}
//	
//	/**
//	 * Constructs a softmax policy with regression tree(s) for logits
//	 * @param regressionTreeRoots
//	 * @param featureSets
//	 */
//	public SoftmaxPolicyLogitTree
//	(
//		final LogitTreeNode[] regressionTreeRoots, 
//		final BaseFeatureSet[] featureSets
//	)
//	{
//		this.regressionTreeRoots = regressionTreeRoots;
//		this.featureSets = Arrays.copyOf(featureSets, featureSets.length);
//	}
//	
//	/**
//	 * Constructs a softmax policy with regression tree(s) for logits,
//	 * and a limit on the number of play-out actions to run with this policy
//	 * plus a fallback Play-out strategy to use afterwards.
//	 * 
//	 * @param regressionTreeRoots
//	 * @param featureSets
//	 * @param playoutActionLimit
//	 */
//	public SoftmaxPolicyLogitTree
//	(
//		final LogitTreeNode[] regressionTreeRoots, 
//		final BaseFeatureSet[] featureSets,
//		final int playoutActionLimit
//	)
//	{
//		this.regressionTreeRoots = regressionTreeRoots;
//		this.featureSets = Arrays.copyOf(featureSets, featureSets.length);
//		this.playoutActionLimit = playoutActionLimit;
//	}
//	
//	/**
//	 * Constructs a softmax policy from a given set of feature trees as created
//	 * by the compiler, using the Selection weights.
//	 * 
//	 * @param features
//	 * @param epsilon Epsilon for epsilon-greedy playouts
//	 */
//	public static SoftmaxPolicyLogitTree constructPolicy(final FeatureTrees featureTrees, final double epsilon)
//	{
//		final SoftmaxPolicyLogitTree softmax = new SoftmaxPolicyLogitTree();
//		
//		final List<BaseFeatureSet> featureSetsList = new ArrayList<BaseFeatureSet>();
//		final List<LogitTreeNode> roots = new ArrayList<LogitTreeNode>();
//				
//		for (final LogitTree logitTree : featureTrees.logitTrees())
//		{
//			if (logitTree.role() == RoleType.Shared || logitTree.role() == RoleType.Neutral)
//				addFeatureSetRoot(0, logitTree.root(), featureSetsList, roots);
//			else
//				addFeatureSetRoot(logitTree.role().owner(), logitTree.root(), featureSetsList, roots);
//		}
//		
//		softmax.featureSets = featureSetsList.toArray(new BaseFeatureSet[featureSetsList.size()]);
//		softmax.regressionTreeRoots = roots.toArray(new LogitTreeNode[roots.size()]);
//		softmax.epsilon = epsilon;
//		
//		return softmax;
//	}
//	
//	//-------------------------------------------------------------------------
//	
//	@Override
//	public FVector computeDistribution
//	(
//		final Context context, 
//		final FastArrayList<Move> actions,
//		final boolean thresholded
//	)
//	{
//		final BaseFeatureSet featureSet;
//		
//		if (featureSets.length == 1)
//			featureSet = featureSets[0];
//		else
//			featureSet = featureSets[context.state().mover()];
//		
//		return computeDistribution(featureSet.computeFeatureVectors(context, actions, thresholded), context.state().mover());
//	}
//	
//	@Override
//	public float computeLogit(final Context context, final Move move)
//	{
//		final BaseFeatureSet featureSet;
//		
//		if (featureSets.length == 1)
//			featureSet = featureSets[0];
//		else
//			featureSet = featureSets[context.state().mover()];
//		
//		final LogitTreeNode regressionTreeRoot;
//		
//		if (regressionTreeRoots.length == 1)
//			regressionTreeRoot = regressionTreeRoots[0];
//		else
//			regressionTreeRoot = regressionTreeRoots[context.state().mover()];
//		
//		return regressionTreeRoot.predict(featureSet.computeFeatureVector(context, move, true));
//	}
//	
//	/**
//	 * @param featureVectors
//	 * @param player
//	 * @return Probability distribution over actions implied by a list of sparse 
//	 * feature vectors
//	 */
//	public FVector computeDistribution
//	(
//		final FeatureVector[] featureVectors,
//		final int player
//	)
//	{
//		final float[] logits = new float[featureVectors.length];
//		final LogitTreeNode regressionTreeRoot;
//		
//		if (regressionTreeRoots.length == 1)
//			regressionTreeRoot = regressionTreeRoots[0];
//		else
//			regressionTreeRoot = regressionTreeRoots[player];
//		
//		for (int i = 0; i < featureVectors.length; ++i)
//		{
//			logits[i] = regressionTreeRoot.predict(featureVectors[i]);
//		}
//		
//		final FVector distribution = FVector.wrap(logits);
//		distribution.softmax();
//		
//		return distribution;
//	}
//	
//	/**
//	 * @param distribution
//	 * @return Samples an action index from a previously-computed distribution
//	 */
//	public int selectActionFromDistribution(final FVector distribution)
//	{
//		return distribution.sampleFromDistribution();
//	}
//	
//	//-------------------------------------------------------------------------
//	
//	@Override
//	public Trial runPlayout(final MCTS mcts, final Context context) 
//	{		
//		final PlayoutMoveSelector playoutMoveSelector;
//		if (epsilon < 1.0)
//		{
//			if (epsilon <= 0.0)
//				playoutMoveSelector = new LogitTreeMoveSelector(featureSets, regressionTreeRoots);
//			else
//				playoutMoveSelector = new EpsilonGreedyWrapper(new LogitTreeMoveSelector(featureSets, regressionTreeRoots), epsilon);
//		}
//		else
//		{
//			playoutMoveSelector = null;
//		}
//		
//		return context.game().playout
//				(
//					context, 
//					null, 
//					1.0, 
//					playoutMoveSelector,
//					playoutActionLimit,
//					playoutTurnLimit,
//					ThreadLocalRandom.current()
//				);
//	}
//	
//	@Override
//	public boolean playoutSupportsGame(final Game game)
//	{
//		return supportsGame(game);
//	}
//	
//	@Override
//	public int backpropFlags()
//	{
//		return 0;
//	}
//
//	@Override
//	public void customise(final String[] inputs) 
//	{
//		final List<String> policyWeightsFilepaths = new ArrayList<String>();
//		boolean boosted = false;
//		
//		for (int i = 1; i < inputs.length; ++i)
//		{
//			final String input = inputs[i];
//			
//			if (input.toLowerCase().startsWith("policyweights="))
//			{
//				if (policyWeightsFilepaths.size() > 0)
//					policyWeightsFilepaths.clear();
//				
//				policyWeightsFilepaths.add(input.substring("policyweights=".length()));
//			}
//			else if (input.toLowerCase().startsWith("policyweights"))
//			{
//				for (int p = 1; p <= Constants.MAX_PLAYERS; ++p)
//				{
//					if (input.toLowerCase().startsWith("policyweights" + p + "="))
//					{
//						while (policyWeightsFilepaths.size() <= p)
//						{
//							policyWeightsFilepaths.add(null);
//						}
//						
//						if (p < 10)
//							policyWeightsFilepaths.set(p, input.substring("policyweightsX=".length()));
//						else		// Doubt we'll ever have more than 99 players
//							policyWeightsFilepaths.set(p, input.substring("policyweightsXX=".length()));
//					}
//				}
//			}
//			else if (input.toLowerCase().startsWith("playoutactionlimit="))
//			{
//				playoutActionLimit = 
//						Integer.parseInt(input.substring(
//								"playoutactionlimit=".length()));
//			}
//			else if (input.toLowerCase().startsWith("playoutturnlimit="))
//			{
//				playoutTurnLimit = 
//						Integer.parseInt
//						(
//							input.substring("playoutturnlimit=".length())
//						);
//			}
//			else if (input.toLowerCase().startsWith("friendly_name="))
//			{
//				friendlyName = input.substring("friendly_name=".length());
//			}
//			else if (input.toLowerCase().startsWith("boosted="))
//			{
//				if (input.toLowerCase().endsWith("true"))
//				{
//					boosted = true;
//				}
//			}
//			else if (input.toLowerCase().startsWith("epsilon="))
//			{
//				epsilon = Double.parseDouble(input.substring("epsilon=".length()));
//			}
//		}
//		
//		if (!policyWeightsFilepaths.isEmpty())
//		{
//			this.linearFunctions = new LinearFunction[policyWeightsFilepaths.size()];
//			this.featureSets = new BaseFeatureSet[linearFunctions.length];
//			
//			for (int i = 0; i < policyWeightsFilepaths.size(); ++i)
//			{
//				String policyWeightsFilepath = policyWeightsFilepaths.get(i);
//				
//				if (policyWeightsFilepath != null)
//				{
//					final String parentDir = new File(policyWeightsFilepath).getParent();
//					
//					if (!new File(policyWeightsFilepath).exists())
//					{
//						// Replace with whatever is the latest file we have
//						if (policyWeightsFilepath.contains("Selection"))
//						{
//							policyWeightsFilepath = 
//								ExperimentFileUtils.getLastFilepath(parentDir + "/PolicyWeightsSelection_P" + i, "txt");
//						}
//						else if (policyWeightsFilepath.contains("Playout"))
//						{
//							policyWeightsFilepath = 
//								ExperimentFileUtils.getLastFilepath(parentDir + "/PolicyWeightsPlayout_P" + i, "txt");
//						}
//						else if (policyWeightsFilepath.contains("TSPG"))
//						{
//							policyWeightsFilepath = 
//								ExperimentFileUtils.getLastFilepath(parentDir + "/PolicyWeightsTSPG_P" + i, "txt");
//						}
//						else
//						{
//							policyWeightsFilepath = null;
//						}
//					}
//					
//					if (boosted)
//						linearFunctions[i] = BoostedLinearFunction.boostedFromFile(policyWeightsFilepath, null);
//					else
//						linearFunctions[i] = LinearFunction.fromFile(policyWeightsFilepath);
//					
//					featureSets[i] = JITSPatterNetFeatureSet.construct(parentDir + File.separator + linearFunctions[i].featureSetFile());
//				}
//			}
//		}
//		else
//		{
//			System.err.println("Cannot construct Softmax Policy from: " + Arrays.toString(inputs));
//		}
//	}
//	
//	//-------------------------------------------------------------------------
//
//	@Override
//	public Move selectAction
//	(
//		final Game game, 
//		final Context context, 
//		final double maxSeconds,
//		final int maxIterations,
//		final int maxDepth
//	)
//	{
//		final Moves actions = game.moves(context);
//		final BaseFeatureSet featureSet;
//			
//		if (featureSets.length == 1)
//		{
//			featureSet = featureSets[0];
//		}
//		else
//		{
//			featureSet = featureSets[context.state().mover()];
//		}
//
//		return actions.moves().get
//				(
//					selectActionFromDistribution
//					(
//						computeDistribution
//						(
//							featureSet.computeFeatureVectors
//							(
//								context, 
//								actions.moves(), 
//								true
//							), 
//							context.state().mover()
//						)
//					)
//				);
//	}
//	
//	//-------------------------------------------------------------------------
//	
//	@Override
//	public void initAI(final Game game, final int playerID)
//	{
//		if (featureSets.length == 1)
//		{
//			final int[] supportedPlayers = new int[game.players().count()];
//			for (int i = 0; i < supportedPlayers.length; ++i)
//			{
//				supportedPlayers[i] = i + 1;
//			}
//			
//			featureSets[0].init(game, supportedPlayers, linearFunctions[0].effectiveParams());
//		}
//		else
//		{
//			for (int i = 1; i < featureSets.length; ++i)
//			{
//				featureSets[i].init(game, new int[] {i}, linearFunctions[i].effectiveParams());
//			}
//		}
//	}
//	
//	@Override
//	public void closeAI()
//	{
//		if (featureSets == null)
//			return;
//		
//		if (featureSets.length == 1)
//		{
//			featureSets[0].closeCache();
//		}
//		else
//		{
//			for (int i = 1; i < featureSets.length; ++i)
//			{
//				featureSets[i].closeCache();
//			}
//		}
//	}
//	
//	//-------------------------------------------------------------------------
//	
//	/**
//	 * @param player
//	 * @return Linear function corresponding to given player
//	 */
//	public LinearFunction linearFunction(final int player)
//	{
//		if (linearFunctions.length == 1)
//			return linearFunctions[0];
//		else
//			return linearFunctions[player];
//	}
//	
//	/**
//	 * @return The linear functions used to compute logits
//	 */
//	public LinearFunction[] linearFunctions()
//	{
//		return linearFunctions;
//	}
//	
//	/**
//	 * @return Feature Sets used by this policy
//	 */
//	public BaseFeatureSet[] featureSets()
//	{
//		return featureSets;
//	}
//	
//	//-------------------------------------------------------------------------
//	
//	/**
//	 * @return A metadata Features item describing the features + weights for this policy
//	 */
////	public metadata.ai.features.Features generateFeaturesMetadata()
////	{
////		final Features features;
////		
////		if (featureSets.length == 1)
////		{
////			// Just a single featureset for all players
////			final BaseFeatureSet featureSet = featureSets[0];
////			final LinearFunction linFunc = linearFunctions[0];
////			final Pair[] pairs = new Pair[featureSet.spatialFeatures().length];
////			
////			for (int i = 0; i < pairs.length; ++i)
////			{
////				final float weight = linFunc.effectiveParams().allWeights().get(i);
////				pairs[i] = new Pair(featureSet.spatialFeatures()[i].toString(), Float.valueOf(weight));
////				
////				if (Float.isNaN(weight))
////					System.err.println("WARNING: writing NaN weight");
////				else if (Float.isInfinite(weight))
////					System.err.println("WARNING: writing infinity weight");
////			}
////			
////			features = new Features(new metadata.ai.features.FeatureSet(RoleType.Shared, pairs));
////		}
////		else
////		{
////			// One featureset per player
////			final metadata.ai.features.FeatureSet[] metadataFeatureSets = new metadata.ai.features.FeatureSet[featureSets.length - 1];
////			
////			for (int p = 0; p < featureSets.length; ++p)
////			{
////				final BaseFeatureSet featureSet = featureSets[p];
////				if (featureSet == null)
////					continue;
////				
////				final LinearFunction linFunc = linearFunctions[p];
////				final Pair[] pairs = new Pair[featureSet.spatialFeatures().length];
////				
////				for (int i = 0; i < pairs.length; ++i)
////				{
////					final float weight = linFunc.effectiveParams().allWeights().get(i);
////					pairs[i] = new Pair(featureSet.spatialFeatures()[i].toString(), Float.valueOf(weight));
////					
////					if (Float.isNaN(weight))
////						System.err.println("WARNING: writing NaN weight");
////					else if (Float.isInfinite(weight))
////						System.err.println("WARNING: writing infinity weight");
////				}
////				
////				metadataFeatureSets[p - 1] = new metadata.ai.features.FeatureSet(RoleType.roleForPlayerId(p), pairs);
////			}
////			
////			features = new Features(metadataFeatureSets);
////		}
////		
////		return features;
////	}
//	
//	//-------------------------------------------------------------------------
//	
//	/**
//	 * @param lines
//	 * @return A softmax policy constructed from a given array of input lines
//	 */
//	public static SoftmaxPolicyLogitTree fromLines(final String[] lines)
//	{
//		SoftmaxPolicyLogitTree policy = null;
//		
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
//			policy = new SoftmaxPolicyLogitTree();
//
//		policy.customise(lines);
//		return policy;
//	}
//	
//	/**
//	 * @param weightsFile
//	 * @return A Softmax policy constructed from a given file
//	 */
//	public static SoftmaxPolicyLogitTree fromFile(final File weightsFile)
//	{
//		final SoftmaxPolicyLogitTree policy = new SoftmaxPolicyLogitTree();
//		boolean boosted = false;
//		
//		try (final BufferedReader reader = new BufferedReader(
//				new InputStreamReader(new FileInputStream(weightsFile.getAbsolutePath()), "UTF-8")))
//		{
//			String line = reader.readLine();
//			String lastLine = null;
//			
//			while (line != null)
//			{
//				lastLine = line;
//				line = reader.readLine();
//			}
//			
//			if (!lastLine.startsWith("FeatureSet="))
//			{
//				boosted = true;
//			}
//		}
//		catch (final IOException e)
//		{
//			e.printStackTrace();
//		}
//		
//		policy.customise(new String[]{
//				"softmax",
//				"policyweights=" + weightsFile.getAbsolutePath(),
//				"boosted=" + boosted
//		});
//		return policy;
//	}
//	
//	//-------------------------------------------------------------------------
//	
//	/**
//	 * Helper method that adds a Feature Set and a Linear Function for the 
//	 * given player index
//	 * 
//	 * @param playerIdx
//	 * @param rootNode
//	 * @param outFeatureSets
//	 * @param outRoots
//	 */
//	protected static void addFeatureSetRoot
//	(
//		final int playerIdx, 
//		final LogitNode rootNode,
//		final List<BaseFeatureSet> outFeatureSets, 
//		final List<LogitTreeNode> outRoots
//	)
//	{
//		while (outFeatureSets.size() <= playerIdx)
//		{
//			outFeatureSets.add(null);
//		}
//		
//		while (outRoots.size() <= playerIdx)
//		{
//			outRoots.add(null);
//		}
//		
//		final List<AspatialFeature> aspatialFeatures = new ArrayList<AspatialFeature>();
//		final List<SpatialFeature> spatialFeatures = new ArrayList<SpatialFeature>();
//		
//		final Set<String> featureStrings = new HashSet<String>();
//		rootNode.collectFeatureStrings(featureStrings);
//		
//		for (final String featureString : featureStrings)
//		{
//			final Feature feature = Feature.fromString(featureString);
//			
//			if (feature instanceof AspatialFeature)
//				aspatialFeatures.add((AspatialFeature)feature);
//			else
//				spatialFeatures.add((SpatialFeature)feature);
//		}
//		
//		final BaseFeatureSet featureSet = JITSPatterNetFeatureSet.construct(aspatialFeatures, spatialFeatures);
//		outFeatureSets.set(playerIdx, featureSet);
//		outRoots.set(playerIdx, LogitTreeNode.fromMetadataNode(rootNode, featureSet));
//	}
//
//	//-------------------------------------------------------------------------
	
}
