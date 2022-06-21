package search.minimax;

import java.io.FileNotFoundException;
import java.io.FileWriter;
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
import main.collections.Pair;
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
import search.minimax.AlphaBetaSearch.AllowedSearchDepths;
import training.expert_iteration.ExItExperience;
import training.expert_iteration.ExpertPolicy;
import training.expert_iteration.ExItExperience.ExItExperienceState;
//import search.minimax.AlphaBetaSearch.AllowedSearchDepths;
//import search.minimax.AlphaBetaSearch.ScoredMove;
import utils.data_structures.transposition_table.TranspositionTable;
import utils.data_structures.transposition_table.TranspositionTable.ABTTData;

/**
 * Implementation of best-first minimax search
 * (as described in Best-First MinimaxSearch:First Results by Richard E. Korf
and David Maxwell Chickering (1993))
 * 
 * @author cyprien
 * (chunks of code copied from AlphaBetaSearch, especially the variables initialisation)
 */

public class BestFirstSearch extends ExpertPolicy
{
	
	/** Boolean to active additional outputs for debugging */
	public boolean debugDisplay = true;
	
	/** Do we want to allow using Transposition Table? */
	protected boolean allowTranspositionTable = true;
	
	/** Set to true to store a description of the search tree in the file Internship/searchTree.sav */
	public boolean savingSearchTreeDescription = true;
	
	public String saveAdress = "search_trees_raw/default.sav";
	
	/** Set to true to activate a scouting phase with Alpha-Beta iterative deepening before the actual BFS algorithm */
	protected boolean alphaBetaScouting = false;
	
	/** If there is a scouting phase, this is the proportion of the decision time dedicated to scouting */
	protected final double scoutingTimeProportion = 0.4;
	
	//-------------------------------------------------------------------------
	

	/** Value we use to initialise alpha ("negative infinity", but not really) */
	public static final float ALPHA_INIT = -1000000.f;
	
	/** Value we use to initialise beta ("positive infinity", but not really) */
	public static final float BETA_INIT = -ALPHA_INIT;
	
	/** Score we give to winning opponents in paranoid searches in states where game is still going (> 2 players) */
	public static final float PARANOID_OPP_WIN_SCORE = 10000.f;
	
	/** We skip computing heuristics with absolute weight value lower than this */
	public static final float ABS_HEURISTIC_WEIGHT_THRESHOLD = 0.001f;
	
	//-------------------------------------------------------------------------
	
	/** Our heuristic value function estimator */
	protected Heuristics heuristicValueFunction = null;
	
	/** If true, we read our heuristic function to use from game's metadata */
	final boolean heuristicsFromMetadata;
	
	/** We'll automatically return our move after at most this number of seconds if we only have one move */
	protected double autoPlaySeconds = 0.0;
	
	/** Estimated score of the root node based on last-run search */
	protected float estimatedRootScore = 0.f;
	
	//-------------------------------------------------------------------------

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
	
	/** Remember if we proved a win in one of our searches */
	protected boolean provedWin = false;
	
	/** Needed for visualisations */
	protected float rootAlphaInit = ALPHA_INIT;
	
	/** Needed for visualisations */
	protected float rootBetaInit = BETA_INIT;
	
	/** Sorted (hopefully cleverly) list of moves available in root node */
	protected FastArrayList<Move> sortedRootMoves = null;
	
	/** If true at end of a search, it means we searched full tree (probably proved a draw) */
	protected boolean searchedFullTree = false;
	
	/** Transposition Table (public because it is manipulated for the heuristic learning)*/
	public TranspositionTable transpositionTable = null;

	/** Do we allow any search depth, or only odd, or only even? */
	protected AllowedSearchDepths allowedSearchDepths = AllowedSearchDepths.Any;
	
	//-------------------------------------------------------------------------
	
	/** Maximum depth of the analysis performed, for an analysis report */
	protected int maxDepthReached;
	
	/** Number of different states evaluated, for an analysis report */
	protected int nbStatesEvaluated;
	
	/** Sorted (hopefully cleverly) list of moves indices available in root node */
	protected int[] sortedRootMovesIndices;
	
	/** A type for the selection policy */
	public enum SelectionPolicy
	{
		best, // picks the move of the current principal path
		safest // variant to pick the move that was explored the most
	}
	
	/** Selection policy used: */
	protected SelectionPolicy selectionPolicy = SelectionPolicy.safest;
	
