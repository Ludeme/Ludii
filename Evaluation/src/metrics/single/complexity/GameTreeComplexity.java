package metrics.single.complexity;

import org.apache.commons.rng.RandomProviderState;

import game.Game;
import main.Constants;
import metrics.Evaluation;
import metrics.Metric;
import metrics.Utils;
import other.concept.Concept;
import other.context.Context;
import other.move.Move;
import other.trial.Trial;

/**
 * Estimate of the number of possible distinct play traces. 
 * https://www.pipmodern.com/post/complexity-state-space-game-tree
 * 
 * @author matthew.stephenson
 */
public class GameTreeComplexity extends Metric
{

	//-------------------------------------------------------------------------

	/**
	 * Constructor
	 */
	public GameTreeComplexity()
	{
		super
		(
			"Game Tree Complexity", 
			"Estimate of the number of possible distinct play traces. ", 
			0.0, 
			Constants.INFINITY,
			Concept.GameTreeComplexity
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
		double gameTreeComplexity = 0.0;
		for (int trialIndex = 0; trialIndex < trials.length; trialIndex++)
		{
			// Get trial and RNG information
			final Trial trial = trials[trialIndex];
			final RandomProviderState rngState = randomProviderStates[trialIndex];
			
			// Setup a new instance of the game
			final Context context = Utils.setupNewContext(game, rngState);
			
			double branchingFactor = 0.0;
			for (final Move m : trial.generateRealMovesList())
			{
				branchingFactor += context.game().moves(context).moves().size() / trial.generateRealMovesList().size();
				context.game().apply(context, m);
			}

			gameTreeComplexity += trial.generateRealMovesList().size() * Math.log10(branchingFactor);
		}

		return Double.valueOf(gameTreeComplexity / trials.length);
	}

}
