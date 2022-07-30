package search.minimax;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

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
 * based on actions, with no exploration.
 * 
 * @author cyprien
 */

public class NaiveActionBasedSelection extends AI
{
	/** A learned policy to use in Selection phase */
	protected SoftmaxPolicy learnedSelectionPolicy = null;
	
	/** Current list of moves available in root */
	protected FastArrayList<Move> currentRootMoves = null;
	
	protected float selectionEpsilon = 0f;
	
	protected int selectActionNbCalls = 0;

	//-------------------------------------------------------------------------
	
	/**
	 * Constructor:
	 */
	public NaiveActionBasedSelection ()
	{
		super();
		friendlyName = "Naive Action Based Selection";
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
		currentRootMoves = new FastArrayList<Move>(game.moves(context).moves());
		
		Move selectedMove;		
		if ((learnedSelectionPolicy != null) && (ThreadLocalRandom.current().nextDouble(1.) < selectionEpsilon))
		{			
			final int numRootMoves = currentRootMoves.size();
			
			final List<ScoredMove> consideredMoveIndices = new ArrayList<ScoredMove>(numRootMoves);
			
			for (int i=0; i<numRootMoves; ++i)
			{
				final Move m = currentRootMoves.get(i);
				
				final float actionValue = learnedSelectionPolicy.computeLogit(context,m);
				
				consideredMoveIndices.add(new ScoredMove(m, actionValue, 1));
			}
			Collections.sort(consideredMoveIndices);
			
			selectedMove = consideredMoveIndices.get(0).move;
		}
		else
		{			
			final int r = ThreadLocalRandom.current().nextInt(currentRootMoves.size());
			final Move move = currentRootMoves.get(r);
			selectedMove = move;
		}
		
		selectActionNbCalls += 1;
		
		return selectedMove;
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
		if ((game.metadata().ai().features() != null) || (game.metadata().ai().trainedFeatureTrees() != null))
		{
			setLearnedSelectionPolicy(new SoftmaxFromMetadataSelection(0f));
			learnedSelectionPolicy.initAI(game, playerID);
		}
		
		selectActionNbCalls = 0;
		
		return;
	}

	@Override
	public boolean supportsGame(final Game game)
	{
		if (game.isStochasticGame())
			return false;
		
		if (game.hiddenInformation())
			return false;
		
		return ((game.metadata().ai().features() != null) || (game.metadata().ai().trainedFeatureTrees() != null));
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
	
	public void setSelectionEpsilon(final float eps)
	{
		selectionEpsilon = eps;
	}
	
	public int getSelectActionNbCalls()
	{
		return selectActionNbCalls;
	}
	
}
