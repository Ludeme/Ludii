package metadata.ai.heuristics.terms;

import java.util.Arrays;
import java.util.List;

import annotations.Name;
import annotations.Opt;
import game.Game;
import game.equipment.component.Component;
import game.types.board.SiteType;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.set.hash.TIntHashSet;
import main.Constants;
import main.StringRoutines;
import main.collections.FVector;
import main.collections.FastArrayList;
import metadata.ai.heuristics.HeuristicUtil;
import metadata.ai.heuristics.transformations.HeuristicTransformation;
import metadata.ai.misc.Pair;
import other.action.Action;
import other.action.ActionType;
import other.action.move.remove.ActionRemove;
import other.concept.Concept;
import other.context.Context;
import other.context.TempContext;
import other.location.Location;
import other.move.Move;
import other.state.owned.Owned;

/**
 * Defines a heuristic term based on the unthreatened material (which
 * opponents cannot threaten with their legal moves).
 * 
 * @author Markus
 */
public class ThreatenedMaterialMultipleCount extends HeuristicTerm
{
	
	//-------------------------------------------------------------------------
	
	/** Array of names specified for piece types */
	private String[] pieceWeightNames;
	
	/** 
	 * Array of weights as specified in metadata. Will be used to initialise
	 * a weight vector for a specific game when init() is called.
	 */
	private float[] gameAgnosticWeightsArray;
	
	/** Vector with weights for every piece type */
	private FVector pieceWeights = null;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * 
	 * @param transformation An optional transformation to be applied to any 
	 * raw heuristic score outputs.
	 * @param weight The weight for this term in a linear combination of multiple terms.
	 * If not specified, a default weight of $1.0$ is used.
	 * @param pieceWeights Weights for different piece types. If no piece weights are
	 * specified at all, all piece types are given an equal weight of $1.0$. If piece
	 * weights are only specified for some piece types, all other piece types get a
	 * weight of $0$.
	 * 
	 * @example (threatenedMaterialMultipleCount pieceWeights:{ (pair "Pawn" 1.0) (pair "Bishop" 3.0) })
	 */
	public ThreatenedMaterialMultipleCount
	(
		@Name @Opt final HeuristicTransformation transformation,
		@Name @Opt final Float weight,
		@Name @Opt final Pair[] pieceWeights
	)
	{
		super(transformation, weight);
		
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
		return new ThreatenedMaterialMultipleCount(this);
	}
	
	/**
	 * Copy constructor (private, so not visible to grammar)
	 * @param other
	 */
	private ThreatenedMaterialMultipleCount(final ThreatenedMaterialMultipleCount other)
	{
		super(other.transformation, Float.valueOf(other.weight));
		pieceWeightNames = Arrays.copyOf(other.pieceWeightNames, other.pieceWeightNames.length);
		gameAgnosticWeightsArray = Arrays.copyOf(other.gameAgnosticWeightsArray, other.gameAgnosticWeightsArray.length);
	}
	
	//-------------------------------------------------------------------------

	@Override
	public float computeValue(final Context context, final int player, final float absWeightThreshold)
	{
		final Game game = context.game();
		final Owned owned = context.state().owned();
		
	
		//final int mover = context.state().mover();
		// First find all threatened positions by any opposing players
		
		final TIntIntHashMap threatenedSitesWithCounter = new TIntIntHashMap();
	
		for (int p = 1; p <= game.players().count(); ++p)
		{
			if (p != player)
				continue;
			
			final FastArrayList<Move> legalMoves;
			if (context.state().mover() == p)
			{
				legalMoves = game.moves(context).moves();
				for (final Move move : legalMoves)
				{
					for (final Action action : move.actions())
					{
						if (action != null && action.actionType()!= null && action.actionType().equals(ActionType.Remove))
						{
							final ActionRemove removeAction = (ActionRemove) action;
							final int removeSite = removeAction.to();
							final int contID = removeSite >= context.containerId().length ? -1 : context.containerId()[removeSite];
							
							if (contID == 0)
							{
								int returnValue = threatenedSitesWithCounter.putIfAbsent(removeSite, 1);
								if (returnValue != threatenedSitesWithCounter.getNoEntryValue())
									threatenedSitesWithCounter.adjustValue(removeSite, 1);
							}
						}
					}
					
					// Also assume we threaten the site we move to, regardless of whether or not there are Remove actions
					if (move.to() >= 0) 
					{
						int returnValue = threatenedSitesWithCounter.putIfAbsent(move.to(), 1);
						if (returnValue != threatenedSitesWithCounter.getNoEntryValue())
							threatenedSitesWithCounter.adjustValue(move.to(), 1);
					}
						
				}
			}
			else
			{
				//final TempContext temp = new TempContext(context);
			}
		}
		
		float value = 0.f;
		
		for (int p = 1; p <= game.players().count(); ++p)
		{
			if (p == player)
				continue;
			
			// Now count material value, but only for threatened sites
			final List<? extends Location>[] pieces = owned.positions(p);
			
			for (int i = 0; i < pieces.length; ++i)
			{
				if (pieces[i].isEmpty())
					continue;
				
				final float pieceWeight = pieceWeights.get(owned.reverseMap(player, i));
				if (Math.abs(pieceWeight) >= absWeightThreshold)
				{
					for (final Location loc : pieces[i])
					{
						if (loc.siteType() != SiteType.Cell || context.containerId()[loc.site()] == 0)
						{
							int returnValue = threatenedSitesWithCounter.get(loc.site());
							if (returnValue != threatenedSitesWithCounter.getNoEntryValue())
								value += returnValue*pieceWeight;
						}
					}
				}
			}
		}
		
	
		return value;
	}
	
