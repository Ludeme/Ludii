package metrics.quality;

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
 * Metric that measures average number of repeated positional states per game.
 * 
 * @author matthew.stephenson
 */
public class PositionalRepetition extends Metric
{

	//-------------------------------------------------------------------------

	/**
	 * Constructor
	 */
	public PositionalRepetition()
	{
		super
		(
			"Positional State Repetition", 
			"Average number of repeated positional states.", 
			"Core Ludii metric.", 
			MetricType.OUTCOMES,
			0.0, 
			-1,
			0.0,
			Concept.PositionalRepetition
		);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public double apply
	(
			final Game game,
			final String args, 
			final Trial[] trials,
			final RandomProviderState[] randomProviderStates
	)
	{
		if (trials.length == 0)
			return 0;
		
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
			
			trialStates.add(context.state().stateHash());
			trialStateCounts.add(1);
			
			for (int i = trial.numInitialPlacementMoves(); i < trial.numMoves(); i++)
			{
				context.game().apply(context, trial.getMove(i));
				
				final long currentState = context.state().stateHash();
				final int currentStateIndex = trialStates.indexOf(currentState);
				
				// If state was seen before
				if(currentStateIndex != -1) 
				{
					trialStateCounts.set(currentStateIndex, trialStateCounts.get(currentStateIndex) + 1);
				} 
				
				// If state is new
				else 
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
