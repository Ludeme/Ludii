package search.minimax;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import game.Game;
import main.collections.FVector;
import main.collections.FastArrayList;
import main.collections.Pair;
import other.RankUtils;
import other.context.Context;
import other.move.Move;
import other.state.State;
import other.trial.Trial;
import policies.softmax.SoftmaxFromMetadataSelection;
import policies.softmax.SoftmaxPolicy;
import utils.data_structures.transposition_table.TranspositionTable;

public class BiasedBFS extends BestFirstSearch
{
	
	/** Number of moves that are really evaluated with the Heuristics: */
	private final int nbStateEvaluationsPerNode = 3;
	
	//-------------------------------------------------------------------------
	
	/** An epsilon parameter to give to the selection policy which hopefully is not chaging anything*/
	private final float epsilon = 0f;

	/** A learned policy to use in Selection phase */
	protected SoftmaxPolicy learnedSelectionPolicy = null;

	//-------------------------------------------------------------------------

	public static BiasedBFS createBiasedBFS ()
	{
		return new BiasedBFS();
	}
	
	/**
	 * Constructor:
	 */
	
	public BiasedBFS ()
	{
		super();
		setLearnedSelectionPolicy(new SoftmaxFromMetadataSelection(epsilon));
		friendlyName = "Biased BFS";
		
		return;
	}
	
	//-------------------------------------------------------------------------

	@Override
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
		
		final int nbLegalMoves = legalMoves.size();
		final int nbBestMoves = Math.min(nbLegalMoves, nbStateEvaluationsPerNode);
		
		/** 
		 * --------------------------------------------------------------------
		 * Computing a quick evaluation of all the possible moves to order them before exploration
		 * --------------------------------------------------------------------
		*/
		final FVector moveValueEstimations = estimateMoveValues(legalMoves,context,maximisingPlayer,inAlpha,inBeta,nodeLabel);

		/**
		 * Sorting the moves using the action evalutations:
		 */
		final FastArrayList<Integer> tempMovesListIndices = new FastArrayList<Integer>(nbLegalMoves);
		for (int i=0; i<nbLegalMoves; i++)
			tempMovesListIndices.add(i);
		final int[] sortedMoveIndices = new int[nbLegalMoves];
		for (int i=0; i<legalMoves.size(); i++)
			sortedMoveIndices[i] = tempMovesListIndices.removeSwap(ThreadLocalRandom.current().nextInt(tempMovesListIndices.size()));
		final List<ScoredMoveIndex> evaluatedMoveIndices = new ArrayList<ScoredMoveIndex>(nbLegalMoves);
		for (int i = 0; i < nbLegalMoves; ++i)
			evaluatedMoveIndices.add(new ScoredMoveIndex(sortedMoveIndices[i], moveValueEstimations.get(sortedMoveIndices[i])));
		
		Collections.sort(evaluatedMoveIndices);
		
		/**
		 * --------------------------------------------------------------------
		 * Evaluating more precisely the most promising moves
		 * --------------------------------------------------------------------
		 */
		final FVector moveScores = new FVector(nbLegalMoves);
		for (int i=0; i<nbBestMoves; i++)
		{
			final int moveIndex = evaluatedMoveIndices.get(i).moveIndex;
			
			final Move m = legalMoves.get(moveIndex);
			
			final Context contextCopy = copyContext(context);
			
			game.apply(contextCopy, m);
			
			final float heuristicScore = getContextValue(contextCopy,maximisingPlayer,mover,inAlpha,inBeta);
		
			if (savingSearchTreeDescription)
			{
				nodeLabel.add(contextCopy.state().fullHash());
				searchTreeOutput.append("("+stringOfNodeLabel(nodeLabel)+","+Float.toString(heuristicScore)+","+((mover==maximisingPlayer)? 1:2)+"),\n");
				nodeLabel.remove(nodeLabel.size()-1);
			}
			
			moveScores.set(moveIndex,heuristicScore);	
		}
		
