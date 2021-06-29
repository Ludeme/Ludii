package metrics.single.stateEvaluation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.rng.RandomProviderState;

import game.Game;
import metrics.Metric;
import metrics.Utils;
import other.concept.Concept;
import other.context.Context;
import other.trial.Trial;

/**
 * Average number of times the expected winner changes.
 * 
 * @author matthew.stephenson
 */
public class LeadChange extends Metric
{

	//-------------------------------------------------------------------------

	/**
	 * Constructor
	 */
	public LeadChange()
	{
		super
		(
			"Lead Change", 
			"Average number of times the expected winner changes.", 
			0.0, 
			1.0,
			Concept.Timeouts
		);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public double apply
	(
			final Game game,
			final Trial[] trials,
			final RandomProviderState[] randomProviderStates
	)
	{
		double avgLeadChange = 0.0;
		for (int trialIndex = 0; trialIndex < trials.length; trialIndex++)
		{
			// Get trial and RNG information
			final Trial trial = trials[trialIndex];
			final RandomProviderState rngState = randomProviderStates[trialIndex];
			
			// Setup a new instance of the game
			final Context context = Utils.setupNewContext(game, rngState);
			
			// Count number of times the expected winner changed.
			double leadChange = 0;
			
			Set<Integer> pastCurrentLeaders = new HashSet<>();
			
			for (int i = trial.numInitialPlacementMoves(); i < trial.numMoves(); i++)
			{
				final Set<Integer> currentLeaders = new HashSet<>();
				final ArrayList<Double> allPlayerStateEvaluations = Utils.allPlayerStateEvaulations(context);
				final double highestStateEvaluation = Collections.max(allPlayerStateEvaluations);
				for (int j = 1; j < allPlayerStateEvaluations.size(); j++)
					if (allPlayerStateEvaluations.get(j) == highestStateEvaluation)
						currentLeaders.add(j);
				
				if (!pastCurrentLeaders.equals(currentLeaders))
					leadChange++;
				
				pastCurrentLeaders = currentLeaders;
				context.game().apply(context, trial.getMove(i));
			}
			
			avgLeadChange += leadChange / trial.numberRealMoves();
		}

		return avgLeadChange / trials.length;
	}

	//-------------------------------------------------------------------------

}
