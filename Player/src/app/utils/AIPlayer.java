package app.utils;

import java.util.ArrayList;
import java.util.List;

import app.PlayerApp;
import game.Game;
import main.grammar.Report;
import metrics.Evaluation;
import metrics.Metric;
import supplementary.experiments.EvalGamesThread;

/**
 * Utility to load AI players and launch the Evaluation dialog for the desktop player.
 * 
 * @author matthew and cambolbro
 */
public class AIPlayer
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * Evaluates a single specified game and option combination, based on the AI parameters passed in.
	 */
	public static void AIEvalution(final PlayerApp app, final Report report, final int numberTrials, final int maxTurns, final double thinkTime, final String AIName, final List<Metric> metricsToEvaluate, final ArrayList<Double> weights, final boolean useDatabaseGames)
	{
		final Evaluation evaluation = new Evaluation();
		final Game game = app.manager().ref().context().game();
		final List<String> options = app.manager().settingsManager().userSelections().selectedOptionStrings();
		
		if (options.size() > 0)
			app.addTextToAnalysisPanel("Analysing " + game.name() + " " + options + "\n\n");
		else
			app.addTextToAnalysisPanel("Analysing " + game.name() + "\n\n");

		final EvalGamesThread evalThread = 	EvalGamesThread.construct
											(
												evaluation, report, game, options, AIName, 
												numberTrials, thinkTime, maxTurns,
												metricsToEvaluate, weights, useDatabaseGames
											);
		evalThread.setDaemon(true);
		evalThread.start();
	}
	
	//-------------------------------------------------------------------------
	
}
