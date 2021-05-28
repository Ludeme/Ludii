package supplementary;

import java.awt.EventQueue;

import analysis.Complexity;
import app.PlayerApp;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import search.pns.ProofNumberSearch;
import search.pns.ProofNumberSearch.ProofGoals;

public class EvalUtil
{
	public static void estimateGameTreeComplexity(final PlayerApp app, final boolean forceNoStateRepetitionRule)
	{
		if (!app.manager().ref().context().game().isDeductionPuzzle())
		{
			final double numSecs = 30.0;

			final Thread thread = new Thread(() ->
			{
				final TObjectDoubleHashMap<String> results = 
						Complexity.estimateGameTreeComplexity
						(
							app.manager().savedLudName(), 
							app.manager().settingsManager().userSelections(),
							numSecs,
							forceNoStateRepetitionRule
						);
				final double avgNumDecisions = results.get("Avg Num Decisions");
				final double avgTrialBranchingFactor = results.get("Avg Trial Branching Factor");
				final double power = results.get("Estimated Complexity Power");
				final int numTrials = (int) results.get("Num Trials");

				EventQueue.invokeLater(() ->
				{
					app.addTextToAnalysisPanel("Avg. number of decisions per trial = " + avgNumDecisions + ".\n");
					app.addTextToAnalysisPanel("Avg. branching factor per trial = " + avgTrialBranchingFactor + ".\n");
					app.addTextToAnalysisPanel("Estimated game-tree complexity ~= 10^" + (int) Math.ceil(power) + ".\n");
					app.addTextToAnalysisPanel("Statistics collected over " + numTrials + " random trials.\n");
					app.setTemporaryMessage("");
				});
			});
			app.selectAnalysisTab();
			app.setTemporaryMessage("Estimate Game Tree Complexity is starting. This will take a bit over " + (int) numSecs + " seconds.\n");
			thread.setDaemon(true);
			thread.start();
		}
		else
		{
			app.setVolatileMessage("Estimate Game Tree Complexity is disabled for deduction puzzles.\n");
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Attempts to prove that the current game state is either a win or a loss
	 * @param proofGoal
	 */
	public static void proveState(final PlayerApp app, final ProofGoals proofGoal)
	{
		final Thread thread = new Thread(() ->
		{
			final ProofNumberSearch pns = new ProofNumberSearch(proofGoal);

			if (!pns.supportsGame(app.manager().ref().context().game()))
			{
				System.err.println("PNS doesn't support this game!");
				return;
			}

			pns.initIfNeeded(app.manager().ref().context().game(), app.manager().ref().context().state().mover());

			pns.selectAction
			(
				app.manager().ref().context().game(), 
				app.manager().ref().context(), 
				1.0, 
				-1, 
				-1
			);
		});
		
		thread.setDaemon(true);
		thread.start();
	}
	
	//-------------------------------------------------------------------------

	public static void estimateGameLength(final PlayerApp app)
	{
		if (!app.manager().ref().context().game().isDeductionPuzzle())
		{
			final double numSecs = 30.0;

			final Thread thread = new Thread(() ->
			{
				final TObjectDoubleHashMap<String> results = Complexity.estimateGameLength(app.manager().ref().context().game(), numSecs);
				final double avgNumDecisions = results.get("Avg Num Decisions");
				final double avgNumPlayerSwitches = results.get("Avg Num Player Switches");
				final int numTrials = (int) results.get("Num Trials");

				EventQueue.invokeLater(() ->
				{
					app.addTextToAnalysisPanel("Avg. number of decisions per trial = " + avgNumDecisions + ".\n");
					app.addTextToAnalysisPanel("Avg. number of player switches per trial = " + avgNumPlayerSwitches + ".\n");
					app.addTextToAnalysisPanel("Statistics collected over " + numTrials + " random trials.\n");
					app.setTemporaryMessage("");
				});
			});
			app.selectAnalysisTab();
			app.setTemporaryMessage("Estimate Game Length is starting. This will take a bit over " + (int) numSecs + " seconds.\n");
			thread.setDaemon(true);
			thread.start();
		}
		else
		{
			app.setVolatileMessage("Estimate Game Length is disabled for deduction puzzles.\n");
		}
	}
	
	//-------------------------------------------------------------------------

	public static void estimateBranchingFactor(final PlayerApp app)
	{
		if (!app.manager().ref().context().game().isDeductionPuzzle())
		{
			final double numSecs = 30.0;

			final Thread thread = new Thread(() ->
			{
				final TObjectDoubleHashMap<String> results = 
						Complexity.estimateBranchingFactor
						(
							app.manager().savedLudName(), 
							app.manager().settingsManager().userSelections(),
							numSecs
						);
				final double avgTrialBranchingFactor = results.get("Avg Trial Branching Factor");
				final double avgStateBranchingFactor = results.get("Avg State Branching Factor");
				final int numTrials = (int) results.get("Num Trials");

				EventQueue.invokeLater(() ->
				{
					app.addTextToAnalysisPanel("Avg. branching factor per trial = " + avgTrialBranchingFactor + ".\n");
					app.addTextToAnalysisPanel("Avg. branching factor per state = " + avgStateBranchingFactor + ".\n");
					app.addTextToAnalysisPanel("Statistics collected over " + numTrials + " random trials.\n");
					app.setTemporaryMessage("");
				});
			});
			app.selectAnalysisTab();
			app.setTemporaryMessage("Estimate Branching Factor is starting. This will take a bit over " + (int) numSecs + " seconds.\n");
			thread.setDaemon(true);
			thread.start();
		}
		else
		{
			app.setVolatileMessage("Estimate Branching Factor is disabled for deduction puzzles.\n");
		}
	}

	//-------------------------------------------------------------------------

}
