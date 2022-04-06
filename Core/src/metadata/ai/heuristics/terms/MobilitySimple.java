package metadata.ai.heuristics.terms;

import annotations.Name;
import annotations.Opt;
import game.Game;
import main.collections.FVector;
import metadata.ai.heuristics.HeuristicUtil;
import metadata.ai.heuristics.transformations.HeuristicTransformation;
import other.context.Context;

/**
 * Defines a simple heuristic term that multiplies its weight by the number
 * of moves that a player has in a current game state. 
 * 
 * @remarks Always produces a score of $0$ for players who are not the current mover.
 * 
 * @author Dennis Soemers
 */
public class MobilitySimple extends HeuristicTerm
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
	 * @example (mobilitySimple weight:0.5)
	 */
	public MobilitySimple
	(
		@Name @Opt final HeuristicTransformation transformation,
		@Name @Opt final Float weight
	)
	{
		super(transformation, weight);
	}
	
	@Override
	public MobilitySimple copy()
	{
		return new MobilitySimple(this);
	}
	
	/**
	 * Copy constructor (private, so not visible to grammar)
	 * @param other
	 */
	private MobilitySimple(final MobilitySimple other)
	{
		super(other.transformation, Float.valueOf(other.weight));
	}
	
	//-------------------------------------------------------------------------

	@Override
	public float computeValue(final Context context, final int player, final float absWeightThreshold)
	{
		if (context.state().mover() == player)
			return context.game().moves(context).count();
		else
			return 0.f;
	}
	
	@Override
	public FVector computeStateFeatureVector(final Context context, final int player)
	{
		final FVector featureVector = new FVector(1);
		featureVector.set(0, computeValue(context, player, -1.f));
		return featureVector;
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
	
	/**
	 * @param game
	 * @return True if the heuristic of this type is sensible for the given game
	 * 	(must be applicable, but even some applicable heuristics may be considered
	 * 	to be not sensible).
	 */
	public static boolean isSensibleForGame(final Game game)
	{
		return isApplicableToGame(game);
	}
	
	@Override
	public boolean isApplicable(final Game game)
	{
		return isApplicableToGame(game);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		
		sb.append("(mobilitySimple");
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
		
			sb.append("(mobilitySimple");
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
	
	@Override
	public String description() 
	{
		return "Number of legal moves.";
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglishString(final Context context, final int playerIndex) 
	{
		final StringBuilder sb = new StringBuilder();

		if (weight > 0)
			sb.append("You should try to maximise the number of moves you can make");
		else
			sb.append("You should try to minimise the number of moves you can make");
		
		sb.append(" (" + HeuristicUtil.convertWeightToString(weight) + ")\n");
		
		return sb.toString();
	}
	
	//-------------------------------------------------------------------------

}
