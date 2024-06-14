package supplementary.experiments.concepts;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
		final AtomicInteger numCoresAvailable = new AtomicInteger(numCoresTotal);
		
		@SuppressWarnings("resource")
		final ExecutorService threadPool = Executors.newFixedThreadPool(numCoresTotal, DaemonThreadFactory.INSTANCE);
		
		final long startTime = System.currentTimeMillis();
		
		try
		{
			for (final String jsonFile : jsonFiles)
			{
				// Let's try to start another batch of trials
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
				
				// Let's clear some unnecessary memory
				game.description().setParseTree(null);
				game.description().setExpanded(null);
								
				System.out.println("Num cores available: " + numCoresAvailable.get());
				System.out.println("Submitting jobs.");
				System.out.println("Game: " + experiment.gameName);
				System.out.println("Ruleset: " + experiment.ruleset);
				
				// TODO start creating and submitting jobs here

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
	 * A single game (+ ruleset) for which we wish to compute concepts.
	 * 
	 * @author Dennis Soemers
	 */
	public static class GameRulesetToCompute
	{
		
		protected final String gameName;
		protected final String ruleset;
		protected final String outDir;
		protected final boolean treatGameNameAsFilepath;
		
		/**
		 * Constructor
		 * 
		 * @param gameName
		 * @param ruleset
		 * @param outDir
		 * @param treatGameNameAsFilepath
		 */
		public GameRulesetToCompute
		(
			final String gameName, final String ruleset, 
			final String outDir, final boolean treatGameNameAsFilepath
		) 
		{
			this.gameName = gameName;
			this.ruleset = ruleset;
			this.outDir = outDir;
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
				json.put("outDir", outDir);
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
				final String outDir = json.getString("outDir");
				final boolean treatGameNameAsFilepath = json.optBoolean("treatGameNameAsFilepath", false);
				
				return new GameRulesetToCompute(gameName, ruleset, outDir, treatGameNameAsFilepath);

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
					"Compute concepts for multiple games in parallel. Configuration of all experiments to be run should be in JSON files."
				);
		
		argParse.addOption(new ArgOption()
				.withNames("--num-cores-total")
				.help("Total number of cores we expect to be able to use for all jobs together.")
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
		final ParallelComputeConceptsMultipleGames experiment = 
				new ParallelComputeConceptsMultipleGames
				(
					argParse.getValueBool("--useGUI"), 
					argParse.getValueInt("--max-wall-time")
				);
		
		experiment.numCoresTotal = argParse.getValueInt("--num-cores-total");
		experiment.jsonFiles = (List<String>) argParse.getValue("--json-files");
		
		experiment.startExperiment();
	}

}
