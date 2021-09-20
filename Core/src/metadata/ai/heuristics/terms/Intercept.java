package metadata.ai.heuristics.terms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import annotations.Name;
import annotations.Opt;
import game.Game;
import game.types.play.RoleType;
import gnu.trove.list.array.TFloatArrayList;
import main.Constants;
import main.StringRoutines;
import main.collections.FVector;
import metadata.ai.heuristics.transformations.HeuristicTransformation;
import metadata.ai.misc.Pair;
import other.context.Context;

/**
 * Defines an intercept term for heuristic-based value functions, with one
 * weight per player
 *
 * @author Dennis Soemers
 */
public class Intercept extends HeuristicTerm
{
	
	//-------------------------------------------------------------------------
	
	/** Array of names specified for piece types */
	private RoleType[] players;
	
	/** 
	 * Array of weights as specified in metadata. Will be used to initialise
	 * a weight vector for a specific game when init() is called.
	 */
	private float[] gameAgnosticWeightsArray;
	
	/** Vector with weights for every player */
	private FVector playerWeightsVector = null;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * 
	 * @param transformation An optional transformation to be applied to any 
	 * raw heuristic score outputs.
	 * @param playerWeights Weights for different players. Players for which no
	 * weights are specified are given a weight of 0.0. Player names must be
	 * one of the following: "P1", "P2", ..., "P16".
	 * 
	 * @example (intercept playerWeights:{ (pair "P1" 1.0) (pair "P2" 0.5) })
	 */
	public Intercept
	(
		@Name @Opt final HeuristicTransformation transformation,
		@Name      final Pair[] playerWeights
	)
	{
		super(transformation, Float.valueOf(1.f));
		
		players = new RoleType[playerWeights.length];
		gameAgnosticWeightsArray = new float[playerWeights.length];
		
		for (int i = 0; i < playerWeights.length; ++i)
		{
			players[i] = RoleType.valueOf(playerWeights[i].key());
			gameAgnosticWeightsArray[i] = playerWeights[i].floatVal();
		}
	}
	
	@Override
	public HeuristicTerm copy()
	{
		return new Intercept(this);
	}
	
	/**
	 * Copy constructor (private, so not visible to grammar)
	 * @param other
	 */
	private Intercept(final Intercept other)
	{
		super(other.transformation, Float.valueOf(other.weight));
		players = Arrays.copyOf(other.players, other.players.length);
		gameAgnosticWeightsArray = Arrays.copyOf(other.gameAgnosticWeightsArray, other.gameAgnosticWeightsArray.length);
	}
	
	//-------------------------------------------------------------------------

	@Override
	public float computeValue(final Context context, final int player, final float absWeightThreshold)
	{
		return playerWeightsVector.get(player);
	}
	
	@Override
	public FVector computeStateFeatureVector(final Context context, final int player)
	{
		final FVector featureVector = new FVector(playerWeightsVector.dim());
		featureVector.set(player, 1.f);
		return featureVector;
	}
	
	@Override
	public FVector paramsVector()
	{
		return playerWeightsVector;
	}
	
	@Override
	public void init(final Game game)
	{
		// Compute vector of player weights
		playerWeightsVector = new FVector(game.players().count() + 1);
		
		for (int i = 0; i < players.length; ++i)
		{
			playerWeightsVector.set(players[i].owner(), gameAgnosticWeightsArray[i]); 
		}
	}
	
	@Override
	public int updateParams(final Game game, final FVector newParams, final int startIdx)
	{
		final int retVal = super.updateParams(game, newParams, startIdx);
		
		// Need to update the array of weights we were passed in constructor
		// in case we decide to write ourselves to a file
		final List<RoleType> roleTypes = new ArrayList<RoleType>();
		final TFloatArrayList nonZeroWeights = new TFloatArrayList();
		
		for (int i = 1; i < newParams.dim(); ++i)
		{
			if (newParams.get(i) != 0.f)
			{
				roleTypes.add(RoleType.roleForPlayerId(i));
				nonZeroWeights.add(newParams.get(i));
			}
		}
		
		players = roleTypes.toArray(new RoleType[roleTypes.size()]);
		gameAgnosticWeightsArray = nonZeroWeights.toArray();
		
		return retVal;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param game
	 * @return True if heuristic of this type could be applicable to given game
	 */
	public static boolean isApplicableToGame(final Game game)
	{
		return true;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		
		sb.append("(intercept");
		if (transformation != null)
			sb.append(" transformation:" + transformation.toString());
		if (weight != 1.f)
			System.err.println("Intercept heuristic does not support weight other than 1.f!");
		
		if (players.length >= 1)
		{
			sb.append(" playerWeights:{\n");
			
			for (int i = 0; i < players.length; ++i)
			{
				if (gameAgnosticWeightsArray[i] != 0.f)
					sb.append("        (pair " + StringRoutines.quote(players[i].name()) + " " + gameAgnosticWeightsArray[i] + ")\n");
			}
			
			sb.append("    }");
		}
		
		sb.append(")");
		
		return sb.toString();
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toStringThresholded(final float threshold)
	{
		boolean shouldPrint = false;
		boolean haveRelevantPlayers = false;
		final StringBuilder playerWeightsSb = new StringBuilder();
		
		if (players.length >= 1)
		{
			for (int i = 0; i < players.length; ++i)
			{
				if (Math.abs(weight * gameAgnosticWeightsArray[i]) >= threshold)
				{
					playerWeightsSb.append("        (pair " + StringRoutines.quote(players[i].name()) + " " + gameAgnosticWeightsArray[i] + ")\n");
					haveRelevantPlayers = true;
					shouldPrint = true;
				}
			}
		}
		
		if (shouldPrint)
		{
			final StringBuilder sb = new StringBuilder();
		
			sb.append("(intercept");
			if (transformation != null)
				sb.append(" transformation:" + transformation.toString());
			if (weight != 1.f)
				System.err.println("Intercept heuristic does not support weight other than 1.f!");
			
			if (haveRelevantPlayers)
			{
				sb.append(" pieceWeights:{\n");
				sb.append(playerWeightsSb);
				sb.append("    }");
			}
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
	public void merge(final HeuristicTerm term) 
	{
		final Intercept castTerm = (Intercept) term;
		for (int i = 0; i < players.length; i++)
			for (int j = 0; j < castTerm.players.length; j++)
				if (players[i].equals(castTerm.players[j]))
					gameAgnosticWeightsArray[i] = gameAgnosticWeightsArray[i] + castTerm.gameAgnosticWeightsArray[j] * (castTerm.weight()/weight());
	}
	
	@Override
	public void simplify()
	{
		if (Math.abs(weight() - 1.f) > Constants.EPSILON)
		{
			for (int i = 0; i < gameAgnosticWeightsArray.length; i++)
				gameAgnosticWeightsArray[i] *= weight();
	
			setWeight(1.f);
		}
	}
	
	@Override
	public float maxAbsWeight() 
	{
		float maxWeight = Math.abs(weight());
		for (final float f : gameAgnosticWeightsArray)
			maxWeight = Math.max(maxWeight, Math.abs(f));
		return maxWeight;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public String description() 
	{
		return "Intercept terms per player, for heuristic-based value functions.";
	}
	
	@Override
	public String toEnglishString(final Context context, final int playerIndex) 
	{
		return "";
	}
	
	//-------------------------------------------------------------------------
}
