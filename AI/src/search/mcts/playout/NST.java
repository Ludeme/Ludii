package search.mcts.playout;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
import search.mcts.MCTS.NGramMoveKey;
import search.mcts.backpropagation.BackpropagationStrategy;

/**
 * N-gram Selection Technique playouts
 *
 * @author Dennis Soemers
 */
public class NST implements PlayoutStrategy
{
	
	//-------------------------------------------------------------------------
	
	/** Auto-end playouts in a draw if they take more turns than this */
	protected int playoutTurnLimit = -1;
	
	/** For epsilon-greedy move selection */
	protected double epsilon = 0.1;
	
	/** For every thread, an NST-based PlayoutMoveSelector */
	protected ThreadLocal<NSTMoveSelector> moveSelector = ThreadLocal.withInitial(() -> new NSTMoveSelector());
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 */
	public NST()
	{
		playoutTurnLimit = -1;	// no limit
	}
	
	/**
	 * Constructor
	 * @param playoutTurnLimit
	 * @param epsilon
	 */
	public NST(final int playoutTurnLimit, final double epsilon)
	{
		this.playoutTurnLimit = playoutTurnLimit;
		this.epsilon = epsilon;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public Trial runPlayout(final MCTS mcts, final Context context)
	{
		final NSTMoveSelector nst = moveSelector.get();
		nst.mcts = mcts;
		final Trial trial =
				context.game().playout(context, null, 1.0, new EpsilonGreedyWrapper(nst, epsilon), -1, playoutTurnLimit, ThreadLocalRandom.current());
		nst.mcts = null;
		return trial;
	}
	
	@Override
	public int backpropFlags()
	{
		return BackpropagationStrategy.GLOBAL_NGRAM_ACTION_STATS | BackpropagationStrategy.GLOBAL_ACTION_STATS;
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
	 * Playout Move Selector for NST (NOTE: this one is just greedy, need
	 * to put an epsilon-greedy wrapper around it for epsilon-greedy behaviour).
	 *
	 * @author Dennis Soemers
	 */
	protected static class NSTMoveSelector extends PlayoutMoveSelector
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
			final int maxNGramLength = Math.min(mcts.maxNGramLength(), context.trial().numberRealMoves() + 1);
			
			for (int i = 0; i < maybeLegalMoves.size(); ++i)
			{
				int numNGramsConsidered = 0;
				float scoresSum = 0.f;
				
				// Start with "N-grams" for N = 1
				final ActionStatistics actionStats = mcts.getOrCreateActionStatsEntry(
						new MoveKey(maybeLegalMoves.get(i), context.trial().numMoves()));
				
				++numNGramsConsidered;
				if (actionStats.visitCount > 0.0)
					scoresSum += (float) (actionStats.accumulatedScore / actionStats.visitCount);
				else
					scoresSum += 1.f;
				
				// Now N-grams for N > 1
				final List<Move> reverseActionSequence = new ArrayList<Move>();
				reverseActionSequence.add(maybeLegalMoves.get(i));
				final Iterator<Move> reverseTrialIterator = context.trial().reverseMoveIterator();

				for (int n = 2; n <= maxNGramLength; ++n)
				{
					reverseActionSequence.add(reverseTrialIterator.next());
					final Move[] nGram = new Move[n];
					
					for (int j = 0; j < n; ++j)
					{
						nGram[j] = reverseActionSequence.get(n - j - 1);
					}
					
					final ActionStatistics nGramStats = 
							mcts.getNGramActionStatsEntry
							(
								new NGramMoveKey
								(
									nGram, 
									context.trial().numberRealMoves() - n + 1
								)
							);
					
					if (nGramStats == null || nGramStats.visitCount <= 0)
						break;
					
					++numNGramsConsidered;
					scoresSum += (float) (nGramStats.accumulatedScore / nGramStats.visitCount);
				}
				
				actionScores.set(i, scoresSum / numNGramsConsidered);
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
