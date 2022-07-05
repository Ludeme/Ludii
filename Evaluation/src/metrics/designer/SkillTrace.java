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
 * NOTE. that this metric doesn't work with stored trials, and must instead generate new trials each time.
 * 
 * @author matthew.stephenson
 */
public class SkillTrace extends Metric
{
	
	private final int numMatches = 8;
	
	private final int numTrialsPerMatch = 10;

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
		
		final List<AI> ais = new ArrayList<AI>(game.players().count() + 1);
		ais.add(null);
		for (int p = 1; p <= game.players().count(); ++p)
		{
			ais.add(MCTS.createUCT());
		}
		
		final Trial trial = new Trial(game);
		final Context context = new Context(game, trial);
		
		int weakIterationValue = 1;
		for (int matchCount = 1; matchCount <= numMatches; matchCount++)
		{
			double strongAIAvgResult = 0.0;
			int strongAgentIdx = 1;
			for (int i = 0; i < numTrialsPerMatch; ++i)
			{
				game.start(context);
				
				for (int p = 1; p <= game.players().count(); ++p)
				{
					ais.get(p).initAI(game, p);
				}

				final Model model = context.model();

				while (!trial.over())
				{
					final int mover = context.state().mover();
					final int numIterations;
					if (mover == strongAgentIdx)
						numIterations = weakIterationValue * 2;
					else
						numIterations = weakIterationValue;
					
					model.startNewStep(context, ais, Double.MAX_VALUE, numIterations, Integer.MAX_VALUE, 0.0);
				}
				
				// Record the utility of the strong agent
				strongAIAvgResult += RankUtils.agentUtilities(context)[strongAgentIdx];
				
				// Change which player is controlled by the strong agent
				++strongAgentIdx;
				if (strongAgentIdx > game.players().count())
					strongAgentIdx = 1;
			}
			
			strongAIAvgResult /= numTrialsPerMatch;
			strongAIResults.add(strongAIAvgResult);
			areaEstimate += Math.pow(Math.max(strongAIAvgResult, 0.0), 2);
			weakIterationValue *= 2;
		}
		
		// Predcit next step y value.
		final double[] xAxis = IntStream.range(0, strongAIResults.size()).asDoubleStream().toArray();
		final double[] yAxis = strongAIResults.stream().mapToDouble(Double::doubleValue).toArray();
		final LinearRegression linearRegression = new LinearRegression(xAxis, yAxis);
		double yValueNextStep = linearRegression.predict(numMatches+1);
		yValueNextStep = Math.max(Math.min(yValueNextStep, 1.0), 0.0);
		
		final double skillTrace = yValueNextStep + (1-yValueNextStep)*areaEstimate;
		
		return skillTrace;
	}

	//-------------------------------------------------------------------------

}
