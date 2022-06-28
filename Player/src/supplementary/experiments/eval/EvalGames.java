package supplementary.experiments.eval;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.rng.RandomProviderState;
import org.apache.commons.rng.core.RandomProviderDefaultState;
import org.json.JSONObject;

import compiler.Compiler;
import game.Game;
import main.CommandLineArgParse;
import main.CommandLineArgParse.ArgOption;
import main.CommandLineArgParse.OptionTypes;
import main.Constants;
import main.FileHandling;
import main.grammar.Report;
import main.options.Ruleset;
import main.options.UserSelections;
import manager.network.DatabaseFunctionsPublic;
import manager.utils.game_logs.MatchRecord;
import metrics.Evaluation;
import metrics.Metric;
import other.AI;
import other.GameLoader;
import other.RankUtils;
import other.context.Context;
import other.move.Move;
import other.trial.Trial;
import utils.AIFactory;
import utils.DBGameInfo;

/**
 * Functions used when evaluating games.
 * 
 * @author Matthew.Stephenson
 */
public class EvalGames
{
	final static String outputFilePath = "EvalResults.csv";		//"../Mining/res/evaluation/Results.csv";
	
	//-------------------------------------------------------------------------
	
	/**
	 * Evaluates all games/rulesets.
	 */
	private static void evaluateAllGames
	(
		final Report report, final int numberTrials, final int maxTurns, final double thinkTime, 
		final String AIName, final boolean useDBGames
	)
	{
		final Evaluation evaluation = new Evaluation();
		final List<Metric> metrics = evaluation.conceptMetrics();
		final ArrayList<Double> weights = new ArrayList<>();
		for (int i = 0; i < metrics.size(); i++)
			weights.add(Double.valueOf(1));
		
		String outputString = "GameName,";
		for (int m = 0; m < metrics.size(); m++)
		{
			outputString += metrics.get(m).name() + ",";
		}
		outputString = outputString.substring(0, outputString.length()-1) + "\n";
		
		final String[] choices = FileHandling.listGames();
		for (final String s : choices)
		{
			if (!FileHandling.shouldIgnoreLudEvaluation(s))
			{
				System.out.println("\n" + s);
				final String gameName = s.split("\\/")[s.split("\\/").length-1];
				final Game tempGame = GameLoader.loadGameFromName(gameName);
				final List<Ruleset> rulesets = tempGame.description().rulesets();
				
				if (tempGame.hasSubgames()) // TODO, we don't currently support matches
					continue;
				
				if (rulesets != null && !rulesets.isEmpty())
				{
					// Record ludemeplexes for each ruleset
					for (int rs = 0; rs < rulesets.size(); rs++)
						if (!rulesets.get(rs).optionSettings().isEmpty())
							outputString += evaluateGame(evaluation, report, tempGame, rulesets.get(rs).optionSettings(), AIName, numberTrials, thinkTime, maxTurns, metrics, weights, useDBGames);
				}
				else
				{
					outputString += evaluateGame(evaluation, report, tempGame, tempGame.description().gameOptions().allOptionStrings(tempGame.getOptions()), AIName, numberTrials, thinkTime, maxTurns, metrics, weights, useDBGames);
				}
			}
		}
		
		try (final BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath, false)))
		{
			writer.write(outputString);
			writer.close();
		}
		catch (final IOException e1)
		{
			e1.printStackTrace();
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Evaluates a given game.
	 */
	public static String evaluateGame
	(
		final Evaluation evaluation,
		final Report report,
		final Game originalGame,
		final List<String> gameOptions,
		final String AIName,
		final int numGames,
		final double thinkingTimeEach,
		final int maxNumTurns, 
		final List<Metric> metricsToEvaluate, 
		final ArrayList<Double> weights,
		final boolean useDatabaseGames
	)
	{
		final Game game = (Game)Compiler.compile(originalGame.description(), new UserSelections(gameOptions), report, false);		
		game.setMaxTurns(maxNumTurns);
		
		final List<AI> aiPlayers = new ArrayList<>();
		for (int i = 0; i < Constants.MAX_PLAYERS+1; i++)
		{
			final JSONObject json = new JSONObject().put("AI",new JSONObject().put("algorithm", AIName));
			aiPlayers.add(AIFactory.fromJson(json));
		}
		
		final double[] thinkingTime = new double[aiPlayers.size()];
		for (int p = 1; p < aiPlayers.size(); ++p)
			thinkingTime[p] = thinkingTimeEach;
		
		final DatabaseFunctionsPublic databaseFunctionsPublic = DatabaseFunctionsPublic.construct();
		String analysisPanelString = "";
		
		// Initialise the AI agents needed.
		final int numPlayers = game.players().count();
		for (int i = 0; i < numPlayers; ++i)
		{
			final AI ai = aiPlayers.get(i + 1);
			final int playerIdx = i + 1;
			
			if (ai == null)
			{
				final String message = "Cannot run evaluation; Player " + playerIdx + " is not AI.\n";
				try
				{
					report.getReportMessageFunctions().printMessageInAnalysisPanel(message);
				}
				catch(final Exception e)
				{
					// probably running from command line.
					System.out.println(message);
				}
				return "\n";
			}
			else if (!ai.supportsGame(game))
			{
				final String message = "Cannot run evaluation; " + ai.friendlyName() + " does not support this game.\n";
				try
				{
					report.getReportMessageFunctions().printMessageInAnalysisPanel(message);
				}
				catch(final Exception e)
				{
					// probably running from command line.
					System.out.println(message);
				}
				return "\n";
			}
		}
		
		final String message = "Please don't touch anything until complete! \nGenerating trials: \n";
		try
		{
			report.getReportMessageFunctions().printMessageInAnalysisPanel(message);
		}
		catch(final Exception e)
		{
			// probably running from command line.
			System.out.println(message);
		}

		// If using Ludii AI, need to get the algorithm used.
		for (int p = 1; p <= game.players().count(); ++p)
			aiPlayers.get(p).initAI(game, p);
		String aiAlgorihtm = aiPlayers.get(1).name();
		if (aiAlgorihtm.length() > 7 && aiAlgorihtm.substring(0, 5).equals("Ludii"))
			aiAlgorihtm = aiAlgorihtm.substring(7, aiAlgorihtm.length()-1);
		
		// Get any valid trials that were in database.
		ArrayList<String> databaseTrials = new ArrayList<>();
		if (useDatabaseGames)
		{
			databaseTrials = databaseFunctionsPublic.getTrialsFromDatabase
			(
				game.name(), game.description().gameOptions().allOptionStrings(game.getOptions()), 
				aiAlgorihtm, thinkingTime[1], game.getMaxTurnLimit(), 
				game.description().raw().hashCode()
			);
			
			// Load files from a specific directory instead.
//			final String dirName = "/home/matthew/Downloads/Banqi";
//			final File dir = new File(dirName);
//			final File[] allFiles = dir.listFiles();
//			for(final File file : allFiles)
//			{
//				String totalContents = "";
//				BufferedReader br;
//				try
//				{
//					br = new BufferedReader(new FileReader(file));
//					String line = null;
//				    while ((line = br.readLine()) != null) 
//				    {
//				    	totalContents += line + "\n";
//				    }
//				}
//				catch (final IOException e)
//				{
//					e.printStackTrace();
//				} 
//				databaseTrials.add(totalContents);
//			}
		}

		// Generate trials and print generic results.
		final List<Trial> allStoredTrials = new ArrayList<>();
		final List<RandomProviderState> allStoredRNG = new ArrayList<>();
		final double[] sumScores = new double[game.players().count() + 1];
		int numDraws = 0;
		int numTimeouts = 0;
		long sumNumMoves = 0L;
		final Context context = new Context(game, new Trial(game));
		
		try
		{
			for (int gameCounter = 0; gameCounter < numGames; ++gameCounter)
			{
				RandomProviderDefaultState rngState = (RandomProviderDefaultState) context.rng().saveState();
				boolean usingSavedTrial = false;
				List<Move> savedTrialMoves = new ArrayList<>();
				
				if (databaseTrials.size() > gameCounter)
				{
					usingSavedTrial = true;
					
					final Path tempFile = Files.createTempFile(null, null);
					Files.write(tempFile, databaseTrials.get(gameCounter).getBytes(StandardCharsets.UTF_8));
					final File file = new File(tempFile.toString());
					final MatchRecord savedMatchRecord = MatchRecord.loadMatchRecordFromTextFile(file, game);
					
					savedTrialMoves = savedMatchRecord.trial().generateCompleteMovesList();
					rngState = savedMatchRecord.rngState();
					context.rng().restoreState(rngState);
				}
				
				allStoredRNG.add(rngState);
				
				// Play a game
				game.start(context);
				for (int p = 1; p <= game.players().count(); ++p)
					aiPlayers.get(p).initAI(game, p);
				
				if (usingSavedTrial)
					for (int i = context.trial().numMoves(); i < savedTrialMoves.size(); i++)
						context.game().apply(context, savedTrialMoves.get(i));	

				while (!context.trial().over())
				{
					context.model().startNewStep
					(
						context, 
						aiPlayers, 
						thinkingTime, 
						-1, -1, 0.0,
						true, // block call until it returns
						false, false, 
						null, null
					);
					
					while (!context.model().isReady())
						Thread.sleep(100L);
				}
				
				final double[] utils = RankUtils.agentUtilities(context);
				for (int p = 1; p <= game.players().count(); ++p)
					sumScores[p] += (utils[p] + 1.0) / 2.0;	// convert [-1, 1] to [0, 1]
								
				if (context.trial().status().winner() == 0)
					++numDraws;
				
				if 
				(
					(
						context.state().numTurn() 
						>= 
						game.getMaxTurnLimit() * game.players().count()					
					)
					|| 
					(
						context.trial().numMoves() - context.trial().numInitialPlacementMoves() 
						>= 
						game.getMaxMoveLimit()
					)
				)
				{
					++numTimeouts;
				}
				
				sumNumMoves += context.trial().numMoves() - context.trial().numInitialPlacementMoves();
				
				try
				{
					report.getReportMessageFunctions().printMessageInAnalysisPanel(".");
				}
				catch(final Exception e)
				{
					// probably running from command line.
					System.out.print(".");
				}
				
				allStoredTrials.add(new Trial(context.trial()));
				
				if (!usingSavedTrial)					
					databaseFunctionsPublic.storeTrialInDatabase
					(
						game.name(), 
						game.description().gameOptions().allOptionStrings(game.getOptions()), 
						aiAlgorihtm, thinkingTime[1], game.getMaxTurnLimit(), 
						game.description().raw().hashCode(), new Trial(context.trial()), rngState
					);
				
				// Close AIs
				for (int p = 1; p < aiPlayers.size(); ++p)
					aiPlayers.get(p).closeAI();
			}
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
		
		try
		{
			report.getReportMessageFunctions().printMessageInAnalysisPanel("\nCalculating metrics: \n");
		}
		catch(final Exception e)
		{
			// probably running from command line.
			System.out.print("\nTrials completed.\n");
		}
		
		final DecimalFormat df = new DecimalFormat("#.#####");
		final String drawPercentage = df.format(numDraws*100.0/numGames) + "%";
		final String timeoutPercentage = df.format(numTimeouts*100.0/numGames) + "%";
		
		analysisPanelString += "\n\nAgent type: " + aiPlayers.get(0).friendlyName();
		analysisPanelString += "\nDraw likelihood: " + drawPercentage;
		analysisPanelString += "\nTimeout likelihood: " + timeoutPercentage;
		analysisPanelString += "\nAverage number of moves per game: " + df.format(sumNumMoves/(double)numGames);
		
		for (int i = 1; i < sumScores.length; i++)
			analysisPanelString += "\nPlayer " + (i) + " win rate: " + df.format(sumScores[i]*100.0/numGames) + "%";
		
		analysisPanelString += "\n\n";
		
		double finalScore = 0.0;
		
		String csvOutputString = DBGameInfo.getUniqueName(game) + ",";
		
		final Trial[] trials = allStoredTrials.toArray(new Trial[allStoredTrials.size()]);
		final RandomProviderState[] randomProviderStates = allStoredRNG.toArray(new RandomProviderState[allStoredRNG.size()]);

		// Specific Metric results
		for (int m = 0; m < metricsToEvaluate.size(); m++)
		{
			final Metric metric = metricsToEvaluate.get(m);
			
			try
			{
				report.getReportMessageFunctions().printMessageInAnalysisPanel(metric.name() + "\n");
			}
			catch(final Exception e)
			{
				// probably running from command line.
				System.out.print(metric.name() + "\n");
			}
			
			final Double score = metric.apply(game, evaluation, trials, randomProviderStates);
			if (score == null)
			{
				csvOutputString += "NULL,";
			}
			else
			{
				final double weight = weights.get(m).doubleValue();
				analysisPanelString += metric.name() + ": " + df.format(score) + " (weight: " + weight + ")\n";
				finalScore += score * weight;
				csvOutputString += score + ",";
			}
		}
		
		analysisPanelString += "Final Score: " + df.format(finalScore) + "\n\n";
				
		try
		{
			report.getReportMessageFunctions().printMessageInAnalysisPanel(analysisPanelString);	
		}
		catch (final Exception e)
		{
			// Probably running from command line
			System.out.println(analysisPanelString);
		}

		return csvOutputString.substring(0, csvOutputString.length()-1) + "\n";
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param args
	 */
	public static void main(final String[] args)
	{
		// Define options for arg parser
		final CommandLineArgParse argParse = 
			new CommandLineArgParse
			(
				true,
				"Evaluate all games in ludii using gameplay metrics."
			);
		
		argParse.addOption(new ArgOption()
				.withNames("--numTrials")
				.help("Number of trials to run for each game.")
				.withDefault(Integer.valueOf(10))
				.withNumVals(1)
				.withType(OptionTypes.Int));
		argParse.addOption(new ArgOption()
				.withNames("--maxTurns")
				.help("Turn limit.")
				.withDefault(Integer.valueOf(50))
				.withNumVals(1)
				.withType(OptionTypes.Int));
		argParse.addOption(new ArgOption()
				.withNames("--thinkTime")
				.help("Thinking time per move.")
				.withDefault(Double.valueOf(0.1))
				.withNumVals(1)
				.withType(OptionTypes.Double));
		argParse.addOption(new ArgOption()
				.withNames("--AIName")
				.help("Name of the Agent to use.")
				.withDefault("Ludii AI")
				.withNumVals(1)
				.withType(OptionTypes.String));
		argParse.addOption(new ArgOption()
				.withNames("--useDatabaseGames")
				.help("Use database games when available.")
				.withDefault(Boolean.valueOf(true))
				.withNumVals(1)
				.withType(OptionTypes.Boolean));
		
		if (!argParse.parseArguments(args))
			return;

		final int numberTrials = argParse.getValueInt("--numTrials");
		final int maxTurns = argParse.getValueInt("--maxTurns");
		final double thinkTime = argParse.getValueDouble("--thinkTime");
		final String AIName = argParse.getValueString("--AIName");
		final boolean useDatabaseGames = argParse.getValueBool("--useDatabaseGames");
		
		evaluateAllGames(null, numberTrials, maxTurns, thinkTime, AIName, useDatabaseGames);
	}
}
