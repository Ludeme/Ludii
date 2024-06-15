package supplementary.experiments.concepts;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.json.JSONObject;
import org.json.JSONTokener;

import features.feature_sets.network.JITSPatterNetFeatureSet;
import game.Game;
import main.CommandLineArgParse;
import main.CommandLineArgParse.ArgOption;
import main.CommandLineArgParse.OptionTypes;
import main.DaemonThreadFactory;
import other.GameLoader;
import other.concept.Concept;
import other.concept.ConceptDataType;

/**
 * Implementation of an experiment that computes concepts for multiple games in parallel.
 * Primarily meant for use on larger compute nodes with many cores available.
 * 
 * @author Dennis Soemers
 */
public class ParallelComputeConceptsMultipleGames 
{
	
	//-------------------------------------------------------------------------
	
	protected int numCoresTotal;
	protected int numThreadsPerJob;
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
	public ParallelComputeConceptsMultipleGames()
	{
		// all defaults already set above
	}

	/**
	 * Constructor. No wall time limit.
	 * @param useGUI
	 */
	public ParallelComputeConceptsMultipleGames(final boolean useGUI)
	{
		this.useGUI = useGUI;
	}

	/**
	 * Constructor
	 * @param useGUI
	 * @param maxWallTime Wall time limit in minutes.
	 */
	public ParallelComputeConceptsMultipleGames(final boolean useGUI, final int maxWallTime)
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
		final List<Concept> booleanConcepts = new ArrayList<Concept>();
		final List<Concept> nonBooleanConcepts = new ArrayList<Concept>();
		for (final Concept concept : Concept.values())
		{
			if (concept.dataType().equals(ConceptDataType.BooleanData))
				booleanConcepts.add(concept);
			else
				nonBooleanConcepts.add(concept);
		}
		
		final AtomicInteger numCoresAvailable = new AtomicInteger(numCoresTotal);
		
		@SuppressWarnings("resource")
		final ExecutorService threadPool = Executors.newFixedThreadPool(
				(int) Math.ceil((double)numCoresTotal / numThreadsPerJob), 
				DaemonThreadFactory.INSTANCE);
		
		// Queue of jobs that are waiting to be submitted because they depend on a set of other jobs
		final List<WaitingJob> waitingJobs = new LinkedList<WaitingJob>();
		
		final long startTime = System.currentTimeMillis();
		
