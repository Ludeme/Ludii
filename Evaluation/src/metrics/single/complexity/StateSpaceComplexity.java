package metrics.single.complexity;

import org.apache.commons.rng.RandomProviderState;

import game.Game;
import main.Constants;
import metrics.Metric;
import other.trial.Trial;

/**
 * State Space Complexity Upper Bound.
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
			"State Space Complexity Upper Bound.", 
			0.0, 
			-1,
			null
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

		return game.board().topology().getAllUsedGraphElements(game).size() * Math.log10(maxStatePossibilites);
	}

}
