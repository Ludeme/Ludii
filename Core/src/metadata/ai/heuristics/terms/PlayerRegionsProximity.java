package metadata.ai.heuristics.terms;

import java.util.Arrays;
import java.util.List;

import annotations.Name;
import annotations.Opt;
import game.Game;
import game.equipment.other.Regions;
import gnu.trove.list.array.TIntArrayList;
import main.Constants;
import main.StringRoutines;
import main.collections.FVector;
import metadata.ai.heuristics.HeuristicUtil;
import metadata.ai.heuristics.transformations.HeuristicTransformation;
import metadata.ai.misc.Pair;
import other.context.Context;
import other.location.Location;
import other.state.owned.Owned;

/**
 * Defines a heuristic term based on the proximity of pieces to the regions
 * owned by a particular player.
 * 
 * @author Dennis Soemers
 */
public class PlayerRegionsProximity extends HeuristicTerm
{
	
	//-------------------------------------------------------------------------
	
	/** Array of names specified for piece types. */
	private String[] pieceWeightNames;
	
	/** 
	 * Array of weights as specified in metadata. Will be used to initialise
	 * a weight vector for a specific game when init() is called.
	 */
	private float[] gameAgnosticWeightsArray;
	
	/** Vector with weights for every piece type */
	private FVector pieceWeights = null;
	
	/** The maximum distance that exists in our Centres distance table */
	private int maxDistance = -1;
	
	/** Player for which we use the regions */
	private final int regionPlayer;
	
	/** Regions across which to compute best proximity */
	private int[] regionIndices = null;

	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * 
	 * @param transformation An optional transformation to be applied to any 
	 * raw heuristic score outputs.
	 * @param weight The weight for this term in a linear combination of multiple terms.
	 * If not specified, a default weight of $1.0$ is used.
	 * @param player The player whose owned regions we compute proximity to.
	 * @param pieceWeights Weights for different piece types. If no piece weights are
	 * specified at all, all piece types are given an equal weight of $1.0$. If piece
	 * weights are only specified for some piece types, all other piece types get a
	 * weight of $0$.
	 * 
	 * @example (playerRegionsProximity player:2)
	 */
	public PlayerRegionsProximity
	(
		@Name @Opt final HeuristicTransformation transformation,
		@Name @Opt final Float weight,
		@Name final Integer player,
		@Name @Opt final Pair[] pieceWeights
	)
	{
		super(transformation, weight);
		
		regionPlayer = player.intValue();
		
		if (pieceWeights == null)
		{
			// We want a weight of 1.0 for everything
			pieceWeightNames = new String[]{""};
			gameAgnosticWeightsArray = new float[]{1.f};
		}
		else
		{
			pieceWeightNames = new String[pieceWeights.length];
			gameAgnosticWeightsArray = new float[pieceWeights.length];
			
			for (int i = 0; i < pieceWeights.length; ++i)
			{
				pieceWeightNames[i] = pieceWeights[i].key();
				gameAgnosticWeightsArray[i] = pieceWeights[i].floatVal();
			}
		}
	}
	
	@Override
	public HeuristicTerm copy()
	{
		return new PlayerRegionsProximity(this);
	}
	
