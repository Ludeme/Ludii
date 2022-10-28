package training.expert_iteration;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

import features.FeatureVector;
import features.WeightVector;
import features.feature_sets.BaseFeatureSet;
import features.feature_sets.network.JITSPatterNetFeatureSet;
import features.generation.AtomicFeatureGenerator;
import function_approx.BoostedLinearFunction;
import function_approx.LinearFunction;
import game.Game;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.array.TLongArrayList;
import main.CommandLineArgParse;
import main.CommandLineArgParse.ArgOption;
import main.CommandLineArgParse.OptionTypes;
import main.FileHandling;
import main.StringRoutines;
import main.collections.FVector;
import main.collections.FastArrayList;
import main.grammar.Report;
import metadata.ai.heuristics.Heuristics;
import optimisers.Optimiser;
import optimisers.OptimiserFactory;
import other.GameLoader;
import other.RankUtils;
import other.context.Context;
import other.context.TempContext;
import other.move.Move;
import other.trial.Trial;
import policies.softmax.SoftmaxPolicyLinear;
import search.mcts.MCTS;
import training.expert_iteration.gradients.Gradients;
import training.expert_iteration.menageries.Menagerie;
import training.expert_iteration.menageries.Menagerie.DrawnAgentsData;
import training.expert_iteration.menageries.NaiveSelfPlay;
import training.expert_iteration.menageries.TournamentMenagerie;
import training.expert_iteration.params.AgentsParams;
import training.expert_iteration.params.FeatureDiscoveryParams;
import training.expert_iteration.params.GameParams;
import training.expert_iteration.params.ObjectiveParams;
import training.expert_iteration.params.OptimisersParams;
import training.expert_iteration.params.OutParams;
import training.expert_iteration.params.OutParams.CheckpointTypes;
import training.expert_iteration.params.TrainingParams;
import training.feature_discovery.CorrelationBasedExpander;
import training.feature_discovery.CorrelationErrorSignExpander;
import training.feature_discovery.FeatureSetExpander;
import training.feature_discovery.RandomExpander;
import training.feature_discovery.SpecialMovesCorrelationExpander;
import training.policy_gradients.Reinforce;
import utils.AIUtils;
import utils.ExperimentFileUtils;
import utils.ExponentialMovingAverage;
import utils.data_structures.experience_buffers.ExperienceBuffer;
import utils.data_structures.experience_buffers.PrioritizedReplayBuffer;
import utils.data_structures.experience_buffers.UniformExperienceBuffer;
import utils.experiments.InterruptableExperiment;

/**
 * Implementation of the Expert Iteration self-play training framework,
 * with additional support for feature learning instead of the standard
 * DNNs (see our various papers).
 * 
 * Currently, this is a sequential implementation, where experience generation
 * and training are all performed on a single thread.
 * 
 * @author Dennis Soemers
 */
public class ExpertIteration
{
	
	//-------------------------------------------------------------------------
	
	/** Format used for checkpoints based on training game count */
	private static final String gameCheckpointFormat = "%s_%05d.%s";
	
	/** Format used for checkpoints based on weight update count */
	private static final String weightUpdateCheckpointFormat = "%s_%08d.%s";
	
	//-------------------------------------------------------------------------
	
	/** Game configuration */
	protected final GameParams gameParams = new GameParams();
	
	/** Agents configuration */
	protected final AgentsParams agentsParams = new AgentsParams();
	
	/** Basic training params */
	protected final TrainingParams trainingParams = new TrainingParams();
	
	/** Feature discovery params */
	protected final FeatureDiscoveryParams featureDiscoveryParams = new FeatureDiscoveryParams();
	
	/** Objective function(s) params */
	protected final ObjectiveParams objectiveParams = new ObjectiveParams();
	
	/** Optimiser(s) params */
	protected final OptimisersParams optimisersParams = new OptimisersParams();
	
	/** Output / file writing params */
	protected final OutParams outParams = new OutParams();

	/*
	 * Auxiliary experiment setup
	 */
	
	/** 
	 * Whether to create a small GUI that can be used to manually interrupt training run. 
	 * False by default. 
	 */
	protected boolean useGUI;
	
	/** Max wall time in minutes (or -1 for no limit) */
	protected int maxWallTime;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor. No GUI for interrupting experiment, no wall time limit.
	 */
	public ExpertIteration()
	{
		// Do nothing
	}

	/**
	 * Constructor. No wall time limit.
	 * @param useGUI
	 */
	public ExpertIteration(final boolean useGUI)
	{
		this.useGUI = useGUI;
	}
	
