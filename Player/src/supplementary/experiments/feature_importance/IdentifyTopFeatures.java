package supplementary.experiments.feature_importance;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import decision_trees.classifiers.DecisionConditionNode;
import decision_trees.classifiers.DecisionTreeNode;
import decision_trees.classifiers.ExperienceUrgencyTreeLearner;
import features.Feature;
import features.FeatureVector;
import features.WeightVector;
import features.aspatial.AspatialFeature;
import features.feature_sets.BaseFeatureSet;
import features.feature_sets.network.JITSPatterNetFeatureSet;
import features.spatial.SpatialFeature;
import function_approx.LinearFunction;
import game.Game;
import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.list.array.TIntArrayList;
import main.CommandLineArgParse;
import main.CommandLineArgParse.ArgOption;
import main.CommandLineArgParse.OptionTypes;
import main.StringRoutines;
import main.collections.ArrayUtils;
import main.collections.FVector;
import main.collections.ListUtils;
import main.collections.ScoredInt;
import main.math.statistics.IncrementalStats;
import other.AI;
import other.GameLoader;
import other.RankUtils;
import other.context.Context;
import other.trial.Trial;
import policies.softmax.SoftmaxPolicyLinear;
import search.mcts.MCTS;
import training.expert_iteration.ExItExperience;
import utils.AIFactory;
import utils.ExperimentFileUtils;
import utils.data_structures.experience_buffers.ExperienceBuffer;
import utils.data_structures.experience_buffers.PrioritizedReplayBuffer;
import utils.data_structures.experience_buffers.UniformExperienceBuffer;

/**
 * Script to identify small collection of top features
 * 
 * @author Dennis Soemers
 */
public class IdentifyTopFeatures
{
	
	//-------------------------------------------------------------------------
	
	/** 
	 * Every feature is, per generation, guaranteed to get this many trials 
	 * (likely each against a different opponent feature) for eval.
	 */
	private static final int NUM_TRIALS_PER_FEATURE_EVAL = 25;
	
