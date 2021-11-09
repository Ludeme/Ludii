package utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import features.feature_sets.BaseFeatureSet;
import function_approx.LinearFunction;
import game.Game;
import game.types.play.RoleType;
import main.collections.FastArrayList;
import main.collections.StringPair;
import metadata.ai.features.Features;
import metadata.ai.heuristics.Heuristics;
import metadata.ai.heuristics.terms.CentreProximity;
import metadata.ai.heuristics.terms.ComponentValues;
import metadata.ai.heuristics.terms.CornerProximity;
import metadata.ai.heuristics.terms.Influence;
import metadata.ai.heuristics.terms.InfluenceAdvanced;
import metadata.ai.heuristics.terms.LineCompletionHeuristic;
import metadata.ai.heuristics.terms.Material;
import metadata.ai.heuristics.terms.MobilityAdvanced;
import metadata.ai.heuristics.terms.MobilitySimple;
import metadata.ai.heuristics.terms.NullHeuristic;
import metadata.ai.heuristics.terms.OwnRegionsCount;
import metadata.ai.heuristics.terms.PlayerRegionsProximity;
import metadata.ai.heuristics.terms.PlayerSiteMapCount;
import metadata.ai.heuristics.terms.RegionProximity;
import metadata.ai.heuristics.terms.Score;
import metadata.ai.heuristics.terms.SidesProximity;
import metadata.ai.heuristics.terms.UnthreatenedMaterial;
import metadata.ai.misc.Pair;
import other.AI;
import other.RankUtils;
import other.context.Context;
import other.move.Move;
import policies.softmax.SoftmaxPolicy;
import search.minimax.AlphaBetaSearch;

/**
 * Some general utility methods for AI
 * 
 * @author Dennis Soemers
 */
public class AIUtils
{
	
	//-------------------------------------------------------------------------
	
