package supplementary.experiments.eval;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import features.feature_sets.network.JITSPatterNetFeatureSet;
import game.Game;
import gnu.trove.list.array.TIntArrayList;
import main.CommandLineArgParse;
import main.CommandLineArgParse.ArgOption;
import main.CommandLineArgParse.OptionTypes;
import main.DaemonThreadFactory;
import main.collections.ListUtils;
import other.AI;
import other.GameLoader;
import other.RankUtils;
import other.context.Context;
import other.model.Model;
import other.trial.Trial;
import utils.AIFactory;
import utils.experiments.ResultsSummary;

/**
 * Implementation of an experiment that evaluates multiple agents across multiple games.
 * Primarily meant for use on larger compute nodes with many cores available for running
 * multiple evals in parallel.
 * 
 * @author Dennis Soemers
 */
public class ParallelEvalMultiGamesMultiAgents 
{
	
	//-------------------------------------------------------------------------
	
	protected int numCoresTotal;
	protected int numThreadsPerTrial;
	protected List<String> jsonFiles;

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
	public ParallelEvalMultiGamesMultiAgents()
	{
		// all defaults already set above
	}

	/**
	 * Constructor. No wall time limit.
	 * @param useGUI
	 */
	public ParallelEvalMultiGamesMultiAgents(final boolean useGUI)
	{
		this.useGUI = useGUI;
	}

	/**
	 * Constructor
	 * @param useGUI
	 * @param maxWallTime Wall time limit in minutes.
	 */
	public ParallelEvalMultiGamesMultiAgents(final boolean useGUI, final int maxWallTime)
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
		final AtomicInteger numCoresAvailable = new AtomicInteger(numCoresTotal);
		
		@SuppressWarnings("resource")
		final ExecutorService threadPool = Executors.newFixedThreadPool(numCoresTotal / numThreadsPerTrial, DaemonThreadFactory.INSTANCE);
		@SuppressWarnings("resource")
		final ExecutorService resultsWritingPool = Executors.newFixedThreadPool(1, DaemonThreadFactory.INSTANCE);
		
		final long startTime = System.currentTimeMillis();
		
