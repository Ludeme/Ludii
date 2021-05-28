package metadata.ai.heuristics.terms;

import annotations.Name;
import annotations.Opt;
import game.Game;
import game.types.state.GameType;
import gnu.trove.set.hash.TIntHashSet;
import main.collections.FVector;
import main.collections.FastArrayList;
import metadata.ai.heuristics.transformations.HeuristicTransformation;
import other.context.Context;
import other.move.Move;

/**
 * Defines a heuristic term that multiplies its weight by the number
 * of moves with distinct "to" positions that a player has in a current game state,
 * divided by the number of playable positions that exist in the game.
 * 
 * @remarks Always produces a score of $0$ for players who are not the current mover.
 * 
 * @author Dennis Soemers
 */
public class Influence extends HeuristicTerm
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
	 * @example (influence weight:0.5)
	 */
	public Influence
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
		return new Influence(this);
	}
	
	/**
	 * Copy constructor (private, so not visible to grammar)
	 * @param other
	 */
	private Influence(final Influence other)
	{
		super(other.transformation, Float.valueOf(other.weight));
	}
	
	//-------------------------------------------------------------------------

	@Override
	public float computeValue(final Context context, final int player, final float absWeightThreshold)
	{
		if (context.state().mover() == player)
		{
			final FastArrayList<Move> moves = context.game().moves(context).moves();
			final TIntHashSet toPositions = new TIntHashSet();
			for (final Move move : moves)
			{
				final int to = move.to();
				if (to >= 0)
					toPositions.add(to);
			}
			return ((float) toPositions.size()) / context.game().equipment().totalDefaultSites();
		}
		else
		{
			return 0.f;
		}
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
		// Note: we only allow this heuristic in games that also use from-positions, because
		// otherwise the number of moves with distinct to-positions is almost always meaningless
		// (with some exceptions, like if moves have same to-position but different consequents)
		return game.isAlternatingMoveGame() && ((game.gameFlags() & GameType.UsesFromPositions) != 0L);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		
		sb.append("(influence");
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
		
			sb.append("(influence");
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