	/** 
	 * We don't let value function estimates (e.g., from heuristics) exceed this absolute value.
	 * This is to ensure that true wins/losses will still be valued more strongly.
	 */
	private static final double MAX_ABS_VALUE_FUNCTION_ESTIMATE = 0.95;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 */
	private AIUtils()
	{
		// do not instantiate
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param game
	 * @return Constructs and returns a default AI for the given game.
	 */
	public static AI defaultAiForGame(final Game game)
	{
		return new LudiiAI();
	}
	
	/**
	 * @param allMoves List of legal moves for all current players
	 * @param mover Mover for which we want the list of legal moves
	 * @return A list of legal moves for the given mover, extracted from a given
	 * list of legal moves for any mover.
	 */
	public static FastArrayList<Move> extractMovesForMover(final FastArrayList<Move> allMoves, final int mover)
	{
		final FastArrayList<Move> moves = new FastArrayList<Move>(allMoves.size());
		
		for (final Move move : allMoves)
		{
			if (move.mover() == mover)
				moves.add(move);
		}
		
		return moves;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param context
	 * @param heuristics
	 * @return An array of value estimates for all players (accounting for swaps), based on a heuristic
	 * 	function (+ normalisation to map to value function)
	 */
	public static double[] heuristicValueEstimates(final Context context, final Heuristics heuristics)
	{
		final double[] valueEstimates = RankUtils.agentUtilities(context);
		
		if (context.active())
		{
			final double[] heuristicScores = new double[valueEstimates.length];
			final int numPlayers = valueEstimates.length - 1;
	
			for (int p = 1; p < heuristicScores.length; ++p)
			{
				final float score = heuristics.computeValue(context, p, AlphaBetaSearch.ABS_HEURISTIC_WEIGHT_THRESHOLD);
				heuristicScores[p] += score;
	
				for (int other = 1; other < heuristicScores.length; ++other)
				{
					if (other != p)
						heuristicScores[other] -= score;
				}
			}
			
			// Lower and upper bounds on util that may still be achieved
			final double utilLowerBound = RankUtils.rankToUtil(context.computeNextLossRank(), numPlayers);
			final double utilUpperBound = RankUtils.rankToUtil(context.computeNextWinRank(), numPlayers);
			final double deltaUtilBounds = utilUpperBound - utilLowerBound;
			
			for (int p = 1; p < valueEstimates.length; ++p)
			{
				if (context.active(context.state().currentPlayerOrder(p)))
				{
					// Need to set value estimate for this player, since rank not already determined
					double valueEstimate = (Math.tanh(heuristicScores[context.state().currentPlayerOrder(p)]));
					
					// Map to range given by lower and upper bounds
					valueEstimate = (((valueEstimate + 1.0) / 2.0) * deltaUtilBounds) + utilLowerBound;
					valueEstimates[p] = valueEstimate * MAX_ABS_VALUE_FUNCTION_ESTIMATE;
				}
			}
		}

		return valueEstimates;
	}
	
	/**
	 * @param context
	 * @param heuristics
	 * @return An array of value bonus estimates for all players (accounting for swaps), based on a heuristic
	 * 	function (+ normalisation to map to value function). This will compute and return heuristics\
	 * 	even for players that already have their final ranking determined.
	 */
	public static double[] heuristicValueBonusEstimates(final Context context, final Heuristics heuristics)
	{
		final double[] heuristicScores = new double[context.game().players().count() + 1];

		for (int p = 1; p < heuristicScores.length; ++p)
		{
			final float score = heuristics.computeValue(context, p, AlphaBetaSearch.ABS_HEURISTIC_WEIGHT_THRESHOLD);
			heuristicScores[p] += score;

			for (int other = 1; other < heuristicScores.length; ++other)
			{
				if (other != p)
					heuristicScores[other] -= score;
			}
		}
		
		for (int p = 1; p < heuristicScores.length; ++p)
		{
			heuristicScores[p] = Math.tanh(heuristicScores[context.state().currentPlayerOrder(p)]);
		}

		return heuristicScores;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param pair
	 * @return True if the given pair of Strings is recognised as AI-related metadata
	 */
	public static boolean isAIMetadata(final StringPair pair)
	{
		final String key = pair.key();
		
		return (
				// Some basic keywords
				key.startsWith("BestAgent") ||
				key.startsWith("AIMetadataGameNameCheck") ||
				
				// Features
				isFeaturesMetadata(pair) ||
				
				// Heuristics
				isHeuristicsMetadata(pair)
				);
	}
	
	/**
	 * @param pair
	 * @return True if the given pair of Strings is recognised as features-related metadata
	 */
	public static boolean isFeaturesMetadata(final StringPair pair)
	{
		final String key = pair.key();
		return key.startsWith("Features");
	}
	
	/**
	 * @param pair
	 * @return True if the given pair of Strings is recognised as heuristics-related metadata
	 */
	public static boolean isHeuristicsMetadata(final StringPair pair)
	{
		final String key = pair.key();
		
		return (
				// Heuristic transformations
				key.startsWith("DivNumBoardCells") ||
				key.startsWith("DivNumInitPlacement") ||
				key.startsWith("Logistic") ||
				key.startsWith("Tanh") ||
				
				// Heuristic terms
				key.startsWith("CentreProximity") ||
				key.startsWith("ComponentValues") ||
				key.startsWith("CornerProximity") ||
				key.startsWith("CurrentMoverHeuristic") ||
				key.startsWith("Influence") ||
				key.startsWith("InfluenceAdvanced") ||
				key.startsWith("Intercept") ||
				key.startsWith("LineCompletionHeuristic") ||
				key.startsWith("Material") ||
				key.startsWith("MobilityAdvanced") ||
				key.startsWith("MobilitySimple") ||
				key.startsWith("NullHeuristic") ||
				key.startsWith("OpponentPieceProximity") ||
				key.startsWith("OwnRegionsCount") ||
				key.startsWith("PlayerRegionsProximity") ||
				key.startsWith("PlayerSiteMapCount") ||
				key.startsWith("RegionProximity") ||
				key.startsWith("Score") ||
				key.startsWith("SidesProximity") ||
				key.startsWith("UnthreatenedMaterial")
				);
	}
	
	//-------------------------------------------------------------------------
	
//	/**
//	 * @param game
//	 * @param gameOptions
//	 * @return All AI metadata relevant for given game with given options
//	 */
//	public static List<StringPair> extractAIMetadata(final Game game, final List<String> gameOptions)
//	{
//		final List<StringPair> metadata = game.metadata();
//		final List<StringPair> relevantAIMetadata = new ArrayList<StringPair>();
//		
//		for (final StringPair pair : metadata)
//		{
//			if (AIUtils.isAIMetadata(pair))
//			{
//				final String key = pair.key();
//				final String[] keySplit = key.split(Pattern.quote(":"));
//				
//				boolean allOptionsMatch = true;
//				if (keySplit.length > 1)
//				{
//					final String[] metadataOptions = keySplit[1].split(Pattern.quote(";"));
//					
//					for (int i = 0; i < metadataOptions.length; ++i)
//					{
//						if (!gameOptions.contains(metadataOptions[i]))
//						{
//							allOptionsMatch = false;
//							break;
//						}
//					}
//				}
//				
//				if (allOptionsMatch)
//				{
//					relevantAIMetadata.add(pair);
//				}
//			}
//		}
//		
//		return relevantAIMetadata;
//	}
	
	/**
	 * @param game
	 * @param gameOptions
	 * @param metadata
	 * @return All features metadata relevant for given game with given options
	 */
	public static List<StringPair> extractFeaturesMetadata
	(
		final Game game, 
		final List<String> gameOptions,
		final List<StringPair> metadata
	)
	{
		final List<StringPair> relevantFeaturesMetadata = new ArrayList<StringPair>();
		
		for (final StringPair pair : metadata)
		{
			if (AIUtils.isFeaturesMetadata(pair))
			{
				final String key = pair.key();
				final String[] keySplit = key.split(Pattern.quote(":"));
				
				boolean allOptionsMatch = true;
				if (keySplit.length > 1)
				{
					final String[] metadataOptions = keySplit[1].split(Pattern.quote(";"));
					
					for (int i = 0; i < metadataOptions.length; ++i)
					{
						if (!gameOptions.contains(metadataOptions[i]))
						{
							allOptionsMatch = false;
							break;
						}
					}
				}
				
				if (allOptionsMatch)
				{
					relevantFeaturesMetadata.add(pair);
				}
			}
		}
		
		return relevantFeaturesMetadata;
	}
	
	/**
	 * @param game
	 * @param gameOptions
	 * @param metadata
	 * @return All heuristics metadata relevant for given game with given options
	 */
	public static List<StringPair> extractHeuristicsMetadata
	(
		final Game game, 
		final List<String> gameOptions,
		final List<StringPair> metadata
	)
	{
		final List<StringPair> relevantHeuristicsMetadata = new ArrayList<StringPair>();
		
		for (final StringPair pair : metadata)
		{
			if (AIUtils.isHeuristicsMetadata(pair))
			{
				final String key = pair.key();
				final String[] keySplit = key.split(Pattern.quote(":"));
				
				boolean allOptionsMatch = true;
				if (keySplit.length > 1)
				{
					final String[] metadataOptions = keySplit[1].split(Pattern.quote(";"));
					
					for (int i = 0; i < metadataOptions.length; ++i)
					{
						if (!gameOptions.contains(metadataOptions[i]))
						{
							allOptionsMatch = false;
							break;
						}
					}
				}
				
				if (allOptionsMatch)
				{
					relevantHeuristicsMetadata.add(pair);
				}
			}
		}
		
		return relevantHeuristicsMetadata;
	}
	
	//-------------------------------------------------------------------------
	
	public static Heuristics convertStringtoHeurisitc(String s)
	{
		switch(s)
		{
		case "MaterialPos" : return new Heuristics(new Material(null, Float.valueOf(1.f), null, null));
		case "UnthreatenedMaterialPos" : return new Heuristics(new UnthreatenedMaterial(null, Float.valueOf(1.f), null));
		case "InfluencePos" : return new Heuristics(new Influence(null, Float.valueOf(1.f)));
		case "InfluenceAdvancedPos" : return new Heuristics(new InfluenceAdvanced(null, Float.valueOf(1.f)));
		case "SidesProximityPos" : return new Heuristics(new SidesProximity(null, Float.valueOf(1.f), null));
		case "LineCompletionHeuristicPos" : return new Heuristics(new LineCompletionHeuristic(null, Float.valueOf(1.f), null));
		case "CornerProximityPos" : return new Heuristics(new CornerProximity(null, Float.valueOf(1.f), null));
		case "MobilitySimplePos" : return new Heuristics(new MobilitySimple(null, Float.valueOf(1.f)));
		case "MobilityAdvancedPos" : return new Heuristics(new MobilityAdvanced(null, Float.valueOf(1.f)));
		case "CentreProximityPos" : return new Heuristics(new CentreProximity(null, Float.valueOf(1.f), null));
		case "RegionProximityPos" : return new Heuristics(new RegionProximity(null, Float.valueOf(1.f), null, null));
		case "ScorePos" : return new Heuristics(new Score(null, Float.valueOf(1.f)));
		case "PlayerRegionsProximityPos" : return new Heuristics(new PlayerRegionsProximity(null, Float.valueOf(1.f), null, null));
		case "PlayerSiteMapCountPos" : return new Heuristics(new PlayerSiteMapCount(null, Float.valueOf(1.f)));
		case "OwnRegionsCountPos" : return new Heuristics(new OwnRegionsCount(null, Float.valueOf(1.f)));
		case "ComponentValuesPos" : return new Heuristics(new ComponentValues(null, Float.valueOf(1.f), null, null));
		
		case "MaterialNeg" : return new Heuristics(new Material(null, Float.valueOf(-1.f), null, null));
		case "UnthreatenedMaterialNeg" : return new Heuristics(new UnthreatenedMaterial(null, Float.valueOf(-1.f), null));
		case "InfluenceNeg" : return new Heuristics(new Influence(null, Float.valueOf(-1.f)));
		case "InfluenceAdvancedNeg" : return new Heuristics(new InfluenceAdvanced(null, Float.valueOf(-1.f)));
		case "SidesProximityNeg" : return new Heuristics(new SidesProximity(null, Float.valueOf(-1.f), null));
		case "LineCompletionHeuristicNeg" : return new Heuristics(new LineCompletionHeuristic(null, Float.valueOf(-1.f), null));
		case "CornerProximityNeg" : return new Heuristics(new CornerProximity(null, Float.valueOf(-1.f), null));
		case "MobilitySimpleNeg" : return new Heuristics(new MobilitySimple(null, Float.valueOf(-1.f)));
		case "MobilityAdvancedNeg" : return new Heuristics(new MobilityAdvanced(null, Float.valueOf(-1.f)));
		case "CentreProximityNeg" : return new Heuristics(new CentreProximity(null, Float.valueOf(-1.f), null));
		case "RegionProximityNeg" : return new Heuristics(new RegionProximity(null, Float.valueOf(-1.f), null, null));
		case "ScoreNeg" : return new Heuristics(new Score(null, Float.valueOf(-1.f)));
		case "PlayerRegionsProximityNeg" : return new Heuristics(new PlayerRegionsProximity(null, Float.valueOf(-1.f), null, null));
		case "PlayerSiteMapCountNeg" : return new Heuristics(new PlayerSiteMapCount(null, Float.valueOf(-1.f)));
		case "OwnRegionsCountNeg" : return new Heuristics(new OwnRegionsCount(null, Float.valueOf(-1.f)));
		case "ComponentValuesNeg" : return new Heuristics(new ComponentValues(null, Float.valueOf(-1.f), null, null));
		
		case "NullHeuristicPos" : return new Heuristics(new Material(null, Float.valueOf(1.f), null, null));
		default : return new Heuristics(new NullHeuristic());
		}
	}
	
	public static String[] allHeuristicNames()
	{
		return new String[] {"MaterialPos",	"InfluencePos",	"SidesProximityPos","LineCompletionHeuristicNeg", "NullHeuristicPos",	
				"LineCompletionHeuristicPos", "CornerProximityNeg",	"MobilitySimpleNeg", "CentreProximityNeg", "InfluenceNeg", 
				"MaterialNeg", "CornerProximityPos", "MobilitySimplePos", "CentreProximityPos", "SidesProximityNeg", "RegionProximityNeg",
				"RegionProximityPos", "ScorePos", "ScoreNeg", "PlayerRegionsProximityNeg", "PlayerRegionsProximityPos", "PlayerSiteMapCountPos", 
				"PlayerSiteMapCountNeg", "OwnRegionsCountPos", "OwnRegionsCountNeg", "ComponentValuesPos", "ComponentValuesNeg",
				"UnthreatenedMaterialPos", "UnthreatenedMaterialNeg", "MobilityAdvancedPos", "MobilityAdvancedNeg",
				"InfluenceAdvancedPos", "InfluenceAdvancedNeg"};
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Generates features metadata from given Selection and Playout policies
	 * @param selectionPolicy
	 * @param playoutPolicy
	 * @return
	 */
	public static metadata.ai.features.Features generateFeaturesMetadata
	(
		final SoftmaxPolicy selectionPolicy, final SoftmaxPolicy playoutPolicy
	)
	{
		final Features features;
		
		Pair[][] selectionPairs = null;
		Pair[][] playoutPairs = null;
		Pair[][] tspgPairs = null;
		int numRoles = 0;
				
		if (selectionPolicy != null)
		{
			final BaseFeatureSet[] featureSets = selectionPolicy.featureSets();
			final LinearFunction[] linearFunctions = selectionPolicy.linearFunctions();
			
			selectionPairs = new Pair[featureSets.length][];
			playoutPairs = new Pair[featureSets.length][];
			tspgPairs = new Pair[featureSets.length][];

			if (featureSets.length == 1)
			{
				// Just a single featureset for all players
				assert (numRoles == 1 || numRoles == 0);
				numRoles = 1;
				final BaseFeatureSet featureSet = featureSets[0];
				final LinearFunction linFunc = linearFunctions[0];
				final Pair[] pairs = new Pair[featureSet.spatialFeatures().length];
				
				for (int i = 0; i < pairs.length; ++i)
				{
					final float weight = linFunc.effectiveParams().allWeights().get(i);
					pairs[i] = new Pair(featureSet.spatialFeatures()[i].toString(), Float.valueOf(weight));
					
					if (Float.isNaN(weight))
						System.err.println("WARNING: writing NaN weight");
					else if (Float.isInfinite(weight))
						System.err.println("WARNING: writing infinity weight");
				}
				
				selectionPairs[0] = pairs;
			}
			else
			{
				// One featureset per player
				assert (numRoles == featureSets.length || numRoles == 0);
				numRoles = featureSets.length;
				
				for (int p = 0; p < featureSets.length; ++p)
				{
					final BaseFeatureSet featureSet = featureSets[p];
					if (featureSet == null)
						continue;
					
					final LinearFunction linFunc = linearFunctions[p];
					final Pair[] pairs = new Pair[featureSet.spatialFeatures().length];
					
					for (int i = 0; i < pairs.length; ++i)
					{
						final float weight = linFunc.effectiveParams().allWeights().get(i);
						pairs[i] = new Pair(featureSet.spatialFeatures()[i].toString(), Float.valueOf(weight));
						
						if (Float.isNaN(weight))
							System.err.println("WARNING: writing NaN weight");
						else if (Float.isInfinite(weight))
							System.err.println("WARNING: writing infinity weight");
					}
					
					selectionPairs[p] = pairs;
				}
			}
		}
		
		if (playoutPolicy != null)
		{
			final BaseFeatureSet[] featureSets = playoutPolicy.featureSets();
			final LinearFunction[] linearFunctions = playoutPolicy.linearFunctions();
			
			if (playoutPairs == null)
			{
				selectionPairs = new Pair[featureSets.length][];
				playoutPairs = new Pair[featureSets.length][];
				tspgPairs = new Pair[featureSets.length][];
			}

			if (featureSets.length == 1)
			{
				// Just a single featureset for all players
				assert (numRoles == 1 || numRoles == 0);
				numRoles = 1;
				final BaseFeatureSet featureSet = featureSets[0];
				final LinearFunction linFunc = linearFunctions[0];
				final Pair[] pairs = new Pair[featureSet.spatialFeatures().length];
				
				for (int i = 0; i < pairs.length; ++i)
				{
					final float weight = linFunc.effectiveParams().allWeights().get(i);
					pairs[i] = new Pair(featureSet.spatialFeatures()[i].toString(), Float.valueOf(weight));
					
					if (Float.isNaN(weight))
						System.err.println("WARNING: writing NaN weight");
					else if (Float.isInfinite(weight))
						System.err.println("WARNING: writing infinity weight");
				}
				
				playoutPairs[0] = pairs;
			}
			else
			{
				// One featureset per player
				assert (numRoles == featureSets.length || numRoles == 0);
				numRoles = featureSets.length;
				
				for (int p = 0; p < featureSets.length; ++p)
				{
					final BaseFeatureSet featureSet = featureSets[p];
					if (featureSet == null)
						continue;
					
					final LinearFunction linFunc = linearFunctions[p];
					final Pair[] pairs = new Pair[featureSet.spatialFeatures().length];
					
					for (int i = 0; i < pairs.length; ++i)
					{
						final float weight = linFunc.effectiveParams().allWeights().get(i);
						pairs[i] = new Pair(featureSet.spatialFeatures()[i].toString(), Float.valueOf(weight));
						
						if (Float.isNaN(weight))
							System.err.println("WARNING: writing NaN weight");
						else if (Float.isInfinite(weight))
							System.err.println("WARNING: writing infinity weight");
					}
					
					playoutPairs[p] = pairs;
				}
			}
		}
		
		if (selectionPairs == null || playoutPairs == null || tspgPairs == null)
			return null;
		
		if (numRoles == 1)
		{
			features = new Features(new metadata.ai.features.FeatureSet(RoleType.Shared, selectionPairs[0], playoutPairs[0], tspgPairs[0]));
		}
		else
		{
			// One featureset per player
			final metadata.ai.features.FeatureSet[] metadataFeatureSets = new metadata.ai.features.FeatureSet[numRoles - 1];
			
			for (int p = 1; p < numRoles; ++p)
			{
				if (selectionPairs[p] == null && playoutPairs[p] == null && tspgPairs[p] == null)
					continue;
				
				metadataFeatureSets[p - 1] = 
						new metadata.ai.features.FeatureSet(RoleType.roleForPlayerId(p), selectionPairs[p], playoutPairs[p], tspgPairs[p]);
			}
			
			features = new Features(metadataFeatureSets);
		}
		
		return features;
	}
	
	//-------------------------------------------------------------------------

}
