package supplementary.experiments.speed;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import game.Game;
import main.CommandLineArgParse;
import main.CommandLineArgParse.ArgOption;
import main.CommandLineArgParse.OptionTypes;
import main.FileHandling;
import main.collections.FastArrayList;
import other.GameLoader;
import other.context.Context;
import other.move.Move;
import other.trial.Trial;

/**
 * Experiment where we measure how far we can get in a certain amount of time in a
 * complete breadth-first tree expansion, storing a copy of every game state in memory
 * in the process.
 *
 * @author Dennis Soemers
 */
public class BreadthFirstExpansion
{
	/** 
	 * Names of the games to play. Each should end with ".lud". 
	 * Use "all" to run all games we can find. Runs all games by default
	 */
	private List<String> gameNames;
	
	/** List of game directories to exclude from experiment */
	private List<String> excludeDirs;
	
	/** Options to tweak game (variant, rules, board, etc.) */
	private List<String> gameOptions;
	
	/** Number of seconds of warming up (per game) */
	private int warmingUpSecs;
	
	/** Number of seconds over which we create breadth-first tree expansion */
	private int measureSecs;
	
	/** 
	 * Extra seconds we want to wait after measureSecs have passed. 
	 * Will still keep the tree in memory. 
	 * Useful for creating a memory snapshot during this time in profiler.
	 */
	private int postMeasureWaitSecs;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 */
	private BreadthFirstExpansion()
	{
		// Nothing to do here
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Start the experiment
	 */
	public void startExperiment()
	{
		// Gather all the game names
		final List<String> gameNamesToTest = new ArrayList<String>();
		
		if (gameNames.get(0).equalsIgnoreCase("all"))
		{
			// lowercase all the exclude dirs, makes tests easier
			for (int i = 0; i < excludeDirs.size(); ++i)
			{
				excludeDirs.set(i, excludeDirs.get(i).toLowerCase());
			}
			
			final String[] allGameNames = FileHandling.listGames();
			
			for (final String gameName : allGameNames)
			{
				final String name = gameName.replaceAll(Pattern.quote("\\"), "/");
				final String[] nameParts = name.split(Pattern.quote("/"));
				
				boolean exclude = false;
				for (final String part : nameParts)
				{
					if 
					(
						excludeDirs.contains(part.toLowerCase()) ||
						part.equals("plex") ||
						part.equals("bad") ||
						part.equals("bad_playout") ||
						part.equals("wip") ||
						part.equals("test")
					)
					{
						exclude = true;
						break;
					}
				}
				
				if (!exclude)
				{
					gameNamesToTest.add(name);
				}
			}
		}
		else
		{
			final String[] allGameNames = FileHandling.listGames();
			
			for (String gameName : gameNames)
			{
				gameName = gameName.replaceAll(Pattern.quote("\\"), "/");
				
				for (String name : allGameNames)
				{
					name = name.replaceAll(Pattern.quote("\\"), "/");
					
					if (name.endsWith(gameName))
					{
						gameNamesToTest.add(name);
					}
				}
			}
		}
		
		System.out.println("Starting timings for games: " + gameNamesToTest);
		System.out.println();
		System.out.println("Using " + warmingUpSecs + " warming-up seconds per game.");
		System.out.println("Measuring results over " + measureSecs + " seconds per game.");
		System.out.println();
		
		for (final String gameName : gameNamesToTest)
		{
			final Game game = GameLoader.loadGameFromName(gameName, gameOptions);
			final Trial startTrial = new Trial(game);
			final Context startContext = new Context(game, startTrial);
			
			if (!game.isAlternatingMoveGame())
			{
				System.err.println("WARNING: did not properly implement this for non-alternating-move games.");
			}
			
			// Warming up
			final List<Context> allContexts = new ArrayList<Context>();
			game.start(startContext);
			allContexts.add(new Context(startContext));
			int allContextsIdx = 0;
			
			long stopAt = 0L;
			long start = System.nanoTime();
			double abortAt = start + warmingUpSecs * 1000000000.0;
			while (stopAt < abortAt || allContextsIdx >= allContexts.size())
			{
				final Context c = allContexts.get(allContextsIdx++);
				final FastArrayList<Move> legalMoves = game.moves(c).moves();
				for (final Move m : legalMoves)
				{
					final Context copyContext = new Context(c);
					game.apply(copyContext, m);
					allContexts.add(copyContext);
				}
				stopAt = System.nanoTime();
			}
			
			allContexts.clear();
			System.gc();
			allContexts.add(new Context(startContext));
			allContextsIdx = 0;
			
			// The Test
			stopAt = 0L;
			start = System.nanoTime();
			abortAt = start + measureSecs * 1000000000.0;

			while (stopAt < abortAt || allContextsIdx >= allContexts.size())
			{
				final Context c = allContexts.get(allContextsIdx++);
				final FastArrayList<Move> legalMoves = game.moves(c).moves();
				for (final Move m : legalMoves)
				{
					final Context copyContext = new Context(c);
					game.apply(copyContext, m);
					allContexts.add(copyContext);
				}
				stopAt = System.nanoTime();
			}

			final double secs = (stopAt - start) / 1000000000.0;
			final double rate = (allContexts.size() / secs);

			System.out.println(game.name() + "\t-\t" + rate + " nodes/s");
			
			if (postMeasureWaitSecs > 0)
			{
				System.out.println("Waiting for " + postMeasureWaitSecs + " seconds, create memory snapshot now!");
				try
				{
					Thread.sleep(1000L * postMeasureWaitSecs);
				}
				catch (final InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Main method
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
					"Measure playouts per second for one or more games."
				);
		
		argParse.addOption(new ArgOption()
				.withNames("--games")
				.help("Names of the games to play. Each should end with \".lud\". "
						+ "Use \"all\" to run all games we can find. "
						+ "Runs all games by default.")
				.withDefault(Arrays.asList("all"))
				.withNumVals("+")
				.withType(OptionTypes.String));
		
		argParse.addOption(new ArgOption()
				.withNames("--exclude-dirs")
				.help("List of game directories to exclude from experiment.")
				.withDefault(Arrays.asList("puzzle"))
				.withNumVals("*")
				.withType(OptionTypes.String));
		
		argParse.addOption(new ArgOption()
				.withNames("--game-options")
				.help("Game Options to load.")
				.withDefault(new ArrayList<String>(0))
				.withNumVals("*")
				.withType(OptionTypes.String));
		
		argParse.addOption(new ArgOption()
				.withNames("--warming-up-secs", "--warming-up")
				.help("Number of seconds of warming up (per game).")
				.withDefault(Integer.valueOf(10))
				.withNumVals(1)
				.withType(OptionTypes.Int));
		argParse.addOption(new ArgOption()
				.withNames("--measure-secs")
				.help("Number of seconds over which we measure playouts (per game).")
				.withDefault(Integer.valueOf(30))
				.withNumVals(1)
				.withType(OptionTypes.Int));
		argParse.addOption(new ArgOption()
				.withNames("--post-measure-wait-secs")
				.help("Extra seconds we want to wait after measureSecs have passed.")
				.withDefault(Integer.valueOf(0))
				.withNumVals(1)
				.withType(OptionTypes.Int));
		
		// parse the args
		if (!argParse.parseArguments(args))
			return;
		
		// use the parsed args
		final BreadthFirstExpansion experiment = new BreadthFirstExpansion();
		
		experiment.gameNames = (List<String>) argParse.getValue("--games");
		experiment.excludeDirs = (List<String>) argParse.getValue("--exclude-dirs");
		experiment.gameOptions = (List<String>) argParse.getValue("--game-options");
		experiment.warmingUpSecs = argParse.getValueInt("--warming-up-secs");
		experiment.measureSecs = argParse.getValueInt("--measure-secs");
		experiment.postMeasureWaitSecs = argParse.getValueInt("--post-measure-wait-secs");

		experiment.startExperiment();
	}

}
