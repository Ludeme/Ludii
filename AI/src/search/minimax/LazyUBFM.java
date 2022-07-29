package search.minimax;

import game.Game;
import gnu.trove.list.array.TLongArrayList;
import main.collections.FVector;
import main.collections.FastArrayList;
import metadata.ai.heuristics.Heuristics;
import other.context.Context;
import other.move.Move;
import other.state.State;
import policies.softmax.SoftmaxFromMetadataSelection;
import policies.softmax.SoftmaxPolicy;

/**
 * AI based on Unbounded Best-First Search, using trained action evaluations to complete the heuristic scores.
 * [...]
 * 
 * @author cyprien
 */

public class LazyUBFM extends UBFM
{
	
	/** Weight of the action evaluation when linearly combined with the heuristic score */
	private static float actionEvaluationWeight = 0.5f;
	
	//-------------------------------------------------------------------------

	/** A learned policy to use in for the action evaluation */
	protected SoftmaxPolicy learnedSelectionPolicy = null; 
	
	/** A boolean to know if it is the first turn the AI is playing on this game. If so, it will just use
	 *  a basic UBFM approach to have an idea of the heuristics range. */
	boolean firstTurn;
	
	/** Different fields to have an idea of how to combine action evaluations and heuristic scores properly */
	float estimatedHeuristicScoresRange;
	float maxActionLogit = Float.NEGATIVE_INFINITY;
	float minActionLogit = Float.POSITIVE_INFINITY;
	float estimatedActionLogitRange;
	float actionLogitSum;
	float actionLogitComputations;
	float estimatedActionLogitMean;
	
	/** For the AI visualisation data: */
	float maxRegisteredValue;
	float minRegisteredValue;	
	
	//-------------------------------------------------------------------------
	
	public static LazyUBFM createLazyUBFM ()
	{
		return new LazyUBFM();
	}
	
	/**
	 * Constructor:
	 */
	public LazyUBFM ()
	{
		super();
		setLearnedSelectionPolicy(new SoftmaxFromMetadataSelection(0f));
		friendlyName = "Lazy UBFM";
		return;
	}
	
	/**
	 * Constructor
	 * @param heuristics
	 */
	public LazyUBFM(final Heuristics heuristics)
	{
		super(heuristics);
		setLearnedSelectionPolicy(new SoftmaxFromMetadataSelection(0f));
		friendlyName = "Lazy UBFM";
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
		
		final Move bestMove = super.selectAction(game, context, maxSeconds, maxIterations, maxDepth);
		// super.selectAction will call this class's own estimateMovesValues
		
		firstTurn = false;
		
		estimatedHeuristicScoresRange = maxHeuristicEval - minHeuristicEval;
		estimatedActionLogitRange = maxActionLogit - minActionLogit;
		estimatedActionLogitMean = actionLogitSum / actionLogitComputations;
		
		return bestMove;
	}
	
	@Override
	protected FVector estimateMovesValues
	(
		final FastArrayList<Move> legalMoves,
		final Context context,
		final int maximisingPlayer,
		final TLongArrayList nodeHashes,
		final int depth,
		final long stopTime
	)
	{
		final State state = context.state();
		final int mover = state.playerToAgent(state.mover());
		
		final float heuristicScore = getContextValue(context, maximisingPlayer, nodeHashes, mover);
		
		if (savingSearchTreeDescription)
			searchTreeOutput.append("("+stringOfNodeHashes(nodeHashes)+","+Float.toString(heuristicScore)+","+((mover==maximisingPlayer)? 1:2)+"),\n");
		
		final int numLegalMoves = legalMoves.size();
		final FVector moveScores = new FVector(numLegalMoves);
		
		// Computing action scores (stored in moveScores)
		for (int i = 0; i < numLegalMoves; ++i)
		{
			final Move m = legalMoves.get(i);
			
			final float actionValue = (float) learnedSelectionPolicy.computeLogit(context, m);
			
			actionLogitSum += actionValue;
			actionLogitComputations += 1;
			maxActionLogit = Math.max(actionValue, maxActionLogit);
			minActionLogit = Math.min(actionValue, minActionLogit);
			
			moveScores.set(i, actionValue);
		};
		
		if (firstTurn)
		{
			// Uses the classical UBFM approach on the first turn.
			final FVector res = super.estimateMovesValues(legalMoves, context, maximisingPlayer, nodeHashes, depth, stopTime);
		
			return res;
		}
		else
		{
			final int sign = (maximisingPlayer == mover)? 1 : -1 ;
				
			for (int i=0; i<numLegalMoves; i++)
			{
				double r = 1;
				if (debugDisplay)
				{
					r = Math.random(); // just for occasional display
					if (r<0.05)
						System.out.printf("action score is %.6g and heuristicScore is %.6g ",moveScores.get(i),heuristicScore);
				}
				
				// (*2 because the maximal gap with the mean is about half of the range)
				float actionScore = (actionEvaluationWeight * (moveScores.get(i)-estimatedActionLogitMean) * sign * estimatedHeuristicScoresRange * 2)  /  estimatedActionLogitRange;
				
				moveScores.set(i, heuristicScore + actionScore);
				
				maxRegisteredValue = Math.max(heuristicScore + actionScore, maxRegisteredValue);
				minRegisteredValue = Math.min(heuristicScore + actionScore, minRegisteredValue);
				
				if (debugDisplay)
					if (r<0.05)
						System.out.printf("-> eval is %.6g\n",moveScores.get(i));
			}
		
			return moveScores;
		}
	}

	//-------------------------------------------------------------------------
	
	public void initAI(final Game game, final int playerID)
	{
		super.initAI(game, playerID);
		
		// Instantiate feature sets for selection policy
		if (learnedSelectionPolicy != null)
			learnedSelectionPolicy.initAI(game, playerID);
		
		firstTurn = true;
		actionLogitComputations = 0;
		actionLogitSum = 0;
		maxActionLogit = Float.NEGATIVE_INFINITY;
		minActionLogit = Float.POSITIVE_INFINITY;
		maxRegisteredValue = Float.NEGATIVE_INFINITY;
		minRegisteredValue = Float.POSITIVE_INFINITY;
		
		return;
	}
	
	@Override
	public boolean supportsGame(final Game game)
	{
		if (game.isStochasticGame())
			return false;
		
		if (game.hiddenInformation())
			return false;
		
		if (game.hasSubgames())		// Cant properly init most heuristics
			return false;
		
		if (!(game.isAlternatingMoveGame()))
			return false;
		
		return ((game.metadata().ai().features() != null) || (game.metadata().ai().trainedFeatureTrees() != null));
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
	@Override
	public double scoreToValueEst(final float score, final float alpha, final float beta)
	{
		if (score <= alpha+10)
			return -1.0;
		
		if (score >= beta-10)
			return 1.0;
		
		minRegisteredValue = Math.min(minRegisteredValue, minHeuristicEval);
		maxRegisteredValue = Math.max(maxRegisteredValue, maxHeuristicEval);

		// Map to range [-0.8, 0.8] based on most extreme heuristic evaluations
		// observed so far.
		return -0.8 + (0.8 - -0.8) * ((score - minRegisteredValue) / (maxRegisteredValue - minRegisteredValue));
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
	
	/**
	 * Sets the weight of the action evaluation in the context evaluations.
	 * @param value the weight
	 */
	public void setActionEvaluationWeight(final float value)
	{
		actionEvaluationWeight = value;
	}

}
