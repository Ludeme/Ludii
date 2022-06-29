package supplementary.experiments.eval;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import features.feature_sets.network.JITSPatterNetFeatureSet;
import game.Game;
import gnu.trove.list.array.TIntArrayList;
import main.CommandLineArgParse;
import main.CommandLineArgParse.ArgOption;
import main.CommandLineArgParse.OptionTypes;
import main.FileHandling;
import main.StringRoutines;
import main.collections.ListUtils;
import main.grammar.Report;
import metadata.ai.agents.BestAgent;
import metadata.ai.features.Features;
import metadata.ai.heuristics.Heuristics;
import other.AI;
import other.GameLoader;
import other.RankUtils;
import other.context.Context;
import other.model.Model;
import other.trial.Trial;
import policies.softmax.SoftmaxPolicyLinear;
import search.mcts.MCTS;
import search.minimax.AlphaBetaSearch;
import utils.AIFactory;
import utils.AIUtils;
import utils.experiments.InterruptableExperiment;
import utils.experiments.ResultsSummary;

/**
 * A "gating" evaluation where we check whether a newly-trained agent
 * performs better than the currently-best agent.
 *
 * @author Dennis Soemers
 */
public class EvalGate
{
	/** Name of the game to play. Should end with .lud */
	protected String gameName;
	
	/** List of game options to use when compiling game */
	protected List<String> gameOptions;
	
	/** Name of ruleset to compile. Any options will be ignored if ruleset is provided. */
	protected String ruleset;

	/** Number of evaluation games to run */
	protected int numGames;
	
	/** Maximum game duration (in moves) */
	protected int gameLengthCap;

	/** Max allowed thinking time per move (in seconds) */
	protected double thinkingTime;
	
	/** Number of seconds for warming-up of JVM */
	protected int warmingUpSecs;

	/** Strings describing the agent to evaluate */
	protected String evalAgent;
	
	/** Filepaths for feature weights to evaluate (if we're evaluating some form of Biased MCTS, one per player) */
	protected List<String> evalFeatureWeightsFilepaths;
	
	/** Filepath for heuristics to evaluate (if we're evaluating some form of Alpha-Beta) */
	protected String evalHeuristicsFilepath;
	
	/** Directory containing best agents data */
	protected File bestAgentsDataDir;
	
	/** Type of gate agent against which we wish to evaluate ("BestAgent", "Alpha-Beta", or "BiasedMCTS") */
	protected String gateAgentType;
	
	/** 
	 * Whether to create a small GUI that can be used to manually interrupt training run. 
	 * False by default. 
	 */
	protected boolean useGUI;
	
