package search.minimax;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import game.Game;
import main.FileHandling;
import main.collections.FVector;
import main.collections.FastArrayList;
import main.grammar.Report;
import metadata.ai.heuristics.Heuristics;
import metadata.ai.heuristics.terms.HeuristicTerm;
import metadata.ai.heuristics.terms.Material;
import metadata.ai.heuristics.terms.MobilitySimple;
import other.RankUtils;
import other.context.Context;
import other.move.Move;
import other.state.State;
import other.trial.Trial;
import training.expert_iteration.ExItExperience;
import training.expert_iteration.ExItExperience.ExItExperienceState;
import training.expert_iteration.ExpertPolicy;
import utils.data_structures.transposition_table.TranspositionTable;
import utils.data_structures.transposition_table.TranspositionTable.ABTTData;

/**
 * Implementation of BRS+ (Esser et al., 2013). Assumes perfect-information games. 
 * Uses iterative deepening when time-restricted, goes straight for
 * depth limit when only depth-limited. Extracts heuristics to use from game's metadata.
 * 
 * Cannot play games with fewer than 3 players (since then it would just revert to the
 * normal AlphaBetaSearch that we already have).
 * 
 * @author Dennis Soemers
 */
public class BRSPlus extends ExpertPolicy
{
	
	//-------------------------------------------------------------------------
	
	/** Value we use to initialise alpha ("negative infinity", but not really) */
	private static final float ALPHA_INIT = -1000000.f;
	
	/** Value we use to initialise beta ("positive infinity", but not really) */
	private static final float BETA_INIT = -ALPHA_INIT;
	
	/** Score we give to winning opponents in paranoid searches in states where game is still going (> 2 players) */
	private static final float PARANOID_OPP_WIN_SCORE = 10000.f;
	
	/** We skip computing heuristics with absolute weight value lower than this */
	public static final float ABS_HEURISTIC_WEIGHT_THRESHOLD = 0.01f;
	
	/** Our heuristic value function estimator */
	private Heuristics heuristicValueFunction = null;
	
	/** If true, we read our heuristic function to use from game's metadata */
	private final boolean heuristicsFromMetadata;
	
	/** We'll automatically return our move after at most this number of seconds if we only have one move */
	protected double autoPlaySeconds = 0.0;
	
	/** Estimated score of the root node based on last-run search */
	protected float estimatedRootScore = 0.f;
	
	/** The maximum heuristic eval we have ever observed */
	protected float maxHeuristicEval = 0.f;
	
	/** The minimum heuristic eval we have ever observed */
	protected float minHeuristicEval = 0.f;
	
	/** String to print to Analysis tab of the Ludii app */
	protected String analysisReport = null;
	
	/** Current list of moves available in root */
	protected FastArrayList<Move> currentRootMoves = null;
	
	/** The last move we returned. Need to memorise this for Expert Iteration with AlphaBeta */
	protected Move lastReturnedMove = null;
	
	/** Root context for which we've last performed a search */
	protected Context lastSearchedRootContext = null;
	
	/** Value estimates of moves available in root */
	protected FVector rootValueEstimates = null;
	
	/** The number of players in the game we're currently playing */
	protected int numPlayersInGame = 0;
	
	/** Needed for visualisations */
	protected float rootAlphaInit = ALPHA_INIT;
	
	/** Needed for visualisations */
	protected float rootBetaInit = BETA_INIT;
	
	/** Sorted (hopefully cleverly) list of moves available in root node */
	protected FastArrayList<Move> sortedRootMoves = null;
	
	/** If true at end of a search, it means we searched full tree (probably proved a draw) */
	protected boolean searchedFullTree = false;
	
	/** Transposition Table */
	protected TranspositionTable transpositionTable = null;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 */
	public BRSPlus()
	{
		friendlyName = "BRS+";
		heuristicsFromMetadata = true;
		
		transpositionTable = new TranspositionTable(12);
	}
	
