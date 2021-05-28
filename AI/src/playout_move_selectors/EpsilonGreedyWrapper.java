package playout_move_selectors;

import java.util.concurrent.ThreadLocalRandom;

import main.collections.FastArrayList;
import other.context.Context;
import other.move.Move;
import other.playout.PlayoutMoveSelector;

/**
 * Epsilon-greedy wrapper around a Playout Move Selector
 *
 * @author Dennis Soemers
 */
public class EpsilonGreedyWrapper extends PlayoutMoveSelector
{
	
	//-------------------------------------------------------------------------
	
	/** The wrapped playout move selector */
	protected final PlayoutMoveSelector wrapped;
	
	/** Probability of picking uniformly at random */
	protected final double epsilon;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * @param wrapped
	 * @param epsilon
	 */
	public EpsilonGreedyWrapper(final PlayoutMoveSelector wrapped, final double epsilon)
	{
		this.wrapped = wrapped;
		this.epsilon = epsilon;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public Move selectMove
	(
		final Context context, 
		final FastArrayList<Move> maybeLegalMoves, 
		final int p,
		final IsMoveReallyLegal isMoveReallyLegal
	)
	{
		return wrapped.selectMove(context, maybeLegalMoves, p, isMoveReallyLegal);
	}
	
	@Override
	public boolean wantsPlayUniformRandomMove()
	{
		return ThreadLocalRandom.current().nextDouble() < epsilon;
	}
	
	//-------------------------------------------------------------------------

}
