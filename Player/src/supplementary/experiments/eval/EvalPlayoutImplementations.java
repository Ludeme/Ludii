package supplementary.experiments.eval;

import java.io.File;

import main.CommandLineArgParse;
import main.CommandLineArgParse.ArgOption;
import main.CommandLineArgParse.OptionTypes;

/**
 * Implementation of an experiment that evaluates the performance
 * of specialised playout implementations.
 * 
 * Note that this is not about MCTS-specific playout enhancements,
 * but about different implementations for the "standard" uniform-random
 * playouts (AddToEmpty, ByPiece, ...)
 * 
 * @author Dennis Soemers
 */
public class EvalPlayoutImplementations
{
	/* Game setup */

	/** Name of the game to play. Should end with .lud */
	protected String gameName;

	// -------------------------------------------------------------------------
	/* Basic experiment setup */

	/** Number of evaluation games to run */
	protected int numGames;

	/** Max allowed thinking time per move (in seconds) */
	protected double thinkingTime;

	/** Max allowed number of MCTS iterations per move */
	protected int iterationLimit;

	// -------------------------------------------------------------------------
	/** File saving stuff */
	
	/** Output directory */
	protected File outDir;

	// -------------------------------------------------------------------------
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
	public EvalPlayoutImplementations()
	{
		// all defaults already set above
	}

	/**
	 * Constructor. No wall time limit.
	 * @param useGUI
	 */
	public EvalPlayoutImplementations(final boolean useGUI)
	{
		this.useGUI = useGUI;
	}
	
	/**
	 * Constructor
	 * @param useGUI
	 * @param maxWallTime Wall time limit in minutes.
	 */
	public EvalPlayoutImplementations(final boolean useGUI, final int maxWallTime)
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
		// TODO
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Can be used for quick testing without command-line args, or proper
	 * testing with elaborate setup through command-line args
	 * @param args
	 */
	public static void main(final String[] args)
	{		
		// define options for arg parser
		final CommandLineArgParse argParse = 
				new CommandLineArgParse
				(
					true,
					"Evaluate playing strength of MCTS agents with different playout implementations against each other."
				);
		
		argParse.addOption(new ArgOption()
				.withNames("--game")
				.help("Name of the game to play. Should end with \".lud\".")
				.withDefault("Amazons.lud")
				.withNumVals(1)
				.withType(OptionTypes.String));
		
		argParse.addOption(new ArgOption()
				.withNames("-n", "--num-games", "--num-eval-games")
				.help("Number of training games to run.")
				.withDefault(Integer.valueOf(200))
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
				.withNames("--out-dir", "--output-directory")
				.help("Filepath for output directory")
				.withNumVals(1)
				.withType(OptionTypes.String));
		
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
		final EvalPlayoutImplementations eval = 
				new EvalPlayoutImplementations
				(
					argParse.getValueBool("--useGUI"), 
					argParse.getValueInt("--max-wall-time")
				);
		
		eval.gameName = argParse.getValueString("--game");
		
		eval.numGames = argParse.getValueInt("-n");
		eval.thinkingTime = argParse.getValueDouble("--thinking-time");
		eval.iterationLimit = argParse.getValueInt("--iteration-limit");

		final String outDirFilepath = argParse.getValueString("--out-dir");
		if (outDirFilepath != null)
			eval.outDir = new File(outDirFilepath);
		else
			eval.outDir = null;
		
		eval.startExperiment();
	}
}
