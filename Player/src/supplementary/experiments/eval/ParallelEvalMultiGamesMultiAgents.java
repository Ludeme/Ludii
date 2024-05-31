package supplementary.experiments.eval;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import features.feature_sets.network.JITSPatterNetFeatureSet;
import main.CommandLineArgParse;

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
	
	/* Auxiliary experiment setup */

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
	 * A single batch of trials (between a specific set of agents for a single game)
	 * that we wish to run.
	 * 
	 * @author Dennis Soemers
	 */
	public static class TrialsBatchToRun
	{
		
		private final String gameName;
		private final String ruleset;
		private final int numTrials;
		private final int gameLengthCap;
		private final double thinkingTime;
		private final int iterationLimit;
		private final int warmingUpSecs;
		private final String outDir;
		private final String[] agentStrings;
		private final int maxWallTime;		// in minutes
		private final String outRedirect;
		private final String errRedirect;
		
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
		 * @param maxWallTime
		 * @param outRedirect
		 * @param errRedirect
		 */
		public TrialsBatchToRun
		(
			final String gameName, final String ruleset, final int numTrials, final int gameLengthCap, 
			final double thinkingTime, final int iterationLimit, final int warmingUpSecs, final String outDir, 
			final String[] agentStrings, final int maxWallTime, final String outRedirect, final String errRedirect
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
			this.maxWallTime = maxWallTime;
			this.outRedirect = outRedirect;
			this.errRedirect = errRedirect;
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
				json.put("maxWallTime", maxWallTime);
				json.put("outRedirect", outRedirect);
				json.put("errRedirect", errRedirect);

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
				final int maxWallTime = json.getInt("maxWallTime");
				final String outRedirect = json.getString("outRedirect");
				final String errRedirect = json.getString("errRedirect");
				
				return new TrialsBatchToRun(gameName, ruleset, numTrials, gameLengthCap, thinkingTime, iterationLimit, 
						warmingUpSecs, outDir, agentStrings, maxWallTime, outRedirect, errRedirect);

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
	}

}
