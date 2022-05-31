package metrics.single.stateRepetition;

import org.apache.commons.rng.RandomProviderState;

import game.Game;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.array.TLongArrayList;
import metrics.Evaluation;
import metrics.Metric;
import metrics.Utils;
import other.concept.Concept;
import other.context.Context;
import other.move.Move;
import other.trial.Trial;

/**
 * Percentage number of repeated positional states.
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
			"Positional Repetition", 
			"Percentage number of repeated positional states.", 
			0.0, 
			1.0,
			Concept.PositionalRepetition
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
			trialStates.add(context.state().stateHash());
			trialStateCounts.add(1);
			
			for (final Move m : trial.generateRealMovesList())
			{
				context.game().apply(context, m);
				
				final long currentState = context.state().stateHash();
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
