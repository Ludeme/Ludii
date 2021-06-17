package metrics.single.stateRepetition;

import org.apache.commons.rng.RandomProviderState;

import game.Game;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.array.TLongArrayList;
import metrics.Metric;
import metrics.Utils;
import other.concept.Concept;
import other.context.Context;
import other.trial.Trial;

/**
 * Average number of repeated situational states.
 * 
 * @author matthew.stephenson
 */
public class SituationalRepetition extends Metric
{

	//-------------------------------------------------------------------------

	/**
	 * Constructor
	 */
	public SituationalRepetition()
	{
		super
		(
			"Situational Repetition", 
			"Average number of repeated situational states.", 
			"Core Ludii metric.", 
			MetricType.OUTCOMES,
			0.0, 
			-1,
			0.0,
			Concept.SituationalRepetition
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
		double avgStateRepeats = 0;
		for (int trialIndex = 0; trialIndex < trials.length; trialIndex++)
		{
			// Get trial and RNG information
			final Trial trial = trials[trialIndex];
			final RandomProviderState rngState = randomProviderStates[trialIndex];
			
			// Setup a new instance of the game
			final Context context = Utils.setupNewContext(game, rngState);
			
			// Record the number of possible options for each move.
			final TLongArrayList trialStates = new TLongArrayList();
			final TIntArrayList trialStateCounts = new TIntArrayList();
			
			// Record the initial state.
			trialStates.add(context.state().fullHash());
			trialStateCounts.add(1);
			
			for (int i = trial.numInitialPlacementMoves(); i < trial.numMoves(); i++)
			{
				context.game().apply(context, trial.getMove(i));
				
				final long currentState = context.state().fullHash();
				final int currentStateIndex = trialStates.indexOf(currentState);
				
				if(currentStateIndex != -1) 		// If state was seen before
				{
					trialStateCounts.set(currentStateIndex, trialStateCounts.get(currentStateIndex) + 1);
				} 
				else 								// If state is new
				{
					trialStates.add(currentState);
					trialStateCounts.add(1);
				}
			}
			
			final int numUniqueStates = trialStates.size();
			final int numTotalStates = trialStateCounts.sum();
			avgStateRepeats += 1.0 - (numUniqueStates / numTotalStates);
		}

		return avgStateRepeats / trials.length;
	}

	//-------------------------------------------------------------------------

}
