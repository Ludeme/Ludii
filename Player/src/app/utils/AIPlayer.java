package app.utils;

import java.util.ArrayList;
import java.util.List;

import app.PlayerApp;
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
	public static void AIEvalution(final PlayerApp app, final int numberTrials, final int maxTurns, final double thinkTime, final String AIName, final List<Metric> metricsToEvaluate, final ArrayList<Double> weights, final boolean useDatabaseGames)
	{
		final String gameName = app.manager().ref().context().game().name();
		final List<String> options = app.manager().settingsManager().userSelections().selectedOptionStrings();
		
		app.addTextToAnalysisPanel("Analysing " + gameName + options + "\n");

		final EvalGamesThread evalThread = 	EvalGamesThread.construct
											(
												gameName, 
												options,
												AIName, 
												numberTrials, thinkTime, maxTurns,
												metricsToEvaluate, weights, useDatabaseGames
											);
		evalThread.setDaemon(true);
		evalThread.start();
	}
	
	//-------------------------------------------------------------------------
	
}