	/**
	 * Number of features we want to end up with at the end (per player).
	 */
	private static final int GOAL_NUM_FEATURES = 15;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Runs the process
	 * @param parsedArgs
	 */
	private static void identifyTopFeatures(final CommandLineArgParse parsedArgs)
	{
		final String gameName = parsedArgs.getValueString("--game");
		final Game game = GameLoader.loadGameFromName(gameName);
			
		if (game == null)
			throw new IllegalArgumentException("Cannot load game: " + gameName);
		
		final int numPlayers = game.players().count();
		
		String trainingOutDirPath = parsedArgs.getValueString("--training-out-dir");
		if (!trainingOutDirPath.endsWith("/"))
			trainingOutDirPath += "/";
		
		// Load feature sets and policies
		final BaseFeatureSet[] featureSets = new BaseFeatureSet[numPlayers + 1];
		final LinearFunction[] linearFunctionsPlayout = new LinearFunction[numPlayers + 1];
		final LinearFunction[] linearFunctionsTSPG = new LinearFunction[numPlayers + 1];
		
		loadFeaturesAndWeights(game, trainingOutDirPath, featureSets, linearFunctionsPlayout, linearFunctionsTSPG);
		
//		for (int p = 1; p < featureSets.length; ++p)
//		{
//			// Add simplified versions of existing spatial features
//			final BaseFeatureSet featureSet = featureSets[p];
//			final SpatialFeature[] origSpatialFeatures = featureSet.spatialFeatures();
//			final List<SpatialFeature> featuresToAdd = new ArrayList<SpatialFeature>();
//			
//			for (final SpatialFeature feature : origSpatialFeatures)
//			{
//				featuresToAdd.addAll(feature.generateGeneralisers(game));
//			}
//			
//			featureSets[p] = featureSet.createExpandedFeatureSet(game, featuresToAdd);
//			featureSets[p].init(game, new int[] {p}, null);
//		}
		
		// Load experience buffers
		final ExperienceBuffer[] experienceBuffers = new ExperienceBuffer[numPlayers + 1];
		loadExperienceBuffers(game, trainingOutDirPath, experienceBuffers);
		
		// Build trees for all players
		final DecisionTreeNode[] playoutTreesPerPlayer = new DecisionTreeNode[numPlayers + 1];
		final DecisionTreeNode[] tspgTreesPerPlayer = new DecisionTreeNode[numPlayers + 1];
		
		for (int p = 1; p <= numPlayers; ++p)
		{
			playoutTreesPerPlayer[p] = 
					ExperienceUrgencyTreeLearner.buildTree(featureSets[p], linearFunctionsPlayout[p], experienceBuffers[p], 4, 10);
			tspgTreesPerPlayer[p] = 
					ExperienceUrgencyTreeLearner.buildTree(featureSets[p], linearFunctionsTSPG[p], experienceBuffers[p], 4, 10);
		}
		
		// For every player, extract candidate features from the decision trees for that player
		final List<List<AspatialFeature>> candidateAspatialFeaturesPerPlayer = new ArrayList<List<AspatialFeature>>();
		final List<List<SpatialFeature>> candidateSpatialFeaturesPerPlayer = new ArrayList<List<SpatialFeature>>();
		candidateAspatialFeaturesPerPlayer.add(null);
		candidateSpatialFeaturesPerPlayer.add(null);
		
		for (int p = 1; p <= numPlayers; ++p)
		{
			// Collect all the features in our trees
			final List<AspatialFeature> aspatialFeaturesList = new ArrayList<AspatialFeature>();
			List<SpatialFeature> spatialFeaturesList = new ArrayList<SpatialFeature>();
			
			final List<Feature> featuresList = new ArrayList<Feature>();
			
			collectFeatures(playoutTreesPerPlayer[p], featuresList);
			collectFeatures(tspgTreesPerPlayer[p], featuresList);
			
			// Get rid of any sorts of duplicates/redundancies
			for (final Feature feature : featuresList)
			{
				if (feature instanceof AspatialFeature)
				{
					if (!aspatialFeaturesList.contains(feature))
						aspatialFeaturesList.add((AspatialFeature) feature);
				}
				else
				{
					spatialFeaturesList.add((SpatialFeature) feature);
				}
			}
			
			spatialFeaturesList = SpatialFeature.deduplicate(spatialFeaturesList);
			spatialFeaturesList = SpatialFeature.simplifySpatialFeaturesList(game, spatialFeaturesList);
			
			// Add generalisers of our candidate spatial features
			final int origNumSpatialFeatures = spatialFeaturesList.size();
			for (int i = 0; i < origNumSpatialFeatures; ++i)
			{
				spatialFeaturesList.addAll(spatialFeaturesList.get(i).generateGeneralisers(game));
			}
			
			// Do another round of cleaning up duplicates
			spatialFeaturesList = SpatialFeature.deduplicate(spatialFeaturesList);
			spatialFeaturesList = SpatialFeature.simplifySpatialFeaturesList(game, spatialFeaturesList);
			
			candidateAspatialFeaturesPerPlayer.add(aspatialFeaturesList);
			candidateSpatialFeaturesPerPlayer.add(spatialFeaturesList);
		}
		
		// Create new feature sets with all the candidate features
		final BaseFeatureSet[] candidateFeatureSets = new BaseFeatureSet[numPlayers + 1];
		for (int p = 1; p <= numPlayers; ++p)
		{
			candidateFeatureSets[p] = JITSPatterNetFeatureSet.construct(candidateAspatialFeaturesPerPlayer.get(p), candidateSpatialFeaturesPerPlayer.get(p));
			candidateFeatureSets[p].init(game, new int [] {p}, null);
		}
		
		// For every candidate feature, determine a single weight as the one we
		// would have used for it if the feature were used in the root of a logit tree
		final FVector[] candidateFeatureWeightsPerPlayer = new FVector[numPlayers + 1];
		for (int p = 1; p <= numPlayers; ++p)
		{
			candidateFeatureWeightsPerPlayer[p] = 
					computeCandidateFeatureWeights
					(
						candidateFeatureSets[p], 
						featureSets[p], 
						linearFunctionsPlayout[p], 
						experienceBuffers[p]
					);
		}
		
		// Clear some memory
		Arrays.fill(experienceBuffers, null);
		Arrays.fill(candidateFeatureSets, null);
		Arrays.fill(featureSets, null);
		
		// Run trials to evaluate all our candidate features and rank them
		final List<List<Feature>> candidateFeaturesPerPlayer = new ArrayList<List<Feature>>(numPlayers + 1);
		candidateFeaturesPerPlayer.add(null);
		for (int p = 1; p <= numPlayers; ++p)
		{
			final List<Feature> candidateFeatures = new ArrayList<Feature>();
			candidateFeatures.addAll(candidateAspatialFeaturesPerPlayer.get(p));
			candidateFeatures.addAll(candidateSpatialFeaturesPerPlayer.get(p));
			candidateFeaturesPerPlayer.add(candidateFeatures);
			
//			System.out.println("Candidate features for player " + p);
//			
//			for (final Feature f : candidateFeatures)
//			{
//				System.out.println(f);
//			}
//			
//			System.out.println();
		}
		
		evaluateCandidateFeatures(game, candidateFeaturesPerPlayer, candidateFeatureWeightsPerPlayer, parsedArgs);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Evaluates all the given candidate features and tries to rank them by playing
	 * a bunch of trials with them.
	 * 
	 * @param game
	 * @param candidateFeaturesPerPlayer
	 * @param candidateFeatureWeightsPerPlayer
	 * @param parsedArgs
	 */
	private static void evaluateCandidateFeatures
	(
		final Game game,
		final List<List<Feature>> candidateFeaturesPerPlayer,
		final FVector[] candidateFeatureWeightsPerPlayer,
		final CommandLineArgParse parsedArgs
	) 
	{
		String outDir = parsedArgs.getValueString("--out-dir");
		if (!outDir.endsWith("/"))
			outDir += "/";
		
		final int numPlayers = game.players().count();
		final IncrementalStats[][] playerFeatureScores = new IncrementalStats[numPlayers + 1][];
		final TIntArrayList[] remainingFeaturesPerPlayer = new TIntArrayList[numPlayers + 1];
		final BaseFeatureSet[] playerFeatureSets = new BaseFeatureSet[numPlayers + 1];
		final LinearFunction[] playerLinFuncs = new LinearFunction[numPlayers + 1];
		
		for (int p = 1; p <= numPlayers; ++p)
		{
			playerFeatureScores[p] = new IncrementalStats[candidateFeaturesPerPlayer.get(p).size()];
			
			for (int i = 0; i < playerFeatureScores[p].length; ++i)
			{
				playerFeatureScores[p][i] = new IncrementalStats();
			}
			
			remainingFeaturesPerPlayer[p] = ListUtils.range(candidateFeaturesPerPlayer.get(p).size());
			playerLinFuncs[p] = new LinearFunction(new WeightVector(FVector.wrap(new float[] {Float.NaN})));
		}
		
		boolean terminateProcess = false;
		
		final Trial trial = new Trial(game);
		final Context context = new Context(game, trial);
		int generation = 0;
		
		while (!terminateProcess)
		{
			// Start a new generation
			++generation;
			
			for (int p = 1; p <= numPlayers; ++p)
			{
				// An evaluation round for all features of player p
				for (int i = 0; i < remainingFeaturesPerPlayer[p].size(); ++i)
				{
					// Evaluate i'th feature for player p
					final int idxFeatureToEval = remainingFeaturesPerPlayer[p].getQuick(i);
					playerFeatureSets[p] = JITSPatterNetFeatureSet.construct(
							Arrays.asList(candidateFeaturesPerPlayer.get(p).get(idxFeatureToEval)));
					playerFeatureSets[p].init(game, new int[] {p}, null);
					playerLinFuncs[p].trainableParams().allWeights().set(0, candidateFeatureWeightsPerPlayer[p].get(idxFeatureToEval));
					
					for (int trialIdx = 0; trialIdx < (NUM_TRIALS_PER_FEATURE_EVAL * generation); ++trialIdx)
					{
						game.start(context);
						
						// Pick random feature indices for all other players
						final int[] featureIndices = new int[numPlayers + 1];
						featureIndices[0] = Integer.MIN_VALUE;
						featureIndices[p] = idxFeatureToEval;
						
						final List<AI> ais = new ArrayList<AI>();
						ais.add(null);
						
						for (int opp = 1; opp <= numPlayers; ++opp)
						{
							if (opp != p)
							{
								featureIndices[opp] = 
										ThreadLocalRandom.current().nextInt(remainingFeaturesPerPlayer[opp].size());
								playerFeatureSets[opp] = JITSPatterNetFeatureSet.construct(
										Arrays.asList(candidateFeaturesPerPlayer.get(opp).get(featureIndices[opp])));
								playerLinFuncs[opp].trainableParams().allWeights().set(0, candidateFeatureWeightsPerPlayer[opp].get(featureIndices[opp]));
							}
						}
						
						for (int player = 1; player <= numPlayers; ++player)
						{
							final SoftmaxPolicyLinear softmax = new SoftmaxPolicyLinear(playerLinFuncs, playerFeatureSets);
							ais.add(softmax);
							softmax.initAI(game, player);
						}
						
						// Play the trial
						game.playout(context, ais, 1.0, null, -1, -1, null);
						
						// Update feature evaluations
						final double[] agentUtils = RankUtils.agentUtilities(context);
						
						for (int player = 1; player <= numPlayers; ++player)
						{
							playerFeatureScores[player][featureIndices[player]].observe(agentUtils[player]);
						}
					}
				}
			}
			
			// We'll terminate except if in the evaluating we decide that we want to keep going
			terminateProcess = true;
			
			// Evaluate the entire generation
			for (int p = 1; p <= numPlayers; ++p)
			{
				// Sort remaining feature indices for this player based on their scores
				final List<ScoredInt> scoredIndices = new ArrayList<ScoredInt>();
				for (int i = 0; i < remainingFeaturesPerPlayer[p].size(); ++i)
				{
					final int fIdx = remainingFeaturesPerPlayer[p].getQuick(i);
					scoredIndices.add(new ScoredInt(fIdx, playerFeatureScores[p][fIdx].getMean()));
				}
				Collections.sort(scoredIndices, ScoredInt.DESCENDING);
				
				// Keep only the best ones
				final int numToKeep = Math.max(GOAL_NUM_FEATURES, scoredIndices.size() / 2);
				final TIntArrayList keepIndices = new TIntArrayList();
				for (int i = 0; i < numToKeep; ++i)
				{
					final int fIdx = scoredIndices.get(i).object();
					keepIndices.add(fIdx);
				}
				remainingFeaturesPerPlayer[p] = keepIndices;
				
				if (numToKeep > GOAL_NUM_FEATURES)
					terminateProcess = false;	// Should keep going
			}
		}
		
		// Obtain the rankings of feature indices per player
		final List<List<ScoredInt>> scoredIndicesPerPlayer = new ArrayList<List<ScoredInt>>();
		scoredIndicesPerPlayer.add(null);
		
		for (int p = 1; p <= numPlayers; ++p)
		{
			final List<ScoredInt> scoredIndices = new ArrayList<ScoredInt>();
			for (int i = 0; i < remainingFeaturesPerPlayer[p].size(); ++i)
			{
				final int fIdx = remainingFeaturesPerPlayer[p].getQuick(i);
				scoredIndices.add(new ScoredInt(fIdx, playerFeatureScores[p][fIdx].getMean()));
			}
			Collections.sort(scoredIndices, ScoredInt.DESCENDING);
			scoredIndicesPerPlayer.add(scoredIndices);
		}
		
		// Print initial rankings of features for all players
		try (final PrintWriter writer = new PrintWriter(outDir + "RankedFeatures.txt", "UTF-8"))
		{
			for (int p = 1; p <= numPlayers; ++p)
			{
				writer.println("Scores for Player " + p);
				
				for (final ScoredInt scoredIdx : scoredIndicesPerPlayer.get(p))
				{
					final int fIdx = scoredIdx.object();
					writer.println
					(
						"Feature=" + candidateFeaturesPerPlayer.get(p).get(fIdx) + 
						",weight=" + candidateFeatureWeightsPerPlayer[p].get(fIdx) + 
						",score=" + playerFeatureScores[p][fIdx].getMean()
					);
				}
	
				writer.println();
			}
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}
		
		// Now we're gonna try to build bigger featuresets that beat smaller ones
		final BaseFeatureSet[] bestFeatureSetPerPlayer = new BaseFeatureSet[numPlayers + 1];
		final LinearFunction[] bestLinFuncPerPlayer = new LinearFunction[numPlayers + 1];
		
		for (int p = 1; p <= numPlayers; ++p)
		{
			final int fIdx = scoredIndicesPerPlayer.get(p).get(0).object();
			bestFeatureSetPerPlayer[p] = JITSPatterNetFeatureSet.construct(
					Arrays.asList(candidateFeaturesPerPlayer.get(p).get(fIdx)));
			bestLinFuncPerPlayer[p] = new LinearFunction(
					new WeightVector(FVector.wrap(new float[] {candidateFeatureWeightsPerPlayer[p].get(fIdx)})));
		}
		
		// TODO loop
			// TODO evaluate baseline against opponents' baselines
			// TODO evaluate baseline + next-best-ranked-feature against opponents' baselines
			// TODO If better, add that feature
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param candidateFeatureSet
	 * @param originalFeatureSet
	 * @param oracleFunction
	 * @param experienceBuffer
	 * @return Vector of weights, with one weight for every feature in the candidate feature set.
	 */
	private static FVector computeCandidateFeatureWeights
	(
		final BaseFeatureSet candidateFeatureSet, 
		final BaseFeatureSet originalFeatureSet, 
		final LinearFunction oracleFunction, 
		final ExperienceBuffer experienceBuffer
	)
	{
		final WeightVector oracleWeightVector = oracleFunction.effectiveParams();
		final ExItExperience[] samples = experienceBuffer.allExperience();
		final List<FeatureVector> allCandidateFeatureVectors = new ArrayList<FeatureVector>();
		final TFloatArrayList allTargetLogits = new TFloatArrayList();
		
		for (final ExItExperience sample : samples)
		{
			if (sample != null && sample.moves().size() > 1)
			{
				final FeatureVector[] featureVectors = sample.generateFeatureVectors(originalFeatureSet);
				final FeatureVector[] candidateFeatureVectors = sample.generateFeatureVectors(candidateFeatureSet);
				final float[] logits = new float[featureVectors.length];

				for (int i = 0; i < featureVectors.length; ++i)
				{
					final FeatureVector featureVector = featureVectors[i];
					logits[i] = oracleWeightVector.dot(featureVector);
				}
				
				final float maxLogit = ArrayUtils.max(logits);
				final float minLogit = ArrayUtils.min(logits);
				
				if (maxLogit == minLogit)
					continue;		// Nothing to learn from this, just skip it
				
				// Maximise logits for winning moves and minimise for losing moves
				for (int i = sample.winningMoves().nextSetBit(0); i >= 0; i = sample.winningMoves().nextSetBit(i + 1))
				{
					logits[i] = maxLogit;
				}
				
				for (int i = sample.losingMoves().nextSetBit(0); i >= 0; i = sample.losingMoves().nextSetBit(i + 1))
				{
					logits[i] = minLogit;
				}
				
				for (int i = 0; i < candidateFeatureVectors.length; ++i)
				{
					allCandidateFeatureVectors.add(candidateFeatureVectors[i]);
					allTargetLogits.add(logits[i]);
				}
			}
		}
		
		final FVector candidateFeatureWeights = new FVector(candidateFeatureSet.getNumFeatures());
		final IncrementalStats[] logitStatsPerFeatureWhenTrue = new IncrementalStats[candidateFeatureWeights.dim()];
		final IncrementalStats[] logitStatsPerFeatureWhenFalse = new IncrementalStats[candidateFeatureWeights.dim()];
		final IncrementalStats totalAvgLogit = new IncrementalStats();
		
		for (int i = 0; i < logitStatsPerFeatureWhenTrue.length; ++i)
		{
			logitStatsPerFeatureWhenTrue[i] = new IncrementalStats();
			logitStatsPerFeatureWhenFalse[i] = new IncrementalStats();
		}
		
		final int numAspatialFeatures = candidateFeatureSet.getNumAspatialFeatures();
		final int numSpatialFeatures = candidateFeatureSet.getNumSpatialFeatures();
		
		for (int i = 0; i < allCandidateFeatureVectors.size(); ++i)
		{
			final FeatureVector featureVector = allCandidateFeatureVectors.get(i);
			final float targetLogit = allTargetLogits.getQuick(i);
			totalAvgLogit.observe(targetLogit);
			
			final FVector aspatialFeatureValues = featureVector.aspatialFeatureValues();
			final TIntArrayList activeSpatialFeatureIndices = featureVector.activeSpatialFeatureIndices();
			
			for (int j = 0; j < aspatialFeatureValues.dim(); ++j)
			{
				if (aspatialFeatureValues.get(j) != 0.f)
					logitStatsPerFeatureWhenTrue[j].observe(targetLogit);
				else
					logitStatsPerFeatureWhenFalse[j].observe(targetLogit);
			}
			
			boolean[] spatialActive = new boolean[numSpatialFeatures];
			
			for (int j = 0; j < activeSpatialFeatureIndices.size(); ++j)
			{
				final int fIdx = activeSpatialFeatureIndices.getQuick(j);
				logitStatsPerFeatureWhenTrue[fIdx + numAspatialFeatures].observe(targetLogit);
				spatialActive[fIdx] = true;
			}
			
			for (int j = 0; j < numSpatialFeatures; ++j)
			{
				if (!spatialActive[j])
					logitStatsPerFeatureWhenFalse[j + numAspatialFeatures].observe(targetLogit);
			}
		}
		
		for (int i = 0; i < candidateFeatureWeights.dim(); ++i)
		{
			candidateFeatureWeights.set(i, (float) (logitStatsPerFeatureWhenTrue[i].getMean() - totalAvgLogit.getMean()));
		}
		
		return candidateFeatureWeights;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Collects all features under a given decision tree node
	 * @param node Root of (sub)tree from which to collect features
	 * @param outList List in which to place features
	 */
	private static void collectFeatures(final DecisionTreeNode node, final List<Feature> outList)
	{
		if (node instanceof DecisionConditionNode)
		{
			final DecisionConditionNode conditionNode = (DecisionConditionNode) node;
			outList.add(conditionNode.feature());
			
			collectFeatures(conditionNode.trueNode(), outList);
			collectFeatures(conditionNode.falseNode(), outList);
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Loads feature sets and weights into the provided arrays
	 * @param game
	 * @param trainingOutDirPath
	 * @param outFeatureSets
	 * @param outLinearFunctionsPlayout
	 * @param outLinearFunctionsTSPG
	 */
	private static void loadFeaturesAndWeights
	(
		final Game game,
		final String trainingOutDirPath,
		final BaseFeatureSet[] outFeatureSets,
		final LinearFunction[] outLinearFunctionsPlayout,
		final LinearFunction[] outLinearFunctionsTSPG
	)
	{
		// First Playout policy
		{
			// Construct a string to load an MCTS guided by features, from that we can then easily extract the
			// features again afterwards
			final StringBuilder playoutSb = new StringBuilder();
			playoutSb.append("playout=softmax");
	
			for (int p = 1; p <= game.players().count(); ++p)
			{
				final String playoutPolicyFilepath = 
						ExperimentFileUtils.getLastFilepath
						(
							trainingOutDirPath + "PolicyWeightsPlayout_P" + p, 
							"txt"
						);
	
				playoutSb.append(",policyweights" + p + "=" + playoutPolicyFilepath);
			}
	
			final StringBuilder selectionSb = new StringBuilder();
			selectionSb.append("learned_selection_policy=playout");
	
			final String agentStr = StringRoutines.join
					(
						";", 
						"algorithm=MCTS",
						"selection=noisyag0selection",
						playoutSb.toString(),
						"final_move=robustchild",
						"tree_reuse=true",
						selectionSb.toString(),
						"friendly_name=BiasedMCTS"
					);
	
			final MCTS mcts = (MCTS) AIFactory.createAI(agentStr);
			final SoftmaxPolicyLinear playoutSoftmax = (SoftmaxPolicyLinear) mcts.playoutStrategy();
	
			final BaseFeatureSet[] featureSets = playoutSoftmax.featureSets();
			final LinearFunction[] linearFunctions = playoutSoftmax.linearFunctions();
	
			playoutSoftmax.initAI(game, -1);
			
			System.arraycopy(featureSets, 0, outFeatureSets, 0, featureSets.length);
			System.arraycopy(linearFunctions, 0, outLinearFunctionsPlayout, 0, linearFunctions.length);
		}
		
		// Repeat the entire process again for TSPG
		{
			// Construct a string to load an MCTS guided by features, from that we can then easily extract the
			// features again afterwards
			final StringBuilder playoutSb = new StringBuilder();
			playoutSb.append("playout=softmax");
	
			for (int p = 1; p <= game.players().count(); ++p)
			{
				final String playoutPolicyFilepath = 
						ExperimentFileUtils.getLastFilepath
						(
							trainingOutDirPath + "PolicyWeightsTSPG_P" + p, 
							"txt"
						);
	
				playoutSb.append(",policyweights" + p + "=" + playoutPolicyFilepath);
			}
	
			playoutSb.append(",boosted=true");
	
			final StringBuilder selectionSb = new StringBuilder();
			selectionSb.append("learned_selection_policy=playout");
	
			final String agentStr = StringRoutines.join
					(
						";", 
						"algorithm=MCTS",
						"selection=noisyag0selection",
						playoutSb.toString(),
						"final_move=robustchild",
						"tree_reuse=true",
						selectionSb.toString(),
						"friendly_name=BiasedMCTS"
					);
	
			final MCTS mcts = (MCTS) AIFactory.createAI(agentStr);
			final SoftmaxPolicyLinear playoutSoftmax = (SoftmaxPolicyLinear) mcts.playoutStrategy();
	
			final LinearFunction[] linearFunctions = playoutSoftmax.linearFunctions();
	
			playoutSoftmax.initAI(game, -1);
			
			System.arraycopy(linearFunctions, 0, outLinearFunctionsTSPG, 0, linearFunctions.length);
		}
	}
	
	/**
	 * Loads experience buffers into the provided array
	 * @param game
	 * @param trainingOutDirPath
	 * @param experienceBuffers
	 */
	private static void loadExperienceBuffers
	(
		final Game game,
		final String trainingOutDirPath,
		final ExperienceBuffer[] experienceBuffers
	)
	{
		for (int p = 1; p < experienceBuffers.length; ++p)
		{
			// Load experience buffer for Player p
			final String bufferFilepath = 
					ExperimentFileUtils.getLastFilepath
					(
						trainingOutDirPath + 
						"ExperienceBuffer_P" + p, 
						"buf"
					);

			ExperienceBuffer buffer = null;
			try
			{
				buffer = PrioritizedReplayBuffer.fromFile(game, bufferFilepath);
			}
			catch (final Exception e)
			{
				if (buffer == null)
				{
					try
					{
						buffer = UniformExperienceBuffer.fromFile(game, bufferFilepath);
					}
					catch (final Exception e2)
					{
						e.printStackTrace();
						e2.printStackTrace();
					}
				}
			}
			
			experienceBuffers[p] = buffer;
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Main method to generate all our scripts
	 * @param args
	 */
	public static void main(final String[] args)
	{
		// define options for arg parser
		final CommandLineArgParse argParse = 
				new CommandLineArgParse
				(
					true,
					"Identift top features for a game."
				);
		
		argParse.addOption(new ArgOption()
				.withNames("--training-out-dir")
				.help("Directory with training results (features, weights, experience buffers, ...).")
				.withNumVals(1)
				.withType(OptionTypes.String)
				.setRequired());
		
		argParse.addOption(new ArgOption()
				.withNames("--out-dir")
				.help("Directory in which to write our outputs.")
				.withNumVals(1)
				.withType(OptionTypes.String)
				.setRequired());
		
		argParse.addOption(new ArgOption()
				.withNames("--game")
				.help("Name of the game.")
				.withNumVals(1)
				.withType(OptionTypes.String)
				.setRequired());
		
		// parse the args
		if (!argParse.parseArguments(args))
			return;
		
		identifyTopFeatures(argParse);
	}
	
	//-------------------------------------------------------------------------

}
