package metadata.ai.heuristics.terms;

import annotations.Name;
import annotations.Opt;
import game.Game;
import main.collections.FVector;
import metadata.ai.heuristics.transformations.HeuristicTransformation;
import other.context.Context;

/**
 * Defines a heuristic term that adds its weight only for the player
 * whose turn it is in any given game state.
 *
 * @author Dennis Soemers
 */
public class CurrentMoverHeuristic extends HeuristicTerm
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * 
	 * @param transformation An optional transformation to be applied to any 
	 * raw heuristic score outputs.
	 * @param weight The weight for this term in a linear combination of multiple terms.
	 * If not specified, a default weight of $1.0$ is used.
	 * 
	 * @example (currentMoverHeuristic weight:1.0)
	 */
	public CurrentMoverHeuristic
	(
		@Name @Opt final HeuristicTransformation transformation,
		@Name @Opt final Float weight
	)
	{
		super(transformation, weight);
	}
	
	@Override
	public HeuristicTerm copy()
	{
		return new CurrentMoverHeuristic(this);
	}
	
	/**
	 * Copy constructor (private, so not visible to grammar)
	 * @param other
	 */
	private CurrentMoverHeuristic(final CurrentMoverHeuristic other)
	{
		super(other.transformation, Float.valueOf(other.weight));
	}
	
	//-------------------------------------------------------------------------

	@Override
	public float computeValue(final Context context, final int player, final float absWeightThreshold)
	{
		if (context.state().mover() == player)
			return 1.f;
		else
			return 0.f;
	}
	
	@Override
	public FVector computeStateFeatureVector(final Context context, final int player)
	{
		if (context.state().mover() == player)
			return FVector.ones(1);
		else
			return FVector.zeros(1);
	}
	
	@Override
	public FVector paramsVector()
	{
		return null;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param game
	 * @return True if heuristic of this type could be applicable to given game
	 */
	public static boolean isApplicableToGame(final Game game)
	{
		return game.isAlternatingMoveGame();
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		
		sb.append("(currentMoverHeuristic");
		if (transformation != null)
			sb.append(" transformation:" + transformation.toString());
		if (weight != 1.f)
			sb.append(" weight:" + weight);
		sb.append(")");
		
		return sb.toString();
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toStringThresholded(final float threshold)
	{
		boolean shouldPrint = false;
		
		if (Math.abs(weight) >= threshold)
		{
			// No manually specified weights, so they will all default to 1.0,
			// and we have a large enough term-wide weight
			shouldPrint = true;
		}
		
		if (shouldPrint)
		{
			final StringBuilder sb = new StringBuilder();
		
			sb.append("(currentMoverHeuristic");
			if (transformation != null)
				sb.append(" transformation:" + transformation.toString());
			if (weight != 1.f)
				sb.append(" weight:" + weight);
			sb.append(")");
			
			return sb.toString();
		}
		else
		{
			return null;
		}
	}
	
	//-------------------------------------------------------------------------

}
