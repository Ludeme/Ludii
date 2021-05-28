package supplementary.experiments;

import java.util.ArrayList;
import java.util.List;

import metrics.Metric;
import supplementary.experiments.eval.EvalGames;

/**
 * Thread in which various metrics of a game can be evaluated
 * from AI vs. AI play.
 * 
 * @author Dennis Soemers and Matthew.Stephenson
 */
public class EvalGamesThread extends Thread
{
	/** Our runnable */
	protected final EvalGamesThreadRunnable runnable;

	//-------------------------------------------------------------------------

	/**
	 * @param gameName
	 * @param gameOptions
	 * @param AIName 
	 * @param numGames 
	 * @param thinkingTime 
	 * @param maxNumTurns 
	 * @return Constructs a thread for games to be evaluated in
	 */
	public static EvalGamesThread construct
	(
		final String gameName,
		final List<String> gameOptions,
		final String AIName,
		final int numGames,
		final double thinkingTime,
		final int maxNumTurns, 
		final List<Metric> metricsToEvaluate, 
		final ArrayList<Double> weights,
		final boolean useDatabaseGames
	)
	{
		final EvalGamesThreadRunnable runnable = 
			new EvalGamesThreadRunnable
			(
				gameName,
				gameOptions,
				AIName,
				numGames,
				thinkingTime,
				maxNumTurns,
				metricsToEvaluate,
				weights,
				useDatabaseGames
			);

		return new EvalGamesThread(runnable);
	}

	/**
	 * Constructor
	 * @param runnable
	 */
	protected EvalGamesThread(final EvalGamesThreadRunnable runnable)
	{
		super(runnable);
		this.runnable = runnable;
	}

	//-------------------------------------------------------------------------

	/**
	 * Runnable class for Eval AIs Thread
	 * 
	 * @author Dennis Soemers
	 */
	private static class EvalGamesThreadRunnable implements Runnable
	{

		//---------------------------------------------------------------------

		/** The game we want to evaluate */
		protected final String gameName;
		
		final List<String> gameOptions;
		
		final int maxNumTurns;
		
		/** AI players */
		protected final String AIName;
		
		/** Number of games to run for evaluation */
		protected final int numGames;
		
		/** Thinking time per move (in seconds) */
		protected final double thinkingTime;
		
		/** The metrics we want to evaluate */
		protected final List<Metric> metricsToEvaluate;
		
		/** the weights for all metrics (between -1 and 1) */
		protected final ArrayList<Double> weights;
		
		/** Use saved trials from the database if available. */
		protected boolean useDatabaseGames;
		
		//protected final List<AI> aiPlayers;

		//---------------------------------------------------------------------

		/**
		 * Constructor 
		 * @param gameName
		 * @param gameOptions 
		 * @param AIName
		 * @param numGames 
		 * @param thinkingTime 
		 * @param maxNumTurns 
		 * @param weights
		 * @param metricsToEvaluate
		 */
		public EvalGamesThreadRunnable
		(
			final String gameName,
			final List<String> gameOptions,
			final String AIName,
			final int numGames,
			final double thinkingTime,
			final int maxNumTurns, 
			final List<Metric> metricsToEvaluate, 
			final ArrayList<Double> weights,
			final boolean useDatabaseGames
		)
		{
			this.gameName = gameName;
			this.gameOptions = gameOptions;
			this.maxNumTurns = maxNumTurns;
			this.AIName = AIName;
			this.numGames = numGames;
			this.thinkingTime = thinkingTime;
			this.metricsToEvaluate = metricsToEvaluate;
			this.weights = weights;
			this.useDatabaseGames = useDatabaseGames;
		}

		//---------------------------------------------------------------------

		@Override
		public void run()
		{
			EvalGames.evaluateGame(gameName, gameOptions, AIName, numGames, thinkingTime, maxNumTurns, metricsToEvaluate, weights, useDatabaseGames);
		}
	}

	//-------------------------------------------------------------------------

}