		try
		{
			for (final String jsonFile : jsonFiles)
			{
				// First check if we can now submit any jobs that were still waiting around
				final Iterator<WaitingJob> it = waitingJobs.iterator();
				while (it.hasNext() && numCoresAvailable.get() >= numThreadsPerJob)
				{
					final WaitingJob job = it.next();
					if (job.checkDependencies()) 
					{
						// This job can be submitted now
						it.remove();
						numCoresAvailable.addAndGet(-numThreadsPerJob);
						threadPool.submit(job.runnable);
					}
				}
				
				while (numCoresAvailable.get() <= 0)
				{
					// We could just continue creating jobs and submitting them to the pool, but then we would
					// already be loading all those games into memory. We don't want to do that, so we'll wait
					// with submitting more tasks until we actually have cores to run them.
					Thread.sleep(20000L);
				}
				
				// Let's try to start jobs for another game
				final GameRulesetToCompute experiment = GameRulesetToCompute.fromJson(jsonFile);
				
				// Load game for this batch
				final Game game;
				
				if (experiment.treatGameNameAsFilepath)
				{
					if (experiment.ruleset != null && !experiment.ruleset.equals(""))
						game = GameLoader.loadGameFromFile(new File(experiment.gameName), experiment.ruleset);
					else
						game = GameLoader.loadGameFromFile(new File(experiment.gameName), new ArrayList<String>());	// TODO add support for options
				}
				else
				{
					if (experiment.ruleset != null && !experiment.ruleset.equals(""))
						game = GameLoader.loadGameFromName(experiment.gameName, experiment.ruleset);
					else
						game = GameLoader.loadGameFromName(experiment.gameName, new ArrayList<String>());	// TODO add support for options
				}
				
				game.setMaxTurns(Math.min(5000, game.getMaxTurnLimit()));
				
				// Let's clear some unnecessary memory
				game.description().setParseTree(null);
				game.description().setExpanded(null);
								
				System.out.println("Num cores available: " + numCoresAvailable.get());
				System.out.println("Submitting jobs.");
				System.out.println("Game: " + experiment.gameName);
				System.out.println("Ruleset: " + experiment.ruleset);
				
				// Check if we need to submit jobs for generating trials (if we don't already have them)
				final File trialsDir = new File(experiment.trialsDir);
				int numExistingTrialFiles = 0;
				
				if (trialsDir.exists())
				{
					for (final File trialFile : trialsDir.listFiles())
					{
						if (trialFile.isFile() && trialFile.getAbsolutePath().endsWith(".txt"))
							++numExistingTrialFiles;
					}
				}
				
				final List<Future<?>> trialJobFutures = new LinkedList<Future<?>>();
				
				if (numExistingTrialFiles < experiment.numTrials)
				{
					// We have to submit a job to create more trial files
					final int numTrialsToRun = experiment.numTrials - numExistingTrialFiles;
					final int firstTrialIndex = numExistingTrialFiles;
					
					numCoresAvailable.addAndGet(-numThreadsPerJob);
					trialJobFutures.add
					(
						threadPool.submit
						(
							() -> 
							{
								try
								{								
									generateRandomTrials
									(
										game, numTrialsToRun, firstTrialIndex,
										trialsDir, numThreadsPerJob
									);
								}
								catch (final Exception e)
								{
									e.printStackTrace();
								}
								finally
								{
									numCoresAvailable.addAndGet(numThreadsPerJob);
								}
							}
						)
					);
				}
				
				// Submit jobs for computing concepts
				// TODO

				while (numCoresAvailable.get() <= 0)
				{
					// We could just continue creating jobs and submitting them to the pool, but then we would
					// already be loading all those games into memory. We don't want to do that, so we'll wait
					// with submitting more tasks until we actually have cores to run them.
					Thread.sleep(20000L);
				}
			}
			
			// Shut down, but wait until everything that is still running is done
			if (threadPool != null)
			{
				final long maxWallTimeMillis = maxWallTime * 60 * 1000L;
				final long alreadyElapsedTime = System.currentTimeMillis() - startTime;
				
				threadPool.shutdown();
				threadPool.awaitTermination(maxWallTimeMillis - alreadyElapsedTime, TimeUnit.MILLISECONDS);
			}
		}
		catch (final InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Helper method for parallelised generation of random trials
	 * 
	 * @param game
	 * @param numTrialsToRun
	 * @param firstTrialIndex
	 * @param trialsDir
	 * @param numThreads
	 */
	protected static void generateRandomTrials
	(
		final Game game, final int numTrialsToRun, final int firstTrialIndex,
		final File trialsDir, final int numThreads
	)
	{
		@SuppressWarnings("resource")
		final ExecutorService threadPool = Executors.newFixedThreadPool(numThreads, DaemonThreadFactory.INSTANCE);
		
		try
		{
			// TODO
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			threadPool.shutdown();
			try 
			{
				threadPool.awaitTermination(24, TimeUnit.HOURS);
			} 
			catch (final InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	//-------------------------------------------------------------------------
	
	/**
	 * A single game (+ ruleset) for which we wish to compute concepts.
	 * 
	 * @author Dennis Soemers
	 */
	public static class GameRulesetToCompute
	{
		
		protected final String gameName;
		protected final String ruleset;
		protected final int numTrials;
		protected final String trialsDir;
		protected final String conceptsDir;
		protected final boolean treatGameNameAsFilepath;
		
		/**
		 * Constructor
		 * 
		 * @param gameName
		 * @param ruleset
		 * @param numTrials
		 * @param trialsDir
		 * @param conceptsDir
		 * @param treatGameNameAsFilepath
		 */
		public GameRulesetToCompute
		(
			final String gameName, final String ruleset, final int numTrials,
			final String trialsDir, final String conceptsDir, 
			final boolean treatGameNameAsFilepath
		) 
		{
			this.gameName = gameName;
			this.ruleset = ruleset;
			this.numTrials = numTrials;
			this.trialsDir = trialsDir;
			this.conceptsDir = conceptsDir;
			this.treatGameNameAsFilepath = treatGameNameAsFilepath;
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
				json.put("trialsDir", trialsDir);
				json.put("conceptsDir", conceptsDir);
				json.put("treatGameNameAsFilepath", treatGameNameAsFilepath);

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
		
		public static GameRulesetToCompute fromJson(final String filepath)
		{
			try (final InputStream inputStream = new FileInputStream(new File(filepath)))
			{
				final JSONObject json = new JSONObject(new JSONTokener(inputStream));
				
				final String gameName = json.getString("gameName");
				final String ruleset = json.getString("ruleset");
				final int numTrials = json.getInt("numTrials");
				final String trialsDir = json.getString("trialsDir");
				final String conceptsDir = json.getString("conceptsDir");
				final boolean treatGameNameAsFilepath = json.optBoolean("treatGameNameAsFilepath", false);
				
				return new GameRulesetToCompute(gameName, ruleset, numTrials, 
						trialsDir, conceptsDir, treatGameNameAsFilepath);

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
	 * A job that is waiting for other jobs that need to be finished first before
	 * it can be submitted to an ExecutorService
	 * 
	 * @author Dennis Soemers
	 */
	public static class WaitingJob
	{
		
		/** The actual job */
		protected final Runnable runnable;
		
		/** Jobs which must've completed running before we can be queued */
		protected final List<Future<?>> dependencies;
		
		/** Num threads this job expects to be using */
		protected final int numThreads;
		
		/**
		 * Constructor
		 * 
		 * @param runnable
		 * @param dependencies
		 * @param numThreads
		 */
		public WaitingJob(final Runnable runnable, final List<Future<?>> dependencies, final int numThreads)
		{
			this.runnable = runnable;
			this.dependencies = dependencies;
			this.numThreads = numThreads;
		}
		
		/**
		 * @return True if and only if all dependencies have finished running.
		 */
		public boolean checkDependencies()
		{
			final Iterator<Future<?>> it = dependencies.iterator();
			while (it.hasNext())
			{
				final Future<?> future = it.next();
				
				if (future.isDone())
					it.remove();
				else
					return false;
			}
			
			return true;
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
					"Compute concepts for multiple games in parallel. Configuration of all experiments to be run should be in JSON files."
				);
		
		argParse.addOption(new ArgOption()
				.withNames("--num-cores-total")
				.help("Total number of cores we expect to be able to use for all jobs together.")
				.withNumVals(1)
				.withType(OptionTypes.Int)
				.setRequired());
		argParse.addOption(new ArgOption()
				.withNames("--num-threads-per-job")
				.help("Number of threads to be used per job. Jobs may either generate random trials, or compute concepts")
				.withNumVals(1)
				.withType(OptionTypes.Int)
				.setRequired());
		
		argParse.addOption(new ArgOption()
				.withNames("--json-files")
				.help("JSON files, each describing one game for which we should compute trials/concepts in this job.")
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
		final ParallelComputeConceptsMultipleGames experiment = 
				new ParallelComputeConceptsMultipleGames
				(
					argParse.getValueBool("--useGUI"), 
					argParse.getValueInt("--max-wall-time")
				);
		
		experiment.numCoresTotal = argParse.getValueInt("--num-cores-total");
		experiment.numThreadsPerJob = argParse.getValueInt("--num-threads-per-job");
		experiment.jsonFiles = (List<String>) argParse.getValue("--json-files");
		
		experiment.startExperiment();
	}

}
