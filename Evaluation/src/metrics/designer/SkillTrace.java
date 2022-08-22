package metrics.designer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.apache.commons.rng.RandomProviderState;

import game.Game;
import main.math.LinearRegression;
import metrics.Evaluation;
import metrics.Metric;
import other.AI;
import other.RankUtils;
import other.context.Context;
import other.model.Model;
import other.trial.Trial;
import search.mcts.MCTS;

/**
 * Skill trace of the game.
 * NOTE. This metric doesn't work with stored trials, and must instead generate new trials each time.
 * NOTE. Only works games that are supported by UCT
 * 
 * @author matthew.stephenson and Dennis Soemers
 */
public class SkillTrace extends Metric
{
	
	// Number of matches (iteration count doubles each time)
	private final int numMatches = 8;
	
	// Number of trials per match
	private final int numTrialsPerMatch = 10;
	
	// A hard time limit in seconds, after which any future trials are aborted
	private final int hardTimeLimit = 300;

	//-------------------------------------------------------------------------

	/**
	 * Constructor
	 */
	public SkillTrace()
	{
		super
		(
			"Skill trace", 
			"Skill trace of the game.", 
			0.0, 
			1.0,
			null
		);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public Double apply
	(
			final Game game,
			final Evaluation evaluation,
			final Trial[] trials,
			final RandomProviderState[] randomProviderStates
	)
	{
		final List<Double> strongAIResults = new ArrayList<>();
		double areaEstimate = 0.0;
		final long startTime = System.currentTimeMillis();
		
		final List<AI> ais = new ArrayList<AI>(game.players().count() + 1);
		ais.add(null);
		for (int p = 1; p <= game.players().count(); ++p)
		{
			ais.add(MCTS.createUCT());
		}
		
		final Trial trial = new Trial(game);
		final Context context = new Context(game, trial);
		
		int weakIterationValue = 2;
		int matchCount = 0;
		for (/**/; matchCount < numMatches; matchCount++)
		{
			double strongAIAvgResult = 0.0;
			int strongAgentIdx = 1;
			for (int i = 0; i < numTrialsPerMatch; ++i)
			{
				game.start(context);
				
				for (int p = 1; p <= game.players().count(); ++p)
					ais.get(p).initAI(game, p);

				final Model model = context.model();

				while (!trial.over())
				{
					final int mover = context.state().playerToAgent(context.state().mover());
					final int numIterations;
					if (mover == strongAgentIdx)
						numIterations = weakIterationValue * 2;
					else
						numIterations = weakIterationValue;
					
					model.startNewStep(context, ais, -1.0, numIterations * game.moves(context).count(), -1, 0.0);
				}
				
				// Record the utility of the strong agent
				strongAIAvgResult += RankUtils.agentUtilities(context)[strongAgentIdx];

				// Change which player is controlled by the strong agent
				++strongAgentIdx;
				if (strongAgentIdx > game.players().count())
					strongAgentIdx = 1;
				
				// Check the current time, and if we have elapsed the limit then abort.
				if (System.currentTimeMillis() > (startTime + hardTimeLimit*1000))
					break;
			}
			
			// If we didn't finish all trials in time, then ignore the match results
			if (System.currentTimeMillis() > (startTime + hardTimeLimit*1000))
			{
				System.out.println("Aborting after " + String.valueOf(matchCount) + " matches.");
				break;
			}
			
			strongAIAvgResult /= numTrialsPerMatch;
			strongAIResults.add(strongAIAvgResult);
			areaEstimate += Math.max(strongAIAvgResult, 0.0);
			weakIterationValue *= 2;
			
			// Print match results in console
			System.out.println("-----");
			System.out.println("Match Index:" + (matchCount+1));
			System.out.println("Strong AI result:" + strongAIAvgResult);
		}
		
		// Predict next step y value.
		final double[] xAxis = IntStream.range(0, strongAIResults.size()).asDoubleStream().toArray();
		final double[] yAxis = strongAIResults.stream().mapToDouble(Double::doubleValue).toArray();
		final LinearRegression linearRegression = new LinearRegression(xAxis, yAxis);
		double yValueNextStep = linearRegression.predict(numMatches+1);
		yValueNextStep = Math.max(Math.min(yValueNextStep, 1.0), 0.0);
		
		// No matches were able to be completed within the time limit.
		if (matchCount == 0)
			return 0.0;
		
		final double skillTrace = yValueNextStep + (1-yValueNextStep)*(areaEstimate/matchCount);
		
		return skillTrace;
	}

	//-------------------------------------------------------------------------

}
