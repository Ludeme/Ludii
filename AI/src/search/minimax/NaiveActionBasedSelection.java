package search.minimax;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import game.Game;
import main.collections.FastArrayList;
import other.AI;
import other.context.Context;
import other.move.Move;
import policies.softmax.SoftmaxFromMetadataSelection;
import policies.softmax.SoftmaxPolicy;
import utils.data_structures.ScoredMove;

/** 
 * Naive AI that just picks the most promising action according to its learned selection policy 
 * base on actions, with no exploration.
 * 
 * @author cyprien
 */

public class NaiveActionBasedSelection extends AI
{
	/** A learned policy to use in Selection phase */
	protected SoftmaxPolicy learnedSelectionPolicy = null;
	
	/** Current list of moves available in root */
	protected FastArrayList<Move> currentRootMoves = null;

	//-------------------------------------------------------------------------
	
	/**
	 * Constructor:
	 */
	public NaiveActionBasedSelection ()
	{
		super();
		setLearnedSelectionPolicy(new SoftmaxFromMetadataSelection(0f));
		friendlyName = "Biased UBFM";
	}

	//-------------------------------------------------------------------------
	
	public Move selectAction
	(
		final Game game, 
		final Context context, 
		final double maxSeconds,
		final int maxIterations,
		final int maxDepth
	)
	{
		currentRootMoves = new FastArrayList<Move>(game.moves(context).moves());
		
		final int numRootMoves = currentRootMoves.size();
		
		final List<ScoredMove> consideredMoveIndices = new ArrayList<ScoredMove>(numRootMoves);
		
		for (int i=0; i<numRootMoves; ++i)
		{
			final Move m = currentRootMoves.get(i);
			
			final float actionValue = (float) learnedSelectionPolicy.computeLogit(context,m);
			
			consideredMoveIndices.add(new ScoredMove(m,actionValue,1));
		};
		Collections.sort(consideredMoveIndices);
		
		return consideredMoveIndices.get(0).move;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Initialising the AI (almost the same as with AlphaBeta)
	 */
	@Override
	public void initAI(final Game game, final int playerID)
	{
		currentRootMoves = null;
		
		// Instantiate feature sets for selection policy
		if (learnedSelectionPolicy != null)
			learnedSelectionPolicy.initAI(game, playerID);
		
		return;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Sets the learned policy to use in Selection phase
	 * @param policy The policy.
	 */
	public void setLearnedSelectionPolicy(final SoftmaxPolicy policy)
	{
		learnedSelectionPolicy = policy;
	}
	
}
