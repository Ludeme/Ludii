package search.mcts.playout;

import java.util.concurrent.ThreadLocalRandom;

import game.Game;
import main.collections.FVector;
import main.collections.FastArrayList;
import other.context.Context;
import other.move.Move;
import other.playout.PlayoutMoveSelector;
import other.trial.Trial;
import playout_move_selectors.EpsilonGreedyWrapper;
import search.mcts.MCTS;
import search.mcts.MCTS.ActionStatistics;
import search.mcts.MCTS.MoveKey;
import search.mcts.backpropagation.BackpropagationStrategy;

/**
 * Move-Average Sampling Technique (MAST) playout strategy (epsilon-greedy)
 *
 * @author Dennis Soemers
 */
public class MAST implements PlayoutStrategy
{
	
	//-------------------------------------------------------------------------
	
	/** Auto-end playouts in a draw if they take more turns than this */
	protected int playoutTurnLimit = -1;
	
	/** For epsilon-greedy move selection */
	protected double epsilon = 0.1;
	
	/** For every thread, a MAST-based PlayoutMoveSelector */
	protected ThreadLocal<MASTMoveSelector> moveSelector = ThreadLocal.withInitial(() -> new MASTMoveSelector());
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 */
	public MAST()
	{
		playoutTurnLimit = -1;	// no limit
	}
	
	/**
	 * Constructor
	 * @param playoutTurnLimit
	 * @param epsilon
	 */
	public MAST(final int playoutTurnLimit, final double epsilon)
	{
		this.playoutTurnLimit = playoutTurnLimit;
		this.epsilon = epsilon;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public Trial runPlayout(final MCTS mcts, final Context context)
	{
		final MASTMoveSelector mast = moveSelector.get();
		mast.mcts = mcts;
		return context.game().playout(context, null, 1.0, new EpsilonGreedyWrapper(mast, epsilon), -1, playoutTurnLimit, ThreadLocalRandom.current());
	}
	
	@Override
	public int backpropFlags()
	{
		return BackpropagationStrategy.GLOBAL_ACTION_STATS;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public boolean playoutSupportsGame(final Game game)
	{
		if (game.isDeductionPuzzle())
			return (playoutTurnLimit() > 0);
		else
			return true;
	}

	@Override
	public void customise(final String[] inputs)
	{
		for (int i = 1; i < inputs.length; ++i)
		{
			final String input = inputs[i];
			
			if (input.toLowerCase().startsWith("playoutturnlimit="))
			{
				playoutTurnLimit = 
						Integer.parseInt
						(
							input.substring("playoutturnlimit=".length())
						);
			}
		}
	}
	
	/**
	 * @return The turn limit we use in playouts
	 */
	public int playoutTurnLimit()
	{
		return playoutTurnLimit;
	}

	//-------------------------------------------------------------------------
	
	/**
	 * Playout Move Selector for MAST (NOTE: this one is just greedy, need
	 * to put an epsilon-greedy wrapper around it for epsilon-greedy behaviour).
	 *
	 * @author Dennis Soemers
	 */
	protected static class MASTMoveSelector extends PlayoutMoveSelector
	{
		
		/** MCTS from which to get our global action stats */
		protected MCTS mcts = null;

		@Override
		public Move selectMove
		(
			final Context context,
			final FastArrayList<Move> maybeLegalMoves,
			final int p,
			final IsMoveReallyLegal isMoveReallyLegal
		)
		{
			final FVector actionScores = new FVector(maybeLegalMoves.size());
			for (int i = 0; i < maybeLegalMoves.size(); ++i)
			{
				final ActionStatistics actionStats = mcts.getOrCreateActionStatsEntry(
						new MoveKey(maybeLegalMoves.get(i), context.trial().numMoves()));
				
				if (actionStats.visitCount > 0.0)
					actionScores.set(i, (float) (actionStats.accumulatedScore / actionStats.visitCount));
				else
					actionScores.set(i, 1.f);
			}
			
			int numLegalMoves = maybeLegalMoves.size();
		
			while (numLegalMoves > 0)
			{
				--numLegalMoves;	// We're trying a move; if this one fails, it's actually not legal
				
				final int n = actionScores.argMaxRand();
				final Move move = maybeLegalMoves.get(n);
				
				if (isMoveReallyLegal.checkMove(move))
					return move;	// Only return this move if it's really legal
				else
					actionScores.set(n, Float.NEGATIVE_INFINITY);	// Illegal action
			}
			
			// No legal moves?
			return null;
		}
		
	}
	
	//-------------------------------------------------------------------------

}