	/**
	 * Copy constructor (private, so not visible to grammar)
	 * @param other
	 */
	private PlayerRegionsProximity(final PlayerRegionsProximity other)
	{
		super(other.transformation, Float.valueOf(other.weight));
		pieceWeightNames = Arrays.copyOf(other.pieceWeightNames, other.pieceWeightNames.length);
		gameAgnosticWeightsArray = Arrays.copyOf(other.gameAgnosticWeightsArray, other.gameAgnosticWeightsArray.length);
		regionPlayer = other.regionPlayer;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public float computeValue(final Context context, final int player, final float absWeightThreshold)
	{
		if (maxDistance == 0)
			return 0.f;
		
		final int[][] distances = context.game().distancesToRegions();		
		final Owned owned = context.state().owned();

		final List<? extends Location>[] pieces = owned.positions(player);
		float value = 0.f;

		for (int i = 0; i < pieces.length; ++i)
		{
			if (pieces[i].isEmpty())
				continue;
			
			final float pieceWeight = pieceWeights.get(owned.reverseMap(player, i));
			
			if (Math.abs(pieceWeight) >= absWeightThreshold)
			{
				for (final Location position : pieces[i])
				{
					final int site = position.site();
					if (site >= distances.length)	// Different container, skip it
						continue;
					
					int minDist = Integer.MAX_VALUE;
					for (final int regionIdx : regionIndices)
					{
						final int dist = distances[regionIdx][site];
						
						if (dist < minDist)
							minDist = dist;
					}
										
					final float proximity = 1.f - ((float) minDist / maxDistance);
					value += pieceWeight * proximity;
				}
			}
		}

		return value;
	}
	
	@Override
	public FVector computeStateFeatureVector(final Context context, final int player)
	{
		final FVector featureVector = new FVector(pieceWeights.dim());
		
		if (maxDistance != 0)
		{
			final int[][] distances = context.game().distancesToRegions();		
			final Owned owned = context.state().owned();

			final List<? extends Location>[] pieces = owned.positions(player);

			for (int i = 0; i < pieces.length; ++i)
			{
				if (pieces[i].isEmpty())
					continue;
				
				final int compIdx = owned.reverseMap(player, i);
						
				for (final Location position : pieces[i])
				{
					final int site = position.site();
					if (site >= distances.length)	// Different container, skip it
						continue;

					int minDist = Integer.MAX_VALUE;
					for (final int regionIdx : regionIndices)
					{
						final int dist = distances[regionIdx][site];

						if (dist < minDist)
							minDist = dist;
					}

					final float proximity = 1.f - ((float) minDist / maxDistance);
					featureVector.addToEntry(compIdx, proximity);
				}
			}
		}
		
		return featureVector;
	}
	
	@Override
	public FVector paramsVector()
	{
		return pieceWeights;
	}
	
	@Override
	public void init(final Game game)
	{
		// Compute vector of piece weights
		pieceWeights = HeuristicTerm.pieceWeightsVector(game, pieceWeightNames, gameAgnosticWeightsArray);
		
		final TIntArrayList relevantIndices = new TIntArrayList();
		int max = 0;
		for (int i = 0; i < game.equipment().regions().length; ++i)
		{
			final Regions region = game.equipment().regions()[i];
			
			if (region.owner() == regionPlayer)
			{
				final int[] distances = game.distancesToRegions()[i];
			
				if (distances != null)
				{			
					relevantIndices.add(i);
					
					for (int j = 0; j < distances.length; ++j)
					{
						if (distances[j] > max)
							max = distances[j];
					}
				}
			}
		}
		
		maxDistance = max;
		regionIndices = relevantIndices.toArray();
	}
	
	@Override
	public int updateParams(final Game game, final FVector newParams, final int startIdx)
	{
		final int retVal = super.updateParams(game, newParams, startIdx);
		
		// Need to update the array of weights we were passed in constructor
		// in case we decide to write ourselves to a file
		final Object[] returnArrays = updateGameAgnosticWeights(game, pieceWeights, pieceWeightNames, gameAgnosticWeightsArray);
		pieceWeightNames = (String[]) returnArrays[0];
		gameAgnosticWeightsArray = (float[]) returnArrays[1];
		
		return retVal;
	}

	//-------------------------------------------------------------------------
	
	/**
	 * @param game
	 * @return True if heuristic of this type could be applicable to given game
	 */
	public static boolean isApplicableToGame(final Game game)
	{
		if (game.distancesToRegions() == null)
			return false;
		
		if (game.equipment().components().length <= 1)
			return false;
		
		final Regions[] regions = game.equipment().regions();
		
		if (regions.length == 0)
			return false;
		
		boolean foundOwnedRegion = false;
		
		for (final Regions region : regions)
		{
			if (region.owner() > 0 && region.owner() <= game.players().count())
			{
				foundOwnedRegion = true;
				break;
			}
		}
		
		return foundOwnedRegion;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Player whose owned regions we compute proximity to
	 */
	public int regionPlayer()
	{
		return regionPlayer;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		
		sb.append("(playerRegionsProximity");
		if (transformation != null)
			sb.append(" transformation:" + transformation.toString());
		if (weight != 1.f)
			sb.append(" weight:" + weight);
		
		sb.append(" player:" + regionPlayer);
		
		if (pieceWeightNames.length > 1 || (pieceWeightNames.length == 1 && pieceWeightNames[0].length() > 0))
		{
			sb.append(" pieceWeights:{\n");
			
			for (int i = 0; i < pieceWeightNames.length; ++i)
			{
				if (gameAgnosticWeightsArray[i] != 0.f)
					sb.append("        (pair " + StringRoutines.quote(pieceWeightNames[i]) + " " + gameAgnosticWeightsArray[i] + ")\n");
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
		boolean haveRelevantPieces = false;
		final StringBuilder pieceWeightsSb = new StringBuilder();
		
		if (pieceWeightNames.length > 1 || (pieceWeightNames.length == 1 && pieceWeightNames[0].length() > 0))
		{
			for (int i = 0; i < pieceWeightNames.length; ++i)
			{
				if (Math.abs(weight * gameAgnosticWeightsArray[i]) >= threshold)
				{
					pieceWeightsSb.append("        (pair " + StringRoutines.quote(pieceWeightNames[i]) + " " + gameAgnosticWeightsArray[i] + ")\n");
					haveRelevantPieces = true;
					shouldPrint = true;
				}
			}
		}
		else if (Math.abs(weight) >= threshold)
		{
			// No manually specified weights, so they will all default to 1.0,
			// and we have a large enough term-wide weight
			shouldPrint = true;
		}
		
		if (shouldPrint)
		{
			final StringBuilder sb = new StringBuilder();
		
			sb.append("(playerRegionsProximity");
			if (transformation != null)
				sb.append(" transformation:" + transformation.toString());
			if (weight != 1.f)
				sb.append(" weight:" + weight);
			
			sb.append(" player:" + regionPlayer);
			
			if (haveRelevantPieces)
			{
				sb.append(" pieceWeights:{\n");
				sb.append(pieceWeightsSb);
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
	public boolean canBeMerged(final HeuristicTerm term)
	{
		return (this.getClass().getName().equals(term.getClass().getName()) && regionPlayer() == ((PlayerRegionsProximity) term).regionPlayer());
	}
	
	@Override
	public void merge(final HeuristicTerm term) 
	{
		final PlayerRegionsProximity castTerm = (PlayerRegionsProximity) term;
		for (int i = 0; i < pieceWeightNames.length; i++)
			for (int j = 0; j < castTerm.pieceWeightNames.length; j++)
				if (pieceWeightNames[i].equals(castTerm.pieceWeightNames[j]))
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
		return "Sum of owned pieces, weighted by proximity to owned region(s).";
	}
	
	@Override
	public String toEnglishString(final Context context, final int playerIndex) 
	{
		final StringBuilder sb = new StringBuilder();

		if (pieceWeightNames.length > 1 || (pieceWeightNames.length == 1 && pieceWeightNames[0].length() > 0))
		{
			for (int i = 0; i < pieceWeightNames.length; ++i)
			{
				if (gameAgnosticWeightsArray[i] != 0.f)
				{
					final int pieceTrailingNumbers = Integer.valueOf(StringRoutines.getTrailingNumbers(pieceWeightNames[i])).intValue();
					
					if (playerIndex == -1 || pieceTrailingNumbers == playerIndex)
					{
						if (gameAgnosticWeightsArray[i] > 0)
							sb.append("You should try to move your " + StringRoutines.removeTrailingNumbers(pieceWeightNames[i]) + "(s) towards the regions owned by Player " + regionPlayer);
						else
							sb.append("You should try to move your " + StringRoutines.removeTrailingNumbers(pieceWeightNames[i]) + "(s) away from the regions owned by Player " + regionPlayer);
						
						sb.append(", " + HeuristicUtil.convertWeightToString(gameAgnosticWeightsArray[i]) + ".\n");
					}
				}
			}
		}
		else
		{
			if (weight > 0)
				sb.append("You should try to move your piece(s) towards the regions owned by Player " + regionPlayer);
			else
				sb.append("You should try to move your piece(s) away from the regions owned by Player " + regionPlayer);
			
			sb.append(", " + HeuristicUtil.convertWeightToString(weight) + ".\n");
		}
		
		return sb.toString();
	}
	
	//-------------------------------------------------------------------------

}