	/** Max wall time in minutes (or -1 for no limit) */
	protected int maxWallTime;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * @param useGUI
	 * @param maxWallTime Wall time limit in minutes.
	 */
	private EvalGate(final boolean useGUI, final int maxWallTime)
	{
		this.useGUI = useGUI;
		this.maxWallTime = maxWallTime;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Helper method to instantiate an AI to evaluate
	 * @return
	 */
	private AI createEvalAI()
	{
		if (evalAgent.equals("Alpha-Beta"))
		{
			return AIFactory.createAI("algorithm=Alpha-Beta;heuristics=" + evalHeuristicsFilepath);
		}
		else if (evalAgent.equals("BiasedMCTS"))
		{
			final StringBuilder playoutSb = new StringBuilder();
			playoutSb.append("playout=softmax");
			
			for (int p = 1; p <= evalFeatureWeightsFilepaths.size(); ++p)
			{
				playoutSb.append(",policyweights" + p + "=" + evalFeatureWeightsFilepaths.get(p - 1));
			}
			
			final String agentStr = StringRoutines.join
					(
						";", 
						"algorithm=MCTS",
						"selection=noisyag0selection",
						playoutSb.toString(),
						"final_move=robustchild",
						"tree_reuse=true",
						"learned_selection_policy=playout",
						"friendly_name=BiasedMCTS"
					);
			
			return AIFactory.createAI(agentStr);
		}
		else if (evalAgent.equals("BiasedMCTSUniformPlayouts"))
		{
			final StringBuilder policySb = new StringBuilder();
			policySb.append("learned_selection_policy=softmax");
			
			for (int p = 1; p <= evalFeatureWeightsFilepaths.size(); ++p)
			{
				policySb.append(",policyweights" + p + "=" + evalFeatureWeightsFilepaths.get(p - 1));
			}
			
			final String agentStr = StringRoutines.join
					(
						";", 
						"algorithm=MCTS",
						"selection=noisyag0selection",
						"playout=random",
						"final_move=robustchild",
						"tree_reuse=true",
						policySb.toString(),
						"friendly_name=BiasedMCTSUniformPlayouts"
					);
			
			return AIFactory.createAI(agentStr);
		}
		else
		{
			System.err.println("Can't build eval AI: " + evalAgent);
			return null;
		}
	}
	
	/**
	 * Helper method to instantiate a "gate AI" (a currently-best AI to beat)
	 * @return
	 */
	private AI createGateAI()
	{
		final String bestAgentDataDirFilepath = bestAgentsDataDir.getAbsolutePath().replaceAll(Pattern.quote("\\"), "/");
		final Report report = new Report();
				
		try
		{
			if (gateAgentType.equals("BestAgent"))
			{
				final BestAgent bestAgent = (BestAgent)compiler.Compiler.compileObject
				(
					FileHandling.loadTextContentsFromFile(bestAgentDataDirFilepath + "/BestAgent.txt"), 
					"metadata.ai.agents.BestAgent",
					report
				);
						
				if (bestAgent.agent().equals("AlphaBeta") || bestAgent.agent().equals("Alpha-Beta"))
				{
					return new AlphaBetaSearch(bestAgentDataDirFilepath + "/BestHeuristics.txt");
				}
				else if (bestAgent.agent().equals("AlphaBetaMetadata"))
				{
					return new AlphaBetaSearch();
				}
				else if (bestAgent.agent().equals("UCT"))
				{
					return AIFactory.createAI("UCT");
				}
				else if (bestAgent.agent().equals("MC-GRAVE"))
				{
					return AIFactory.createAI("MC-GRAVE");
				}
				else if (bestAgent.agent().equals("MAST"))
				{
					return AIFactory.createAI("MAST");
				}
				else if (bestAgent.agent().equals("ProgressiveHistory") || bestAgent.agent().equals("Progressive History"))
				{
					return AIFactory.createAI("Progressive History");
				}
				else if (bestAgent.agent().equals("Biased MCTS"))
				{
					final Features features = (Features)compiler.Compiler.compileObject
					(
						FileHandling.loadTextContentsFromFile(bestAgentDataDirFilepath + "/BestFeatures.txt"), 
						"metadata.ai.features.Features",
						report
					);
					
					return MCTS.createBiasedMCTS(features, 1.0);
				}
				else if (bestAgent.agent().equals("Biased MCTS (Uniform Playouts)"))
				{
					final Features features = (Features)compiler.Compiler.compileObject
					(
						FileHandling.loadTextContentsFromFile(bestAgentDataDirFilepath + "/BestFeatures.txt"), 
						"metadata.ai.features.Features",
						report
					);
					
					return MCTS.createBiasedMCTS(features, 0.0);
				}
				else
				{
					System.err.println("Unrecognised best agent: " + bestAgent.agent());
				}
			}
			else if (gateAgentType.equals("Alpha-Beta"))
			{
				return new AlphaBetaSearch(bestAgentDataDirFilepath + "/BestHeuristics.txt");
			}
			else if (gateAgentType.equals("BiasedMCTS"))
			{
				final Features features = (Features)compiler.Compiler.compileObject
				(
					FileHandling.loadTextContentsFromFile(bestAgentDataDirFilepath + "/BestFeatures.txt"), 
					"metadata.ai.features.Features",
					report
				);
				
				// We'll take biased playouts or uniform playouts to be equal to the agent we're evaluating
				if (evalAgent.equals("BiasedMCTS"))
					return MCTS.createBiasedMCTS(features, 1.0);
				else if (evalAgent.equals("BiasedMCTSUniformPlayouts"))
					return MCTS.createBiasedMCTS(features, 0.0);
				else
					System.err.println("Trying to use Biased MCTS gate when evaluating something other than Biased MCTS!");
			}
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}
		
		System.err.println("Failed to build gate AI: " + gateAgentType);
		return null;
	}
	
	/**
	 * Starts the experiment
	 */
	@SuppressWarnings("unused")
	public void startExperiment()
	{
		final Game game;
		
		if (ruleset != null && !ruleset.equals(""))
			game = GameLoader.loadGameFromName(gameName, ruleset);
		else
			game = GameLoader.loadGameFromName(gameName, gameOptions);
		
		final int numPlayers = game.players().count();

		if (gameLengthCap >= 0)
			game.setMaxTurns(Math.min(gameLengthCap, game.getMaxTurnLimit()));
		
		final Trial trial = new Trial(game);
		final Context context = new Context(game, trial);
		
		// We want an even number of AIs, with equally many instances of:
		// 	1) our AI to evaluate
		//	2) our current best AI
		final List<AI> ais = new ArrayList<AI>((numPlayers % 2 == 0) ? numPlayers : numPlayers + 1);
		for (int i = 0; i < numPlayers; i += 2)
		{
			final AI evalAI = createEvalAI();
			evalAI.setFriendlyName("EvalAI");
			
			final AI gateAI = createGateAI();
			gateAI.setFriendlyName("GateAI");
			
			ais.add(evalAI);
			ais.add(gateAI);
		}

		new InterruptableExperiment(useGUI, maxWallTime)
		{

			@Override
			public void runExperiment()
			{
				int numGamesToPlay = numGames;
				List<TIntArrayList> aiListPermutations = new ArrayList<TIntArrayList>();
				
				// compute all possible permutations of indices for the list of AIs
				aiListPermutations = ListUtils.generatePermutations(
						TIntArrayList.wrap(IntStream.range(0, numPlayers).toArray())
						);

				if (numGamesToPlay % aiListPermutations.size() != 0)
				{
					// Increase number of games to play such that we can divide by number of AI permutations
					numGamesToPlay += (numGamesToPlay % aiListPermutations.size());
				}
				
				// start with a warming up
				long stopAt = 0L;
				final long start = System.nanoTime();
				final double abortAt = start + warmingUpSecs * 1000000000.0;
				while (stopAt < abortAt)
				{
					game.start(context);
					game.playout(context, null, 1.0, null, -1, -1, ThreadLocalRandom.current());
					stopAt = System.nanoTime();
				}
				System.gc();
				
				// prepare results writing
				final List<String> agentStrings = new ArrayList<String>();
				for (final AI ai : ais)
				{
					agentStrings.add(ai.friendlyName());
				}
				final ResultsSummary resultsSummary = new ResultsSummary(game, agentStrings);
				
				for (int gameCounter = 0; gameCounter < numGamesToPlay; ++gameCounter)
				{
					checkWallTime(0.05);
					
					if (interrupted)
					{
						// time to abort the experiment due to wall time
						break;
					}
					
					// compute list of AIs to use for this game
					// (we rotate every game)
					final List<AI> currentAIList = new ArrayList<AI>(numPlayers);
					final int currentAIsPermutation = gameCounter % aiListPermutations.size();
					
					final TIntArrayList currentPlayersPermutation = aiListPermutations.get(currentAIsPermutation);
					currentAIList.add(null); // 0 index not used

					for (int i = 0; i < currentPlayersPermutation.size(); ++i)
					{
						currentAIList.add
						(
							ais.get(currentPlayersPermutation.getQuick(i) % ais.size())
						);
					}

					// Play a game
					game.start(context);

					for (int p = 1; p < currentAIList.size(); ++p)
					{
						currentAIList.get(p).initAI(game, p);
					}
					
					final Model model = context.model();

					while (!context.trial().over())
					{
						if (interrupted)
						{
							// Time to abort the experiment due to wall time						
							break;
						}
						
						model.startNewStep(context, currentAIList, thinkingTime, -1, -1, 0.0);
					}

					// Record results
					if (context.trial().over())
					{
						final double[] utilities = RankUtils.agentUtilities(context);
						final int numMovesPlayed = context.trial().numMoves() - context.trial().numInitialPlacementMoves();
						final int[] agentPermutation = new int[currentPlayersPermutation.size() + 1];
						currentPlayersPermutation.toArray(agentPermutation, 0, 1, currentPlayersPermutation.size());
						
						resultsSummary.recordResults(agentPermutation, utilities, numMovesPlayed);
					}
					
					// Close AIs
					for (int p = 1; p < currentAIList.size(); ++p)
					{
						currentAIList.get(p).closeAI();
					}
				}
				
				// If we outperform the gate, we'll have to write some new files
				final double avgEvalScore = resultsSummary.avgScoreForAgentName("EvalAI");
				final double avgGateScore = resultsSummary.avgScoreForAgentName("GateAI");
				
				System.out.println("----------------------------------");
				System.out.println("Eval Agent = " + evalAgent);
				System.out.println("Gate Agent = " + gateAgentType);
				System.out.println();
				System.out.println("Eval Agent Score = " + avgEvalScore);
				System.out.println("Gate Agent Score = " + avgGateScore);
				System.out.println("----------------------------------");
				
				if (avgEvalScore > avgGateScore)
				{
					// We passed the gate
					boolean writeBestAgent = false;
					boolean writeFeatures = false;
					boolean writeHeuristics = false;
					
					if (gateAgentType.equals("BestAgent"))
					{
						writeBestAgent = true;
						
						if (evalAgent.equals("Alpha-Beta"))
							writeHeuristics = true;
						else if (evalAgent.contains("BiasedMCTS"))
							writeFeatures = true;
						else
							System.err.println("Eval agent is neither Alpha-Beta nor a variant of BiasedMCTS");
					}
					else if (gateAgentType.equals("Alpha-Beta"))
					{
						if (evalAgent.equals("Alpha-Beta"))
							writeHeuristics = true;
						else
							System.err.println("evalAgent = " + evalAgent + " against gateAgentType = " + gateAgentType);
					}
					else if (gateAgentType.equals("BiasedMCTS"))
					{
						if (evalAgent.contains("BiasedMCTS"))
							writeFeatures = true;
						else
							System.err.println("evalAgent = " + evalAgent + " against gateAgentType = " + gateAgentType);
					}
					else
					{
						System.err.println("Unrecognised gate agent type: " + gateAgentType);
					}
					
					final String bestAgentsDataDirPath = bestAgentsDataDir.getAbsolutePath().replaceAll(Pattern.quote("\\"), "/");
					
					if (writeBestAgent)
					{
						final File bestAgentFile = new File(bestAgentsDataDirPath + "/BestAgent.txt");
						
						try (final PrintWriter writer = new PrintWriter(bestAgentFile))
						{
							final BestAgent bestAgent;
							
							if (evalAgent.equals("Alpha-Beta"))
								bestAgent = new BestAgent("AlphaBeta");
							else if (evalAgent.equals("BiasedMCTS"))
								bestAgent = new BestAgent("Biased MCTS");
							else if (evalAgent.equals("BiasedMCTSUniformPlayouts"))
								bestAgent = new BestAgent("Biased MCTS (Uniform Playouts)");
							else
								bestAgent = null;
							
							System.out.println("Writing new best agent: " + evalAgent);
							writer.println(bestAgent.toString());
						}
						catch (final FileNotFoundException e)
						{
							e.printStackTrace();
						}
					}
					
					if (writeHeuristics)
					{
						final File bestHeuristicsFile = new File(bestAgentsDataDirPath + "/BestHeuristics.txt");
						
						try (final PrintWriter writer = new PrintWriter(bestHeuristicsFile))
						{
							final String heuristicsStr = FileHandling.loadTextContentsFromFile(evalHeuristicsFilepath);
							final Heuristics heuristics = 	(Heuristics)compiler.Compiler.compileObject
														  	(
																heuristicsStr, 
																"metadata.ai.heuristics.Heuristics", 
																new Report()
															);
							System.out.println("writing new best heuristics");
							writer.println(heuristics.toString());
						}
						catch (final IOException e)
						{
							e.printStackTrace();
						}
					}
					
					if (writeFeatures)
					{
						final File bestFeaturesFile = new File(bestAgentsDataDirPath + "/BestFeatures.txt");
						
						// We'll first just use the command line args we got to build a Biased MCTS
						// Then we'll extract the features from that one
						final StringBuilder playoutSb = new StringBuilder();
						playoutSb.append("playout=softmax");
						
						for (int p = 1; p <= evalFeatureWeightsFilepaths.size(); ++p)
						{
							playoutSb.append(",policyweights" + p + "=" + evalFeatureWeightsFilepaths.get(p - 1));
						}
						
						final String agentStr = StringRoutines.join
								(
									";", 
									"algorithm=MCTS",
									"selection=noisyag0selection",
									playoutSb.toString(),
									"final_move=robustchild",
									"tree_reuse=true",
									"learned_selection_policy=playout",
									"friendly_name=BiasedMCTS"
								);
						
						final MCTS mcts = (MCTS) AIFactory.createAI(agentStr);
						
						// Generate our features metadata and write it
						final Features features = 
								AIUtils.generateFeaturesMetadata
								(
									(SoftmaxPolicyLinear) mcts.learnedSelectionPolicy(), 
									(SoftmaxPolicyLinear) mcts.playoutStrategy()
								);
						
						try (final PrintWriter writer = new PrintWriter(bestFeaturesFile))
						{
							System.out.println("writing new best features");
							writer.println(features.toString());
						}
						catch (final IOException e)
						{
							e.printStackTrace();
						}
					}
				}
			}	
		};
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
					"Gating experiment to test if a newly-trained agent outperforms current best."
				);
		
		argParse.addOption(new ArgOption()
				.withNames("--game")
				.help("Name of the game to play. Should end with \".lud\".")
				.withDefault("Amazons.lud")
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
				.withNames("--eval-agent")
				.help("Agent to be evaluated.")
				.withNumVals(1)
				.withType(OptionTypes.String)
				.withLegalVals("Alpha-Beta", "BiasedMCTS", "BiasedMCTSUniformPlayouts")
				.setRequired());
		argParse.addOption(new ArgOption()
				.withNames("--eval-feature-weights-filepaths")
				.help("Filepaths for feature weights to be evaluated.")
				.withNumVals("*")
				.withType(OptionTypes.String));
		argParse.addOption(new ArgOption()
				.withNames("--eval-heuristics-filepath")
				.help("Filepath for heuristics to be evaluated.")
				.withNumVals(1)
				.withType(OptionTypes.String));
		
		argParse.addOption(new ArgOption()
				.withNames("-n", "--num-games", "--num-eval-games")
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
				.withNames("--warming-up-secs")
				.help("Number of seconds for which to warm up JVM.")
				.withType(OptionTypes.Int)
				.withNumVals(1)
				.withDefault(Integer.valueOf(60)));

		argParse.addOption(new ArgOption()
				.withNames("--best-agents-data-dir")
				.help("Filepath for directory containing data on best agents")
				.withNumVals(1)
				.withType(OptionTypes.String)
				.setRequired());
		argParse.addOption(new ArgOption()
				.withNames("--gate-agent-type")
				.help("Type of gate agent against which we wish to evaluate.")
				.withNumVals(1)
				.withType(OptionTypes.String)
				.setRequired()
				.withLegalVals("BestAgent", "Alpha-Beta", "BiasedMCTS"));
		
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
		final EvalGate eval = 
				new EvalGate
				(
					argParse.getValueBool("--useGUI"), 
					argParse.getValueInt("--max-wall-time")
				);
		
		eval.gameName = argParse.getValueString("--game");
		eval.gameOptions = (List<String>) argParse.getValue("--game-options");
		eval.ruleset = argParse.getValueString("--ruleset");
		
		eval.evalAgent = argParse.getValueString("--eval-agent");
		eval.evalFeatureWeightsFilepaths = (List<String>) argParse.getValue("--eval-feature-weights-filepaths");
		eval.evalHeuristicsFilepath = argParse.getValueString("--eval-heuristics-filepath");
		
		eval.numGames = argParse.getValueInt("-n");
		eval.gameLengthCap = argParse.getValueInt("--game-length-cap");
		eval.thinkingTime = argParse.getValueDouble("--thinking-time");
		eval.warmingUpSecs = argParse.getValueInt("--warming-up-secs");

		eval.bestAgentsDataDir = new File(argParse.getValueString("--best-agents-data-dir"));
		eval.gateAgentType = argParse.getValueString("--gate-agent-type");
		
		eval.startExperiment();
	}
	
	//-------------------------------------------------------------------------

}
