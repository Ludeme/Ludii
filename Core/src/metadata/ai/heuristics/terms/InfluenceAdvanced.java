package metadata.ai.heuristics.terms;

import annotations.Name;
import annotations.Opt;
import game.Game;
import game.types.state.GameType;
import gnu.trove.set.hash.TIntHashSet;
import main.collections.FVector;
import main.collections.FastArrayList;
import metadata.ai.heuristics.HeuristicUtil;
import metadata.ai.heuristics.transformations.HeuristicTransformation;
import other.context.Context;
import other.context.TempContext;
import other.move.Move;

/**
 * Defines a heuristic term that multiplies its weight by the number
 * of moves with distinct "to" positions that a player has in a current game state,
 * divided by the number of playable positions that exist in the game.
 * In comparison to Influence, this is a more advanced version that will also attempt
 * to gain non-zero estimates of the influence of players other than the current
 * player to move.
 * 
 * @author Dennis Soemers
 */
public class InfluenceAdvanced extends HeuristicTerm
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
	 * @example (influenceAdvanced weight:0.5)
	 */
	public InfluenceAdvanced
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
		return new InfluenceAdvanced(this);
	}
	
	/**
	 * Copy constructor (private, so not visible to grammar)
	 * @param other
	 */
	private InfluenceAdvanced(final InfluenceAdvanced other)
	{
		super(other.transformation, Float.valueOf(other.weight));
	}
	
	//-------------------------------------------------------------------------

	@Override
	public float computeValue(final Context context, final int player, final float absWeightThreshold)
	{
		final Context computeContext;
		
		if (context.state().mover() == player)
		{
			computeContext = context;
		}
		else
		{
			computeContext = new TempContext(context);
			computeContext.state().setMover(player);
			computeContext.trial().clearLegalMoves();
		}
		
		final FastArrayList<Move> legalMoves = computeContext.game().moves(computeContext).moves();
		final TIntHashSet toPositions = new TIntHashSet();
		for (final Move move : legalMoves)
		{
			final int to = move.to();
			if (to >= 0)
				toPositions.add(to);
		}
		
		return ((float) toPositions.size()) / computeContext.game().equipment().totalDefaultSites();
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
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		
		sb.append("(influenceAdvanced");
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
		
			sb.append("(influenceAdvanced");
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
	
	@Override
	public String description() 
	{
		return "Number of legal moves with distinct destination positions.";
	}
	
	@Override
	public String toEnglishString(final Context context, final int playerIndex) 
	{
		final StringBuilder sb = new StringBuilder();

		if (weight > 0)
			sb.append("You should try to maximise the number of spaces you can move to");
		else
			sb.append("You should try to minimise the number of spaces you can move to");
		
		sb.append(" (" + HeuristicUtil.convertWeightToString(weight) + ")\n");
		
		return sb.toString();
	}
	
	//-------------------------------------------------------------------------

}