		float estimatedScore;		
		for (int i = 0; i < nbLegalMoves; ++i)
		{
			final int moveIndex = evaluatedMoveIndices.get(i).moveIndex;
			
			estimatedScore = moveScores.get(moveIndex);
			
			if (((mover == maximisingPlayer)&&(estimatedScore > inBeta)) || ((mover != maximisingPlayer)&&(estimatedScore < inAlpha)))
			{
				// in this case we can stop the search because this value or any other won't be used
				return new Pair<Integer,Float>(moveIndex,estimatedScore);
			};
			
			if (analysisDepth==1) {
				rootMovesScores[moveIndex] = estimatedScore;
				//rootValueEstimates.set(i,(float) scoreToValueEst(estimatedScore,inAlpha,inBeta));
			}
		};

		// Create a shuffled version of list of moves indices (random tie-breaking)
		final List<ScoredMoveIndex> scoredMoveIndices = new ArrayList<ScoredMoveIndex>(nbBestMoves);
		for (int i = 0; i < nbBestMoves; ++i)
		{
			final int moveIndex = evaluatedMoveIndices.get(i).moveIndex;
			scoredMoveIndices.add(new ScoredMoveIndex(moveIndex, moveScores.get(moveIndex)));
		}
		if (mover == maximisingPlayer)
			Collections.sort(scoredMoveIndices);
		else
			Collections.sort(scoredMoveIndices,Collections.reverseOrder());
		
		
		
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
				if ( nbLegalMoves<=1 ) 
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
				if ( nbLegalMoves<=1 )
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
			
			// If the best scenario is actually the worse (which happens for instance if it leads to a loss), we evaluate a new state.
			// This way any number of states can be fully explored in the long run.
			if ((k == scoredMoveIndices.size()) && (k < evaluatedMoveIndices.size()))
			{
				final int moveIndex = evaluatedMoveIndices.get(k).moveIndex;
				
				final Move m = legalMoves.get(moveIndex);
				
				final Context contextCopy = copyContext(context);
				
				game.apply(contextCopy, m);
				
				final float heuristicScore = getContextValue(contextCopy,maximisingPlayer,mover,inAlpha,inBeta);
			
				if (savingSearchTreeDescription)
				{
					nodeLabel.add(contextCopy.state().fullHash());
					searchTreeOutput.append("("+stringOfNodeLabel(nodeLabel)+","+Float.toString(heuristicScore)+","+((mover==maximisingPlayer)? 1:2)+"),\n");
					nodeLabel.remove(nodeLabel.size()-1);
				}
				
				moveScores.set(moveIndex,heuristicScore);
				
				scoredMoveIndices.add(new ScoredMoveIndex(moveIndex,heuristicScore));
				int j = k;
				while ((j>1 && (  ( (mover==maximisingPlayer) && (scoredMoveIndices.get(j-1).score<=heuristicScore) )  ||  ( (mover!=maximisingPlayer) && (scoredMoveIndices.get(j-1).score>=heuristicScore) )) ))
				{
					scoredMoveIndices.set(j, scoredMoveIndices.get(j-1));
					j -= 1;
				}
				scoredMoveIndices.set(j, new ScoredMoveIndex(moveIndex, heuristicScore));
				
			}
			
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

	@Override
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
	
		for (int i = 0; i < numLegalMoves; ++i)
		{
			final Move m = legalMoves.get(i);
			
			final float actionValue = (float) learnedSelectionPolicy.computeLogit(context,m);
			
			moveScores.set(i,actionValue);
		};
		
		return moveScores;
	}
	
	//-------------------------------------------------------------------------
	
	public void initAI(final Game game, final int playerID)
	{
		super.initAI(game,  playerID);
		
		// Instantiate feature sets for selection policy
		if (learnedSelectionPolicy != null)
		{
			learnedSelectionPolicy.initAI(game, playerID);
		}
		
		return;
	}

	/**
	 * Sets the learned policy to use in Selection phase
	 * @param policy The policy.
	 */
	public void setLearnedSelectionPolicy(final SoftmaxPolicy policy)
	{
		learnedSelectionPolicy = policy;
	}
}
