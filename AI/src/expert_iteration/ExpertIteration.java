package expert_iteration;

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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import expert_iteration.feature_discovery.CorrelationBasedExpander;
import expert_iteration.feature_discovery.FeatureSetExpander;
import expert_iteration.menageries.Menagerie;
import expert_iteration.menageries.Menagerie.DrawnAgentsData;
import expert_iteration.menageries.NaiveSelfPlay;
import expert_iteration.menageries.TournamentMenagerie;
import expert_iteration.params.AgentsParams;
import expert_iteration.params.FeatureDiscoveryParams;
import expert_iteration.params.GameParams;
import expert_iteration.params.ObjectiveParams;
import expert_iteration.params.OptimisersParams;
import expert_iteration.params.OutParams;
import expert_iteration.params.OutParams.CheckpointTypes;
import expert_iteration.params.TrainingParams;
import features.FeatureVector;
import features.WeightVector;
import features.feature_sets.BaseFeatureSet;
import features.feature_sets.network.JITSPatterNetFeatureSet;
import features.generation.AtomicFeatureGenerator;
import features.spatial.Pattern;
import features.spatial.SpatialFeature;
import features.spatial.elements.FeatureElement;
import features.spatial.elements.RelativeFeatureElement;
import function_approx.BoostedLinearFunction;
import function_approx.LinearFunction;
import game.Game;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.set.hash.TIntHashSet;
import main.CommandLineArgParse;
import main.CommandLineArgParse.ArgOption;
import main.CommandLineArgParse.OptionTypes;
import main.FileHandling;
import main.collections.FVector;
import main.collections.FastArrayList;
import main.grammar.Report;
import metadata.ai.heuristics.Heuristics;
import optimisers.Optimiser;
import optimisers.OptimiserFactory;
import other.GameLoader;
import other.RankUtils;
import other.context.Context;
import other.move.Move;
import other.trial.Trial;
import policies.softmax.SoftmaxPolicy;
import search.mcts.MCTS;
import search.mcts.utils.RegPolOptMCTS;
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
			
			/** Filenames corresponding to our current policy weights optimised for CE */
			protected String[] currentPolicyWeightsCEFilenames;
			
			/** Filenames corresponding to our current policy weights optimised for TSPG */
			protected String[] currentPolicyWeightsTSPGFilenames;
			
			/** Filenames corresponding to our current policy weights optimised for CE exploration */
			protected String[] currentPolicyWeightsCEEFilenames;
			
			/** Filename for our current heuristics and weights for value function */
			protected String currentValueFunctionFilename;
			
			/** Filenames corresponding to our current experience buffers */
			protected String[] currentExperienceBufferFilenames;
			
			/** Filenames corresponding to our current moving average trackers of game durations */
			protected String[] currentGameDurationTrackerFilenames;
			
			/** Filenames corresponding to our current optimisers for CE */
			protected String[] currentOptimiserCEFilenames;
			
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
				currentPolicyWeightsCEFilenames = new String[numPlayers + 1];
				currentPolicyWeightsTSPGFilenames = new String[numPlayers + 1];
				currentPolicyWeightsCEEFilenames = new String[numPlayers + 1];
				currentValueFunctionFilename = null;
				currentExperienceBufferFilenames = new String[numPlayers + 1];
				currentGameDurationTrackerFilenames = new String[numPlayers + 1];
				currentOptimiserCEFilenames = new String[numPlayers + 1];
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
				
				// TODO add log statements describing complete setup of experiment
				
				// Create menagerie
				final Menagerie menagerie;
				
				if (agentsParams.tournamentMode)
					menagerie = new TournamentMenagerie();
				else
					menagerie = new NaiveSelfPlay();
				
				// prepare our feature sets
				BaseFeatureSet[] featureSets = prepareFeatureSets();
				
				// For every feature set, a list for every feature of its lifetime (how often it could've been active)
				TLongArrayList[] featureLifetimes = new TLongArrayList[featureSets.length];
				// For every feature set, a list for every feature of the ratio of cases in which it was active
				TDoubleArrayList[] featureActiveRatios = new TDoubleArrayList[featureSets.length];
				
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
					}
				}
				
				// prepare our linear functions
				final LinearFunction[] crossEntropyFunctions = prepareCrossEntropyFunctions(featureSets);
				final LinearFunction[] tspgFunctions = prepareTSPGFunctions(featureSets, crossEntropyFunctions);
				
				// create our policies
				final SoftmaxPolicy cePolicy = 
						new SoftmaxPolicy
						(
							crossEntropyFunctions, 
							featureSets,
							agentsParams.maxNumBiasedPlayoutActions
						);
				
				final SoftmaxPolicy tspgPolicy = 
						new SoftmaxPolicy
						(
							tspgFunctions, 
							featureSets,
							agentsParams.maxNumBiasedPlayoutActions
						);
				
				final FeatureSetExpander featureSetExpander = new CorrelationBasedExpander();
				
				// create our value function
				final Heuristics valueFunction = prepareValueFunction();
				
				// construct optimisers
				final Optimiser[] ceOptimisers = prepareCrossEntropyOptimisers();
				final Optimiser[] tspgOptimisers = prepareTSPGOptimisers();
				final Optimiser valueFunctionOptimiser = prepareValueFunctionOptimiser();
				
				// Initialise menagerie's population
				menagerie.initialisePopulation(game, agentsParams, cePolicy.generateFeaturesMetadata(), Heuristics.copy(valueFunction));
				
				// instantiate trial / context
				final Trial trial = new Trial(game);
				final Context context = new Context(game, trial);
				
				// prepare our replay buffers (we use one per player)
				final ExperienceBuffer[] experienceBuffers = prepareExperienceBuffers(trainingParams.prioritizedExperienceReplay);
				
				// keep track of average game duration (separate per player) 
				final ExponentialMovingAverage[] avgGameDurations = prepareGameDurationTrackers();
				
				// our big game-playing loop
				long actionCounter = 0L;
				long weightsUpdateCounter = (outParams.checkpointType == CheckpointTypes.WeightUpdate) ? lastCheckpoint : 0L;

				int gameCounter = 0;

				if (outParams.checkpointType == CheckpointTypes.Game && lastCheckpoint >= 0L)
				{
					gameCounter = (int)lastCheckpoint;
					trainingParams.numTrainingGames += lastCheckpoint;
				}
				
				for (/**/; gameCounter < trainingParams.numTrainingGames; ++gameCounter)
				{
					checkWallTime(0.05);
					
					if (interrupted) // time to abort the experiment
					{
						logLine(logWriter, "interrupting experiment...");
						break;
					}
					
					saveCheckpoints
					(
						gameCounter, 
						weightsUpdateCounter, 
						featureSets, 
						crossEntropyFunctions, 
						tspgFunctions,
						valueFunction,
						experienceBuffers,
						ceOptimisers,
						tspgOptimisers,
						valueFunctionOptimiser,
						avgGameDurations,
						false
					);
					
					final BaseFeatureSet[] expandedFeatureSets = new BaseFeatureSet[numPlayers + 1];
					
					if 
					(
						!featureDiscoveryParams.noGrowFeatureSet 
						&& 
						gameCounter > 0 
						&& 
						gameCounter % featureDiscoveryParams.addFeatureEvery == 0
					)
					{
						if (trainingParams.sharedFeatureSet)
						{
							// Sample some batches from each player's replay buffer for feature set growing
							final List<ExItExperience> batch = new ArrayList<ExItExperience>();
							
							for (int p = 1; p <= numPlayers; ++p)
							{
								final int batchSize = Math.max(1, (int) Math.ceil((double) trainingParams.batchSize / numPlayers));
								
								for (final ExItExperience exp : experienceBuffers[p].sampleExperienceBatchUniformly(batchSize))
								{
									batch.add(exp);
								}
							}
							
							if (batch.size() > 0)
							{
								final long startTime = System.currentTimeMillis();
								final BaseFeatureSet expandedFeatureSet = 
										featureSetExpander.expandFeatureSet
										(
											batch.toArray(new ExItExperience[batch.size()]),
											featureSets[0],
											cePolicy,
											game,
											featureDiscoveryParams.combiningFeatureInstanceThreshold,
											featureActiveRatios[0],
											objectiveParams, 
											logWriter,
											this
										);

								if (expandedFeatureSet != null)
								{
									final int[] supportedPlayers = new int[game.players().count()];
									for (int i = 0; i < supportedPlayers.length; ++i)
									{
										supportedPlayers[i] = i + 1;
									}
									
									expandedFeatureSets[0] = expandedFeatureSet;
									expandedFeatureSet.init(game, supportedPlayers, null);
								}
								else
								{
									expandedFeatureSets[0] = featureSets[0];
								}

								logLine
								(
									logWriter,
									"Expanded feature set in " + (System.currentTimeMillis() - startTime) + " ms for P" + 0 + "."
								);
							}
							else
							{
								expandedFeatureSets[0] = featureSets[0];
							}
						}
						else
						{
							for (int p = 1; p <= numPlayers; ++p)
							{
								// we'll sample a batch from our replay buffer, and grow feature set
								final ExItExperience[] batch = experienceBuffers[p].sampleExperienceBatchUniformly(trainingParams.batchSize);
								
								if (batch.length > 0)
								{
									final long startTime = System.currentTimeMillis();
									final BaseFeatureSet expandedFeatureSet = 
											featureSetExpander.expandFeatureSet
											(
												batch,
												featureSets[p],
												cePolicy,
												game,
												featureDiscoveryParams.combiningFeatureInstanceThreshold,
												featureActiveRatios[p],
												objectiveParams, 
												logWriter,
												this
											);
									
									if (expandedFeatureSet != null)
									{
										expandedFeatureSets[p] = expandedFeatureSet;
										expandedFeatureSet.init(game, new int[]{p}, null);
										
										// Add new entries for lifetime and average activity
										while (featureActiveRatios[p].size() < expandedFeatureSet.getNumSpatialFeatures())
										{
											featureActiveRatios[p].add(0.0);
											featureLifetimes[p].add(0L);
										}
									}
									else
									{
										expandedFeatureSets[p] = featureSets[p];
									}
									
									logLine
									(
										logWriter,
										"Expanded feature set in " + (System.currentTimeMillis() - startTime) + " ms for P" + p + "."
									);
								}
								else
								{
									expandedFeatureSets[p] = featureSets[p];
								}
							}
						}

						cePolicy.updateFeatureSets(expandedFeatureSets);
						menagerie.updateDevFeatures(cePolicy.generateFeaturesMetadata());
						
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
						experts.get(p).initAI(game, p);
						gameExperienceSamples.add(new ArrayList<ExItExperience>());
						
						if (objectiveParams.trainTSPG && !(experts.get(p) instanceof MCTS))
							System.err.println("A non-MCTS expert cannot be used for training the TSPG objective!");
						if (objectiveParams.expDeltaValWeighting && !(experts.get(p) instanceof MCTS))
							System.err.println("A non-MCTS expert cannot be used for training with expected delta value weighting!");
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

						final FVector expertDistribution;
						if (objectiveParams.mctsRegPolOpt)
							expertDistribution = RegPolOptMCTS.computePiBar(((MCTS)expert).rootNode(), 2.5);	// TODO this 2.5 should track hyperparam in Selection
						else
							expertDistribution = expert.computeExpertPolicy(1.0);

						final Move move = legalMoves.get(expertDistribution.sampleProportionally());	
							
						// collect experience for this game (don't store in buffer yet, don't know episode duration or value)
						final ExItExperience newExperience = expert.generateExItExperience();
						
						if (valueFunction != null)
							newExperience.setStateFeatureVector(valueFunction.computeStateFeatureVector(context, mover));
						
						// Update feature lifetimes and active ratios		TODO refactor into method
						{
							final TIntArrayList[] sparseFeatureVectors = 
									featureSets[mover].computeSparseSpatialFeatureVectors(context, legalMoves, false);
							
							for (final TIntArrayList featureVector : sparseFeatureVectors)
							{
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
						}
						
						gameExperienceSamples.get(mover).add(newExperience);
						
						// apply chosen action
						game.apply(context, move);
						++actionCounter;
						
						if (actionCounter % trainingParams.updateWeightsEvery == 0)
						{
							// Time to update our weights a bit (once for every player-specific model)
							final int startP = trainingParams.sharedFeatureSet ? 0 : 1;
							for (int p = startP; p <= numPlayers; ++p)
							{
								final ExItExperience[] batch;
								final int featureSetIdx = trainingParams.sharedFeatureSet ? 0 : p;
								
								if (p == 0)
								{
									// Sample some batches from each player's replay buffer for feature set growing
									final List<ExItExperience> samples = new ArrayList<ExItExperience>();
									
									for (int i = 1; i <= numPlayers; ++i)
									{
										final int batchSize = Math.max(1, (int) Math.ceil((double) trainingParams.batchSize / numPlayers));
										
										for (final ExItExperience exp : experienceBuffers[i].sampleExperienceBatchUniformly(batchSize))
										{
											samples.add(exp);
										}
									}
									
									batch = samples.toArray(new ExItExperience[samples.size()]);
								}
								else 
								{
									// Sample batch only for this player
									batch = experienceBuffers[p].sampleExperienceBatch(trainingParams.batchSize);
								}

								if (batch.length == 0)
									continue;
								
								final List<FVector> gradientsCE = new ArrayList<FVector>(batch.length);
								final List<FVector> gradientsTSPG = new ArrayList<FVector>(batch.length);
								final List<FVector> gradientsCEExplore = new ArrayList<FVector>(batch.length);
								final List<FVector> gradientsValueFunction = new ArrayList<FVector>(batch.length);
								
								// for PER
								final int[] indices = new int[batch.length];
								final float[] priorities = new float[batch.length];
								
								// for WIS
								double sumImportanceSamplingWeights = 0.0;
								
								for (int idx = 0; idx < batch.length; ++idx)
								{
									final ExItExperience sample = batch[idx];
									final FeatureVector[] featureVectors = 
											featureSets[featureSetIdx].computeFeatureVectors
											(
												sample.state().state(),
												sample.state().lastDecisionMove(),
												sample.moves(), 
												false
											);
									
									// Note: NOT using sample.state().state().mover(), but p here, important to update
									// shared weights correctly!
									final FVector apprenticePolicy = cePolicy.computeDistribution(featureVectors, p);
									FVector expertPolicy = sample.expertDistribution();
									
									if (objectiveParams.handleAliasing)
									{
										// Need to handle aliased moves
										final Map<FeatureVector, TIntArrayList> movesPerFeatureVector = 
											new HashMap<FeatureVector, TIntArrayList>();
										for (int moveIdx = 0; moveIdx < featureVectors.length; ++moveIdx)
										{
											final FeatureVector featureVector = featureVectors[moveIdx];
											if (!movesPerFeatureVector.containsKey(featureVector))
												movesPerFeatureVector.put(featureVector, new TIntArrayList());
											
											movesPerFeatureVector.get(featureVector).add(moveIdx);
										}
										
										expertPolicy = expertPolicy.copy();		// don't want to permanently modify the original
										
										final boolean[] alreadyUpdatedValue = new boolean[expertPolicy.dim()];
										for (int moveIdx = 0; moveIdx < expertPolicy.dim(); ++moveIdx)
										{
											if (alreadyUpdatedValue[moveIdx])
												continue;
											
											final TIntArrayList aliasedMoves = movesPerFeatureVector.get(featureVectors[moveIdx]);
											if (aliasedMoves.size() > 1)
											{
												float maxVal = 0.f;
												for (int i = 0; i < aliasedMoves.size(); ++i)
												{
													final float val = expertPolicy.get(aliasedMoves.getQuick(i));
													if (val > maxVal)
														maxVal = val;
												}
												
												// Set all aliased moves to the max probability
												for (int i = 0; i < aliasedMoves.size(); ++i)
												{
													expertPolicy.set(aliasedMoves.getQuick(i), maxVal);
													alreadyUpdatedValue[aliasedMoves.getQuick(i)] = true;
												}
											}
										}
										
										// Renormalise the expert policy
										expertPolicy.normalise();
									}
									
									// First gradients for Cross-Entropy
									final FVector errors = cePolicy.computeDistributionErrors(apprenticePolicy, expertPolicy);
									
//									System.out.println("---------------------------------------------------");
//									for (final Entry<TIntArrayList, TIntArrayList> entry : movesPerFeatureVector.entrySet())
//									{
//										if (entry.getValue().size() > 1)
//										{
//											System.out.print("Errors for repeated feature vector: ");
//											for (int moveIdx = 0; moveIdx < entry.getValue().size(); ++moveIdx)
//											{
//												System.out.print(errors.get(moveIdx) + ", ");
//											}
//											System.out.println();
//										}
//									}
//									System.out.println("---------------------------------------------------");
									
									final FVector ceGradients = cePolicy.computeParamGradients
										(
											errors,
											featureVectors,
											p
										);
									
									FVector valueGradients = null;
									if (valueFunction != null && p > 0)
									{
										// Compute gradients for value function
										final FVector valueFunctionParams = valueFunction.paramsVector();
										final float predictedValue = (float) Math.tanh(valueFunctionParams.dot(sample.stateFeatureVector()));
										final float gameOutcome = (float) sample.playerOutcomes()[sample.state().state().mover()];
										
										final float valueError = predictedValue - gameOutcome;
										valueGradients = new FVector(valueFunctionParams.dim());
										
										// Need to multiply this by feature value to compute gradient per feature
										final float gradDivFeature = 2.f * valueError * (1.f - predictedValue*predictedValue);
										
										for (int i = 0; i < valueGradients.dim(); ++i)
										{
											valueGradients.set(i, gradDivFeature * sample.stateFeatureVector().get(i));
										}
										
//										System.out.println();
//										System.out.println("State Features = " + sample.stateFeatureVector());
//										System.out.println("pred. value = " + predictedValue);
//										System.out.println("observed outcome = " + gameOutcome);
//										System.out.println("value error = " + valueError);
//										System.out.println("value grads = " + valueGradients);
//										System.out.println();
									}

									double importanceSamplingWeight = 1.0;
									double nonImportanceSamplingWeight = 1.0;	// Also used to scale gradients, but doesn't count as IS
									
									if (objectiveParams.importanceSamplingEpisodeDurations)
										importanceSamplingWeight *= (avgGameDurations[sample.state().state().mover()].movingAvg() / sample.episodeDuration());
									
									if (trainingParams.prioritizedExperienceReplay)
									{
										final FVector absErrors = errors.copy();
										absErrors.abs();
										
										// Minimum priority of 0.05 to avoid crashes with 0-error samples
										priorities[idx] = Math.max(0.05f, absErrors.sum());
										importanceSamplingWeight *= sample.weightPER();
										indices[idx] = sample.bufferIdx();
									}
									
									if (objectiveParams.expDeltaValWeighting)
									{
										// Compute expected values of expert and apprentice policies
										double expValueExpert = 0.0;
										double expValueApprentice = 0.0;
										final FVector expertQs = sample.expertValueEstimates();
										
										for (int i = 0; i < expertQs.dim(); ++i)
										{
											expValueExpert += expertQs.get(i) * expertPolicy.get(i);
											expValueApprentice += expertQs.get(i) * apprenticePolicy.get(i);
										}
										
										// Update weight
										final double expDeltaValWeight = Math.max(
												objectiveParams.expDeltaValWeightingLowerClip, 
												(expValueExpert - expValueApprentice));
										nonImportanceSamplingWeight *= expDeltaValWeight;
										
										if (trainingParams.prioritizedExperienceReplay)
										{
											// Also scale the priorities for replay buffer
											// Minimum priority of 0.025 to avoid crashes with 0-error samples
											priorities[idx] = Math.max(0.025f, priorities[idx] * 2.f * (float)expDeltaValWeight);
										}
									}
									
									sumImportanceSamplingWeights += importanceSamplingWeight;
									ceGradients.mult((float) (importanceSamplingWeight * nonImportanceSamplingWeight));
									gradientsCE.add(ceGradients);
									
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

										gradientsTSPG.add(grads);
									}
								}
								
								final FVector meanGradientsCE;
								FVector meanGradientsValue = null;
								
								if (objectiveParams.weightedImportanceSampling)
								{
									// for WIS, we don't divide by number of vectors, but by sum of IS weights
									meanGradientsCE = gradientsCE.get(0).copy();
									for (int i = 1; i < gradientsCE.size(); ++i)
									{
										meanGradientsCE.add(gradientsCE.get(i));
									}
									
									if (sumImportanceSamplingWeights > 0.0)
										meanGradientsCE.div((float)sumImportanceSamplingWeights);
									
									if (!gradientsValueFunction.isEmpty())
									{
										meanGradientsValue = gradientsValueFunction.get(0).copy();
										for (int i = 1; i < gradientsValueFunction.size(); ++i)
										{
											meanGradientsValue.add(gradientsValueFunction.get(i));
										}
										
										if (sumImportanceSamplingWeights > 0.0)
											meanGradientsValue.div((float)sumImportanceSamplingWeights);
									}
								}
								else
								{
									meanGradientsCE = FVector.mean(gradientsCE);
									
									if (!gradientsValueFunction.isEmpty())
										meanGradientsValue = FVector.mean(gradientsValueFunction);
								}
								
								final FVector weightDecayVector = new FVector(crossEntropyFunctions[p].trainableParams().allWeights());
								weightDecayVector.mult((float) objectiveParams.weightDecayLambda);
								
								ceOptimisers[p].minimiseObjective(crossEntropyFunctions[p].trainableParams().allWeights(), meanGradientsCE);
								
								if (p > 0)	// No weight decay for shared params
									crossEntropyFunctions[p].trainableParams().allWeights().subtract(weightDecayVector);
								
								menagerie.updateDevFeatures(cePolicy.generateFeaturesMetadata());
								
								if (meanGradientsValue != null)
								{
									final FVector valueFunctionParams = valueFunction.paramsVector();
									valueFunctionOptimiser.minimiseObjective(valueFunctionParams, meanGradientsValue);
									valueFunction.updateParams(game, valueFunctionParams, 0);
									
									menagerie.updateDevHeuristics(Heuristics.copy(valueFunction));
								}
								
								if (objectiveParams.trainTSPG && p > 0)
								{
									final FVector meanGradientsTSPG = FVector.mean(gradientsTSPG);
									tspgOptimisers[p].maximiseObjective(tspgFunctions[p].trainableParams().allWeights(), meanGradientsTSPG);
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
						// game is over, we can now store all experience collected in the real buffers
						for (int p = 1; p <= numPlayers; ++p)
						{
							Collections.shuffle(gameExperienceSamples.get(p), ThreadLocalRandom.current());
							
							// Note: not really game duration! Just from perspective of one player!
							final int gameDuration = gameExperienceSamples.get(p).size();
							avgGameDurations[p].observe(gameDuration);
							
							final double[] playerOutcomes = RankUtils.agentUtilities(context);
							
							for (final ExItExperience experience : gameExperienceSamples.get(p))
							{
								experience.setEpisodeDuration(gameDuration);
								experience.setPlayerOutcomes(playerOutcomes);
								experienceBuffers[p].add(experience);
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
				
				// final forced save of checkpoints at end of run
				saveCheckpoints
				(
					gameCounter + 1, 
					weightsUpdateCounter, 
					featureSets, 
					crossEntropyFunctions, 
					tspgFunctions,
					valueFunction,
					experienceBuffers,
					ceOptimisers,
					tspgOptimisers,
					valueFunctionOptimiser,
					avgGameDurations,
					true
				);
				
				final String menagerieLog = menagerie.generateLog();
				if (menagerieLog != null)
					logLine(logWriter, menagerie.generateLog());
			}
			
			//-----------------------------------------------------------------
			
			/**
			 * Creates (or loads) optimisers for CE (one per player)
			 * 
			 * @return
			 */
			private Optimiser[] prepareCrossEntropyOptimisers()
			{
				final Optimiser[] optimisers = new Optimiser[numPlayers + 1];
				final int startP = trainingParams.sharedFeatureSet ? 0 : 1;
				
				for (int p = startP; p <= numPlayers; ++p)
				{
					Optimiser optimiser = null;
					
					currentOptimiserCEFilenames[p] = getFilenameLastCheckpoint("OptimiserCE_P" + p, "opt");
					lastCheckpoint = 
							Math.min
							(
								lastCheckpoint,
								extractCheckpointFromFilename(currentOptimiserCEFilenames[p], "OptimiserCE_P" + p, "opt")
							);
					//System.out.println("CE opt set lastCheckpoint = " + lastCheckpoint);
					
					if (currentOptimiserCEFilenames[p] == null)
					{
						// create new optimiser
						optimiser = OptimiserFactory.createOptimiser(optimisersParams.crossEntropyOptimiserConfig);
						logLine(logWriter, "starting with new optimiser for Cross-Entropy");
					}
					else
					{
						// load optimiser from file
						try 
						(
							final ObjectInputStream reader = 
								new ObjectInputStream(new BufferedInputStream(new FileInputStream(
										outParams.outDir.getAbsolutePath() + File.separator + currentOptimiserCEFilenames[p]
								)))
						)
						{
							optimiser = (Optimiser) reader.readObject();
						}
						catch (final IOException | ClassNotFoundException e)
						{
							e.printStackTrace();
						}
						
						logLine(logWriter, "continuing with CE optimiser loaded from " + currentOptimiserCEFilenames[p]);
					}
					
					optimisers[p] = optimiser;
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
						// create new optimiser		TODO allow different one from CE here
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
						// create new tracker
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
			 * Creates (or loads) linear functions (one per player)
			 * @param featureSets
			 * @return
			 */
			private LinearFunction[] prepareCrossEntropyFunctions(final BaseFeatureSet[] featureSets)
			{
				final LinearFunction[] linearFunctions = new LinearFunction[numPlayers + 1];
				final int startP = trainingParams.sharedFeatureSet ? 0 : 1;
				
				for (int p = startP; p <= numPlayers; ++p)
				{
					final LinearFunction linearFunction;
					
					currentPolicyWeightsCEFilenames[p] = getFilenameLastCheckpoint("PolicyWeightsCE_P" + p, "txt");
					lastCheckpoint = 
							Math.min
							(
								lastCheckpoint,
								extractCheckpointFromFilename(currentPolicyWeightsCEFilenames[p], "PolicyWeightsCE_P" + p, "txt")
							);
					//System.out.println("CE funcs set lastCheckpoint = " + lastCheckpoint);
					
					if (currentPolicyWeightsCEFilenames[p] == null)
					{
						// Create new linear function
						if (trainingParams.sharedFeatureSet && p > 0)
						{
							linearFunction = 
									new BoostedLinearFunction
									(
										new WeightVector(new FVector(featureSets[0].getNumFeatures())),
										linearFunctions[0]
									);
						}
						else
						{
							linearFunction = new LinearFunction(new WeightVector(new FVector(featureSets[p].getNumFeatures())));
						}

						logLine(logWriter, "starting with new 0-weights linear function for Cross-Entropy");
					}
					else
					{
						// Load weights from file
						if (trainingParams.sharedFeatureSet && p > 0)
						{
							linearFunction = 
									BoostedLinearFunction.boostedFromFile
									(
										outParams.outDir.getAbsolutePath() + File.separator + currentPolicyWeightsCEFilenames[p],
										linearFunctions[0]
									);
						}
						else
						{
							linearFunction = 
								LinearFunction.fromFile(outParams.outDir.getAbsolutePath() + File.separator + currentPolicyWeightsCEFilenames[p]);
						}

						logLine(logWriter, "continuing with Selection policy weights loaded from " + currentPolicyWeightsCEFilenames[p]);
						
						try 
						{
							// make sure we're combining correct function with feature set
							String featureSetFilepath = 
									new File(outParams.outDir.getAbsolutePath() + File.separator + currentPolicyWeightsCEFilenames[p]).getParent();
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
					if (agentsParams.bestAgentsDataDir != null)
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
			 * Creates (or loads) feature sets (one per player, or a single shared one)
			 * @return
			 */
			private BaseFeatureSet[] prepareFeatureSets()
			{
				final BaseFeatureSet[] featureSets;
				final TIntArrayList newlyCreated = new TIntArrayList();
				
				if (trainingParams.sharedFeatureSet)
				{
					featureSets = new BaseFeatureSet[1];
					
					final BaseFeatureSet featureSet;
					currentFeatureSetFilenames[0] = getFilenameLastCheckpoint("FeatureSet_P" + 0, "fs");
					lastCheckpoint = 
							Math.min
							(
								lastCheckpoint,
								extractCheckpointFromFilename(currentFeatureSetFilenames[0], "FeatureSet_P" + 0, "fs")
							);
					//System.out.println("Feature sets set lastCheckpoint = " + lastCheckpoint);

					if (currentFeatureSetFilenames[0] == null)
					{
						// create new Feature Set
						final AtomicFeatureGenerator atomicFeatures = new AtomicFeatureGenerator(game, 2, 4);
						featureSet = new JITSPatterNetFeatureSet(atomicFeatures.getAspatialFeatures(), atomicFeatures.getSpatialFeatures());
						newlyCreated.add(0);
						logLine(logWriter, "starting with new initial feature set for Player " + 0);
						logLine(logWriter, "num atomic features = " + featureSet.getNumSpatialFeatures());
					}
					else
					{
						// load feature set from file
						featureSet = new JITSPatterNetFeatureSet(outParams.outDir.getAbsolutePath() + File.separator + currentFeatureSetFilenames[0]);
						logLine
						(
							logWriter, 
							"continuing with feature set loaded from " + 
							outParams.outDir.getAbsolutePath() + File.separator + currentFeatureSetFilenames[0] +
							" for Player " + 0
						);
					}

					if (featureSet.getNumSpatialFeatures() == 0)
					{
						System.err.println("ERROR: Feature Set has 0 features!");
						logLine(logWriter, "Training with 0 features makes no sense, interrupting experiment.");
						interrupted = true;
					}
					
					final int[] supportedPlayers = new int[game.players().count()];
					for (int i = 0; i < supportedPlayers.length; ++i)
					{
						supportedPlayers[i] = i + 1;
					}

					featureSet.init(game, supportedPlayers, null);
					featureSets[0] = featureSet;
				}
				else
				{
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
							featureSet = new JITSPatterNetFeatureSet(atomicFeatures.getAspatialFeatures(), atomicFeatures.getSpatialFeatures());
							newlyCreated.add(p);
							logLine(logWriter, "starting with new initial feature set for Player " + p);
							logLine(logWriter, "num atomic features = " + featureSet.getNumSpatialFeatures());
						}
						else
						{
							// load feature set from file
							featureSet = new JITSPatterNetFeatureSet(outParams.outDir.getAbsolutePath() + File.separator + currentFeatureSetFilenames[p]);
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
				}

				if (newlyCreated.size() > 0)
				{
					// we have some brand new feature sets; we'll likely have
					// obsolete features in there, and want to prune them
					
					// create matrices to store frequencies
					final long[][][] frequencies;
					
					if (trainingParams.sharedFeatureSet)
					{
						frequencies = new long[1][][];
						final int numAtomicFeatures = featureSets[0].getNumSpatialFeatures();
						frequencies[0] = new long[numAtomicFeatures][numAtomicFeatures];
					}
					else
					{
						frequencies = new long[numPlayers + 1][][];
						for (int p = 1; p <= numPlayers; ++p)
						{
							final int numAtomicFeatures = featureSets[p].getNumSpatialFeatures();
							frequencies[p] = new long[numAtomicFeatures][numAtomicFeatures];
						}
					}
					
					// play random games
					final Trial trial = new Trial(game);
					final Context context = new Context(game, trial);
					
					final long pruningGamesStartTime = System.currentTimeMillis();
					final long endTime = pruningGamesStartTime + featureDiscoveryParams.maxNumPruningSeconds * 1000L;
					
					for (int gameCounter = 0; gameCounter < featureDiscoveryParams.numPruningGames; ++gameCounter)
					{
						if (System.currentTimeMillis() > endTime)
							break;
						
						game.start(context);
						int numActions = 0;
						
						while (!context.trial().over())
						{
							final FastArrayList<Move> legal = game.moves(context).moves();
							final int mover = trainingParams.sharedFeatureSet ? 0 : context.state().mover();
							
							if (newlyCreated.contains(mover))
							{
								final BaseFeatureSet featureSet = featureSets[mover];
								
								// compute active feature indices for all actions
								final TIntArrayList[] sparseFeatureVectors = 
										featureSet.computeSparseSpatialFeatureVectors(context, legal, false);
								
								// update frequencies matrix
								for (final TIntArrayList sparse : sparseFeatureVectors)
								{
									for (int i = 0; i < sparse.size(); ++i)
									{
										final int firstFeature = sparse.getQuick(i);
										
										// update diagonal
										frequencies[mover][firstFeature][firstFeature]++;
										
										for (int j = i + 1; j < sparse.size(); ++j)
										{
											final int secondFeature = sparse.getQuick(j);
											
											// update off-diagonals
											frequencies[mover][firstFeature][secondFeature]++;
											frequencies[mover][secondFeature][firstFeature]++;
										}
									}
								}
							}
							
							// apply random action
							final int r = ThreadLocalRandom.current().nextInt(legal.size());
							game.apply(context, legal.get(r));
							
							++numActions;
						}
					}
					
					// find features that we can safely remove for every newly created set
					for (int f = 0; f < newlyCreated.size(); ++f)
					{
						final int p = newlyCreated.getQuick(f);
						
						final TIntHashSet featuresToRemove = new TIntHashSet();
						final BaseFeatureSet featureSet = featureSets[p];
						final int numAtomicFeatures = featureSet.getNumSpatialFeatures();
						
						for (int i = 0; i < numAtomicFeatures; ++i)
						{
							// only proceed if we didn't already decide to remove 
							// this feature
							if (featuresToRemove.contains(i))
								continue;
							
							final long soloCount = frequencies[p][i][i];
							
							// only proceed if we have enough observations for feature
							if (soloCount < featureDiscoveryParams.pruneInitFeaturesThreshold)
								continue;
							
							for (int j = i + 1; j < numAtomicFeatures; ++j)
							{
								// only proceed if we didn't already decide to remove 
								// this feature
								if (featuresToRemove.contains(j))
									continue;
								
								if (soloCount == frequencies[p][i][j] && soloCount == frequencies[p][j][j])
								{
									// should remove the most complex of i and j
									final SpatialFeature firstFeature = featureSet.spatialFeatures()[i];
									final SpatialFeature secondFeature = featureSet.spatialFeatures()[j];
									final Pattern a = firstFeature.pattern();
									final Pattern b = secondFeature.pattern();
									
									// by default just keep the first feature if both
									// are equally "complex"
									boolean keepFirst = true;
									
									if (b.featureElements().length < a.featureElements().length)
									{
										// fewer elements is simpler
										keepFirst = false;
									}
									else 
									{
										int sumWalkLengthsA = 0;
										for (final FeatureElement el : a.featureElements())
										{
											if (el instanceof RelativeFeatureElement)
											{
												final RelativeFeatureElement rel = (RelativeFeatureElement) el;
												sumWalkLengthsA += rel.walk().steps().size();
											}
										}
										
										int sumWalkLengthsB = 0;
										for (final FeatureElement el : b.featureElements())
										{
											if (el instanceof RelativeFeatureElement)
											{
												final RelativeFeatureElement rel = (RelativeFeatureElement) el;
												sumWalkLengthsB += rel.walk().steps().size();
											}
										}
										
										if (sumWalkLengthsB < sumWalkLengthsA)
										{
											// fewer steps in Walks is simpler
											keepFirst = false;
										}
									}
									
									if (keepFirst)
										featuresToRemove.add(j);
									else
										featuresToRemove.add(i);
									
//									if (keepFirst)
//										System.out.println("pruning " + secondFeature + " in favour of " + firstFeature);
//									else
//										System.out.println("pruning " + firstFeature + " in favour of " + secondFeature);
								}
							}
						}
						
						// create new feature set
						final List<SpatialFeature> keepFeatures = new ArrayList<SpatialFeature>();
						for (int i = 0; i < numAtomicFeatures; ++i)
						{
							if (!featuresToRemove.contains(i))
								keepFeatures.add(featureSet.spatialFeatures()[i]);
						}
						final BaseFeatureSet newFeatureSet = new JITSPatterNetFeatureSet(Arrays.asList(featureSet.aspatialFeatures()), keepFeatures);
						
						final int[] supportedPlayers;
						if (p == 0)
						{
							supportedPlayers = new int[game.players().count()];
							for (int i = 0; i < supportedPlayers.length; ++i)
							{
								supportedPlayers[i] = i + 1;
							}
						}
						else
						{
							supportedPlayers = new int[]{p};
						}
						
						newFeatureSet.init(game, supportedPlayers, null);
						featureSets[p] = newFeatureSet;
						
						logLine(logWriter, "Finished pruning atomic feature set for Player " + p);
						logLine(logWriter, "Num atomic features after pruning = " + newFeatureSet.getNumSpatialFeatures());
					}
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
			 * @param crossEntropyFunctions
			 * @param tspgFunctions
			 * @param experienceBuffers
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
				final LinearFunction[] crossEntropyFunctions,
				final LinearFunction[] tspgFunctions,
				final Heuristics valueFunction,
				final ExperienceBuffer[] experienceBuffers,
				final Optimiser[] ceOptimisers,
				final Optimiser[] tspgOptimisers,
				final Optimiser valueFunctionOptimiser,
				final ExponentialMovingAverage[] avgGameDurations,
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
				
				final int startP = trainingParams.sharedFeatureSet ? 0 : 1;
				
				for (int p = startP; p <= numPlayers; ++p)
				{
					// save feature set
					if (trainingParams.sharedFeatureSet && p > 0)
					{
						final String featureSetFilename = createCheckpointFilename("FeatureSet_P0", nextCheckpoint, "fs");
						currentFeatureSetFilenames[p] = featureSetFilename;
					}
					else
					{
						final String featureSetFilename = createCheckpointFilename("FeatureSet_P" + p, nextCheckpoint, "fs");
						featureSets[p].toFile(outParams.outDir.getAbsolutePath() + File.separator + featureSetFilename);
						currentFeatureSetFilenames[p] = featureSetFilename;
					}
					
					// save CE weights
					final String ceWeightsFilename = createCheckpointFilename("PolicyWeightsCE_P" + p, nextCheckpoint, "txt");
					crossEntropyFunctions[p].writeToFile(
							outParams.outDir.getAbsolutePath() + File.separator + ceWeightsFilename, new String[]{currentFeatureSetFilenames[p]});
					currentPolicyWeightsCEFilenames[p] = ceWeightsFilename;
					
					// The following only exist for p > 0
					if (p > 0)
					{
						// save TSPG weights
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
					}

					if (forced)
					{
						// in this case, we'll also store experience buffers
						if (p > 0)
						{
							final String experienceBufferFilename = createCheckpointFilename("ExperienceBuffer_P" + p, nextCheckpoint, "buf");
							experienceBuffers[p].writeToFile(outParams.outDir.getAbsolutePath() + File.separator + experienceBufferFilename);
						}
						
						// and optimisers
						final String ceOptimiserFilename = createCheckpointFilename("OptimiserCE_P" + p, nextCheckpoint, "opt");
						ceOptimisers[p].writeToFile(outParams.outDir.getAbsolutePath() + File.separator + ceOptimiserFilename);
						currentOptimiserCEFilenames[p] = ceOptimiserFilename;
						
						if (p > 0)
						{
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
		// define options for arg parser
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
				.withNames("--expert-ai")
				.help("Type of AI to use as expert.")
				.withDefault("Biased MCTS")
				.withNumVals(1)
				.withType(OptionTypes.String)
				.withLegalVals("BEST_AGENT", "FROM_METADATA", "Biased MCTS", "UCT"));
		argParse.addOption(new ArgOption()
				.withNames("--best-agents-data-dir")
				.help("Filepath for directory with best agents data for this game (+ options).")
				.withNumVals(1)
				.withType(OptionTypes.String));
		argParse.addOption(new ArgOption()
				.withNames("-n", "--num-games", "--num-training-games")
				.help("Number of training games to run.")
				.withDefault(Integer.valueOf(200))
				.withNumVals(1)
				.withType(OptionTypes.Int));
		argParse.addOption(new ArgOption()
				.withNames("--game-length-cap", "--max-num-actions")
				.help("Maximum number of actions that may be taken before a game is terminated as a draw (-1 for no limit).")
				.withDefault(Integer.valueOf(-1))
				.withNumVals(1)
				.withType(OptionTypes.Int));
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
				.withNames("--add-feature-every")
				.help("After this many training games, we add a new feature.")
				.withDefault(Integer.valueOf(1))
				.withNumVals(1)
				.withType(OptionTypes.Int));
		argParse.addOption(new ArgOption()
				.withNames("--batch-size")
				.help("Max size of minibatches in training.")
				.withDefault(Integer.valueOf(30))
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
				.withNames("--no-grow-features", "--no-grow-featureset", "--no-grow-feature-set")
				.help("If true, we'll not grow feature set (but still train weights).")
				.withType(OptionTypes.Boolean));
		argParse.addOption(new ArgOption()
				.withNames("--train-tspg")
				.help("If true, we'll train a policy on TSPG objective (see COG paper).")
				.withType(OptionTypes.Boolean));
		argParse.addOption(new ArgOption()
				.withNames("--ce-optimiser", "--cross-entropy-optimiser")
				.help("Optimiser to use for policy trained on Cross-Entropy loss.")
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
				.withNames("--combining-feature-instance-threshold")
				.help("At most this number of feature instances will be taken into account when combining features.")
				.withDefault(Integer.valueOf(40))
				.withNumVals(1)
				.withType(OptionTypes.Int));
		argParse.addOption(new ArgOption()
				.withNames("--is-episode-durations")
				.help("If true, we'll use importance sampling weights based on episode durations for CE-loss.")
				.withType(OptionTypes.Boolean));
		argParse.addOption(new ArgOption()
				.withNames("--prioritized-experience-replay", "--per")
				.help("If true, we'll use prioritized experience replay")
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
				.withNames("--mcts-as-reg-pol-opt")
				.help("If true, we use Act, Search, and Learn as described in the MCTS as regularized policy optimization paper for Biased MCTS.")
				.withType(OptionTypes.Boolean));
		argParse.addOption(new ArgOption()
				.withNames("--exp-delta-val-weighting")
				.help("If true, we weight samples based on the expected improvement in value.")
				.withType(OptionTypes.Boolean));
		argParse.addOption(new ArgOption()
				.withNames("--exp-delta-val-weighting-lower-clip")
				.help("Minimum per-sample weight when weighting samples based on expected immprovement in value")
				.withDefault(Double.valueOf(0.0))
				.withNumVals(1)
				.withType(OptionTypes.Double));
		argParse.addOption(new ArgOption()
				.withNames("--tournament-mode")
				.help("If true, we use the tournament mode (similar to the one in Polygames).")
				.withType(OptionTypes.Boolean));
		argParse.addOption(new ArgOption()
				.withNames("--handle-aliasing")
				.help("If true, we handle move aliasing by putting the maximum mass among all aliased moves on each of them")
				.withType(OptionTypes.Boolean));
		argParse.addOption(new ArgOption()
				.withNames("--weight-decay-lambda")
				.help("Lambda param for weight decay")
				.withDefault(Double.valueOf(0.000001))
				.withNumVals(1)
				.withType(OptionTypes.Double));
		argParse.addOption(new ArgOption()
				.withNames("--shared-feature-set")
				.help("If true, we train a single shared feature set for all players (and boosted weights per player)")
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
				.withNames("--prune-init-features-threshold")
				.help("Will only consider pruning features if they have been active at least this many times.")
				.withDefault(Integer.valueOf(50))
				.withNumVals(1)
				.withType(OptionTypes.Int));
		argParse.addOption(new ArgOption()
				.withNames("--num-pruning-games")
				.help("Number of random games to play out for determining features to prune.")
				.withDefault(Integer.valueOf(0))
				.withNumVals(1)
				.withType(OptionTypes.Int));
		argParse.addOption(new ArgOption()
				.withNames("--max-pruning-seconds")
				.help("Maximum number of seconds to spend on random games for pruning initial featureset.")
				.withDefault(Integer.valueOf(0))
				.withNumVals(1)
				.withType(OptionTypes.Int));
		
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
		
		exIt.agentsParams.expertAI = argParse.getValueString("--expert-ai");
		exIt.agentsParams.bestAgentsDataDir = argParse.getValueString("--best-agents-data-dir");
		exIt.trainingParams.numTrainingGames = argParse.getValueInt("-n");
		exIt.gameParams.gameLengthCap = argParse.getValueInt("--game-length-cap");
		exIt.agentsParams.thinkingTime = argParse.getValueDouble("--thinking-time");
		exIt.agentsParams.iterationLimit = argParse.getValueInt("--iteration-limit");
		exIt.agentsParams.depthLimit = argParse.getValueInt("--depth-limit");
		
		exIt.featureDiscoveryParams.addFeatureEvery = argParse.getValueInt("--add-feature-every");
		exIt.trainingParams.batchSize = argParse.getValueInt("--batch-size");
		exIt.trainingParams.experienceBufferSize = argParse.getValueInt("--buffer-size");
		exIt.trainingParams.updateWeightsEvery = argParse.getValueInt("--update-weights-every");
		exIt.featureDiscoveryParams.noGrowFeatureSet = argParse.getValueBool("--no-grow-features");
		exIt.objectiveParams.trainTSPG = argParse.getValueBool("--train-tspg");
		exIt.optimisersParams.crossEntropyOptimiserConfig = argParse.getValueString("--ce-optimiser");
		exIt.optimisersParams.ceExploreOptimiserConfig = argParse.getValueString("--cee-optimiser");
		exIt.optimisersParams.tspgOptimiserConfig = argParse.getValueString("--tspg-optimiser");
		exIt.optimisersParams.valueOptimiserConfig = argParse.getValueString("--value-optimiser");
		exIt.featureDiscoveryParams.combiningFeatureInstanceThreshold = argParse.getValueInt("--combining-feature-instance-threshold");
		exIt.objectiveParams.importanceSamplingEpisodeDurations = argParse.getValueBool("--is-episode-durations");
		exIt.trainingParams.prioritizedExperienceReplay = argParse.getValueBool("--prioritized-experience-replay");
		exIt.objectiveParams.weightedImportanceSampling = argParse.getValueBool("--wis");
		exIt.objectiveParams.noValueLearning = argParse.getValueBool("--no-value-learning");
		exIt.objectiveParams.mctsRegPolOpt = argParse.getValueBool("--mcts-as-reg-pol-opt");
		exIt.objectiveParams.expDeltaValWeighting = argParse.getValueBool("--exp-delta-val-weighting");
		exIt.objectiveParams.expDeltaValWeightingLowerClip = argParse.getValueDouble("--exp-delta-val-weighting-lower-clip");
		exIt.agentsParams.tournamentMode = argParse.getValueBool("--tournament-mode");
		exIt.objectiveParams.handleAliasing = argParse.getValueBool("--handle-aliasing");
		exIt.objectiveParams.weightDecayLambda = argParse.getValueDouble("--weight-decay-lambda");
		exIt.trainingParams.sharedFeatureSet = argParse.getValueBool("--shared-feature-set");
		exIt.agentsParams.playoutFeaturesEpsilon = argParse.getValueDouble("--playout-features-epsilon");
		
		exIt.agentsParams.maxNumBiasedPlayoutActions = argParse.getValueInt("--max-num-biased-playout-actions");
		
		exIt.featureDiscoveryParams.pruneInitFeaturesThreshold = argParse.getValueInt("--prune-init-features-threshold");
		exIt.featureDiscoveryParams.numPruningGames = argParse.getValueInt("--num-pruning-games");
		exIt.featureDiscoveryParams.maxNumPruningSeconds = argParse.getValueInt("--max-pruning-seconds");
		
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
