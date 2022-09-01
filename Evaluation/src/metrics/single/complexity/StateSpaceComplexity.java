package metrics.single.complexity;

import org.apache.commons.rng.RandomProviderState;

import game.Game;
import main.Constants;
import metrics.Evaluation;
import metrics.Metric;
import other.concept.Concept;
import other.trial.Trial;

/**
 * Estimate of the total number of possible game board states.
 * https://www.pipmodern.com/post/complexity-state-space-game-tree
 * 
 * @author matthew.stephenson
 */
public class StateSpaceComplexity extends Metric
{

	//-------------------------------------------------------------------------

	/**
	 * Constructor
	 */
	public StateSpaceComplexity()
	{
		super
		(
			"State Space Complexity", 
			"Estimate of the total number of possible game board states.", 
			0.0, 
			Constants.INFINITY,
			Concept.StateTreeComplexity
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
		if (game.hasSubgames() || game.isSimultaneousMoveGame())
			return null;
		
		long maxStatePossibilites = game.numComponents() + 1;
		if (game.isStacking())
			maxStatePossibilites *= Constants.MAX_STACK_HEIGHT;
		else if (game.requiresCount())
			maxStatePossibilites *= game.maxCount();
		if (game.requiresLocalState())
			maxStatePossibilites *= game.maximalLocalStates();
		if (game.requiresRotation())
			maxStatePossibilites *= game.maximalRotationStates();
		if (game.requiresPieceValue())
			maxStatePossibilites *= game.maximalValue();
		if (game.hiddenInformation())
			maxStatePossibilites *= Math.pow(2, 7);

		return Double.valueOf(game.board().topology().getAllUsedGraphElements(game).size() * Math.log10(maxStatePossibilites));
	}

}