		try
		{
			for (final String jsonFile : jsonFiles)
			{
				// Let's try to start another batch of trials
				final TrialsBatchToRun trialsBatch = TrialsBatchToRun.fromJson(jsonFile);
				
				// Load game for this batch
				final Game game;
				
				if (trialsBatch.ruleset != null && !trialsBatch.ruleset.equals(""))
					game = GameLoader.loadGameFromName(trialsBatch.gameName, trialsBatch.ruleset);
				else
					game = GameLoader.loadGameFromName(trialsBatch.gameName, new ArrayList<String>());	// TODO add support for options
				
				final int numPlayers = game.players().count();
				
				if (trialsBatch.agentStrings.length != numPlayers)
				{
					System.err.println
					(
						"Expected " + numPlayers + 
						" agents, but received list of " + trialsBatch.agentStrings.length + 
						" agents. Aborting set of games."
					);
					break;
				}
				
				if (trialsBatch.gameLengthCap >= 0)
					game.setMaxTurns(Math.min(trialsBatch.gameLengthCap, game.getMaxTurnLimit()));
				
				// Permutations of agents list, to rotate through
				final List<TIntArrayList> aiListPermutations;
				if (numPlayers <= 5)
				{
					// Compute all possible permutations of indices for the list of AIs
					aiListPermutations = ListUtils.generatePermutations(
							TIntArrayList.wrap(IntStream.range(0, numPlayers).toArray()));
					
					Collections.shuffle(aiListPermutations);
				}
				else
				{
					// Randomly generate some permutations of indices for the list of AIs
					aiListPermutations = ListUtils.samplePermutations(
							TIntArrayList.wrap(IntStream.range(0, numPlayers).toArray()), 120);
				}
				
				{
					// Do a warming up (just a few seconds anyway, so let's not worry about parallelisation)
					final Trial trial = new Trial(game);
					final Context context = new Context(game, trial);
					long stopAt = 0L;
					final long start = System.nanoTime();
					final double abortAt = start + trialsBatch.warmingUpSecs * 1000000000.0;
					while (stopAt < abortAt)
					{
						game.start(context);
						game.playout(context, null, 1.0, null, -1, -1, ThreadLocalRandom.current());
						stopAt = System.nanoTime();
					}
					System.gc();
				}
				
				// Prepare results writing (ResultsSummary is thread-safe)
				final List<String> agentStrings = new ArrayList<String>();
				for (final String agentString : trialsBatch.agentStrings)
				{
					agentStrings.add(AIFactory.createAI(agentString).friendlyName());
				}
				final ResultsSummary resultsSummary = new ResultsSummary(game, agentStrings);
				final CountDownLatch resultsSummaryLatch = new CountDownLatch(trialsBatch.numTrials);
				
				for (int trialCounter = 0; trialCounter < trialsBatch.numTrials; ++trialCounter)
				{
					// Submit another job to the thread pool for this specific trial
					final int thisTrialCounter = trialCounter;		// Need to make this variable final for inner class
					
					threadPool.submit
					(
						() -> 
						{
							try
							{
								numCoresAvailable.addAndGet(-numThreadsPerTrial);
								
								// Compute list of AIs to use for this trial (we rotate every trial)
								final List<AI> currentAIList = new ArrayList<AI>(numPlayers);
								final int currentAIsPermutation = thisTrialCounter % aiListPermutations.size();
								
								final TIntArrayList currentPlayersPermutation = aiListPermutations.get(currentAIsPermutation);
								currentAIList.add(null); // 0 index not used

								for (int i = 0; i < currentPlayersPermutation.size(); ++i)
								{
									currentAIList.add
									(
										AIFactory.createAI(trialsBatch.agentStrings[currentPlayersPermutation.getQuick(i) % numPlayers])
									);
								}

								// Play a game
								final Trial trial = new Trial(game);
								final Context context = new Context(game, trial);
								game.start(context);

								for (int p = 1; p < currentAIList.size(); ++p)
								{
									currentAIList.get(p).initAI(game, p);
								}
								
								final Model model = context.model();

								while (!context.trial().over())
								{
									model.startNewStep
									(
										context, currentAIList, trialsBatch.thinkingTime, trialsBatch.iterationLimit, 
										-1, 0.0
									);
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
							catch (final Exception e)
							{
								e.printStackTrace();
							}
							finally
							{
								numCoresAvailable.addAndGet(numThreadsPerTrial);
								resultsSummaryLatch.countDown();
							}
						}
					);
				}
				
				// Submit job to write results once we're ready
				resultsWritingPool.submit
				(
					() -> 
					{
						try
						{
							resultsSummaryLatch.await(maxWallTime, TimeUnit.MINUTES);
							// TODO actually write results
						}
						catch (final Exception e)
						{
							e.printStackTrace();
						}
					}
				);

				while (numCoresAvailable.get() <= 0)
				{
					// We could just continue creating jobs and submitting them to the pool, but then we would
					// already be loading all those games into memory. We don't want to do that, so we'll wait
					// with submitting more tasks until we actually have cores to run them.
					Thread.sleep(10000L);
				}
			}
			
			// Shut down, but wait until everything that is still running is done
			if (threadPool != null)
			{
				final long maxWallTimeMillis = maxWallTime * 60 * 1000L;
				long alreadyElapsedTime = System.currentTimeMillis() - startTime;
				
				threadPool.shutdown();
				resultsWritingPool.shutdown();
				threadPool.awaitTermination(maxWallTimeMillis - alreadyElapsedTime, TimeUnit.MILLISECONDS);
				
				alreadyElapsedTime = System.currentTimeMillis() - startTime;
				resultsWritingPool.awaitTermination(maxWallTimeMillis - alreadyElapsedTime, TimeUnit.MILLISECONDS);
			}
		}
		catch (final InterruptedException e) {
			e.printStackTrace();
		}
	}

	//-------------------------------------------------------------------------
	
	/**
	 * A single batch of trials (between a specific set of agents for a single game)
	 * that we wish to run.
	 * 
	 * @author Dennis Soemers
	 */
	public static class TrialsBatchToRun
	{
		
		protected final String gameName;
		protected final String ruleset;
		protected final int numTrials;
		protected final int gameLengthCap;
		protected final double thinkingTime;
		protected final int iterationLimit;
		protected final int warmingUpSecs;
		protected final String outDir;
		protected final String[] agentStrings;
		
		/**
		 * Constructor
		 * 
		 * @param gameName
		 * @param ruleset
		 * @param numTrials
		 * @param gameLengthCap
		 * @param thinkingTime
		 * @param iterationLimit
		 * @param warmingUpSecs
		 * @param outDir
		 * @param agentStrings
		 */
		public TrialsBatchToRun
		(
			final String gameName, final String ruleset, final int numTrials, final int gameLengthCap, 
			final double thinkingTime, final int iterationLimit, final int warmingUpSecs, final String outDir, 
			final String[] agentStrings
		) 
		{
			this.gameName = gameName;
			this.ruleset = ruleset;
			this.numTrials = numTrials;
			this.gameLengthCap = gameLengthCap;
			this.thinkingTime = thinkingTime;
			this.iterationLimit = iterationLimit;
			this.warmingUpSecs = warmingUpSecs;
			this.outDir = outDir;
			this.agentStrings = agentStrings;
		}
		
		public void toJson(final String jsonFilepath)
		{
			BufferedWriter bw = null;
			try
			{
				final File file = new File(jsonFilepath);
				file.getParentFile().mkdirs();
				if (!file.exists())
					file.createNewFile();

				final JSONObject json = new JSONObject();
				
				json.put("gameName", gameName);
				json.put("ruleset", ruleset);
				json.put("numTrials", numTrials);
				json.put("gameLengthCap", gameLengthCap);
				json.put("thinkingTime", thinkingTime);
				json.put("iterationLimit", iterationLimit);
				json.put("warmingUpSecs", warmingUpSecs);
				json.put("outDir", outDir);
				final JSONArray agentStringsJsonArray = new JSONArray(Arrays.asList(agentStrings));
				json.put("agentStrings", agentStringsJsonArray);

				final FileWriter fw = new FileWriter(file);
				bw = new BufferedWriter(fw);
				bw.write(json.toString(4));
				
			}
			catch (final Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				try
				{
					if (bw != null)
						bw.close();
				}
				catch (final Exception ex)
				{
					System.out.println("Error in closing the BufferedWriter" + ex);
				}
			}
		}
		
		public static TrialsBatchToRun fromJson(final String filepath)
		{
			try (final InputStream inputStream = new FileInputStream(new File(filepath)))
			{
				final JSONObject json = new JSONObject(new JSONTokener(inputStream));
				
				final String gameName = json.getString("gameName");
				final String ruleset = json.getString("ruleset");
				final int numTrials = json.getInt("numTrials");
				final int gameLengthCap = json.getInt("gameLengthCap");
				final double thinkingTime = json.getDouble("thinkingTime");
				final int iterationLimit = json.getInt("iterationLimit");
				final int warmingUpSecs = json.getInt("warmingUpSecs");
				final String outDir = json.getString("outDir");
				final JSONArray jArray = json.optJSONArray("agentStrings");
				final String[] agentStrings = jArray.toList().toArray(new String[0]);
				
				return new TrialsBatchToRun(gameName, ruleset, numTrials, gameLengthCap, thinkingTime, iterationLimit, 
						warmingUpSecs, outDir, agentStrings);

			}
			catch (final Exception e)
			{
				e.printStackTrace();
				return null;
			}
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
					"Evaluate many agents in many games in parallel. Configuration of all experiments to be run should be in a JSON file."
				);
		
		argParse.addOption(new ArgOption()
				.withNames("--num-cores-total")
				.help("Total number of cores we expect to be able to use for all jobs together.")
				.withNumVals(1)
				.withType(OptionTypes.Int)
				.setRequired());
		argParse.addOption(new ArgOption()
				.withNames("--num-threads-per-trial")
				.help("Number of threads to be used per individual trial (e.g., by AIs).")
				.withNumVals(1)
				.withType(OptionTypes.Int)
				.setRequired());
		
		argParse.addOption(new ArgOption()
				.withNames("--json-files")
				.help("JSON files, each describing one batch of trials, which we should run in this job.")
				.withNumVals("+")
				.withType(OptionTypes.String)
				.setRequired());
		
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
		
		// Parse the args
		if (!argParse.parseArguments(args))
			return;

		// Use the parsed args
		final ParallelEvalMultiGamesMultiAgents experiment = 
				new ParallelEvalMultiGamesMultiAgents
				(
					argParse.getValueBool("--useGUI"), 
					argParse.getValueInt("--max-wall-time")
				);
		
		experiment.numCoresTotal = argParse.getValueInt("--num-cores-total");
		experiment.numThreadsPerTrial = argParse.getValueInt("--num-threads-per-trial");
		experiment.jsonFiles = (List<String>) argParse.getValue("--json-files");
		
		experiment.startExperiment();
	}

}
