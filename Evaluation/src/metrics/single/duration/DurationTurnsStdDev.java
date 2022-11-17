package metrics.single.duration;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.rng.RandomProviderState;

import game.Game;
import main.Constants;
import metrics.Evaluation;
import metrics.Metric;
import other.concept.Concept;
import other.trial.Trial;

/**
 * Number of turns in a game.
 * 
 * @author matthew.stephenson
 */
public class DurationTurnsStdDev extends Metric
{

	//-------------------------------------------------------------------------

	/**
	 * Constructor
	 */
	public DurationTurnsStdDev()
	{
		super
		(
			"Duration Turns Std Dev", 
			"Number or turns in a game (std dev).", 
			0.0, 
			Constants.INFINITY,
			Concept.DurationTurns
		);
	}
	
	//-------------------------------------------------------------------------
	
	private static double calculateSD(final List<Integer> turnTally)
    {
        double sum = 0.0;
        double standardDeviation = 0.0;
        final int length = turnTally.size();

        for(final int num : turnTally)
            sum += num;

        final double mean = sum/length;

        for(final int num: turnTally)
            standardDeviation += Math.pow(num - mean, 2);

        return Math.sqrt(standardDeviation/length);
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
		// Count the number of turns.
		final List<Integer> turnTally = new ArrayList<>();
		for (final Trial trial : trials)
			turnTally.add(Integer.valueOf(trial.numTurns()));
		
		return Double.valueOf(calculateSD(turnTally));
	}

	//-------------------------------------------------------------------------

}