	/**
	 * Constructor
	 * @param heuristicsFilepath
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public BRSPlus(final String heuristicsFilepath) throws FileNotFoundException, IOException
	{
		friendlyName = "BRS+";
		final String heuristicsStr = FileHandling.loadTextContentsFromFile(heuristicsFilepath);
		this.heuristicValueFunction = (Heuristics)compiler.Compiler.compileObject
										(
											heuristicsStr, 
											"metadata.ai.heuristics.Heuristics",
											new Report()
										);
		heuristicsFromMetadata = false;
		
		transpositionTable = new TranspositionTable(12);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public Move selectAction
	(
		final Game game, 
		final Context context, 
		final double maxSeconds,
		final int maxIterations,
		final int maxDepth
	)
	{
		final int depthLimit = maxDepth > 0 ? maxDepth : Integer.MAX_VALUE;
		lastSearchedRootContext = context;
		
		if (transpositionTable != null)
			transpositionTable.allocate();
		
		if (maxSeconds > 0)
		{
			lastReturnedMove = iterativeDeepening(game, context, maxSeconds, depthLimit, 1);
			if (transpositionTable != null)
				transpositionTable.deallocate();
			return lastReturnedMove;
		}
		else
		{
			// we'll just do iterative deepening with the depth limit as starting depth
			lastReturnedMove = iterativeDeepening(game, context, maxSeconds, depthLimit, depthLimit);
			if (transpositionTable != null)
				transpositionTable.deallocate();
			return lastReturnedMove;
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Runs iterative deepening alpha-beta
	 * @param game
	 * @param context
	 * @param maxSeconds
	 * @param maxDepth
	 * @param startDepth
	 * @return Move to play
	 */
	public Move iterativeDeepening
	(
		final Game game, 
		final Context context, 
		final double maxSeconds, 
		final int maxDepth,
		final int startDepth
	)
	{
		final long startTime = System.currentTimeMillis();
		long stopTime = (maxSeconds > 0.0) ? startTime + (long) (maxSeconds * 1000) : Long.MAX_VALUE;
		
		final int numPlayers = game.players().count();
		currentRootMoves = new FastArrayList<Move>(game.moves(context).moves());
		
		// Create a shuffled version of list of moves (random tie-breaking)
		final FastArrayList<Move> tempMovesList = new FastArrayList<Move>(currentRootMoves);
		sortedRootMoves = new FastArrayList<Move>(currentRootMoves.size());
		while (!tempMovesList.isEmpty())
		{
			sortedRootMoves.add(tempMovesList.removeSwap(ThreadLocalRandom.current().nextInt(tempMovesList.size())));
		}
		
		final int numRootMoves = sortedRootMoves.size();
		final List<ScoredMove> scoredMoves = new ArrayList<ScoredMove>(sortedRootMoves.size());
		
		if (numRootMoves == 1)
		{
			// play faster if we only have one move available anyway
			if (autoPlaySeconds >= 0.0 && autoPlaySeconds < maxSeconds)
				stopTime = startTime + (long) (autoPlaySeconds * 1000);
		}
		
		// Vector for visualisation purposes
		rootValueEstimates = new FVector(currentRootMoves.size());
		
		// storing scores found for purpose of move ordering
		final FVector moveScores = new FVector(numRootMoves);
		int searchDepth = startDepth - 1;
		final int maximisingPlayer = context.state().playerToAgent(context.state().mover());
		
		// best move found so far during a fully-completed search 
		// (ignoring incomplete early-terminated search)
		Move bestMoveCompleteSearch = sortedRootMoves.get(0);
		
		// For paranoid search, we can narrow alpha-beta window if some players already won/lost
		rootAlphaInit = ((float) RankUtils.rankToUtil(context.computeNextLossRank(), numPlayers)) * BETA_INIT;
		rootBetaInit = ((float) RankUtils.rankToUtil(context.computeNextWinRank(), numPlayers)) * BETA_INIT;
		
		while (searchDepth < maxDepth)
		{
			++searchDepth;
			searchedFullTree = true;
			//System.out.println("SEARCHING TO DEPTH: " + searchDepth);
			
			// the real alpha-beta stuff starts here
			float score = rootAlphaInit;
			float alpha = rootAlphaInit;
			final float beta = rootBetaInit;
			
			// best move during this particular search
			Move bestMove = sortedRootMoves.get(0);
			
			for (int i = 0; i < numRootMoves; ++i)
			{
				final Context copyContext = copyContext(context);
				final Move m = sortedRootMoves.get(i);
				game.apply(copyContext, m);
				final float value = alphaBeta(copyContext, searchDepth - 1, alpha, beta, maximisingPlayer, stopTime, 1);
				
				if (System.currentTimeMillis() >= stopTime || wantsInterrupt)	// time to abort search
				{
					bestMove = null;
					break;
				}
				
				final int origMoveIdx = currentRootMoves.indexOf(m);
				if (origMoveIdx >= 0)
				{
					rootValueEstimates.set(origMoveIdx, (float) scoreToValueEst(value, rootAlphaInit, rootBetaInit));
				}
				
				moveScores.set(i, value);
				
				if (value > score)		// new best move found
				{
					//System.out.println("New best move: " + m + " with eval = " + value);
					score = value;
					bestMove = m;
				}
				
				if (score > alpha)		// new lower bound
					alpha = score;
				
				if (alpha >= beta)		// beta cut-off
					break;
			}
			
			// alpha-beta is over, this is iterative deepening stuff again
			
			if (bestMove != null)		// search was not interrupted
			{
				estimatedRootScore = score;
				
				if (score == rootBetaInit)
				{
					// we've just proven a win, so we can return best move
					// found during this search
					analysisReport = friendlyName + " found a proven win at depth " + searchDepth + ".";
					return bestMove;
				}
				else if (score == rootAlphaInit)
				{
					// we've just proven a loss, so we return the best move
					// of the PREVIOUS search (delays loss for the longest
					// amount of time)
					analysisReport = friendlyName + " found a proven loss at depth " + searchDepth + ".";
					return bestMoveCompleteSearch;
				}
				else if (searchedFullTree)
				{
					// We've searched full tree but did not prove a win or loss
					// probably means a draw, play best line we have
					analysisReport = friendlyName + " completed search of depth " + searchDepth + " (no proven win or loss).";
					return bestMove;
				}
					
				bestMoveCompleteSearch = bestMove;
			}
			else
			{
				// decrement because we didn't manage to complete this search
				--searchDepth;
			}
			
			if (System.currentTimeMillis() >= stopTime || wantsInterrupt)
			{
				// we need to return
				analysisReport = friendlyName + " completed search of depth " + searchDepth + ".";
				return bestMoveCompleteSearch;
			}
			
			// order moves based on scores found, for next search
			scoredMoves.clear();
			for (int i = 0; i < numRootMoves; ++i)
			{
				scoredMoves.add(new ScoredMove(sortedRootMoves.get(i), moveScores.get(i)));
			}
			Collections.sort(scoredMoves);
			
			sortedRootMoves.clear();
			for (int i = 0; i < numRootMoves; ++i)
			{
				sortedRootMoves.add(scoredMoves.get(i).move);
			}
			
			// clear the vector of scores
			moveScores.fill(0, numRootMoves, 0.f);
		}
		
		analysisReport = friendlyName + " completed search of depth " + searchDepth + ".";
		return bestMoveCompleteSearch;
	}
	
