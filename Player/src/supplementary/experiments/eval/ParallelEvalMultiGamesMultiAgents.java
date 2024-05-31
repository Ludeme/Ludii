package supplementary.experiments.eval;

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
	
	public class TrialsBatchToRun
	{
		
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
