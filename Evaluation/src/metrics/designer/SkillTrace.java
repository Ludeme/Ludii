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
import other.trial.Trial;
import utils.AIFactory;
import utils.experiments.ResultsSummary;

/**
 * Skill trace of the game.
 * NOTE. that this metric doesn't work with stored trials, and must instead generate new trials each time.
 * 
 * @author matthew.stephenson
 */
public class SkillTrace extends Metric
{
	
	private final int numMatches = 8;

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
		
		int weakIterationValue = 1;
		for (int matchCount = 1; matchCount <= numMatches; matchCount++)
		{
			final AI weakerAI = AIFactory.createAI("UCT");	// THIS SHOULD MAKE A UCT BASED ON weakIterationValue
			final AI strongerAI = AIFactory.createAI("UCT");	// THIS SHOULD MAKE A UCT BASED ON weakIterationValue*2
			
			// DENNIS CAN YOU MODIFY THIS
			final List<String> agentStrings = new ArrayList<>();
			agentStrings.add(weakerAI.name());
			agentStrings.add(strongerAI.name());
			final ResultsSummary results = new ResultsSummary(game, agentStrings);
			
			final double strongAIResult = 0.5;
			strongAIResults.add(strongAIResult);
			areaEstimate += Math.pow(Math.max(strongAIResult, 0.0), 2);
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
