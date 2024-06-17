package metrics.single.stateEvaluation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.rng.RandomProviderState;

import game.Game;
import metrics.Evaluation;
import metrics.Metric;
import metrics.Utils;
import other.concept.Concept;
import other.context.Context;
import other.move.Move;
import other.trial.Trial;

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
	public Double apply
	(
			final Game game,
			final Evaluation evaluation,
			final Trial[] trials,
			final RandomProviderState[] randomProviderStates
	)
	{
		// Cannot perform move/state evaluation for matches.
		if (game.hasSubgames() || game.isSimultaneousMoveGame())
			return null;
		
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
			
			for (final Move m : trial.generateRealMovesList())
			{
				final Set<Integer> currentLeaders = new HashSet<>();
				final ArrayList<Double> allPlayerStateEvaluations = Utils.allPlayerStateEvaluations(evaluation, context);
				final double highestStateEvaluation = Collections.max(allPlayerStateEvaluations).doubleValue();
				for (int j = 1; j < allPlayerStateEvaluations.size(); j++)
					if (allPlayerStateEvaluations.get(j).doubleValue() == highestStateEvaluation)
						currentLeaders.add(Integer.valueOf(j));
				
				if (!pastCurrentLeaders.equals(currentLeaders))
					leadChange++;
				
				pastCurrentLeaders = currentLeaders;
				context.game().apply(context, m);
			}
			
			avgLeadChange += leadChange / trial.generateRealMovesList().size();
		}

		return Double.valueOf(avgLeadChange / trials.length);
	}

	//-------------------------------------------------------------------------
	
	@Override
	public void startNewTrial(final Context context, final Trial fullTrial)
	{
		System.err.println("Incrementally computing metric not yet implemented for LeadChange.");
	}
	
	@Override
	public void observeNextState(final Context context)
	{
		System.err.println("Incrementally computing metric not yet implemented for LeadChange.");
	}
	
	@Override
	public void observeFinalState(final Context context)
	{
		System.err.println("Incrementally computing metric not yet implemented for LeadChange.");
	}
	
	@Override
	public double finaliseMetric(final Game game, final int numTrials)
	{
		System.err.println("Incrementally computing metric not yet implemented for LeadChange.");
		return Double.NaN;
	}

	//-------------------------------------------------------------------------

}