	@Override
	public FVector computeStateFeatureVector(final Context context, final int player)
	{
		final FVector featureVector = new FVector(pieceWeights.dim());
		
		final Game game = context.game();
		final Owned owned = context.state().owned();
		
		// First find all threatened positions by any opposing players
		final TIntHashSet threatenedSites = new TIntHashSet();
		for (int p = 1; p <= game.players().count(); ++p)
		{
			if (p == player)
				continue;
			
			final FastArrayList<Move> oppLegalMoves;
			if (context.state().mover() == p)
			{
				oppLegalMoves = game.moves(context).moves();
			}
			else
			{
				final TempContext temp = new TempContext(context);
				temp.state().setMover(player);
				temp.trial().clearLegalMoves();
				oppLegalMoves = game.moves(temp).moves();
				
				for (final Move move : oppLegalMoves)
				{
					for (final Action action : move.actions())
					{
						if (action != null && action.actionType().equals(ActionType.Remove))
						{
							final ActionRemove removeAction = (ActionRemove) action;
							final int removeSite = removeAction.to();
							final int contID = removeSite >= context.containerId().length ? -1 : context.containerId()[removeSite];
							
							if (contID == 0)
							{
								threatenedSites.add(removeSite);
							}
						}
					}
					
					// Also assume we threaten the site we move to, regardless of whether or not there are Remove actions
					if (move.to() >= 0)
						threatenedSites.add(move.to());
				}
			}
		}
		
		final List<? extends Location>[] pieces = owned.positions(player);
		
		for (int i = 0; i < pieces.length; ++i)
		{
			if (pieces[i].isEmpty())
				continue;
			
			final int compIdx = owned.reverseMap(player, i);

			for (final Location loc : pieces[i])
			{
				if (loc.siteType() != SiteType.Cell || context.containerId()[loc.site()] == 0)
				{
					if (!threatenedSites.contains(loc.site()))
						featureVector.addToEntry(compIdx, 1.f);
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
		final Component[] components = game.equipment().components();
		
		if (components.length <= 1)
			return false;
		
		return true;
	}
	
	/**
	 * @param game
	 * @return True if the heuristic of this type is sensible for the given game
	 * 	(must be applicable, but even some applicable heuristics may be considered
	 * 	to be not sensible).
	 */
	public static boolean isSensibleForGame(final Game game)
	{
		return isApplicableToGame(game) && game.booleanConcepts().get(Concept.Capture.id());
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
		
		sb.append("(threatenedMaterialMultipleCount");
		if (transformation != null)
			sb.append(" transformation:" + transformation.toString());
		if (weight != 1.f)
			sb.append(" weight:" + weight);
		
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
		
			sb.append("(threatenedMaterialMultipleCount");
			if (transformation != null)
				sb.append(" transformation:" + transformation.toString());
			if (weight != 1.f)
				sb.append(" weight:" + weight);
			
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
	public void merge(final HeuristicTerm term) 
	{
		final ThreatenedMaterialMultipleCount castTerm = (ThreatenedMaterialMultipleCount) term;
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
		return "Sum of threatened owned pieces (with count).";
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
					final String pieceTrailingNumbers = StringRoutines.getTrailingNumbers(pieceWeightNames[i]);

					if (pieceTrailingNumbers.length() == 0 || playerIndex < 0 || Integer.valueOf(pieceTrailingNumbers).intValue() == playerIndex)
					{
						if (gameAgnosticWeightsArray[i] > 0)
							sb.append("You should try to maximise the number of threatened " + StringRoutines.removeTrailingNumbers(pieceWeightNames[i]) + "(s) you control");
						else
							sb.append("You should try to minimise the number of threatened " + StringRoutines.removeTrailingNumbers(pieceWeightNames[i]) + "(s) you control");
						
						sb.append(" (" + HeuristicUtil.convertWeightToString(gameAgnosticWeightsArray[i]) + ")\n");
					}
				}
			}
		}
		else
		{
			if (weight > 0)
				sb.append("You should try to maximise the number of threatened piece(s) you control");
			else
				sb.append("You should try to minimise the number of threatened piece(s) you control");
			
			sb.append(" (" + HeuristicUtil.convertWeightToString(weight) + ")\n");
		}
		
		return sb.toString();
	}
	
	//-------------------------------------------------------------------------

	@Override
	public float[] gameAgnosticWeightsArray() 
	{
		return gameAgnosticWeightsArray;
	}
	
	@Override
	public FVector pieceWeights() 
	{
		return pieceWeights;
	}
}
