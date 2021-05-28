package metadata.ai.heuristics.terms;

import annotations.Name;
import annotations.Opt;
import game.Game;
import game.equipment.other.Map;
import main.Constants;
import main.collections.FVector;
import metadata.ai.heuristics.transformations.HeuristicTransformation;
import other.context.Context;

/**
 * Defines a heuristic term that adds up the counts in sites
 * corresponding to values in Maps where Player IDs (e.g. $1$, $2$, etc.)
 * may be used as keys.
 *
 * @author Dennis Soemers
 */
public class PlayerSiteMapCount extends HeuristicTerm
{
	
	//-------------------------------------------------------------------------
	
	/** Num sites in the main container in the game for which we initialise */
	private int numSites = Constants.UNDEFINED;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * 
	 * @param transformation An optional transformation to be applied to any 
	 * raw heuristic score outputs.
	 * @param weight The weight for this term in a linear combination of multiple terms.
	 * If not specified, a default weight of $1.0$ is used.
	 * 
	 * @example (playerSiteMapCount weight:1.0)
	 */
	public PlayerSiteMapCount
	(
		@Name @Opt final HeuristicTransformation transformation,
		@Name @Opt final Float weight
	)
	{
		super(transformation, weight);
	}
	
	@Override
	public PlayerSiteMapCount copy()
	{
		return new PlayerSiteMapCount(this);
	}
	
	/**
	 * Copy constructor (private, so not visible to grammar)
	 * @param other
	 */
	private PlayerSiteMapCount(final PlayerSiteMapCount other)
	{
		super(other.transformation, Float.valueOf(other.weight));
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public float computeValue(final Context context, final int player, final float absWeightThreshold)
	{
		int sumCounts = 0;
		final Map[] maps = context.game().equipment().maps();
		
		for (final Map map : maps)
		{
			final int playerVal = map.to(player);
			
			if (playerVal != Constants.OFF && playerVal != map.noEntryValue() && playerVal < numSites && playerVal >= 0)
			{
				sumCounts += context.containerState(0).count(playerVal, context.game().equipment().containers()[0].defaultSite());
			}
		}
		
		return sumCounts;
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
	
	@Override
	public void init(final Game game)
	{
		numSites = game.equipment().containers()[0].numSites();
	}
	
	/**
	 * @param game
	 * @return True if heuristic of this type could be applicable to given game
	 */
	public static boolean isApplicableToGame(final Game game)
	{
		final Map[] maps = game.equipment().maps();
		
		if (maps.length == 0)
			return false;
		
		final int numPlayers = game.players().count();
		boolean foundPlayerMapping = false;
		
		final int numSites = game.equipment().containers()[0].numSites();
		
		for (final Map map : maps)
		{
			for (int p = 1; p <= numPlayers; ++p)
			{
				final int val = map.to(p);
				if (val != Constants.OFF && val != map.noEntryValue() && val < numSites)
				{
					foundPlayerMapping = true;
					break;
				}
			}
			
			if (foundPlayerMapping)
				break;
		}
		
		return foundPlayerMapping;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		
		sb.append("(playerSiteMapCount");
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
		
			sb.append("(playerSiteMapCount");
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
