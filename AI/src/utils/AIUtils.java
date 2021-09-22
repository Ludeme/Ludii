package utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.rng.core.RandomProviderDefaultState;

import game.Game;
import main.collections.FastArrayList;
import main.collections.StringPair;
import metadata.ai.heuristics.Heuristics;
import metadata.ai.heuristics.terms.CentreProximity;
import metadata.ai.heuristics.terms.ComponentValues;
import metadata.ai.heuristics.terms.CornerProximity;
import metadata.ai.heuristics.terms.Influence;
import metadata.ai.heuristics.terms.LineCompletionHeuristic;
import metadata.ai.heuristics.terms.Material;
import metadata.ai.heuristics.terms.MobilitySimple;
import metadata.ai.heuristics.terms.NullHeuristic;
import metadata.ai.heuristics.terms.OwnRegionsCount;
import metadata.ai.heuristics.terms.PlayerRegionsProximity;
import metadata.ai.heuristics.terms.PlayerSiteMapCount;
import metadata.ai.heuristics.terms.RegionProximity;
import metadata.ai.heuristics.terms.Score;
import metadata.ai.heuristics.terms.SidesProximity;
import other.AI;
import other.RankUtils;
import other.context.Context;
import other.move.Move;
import other.trial.Trial;
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
	 * @return An array of value estimates for all players (unswapped), based on a heuristic
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
				if (context.active(p))
				{
					// Need to set value estimate for this player, since rank not already determined
					double valueEstimate = (Math.tanh(heuristicScores[p]));
					
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
	 * @return An array of value bonus estimates for all players (unswapped), based on a heuristic
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
			heuristicScores[p] = Math.tanh(heuristicScores[p]);
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
				key.startsWith("Intercept") ||
				key.startsWith("LineCompletionHeuristic") ||
				key.startsWith("Material") ||
				key.startsWith("MobilitySimple") ||
				key.startsWith("NullHeuristic") ||
				key.startsWith("OpponentPieceProximity") ||
				key.startsWith("OwnRegionsCount") ||
				key.startsWith("PlayerRegionsProximity") ||
				key.startsWith("PlayerSiteMapCount") ||
				key.startsWith("RegionProximity") ||
				key.startsWith("Score") ||
				key.startsWith("SidesProximity")
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
		case "InfluencePos" : return new Heuristics(new Influence(null, Float.valueOf(1.f)));
		case "SidesProximityPos" : return new Heuristics(new SidesProximity(null, Float.valueOf(1.f), null));
		case "LineCompletionHeuristicPos" : return new Heuristics(new LineCompletionHeuristic(null, Float.valueOf(1.f), null));
		case "CornerProximityPos" : return new Heuristics(new CornerProximity(null, Float.valueOf(1.f), null));
		case "MobilitySimplePos" : return new Heuristics(new MobilitySimple(null, Float.valueOf(1.f)));
		case "CentreProximityPos" : return new Heuristics(new CentreProximity(null, Float.valueOf(1.f), null));
		case "RegionProximityPos" : return new Heuristics(new RegionProximity(null, Float.valueOf(1.f), null, null));
		case "ScorePos" : return new Heuristics(new Score(null, Float.valueOf(1.f)));
		case "PlayerRegionsProximityPos" : return new Heuristics(new PlayerRegionsProximity(null, Float.valueOf(1.f), null, null));
		case "PlayerSiteMapCountPos" : return new Heuristics(new PlayerSiteMapCount(null, Float.valueOf(1.f)));
		case "OwnRegionsCountPos" : return new Heuristics(new OwnRegionsCount(null, Float.valueOf(1.f)));
		case "ComponentValuesPos" : return new Heuristics(new ComponentValues(null, Float.valueOf(1.f), null, null));
		
		case "MaterialNeg" : return new Heuristics(new Material(null, Float.valueOf(-1.f), null, null));
		case "InfluenceNeg" : return new Heuristics(new Influence(null, Float.valueOf(-1.f)));
		case "SidesProximityNeg" : return new Heuristics(new SidesProximity(null, Float.valueOf(-1.f), null));
		case "LineCompletionHeuristicNeg" : return new Heuristics(new LineCompletionHeuristic(null, Float.valueOf(-1.f), null));
		case "CornerProximityNeg" : return new Heuristics(new CornerProximity(null, Float.valueOf(-1.f), null));
		case "MobilitySimpleNeg" : return new Heuristics(new MobilitySimple(null, Float.valueOf(-1.f)));
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
				"PlayerSiteMapCountNeg", "OwnRegionsCountPos", "OwnRegionsCountNeg", "ComponentValuesPos", "ComponentValuesNeg"};
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Saves a CSV file with heuristic scores of all the states encountered in
	 * the given trial.
	 * 
	 * @param origTrial
	 * @param origContext
	 * @param gameStartRNGState
	 * @param file
	 */
	public static void saveHeuristicScores
	(
		final Trial origTrial,
		final Context origContext,
		final RandomProviderDefaultState gameStartRNGState,
		final File file
	)
	{
		System.err.println("saveHeuristicScores() currently not implemented");
//		final Game game = origContext.activeGame();
//		
//		// Collect all the interesting, applicable heuristics for this game
//		final int numPlayers = game.players().count();
//		final List<Component> components = game.equipment().components();
//		final int numComponents = components.size() - 1;
//		final List<Regions> regions = game.equipment().regions();
//		final List<StateHeuristicValue> heuristics = new ArrayList<StateHeuristicValue>();
//		
//		final List<String> heuristicNames = new ArrayList<String>();
//		
//		HeuristicEnsemble ensemble = HeuristicEnsemble.fromMetadata(game, game.metadata());
//		if (ensemble == null)
//			ensemble = HeuristicEnsemble.constructDefaultHeuristics(game);
//		
//		heuristics.add(ensemble);
//		heuristicNames.add("DefaultHeuristic");
//
//		if (CentreProximity.isApplicableToGame(game))
//		{
//			for (int e = 1; e <= numComponents; ++e)
//			{
//				final FVector pieceWeights = new FVector(components.size());
//				pieceWeights.set(e, 1.f);
//				heuristics.add(new CentreProximity(game, pieceWeights));
//				heuristicNames.add("CentreProximity_" + e);
//			}
//		}
//
//		if (CornerProximity.isApplicableToGame(game))
//		{
//			for (int e = 1; e <= numComponents; ++e)
//			{
//				final FVector pieceWeights = new FVector(components.size());
//				pieceWeights.set(e, 1.f);
//				heuristics.add(new CornerProximity(game, pieceWeights));
//				heuristicNames.add("CornerProximity_" + e);
//			}
//		}
//
//		if (CurrentMoverHeuristic.isApplicableToGame(game))
//		{
//			heuristics.add(new CurrentMoverHeuristic());
//			heuristicNames.add("CurrentMoverHeuristic");
//		}
//
//		if (LineCompletionHeuristic.isApplicableToGame(game))
//		{
//			heuristics.add(new LineCompletionHeuristic(game));
//			heuristicNames.add("LineCompletionHeuristic");
//		}
//
//		if (Material.isApplicableToGame(game))
//		{
//			for (int e = 1; e <= numComponents; ++e)
//			{
//				final FVector pieceWeights = new FVector(components.size());
//				pieceWeights.set(e, 1.f);
//				heuristics.add(new Material(game, pieceWeights));
//				heuristicNames.add("Material_" + e);
//			}
//		}
//
//		if (MobilitySimple.isApplicableToGame(game))
//		{
//			heuristics.add(new MobilitySimple());
//			heuristicNames.add("MobilitySimple");
//		}
//		
//		if (OpponentPieceProximity.isApplicableToGame(game))
//		{
//			heuristics.add(new OpponentPieceProximity(game));
//			heuristicNames.add("OpponentPieceProximity");
//		}
//
//		if (OwnRegionsCount.isApplicableToGame(game))
//		{
//			heuristics.add(new OwnRegionsCount(game));
//			heuristicNames.add("OwnRegionsCount");
//		}
//
//		if (PlayerRegionsProximity.isApplicableToGame(game))
//		{
//			for (int p = 1; p <= numPlayers; ++p)
//			{
//				for (int e = 1; e <= numComponents; ++e)
//				{
//					final FVector pieceWeights = new FVector(components.size());
//					pieceWeights.set(e, 1.f);
//					heuristics.add(new PlayerRegionsProximity(game, pieceWeights, p));
//					heuristicNames.add("PlayerRegionsProximity_C" + e + "_P" + p);
//				}
//			}
//		}
//
//		if (PlayerSiteMapCount.isApplicableToGame(game))
//		{
//			heuristics.add(new PlayerSiteMapCount());
//			heuristicNames.add("PlayerSiteMapCount");
//		}
//
//		if (RegionProximity.isApplicableToGame(game))
//		{
//			for (int i = 0; i < regions.size(); ++i)
//			{
//				for (int e = 1; e <= numComponents; ++e)
//				{
//					final FVector pieceWeights = new FVector(components.size());
//					pieceWeights.set(e, 1.f);
//					heuristics.add(new RegionProximity(game, pieceWeights, i));
//					heuristicNames.add("RegionProximity_C" + e + "_R" + i);
//				}
//			}
//		}
//
//		if (Score.isApplicableToGame(game))
//		{
//			heuristics.add(new Score());
//			heuristicNames.add("Score");
//		}
//
//		if (SidesProximity.isApplicableToGame(game))
//		{
//			for (int e = 1; e <= numComponents; ++e)
//			{
//				final FVector pieceWeights = new FVector(components.size());
//				pieceWeights.set(e, 1.f);
//				heuristics.add(new SidesProximity(game, pieceWeights));
//				heuristicNames.add("SidesProximity_" + e);
//			}
//		}
//		
//		// Here we'll store all our heuristic scores
//		final float[][][] scoresMatrix = 
//				new float[1 + origTrial.numMoves() - origTrial.numInitPlace()][numPlayers + 1][heuristics.size()];
//		
//		// Re-play our trial
//		final Trial replayTrial = new Trial(game);
//		final Context replayContext = new Context(game, replayTrial);
//		replayContext.rng().restoreState(gameStartRNGState);
//		game.start(replayContext);
//		
//		// Evaluate our first state
//		for (int i = 0; i < heuristics.size(); ++i)
//		{
//			for (int p = 1; p <= numPlayers; ++p)
//			{
//				scoresMatrix[0][p][i] = heuristics.get(i).computeValue(replayContext, p, -1.f);
//			}
//		}
//		
//		int moveIdx = 0;
//					
//		while (moveIdx < replayTrial.numInitPlace())
//		{
//			++moveIdx;
//		}
//
//		while (moveIdx < origTrial.numMoves())
//		{
//			while (moveIdx < replayTrial.numMoves())
//			{
//				// looks like some actions were auto-applied (e.g. in ByScore End condition)
//				// so we just increment index without doing anything
//				++moveIdx;
//			}
//
//			if (moveIdx == origTrial.numMoves())
//				break;
//
//			final Moves legalMoves = game.moves(replayContext);
//			final List<List<Action>> legalMovesAllActions = new ArrayList<List<Action>>();
//			for (final Move legalMove : legalMoves.moves())
//			{
//				legalMovesAllActions.add(legalMove.getAllActions(replayContext));
//			}
//
//			if (game.mode().mode() == ModeType.Alternating)
//			{
//				Move matchingMove = null;
//				for (int i = 0; i < legalMovesAllActions.size(); ++i)
//				{
//					if (legalMovesAllActions.get(i).equals(origTrial.moves().get(moveIdx).getAllActions(replayContext)))
//					{
//						matchingMove = legalMoves.moves().get(i);
//						break;
//					}
//				}
//
//				if (matchingMove == null)
//				{
//					if (origTrial.moves().get(moveIdx).isPass() && legalMoves.moves().isEmpty())
//						matchingMove = origTrial.moves().get(moveIdx);
//				}
//
//				game.apply(replayContext, matchingMove);
//			}
//			else
//			{
//				// simultaneous-move game
//				// we expect the loaded move to consist of multiple moves,
//				// each of which should match one legal move
//				final List<Action> matchingSubActions = new ArrayList<Action>();
//
//				for (final Action subAction : origTrial.moves().get(moveIdx).actions())
//				{
//					final Move subMove = (Move) subAction;
//
//					Move matchingMove = null;
//					for (int i = 0; i < legalMovesAllActions.size(); ++i)
//					{
//						if (legalMovesAllActions.get(i).equals(subMove.actions()))
//						{
//							matchingMove = legalMoves.moves().get(i);
//							break;
//						}
//					}
//
//					if (matchingMove == null)
//					{
//						if (subMove.isPass() && legalMoves.moves().isEmpty())
//							matchingMove = origTrial.moves().get(moveIdx);
//					}
//
//					matchingSubActions.add(matchingMove);
//				}
//
//				final Move combinedMove = new Move(matchingSubActions);
//				combinedMove.setMover(game.players().count() + 1);
//				game.apply(replayContext, combinedMove);
//			}
//			
//			// Evaluate the state we just reached
//			for (int i = 0; i < heuristics.size(); ++i)
//			{
//				for (int p = 1; p <= numPlayers; ++p)
//				{
//					scoresMatrix[1 + moveIdx - replayTrial.numInitPlace()][p][i] = 
//							heuristics.get(i).computeValue(replayContext, p, -1.f);
//				}
//			}
//
//			++moveIdx;
//		}
//		
//		// Finally, write our CSV file
//		try (final PrintWriter writer = new PrintWriter(file))
//		{
//			// First write our header
//			for (int p = 1; p <= numPlayers; ++p)
//			{
//				for (int i = 0; i < heuristicNames.size(); ++i)
//				{
//					if (i > 0 || p > 1)
//						writer.print(",");
//					
//					writer.print("P" + p + "_" + heuristicNames.get(i));
//				}
//			}
//			
//			writer.println();
//			
//			// Now write all our rows of scores
//			for (int s = 0; s < scoresMatrix.length; ++s)
//			{
//				for (int p = 1; p <= numPlayers; ++p)
//				{
//					for (int i = 0; i < scoresMatrix[s][p].length; ++i)
//					{
//						if (i > 0 || p > 1)
//							writer.print(",");
//						
//						writer.print(scoresMatrix[s][p][i]);
//					}
//				}
//				
//				writer.println();
//			}
//		} 
//		catch (final FileNotFoundException e)
//		{
//			e.printStackTrace();
//		}
	}
	
	//-------------------------------------------------------------------------

}
