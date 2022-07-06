package search.minimax;

import java.util.ArrayList;

import java.util.Collections;
import java.util.List;

import game.Game;
import main.collections.FVector;
import main.collections.FastArrayList;
import other.context.Context;
import other.move.Move;
import other.state.State;
import policies.softmax.SoftmaxFromMetadataSelection;
import policies.softmax.SoftmaxPolicy;
import utils.data_structures.ScoredIndex;

/**
 * AI based on Unbounded Best-First Minimax, which uses the action evaluation to select a small number 
 * of actions that will really be simulated. If epsilon != 0, then any other move can still be randomly 
 * picked for exploration.
 * 
 * @author cyprien
 */

public class BiasedUBFM extends UBFM
{
	
	/** Number of moves that are really evaluated with the heuristics at each step of the exploration. */
	private int nbStateEvaluationsPerNode = 3;
	
	//-------------------------------------------------------------------------
	
	/** An epsilon parameter to give to the selection policy which hopefully is not chaging anything*/
	private final float epsilon = 0f;

	/** A learned policy to use in Selection phase */
	protected SoftmaxPolicy learnedSelectionPolicy = null;

	//-------------------------------------------------------------------------

	public static BiasedUBFM createBiasedBFS ()
	{
		return new BiasedUBFM();
	}
	
	/**
	 * Constructor:
	 */
	public BiasedUBFM ()
	{
		super();
		setLearnedSelectionPolicy(new SoftmaxFromMetadataSelection(epsilon));
		friendlyName = "Biased UBFM";
		return;
	}
	
	//-------------------------------------------------------------------------

	@Override
	/**
	 * In this variant only the most promising actions are really evaluated, 
	 * the others are given a score of -"infinity" + 1 (or the opposite 
	 * if mover is not maximising_player).
	 * 
	 * @param legalMoves
	 * @param context
	 * @param maximisingPlayer
	 * @param nodeHashes
	 * @param depth
	 * @param stopTime
	 * @return a vector with the scores for each moves.
	 */
	protected FVector estimateMoveValues
	(
		final FastArrayList<Move> legalMoves,
		final Context context,
		final int maximisingPlayer,
		final List<Long> nodeHashes,
		final int depth,
		final long stopTime
	)
	{
		final int numLegalMoves = legalMoves.size();
		final Game game = context.game();
		final State state = context.state();
		final int mover = state.playerToAgent(state.mover());
		
		final List<ScoredIndex> consideredMoveIndices = new ArrayList<ScoredIndex>(numLegalMoves);
		
		for (int i = 0; i < numLegalMoves; ++i)
		{
			final Move m = legalMoves.get(i);
			
			final float actionValue = (float) learnedSelectionPolicy.computeLogit(context,m);
			
			consideredMoveIndices.add( new ScoredIndex(i,actionValue) );
		};
		Collections.sort(consideredMoveIndices);

		final FVector moveScores = new FVector(numLegalMoves);
		for (int i = 0; i < numLegalMoves; ++i)
		{
			// filling default score for each moves:
			moveScores.set(i,(mover==maximisingPlayer)? -BETA_INIT+1: BETA_INIT-1);
		}
		
		for (int k = 0; k < Math.min(nbStateEvaluationsPerNode, numLegalMoves); k++)
		{
			final int i = consideredMoveIndices.get(k).index;
			
			final Move m = legalMoves.get(i);
			final Context contextCopy = copyContext(context);
			
			game.apply(contextCopy, m);

			nodeHashes.add(contextCopy.state().fullHash());
			final float heuristicScore = getContextValue(contextCopy,maximisingPlayer,nodeHashes,depth);
			nodeHashes.remove(nodeHashes.size()-1);
			
			moveScores.set(i,heuristicScore);
			
			if (System.currentTimeMillis() >= stopTime || ( wantsInterrupt))
			{
				for (int j=k+1; j<Math.min(nbStateEvaluationsPerNode, numLegalMoves); j++)
					moveScores.set(consideredMoveIndices.get(k).index, mover==maximisingPlayer? -BETA_INIT + 1 : BETA_INIT-1);
				break;
			}
		};
		
		return moveScores;
	}
	
	//-------------------------------------------------------------------------
	
	public void initAI(final Game game, final int playerID)
	{
		super.initAI(game,  playerID);
		
		// Instantiate feature sets for selection policy
		if (learnedSelectionPolicy != null)
			learnedSelectionPolicy.initAI(game, playerID);
		
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
	
	/**
	 * Sets the number of moves that will be really evaluated with a simulation and a call to the heuristicValue function.
	 * @param value
	 */
	public void setNbStateEvaluationsPerNode(final int value)
	{
		nbStateEvaluationsPerNode = value;
	}
}
