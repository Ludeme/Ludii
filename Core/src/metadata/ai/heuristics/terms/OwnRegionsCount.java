package metadata.ai.heuristics.terms;

import annotations.Name;
import annotations.Opt;
import game.Game;
import game.equipment.other.Regions;
import gnu.trove.list.array.TIntArrayList;
import main.collections.FVector;
import metadata.ai.heuristics.transformations.HeuristicTransformation;
import other.context.Context;

/**
 * Defines a heuristic term based on the sum of all counts of sites
 * in a player's owned regions.
 *
 * @author Dennis Soemers
 */
public class OwnRegionsCount extends HeuristicTerm
{
	
	//-------------------------------------------------------------------------
	
	/** Regions over which to sum up counts (per player) */
	private int[][] regionIndices = null;

	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * 
	 * @param transformation An optional transformation to be applied to any 
	 * raw heuristic score outputs.
	 * @param weight The weight for this term in a linear combination of multiple terms.
	 * If not specified, a default weight of $1.0$ is used.
	 * 
	 * @example (ownRegionsCount weight:1.0)
	 */
	public OwnRegionsCount
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
		return new OwnRegionsCount(this);
	}
	
	/**
	 * Copy constructor (private, so not visible to grammar)
	 * @param other
	 */
	private OwnRegionsCount(final OwnRegionsCount other)
	{
		super(other.transformation, Float.valueOf(other.weight));
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public float computeValue(final Context context, final int player, final float absWeightThreshold)
	{
		if (regionIndices[player].length == 0)
			return 0.f;
		
		final Regions[] regions = context.game().equipment().regions();
		int sumCounts = 0;
		
		for (int i = 0; i < regionIndices[player].length; ++i)
		{
			final int regionIdx = regionIndices[player][i];
			
			final Regions region = regions[regionIdx];
			final int[] sites = region.eval(context);
			
			for (final int site : sites)
			{
				sumCounts += context.containerState(0).count(site, context.game().equipment().containers()[0].defaultSite());
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
	
	@Override
	public void init(final Game game)
	{
		regionIndices = new int[game.players().count() + 1][];
		
		for (int p = 1; p <= game.players().count(); ++p)
		{
			final TIntArrayList relevantIndices = new TIntArrayList();
			for (int i = 0; i < game.equipment().regions().length; ++i)
			{
				final Regions region = game.equipment().regions()[i];
				
				if (region.owner() == p)
				{
					final int[] distances = game.distancesToRegions()[i];
				
					if (distances != null)		// This means it's a static region
					{			
						relevantIndices.add(i);
					}
				}
			}
			
			regionIndices[p] = relevantIndices.toArray();
		}
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
		
		final Regions[] regions = game.equipment().regions();
		
		if (regions.length == 1)
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
	
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		
		sb.append("(ownRegionsCount");
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
		
			sb.append("(ownRegionsCount");
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
		return "Sum of (piece) counts in owned regions.";
	}
	
	//-------------------------------------------------------------------------

}
