package supplementary.experiments.speed;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

import game.Game;
import main.CommandLineArgParse;
import main.CommandLineArgParse.ArgOption;
import main.CommandLineArgParse.OptionTypes;
import other.GameLoader;
import main.FileHandling;
import utils.LudiiGameWrapper;
import utils.LudiiStateWrapper;

/**
 * Experiment for timing random playouts in which we also create a state tensor
 * for every state that we encounter. Note that this also means that we cannot
 * use optimised playout strategies.
 * 
 * @author Dennis Soemers
 */
public final class TimeTensorsRandomPlayouts
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
	
	/** Number of seconds over which we measure playouts (per game) */
	private int measureSecs;
	
	/** Maximum number of actions to execute per playout (-1 for no cap) */
	private int playoutActionCap;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 */
	private TimeTensorsRandomPlayouts()
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
			final LudiiGameWrapper gameWrapper = new LudiiGameWrapper(game);
			final LudiiStateWrapper stateWrapper = new LudiiStateWrapper(gameWrapper);
			
			// Warming up
			long stopAt = 0L;
			long start = System.nanoTime();
			double abortAt = start + warmingUpSecs * 1000000000.0;
			while (stopAt < abortAt)
			{
				stateWrapper.reset();
				
				int numActionsPlayed = 0;
				while (!stateWrapper.isTerminal() && (numActionsPlayed < playoutActionCap || playoutActionCap < 0))
				{
					// Compute tensor for current state
					@SuppressWarnings("unused")		// Do NOT remove! We need this for accurate timings!!!!!!!!!
					final float[][][] stateTensor = stateWrapper.toTensor();
					
					// Play random action
					stateWrapper.applyNthMove(ThreadLocalRandom.current().nextInt(stateWrapper.numLegalMoves()));
					++numActionsPlayed;
				}

				stopAt = System.nanoTime();
			}
			System.gc();
			
			// The Test
			stopAt = 0L;
			start = System.nanoTime();
			abortAt = start + measureSecs * 1000000000.0;
			int playouts = 0;
			long numDecisions = 0L;
			while (stopAt < abortAt)
			{
				stateWrapper.reset();
				
				int numActionsPlayed = 0;
				while (!stateWrapper.isTerminal() && (numActionsPlayed < playoutActionCap || playoutActionCap < 0))
				{
					// Compute tensor for current state
					@SuppressWarnings("unused")		// Do NOT remove! We need this for accurate timings!!!!!!!!!
					final float[][][] stateTensor = stateWrapper.toTensor();
					
					// Play random action
					stateWrapper.applyNthMove(ThreadLocalRandom.current().nextInt(stateWrapper.numLegalMoves()));
					++numActionsPlayed;
				}
				
				numDecisions += stateWrapper.trial().numMoves() - stateWrapper.trial().numInitialPlacementMoves();
				stopAt = System.nanoTime();
				playouts++;
			}

			final double secs = (stopAt - start) / 1000000000.0;
			final double rate = (playouts / secs);
			final double decisionsPerPlayout = ((double) numDecisions) / playouts;

			System.out.println(game.name() + "\t-\t" + rate + " p/s\t-\t" + decisionsPerPlayout + " decisions per playout\n");
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
				.withNames("--playout-action-cap")
				.help("Maximum number of actions to execute per playout (-1 for no cap).")
				.withDefault(Integer.valueOf(-1))
				.withNumVals(1)
				.withType(OptionTypes.Int));
		
		// parse the args
		if (!argParse.parseArguments(args))
			return;
		
		// use the parsed args
		final TimeTensorsRandomPlayouts experiment = new TimeTensorsRandomPlayouts();
		
		experiment.gameNames = (List<String>) argParse.getValue("--games");
		experiment.excludeDirs = (List<String>) argParse.getValue("--exclude-dirs");
		experiment.gameOptions = (List<String>) argParse.getValue("--game-options");
		experiment.warmingUpSecs = argParse.getValueInt("--warming-up-secs");
		experiment.measureSecs = argParse.getValueInt("--measure-secs");
		experiment.playoutActionCap = argParse.getValueInt("--playout-action-cap");

		experiment.startExperiment();
	}

}
