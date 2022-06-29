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
import utils.data_structures.ScoredMove;
//import search.minimax.AlphaBetaSearch.AllowedSearchDepths;
//import search.minimax.AlphaBetaSearch.ScoredMove;
import utils.data_structures.transposition_table.TranspositionTableBFS;
import utils.data_structures.transposition_table.TranspositionTableBFS.BFSTTData;

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
	public boolean debugDisplay = false;
	
	/** Set to true to store a description of the search tree in the file Internship/searchTree.sav */
	public boolean savingSearchTreeDescription = true;
	
	public String saveAdress = "search_trees_raw/default.sav";
	
	/** Set to true to activate a scouting phase with Alpha-Beta iterative deepening before the actual BFS algorithm */
	protected boolean alphaBetaScouting = false; // won't work while bounds are not dealt with properly in minimax
	
	/** If there is a scouting phase, this is the proportion of the decision time dedicated to scouting */
	protected final double scoutingTimeProportion = 0.4;
	
	/** If true, each exploration will be continued up to the end of the tree. */
	protected boolean fullPlayouts = true;
	
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
	public TranspositionTableBFS transpositionTable = null;

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
	
	/** A type for the exploration policy */
	public enum ExplorationPolicy
	{
		best, // always picks the move that seems the best
		epsilon_greedy, // with a probability epsilon, picks a uniformly random move, else picks the best
		// to add: softmax
	}
	
	/** Exploration policy used: */
	protected ExplorationPolicy explorationPolicy = ExplorationPolicy.epsilon_greedy;
	
	/** Value of epsilon if epsilon_greedy policy is picked */
	protected double epsilon = 0.2;
	
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
	
	public void setIfFullPlayouts(Boolean b)
	{
		fullPlayouts = b;
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
		
		if ((transpositionTable != null)&&(!transpositionTable.isAllocated()))
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

				// currentlyScouting = true;
				/** AlphaBeta Iterative deepening is used to build a primary evaluation of the first moves in the TT */
				//alphaBetaSlave.iterativeDeepening(game, context, maxSeconds*scoutingTimeProportion, depthLimit, initDepth);
				//this.transpositionTable = alphaBetaSlave.transpositionTable;
				//currentlyScouting = false;
				
			};

			if (game.players().count() > 2)
				throw new RuntimeException("BFS not implemented for more than 2 players");
			
			// First do BFS (paranoid if > 2 players)
			lastReturnedMove = BFSSelection(game, context, maxSeconds, Integer.MAX_VALUE);
			
			// if (transpositionTable != null)
			// deallocates the transposition table even if it is the one shared with an alpha beta AI
			// transpositionTable.deallocate();

			if (debugDisplay)
			{
				System.out.println("Nb of entries in the TT:"+transpositionTable.nbEntries());
				transpositionTable.dispValueStats();
			}
			
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
	
	private ScoredMove finalDecision(final BFSTTData tableData, boolean maximising)
	{
		switch (selectionPolicy)
		{
			case best:
				return tableData.sortedScoredMoves.get(0);
			case safest:
				ScoredMove scoredMove;
				
				if (debugDisplay) {
					System.out.print("sortedScoredMoves:\n(");
					for (int i=0; i<tableData.sortedScoredMoves.size();i++)
					{
						scoredMove = tableData.sortedScoredMoves.get(i);
						System.out.print(Integer.toString(i)+": score "+Float.toString(scoredMove.score)+" ("+Integer.toString(scoredMove.nbVisits)+"); ");
					};
					System.out.println(")");
				};
				
				ScoredMove safestScoredMove = tableData.sortedScoredMoves.get(0);
				for (int i=0; i<tableData.sortedScoredMoves.size();i++)
				{
					scoredMove = tableData.sortedScoredMoves.get(i);
					if ((scoredMove.nbVisits>safestScoredMove.nbVisits) || (scoredMove.nbVisits==safestScoredMove.nbVisits&&( (maximising&&scoredMove.score>safestScoredMove.score) || ((!maximising)&&scoredMove.score<safestScoredMove.score))))
					{
						safestScoredMove = scoredMove;
					}
				}
				return safestScoredMove;
			default:
				return tableData.sortedScoredMoves.get(0);
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
		
		// Calling the recursive minimaxBFS strategy:
		long zobrist = context.state().fullHash();
		
		final Context contextCopy = copyContext(context);
		
		legalRootMoves = game.moves(contextCopy).moves();

		zobrist = context.state().fullHash();
		
		List<Long> initialNodeLabel = new ArrayList<Long>();
		initialNodeLabel.add(contextCopy.state().fullHash());
		if (savingSearchTreeDescription)
			searchTreeOutput.append("("+stringOfNodeLabel(initialNodeLabel)+","+Float.toString(getContextValue(contextCopy,maximisingPlayer,initialNodeLabel,0))+","+Integer.toString((mover==maximisingPlayer)? 1:2)+"),\n");
		
		while (System.currentTimeMillis() < stopTime && ( !wantsInterrupt))
		{
			
			minimaxBFS(contextCopy,maximisingPlayer,stopTime,1,depthLimit,initialNodeLabel);
		
		};
		
		zobrist = context.state().fullHash();
		final BFSTTData tableData = transpositionTable.retrieve(zobrist);
		final ScoredMove finalChoice = finalDecision(tableData, mover==maximisingPlayer);
		
		analysisReport = friendlyName + " (player " + maximisingPlayer + ") completed an analysis that reached at some point a depth of " + maxDepthReached + ":\n";
		analysisReport += "best value observed: "+Float.toString(finalChoice.score)+",\n";
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
		
		return finalChoice.move;
	}
	
	protected FVector estimateMoveValues
	(
		final FastArrayList<Move> legalMoves,
		final Context context,
		final int maximisingPlayer,
		final List<Long> nodeLabel,
		final int depth
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

			nodeLabel.add(contextCopy.state().fullHash());
			final float heuristicScore = getContextValue(contextCopy,maximisingPlayer,nodeLabel,depth);
			nodeLabel.remove(nodeLabel.size()-1);
			
			moveScores.set(i,heuristicScore);
			
		};
		
		return moveScores;
	}
	
	protected Float minimaxBFS
	(
		final Context context,
		final int maximisingPlayer,
		final long stopTime,
		final int analysisDepth,
		final int depthLimit,
		final List<Long> nodeLabel //used when we want to output the tree-search graph
	)
	{
		final Trial trial = context.trial();
		final State state = context.state();
		final Game game = context.game();
		final int mover = state.playerToAgent(state.mover());
		
		final FastArrayList<Move> legalMoves = game.moves(context).moves();
		final int numLegalMoves = legalMoves.size();
		
		callsOfMinimax += 1;
		// updating maxDepthReached
		if (analysisDepth > maxDepthReached)
			maxDepthReached = analysisDepth;
		
		/** First we check if the state is termninal (at least for maximizing player). 
		 * If so we can just return the value of the value of the state for maximisingPlayer
		 */
		if (trial.over() || !context.active(maximisingPlayer))
			return getContextValue(context,mover,nodeLabel,analysisDepth-1);

		final long zobrist = context.state().fullHash();
		final BFSTTData tableData = transpositionTable.retrieve(zobrist);
		List<ScoredMove> sortedScoredMoves = null;
		if (tableData != null)
			if (tableData.sortedScoredMoves != null)
				sortedScoredMoves = new ArrayList<ScoredMove>(tableData.sortedScoredMoves);
		
		float outputScore = 666; // this value shoud always we replaced before it is read
		Boolean firstExploration = false;
		
		if (sortedScoredMoves == null) // we can suppose that it is an exact value in this case
		{
			firstExploration = true;
			
			/** 
			 * ------------------------------------------------------------------------------
			 * In this case it is the first full analysis of this state.
			 * Thus we compute a quick evaluation of all the possible moves to order them before exploration.
			 * ------------------------------------------------------------------------------
			*/
			final FVector moveScores = estimateMoveValues(legalMoves,context,maximisingPlayer,nodeLabel,analysisDepth);

			// Create a shuffled version of list of moves indices (random tie-breaking)
			final FastArrayList<ScoredMove> tempScoredMoves = new FastArrayList<ScoredMove>(numLegalMoves);
			for (int i=0; i<numLegalMoves; i++)
			{
				tempScoredMoves.add(new ScoredMove(legalMoves.get(i), moveScores.get(i), 1));
			}
			sortedScoredMoves = new ArrayList<ScoredMove>(numLegalMoves);
			for (int i = 0; i < numLegalMoves; ++i)
			{
				sortedScoredMoves.add( tempScoredMoves.removeSwap(ThreadLocalRandom.current().nextInt(tempScoredMoves.size())) );
			}
			if (mover == maximisingPlayer)
				Collections.sort(sortedScoredMoves);
			else
				Collections.sort(sortedScoredMoves,Collections.reverseOrder());
			// (the natural order of scored Move Indices is decreasing)
			
			outputScore = sortedScoredMoves.get(0).score;
			
		}
		
		if ((!firstExploration) || fullPlayouts)
		{
			/** 
			 * ------------------------------------------------------------------------------
			 * If we already explored this state (or if fullPlayout is true), then we will recursively explore the most promising move
			 * at this state.
			 * ------------------------------------------------------------------------------
			*/
			
			final Context copyContext = copyContext(context); // could be replaced by a use of game.undo if it was reliable
			
			final int indexPicked;
			
			switch (explorationPolicy)
			{
			case best:
				indexPicked = 0;
				break;
			case epsilon_greedy:
				if (ThreadLocalRandom.current().nextDouble(1.)<epsilon)
					indexPicked = ThreadLocalRandom.current().nextInt(numLegalMoves);
				else
					indexPicked = 0;
				break;
			default:
				throw new RuntimeException("Unkown exploration policy");
			}
			
			
			final Move bestMove = sortedScoredMoves.get(indexPicked).move;
			final int previousNbVisits = sortedScoredMoves.get(indexPicked).nbVisits;
			
			// if (analysisDepth==1)
			//     System.out.println("Value of selected move at root : "+Float.toString(sortedScoredMoves.get(indexPicked).score));
			
			game.apply(copyContext, bestMove);

			nodeLabel.add(copyContext.state().fullHash());
			
			final Float scoreOfMostPromisingMove = minimaxBFS(copyContext,maximisingPlayer,stopTime,analysisDepth+1,depthLimit,nodeLabel);			
			
			nodeLabel.remove(nodeLabel.size()-1);
			
			// re-inserting the new value in the list of scored moves, last among moves of equal values			
			int k = indexPicked;
			while ((k < numLegalMoves-1))
			{
				if ( ((sortedScoredMoves.get(k+1).score >= scoreOfMostPromisingMove)&&(mover==maximisingPlayer)) || ((sortedScoredMoves.get(k+1).score <= scoreOfMostPromisingMove)&&(mover!=maximisingPlayer)))
				{
					sortedScoredMoves.set(k, sortedScoredMoves.get(k+1));
					k += 1;
				}
				else
				{
					if (k > 0)
					{
						if (( ((sortedScoredMoves.get(k-1).score < scoreOfMostPromisingMove)&&(mover==maximisingPlayer)) || ((sortedScoredMoves.get(k-1).score > scoreOfMostPromisingMove)&&(mover!=maximisingPlayer))))
						{
							sortedScoredMoves.set(k, sortedScoredMoves.get(k-1));
							k -= 1;
							//System.out.println("bad move is actually not so bad");
						}
						else break;
					}
					else break;
				}
			};
			sortedScoredMoves.set(k, new ScoredMove(bestMove,scoreOfMostPromisingMove,previousNbVisits+1));

			outputScore = sortedScoredMoves.get(0).score;
			/*
			System.out.print("Sorted moves scores (maximising="+Boolean.toString(mover==maximisingPlayer)+"): ");
			for (ScoredMove scoredMove : sortedScoredMoves)
			{
				System.out.print(Float.toString(scoredMove.score)+ ",");
			}
			System.out.println(")");

			if (analysisDepth==1)
				System.out.println("Value became "+Float.toString(outputScore)); */
			
		}
		
		if (analysisDepth==1) {
			//rootValueEstimates.set(bestMoveIndex,(float) scoreToValueEst(bestScore, inAlpha, inBeta));
			estimatedRootScore = outputScore;
		};
		
		// We can consider that this value for the node is more accurate than any previous value calculated
		if (transpositionTable != null)
			transpositionTable.store(null, zobrist, outputScore, analysisDepth-1, TranspositionTableBFS.EXACT_VALUE,sortedScoredMoves);

		return outputScore;
	}
	
	/** 
	 * Method to evaluate a state, with heuristics if the state is not terminal.
	 * Since the transposition tables can be used, the value returned is only exact if it is
	 * in the bounds.
	 * Note: if the bounds are infinite, the variable previousMover is not used.
	 * @param context
	 * @param maximisingPlayer
	 * @param previousMover
	 * @param nodeLabel
	 * @return
	 */
	protected float getContextValue
	(
		final Context context,
		final int maximisingPlayer,
		final List<Long> nodeLabel,
		final int depth // just used  to fill the depth field in the TT which is not important
	)
	{
		boolean valueRetrievedFromMemory = false;
		float heuristicScore = 0;
		
		final long zobrist = context.state().fullHash();
		final State state = context.state();
		final int newMover = state.playerToAgent(state.mover());
		
		final BFSTTData tableData;
		if (transpositionTable != null)
		{
			tableData = transpositionTable.retrieve(zobrist);
			
			if (tableData != null)
			{
				// Already searched for data in TT, use results
				switch(tableData.valueType)
				{
				case TranspositionTableBFS.EXACT_VALUE:
					heuristicScore = tableData.value;
					valueRetrievedFromMemory = true;
					break;
				case TranspositionTableBFS.INVALID_VALUE:
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
				//System.out.println("terminal node reached");
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
				transpositionTable.store(null, zobrist, heuristicScore, depth, TranspositionTableBFS.EXACT_VALUE, null);
			}
			
			nbStatesEvaluated += 1;
		};

		if (savingSearchTreeDescription)
			searchTreeOutput.append("("+stringOfNodeLabel(nodeLabel)+","+Float.toString(heuristicScore)+","+((newMover==maximisingPlayer)? 1:2)+"),\n");
		
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
		
		transpositionTable = new TranspositionTableBFS(numBitsPrimaryCodeForTT);
	
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