	/**
	 * Constructor
	 * @param useGUI
	 * @param maxWallTime Wall time limit in minutes.
	 */
	public ExpertIteration(final boolean useGUI, final int maxWallTime)
	{
		this.useGUI = useGUI;
		this.maxWallTime = maxWallTime;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Starts the experiment
	 */
	public void startExperiment()
	{
		try (final PrintWriter logWriter = createLogWriter())
		{
			startTraining(logWriter);
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Start the training run
	 */
	private void startTraining(final PrintWriter logWriter)
	{
		final Game game;
		
		if (gameParams.ruleset != null && !gameParams.ruleset.equals(""))
			game = GameLoader.loadGameFromName(gameParams.gameName, gameParams.ruleset);
		else
			game = GameLoader.loadGameFromName(gameParams.gameName, gameParams.gameOptions);
		
		final int numPlayers = game.players().count();
		
		if (gameParams.gameLengthCap >= 0)
			game.setMaxTurns(Math.min(gameParams.gameLengthCap, game.getMaxTurnLimit()));
				
		@SuppressWarnings("unused")
		final InterruptableExperiment experiment = new InterruptableExperiment(useGUI, maxWallTime)
		{
			
			//-----------------------------------------------------------------
			
			/** Last checkpoint for which we've saved files */
			protected long lastCheckpoint;
			
			/** Filenames corresponding to our current Feature Sets */
			protected String[] currentFeatureSetFilenames;
			
			/** Filenames corresponding to our current policy weights optimised for Selection */
			protected String[] currentPolicyWeightsSelectionFilenames;
			
			/** Filenames corresponding to our current policy weights optimised for Playouts */
			protected String[] currentPolicyWeightsPlayoutFilenames;
			
			/** Filenames corresponding to our current policy weights optimised for TSPG */
			protected String[] currentPolicyWeightsTSPGFilenames;
			
			/** Filenames corresponding to our current policy weights optimised for CE exploration */
			protected String[] currentPolicyWeightsCEEFilenames;
			
			/** Filename for our current heuristics and weights for value function */
			protected String currentValueFunctionFilename;
			
			/** Filenames corresponding to our current experience buffers */
			protected String[] currentExperienceBufferFilenames;
			
			/** Filenames corresponding to our current experience buffers for special moves */
			protected String[] currentSpecialMoveExperienceBufferFilenames;
			
			/** Filenames corresponding to our current experience buffers for final states */
			protected String[] currentFinalStatesExperienceBufferFilenames;
			
			/** Filenames corresponding to our current moving average trackers of game durations */
			protected String[] currentGameDurationTrackerFilenames;
			
			/** Filenames corresponding to our current optimisers for Selection policy */
			protected String[] currentOptimiserSelectionFilenames;
			
			/** Filenames corresponding to our current optimisers for Playout policy */
			protected String[] currentOptimiserPlayoutFilenames;
			
			/** Filenames corresponding to our current optimisers for TSPG */
			protected String[] currentOptimiserTSPGFilenames;
			
			/** Filenames corresponding to our current optimisers for CE exploration */
			protected String[] currentOptimiserCEEFilenames;
			
			/** Filename corresponding to our current optimiser for Value function */
			protected String currentOptimiserValueFilename;
			
			/**
			 * Init class members. Cant do this in field declarations because
			 * runExperiment() is called inside constructor of parent class.
			 */
			private void initMembers()
			{
				lastCheckpoint = Long.MAX_VALUE;
				currentFeatureSetFilenames = new String[numPlayers + 1];
				currentPolicyWeightsSelectionFilenames = new String[numPlayers + 1];
				currentPolicyWeightsPlayoutFilenames = new String[numPlayers + 1];
				currentPolicyWeightsTSPGFilenames = new String[numPlayers + 1];
				currentPolicyWeightsCEEFilenames = new String[numPlayers + 1];
				currentValueFunctionFilename = null;
				currentExperienceBufferFilenames = new String[numPlayers + 1];
				currentSpecialMoveExperienceBufferFilenames = new String[numPlayers + 1];
				currentFinalStatesExperienceBufferFilenames = new String[numPlayers + 1];
				currentGameDurationTrackerFilenames = new String[numPlayers + 1];
				currentOptimiserSelectionFilenames = new String[numPlayers + 1];
				currentOptimiserPlayoutFilenames = new String[numPlayers + 1];
				currentOptimiserTSPGFilenames = new String[numPlayers + 1];
				currentOptimiserCEEFilenames = new String[numPlayers + 1];
				currentOptimiserValueFilename = null;
			}
			
			//-----------------------------------------------------------------

			@Override
			public void runExperiment()
			{
				if (outParams.outDir == null)
					System.err.println("Warning: we're not writing any output files for this run!");
				else if (!outParams.outDir.exists())
					outParams.outDir.mkdirs();
				
				initMembers();
				
				// Don't want our MCTSes to null undo data in Trial objects, we need that data sometimes
				MCTS.NULL_UNDO_DATA = false;
				
				// TODO add log statements describing complete setup of experiment
				
				// Create menagerie
				final Menagerie menagerie;
				
				if (agentsParams.tournamentMode)
					menagerie = new TournamentMenagerie();
				else
					menagerie = new NaiveSelfPlay();
				
				// Prepare our feature sets
				BaseFeatureSet[] featureSets = prepareFeatureSets();
				
				// For every feature set, a list for every feature of its lifetime (how often it could've been active)
				final TLongArrayList[] featureLifetimes = new TLongArrayList[featureSets.length];
				// For every feature set, a list for every feature of the ratio of cases in which it was active
				final TDoubleArrayList[] featureActiveRatios = new TDoubleArrayList[featureSets.length];
				// For every feature set, a list for every feature of how frequently we observed it being active
				final TLongArrayList[] featureOccurrences = new TLongArrayList[featureSets.length];
				// For every feature set, a BitSet containing features that are (or could be) 100% winning moves
				final BitSet[] winningMovesFeatures = new BitSet[featureSets.length];
				// For every feature set, a BitSet containing features that are (or could be) 100% losing moves
				final BitSet[] losingMovesFeatures = new BitSet[featureSets.length];
				// For every feature set, a BitSet containing features that are (or could be) anti-moves for subsequent opponent moves that defeat us
				// (like anti-decisive moves, but in games with more than 2 players the distinction between anti-opponent-winning vs. anti-us-losing
				// is important)
				final BitSet[] antiDefeatingMovesFeatures = new BitSet[featureSets.length];
				
				for (int i = 0; i < featureSets.length; ++i)
				{
					if (featureSets[i] != null)
					{
						final TLongArrayList featureLifetimesList = new TLongArrayList();
						featureLifetimesList.fill(0, featureSets[i].getNumSpatialFeatures(), 0L);
						featureLifetimes[i] = featureLifetimesList;
						
						final TDoubleArrayList featureActiveRatiosList = new TDoubleArrayList();
						featureActiveRatiosList.fill(0, featureSets[i].getNumSpatialFeatures(), 0.0);
						featureActiveRatios[i] = featureActiveRatiosList;
						
						final TLongArrayList featureOccurrencesList = new TLongArrayList();
						featureOccurrencesList.fill(0, featureSets[i].getNumSpatialFeatures(), 0L);
						featureOccurrences[i] = featureOccurrencesList;
						
						final BitSet winningMovesSet = new BitSet();
						winningMovesSet.set(0, featureSets[i].getNumSpatialFeatures());
						winningMovesFeatures[i] = winningMovesSet;
						
						final BitSet losingMovesSet = new BitSet();
						losingMovesSet.set(0, featureSets[i].getNumSpatialFeatures());
						losingMovesFeatures[i] = losingMovesSet;
						
						final BitSet antiDefeatingMovesSet = new BitSet();
						antiDefeatingMovesSet.set(0, featureSets[i].getNumSpatialFeatures());
						antiDefeatingMovesFeatures[i] = antiDefeatingMovesSet;
					}
				}
				
				// prepare our linear functions
				final LinearFunction[] selectionFunctions = prepareSelectionFunctions(featureSets);
				final LinearFunction[] playoutFunctions = preparePlayoutFunctions(featureSets);
				final LinearFunction[] tspgFunctions = prepareTSPGFunctions(featureSets, selectionFunctions);
				
				// create our policies
				final SoftmaxPolicyLinear selectionPolicy = 
						new SoftmaxPolicyLinear
						(
							selectionFunctions, 
							featureSets,
							agentsParams.maxNumBiasedPlayoutActions
						);
				
				final SoftmaxPolicyLinear playoutPolicy = 
						new SoftmaxPolicyLinear
						(
							playoutFunctions, 
							featureSets,
							agentsParams.maxNumBiasedPlayoutActions
						);
				
				final SoftmaxPolicyLinear tspgPolicy = 
						new SoftmaxPolicyLinear
						(
							tspgFunctions, 
							featureSets,
							agentsParams.maxNumBiasedPlayoutActions
						);
				
				final FeatureSetExpander featureSetExpander;
				switch (featureDiscoveryParams.expanderType)
				{
				case "CorrelationBasedExpander":
					featureSetExpander = new CorrelationBasedExpander();
					break;
				case "CorrelationErrorSignExpander":
					featureSetExpander = new CorrelationErrorSignExpander();
					break;
				case "Random":
					featureSetExpander = new RandomExpander();
					break;
				default:
					System.err.println("Did not recognise feature set expander type: " + featureDiscoveryParams.expanderType);
					return;
				}
				
				final FeatureSetExpander specialMovesExpander = new SpecialMovesCorrelationExpander();
				
				// create our value function
				final Heuristics valueFunction = prepareValueFunction();
				
				// construct optimisers
				final Optimiser[] selectionOptimisers = prepareSelectionOptimisers();
				final Optimiser[] playoutOptimisers = preparePlayoutOptimisers();
				final Optimiser[] tspgOptimisers = prepareTSPGOptimisers();
				final Optimiser valueFunctionOptimiser = prepareValueFunctionOptimiser();
				
				// Initialise menagerie's population
				menagerie.initialisePopulation
				(
					game, agentsParams, 
					AIUtils.generateFeaturesMetadata(selectionPolicy, playoutPolicy), 
					Heuristics.copy(valueFunction)
				);
				
				// instantiate trial / context
				final Trial trial = new Trial(game);
				final Context context = new Context(game, trial);
				
				// Prepare our replay buffers (we use one per player)
				final ExperienceBuffer[] experienceBuffers = prepareExperienceBuffers(trainingParams.prioritizedExperienceReplay);
				final ExperienceBuffer[] specialMoveExperienceBuffers = prepareSpecialMoveExperienceBuffers();
				
				// Keep track of average game duration (separate per player) 
				final ExponentialMovingAverage[] avgGameDurations = prepareGameDurationTrackers();
				
				// Our big game-playing loop
				long actionCounter = 0L;
				long weightsUpdateCounter = (outParams.checkpointType == CheckpointTypes.WeightUpdate) ? lastCheckpoint : 0L;

				int gameCounter = 0;

				if (outParams.checkpointType == CheckpointTypes.Game && lastCheckpoint >= 0L)
				{
					gameCounter = (int)lastCheckpoint;
					trainingParams.numTrainingGames += lastCheckpoint;
				}
				else if (trainingParams.numPolicyGradientEpochs > 0)
				{
					// Start out with some policy gradients
					featureSets = Reinforce.runSelfPlayPG
					(
						game, 
						selectionPolicy,
						playoutPolicy, 
						tspgPolicy,
						featureSets, 
						featureSetExpander, 
						instantiateCrossEntropyOptimisers(), 
						objectiveParams, 
						featureDiscoveryParams, 
						trainingParams, 
						logWriter, 
						this
					);
					
					// Scale down all the policy weights obtained from PG
					for (int i = 0; i < playoutPolicy.linearFunctions().length; ++i)
					{
						final LinearFunction linFunc = playoutPolicy.linearFunctions()[i];
						if (linFunc == null)
							continue;
						
						final FVector weights = linFunc.trainableParams().allWeights();
						weights.mult((float) trainingParams.postPGWeightScalar);
						
						// Also copy the weights over into selection policy
						selectionPolicy.linearFunctions()[i].trainableParams().allWeights().copyFrom(weights, 0, 0, weights.dim());
					}
					
					for (int p = 1; p <= numPlayers; ++p)
					{
						// Add new entries for lifetime, average activity, occurrences, and winning/losing/anti-defeating
						winningMovesFeatures[p].set(featureActiveRatios[p].size(), featureSets[p].getNumSpatialFeatures());
						losingMovesFeatures[p].set(featureActiveRatios[p].size(), featureSets[p].getNumSpatialFeatures());
						antiDefeatingMovesFeatures[p].set(featureActiveRatios[p].size(), featureSets[p].getNumSpatialFeatures());
						while (featureActiveRatios[p].size() < featureSets[p].getNumSpatialFeatures())
						{
							featureActiveRatios[p].add(0.0);
							featureLifetimes[p].add(0L);
							featureOccurrences[p].add(0L);
						}
					}
					
//					// Reset all the weights (just keep features, untrained)
//					for (final LinearFunction func : cePolicy.linearFunctions())
//					{
//						if (func != null)
//							func.trainableParams().allWeights().mult(0.f);
//					}
				}
				
				for (/**/; gameCounter < trainingParams.numTrainingGames; ++gameCounter)
				{
					checkWallTime(0.05);
					
					if (interrupted) // time to abort the experiment
					{
						logLine(logWriter, "interrupting experiment...");
						break;
					}
					
					final int thisIterationGameCounter = gameCounter;
					
					saveCheckpoints
					(
						gameCounter, 
						weightsUpdateCounter, 
						featureSets, 
						selectionFunctions, 
						playoutFunctions,
						tspgFunctions,
						valueFunction,
						experienceBuffers,
						specialMoveExperienceBuffers,
						selectionOptimisers,
						tspgOptimisers,
						valueFunctionOptimiser,
						avgGameDurations,
						featureOccurrences,
						winningMovesFeatures,
						losingMovesFeatures,
						antiDefeatingMovesFeatures,
						false
					);

					if 
					(
						!featureDiscoveryParams.noGrowFeatureSet 
						&& 
						gameCounter > 0 
						&& 
						gameCounter % featureDiscoveryParams.addFeatureEvery == 0
					)
					{
						final BaseFeatureSet[] expandedFeatureSets = new BaseFeatureSet[numPlayers + 1];
						final ExecutorService threadPool = Executors.newFixedThreadPool(featureDiscoveryParams.numFeatureDiscoveryThreads);
						final CountDownLatch latch = new CountDownLatch(numPlayers);
						for (int pIdx = 1; pIdx <= numPlayers; ++pIdx)
						{
							final int p = pIdx;
							final BaseFeatureSet featureSetP = featureSets[p];
							threadPool.submit
							(
								() ->
								{
									try
									{
										// We'll sample a batch from our replay buffer, and grow feature set
										final int batchSize = trainingParams.batchSize;
										final List<ExItExperience> batch = experienceBuffers[p].sampleExperienceBatchUniformly(batchSize);
										
										final List<ExItExperience> specialMovesBatch = 
												specialMoveExperienceBuffers[p].sampleExperienceBatchUniformly(batchSize);
										
										final long startTime = System.currentTimeMillis();
										final BaseFeatureSet expandedFeatureSet;
										if (!featureDiscoveryParams.useSpecialMovesExpanderSplit || thisIterationGameCounter % 2 != 0)
										{
											expandedFeatureSet = 
													expandFeatureSet
													(
														batch, featureSetExpander, featureSetP, p, selectionPolicy, 
														featureActiveRatios[p], featureLifetimes[p], featureOccurrences[p],
														winningMovesFeatures[p], losingMovesFeatures[p], antiDefeatingMovesFeatures[p]
													);
										}
										else
										{
											expandedFeatureSet = 
													expandFeatureSet
													(
														specialMovesBatch, specialMovesExpander, featureSetP, p, selectionPolicy, 
														featureActiveRatios[p], featureLifetimes[p], featureOccurrences[p],
														winningMovesFeatures[p], losingMovesFeatures[p], antiDefeatingMovesFeatures[p]
													);
										}
											
										
										// And try again to expand for special moves
										final BaseFeatureSet toExpand = (expandedFeatureSet != null) ? expandedFeatureSet : featureSetP;
										
										final BaseFeatureSet specialMovesExpandedFeatureSet;
										
										if (featureDiscoveryParams.useSpecialMovesExpander)
										{
											specialMovesExpandedFeatureSet = 
													expandFeatureSet
													(
														specialMovesBatch, specialMovesExpander, toExpand, p, selectionPolicy, 
														featureActiveRatios[p], featureLifetimes[p], featureOccurrences[p],
														winningMovesFeatures[p], losingMovesFeatures[p], antiDefeatingMovesFeatures[p]
													);
										}
										else
										{
											specialMovesExpandedFeatureSet = null;
										}
										
										final BaseFeatureSet newFeatureSet;
										if (specialMovesExpandedFeatureSet != null)
											newFeatureSet = specialMovesExpandedFeatureSet;
										else if (expandedFeatureSet != null)
											newFeatureSet = expandedFeatureSet;
										else
											newFeatureSet = null;
										
										if (newFeatureSet != null)
										{
											expandedFeatureSets[p] = expandedFeatureSet;
											
											logLine
											(
												logWriter,
												"Expanded feature set in " + (System.currentTimeMillis() - startTime) + " ms for P" + p + "."
											);
											//System.out.println("Expanded feature set in " + (System.currentTimeMillis() - startTime) + " ms for P" + p + ".");
										}
										else
										{
											expandedFeatureSets[p] = featureSetP;
										}
									}
									catch (final Exception e)
									{
										e.printStackTrace();
									}
									finally
									{
										latch.countDown();
									}
								}
							);	
						}
						
						try
						{
							latch.await();
						} 
						catch (final InterruptedException e)
						{
							e.printStackTrace();
						}
						threadPool.shutdown();

						selectionPolicy.updateFeatureSets(expandedFeatureSets);
						playoutPolicy.updateFeatureSets(expandedFeatureSets);
						menagerie.updateDevFeatures(AIUtils.generateFeaturesMetadata(selectionPolicy, playoutPolicy));
						
						if (objectiveParams.trainTSPG)
							tspgPolicy.updateFeatureSets(expandedFeatureSets);
						
						featureSets = expandedFeatureSets;
					}
					
					logLine(logWriter, "starting game " + (gameCounter + 1));
					
					// play a game
					game.start(context);
					
					// here we'll collect all tuples of experience during this game
					final List<List<ExItExperience>> gameExperienceSamples = new ArrayList<List<ExItExperience>>(numPlayers + 1);
					gameExperienceSamples.add(null);
					
					// Get agents from menagerie
					final DrawnAgentsData drawnExperts = menagerie.drawAgents(game, agentsParams);
					final List<ExpertPolicy> experts = drawnExperts.getAgents();
					
					for (int p = 1; p < experts.size(); ++p)
					{
						if (experts.get(p) instanceof MCTS)
						{
							((MCTS)experts.get(p)).setNumThreads(agentsParams.numAgentThreads);
							((MCTS)experts.get(p)).setUseScoreBounds(true);
						}
						
						experts.get(p).initAI(game, p);
						gameExperienceSamples.add(new ArrayList<ExItExperience>());
						
						if (objectiveParams.trainTSPG && !(experts.get(p) instanceof MCTS))
							System.err.println("A non-MCTS expert cannot be used for training the TSPG objective!");
					}
					
					// init some stuff for CE exploration
					double ceExploreCurrISWeight = 1.0;
					final List<FVector> ceExploreGradientVectors = new ArrayList<FVector>();
					final TIntArrayList ceExploreMovers = new TIntArrayList();
					final TFloatArrayList ceExploreRewards = new TFloatArrayList();
					
					while (!context.trial().over())
					{
						if (interrupted) // time to abort the experiment
						{
							logLine(logWriter, "interrupting experiment...");
							break;
						}
						
						// have expert choose action
						final int mover = context.state().mover();
						final ExpertPolicy expert = experts.get(context.state().playerToAgent(mover));
						
						expert.selectAction
						(
							game, 
							expert.copyContext(context), 
							agentsParams.thinkingTime,
							agentsParams.iterationLimit,
							agentsParams.depthLimit
						);

						final FastArrayList<Move> legalMoves = new FastArrayList<Move>();
						for (final Move legalMove : expert.lastSearchRootMoves())
						{
							legalMoves.add(legalMove);
						}

						final FVector expertDistribution = expert.computeExpertPolicy(1.0);

						final Move move = legalMoves.get(expertDistribution.sampleProportionally());	
							
						// Collect experiences for this game (don't store in buffer yet, don't know episode duration or value)
						final List<ExItExperience> newExperiences = expert.generateExItExperiences();
						
						for (final ExItExperience newExperience : newExperiences)
						{
							final int experienceMover = newExperience.state().state().mover();
							
							if (valueFunction != null)
								newExperience.setStateFeatureVector(valueFunction.computeStateFeatureVector(newExperience.context(), experienceMover));
							
							// Update feature lifetimes, active ratios, winning/losing/anti-defeating features, etc.
							updateFeatureActivityData
							(
								newExperience.context(), experienceMover, featureSets, 
								featureLifetimes, featureActiveRatios, featureOccurrences, 
								winningMovesFeatures, losingMovesFeatures, antiDefeatingMovesFeatures,
								newExperience
							);
							
							gameExperienceSamples.get(experienceMover).add(newExperience);
						}

						// Apply chosen action
						game.apply(context, move);
						++actionCounter;
						
						if (actionCounter % trainingParams.updateWeightsEvery == 0)
						{
							// Time to update our weights a bit (once for every player-specific model)
							final int batchSize = trainingParams.batchSize;
							for (int p = 1; p <= numPlayers; ++p)
							{
								final List<ExItExperience> batch = experienceBuffers[p].sampleExperienceBatch(batchSize);

								if (batch.size() == 0)
									continue;
								
								final List<FVector> gradientsSelection = new ArrayList<FVector>(batch.size());
								final List<FVector> gradientsPlayout = new ArrayList<FVector>(batch.size());
								final List<FVector> gradientsTSPG = new ArrayList<FVector>(batch.size());
								final List<FVector> gradientsCEExplore = new ArrayList<FVector>(batch.size());
								final List<FVector> gradientsValueFunction = new ArrayList<FVector>(batch.size());
								
								// for PER
								final int[] indices = new int[batch.size()];
								final float[] priorities = new float[batch.size()];
								
								// for WIS
								double sumImportanceSamplingWeights = 0.0;
								
								for (int idx = 0; idx < batch.size(); ++idx)
								{
									final ExItExperience sample = batch.get(idx);
									final FeatureVector[] featureVectors = 
											featureSets[p].computeFeatureVectors
											(
												sample.state().state(),
												sample.state().lastDecisionMove(),
												sample.moves(), 
												false
											);

									final FVector expertPolicy = sample.expertDistribution();
									
									// Note: NOT using sample.state().state().mover(), but p here, important to update
									// shared weights correctly!
									final FVector selectionErrors = 
											Gradients.computeCrossEntropyErrors
											(
												selectionPolicy, expertPolicy, featureVectors, p, objectiveParams.handleAliasing
											);
									final FVector playoutErrors = 
											Gradients.computeCrossEntropyErrors
											(
												playoutPolicy, expertPolicy, featureVectors, p, objectiveParams.handleAliasingPlayouts
											);

									final FVector selectionGradients = selectionPolicy.computeParamGradients
										(
											selectionErrors,
											featureVectors,
											p
										);
									final FVector playoutGradients = selectionPolicy.computeParamGradients
										(
											playoutErrors,
											featureVectors,
											p
										);
									
									final FVector valueGradients = Gradients.computeValueGradients(valueFunction, p, sample);

									double importanceSamplingWeight = sample.weightVisitCount();
									double nonImportanceSamplingWeight = 1.0;	// Also used to scale gradients, but doesn't count as IS
									
									if (objectiveParams.importanceSamplingEpisodeDurations)
										importanceSamplingWeight *= (avgGameDurations[sample.state().state().mover()].movingAvg() / sample.episodeDuration());
									
									if (trainingParams.prioritizedExperienceReplay)
									{
										final FVector absErrors = selectionErrors.copy();
										absErrors.abs();
										
										// Minimum priority of 0.05 to avoid crashes with 0-error samples
										priorities[idx] = Math.max(0.05f, absErrors.sum());
										importanceSamplingWeight *= sample.weightPER();
										indices[idx] = sample.bufferIdx();
									}
									
									sumImportanceSamplingWeights += importanceSamplingWeight;
									selectionGradients.mult((float) (importanceSamplingWeight * nonImportanceSamplingWeight));
									playoutGradients.mult((float) (importanceSamplingWeight * nonImportanceSamplingWeight));
									
									gradientsSelection.add(selectionGradients);
									gradientsPlayout.add(playoutGradients);
									
									if (valueGradients != null)
									{
										valueGradients.mult((float) importanceSamplingWeight); 
										gradientsValueFunction.add(valueGradients);
									}
									
									if (objectiveParams.trainTSPG && p > 0)
									{
										// and gradients for TSPG
										final FVector pi = 
												tspgPolicy.computeDistribution(featureVectors, sample.state().state().mover());
										final FVector expertQs = sample.expertValueEstimates();
										
										final FVector grads = new FVector(tspgFunctions[p].trainableParams().allWeights().dim());
										for (int i = 0; i < sample.moves().size(); ++i)
										{
											final float expertQ = expertQs.get(i);
											final float pi_sa = pi.get(i);
											
											for (int j = 0; j < sample.moves().size(); ++j)
											{
												final FeatureVector featureVector = featureVectors[j];
												
												// Dense representation for aspatial features
												final FVector aspatialFeatureVals = featureVector.aspatialFeatureValues();
												final int numAspatialFeatures = aspatialFeatureVals.dim();
												
												for (int k = 0; k < numAspatialFeatures; ++k)
												{
													if (i == j)
														grads.addToEntry(k, aspatialFeatureVals.get(k) * expertQ * pi_sa * (1.f - pi_sa));
													else
														grads.addToEntry(k, aspatialFeatureVals.get(k) * expertQ * pi_sa * (0.f - pi.get(j)));
												}
												
												// Sparse representation for spatial features (num aspatial features as offset for indexing)
												final TIntArrayList sparseSpatialFeatures = featureVector.activeSpatialFeatureIndices();
												
												for (int k = 0; k < sparseSpatialFeatures.size(); ++k)
												{
													final int feature = sparseSpatialFeatures.getQuick(k);
													
													if (i == j)
														grads.addToEntry(feature + numAspatialFeatures, expertQ * pi_sa * (1.f - pi_sa));
													else
														grads.addToEntry(feature + numAspatialFeatures, expertQ * pi_sa * (0.f - pi.get(j)));
												}
											}
										}

										grads.mult((float) (importanceSamplingWeight * nonImportanceSamplingWeight));
										gradientsTSPG.add(grads);
									}
								}
								
								final FVector meanGradientsSelection;
								final FVector meanGradientsPlayout;
								final FVector meanGradientsValue;
								final FVector meanGradientsTSPG;
								
								if (objectiveParams.weightedImportanceSampling)
								{
									// For WIS, we don't divide by number of vectors, but by sum of IS weights
									meanGradientsSelection = Gradients.wisGradients(gradientsSelection, (float)sumImportanceSamplingWeights);
									meanGradientsPlayout = Gradients.wisGradients(gradientsPlayout, (float)sumImportanceSamplingWeights);
									meanGradientsValue = Gradients.wisGradients(gradientsValueFunction, (float)sumImportanceSamplingWeights);
									meanGradientsTSPG = Gradients.wisGradients(gradientsTSPG, (float)sumImportanceSamplingWeights);
								}
								else
								{
									meanGradientsSelection = Gradients.meanGradients(gradientsSelection);
									meanGradientsPlayout = Gradients.meanGradients(gradientsPlayout);
									meanGradientsValue = Gradients.meanGradients(gradientsValueFunction);
									meanGradientsTSPG = Gradients.meanGradients(gradientsTSPG);
								}
								
								Gradients.minimise
								(
									selectionOptimisers[p], 
									selectionFunctions[p].trainableParams().allWeights(), 
									meanGradientsSelection, 
									(float)objectiveParams.weightDecayLambda
								);
								Gradients.minimise
								(
									playoutOptimisers[p], 
									playoutFunctions[p].trainableParams().allWeights(), 
									meanGradientsPlayout, 
									(float)objectiveParams.weightDecayLambda
								);
								
								menagerie.updateDevFeatures(AIUtils.generateFeaturesMetadata(selectionPolicy, playoutPolicy));
								
								if (meanGradientsValue != null && valueFunction != null)
								{
									final FVector valueFunctionParams = valueFunction.paramsVector();
									Gradients.minimise
									(
										valueFunctionOptimiser, 	// TODO dont we need separate one per player??????
										valueFunctionParams, 
										meanGradientsValue, 
										(float)objectiveParams.weightDecayLambda
									);
									
									valueFunction.updateParams(game, valueFunctionParams, 0);
									menagerie.updateDevHeuristics(Heuristics.copy(valueFunction));
								}
								
								if (objectiveParams.trainTSPG && p > 0)
								{
									// NOTE: maximise here instead of minimise!
									Gradients.maximise
									(
										tspgOptimisers[p], 
										tspgFunctions[p].trainableParams().allWeights(), 
										meanGradientsTSPG, 
										(float)objectiveParams.weightDecayLambda
									);
								}
								
								// update PER priorities
								if (trainingParams.prioritizedExperienceReplay && p > 0)
								{
									final PrioritizedReplayBuffer buffer = (PrioritizedReplayBuffer) experienceBuffers[p];
									buffer.setPriorities(indices, priorities);
								}
							}
							
							++weightsUpdateCounter;
						}
					}
					
					if (!interrupted)
					{
						// Game is over, we can now store all experience collected in the real buffers
						for (int p = 1; p <= numPlayers; ++p)
						{
							final List<ExItExperience> pExperience = gameExperienceSamples.get(p);

							// Note: not really game duration! Just from perspective of one player!
							final int gameDuration = pExperience.size();	// NOTE: technically wrong for non-root experiences
							
							avgGameDurations[p].observe(gameDuration);
							
//							// For WED we want to weigh this observation proportionally to the value of the observation itself
//							for (int i = 0; i < gameDuration; ++i)
//							{
//								avgGameDurations[p].observe(gameDuration);
//							}
							
							final double[] playerOutcomes = RankUtils.agentUtilities(context);
							
							// Shuffle experiences so they're no longer in chronological order
							Collections.shuffle(pExperience, ThreadLocalRandom.current());
							
							for (final ExItExperience experience : pExperience)
							{
								experience.setEpisodeDuration(gameDuration);
								experience.setPlayerOutcomes(playerOutcomes);
								experienceBuffers[p].add(experience);
								
								if 
								(
									!experience.winningMoves().isEmpty() 
									|| 
									!experience.losingMoves().isEmpty() 
									|| 
									!experience.antiDefeatingMoves().isEmpty()
								)
								{
									specialMoveExperienceBuffers[p].add(experience);
								}
							}
						}
					}
					
					if (context.trial().over())
					{
						// Menagerie may want to know about the outcome
						menagerie.updateOutcome(context, drawnExperts);
						
						logLine(logWriter, "Finished running game " + (gameCounter + 1));
					}
					
					for (int p = 1; p < experts.size(); ++p)
					{
						experts.get(p).closeAI();
					}
				}
				
				// Final forced save of checkpoints at end of run
				saveCheckpoints
				(
					gameCounter + 1, 
					weightsUpdateCounter, 
					featureSets, 
					selectionFunctions, 
					playoutFunctions,
					tspgFunctions,
					valueFunction,
					experienceBuffers,
					specialMoveExperienceBuffers,
					selectionOptimisers,
					tspgOptimisers,
					valueFunctionOptimiser,
					avgGameDurations,
					featureOccurrences,
					winningMovesFeatures,
					losingMovesFeatures,
					antiDefeatingMovesFeatures,
					true
				);
				
				final String menagerieLog = menagerie.generateLog();
				if (menagerieLog != null)
					logLine(logWriter, menagerie.generateLog());
			}
			
			//-----------------------------------------------------------------
			
			/**
			 * Updates data related to which features are active how often,
			 * their lifetimes, whether they are special types of moves 
			 * (like 100% winning moves), etc.
			 * 
			 * @param context
			 * @param mover
			 * @param featureSets
			 * @param featureLifetimes
			 * @param featureActiveRatios
			 * @param featureOccurrences
			 * @param winningMovesFeatures
			 * @param losingMovesFeatures
			 * @param antiDefeatingMovesFeatures
			 * @param experience
			 */
			private void updateFeatureActivityData
			(
				final Context context,
				final int mover,
				final BaseFeatureSet[] featureSets,
				final TLongArrayList[] featureLifetimes,
				final TDoubleArrayList[] featureActiveRatios,
				final TLongArrayList[] featureOccurrences,
				final BitSet[] winningMovesFeatures,
				final BitSet[] losingMovesFeatures,
				final BitSet[] antiDefeatingMovesFeatures,
				final ExItExperience experience
			)
			{
				final FastArrayList<Move> legalMoves = experience.moves();
				final TIntArrayList[] sparseFeatureVectors = 
						featureSets[mover].computeSparseSpatialFeatureVectors(context, legalMoves, false);

				for (final TIntArrayList featureVector : sparseFeatureVectors)
				{
					if (featureVector.isEmpty())
						continue;		// Probably a pass/swap/other special move, don't want these affecting our active ratios
					
					// Following code expects the indices in the sparse feature vector to be sorted
					featureVector.sort();

					// Increase lifetime of all features by 1
					featureLifetimes[mover].transformValues((final long l) -> {return l + 1L;});

					// Incrementally update all average feature values
					final TDoubleArrayList list = featureActiveRatios[mover];
					int vectorIdx = 0;
					for (int i = 0; i < list.size(); ++i)
					{
						final double oldMean = list.getQuick(i);

						if (vectorIdx < featureVector.size() && featureVector.getQuick(vectorIdx) == i)
						{
							// ith feature is active
							list.setQuick(i, oldMean + ((1.0 - oldMean) / featureLifetimes[mover].getQuick(i)));
							featureOccurrences[mover].setQuick(i, featureOccurrences[mover].getQuick(i) + 1);
							++vectorIdx;
						}
						else
						{
							// ith feature is not active
							list.setQuick(i, oldMean + ((0.0 - oldMean) / featureLifetimes[mover].getQuick(i)));
						}
					}

					if (vectorIdx != featureVector.size())
					{
						System.err.println("ERROR: expected vectorIdx == featureVector.size()!");
						System.err.println("vectorIdx = " + vectorIdx);
						System.err.println("featureVector.size() = " + featureVector.size());
						System.err.println("featureVector = " + featureVector);
					}
				}
				
				// Compute which moves (if any) are winning, losing, or anti-defeating
				final boolean[] isWinning = new boolean[legalMoves.size()];
				final boolean[] isLosing = new boolean[legalMoves.size()];
				final int[] numDefeatingResponses = new int[legalMoves.size()];
				int maxNumDefeatingResponses = 0;
				
				// For every legal move, a BitSet-representation of which features are active
				final BitSet[] activeFeatureBitSets = new BitSet[legalMoves.size()];
				
				for (int i = 0; i < legalMoves.size(); ++i)
				{
					// Compute BitSet representation of active features
					final TIntArrayList featureVector = sparseFeatureVectors[i];
					activeFeatureBitSets[i] = new BitSet();
					for (int j = featureVector.size() - 1; j >= 0; --j)
					{
						activeFeatureBitSets[i].set(featureVector.getQuick(j));
					}
					
					final Context contextCopy = new TempContext(context);
					contextCopy.game().apply(contextCopy, legalMoves.get(i));
					
					if (!contextCopy.active(mover))
					{
						if (contextCopy.winners().contains(mover))
							isWinning[i] = true;
						else if (contextCopy.losers().contains(mover))
							isLosing[i] = true;
					}
					else if (contextCopy.state().mover() != mover)	// Not interested in defeating responses if we're the mover again
					{
						final BitSet antiDefeatingActiveFeatures = (BitSet) activeFeatureBitSets[i].clone();
						antiDefeatingActiveFeatures.and(antiDefeatingMovesFeatures[mover]);
						
						final FastArrayList<Move> responses = contextCopy.game().moves(contextCopy).moves();
						for (int j = 0; j < responses.size(); ++j)
						{
							final Context responseContextCopy = new TempContext(contextCopy);
							responseContextCopy.game().apply(responseContextCopy, responses.get(j));
							if (responseContextCopy.losers().contains(mover))
							{
								++numDefeatingResponses[i];
								if (numDefeatingResponses[i] > maxNumDefeatingResponses)
									maxNumDefeatingResponses = numDefeatingResponses[i];
							}
						}
					}
					else
					{
						numDefeatingResponses[i] = Integer.MAX_VALUE;	// Accounting for moves that let us move again gets too complicated
					}
				}
				
				final BitSet winningFeatures = winningMovesFeatures[mover];
				final BitSet losingFeatures = losingMovesFeatures[mover];
				final BitSet antiDefeatingFeatures = antiDefeatingMovesFeatures[mover];
				final BitSet winningMoves = new BitSet();
				final BitSet losingMoves = new BitSet();
				final BitSet antiDefeatingMoves = new BitSet();
				for (int i = legalMoves.size() - 1; i >= 0; --i)
				{
					if (!isWinning[i])
						winningFeatures.andNot(activeFeatureBitSets[i]);
					else
						winningMoves.set(i);
					
					if (!isLosing[i])
						losingFeatures.andNot(activeFeatureBitSets[i]);
					else
						losingMoves.set(i);
					
					if (numDefeatingResponses[i] >= maxNumDefeatingResponses)
						antiDefeatingFeatures.andNot(activeFeatureBitSets[i]);
					else
						antiDefeatingMoves.set(i);
				}
				
//				if (maxNumDefeatingResponses != 0)
//					System.out.println("numDefeatingResponses = " + Arrays.toString(numDefeatingResponses));

				experience.setWinningMoves(winningMoves);
				experience.setLosingMoves(losingMoves);
				experience.setAntiDefeatingMoves(antiDefeatingMoves);
			}
			
			/**
			 * Try to expand a feature set.
			 * @param batch
			 * @param expander
			 * @param featureSet
			 * @param p
			 * @param policy
			 * @param featureActiveRatios
			 * @return Expanded version of feature set, or null if failed
			 */
			private BaseFeatureSet expandFeatureSet
			(
				final List<ExItExperience> batch, 
				final FeatureSetExpander expander,
				final BaseFeatureSet featureSet,
				final int p,
				final SoftmaxPolicyLinear policy,
				final TDoubleArrayList featureActiveRatios,
				final TLongArrayList featureLifetimes,
				final TLongArrayList featureOccurrences,
				final BitSet winningMovesFeatures,
				final BitSet losingMovesFeatures,
				final BitSet antiDefeatingMovesFeatures
			)
			{
				if (batch.size() > 0)
				{
					final BaseFeatureSet expandedFeatureSet = 
							expander.expandFeatureSet
							(
								batch,
								featureSet,
								policy,
								game,
								featureDiscoveryParams.combiningFeatureInstanceThreshold,
								objectiveParams, 
								featureDiscoveryParams,
								featureActiveRatios,
								logWriter,
								this
							);
					
					if (expandedFeatureSet != null)
					{
						expandedFeatureSet.init(game, new int[]{p}, null);
						
						if (featureActiveRatios.size() < expandedFeatureSet.getNumSpatialFeatures())
						{
							// Add new entries for lifetime, average activity, occurrences, and winning/losing/anti-defeating
							winningMovesFeatures.set(featureActiveRatios.size(), expandedFeatureSet.getNumSpatialFeatures());
							losingMovesFeatures.set(featureActiveRatios.size(), expandedFeatureSet.getNumSpatialFeatures());
							antiDefeatingMovesFeatures.set(featureActiveRatios.size(), expandedFeatureSet.getNumSpatialFeatures());
							while (featureActiveRatios.size() < expandedFeatureSet.getNumSpatialFeatures())
							{
								featureActiveRatios.add(0.0);
								featureLifetimes.add(0L);
								featureOccurrences.add(0L);
							}
						}
					}
					
					// Previously cached feature sets likely useless / less useful now, so clear cache
					JITSPatterNetFeatureSet.clearFeatureSetCache();

					return expandedFeatureSet;
				}
				
				return null;
			}
			
			/**
			 * Creates (or loads) optimisers for Selection (one per player)
			 * 
			 * @return
			 */
			private Optimiser[] prepareSelectionOptimisers()
			{
				final Optimiser[] optimisers = new Optimiser[numPlayers + 1];
				
				for (int p = 1; p <= numPlayers; ++p)
				{
					Optimiser optimiser = null;
					
					currentOptimiserSelectionFilenames[p] = getFilenameLastCheckpoint("OptimiserSelection_P" + p, "opt");
					lastCheckpoint = 
							Math.min
							(
								lastCheckpoint,
								extractCheckpointFromFilename(currentOptimiserSelectionFilenames[p], "OptimiserSelection_P" + p, "opt")
							);
					//System.out.println("CE opt set lastCheckpoint = " + lastCheckpoint);
					
					if (currentOptimiserSelectionFilenames[p] == null)
					{
						// create new optimiser
						optimiser = OptimiserFactory.createOptimiser(optimisersParams.selectionOptimiserConfig);
						logLine(logWriter, "starting with new optimiser for Selection phase");
					}
					else
					{
						// load optimiser from file
						try 
						(
							final ObjectInputStream reader = 
								new ObjectInputStream(new BufferedInputStream(new FileInputStream(
										outParams.outDir.getAbsolutePath() + File.separator + currentOptimiserSelectionFilenames[p]
								)))
						)
						{
							optimiser = (Optimiser) reader.readObject();
						}
						catch (final IOException | ClassNotFoundException e)
						{
							e.printStackTrace();
						}
						
						logLine(logWriter, "continuing with Selection optimiser loaded from " + currentOptimiserSelectionFilenames[p]);
					}
					
					optimisers[p] = optimiser;
				}
				
				return optimisers;
			}
			
			/**
			 * Creates (or loads) optimisers for Playout (one per player)
			 * 
			 * @return
			 */
			private Optimiser[] preparePlayoutOptimisers()
			{
				final Optimiser[] optimisers = new Optimiser[numPlayers + 1];
				
				for (int p = 1; p <= numPlayers; ++p)
				{
					Optimiser optimiser = null;
					
					currentOptimiserPlayoutFilenames[p] = getFilenameLastCheckpoint("OptimiserPlayout_P" + p, "opt");
					lastCheckpoint = 
							Math.min
							(
								lastCheckpoint,
								extractCheckpointFromFilename(currentOptimiserPlayoutFilenames[p], "OptimiserPlayout_P" + p, "opt")
							);
					//System.out.println("CE opt set lastCheckpoint = " + lastCheckpoint);
					
					if (currentOptimiserPlayoutFilenames[p] == null)
					{
						// create new optimiser
						optimiser = OptimiserFactory.createOptimiser(optimisersParams.playoutOptimiserConfig);
						logLine(logWriter, "starting with new optimiser for Playout phase");
					}
					else
					{
						// load optimiser from file
						try 
						(
							final ObjectInputStream reader = 
								new ObjectInputStream(new BufferedInputStream(new FileInputStream(
										outParams.outDir.getAbsolutePath() + File.separator + currentOptimiserPlayoutFilenames[p]
								)))
						)
						{
							optimiser = (Optimiser) reader.readObject();
						}
						catch (final IOException | ClassNotFoundException e)
						{
							e.printStackTrace();
						}
						
						logLine(logWriter, "continuing with Playout optimiser loaded from " + currentOptimiserPlayoutFilenames[p]);
					}
					
					optimisers[p] = optimiser;
				}
				
				return optimisers;
			}
			
			/**
			 * Creates new optimisers for CE (one per player)
			 * 
			 * @return
			 */
			private Optimiser[] instantiateCrossEntropyOptimisers()
			{
				final Optimiser[] optimisers = new Optimiser[numPlayers + 1];
				
				for (int p = 1; p <= numPlayers; ++p)
				{
					optimisers[p] = OptimiserFactory.createOptimiser(optimisersParams.playoutOptimiserConfig);
				}
				
				return optimisers;
			}
			
			/**
			 * Creates (or loads) optimisers for TSPG (one per player)
			 * 
			 * @return
			 */
			private Optimiser[] prepareTSPGOptimisers()
			{
				final Optimiser[] optimisers = new Optimiser[numPlayers + 1];
				
				if (objectiveParams.trainTSPG)
				{
					for (int p = 1; p <= numPlayers; ++p)
					{
						Optimiser optimiser = null;
						
						currentOptimiserTSPGFilenames[p] = getFilenameLastCheckpoint("OptimiserTSPG_P" + p, "opt");
						lastCheckpoint = 
								Math.min
								(
									lastCheckpoint,
									extractCheckpointFromFilename(currentOptimiserTSPGFilenames[p], "OptimiserTSPG_P" + p, "opt")
								);
						//System.out.println("TSPG opt set lastCheckpoint = " + lastCheckpoint);
						
						if (currentOptimiserTSPGFilenames[p] == null)
						{
							// create new optimiser
							optimiser = OptimiserFactory.createOptimiser(optimisersParams.tspgOptimiserConfig);
							logLine(logWriter, "starting with new optimiser for TSPG");
						}
						else
						{
							// load optimiser from file
							try 
							(
								final ObjectInputStream reader = 
									new ObjectInputStream(new BufferedInputStream(new FileInputStream(
											outParams.outDir.getAbsolutePath() + File.separator + currentOptimiserTSPGFilenames[p]
									)))
							)
							{
								optimiser = (Optimiser) reader.readObject();
							}
							catch (final IOException | ClassNotFoundException e)
							{
								e.printStackTrace();
							}
							
							logLine(logWriter, "continuing with TSPG optimiser loaded from " + currentOptimiserTSPGFilenames[p]);
						}
						
						optimisers[p] = optimiser;
					}
				}
				
				return optimisers;
			}
			
			/**
			 * Creates (or loads) optimisers for CEE (one per player)
			 * 
			 * @return
			 */
			private Optimiser[] prepareCEExploreOptimisers()
			{
				final Optimiser[] optimisers = new Optimiser[numPlayers + 1];
				
				for (int p = 1; p <= numPlayers; ++p)
				{
					Optimiser optimiser = null;
					
					currentOptimiserCEEFilenames[p] = getFilenameLastCheckpoint("OptimiserCEE_P" + p, "opt");
					lastCheckpoint = 
							Math.min
							(
								lastCheckpoint,
								extractCheckpointFromFilename(currentOptimiserCEEFilenames[p], "OptimiserCEE_P" + p, "opt")
							);
					//System.out.println("CEE opt set lastCheckpoint = " + lastCheckpoint);
					
					if (currentOptimiserCEEFilenames[p] == null)
					{
						// create new optimiser
						optimiser = OptimiserFactory.createOptimiser(optimisersParams.ceExploreOptimiserConfig);
						logLine(logWriter, "starting with new optimiser for CEE");
					}
					else
					{
						// load optimiser from file
						try 
						(
							final ObjectInputStream reader = 
								new ObjectInputStream(new BufferedInputStream(new FileInputStream(
										outParams.outDir.getAbsolutePath() + File.separator + currentOptimiserCEEFilenames[p]
								)))
						)
						{
							optimiser = (Optimiser) reader.readObject();
						}
						catch (final IOException | ClassNotFoundException e)
						{
							e.printStackTrace();
						}
						
						logLine(logWriter, "continuing with CEE optimiser loaded from " + currentOptimiserCEEFilenames[p]);
					}
					
					optimisers[p] = optimiser;
				}
				
				return optimisers;
			}
			
			/**
			 * Creates (or loads) optimiser for Value function (one shared for all players)
			 * 
			 * @return
			 */
			private Optimiser prepareValueFunctionOptimiser()
			{
				final Optimiser[] optimisers = new Optimiser[numPlayers + 1];
				
				Optimiser optimiser = null;
					
				currentOptimiserValueFilename = getFilenameLastCheckpoint("OptimiserValue", "opt");
				lastCheckpoint = 
						Math.min
						(
							lastCheckpoint,
							extractCheckpointFromFilename(currentOptimiserValueFilename, "OptimiserValue", "opt")
						);

				if (currentOptimiserValueFilename == null)
				{
					// create new optimiser
					optimiser = OptimiserFactory.createOptimiser(optimisersParams.valueOptimiserConfig);
					logLine(logWriter, "starting with new optimiser for Value function");
				}
				else
				{
					// load optimiser from file
					try 
					(
							final ObjectInputStream reader = 
							new ObjectInputStream(new BufferedInputStream(new FileInputStream(
									outParams.outDir.getAbsolutePath() + File.separator + currentOptimiserValueFilename
								)))
							)
					{
						optimiser = (Optimiser) reader.readObject();
					}
					catch (final IOException | ClassNotFoundException e)
					{
						e.printStackTrace();
					}

					logLine(logWriter, "continuing with Value function optimiser loaded from " + currentOptimiserValueFilename);
				}
				
				return optimiser;
			}
			
			/**
			 * Creates (or loads) experience buffers (one per player)
			 * 
			 * @param prio
			 * @return
			 */
			private ExperienceBuffer[] prepareExperienceBuffers(final boolean prio)
			{
				final ExperienceBuffer[] experienceBuffers = new ExperienceBuffer[numPlayers + 1];
				
				for (int p = 1; p <= numPlayers; ++p)
				{
					final ExperienceBuffer experienceBuffer;
					
					currentExperienceBufferFilenames[p] = getFilenameLastCheckpoint("ExperienceBuffer_P" + p, "buf");
					lastCheckpoint = 
							Math.min
							(
								lastCheckpoint,
								extractCheckpointFromFilename(currentExperienceBufferFilenames[p], "ExperienceBuffer_P" + p, "buf")
							);
					//System.out.println("Buffers set lastCheckpoint = " + lastCheckpoint);
					
					if (currentExperienceBufferFilenames[p] == null)
					{
						// create new Experience Buffer
						if (prio)
							experienceBuffer = new PrioritizedReplayBuffer(trainingParams.experienceBufferSize);
						else
							experienceBuffer = new UniformExperienceBuffer(trainingParams.experienceBufferSize);
						logLine(logWriter, "starting with empty experience buffer");
					}
					else
					{
						// load experience buffer from file
						experienceBuffer = 
								prio
								? PrioritizedReplayBuffer.fromFile(game, outParams.outDir.getAbsolutePath() + File.separator + currentExperienceBufferFilenames[p])
								: UniformExperienceBuffer.fromFile(game, outParams.outDir.getAbsolutePath() + File.separator + currentExperienceBufferFilenames[p]);
						
						logLine(logWriter, "continuing with experience buffer loaded from " + currentExperienceBufferFilenames[p]);
					}
					
					experienceBuffers[p] = experienceBuffer;
				}
				
				return experienceBuffers;
			}
			
			/**
			 * Creates (or loads) experience buffers (one per player) for special moves
			 * (winning, losing, and anti-defeating moves)
			 * 
			 * @return
			 */
			private ExperienceBuffer[] prepareSpecialMoveExperienceBuffers()
			{
				final ExperienceBuffer[] experienceBuffers = new ExperienceBuffer[numPlayers + 1];
				
				for (int p = 1; p <= numPlayers; ++p)
				{
					final ExperienceBuffer experienceBuffer;
					
					currentSpecialMoveExperienceBufferFilenames[p] = getFilenameLastCheckpoint("SpecialMoveExperienceBuffer_P" + p, "buf");
					lastCheckpoint = 
							Math.min
							(
								lastCheckpoint,
								extractCheckpointFromFilename(currentSpecialMoveExperienceBufferFilenames[p], "SpecialMoveExperienceBuffer_P" + p, "buf")
							);
					//System.out.println("Buffers set lastCheckpoint = " + lastCheckpoint);
					
					if (currentSpecialMoveExperienceBufferFilenames[p] == null)
					{
						// create new Experience Buffer
						experienceBuffer = new UniformExperienceBuffer(trainingParams.experienceBufferSize);
						logLine(logWriter, "starting with empty experience buffer for special moves");
					}
					else
					{
						// load experience buffer from file
						experienceBuffer = 
								UniformExperienceBuffer.fromFile(game, outParams.outDir.getAbsolutePath() + File.separator + currentSpecialMoveExperienceBufferFilenames[p]);
						
						logLine(logWriter, "continuing with experience buffer for special moves loaded from " + currentSpecialMoveExperienceBufferFilenames[p]);
					}
					
					experienceBuffers[p] = experienceBuffer;
				}
				
				return experienceBuffers;
			}
			
			/**
			 * Creates (or loads) experience buffers for final states (one per player)
			 * 
			 * @param prio
			 * @return
			 */
			private ExperienceBuffer[] prepareFinalStatesExperienceBuffers()
			{
				final ExperienceBuffer[] experienceBuffers = new ExperienceBuffer[numPlayers + 1];
				
				for (int p = 1; p <= numPlayers; ++p)
				{
					final ExperienceBuffer experienceBuffer;
					
					currentFinalStatesExperienceBufferFilenames[p] = getFilenameLastCheckpoint("FinalStatesExperienceBuffer_P" + p, "buf");
					lastCheckpoint = 
							Math.min
							(
								lastCheckpoint,
								extractCheckpointFromFilename(currentFinalStatesExperienceBufferFilenames[p], "FinalStatesExperienceBuffer_P" + p, "buf")
							);
					
					if (currentFinalStatesExperienceBufferFilenames[p] == null)
					{
						// create new Experience Buffer
						experienceBuffer = new UniformExperienceBuffer(trainingParams.experienceBufferSize);
						logLine(logWriter, "starting with empty final states experience buffer");
					}
					else
					{
						// load experience buffer from file
						experienceBuffer = 
								UniformExperienceBuffer.fromFile(game, outParams.outDir.getAbsolutePath() + File.separator + currentFinalStatesExperienceBufferFilenames[p]);
						
						logLine(logWriter, "continuing with final states experience buffer loaded from " + currentFinalStatesExperienceBufferFilenames[p]);
					}
					
					experienceBuffers[p] = experienceBuffer;
				}
				
				return experienceBuffers;
			}
			
			/**
			 * Creates (or loads) trackers for average game duration (one per player)
			 * 
			 * @return
			 */
			private ExponentialMovingAverage[] prepareGameDurationTrackers()
			{
				final ExponentialMovingAverage[] trackers = new ExponentialMovingAverage[numPlayers + 1];
				
				for (int p = 1; p <= numPlayers; ++p)
				{
					ExponentialMovingAverage tracker = null;
					
					currentGameDurationTrackerFilenames[p] = getFilenameLastCheckpoint("GameDurationTracker_P" + p, "bin");
					lastCheckpoint = 
							Math.min
							(
								lastCheckpoint,
								extractCheckpointFromFilename(currentGameDurationTrackerFilenames[p], "GameDurationTracker_P" + p, "bin")
							);
					//System.out.println("Game dur trackers set lastCheckpoint = " + lastCheckpoint);
					
					if (currentGameDurationTrackerFilenames[p] == null)
					{
						// Create new tracker
						tracker = new ExponentialMovingAverage();
						logLine(logWriter, "starting with new tracker for average game duration");
					}
					else
					{
						// load tracker from file
						try 
						(
							final ObjectInputStream reader = 
								new ObjectInputStream(new BufferedInputStream(new FileInputStream(
										outParams.outDir.getAbsolutePath() + File.separator + currentGameDurationTrackerFilenames[p]
								)))
						)
						{
							tracker = (ExponentialMovingAverage) reader.readObject();
						}
						catch (final IOException | ClassNotFoundException e)
						{
							e.printStackTrace();
						}
						
						logLine(logWriter, "continuing with average game duration tracker loaded from " + currentGameDurationTrackerFilenames[p]);
					}
					
					trackers[p] = tracker;
				}
				
				return trackers;
			}
			
			/**
			 * Creates (or loads) linear functions (one per player) for Selection phase
			 * @param featureSets
			 * @return
			 */
			private LinearFunction[] prepareSelectionFunctions(final BaseFeatureSet[] featureSets)
			{
				final LinearFunction[] linearFunctions = new LinearFunction[numPlayers + 1];
				
				for (int p = 1; p <= numPlayers; ++p)
				{
					final LinearFunction linearFunction;
					
					currentPolicyWeightsSelectionFilenames[p] = getFilenameLastCheckpoint("PolicyWeightsSelection_P" + p, "txt");
					lastCheckpoint = 
							Math.min
							(
								lastCheckpoint,
								extractCheckpointFromFilename(currentPolicyWeightsSelectionFilenames[p], "PolicyWeightsSelection_P" + p, "txt")
							);
					//System.out.println("CE funcs set lastCheckpoint = " + lastCheckpoint);
					
					if (currentPolicyWeightsSelectionFilenames[p] == null)
					{
						// Create new linear function
						linearFunction = new LinearFunction(new WeightVector(new FVector(featureSets[p].getNumFeatures())));
						logLine(logWriter, "starting with new 0-weights linear function for Selection phase");
					}
					else
					{
						// Load weights from file
						linearFunction = 
								LinearFunction.fromFile(outParams.outDir.getAbsolutePath() + File.separator + currentPolicyWeightsSelectionFilenames[p]);
						logLine(logWriter, "continuing with Selection policy weights loaded from " + currentPolicyWeightsSelectionFilenames[p]);
						
						try 
						{
							// make sure we're combining correct function with feature set
							String featureSetFilepath = 
									new File(outParams.outDir.getAbsolutePath() + File.separator + currentPolicyWeightsSelectionFilenames[p]).getParent();
							featureSetFilepath += File.separator + linearFunction.featureSetFile();
							
							if 
							(
								!new File(featureSetFilepath).getCanonicalPath().equals
								(
									new File(outParams.outDir.getAbsolutePath() + File.separator + currentFeatureSetFilenames[p]).getCanonicalPath()
								)
							)
							{
								System.err.println
								(
									"Warning: selection policy weights were saved for feature set " + featureSetFilepath 
									+ ", but we are now using " + currentFeatureSetFilenames[p]
								);
							}
						}
						catch (final IOException e)
						{
							e.printStackTrace();
						}
					}
					
					linearFunctions[p] = linearFunction;
				}
				
				return linearFunctions;
			}
			
			/**
			 * Creates (or loads) linear functions (one per player) for Playout phase
			 * @param featureSets
			 * @return
			 */
			private LinearFunction[] preparePlayoutFunctions(final BaseFeatureSet[] featureSets)
			{
				final LinearFunction[] linearFunctions = new LinearFunction[numPlayers + 1];
				
				for (int p = 1; p <= numPlayers; ++p)
				{
					final LinearFunction linearFunction;
					
					currentPolicyWeightsPlayoutFilenames[p] = getFilenameLastCheckpoint("PolicyWeightsPlayout_P" + p, "txt");
					lastCheckpoint = 
							Math.min
							(
								lastCheckpoint,
								extractCheckpointFromFilename(currentPolicyWeightsPlayoutFilenames[p], "PolicyWeightsPlayout_P" + p, "txt")
							);
					//System.out.println("CE funcs set lastCheckpoint = " + lastCheckpoint);
					
					if (currentPolicyWeightsSelectionFilenames[p] == null)
					{
						// Create new linear function
						linearFunction = new LinearFunction(new WeightVector(new FVector(featureSets[p].getNumFeatures())));
						logLine(logWriter, "starting with new 0-weights linear function for Playout phase");
					}
					else
					{
						// Load weights from file
						linearFunction = 
								LinearFunction.fromFile(outParams.outDir.getAbsolutePath() + File.separator + currentPolicyWeightsPlayoutFilenames[p]);
						logLine(logWriter, "continuing with Playout policy weights loaded from " + currentPolicyWeightsPlayoutFilenames[p]);
						
						try 
						{
							// make sure we're combining correct function with feature set
							String featureSetFilepath = 
									new File(outParams.outDir.getAbsolutePath() + File.separator + currentPolicyWeightsPlayoutFilenames[p]).getParent();
							featureSetFilepath += File.separator + linearFunction.featureSetFile();
							
							if 
							(
								!new File(featureSetFilepath).getCanonicalPath().equals
								(
									new File(outParams.outDir.getAbsolutePath() + File.separator + currentFeatureSetFilenames[p]).getCanonicalPath()
								)
							)
							{
								System.err.println
								(
									"Warning: playout policy weights were saved for feature set " + featureSetFilepath 
									+ ", but we are now using " + currentFeatureSetFilenames[p]
								);
							}
						}
						catch (final IOException e)
						{
							e.printStackTrace();
						}
					}
					
					linearFunctions[p] = linearFunction;
				}
				
				return linearFunctions;
			}
			
			/**
			 * Creates (or loads) linear functions (one per player)
			 * @param featureSets
			 * @param crossEntropyFunctions CE-trained functions used for boosting
			 * @return
			 */
			private LinearFunction[] prepareTSPGFunctions
			(
				final BaseFeatureSet[] featureSets, 
				final LinearFunction[] crossEntropyFunctions
			)
			{
				final LinearFunction[] linearFunctions = new LinearFunction[numPlayers + 1];
				
				if (objectiveParams.trainTSPG)
				{
					for (int p = 1; p <= numPlayers; ++p)
					{
						final LinearFunction linearFunction;
						
						currentPolicyWeightsTSPGFilenames[p] = getFilenameLastCheckpoint("PolicyWeightsTSPG_P" + p, "txt");
						lastCheckpoint = 
								Math.min
								(
									lastCheckpoint,
									extractCheckpointFromFilename(currentPolicyWeightsTSPGFilenames[p], "PolicyWeightsTSPG_P" + p, "txt")
								);
						//System.out.println("TSPG funcs set lastCheckpoint = " + lastCheckpoint);
						
						if (currentPolicyWeightsTSPGFilenames[p] == null)
						{
							// create new boosted linear function
							linearFunction = 
									new BoostedLinearFunction
									(
										new WeightVector(new FVector(featureSets[p].getNumFeatures())),
										crossEntropyFunctions[p]
									);
							logLine(logWriter, "starting with new 0-weights linear function for TSPG");
						}
						else
						{
							// load weights from file
							linearFunction = 
									BoostedLinearFunction.boostedFromFile
									(
										outParams.outDir.getAbsolutePath() + File.separator + currentPolicyWeightsTSPGFilenames[p],
										crossEntropyFunctions[p]
									);
							logLine(logWriter, "continuing with Selection policy weights loaded from " + currentPolicyWeightsTSPGFilenames[p]);
							
							try 
							{
								// make sure we're combining correct function with feature set
								String featureSetFilepath = 
										new File(outParams.outDir.getAbsolutePath() + File.separator + currentPolicyWeightsTSPGFilenames[p]).getParent();
								featureSetFilepath += File.separator + linearFunction.featureSetFile();
								
								if 
								(
									!new File(featureSetFilepath).getCanonicalPath().equals
									(
										new File(outParams.outDir.getAbsolutePath() + File.separator + currentFeatureSetFilenames[p]).getCanonicalPath()
									)
								)
								{
									System.err.println
									(
										"Warning: policy weights were saved for feature set " + featureSetFilepath 
										+ ", but we are now using " + currentFeatureSetFilenames[p]
									);
								}
							}
							catch (final IOException e)
							{
								e.printStackTrace();
							}
						}
						
						linearFunctions[p] = linearFunction;
					}
				}
				
				return linearFunctions;
			}
			
			/**
			 * Creates (or loads) value function
			 * @return
			 */
			private Heuristics prepareValueFunction()
			{
				if (objectiveParams.noValueLearning)
					return null;
				
				Heuristics valueFunction = null;
				
				currentValueFunctionFilename = getFilenameLastCheckpoint("ValueFunction", "txt");
				lastCheckpoint = 
							Math.min
							(
								lastCheckpoint,
								extractCheckpointFromFilename(currentValueFunctionFilename, "ValueFunction", "txt")
							);
				final Report report = new Report();
				
				if (currentValueFunctionFilename == null)
				{
					Heuristics initHeuristics;
					if ((initHeuristics = loadInitHeuristics()) != null)
					{
						valueFunction = initHeuristics;
						valueFunction.init(game);
					}
					else if (agentsParams.bestAgentsDataDir != null)
					{
						// load heuristics from the best-agents-data dir
						try
						{
							final String descr = FileHandling.loadTextContentsFromFile(agentsParams.bestAgentsDataDir + "/BestHeuristics.txt");
							valueFunction = (Heuristics)compiler.Compiler.compileObject
											(
												descr, 
												"metadata.ai.heuristics.Heuristics",
												report
											);
							valueFunction.init(game);
						}
						catch (final IOException e)
						{
							e.printStackTrace();
						}
					}
					else
					{
						// copy value function from game metadata
						valueFunction = Heuristics.copy(game.metadata().ai().heuristics());
						valueFunction.init(game);
						logLine(logWriter, "starting with new initial value function from .lud metadata");
					}
				}
				else
				{
					// load value function from file
					try
					{
						final String descr = FileHandling.loadTextContentsFromFile(
								outParams.outDir.getAbsolutePath() + File.separator + currentValueFunctionFilename);
						valueFunction = (Heuristics)compiler.Compiler.compileObject
										(
											descr, 
											"metadata.ai.heuristics.Heuristics",
											report
										);
						valueFunction.init(game);
					} 
					catch (final IOException e)
					{
						e.printStackTrace();
					}

					logLine
					(
						logWriter, 
						"continuing with value function from " + 
							outParams.outDir.getAbsolutePath() + File.separator + currentValueFunctionFilename
					);
				}
				
				return valueFunction;
			}
			
			/**
			 * @return Heuristics specified as initial value function, or null if not specified / failed to load.
			 */
			private Heuristics loadInitHeuristics()
			{
				if (trainingParams.initValueFuncDir == null || trainingParams.initValueFuncDir.equals(""))
					return null;
				
				final File initHeuristicsDir = new File(trainingParams.initValueFuncDir);
				
				if (initHeuristicsDir.exists() && initHeuristicsDir.isDirectory())
				{
					final File[] files = initHeuristicsDir.listFiles();
					int latestGen = -1;
					File latestGenFile = null;
					
					for (final File file : files)
					{
						final String filename = file.getName();
						if (filename.startsWith("results_") && filename.endsWith(".txt"))
						{
							final int gen = 
									Integer.parseInt
									(
										filename.split
										(
											java.util.regex.Pattern.quote("_")
										)[2].replaceAll(java.util.regex.Pattern.quote(".txt"), "")
									);
							
							if (gen > latestGen)
							{
								latestGen = gen;
								latestGenFile = file;
							}
						}
					}
					
					if (latestGenFile != null)
					{
						try
						{
							final String contents = FileHandling.loadTextContentsFromFile(latestGenFile.getAbsolutePath());
							final String[] splitContents = contents.split(java.util.regex.Pattern.quote("\n"));
							
							final List<String> topHeuristicLines = new ArrayList<String>();
							
							// We skip first line, that's just a "-------------------------------" line
							for (int i = 1; i < splitContents.length; ++i)
							{
								final String line = splitContents[i];
								
								if (line.equals("-------------------------------"))
									break;		// We're done
								
								topHeuristicLines.add(line);
							}
							
							// Remove final two lines: they're an empty line, and the top heuristic's score
							topHeuristicLines.remove(topHeuristicLines.size() - 1);
							topHeuristicLines.remove(topHeuristicLines.size() - 1);
							
							// Compile heuristic
							final Heuristics heuristic = 
									(Heuristics)compiler.Compiler.compileObject
									(
										StringRoutines.join("\n", topHeuristicLines), 
										"metadata.ai.heuristics.Heuristics",
										new Report()
									);
							
							return heuristic;
						} 
						catch (final IOException e)
						{
							e.printStackTrace();
						}
					}
				}
				
				return null;
			}
			
			/**
			 * Creates (or loads) feature sets (one per player, or a single shared one)
			 * @return
			 */
			private BaseFeatureSet[] prepareFeatureSets()
			{
				final BaseFeatureSet[] featureSets;
				final TIntArrayList newlyCreated = new TIntArrayList();
				
				featureSets = new BaseFeatureSet[numPlayers + 1];
					
				for (int p = 1; p <= numPlayers; ++p)
				{
					final BaseFeatureSet featureSet;

					currentFeatureSetFilenames[p] = getFilenameLastCheckpoint("FeatureSet_P" + p, "fs");
					lastCheckpoint = 
							Math.min
							(
								lastCheckpoint,
								extractCheckpointFromFilename(currentFeatureSetFilenames[p], "FeatureSet_P" + p, "fs")
							);
					//System.out.println("Feature sets set lastCheckpoint = " + lastCheckpoint);

					if (currentFeatureSetFilenames[p] == null)
					{
						// create new Feature Set
						final AtomicFeatureGenerator atomicFeatures = new AtomicFeatureGenerator(game, 2, 4);
						featureSet = JITSPatterNetFeatureSet.construct(atomicFeatures.getAspatialFeatures(), atomicFeatures.getSpatialFeatures());
						newlyCreated.add(p);
						logLine(logWriter, "starting with new initial feature set for Player " + p);
						logLine(logWriter, "num atomic features = " + featureSet.getNumSpatialFeatures());
					}
					else
					{
						// load feature set from file
						featureSet = JITSPatterNetFeatureSet.construct(outParams.outDir.getAbsolutePath() + File.separator + currentFeatureSetFilenames[p]);
						logLine
						(
							logWriter, 
							"continuing with feature set loaded from " + 
							outParams.outDir.getAbsolutePath() + File.separator + currentFeatureSetFilenames[p] +
							" for Player " + p
						);
					}

					if (featureSet.getNumSpatialFeatures() == 0)
					{
						System.err.println("ERROR: Feature Set has 0 features!");
						logLine(logWriter, "Training with 0 features makes no sense, interrupting experiment.");
						interrupted = true;
					}

					featureSet.init(game, new int[]{p}, null);
					featureSets[p] = featureSet;
				}
				
				return featureSets;
			}
			
			//-----------------------------------------------------------------
			
			/**
			 * @return When should the next checkpoint be?
			 */
			private long computeNextCheckpoint()
			{
				if (lastCheckpoint < 0L)
					return 0L;
				else if (lastCheckpoint < 10)
					return lastCheckpoint + 1;
				else
					return lastCheckpoint + outParams.checkpointFrequency;
			}
			
			/**
			 * Creates a filename for a given checkpoint
			 * @param baseFilename
			 * @param checkpoint
			 * @param extension
			 * @return
			 */
			private String createCheckpointFilename
			(
				final String baseFilename,
				final long checkpoint,
				final String extension
			)
			{
				final String format = (outParams.checkpointType == CheckpointTypes.Game) 
						? gameCheckpointFormat : weightUpdateCheckpointFormat;
				
				return String.format(format, baseFilename, Long.valueOf(checkpoint), extension);
			}
			
			/**
			 * @param baseFilename
			 * @param extension
			 * @return Checkpoint extracted from existing filename
			 */
			private int extractCheckpointFromFilename
			(
				final String filename,
				final String baseFilename,
				final String extension
			)
			{
				if (filename == null)
					return -1;
				
				final String checkpoint = 
						filename.substring
						(
							(baseFilename + "_").length(), 
							filename.length() - ("." + extension).length()
						);
				
				return Integer.parseInt(checkpoint);
			}
			
			/**
			 * Computes a filename for the last checkpoint
			 * @param baseFilename
			 * @param extension
			 * @return Computed filepath, or null if none saved yet
			 */
			private String getFilenameLastCheckpoint
			(
				final String baseFilename,
				final String extension
			)
			{
				if (outParams.outDir == null)
					return null;
				
				final String[] filenames = outParams.outDir.list();
				int maxCheckpoint = -1;
				
				for (final String filename : filenames)
				{
					if 
					(
						filename.startsWith(baseFilename + "_") && 
						filename.endsWith("." + extension)
					)
					{
						final int checkpoint = extractCheckpointFromFilename(filename, baseFilename, extension);
						if (checkpoint > maxCheckpoint)
							maxCheckpoint = checkpoint;
					}
				}
				
				if (maxCheckpoint < 0)
					return null;
				
				return createCheckpointFilename(baseFilename, maxCheckpoint, extension);
			}
			
			/**
			 * Saves checkpoints (if we want to or are forced to)
			 * @param gameCounter
			 * @param weightsUpdateCounter
			 * @param featureSets
			 * @param selectionFunctions
			 * @param playoutFunctions
			 * @param tspgFunctions
			 * @param experienceBuffers
			 * @param specialMoveExperienceBuffers
			 * @param ceOptimisers
			 * @param tspgOptimisers
			 * @param valueFunctionOptimiser
			 * @param avgGameDurations
			 * @param forced
			 */
			private void saveCheckpoints
			(
				final int gameCounter, 
				final long weightsUpdateCounter,
				final BaseFeatureSet[] featureSets, 
				final LinearFunction[] selectionFunctions,
				final LinearFunction[] playoutFunctions,
				final LinearFunction[] tspgFunctions,
				final Heuristics valueFunction,
				final ExperienceBuffer[] experienceBuffers,
				final ExperienceBuffer[] specialMoveExperienceBuffers,
				final Optimiser[] ceOptimisers,
				final Optimiser[] tspgOptimisers,
				final Optimiser valueFunctionOptimiser,
				final ExponentialMovingAverage[] avgGameDurations,
				final TLongArrayList[] featureOccurrences,
				final BitSet[] winningMovesFeatures,
				final BitSet[] losingMovesFeatures,
				final BitSet[] antiDefeatingMovesFeatures,
				final boolean forced
			)
			{
				if (outParams.outDir == null)
					return;
				
				long nextCheckpoint = computeNextCheckpoint();
				
				if (outParams.checkpointType == CheckpointTypes.Game)
				{
					if (!forced && gameCounter < nextCheckpoint)
						return;
					else
						nextCheckpoint = gameCounter;
				}
				else if (outParams.checkpointType == CheckpointTypes.WeightUpdate)
				{
					if (!forced && weightsUpdateCounter < nextCheckpoint)
						return;
					else
						nextCheckpoint = weightsUpdateCounter;
				}
								
				for (int p = 1; p <= numPlayers; ++p)
				{
					// Save feature set
					final String featureSetFilename = createCheckpointFilename("FeatureSet_P" + p, nextCheckpoint, "fs");
					featureSets[p].toFile(outParams.outDir.getAbsolutePath() + File.separator + featureSetFilename);
					currentFeatureSetFilenames[p] = featureSetFilename;
					
					// Save Selection weights
					final String selectionWeightsFilename = createCheckpointFilename("PolicyWeightsSelection_P" + p, nextCheckpoint, "txt");
					selectionFunctions[p].writeToFile(
							outParams.outDir.getAbsolutePath() + File.separator + selectionWeightsFilename, new String[]{currentFeatureSetFilenames[p]});
					currentPolicyWeightsSelectionFilenames[p] = selectionWeightsFilename;
					
					// Save Playout weights
					final String playoutWeightsFilename = createCheckpointFilename("PolicyWeightsPlayout_P" + p, nextCheckpoint, "txt");
					playoutFunctions[p].writeToFile(
							outParams.outDir.getAbsolutePath() + File.separator + playoutWeightsFilename, new String[]{currentFeatureSetFilenames[p]});
					currentPolicyWeightsPlayoutFilenames[p] = playoutWeightsFilename;
					
					// Save TSPG weights
					if (objectiveParams.trainTSPG)
					{
						final String tspgWeightsFilename = createCheckpointFilename("PolicyWeightsTSPG_P" + p, nextCheckpoint, "txt");
						tspgFunctions[p].writeToFile(
								outParams.outDir.getAbsolutePath() + File.separator + tspgWeightsFilename, new String[]{currentFeatureSetFilenames[p]});
						currentPolicyWeightsTSPGFilenames[p] = tspgWeightsFilename;
					}

					if (valueFunction != null)
					{
						// save Value function
						final String valueFunctionFilename = createCheckpointFilename("ValueFunction", nextCheckpoint, "txt");
						valueFunction.toFile(game, outParams.outDir.getAbsolutePath() + File.separator + valueFunctionFilename);
					}

					if (forced)
					{
						// In this case, we'll also store experience buffers
						final String experienceBufferFilename = createCheckpointFilename("ExperienceBuffer_P" + p, nextCheckpoint, "buf");
						experienceBuffers[p].writeToFile(outParams.outDir.getAbsolutePath() + File.separator + experienceBufferFilename);
						
						final String specialMoveExperienceBufferFilename = createCheckpointFilename("SpecialMoveExperienceBuffer_P" + p, nextCheckpoint, "buf");
						specialMoveExperienceBuffers[p].writeToFile(outParams.outDir.getAbsolutePath() + File.separator + specialMoveExperienceBufferFilename);
											
						// and optimisers
						final String ceOptimiserFilename = createCheckpointFilename("OptimiserCE_P" + p, nextCheckpoint, "opt");
						ceOptimisers[p].writeToFile(outParams.outDir.getAbsolutePath() + File.separator + ceOptimiserFilename);
						currentOptimiserSelectionFilenames[p] = ceOptimiserFilename;
						
						if (objectiveParams.trainTSPG)
						{
							final String tspgOptimiserFilename = createCheckpointFilename("OptimiserTSPG_P" + p, nextCheckpoint, "opt");
							tspgOptimisers[p].writeToFile(outParams.outDir.getAbsolutePath() + File.separator + tspgOptimiserFilename);
							currentOptimiserTSPGFilenames[p] = tspgOptimiserFilename;
						}

						// and average game duration trackers
						final String gameDurationTrackerFilename = createCheckpointFilename("GameDurationTracker_P" + p, nextCheckpoint, "bin");
						avgGameDurations[p].writeToFile(outParams.outDir.getAbsolutePath() + File.separator + gameDurationTrackerFilename);
						currentGameDurationTrackerFilenames[p] = gameDurationTrackerFilename;
						
						// and special moves CSV
						final String specialMovesCSVFilename = createCheckpointFilename("SpecialMoves_P" + p, nextCheckpoint, "csv");
						try 
						(
							final PrintWriter writer = new PrintWriter(outParams.outDir.getAbsolutePath() + File.separator + specialMovesCSVFilename, "UTF-8")
						)
						{
							// Write header
							writer.println
							(
								StringRoutines.join(",", "SpatialFeatureIndex", "AlwaysWinning", "AlwaysLosing", "AlwaysAntiDefeating", "NumOccurrences")
							);
							
							for (int i = 0; i < featureSets[p].getNumSpatialFeatures(); ++i)
							{
								writer.println
								(
									StringRoutines.join
									(
										",", 
										String.valueOf(i),
										winningMovesFeatures[p].get(i) ? "1" : "0",
										losingMovesFeatures[p].get(i) ? "1" : "0",
										antiDefeatingMovesFeatures[p].get(i) ? "1" : "0",
										String.valueOf(featureOccurrences[p].getQuick(i))
									)
								);
							}
						} 
						catch (final IOException e) 
						{
							e.printStackTrace();
						}
					}
				}
				
				if (forced)
				{
					final String valueOptimiserFilename = createCheckpointFilename("OptimiserValue", nextCheckpoint, "opt");
					valueFunctionOptimiser.writeToFile(outParams.outDir.getAbsolutePath() + File.separator + valueOptimiserFilename);
					currentOptimiserValueFilename = valueOptimiserFilename;
				}
				
				lastCheckpoint = nextCheckpoint;
			}
			
			//-----------------------------------------------------------------
			
			@Override
			public void logLine(final PrintWriter log, final String line)
			{
				if (!outParams.noLogging)
					super.logLine(log, line);
			}
			
			//-----------------------------------------------------------------
		};
		
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Creates a writer for output log, or null if we don't want one
	 * @return
	 */
	private PrintWriter createLogWriter()
	{
		if (outParams.outDir != null && !outParams.noLogging)
		{
			final String nextLogFilepath = 
					ExperimentFileUtils.getNextFilepath(outParams.outDir.getAbsolutePath() + File.separator + "ExIt", "log");
			
			// first create directories if necessary
			new File(nextLogFilepath).getParentFile().mkdirs();
			
			try
			{
				return new PrintWriter(nextLogFilepath, "UTF-8");
			}
			catch (final FileNotFoundException | UnsupportedEncodingException e)
			{
				e.printStackTrace();
				return null;
			}
		}
		else
		{
			return null;
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Can be used for quick testing without command-line args, or proper
	 * testing with elaborate setup through command-line args
	 * @param args
	 */
	@SuppressWarnings("unchecked")
	public static void main(final String[] args)
	{
		// Feature Set caching is safe in this main method
		JITSPatterNetFeatureSet.ALLOW_FEATURE_SET_CACHE = true;
		
		// Define options for arg parser
		final CommandLineArgParse argParse = 
				new CommandLineArgParse
				(
					true,
					"Execute a training run from self-play using Expert Iteration."
				);
		
		argParse.addOption(new ArgOption()
				.withNames("--game")
				.help("Name of the game to play. Should end with \".lud\".")
				.withDefault("/Amazons.lud")
				.withNumVals(1)
				.withType(OptionTypes.String));
		argParse.addOption(new ArgOption()
				.withNames("--game-options")
				.help("Game Options to load.")
				.withDefault(new ArrayList<String>(0))
				.withNumVals("*")
				.withType(OptionTypes.String));
		argParse.addOption(new ArgOption()
				.withNames("--ruleset")
				.help("Ruleset to compile.")
				.withDefault("")
				.withNumVals(1)
				.withType(OptionTypes.String));
		argParse.addOption(new ArgOption()
				.withNames("--game-length-cap", "--max-num-actions")
				.help("Maximum number of actions that may be taken before a game is terminated as a draw (-1 for no limit).")
				.withDefault(Integer.valueOf(-1))
				.withNumVals(1)
				.withType(OptionTypes.Int));
		
		argParse.addOption(new ArgOption()
				.withNames("--expert-ai")
				.help("Type of AI to use as expert.")
				.withDefault("Biased MCTS")
				.withNumVals(1)
				.withType(OptionTypes.String)
				.withLegalVals("BEST_AGENT", "FROM_METADATA", "Biased MCTS", "UCT", "PVTS"));
		argParse.addOption(new ArgOption()
				.withNames("--best-agents-data-dir")
				.help("Filepath for directory with best agents data for this game (+ options).")
				.withNumVals(1)
				.withType(OptionTypes.String));
		argParse.addOption(new ArgOption()
				.withNames("--thinking-time", "--time", "--seconds")
				.help("Max allowed thinking time per move (in seconds).")
				.withDefault(Double.valueOf(1.0))
				.withNumVals(1)
				.withType(OptionTypes.Double));
		argParse.addOption(new ArgOption()
				.withNames("--iteration-limit", "--iterations")
				.help("Max allowed number of MCTS iterations per move.")
				.withDefault(Integer.valueOf(-1))
				.withNumVals(1)
				.withType(OptionTypes.Int));
		argParse.addOption(new ArgOption()
				.withNames("--depth-limit")
				.help("Search depth limit (e.g. for Alpha-Beta experts).")
				.withDefault(Integer.valueOf(-1))
				.withNumVals(1)
				.withType(OptionTypes.Int));
		argParse.addOption(new ArgOption()
				.withNames("--tournament-mode")
				.help("If true, we use the tournament mode (similar to the one in Polygames).")
				.withType(OptionTypes.Boolean));
		argParse.addOption(new ArgOption()
				.withNames("--playout-features-epsilon")
				.help("Epsilon for epsilon greedy feature-based playouts")
				.withDefault(Double.valueOf(0.0))
				.withNumVals(1)
				.withType(OptionTypes.Double));
		argParse.addOption(new ArgOption()
				.withNames("--max-biased-playout-actions", "--max-num-biased-playout-actions")
				.help("Maximum number of actions per playout which we'll bias using features (-1 for no limit).")
				.withDefault(Integer.valueOf(-1))
				.withNumVals(1)
				.withType(OptionTypes.Int));
		argParse.addOption(new ArgOption()
				.withNames("--num-agent-threads")
				.help("Number of threads to use for Tree Parallelisation in MCTS-based agents.")
				.withDefault(Integer.valueOf(1))
				.withNumVals(1)
				.withType(OptionTypes.Int));
		
		argParse.addOption(new ArgOption()
				.withNames("-n", "--num-games", "--num-training-games")
				.help("Number of training games to run.")
				.withDefault(Integer.valueOf(200))
				.withNumVals(1)
				.withType(OptionTypes.Int));
		argParse.addOption(new ArgOption()
				.withNames("--batch-size")
				.help("Max size of minibatches in training.")
				.withDefault(Integer.valueOf(80))
				.withNumVals(1)
				.withType(OptionTypes.Int));
		argParse.addOption(new ArgOption()
				.withNames("--buffer-size", "--experience-buffer-size")
				.help("Max size of the experience buffer.")
				.withDefault(Integer.valueOf(2500))
				.withNumVals(1)
				.withType(OptionTypes.Int));
		argParse.addOption(new ArgOption()
				.withNames("--update-weights-every")
				.help("After this many moves (decision points) in training games, we update weights.")
				.withDefault(Integer.valueOf(1))
				.withNumVals(1)
				.withType(OptionTypes.Int));
		argParse.addOption(new ArgOption()
				.withNames("--prioritized-experience-replay", "--per")
				.help("If true, we'll use prioritized experience replay")
				.withType(OptionTypes.Boolean));
		argParse.addOption(new ArgOption()
				.withNames("--init-value-func-dir")
				.help("Directory from which to attempt extracting an initial value function.")
				.withDefault("")
				.withNumVals(1)
				.withType(OptionTypes.String));
		argParse.addOption(new ArgOption()
				.withNames("--num-policy-gradient-epochs")
				.help("Number of epochs to run with policy gradients.")
				//.withDefault(Integer.valueOf(100))
				.withDefault(Integer.valueOf(0))
				.withNumVals(1)
				.withType(OptionTypes.Int));
		argParse.addOption(new ArgOption()
				.withNames("--num-trials-per-policy-gradient-epoch")
				.help("Number of trials to run per epoch for policy gradients.")
				.withDefault(Integer.valueOf(60))
				.withNumVals(1)
				.withType(OptionTypes.Int));
		argParse.addOption(new ArgOption()
				.withNames("--pg-gamma")
				.help("Discount factor gamma for policy gradients (excluding TSPG).")
				.withDefault(Double.valueOf(0.9))
				.withNumVals(1)
				.withType(OptionTypes.Double));
		argParse.addOption(new ArgOption()
				.withNames("--entropy-reg-weight")
				.help("Weight for entropy regularization in policy gradients.")
				.withDefault(Double.valueOf(0.1))
				.withNumVals(1)
				.withType(OptionTypes.Double));
		argParse.addOption(new ArgOption()
				.withNames("--num-policy-gradient-threads")
				.help("Number of threads to use for parallel trials for policy gradients.")
				.withDefault(Integer.valueOf(1))
				.withNumVals(1)
				.withType(OptionTypes.Int));
		argParse.addOption(new ArgOption()
				.withNames("--post-pg-weight-scalar")
				.help("After running policy gradients, scale weights by this value.")
				.withDefault(Double.valueOf(0.01))
				.withNumVals(1)
				.withType(OptionTypes.Double));
		
		argParse.addOption(new ArgOption()
				.withNames("--add-feature-every")
				.help("After this many training games, we add a new feature.")
				.withDefault(Integer.valueOf(1))
				.withNumVals(1)
				.withType(OptionTypes.Int));
		argParse.addOption(new ArgOption()
				.withNames("--no-grow-features", "--no-grow-featureset", "--no-grow-feature-set")
				.help("If true, we'll not grow feature set (but still train weights).")
				.withType(OptionTypes.Boolean));
		argParse.addOption(new ArgOption()
				.withNames("--combining-feature-instance-threshold")
				.help("At most this number of feature instances will be taken into account when combining features.")
				.withDefault(Integer.valueOf(60))
				.withNumVals(1)
				.withType(OptionTypes.Int));
		argParse.addOption(new ArgOption()
				.withNames("--num-feature-discovery-threads")
				.help("Number of threads to use for parallel feature discovery.")
				.withDefault(Integer.valueOf(1))
				.withNumVals(1)
				.withType(OptionTypes.Int));
		argParse.addOption(new ArgOption()
				.withNames("--critical-value-corr-conf")
				.help("Critical value used when computing confidence intervals for correlations ")
				//.withDefault(Double.valueOf(1.64))
				.withDefault(Double.valueOf(0.00))
				.withNumVals(1)
				.withType(OptionTypes.Double));
		argParse.addOption(new ArgOption()
				.withNames("--special-moves-expander")
				.help("If true, we'll use a special-moves feature expander in addition to the normal one.")
				.withType(OptionTypes.Boolean));
		argParse.addOption(new ArgOption()
				.withNames("--special-moves-expander-split")
				.help("If true, we'll use a special-moves feature expander in addition to the normal one, splitting time with the normal one.")
				.withType(OptionTypes.Boolean));
		argParse.addOption(new ArgOption()
				.withNames("--expander-type")
				.help("Type of feature set expander to use.")
				.withNumVals(1)
				.withType(OptionTypes.String)
				.withDefault("CorrelationBasedExpander")
				.withLegalVals("CorrelationBasedExpander", "CorrelationErrorSignExpander", "Random"));
		
		argParse.addOption(new ArgOption()
				.withNames("--train-tspg")
				.help("If true, we'll train a policy on TSPG objective (see COG paper).")
				.withType(OptionTypes.Boolean));
		argParse.addOption(new ArgOption()
				.withNames("--is-episode-durations")
				.help("If true, we'll use importance sampling weights based on episode durations for CE-loss.")
				.withType(OptionTypes.Boolean));
		argParse.addOption(new ArgOption()
				.withNames("--wis", "--weighted-importance-sampling")
				.help("If true, we use Weighted Importance Sampling instead of Ordinary Importance Sampling for any of the above")
				.withType(OptionTypes.Boolean));
		argParse.addOption(new ArgOption()
				.withNames("--no-value-learning")
				.help("If true, we don't do any value function learning.")
				.withType(OptionTypes.Boolean));
		argParse.addOption(new ArgOption()
				.withNames("--handle-aliasing")
				.help("If true, we handle move aliasing by putting the maximum mass among all aliased moves on each of them, for training selection policy.")
				.withType(OptionTypes.Boolean));
		argParse.addOption(new ArgOption()
				.withNames("--handle-aliasing-playouts")
				.help("If true, we handle move aliasing by putting the maximum mass among all aliased moves on each of them, for training playout policy.")
				.withType(OptionTypes.Boolean));
		argParse.addOption(new ArgOption()
				.withNames("--weight-decay-lambda")
				.help("Lambda param for weight decay")
				.withDefault(Double.valueOf(0.000001))
				.withNumVals(1)
				.withType(OptionTypes.Double));
		
		argParse.addOption(new ArgOption()
				.withNames("--selection-optimiser")
				.help("Optimiser to use for policy trained for Selection phase.")
				.withDefault("RMSProp")
				.withNumVals(1)
				.withType(OptionTypes.String));
		argParse.addOption(new ArgOption()
				.withNames("--playout-optimiser")
				.help("Optimiser to use for policy trained for Playouts.")
				.withDefault("RMSProp")
				.withNumVals(1)
				.withType(OptionTypes.String));
		argParse.addOption(new ArgOption()
				.withNames("--cee-optimiser", "--cross-entropy-exploration-optimiser")
				.help("Optimiser to use for training Cross-Entropy Exploration policy.")
				.withDefault("RMSProp")
				.withNumVals(1)
				.withType(OptionTypes.String));
		argParse.addOption(new ArgOption()
				.withNames("--tspg-optimiser")
				.help("Optimiser to use for policy trained on TSPG objective (see COG paper).")
				.withDefault("RMSProp")
				.withNumVals(1)
				.withType(OptionTypes.String));
		argParse.addOption(new ArgOption()
				.withNames("--value-optimiser")
				.help("Optimiser to use for value function optimisation.")
				.withDefault("RMSProp")
				.withNumVals(1)
				.withType(OptionTypes.String));
		
		argParse.addOption(new ArgOption()
				.withNames("--checkpoint-type", "--checkpoints")
				.help("When do we store checkpoints of trained weights?")
				.withDefault(CheckpointTypes.Game.toString())
				.withNumVals(1)
				.withType(OptionTypes.String)
				.withLegalVals(Arrays.stream(CheckpointTypes.values()).map(Object::toString).toArray()));
		argParse.addOption(new ArgOption()
				.withNames("--checkpoint-freq", "--checkpoint-frequency")
				.help("Frequency of checkpoint updates")
				.withDefault(Integer.valueOf(1))
				.withNumVals(1)
				.withType(OptionTypes.Int));
		argParse.addOption(new ArgOption()
				.withNames("--out-dir", "--output-directory")
				.help("Filepath for output directory")
				.withNumVals(1)
				.withType(OptionTypes.String));
		argParse.addOption(new ArgOption()
				.withNames("--no-logging")
				.help("If true, we don't write a bunch of messages to a log file.")
				.withType(OptionTypes.Boolean));
		
		argParse.addOption(new ArgOption()
				.withNames("--useGUI")
				.help("Whether to create a small GUI that can be used to "
						+ "manually interrupt training run. False by default."));
		argParse.addOption(new ArgOption()
				.withNames("--max-wall-time")
				.help("Max wall time in minutes (or -1 for no limit).")
				.withDefault(Integer.valueOf(-1))
				.withNumVals(1)
				.withType(OptionTypes.Int));
		
		// parse the args
		if (!argParse.parseArguments(args))
			return;
		
		// use the parsed args
		final ExpertIteration exIt = 
				new ExpertIteration
				(
					argParse.getValueBool("--useGUI"), 
					argParse.getValueInt("--max-wall-time")
				);
		
		exIt.gameParams.gameName = argParse.getValueString("--game");
		exIt.gameParams.gameOptions = (List<String>) argParse.getValue("--game-options"); 
		exIt.gameParams.ruleset = argParse.getValueString("--ruleset");
		exIt.gameParams.gameLengthCap = argParse.getValueInt("--game-length-cap");
		
		exIt.agentsParams.expertAI = argParse.getValueString("--expert-ai");
		exIt.agentsParams.bestAgentsDataDir = argParse.getValueString("--best-agents-data-dir");
		exIt.agentsParams.thinkingTime = argParse.getValueDouble("--thinking-time");
		exIt.agentsParams.iterationLimit = argParse.getValueInt("--iteration-limit");
		exIt.agentsParams.depthLimit = argParse.getValueInt("--depth-limit");
		exIt.agentsParams.tournamentMode = argParse.getValueBool("--tournament-mode");
		exIt.agentsParams.playoutFeaturesEpsilon = argParse.getValueDouble("--playout-features-epsilon");
		exIt.agentsParams.maxNumBiasedPlayoutActions = argParse.getValueInt("--max-num-biased-playout-actions");
		exIt.agentsParams.numAgentThreads = argParse.getValueInt("--num-agent-threads");
		
		exIt.trainingParams.numTrainingGames = argParse.getValueInt("-n");
		exIt.trainingParams.batchSize = argParse.getValueInt("--batch-size");
		exIt.trainingParams.experienceBufferSize = argParse.getValueInt("--buffer-size");
		exIt.trainingParams.updateWeightsEvery = argParse.getValueInt("--update-weights-every");
		exIt.trainingParams.prioritizedExperienceReplay = argParse.getValueBool("--prioritized-experience-replay");
		exIt.trainingParams.initValueFuncDir = argParse.getValueString("--init-value-func-dir");
		exIt.trainingParams.numPolicyGradientEpochs = argParse.getValueInt("--num-policy-gradient-epochs");
		exIt.trainingParams.numTrialsPerPolicyGradientEpoch = argParse.getValueInt("--num-trials-per-policy-gradient-epoch");
		exIt.trainingParams.pgGamma = argParse.getValueDouble("--pg-gamma");
		exIt.trainingParams.entropyRegWeight = argParse.getValueDouble("--entropy-reg-weight");
		exIt.trainingParams.numPolicyGradientThreads = argParse.getValueInt("--num-policy-gradient-threads");
		exIt.trainingParams.postPGWeightScalar = argParse.getValueDouble("--post-pg-weight-scalar");
		
		exIt.featureDiscoveryParams.addFeatureEvery = argParse.getValueInt("--add-feature-every");
		exIt.featureDiscoveryParams.noGrowFeatureSet = argParse.getValueBool("--no-grow-features");
		exIt.featureDiscoveryParams.combiningFeatureInstanceThreshold = argParse.getValueInt("--combining-feature-instance-threshold");
		exIt.featureDiscoveryParams.numFeatureDiscoveryThreads = argParse.getValueInt("--num-feature-discovery-threads");
		exIt.featureDiscoveryParams.criticalValueCorrConf = argParse.getValueDouble("--critical-value-corr-conf");
		exIt.featureDiscoveryParams.useSpecialMovesExpander = argParse.getValueBool("--special-moves-expander");
		exIt.featureDiscoveryParams.useSpecialMovesExpanderSplit = argParse.getValueBool("--special-moves-expander-split");
		exIt.featureDiscoveryParams.expanderType = argParse.getValueString("--expander-type");
		
		exIt.objectiveParams.trainTSPG = argParse.getValueBool("--train-tspg");
		exIt.objectiveParams.importanceSamplingEpisodeDurations = argParse.getValueBool("--is-episode-durations");
		exIt.objectiveParams.weightedImportanceSampling = argParse.getValueBool("--wis");
		exIt.objectiveParams.noValueLearning = argParse.getValueBool("--no-value-learning");
		exIt.objectiveParams.handleAliasing = argParse.getValueBool("--handle-aliasing");
		exIt.objectiveParams.handleAliasingPlayouts = argParse.getValueBool("--handle-aliasing-playouts");
		exIt.objectiveParams.weightDecayLambda = argParse.getValueDouble("--weight-decay-lambda");
		
		exIt.optimisersParams.selectionOptimiserConfig = argParse.getValueString("--selection-optimiser");
		exIt.optimisersParams.playoutOptimiserConfig = argParse.getValueString("--playout-optimiser");
		exIt.optimisersParams.ceExploreOptimiserConfig = argParse.getValueString("--cee-optimiser");
		exIt.optimisersParams.tspgOptimiserConfig = argParse.getValueString("--tspg-optimiser");
		exIt.optimisersParams.valueOptimiserConfig = argParse.getValueString("--value-optimiser");

		exIt.outParams.checkpointType = CheckpointTypes.valueOf(argParse.getValueString("--checkpoint-type"));
		exIt.outParams.checkpointFrequency = argParse.getValueInt("--checkpoint-freq");
		final String outDirFilepath = argParse.getValueString("--out-dir");
		if (outDirFilepath != null)
			exIt.outParams.outDir = new File(outDirFilepath);
		else
			exIt.outParams.outDir = null;
		exIt.outParams.noLogging = argParse.getValueBool("--no-logging");
		
		exIt.startExperiment();
	}

}