	/** Legal root moves from the root */
	protected FastArrayList<Move> legalRootMoves;
	
	/** Number of times each move from the root was selected, for the _safest_ selection policy */
	protected int[] nbVisitsRootMoves;
	
	/** Scores of the moves from the root, for the final decision of the move to play */
	protected float[] rootMovesScores;
	
	/** numBitsPrimaryCode argument given when a TT is created (to avoid magic numbers in the code)*/
	private final int numBitsPrimaryCodeForTT = 12;
	
	/** Indicates if the alpha-beta scouting phase is in process */
	protected boolean currentlyScouting = false;
	
	/** An Alpha-Beta AI for the scouting */
	protected AlphaBetaSearch alphaBetaSlave;
	
	//-------------------------------------------------------------------------
	
	public StringBuffer searchTreeOutput = new StringBuffer();
	
	protected int callsOfMinimax = 0;
	
	protected int callsOfSelectAction = 0;
	
	/** We skip the first select action because it is usually not comparable to the others regarding available time */
	boolean skippedFirstSelectAction = false;
	
	//-------------------------------------------------------------------------

	/**
	 * Creates a standard best-first searcher.
	 * @return Best-first search algorithm.
	 */
	public static BestFirstSearch createBestFirstSearch()
	{
		return new BestFirstSearch();
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 */
	public BestFirstSearch()
	{
		friendlyName = "BFS";
		heuristicsFromMetadata = true;
	}
	
	/**
	 * Constructor
	 * @param heuristicsFilepath
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public BestFirstSearch(final String heuristicsFilepath) throws FileNotFoundException, IOException
	{
		final String heuristicsStr = FileHandling.loadTextContentsFromFile(heuristicsFilepath);
		heuristicValueFunction = (Heuristics)compiler.Compiler.compileObject
										(
											heuristicsStr, 
											"metadata.ai.heuristics.Heuristics",
											new Report()
										);
		heuristicsFromMetadata = false;
		friendlyName = "BFS";
	}

	/**
	 * Constructor
	 * @param heuristics
	 */
	public BestFirstSearch(final Heuristics heuristics)
	{
		heuristicValueFunction = heuristics;
		heuristicsFromMetadata = false;
		friendlyName = "BFS";
	}
	
	//-------------------------------------------------------------------------
	
	public void setSelectionPolicy(SelectionPolicy s)
	{
		selectionPolicy = s;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Wrapper for score + index, used for sorting moves indices based on scores.
	 * 
	 * @author cyprien
	 */
	protected class ScoredMoveIndex implements Comparable<ScoredMoveIndex>
	{
		/** The move */
		public final int moveIndex;
		/** The move's score */
		public final float score;
		
		/**
		 * Constructor
		 * @param moveIndex
		 * @param score
		 */
		public ScoredMoveIndex(final int moveIndex, final float score)
		{
			this.moveIndex = moveIndex;
			this.score = score;
		}
		
		public int compareTo(final ScoredMoveIndex other)
		{
			final float delta = other.score - score;
			if (delta < 0.f)
				return -1;
			else if (delta > 0.f)
				return 1;
			else
				return 0;
		}
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
		maxDepthReached = 1;
		nbStatesEvaluated = 0;
		
		if ((callsOfSelectAction==1)&&(!skippedFirstSelectAction))
		{
			callsOfSelectAction = 1;
			callsOfMinimax = 0;
			skippedFirstSelectAction = true;
		}
		else
		{
			callsOfSelectAction += 1;
		}
		
		if (transpositionTable != null)
			transpositionTable.allocate();
		
		if (maxSeconds > 0)
		{
			final long startTime = System.currentTimeMillis();
			final long stopTime = startTime + (long) (maxSeconds * 1000);
			
			if (alphaBetaScouting)
			{
				final int initDepth = allowedSearchDepths == AllowedSearchDepths.Even ? 2 : 1;
				final int depthLimit = maxDepth > 0 ? maxDepth : Integer.MAX_VALUE;

				
				if (alphaBetaSlave.transpositionTable != null)
					alphaBetaSlave.transpositionTable.allocate();

				currentlyScouting = true;
				
				/** AlphaBeta Iterative deepening is used to build a primary evaluation of the first moves in the TT */
				alphaBetaSlave.iterativeDeepening(game, context, maxSeconds*scoutingTimeProportion, depthLimit, initDepth);

				this.transpositionTable = alphaBetaSlave.transpositionTable;
				
				currentlyScouting = false;
				
				if (debugDisplay)
				{
					System.out.println("Nb of entries in the TT:"+transpositionTable.nbEntries());
					transpositionTable.dispValueStats();
				}
			};
			
			// First do BFS (paranoid if > 2 players)
			lastReturnedMove = BFSSelection(game, context, maxSeconds, Integer.MAX_VALUE);
			
			final long currentTime = System.currentTimeMillis();
			
			if (game.players().count() > 2 && currentTime < stopTime)
				throw new RuntimeException("BFS not implemented for more than 2 players");
			
			if (transpositionTable != null)
				// deallocates the transposition table even if it is the one shared with an alpha beta AI
				transpositionTable.deallocate();
			
			return lastReturnedMove;
		}
		else
		{
			throw new RuntimeException("not ready to be used");
			
			// If given no time limit, we'll use the depth limit instead
			//lastReturnedMove = BFSSelection(game, context, maxSeconds, maxDepth);
			
			//if (transpositionTable != null)
			//	transpositionTable.deallocate();
			
			//return lastReturnedMove;
		}
	}
	
	private Move finalDecision(Integer bestMoveIndex)
	{
		// need to clean the full transposition table to avoid biasing the answer, if policy is safest
		// or if the alpha-beta scouting uses the same table. /FIXME
		if (transpositionTable != null)
			transpositionTable.deallocate();
		
		switch (selectionPolicy)
		{
			case best:
				return legalRootMoves.get(bestMoveIndex);
			case safest:
				if (debugDisplay) {
					System.out.print("nbVisitsRootMoves:\n(");
					for (int i=0; i<nbVisitsRootMoves.length;i++) {
						System.out.print(nbVisitsRootMoves[i]+";");
					};
					System.out.println(")");
					
					System.out.print("rootMovesScores:\n(");
					for (int i=0; i<nbVisitsRootMoves.length;i++) {
						System.out.print(rootMovesScores[i]+";");
					};
					System.out.println(")");
				};
				return legalRootMoves.get(indexOfSafestMove());
			default:
				return legalRootMoves.get(bestMoveIndex);
		}
	}
	
	protected Move BFSSelection
	(
			final Game game,
			final Context context,
			final double maxSeconds,
			final int depthLimit
	)
	{
		final long startTime = System.currentTimeMillis();
		long stopTime = (maxSeconds > 0.0) ? startTime + (long) (maxSeconds * 1000) : Long.MAX_VALUE;
		
		final int numPlayers = game.players().count();
		currentRootMoves = new FastArrayList<Move>(game.moves(context).moves());
		final int numRootMoves = currentRootMoves.size();
		
		if (numRootMoves == 1)
		{
			// play faster if we only have one move available anyway
			if (autoPlaySeconds >= 0.0 && autoPlaySeconds < maxSeconds)
				stopTime = startTime + (long) (autoPlaySeconds * 1000);
		}
		
		// Vector for visualisation purposes
		rootValueEstimates = new FVector(currentRootMoves.size());
		rootMovesScores = new float[currentRootMoves.size()];
		
		// Storing scores found for purpose of move ordering
		//final FVector moveScores = new FVector(numRootMoves);

		final int maximisingPlayer = context.state().playerToAgent(context.state().mover());
		final State state = context.state();
		final int mover = state.playerToAgent(state.mover());
		
		if (numPlayers > 2)
			throw new RuntimeException("BFS not implemented for more than 2 players");
		
		rootAlphaInit = ALPHA_INIT;
		rootBetaInit = BETA_INIT;
		
		// For visualisation purpose:
		minHeuristicEval = rootBetaInit;
		maxHeuristicEval = rootAlphaInit;
		
		// To ouput a visual graph of the search tree:
		searchTreeOutput.setLength(0);
		searchTreeOutput.append("[\n");
		
		nbVisitsRootMoves = new int[numRootMoves];
		for (int i=0; i<numRootMoves; i++) {
			nbVisitsRootMoves[i] = 0;
		}
		
		//float score = rootAlphaInit; // to check
		float alpha = rootAlphaInit;
		float beta = rootBetaInit;
		
		// Calling the recursive minimaxBFS strategy:
		
		final Context contextCopy = copyContext(context);
		
		legalRootMoves = game.moves(contextCopy).moves();
		
		List<Long> initialNodeLabel = new ArrayList<Long>();
		initialNodeLabel.add(contextCopy.state().stateHash());
		if (savingSearchTreeDescription)
			searchTreeOutput.append("("+stringOfNodeLabel(initialNodeLabel)+","+Float.toString(getContextValue(contextCopy,maximisingPlayer,maximisingPlayer,alpha,beta))+","+Integer.toString((mover==maximisingPlayer)? 1:2)+"),\n");
				
		Pair<Integer,Float> minimaxResult = minimaxBFS(contextCopy,alpha,beta,maximisingPlayer,stopTime,1,depthLimit,legalRootMoves,initialNodeLabel);
		
		analysisReport = friendlyName + " (player " + maximisingPlayer + ") completed an analysis that reached at some point a depth of " + maxDepthReached + ":\n";
		analysisReport += "selected move has a value of "+Float.toString(minimaxResult.value())+",\n";
		analysisReport += Integer.toString(nbStatesEvaluated)+" different states were evaluated";
		analysisReport += "\n"+Integer.toString(callsOfMinimax)+" calls of minimax";
		
		
		if ((maxSeconds > 0.)&&(System.currentTimeMillis()<stopTime))
				analysisReport += " (finished analysis early) ";
		
		if (debugDisplay)
		{
			System.out.print("rootValueEstimates: (");
			for (int i=0; i<currentRootMoves.size(); i++) {
					System.out.print(rootValueEstimates.get(i)+".");
			}
			System.out.println(")");
		}
		
		// To ouput a visual graph of the search tree:
		searchTreeOutput.append("]");
		if (savingSearchTreeDescription)
		{
			try {
		      FileWriter myWriter = new FileWriter("/home/cyprien/Documents/M1/Internship/"+saveAdress);
		      myWriter.write(searchTreeOutput.toString());
		      myWriter.close();
		      System.out.println("Successfully wrote to the file.");
		    } catch (IOException e) {
		      System.out.println("An error occurred.");
		      e.printStackTrace();
		    }
		}
		try {
			FileWriter myWriter = new FileWriter("/home/cyprien/Documents/M1/Internship/average_recursive_calls/"+this.getClass().getSimpleName()+".sav");
			myWriter.write(Double.toString(((double) callsOfMinimax)/callsOfSelectAction));
		    myWriter.close();
		} catch (IOException e) {
		      System.out.println("An error occurred.");
		      e.printStackTrace();
		}
		
		return finalDecision(minimaxResult.key());
	}
	
	protected FVector estimateMoveValues
	(
		final FastArrayList<Move> legalMoves,
		final Context context,
		final int maximisingPlayer,
		final float inAlpha,
		final float inBeta,
		final List<Long> nodeLabel
	)
	{
		final int numLegalMoves = legalMoves.size();
		final FVector moveScores = new FVector(numLegalMoves);
		
		final Game game = context.game();

		final State state = context.state();
		final int mover = state.playerToAgent(state.mover());
		
		for (int i = 0; i < numLegalMoves; ++i)
		{			
			final Move m = legalMoves.get(i);
			final Context contextCopy = copyContext(context);
			
			game.apply(contextCopy, m);
			
			final State newState = contextCopy.state();
			final int newMover = newState.mover();
			
			final float heuristicScore = getContextValue(contextCopy,maximisingPlayer,mover,inAlpha,inBeta);

			if (savingSearchTreeDescription)
			{
				nodeLabel.add(contextCopy.state().fullHash());
				searchTreeOutput.append("("+stringOfNodeLabel(nodeLabel)+","+Float.toString(heuristicScore)+","+((newMover==maximisingPlayer)? 1:2)+"),\n");
				nodeLabel.remove(nodeLabel.size()-1);
			}
			moveScores.set(i,heuristicScore);
			
		};
		
		return moveScores;
	}
	
	protected Pair<Integer,Float> minimaxBFS
	(
		final Context context,
		final float inAlpha,
		final float inBeta,
		final int maximisingPlayer,
		final long stopTime,
		final int analysisDepth,
		final int depthLimit,
		final FastArrayList<Move> legalMoves,
		final List<Long> nodeLabel //used when we want to output the tree-search graph
	)
	{
		final Trial trial = context.trial();
		final State state = context.state();
		
		float alpha = inAlpha;
		float beta = inBeta;
		
		callsOfMinimax += 1;
		
		// updating maxDepthReached
		if (analysisDepth > maxDepthReached) {
			maxDepthReached = analysisDepth;
		}

		if (trial.over() || !context.active(maximisingPlayer))
		{
			// terminal node (at least for maximizing player)
			return new Pair<Integer,Float>( -1  , (float) (RankUtils.agentUtilities(context)[maximisingPlayer] * BETA_INIT));
		};
		

		final Game game = context.game();
		final int mover = state.playerToAgent(state.mover());
		
		final int numLegalMoves = legalMoves.size();
		
		/** 
		 * ------------------------------------------------------------------------------
		 * Computing a quick evaluation of all the possible moves to order them before exploration
		 * ------------------------------------------------------------------------------
		*/
		final FVector moveScores = estimateMoveValues(legalMoves,context,maximisingPlayer,inAlpha,inBeta,nodeLabel);
		
		float estimatedScore;		
		for (int i = 0; i < numLegalMoves; ++i)
		{
			estimatedScore = moveScores.get(i);
			
			if (((mover == maximisingPlayer)&&(estimatedScore > inBeta)) || ((mover != maximisingPlayer)&&(estimatedScore < inAlpha)))
			{
				// in this case we can stop the search because this value or any other won't be used
				return new Pair<Integer,Float>(i,estimatedScore);
			};
			
			if (analysisDepth==1) {
				rootMovesScores[i] = estimatedScore;
				//rootValueEstimates.set(i,(float) scoreToValueEst(estimatedScore,inAlpha,inBeta));
			}
		};

		// Create a shuffled version of list of moves indices (random tie-breaking)
		final FastArrayList<Integer> tempMovesListIndices = new FastArrayList<Integer>(legalMoves.size());
		for (int i=0; i<numLegalMoves; i++)
		{
			tempMovesListIndices.add(i);
		}
		final int[] sortedMoveIndices = new int[numLegalMoves];
		for (int i=0; i<legalMoves.size(); i++)
		{
			sortedMoveIndices[i] = tempMovesListIndices.removeSwap(ThreadLocalRandom.current().nextInt(tempMovesListIndices.size()));
		}

		final List<ScoredMoveIndex> scoredMoveIndices = new ArrayList<ScoredMoveIndex>(numLegalMoves);
		for (int i = 0; i < numLegalMoves; ++i)
		{
			scoredMoveIndices.add(new ScoredMoveIndex(sortedMoveIndices[i], moveScores.get(sortedMoveIndices[i])));
		}
		if (mover == maximisingPlayer)
			Collections.sort(scoredMoveIndices);
		else
			Collections.sort(scoredMoveIndices,Collections.reverseOrder());
		// (the natural order of scored Move Indices is decreasing)
		
		Integer bestMoveIndex = scoredMoveIndices.get(0).moveIndex;
		float bestScore = scoredMoveIndices.get(0).score;
		
		if (analysisDepth >= depthLimit) {
			return new Pair<Integer,Float>(bestMoveIndex,bestScore);
		}
		
		/** 
		 * ------------------------------------------------------------------------------
		 * This is were the real recursive exploration begins:
		 * ------------------------------------------------------------------------------
		*/
		
		float newAlpha;
		float newBeta;
		
		while ((alpha <= bestScore)&&(bestScore <= beta))
		{
			final Context copyContext = copyContext(context); // could be replaced by a use of game.undo if it was reliable
			
			if (System.currentTimeMillis() >= stopTime || wantsInterrupt)
				break;
			
			if (analysisDepth==1) {
				nbVisitsRootMoves[bestMoveIndex] += 1;
			}		
			
			game.apply(copyContext, legalMoves.get(bestMoveIndex));
			
			final long zobrist = copyContext.state().fullHash();
			
			if (mover == maximisingPlayer)
			{
				if ( numLegalMoves<=1 ) 
				{
					newAlpha = alpha;
					newBeta = beta;
				}
				else
				{
					// We might use the score of the second move as a new alpha
					newAlpha = Math.max( alpha , scoredMoveIndices.get(1).score );
					newBeta = beta;
				}
			}
			else
			{
				if ( numLegalMoves<=1 )
				{
					newAlpha = alpha;
					newBeta = beta;
				}
				else
				{
					// We might use the score of the second move as a new beta
					newAlpha = alpha;
					newBeta = Math.min( beta , scoredMoveIndices.get(1).score );
				}
			}
			
			// Recursive call of minimaxBFS (we only need the value of the score):
			
			final FastArrayList<Move> nextLegalMoves = game.moves(copyContext).moves();

			nodeLabel.add(copyContext.state().fullHash());
			
			bestScore = minimaxBFS(copyContext,newAlpha,newBeta,maximisingPlayer,stopTime,analysisDepth+1,depthLimit,nextLegalMoves,nodeLabel).value();			
			
			nodeLabel.remove(nodeLabel.size()-1);
			
			if (analysisDepth==1) {
				rootMovesScores[bestMoveIndex] = bestScore;
				rootValueEstimates.set(bestMoveIndex,(float) scoreToValueEst(bestScore, inAlpha, inBeta));
				estimatedRootScore = bestScore;
			};
			
			// We can consider that this value for the node is more accurate than any previous value calculated
			if (transpositionTable != null)
				transpositionTable.store(null, zobrist, bestScore, 0, TranspositionTable.EXACT_VALUE);
			
			// Insert the updated scored move in the list of moves
			scoredMoveIndices.set(0,new ScoredMoveIndex(bestMoveIndex, bestScore));
			int k = 1;
			while ((k<scoredMoveIndices.size() && (  ( (mover==maximisingPlayer) && (scoredMoveIndices.get(k).score>=bestScore) )  ||  ( (mover!=maximisingPlayer) && (scoredMoveIndices.get(k).score<=bestScore) )) ))
			{
				scoredMoveIndices.set(k-1, scoredMoveIndices.get(k));
				k += 1;
			}
			scoredMoveIndices.set(k-1, new ScoredMoveIndex(bestMoveIndex, bestScore));
			
			if (bestMoveIndex == scoredMoveIndices.get(0).moveIndex) {
				// If the best move did not change eventhough it was fully explored then we can keep it
				break;
			}
			
			bestMoveIndex = scoredMoveIndices.get(0).moveIndex;
			bestScore = scoredMoveIndices.get(0).score;
		};
		
		//assert (state.stateHash()==copyContext.state().stateHash());
		
		return new Pair<Integer,Float>(bestMoveIndex,bestScore);
	}
	
	/** 
	 * Method to evaluate a state, with heuristics if the state is not terminal.
	 * Since the transposition tables can be used, the value returned is only exact if it is
	 * in the bounds.
	 * Note: if the bounds are infinite, the variable previousMover is not used.
	 * @param context
	 * @param maximisingPlayer
	 * @param previousMover
	 * @param inAlpha
	 * @param inBeta
	 * @return
	 */
	protected float getContextValue
	(
		final Context context,
		final int maximisingPlayer,
		final int previousMover,
		final float inAlpha,
		final float inBeta
	)
	{
		boolean valueRetrievedFromMemory = false;
		float heuristicScore = 0;
		
		final long zobrist = context.state().fullHash();
		final ABTTData tableData;
		if (transpositionTable != null)
		{
			tableData = transpositionTable.retrieve(zobrist);
			
			if (tableData != null)
			{
				// Already searched for data in TT, use results
				switch(tableData.valueType)
				{
				case TranspositionTable.EXACT_VALUE:
					heuristicScore = tableData.value;
					valueRetrievedFromMemory = true;
					break;
				case TranspositionTable.LOWER_BOUND:
					if ((previousMover == maximisingPlayer)&&(tableData.value > inBeta)) {
						heuristicScore = tableData.value;
						// the value wouldn't be exact but we don't care since the principal path will change
						valueRetrievedFromMemory = true;
					};
					break;
				case TranspositionTable.UPPER_BOUND:
					if ((previousMover != maximisingPlayer)&&(tableData.value < inAlpha)) {
						heuristicScore = tableData.value;
						valueRetrievedFromMemory = true;
					};
					break;
				case TranspositionTable.INVALID_VALUE:
					System.err.println("INVALID TRANSPOSITION TABLE DATA: INVALID VALUE");
					break;
				default:
					// bounds are not used up to this point
					break;
				}
			}
		}
		
		// Only compute heuristicScore if we didn't have a score registered in the TT
		if (!valueRetrievedFromMemory) {
			if (context.trial().over() || !context.active(maximisingPlayer))
			{
				// terminal node (at least for maximising player)
				heuristicScore = (float) RankUtils.agentUtilities(context)[maximisingPlayer] * BETA_INIT;
			}
			else {
				heuristicScore = heuristicValueFunction().computeValue(
						context, maximisingPlayer, ABS_HEURISTIC_WEIGHT_THRESHOLD);
			
				for (final int opp : opponents(maximisingPlayer))
				{
					if (context.active(opp))
						heuristicScore -= heuristicValueFunction().computeValue(context, opp, ABS_HEURISTIC_WEIGHT_THRESHOLD);
					else if (context.winners().contains(opp))
						heuristicScore -= PARANOID_OPP_WIN_SCORE;
				}
			}
			
			// Invert scores if players swapped (to check)
			if (context.state().playerToAgent(maximisingPlayer) != maximisingPlayer)
				heuristicScore = -heuristicScore;
			
			if (transpositionTable != null)
			{
				transpositionTable.store(null, zobrist, heuristicScore, 0, TranspositionTable.EXACT_VALUE);
			}
			
			nbStatesEvaluated += 1;
		};

		minHeuristicEval = Math.min(minHeuristicEval, heuristicScore);
		maxHeuristicEval = Math.max(maxHeuristicEval, heuristicScore);
		
		return heuristicScore;
	}
	
	//-------------------------------------------------------------------------

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
	
	/* We redefine the full method initAI just for using a new type of 
	*TranspositionTable for now
	FIXME: using a type union could avoid that*/
	@Override
	public void initAI(final Game game, final int playerID)
	{
		
		if (heuristicsFromMetadata)
		{
			if (debugDisplay) System.out.println("Reading heuristics from game metadata...");
			
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
		
		if (heuristicValueFunction() != null)
			heuristicValueFunction().init(game);
		
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
		
		if (game.usesNoRepeatPositionalInGame() || game.usesNoRepeatPositionalInTurn())
			transpositionTable = null;
		else if (!allowTranspositionTable)
			transpositionTable = null;
		else
			transpositionTable = new TranspositionTable(numBitsPrimaryCodeForTT);
	
		if (alphaBetaScouting)
		{
			alphaBetaSlave = new AlphaBetaSearch(heuristicValueFunction);
			alphaBetaSlave.initAI(game, playerID);
		}
	
	
	}
	
	@Override
	public boolean supportsGame(final Game game)
	{
		if (game.players().count() != 2)
			return false;
		
		if (game.isStochasticGame())
			return false;
		
		if (game.hiddenInformation())
			return false;
		
		if (game.hasSubgames())		// Cant properly init most heuristics
			return false;
		
		if (!(game.isAlternatingMoveGame()))
			return false;
		
		return true;
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
		
		if (currentlyScouting)
			return alphaBetaSlave.aiVisualisationData();
		else
		{		
			final FVector aiDistribution = rootValueEstimates.copy();
			aiDistribution.subtract(aiDistribution.min());
		
			return new AIVisualisationData(aiDistribution, rootValueEstimates, currentRootMoves);
		}
	}
	
	public Heuristics heuristicValueFunction() 
	{
		return heuristicValueFunction;
	}
	
	public Integer indexOfSafestMove()
	{
		
		/** Picks the safest move according to the following policy:
		 * - if a move is a win, pick it
		 * - picks a move that was visited a maximal number of times and that a loosing play	
		 * - in case of equality, uses the score attributed to each moves as a tie breaker
		* */
		for (int i = 0; i < nbVisitsRootMoves.length; i++)
		{
			if (rootMovesScores[i]>=BETA_INIT-1) {
				return i;
			}
		}
		
		int index = 0;
		int value1 = nbVisitsRootMoves[index];
		float value2 = rootMovesScores[index];
		
		for (int i = 0; i < nbVisitsRootMoves.length; i++)
		{
			if (nbVisitsRootMoves[i] >= value1)
			{
				if ((nbVisitsRootMoves[i]>value1)||(rootMovesScores[i]>value2)) {
					index = i;
					value1 = nbVisitsRootMoves[i];
					value2 = rootMovesScores[i];
				}
			}
		}
		return index;
	}
	
	// ------------------------------------------------------------------------

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
	
	// ------------------------------------------------------------------------
	
	public static Integer indexOfMax( int[] array)
	{
		
		int index = 0;
		int value = array[index];
		
		for (int i = 0; i < array.length; i++)
		{
			if (array[i] > value)
			{
				index = i;
				value = array[i];
			}
		}
		return index;
	}
	
	public static String stringOfNodeLabel( List<Long> nodeLabel)
	{
		String res = "(";
		
		for (Long hash: nodeLabel)
		{
			res += Long.toString(hash);
			res += ",";
		};
		res += ")";
		
		return res;
		
	}
}
