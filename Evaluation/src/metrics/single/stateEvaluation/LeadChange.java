package metrics.single.stateEvaluation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.rng.RandomProviderState;

import game.Game;
import metrics.Evaluation;
import metrics.Metric;
import metrics.ReplayTrial;
import metrics.Utils;
import other.concept.Concept;
import other.context.Context;
import other.move.Move;

/**
 * Percentage number of times the expected winner changes.
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
			"Percentage number of times the expected winner changes.", 
			0.0, 
			1.0,
			Concept.LeadChange
		);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public double apply
	(
			final Game game,
			final Evaluation evaluation,
			final ReplayTrial[] trials,
			final RandomProviderState[] randomProviderStates
	)
	{
		double avgLeadChange = 0.0;
		for (int trialIndex = 0; trialIndex < trials.length; trialIndex++)
		{
			// Get trial and RNG information
			final ReplayTrial trial = trials[trialIndex];
			final RandomProviderState rngState = randomProviderStates[trialIndex];
			
			// Setup a new instance of the game
			final Context context = Utils.setupNewContext(game, rngState);
			
			// Count number of times the expected winner changed.
			double leadChange = 0;
			
			Set<Integer> pastCurrentLeaders = new HashSet<>();
			
			for (final Move m : trial.fullMoves())
			{
				final Set<Integer> currentLeaders = new HashSet<>();
				final ArrayList<Double> allPlayerStateEvaluations = Utils.allPlayerStateEvaluations(evaluation, context);
				final double highestStateEvaluation = Collections.max(allPlayerStateEvaluations);
				for (int j = 1; j < allPlayerStateEvaluations.size(); j++)
					if (allPlayerStateEvaluations.get(j) == highestStateEvaluation)
						currentLeaders.add(j);
				
				if (!pastCurrentLeaders.equals(currentLeaders))
					leadChange++;
				
				pastCurrentLeaders = currentLeaders;
				context.game().apply(context, m);
			}
			
			avgLeadChange += leadChange / trial.fullMoves().size();
		}

		return avgLeadChange / trials.length;
	}

	//-------------------------------------------------------------------------

}