	/**
	 * Recursive alpha-beta search function.
	 * 
	 * @param context
	 * @param depth
	 * @param inAlpha
	 * @param inBeta
	 * @param maximisingPlayer Who is the maximising player?
	 * @param stopTime
	 * @param regMoveCounter Tracks the number of regular moves between successive turns of root player (for BRS+)
	 * @return (heuristic) evaluation of the reached state, from perspective of maximising player.
	 */
	public float alphaBeta
	(
		final Context context, 
		final int depth,
		final float inAlpha,
		final float inBeta,
		final int maximisingPlayer,
		final long stopTime,
		final int regMoveCounter
	)
	{
		final Trial trial = context.trial();
		final State state = context.state();
		
		final float originalAlpha = inAlpha;
		float alpha = inAlpha;
		float beta = inBeta;
		
		final long zobrist = state.fullHash();
		final ABTTData tableData;
		if (transpositionTable != null)
		{
			tableData = transpositionTable.retrieve(zobrist);
			
			if (tableData != null)
			{
				if (tableData.depth >= depth)
				{
					// Already searched deep enough for data in TT, use results
					switch(tableData.valueType)
					{
					case TranspositionTable.EXACT_VALUE:
						return tableData.value;
					case TranspositionTable.LOWER_BOUND:
						alpha = Math.max(alpha, tableData.value);
						break;
					case TranspositionTable.UPPER_BOUND:
						beta = Math.min(beta, tableData.value);
						break;
					default:
						System.err.println("INVALID TRANSPOSITION TABLE DATA!");
						break;
					}
					
					if (alpha >= beta)
						return tableData.value;
				}
			}
		}
		else
		{
			tableData = null;
		}
		
		if (trial.over() || !context.active(maximisingPlayer))
		{
			// terminal node (at least for maximising player)
			return (float) RankUtils.agentUtilities(context)[maximisingPlayer] * BETA_INIT;
		}
		else if (depth == 0)
		{
			searchedFullTree = false;
			
			// heuristic evaluation
			float heuristicScore = heuristicValueFunction.computeValue(
					context, maximisingPlayer, ABS_HEURISTIC_WEIGHT_THRESHOLD);
			
			for (final int opp : opponents(maximisingPlayer))
			{
				if (context.active(opp))
					heuristicScore -= heuristicValueFunction.computeValue(context, opp, ABS_HEURISTIC_WEIGHT_THRESHOLD);
				else if (context.winners().contains(opp))
					heuristicScore -= PARANOID_OPP_WIN_SCORE;
			}
			
			// Invert scores if players swapped
			if (state.playerToAgent(maximisingPlayer) != maximisingPlayer)
				heuristicScore = -heuristicScore;
			
			minHeuristicEval = Math.min(minHeuristicEval, heuristicScore);
			maxHeuristicEval = Math.max(maxHeuristicEval, heuristicScore);
			
			return heuristicScore;
		}
		
		final Game game = context.game();
		final int mover = state.playerToAgent(state.mover());
		
		FastArrayList<Move> legalMoves = game.moves(context).moves();
		final int numLegalMoves = legalMoves.size();
		
		if (tableData != null)
		{
			// Put best move according to Transposition Table first
			final Move transpositionBestMove = tableData.bestMove;
			legalMoves = new FastArrayList<Move>(legalMoves);	// Copy to avoid modifying original
			
			for (int i = 0; i < legalMoves.size(); ++i)
			{
				if (transpositionBestMove.equals(legalMoves.get(i)))
				{
					final Move temp = legalMoves.get(0);
					legalMoves.set(0, legalMoves.get(i));
					legalMoves.set(i, temp);
					break;
				}
			}
		}
		
		final int numPlayers = game.players().count();
		
		// For paranoid search, we can maybe narrow alpha-beta window if some players already won/lost
		alpha = Math.max(alpha, ((float) RankUtils.rankToUtil(context.computeNextLossRank(), numPlayers)) * BETA_INIT);
		beta = Math.min(beta, ((float) RankUtils.rankToUtil(context.computeNextWinRank(), numPlayers)) * BETA_INIT);
		
		Move bestMove = legalMoves.get(0);
		
		if (mover == maximisingPlayer)
		{
			float score = ALPHA_INIT;
			
			for (int i = 0; i < numLegalMoves; ++i)
			{
				final Context copyContext = copyContext(context);
				final Move m = legalMoves.get(i);
				game.apply(copyContext, m);
				final float value = alphaBeta(copyContext, depth - 1, alpha, beta, maximisingPlayer, stopTime, 1);
				
				if (System.currentTimeMillis() >= stopTime || wantsInterrupt)	// time to abort search
				{
					return 0;
				}
				
				if (value > score)
				{
					bestMove = m;
					score = value;
				}
				
				if (score > alpha)
					alpha = score;
				
				if (alpha >= beta)	// beta cut-off
					break;
			}
			
			if (transpositionTable != null)
			{
				// Store data in transposition table
				if (score <= originalAlpha)		// Found upper bound
					transpositionTable.store(bestMove, zobrist, score, depth, TranspositionTable.UPPER_BOUND);
				else if (score >= beta)			// Found lower bound
					transpositionTable.store(bestMove, zobrist, score, depth, TranspositionTable.LOWER_BOUND);
				else							// Found exact value
					transpositionTable.store(bestMove, zobrist, score, depth, TranspositionTable.EXACT_VALUE);
			}
			
			return score;
		}
		else
		{
			float score = BETA_INIT;
			
			boolean allowRegularMoves = true;
			boolean allowSpecialMove = false;
			if (regMoveCounter == 2)
			{
				allowRegularMoves = false;
				allowSpecialMove = true;
			}
			else if (state.playerToAgent(state.next()) != maximisingPlayer)
			{
				allowSpecialMove = true;
			}
			
			boolean cutOff = false;
			
			if (allowRegularMoves)
			{
				for (int i = 0; i < numLegalMoves; ++i)
				{
					final Context copyContext = copyContext(context);
					final Move m = legalMoves.get(i);
					game.apply(copyContext, m);
					final float value = alphaBeta(copyContext, depth - 1, alpha, beta, maximisingPlayer, stopTime, regMoveCounter + 1);
					
					if (System.currentTimeMillis() >= stopTime || wantsInterrupt)	// time to abort search
					{
						return 0;
					}
					
					if (value < score)
					{
						bestMove = m;
						score = value;
					}
					
					if (score < beta)
						beta = score;
					
					if (alpha >= beta)	// alpha cut-off
					{
						cutOff = true;
						break;
					}
				}
			}
			
			if (allowSpecialMove & !cutOff)
			{
				final Context copyContext = copyContext(context);
				
				final Move m;
				if (tableData != null)		// We have move ordering from TT
					m = legalMoves.get(0);
				else						// No move ordering, just randomly pick a move
					m = legalMoves.get(ThreadLocalRandom.current().nextInt(legalMoves.size()));
				
				game.apply(copyContext, m);
				final float value = alphaBeta(copyContext, depth - 1, alpha, beta, maximisingPlayer, stopTime, regMoveCounter + 1);

				if (System.currentTimeMillis() >= stopTime || wantsInterrupt)	// time to abort search
				{
					return 0;
				}
				
				if (value < score)
				{
					bestMove = m;
					score = value;
				}
			}
			
			if (transpositionTable != null)
			{
				// Store data in transposition table
				if (score <= originalAlpha)		// Found upper bound
					transpositionTable.store(bestMove, zobrist, score, depth, TranspositionTable.UPPER_BOUND);
				else if (score >= beta)			// Found lower bound
					transpositionTable.store(bestMove, zobrist, score, depth, TranspositionTable.LOWER_BOUND);
				else							// Found exact value
					transpositionTable.store(bestMove, zobrist, score, depth, TranspositionTable.EXACT_VALUE);
			}
			
			return score;
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param player
	 * @return Opponents of given player
	 */
	public int[] opponents(final int player)
	{
		final int[] opponents = new int[numPlayersInGame - 1];
		int idx = 0;
		
		for (int p = 1; p <= numPlayersInGame; ++p)
		{
			if (p != player)
				opponents[idx++] = p;
		}
		
		return opponents;
	}
	
	/**
	 * Converts a score into a value estimate in [-1, 1]. Useful for visualisations.
	 * 
	 * @param score
	 * @param alpha 
	 * @param beta 
	 * @return Value estimate in [-1, 1] from unbounded (heuristic) score.
	 */
	public double scoreToValueEst(final float score, final float alpha, final float beta)
	{
		if (score == alpha)
			return -1.0;
		
		if (score == beta)
			return 1.0;
		
		// Map to range [-0.8, 0.8] based on most extreme heuristic evaluations
		// observed so far.
		return -0.8 + (0.8 - -0.8) * ((score - minHeuristicEval) / (maxHeuristicEval - minHeuristicEval));
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public void initAI(final Game game, final int playerID)
	{
		if (heuristicsFromMetadata)
		{
			// Read heuristics from game metadata
			final metadata.ai.Ai aiMetadata = game.metadata().ai();
			if (aiMetadata != null && aiMetadata.heuristics() != null)
			{
				heuristicValueFunction = Heuristics.copy(aiMetadata.heuristics());
			}
			else
			{
				// construct default heuristic
				heuristicValueFunction = new Heuristics(new HeuristicTerm[]{
						new Material(null, Float.valueOf(1.f), null, null),
						new MobilitySimple(null, Float.valueOf(0.001f))
				});
			}
		}
		
		if (heuristicValueFunction != null)
			heuristicValueFunction.init(game);
		
		// reset these things used for visualisation purposes
		estimatedRootScore = 0.f;
		maxHeuristicEval = 0.f;
		minHeuristicEval = 0.f;
		analysisReport = null;
		
		currentRootMoves = null;
		rootValueEstimates = null;
		
		// and these things for ExIt
		lastSearchedRootContext = null;
		lastReturnedMove = null;
		
		numPlayersInGame = game.players().count();
	}
	
	@Override
	public boolean supportsGame(final Game game)
	{
		if (game.players().count() <= 2)
			return false;
		
		if (game.isStochasticGame())
			return false;
		
		if (game.hiddenInformation())
			return false;
		
		return game.isAlternatingMoveGame();
	}
	
	@Override
	public double estimateValue()
	{
		return scoreToValueEst(estimatedRootScore, rootAlphaInit, rootBetaInit);
	}
	
	@Override
	public String generateAnalysisReport()
	{
		return analysisReport;
	}
	
	@Override
	public AIVisualisationData aiVisualisationData()
	{
		if (currentRootMoves == null || rootValueEstimates == null)
			return null;
		
		final FVector aiDistribution = rootValueEstimates.copy();
		aiDistribution.subtract(aiDistribution.min());
		
		return new AIVisualisationData(aiDistribution, rootValueEstimates, currentRootMoves);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public FastArrayList<Move> lastSearchRootMoves()
	{
		final FastArrayList<Move> moves = new FastArrayList<Move>(currentRootMoves.size());
		for (final Move move : currentRootMoves)
		{
			moves.add(move);
		}
		return moves;
	}
	
	@Override
	public FVector computeExpertPolicy(final double tau)
	{
		final FVector distribution = FVector.zeros(currentRootMoves.size());
		distribution.set(currentRootMoves.indexOf(lastReturnedMove), 1.f);
		distribution.softmax();
		return distribution;
	}
	
	@Override
	public List<ExItExperience> generateExItExperiences()
	{
		final FastArrayList<Move> actions = new FastArrayList<Move>(currentRootMoves.size());
		for (int i = 0; i < currentRootMoves.size(); ++i)
		{
			final Move m = new Move(currentRootMoves.get(i));
			m.setMover(currentRootMoves.get(i).mover());
    		m.then().clear();	// Can't serialise these, and won't need them
    		actions.add(m);
		}
		
    	final ExItExperience experience =
    			new ExItExperience
    			(
    				new Context(lastSearchedRootContext),
    				new ExItExperienceState(lastSearchedRootContext),
    				actions,
    				computeExpertPolicy(1.0),
    				FVector.zeros(actions.size()),
    				1.f
    			);
    	
    	return Arrays.asList(experience);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Wrapper for score + move, used for sorting moves based on scores.
	 * 
	 * @author Dennis Soemers
	 */
	private class ScoredMove implements Comparable<ScoredMove>
	{
		/** The move */
		public final Move move;
		/** The move's score */
		public final float score;
		
		/**
		 * Constructor
		 * @param move
		 * @param score
		 */
		public ScoredMove(final Move move, final float score)
		{
			this.move = move;
			this.score = score;
		}

		@Override
		public int compareTo(final ScoredMove other)
		{
			final float delta = other.score - this.score;
			if (delta < 0.f)
				return -1;
			else if (delta > 0.f)
				return 1;
			else
				return 0;
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param lines
	 * @return Constructs a BRS+ object from instructions in the 
	 * given array of lines
	 */
	public static BRSPlus fromLines(final String[] lines)
	{
		String friendlyName = "BRS+";
		String heuristicsFilepath = null;

		for (final String line : lines)
		{
			final String[] lineParts = line.split(",");

			if (lineParts[0].toLowerCase().startsWith("heuristics="))
			{
				heuristicsFilepath = lineParts[0].substring("heuristics=".length());
			}
			else if (lineParts[0].toLowerCase().startsWith("friendly_name="))
			{
				friendlyName = lineParts[0].substring("friendly_name=".length());
			}
		}
		
		BRSPlus brsPlus = null;
		
		if (heuristicsFilepath != null)
		{
			try
			{
				brsPlus = new BRSPlus(heuristicsFilepath);
			} 
			catch (final IOException e)
			{
				e.printStackTrace();
			}
		}
		
		if (brsPlus == null)
			brsPlus = new BRSPlus();

		brsPlus.friendlyName = friendlyName;

		return brsPlus;
	}
	
	//-------------------------------------------------------------------------

}
