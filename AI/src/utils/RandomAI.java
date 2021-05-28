package utils;

import java.util.concurrent.ThreadLocalRandom;

import game.Game;
import main.collections.FastArrayList;
import other.AI;
import other.context.Context;
import other.move.Move;

/**
 * AI player which selects actions uniformly at random.
 * 
 * @author Dennis Soemers
 */
public class RandomAI extends AI
{

	//-------------------------------------------------------------------------
	
	/** Our player index */
	protected int player = -1;
	
	/** The last move we returned */
	protected Move lastReturnedMove = null;
	
	//-------------------------------------------------------------------------

	/**
	 * Constructor
	 */
	public RandomAI()
	{
		friendlyName = "Random";
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
		FastArrayList<Move> legalMoves = game.moves(context).moves();

		if (!game.isAlternatingMoveGame())
			legalMoves = AIUtils.extractMovesForMover(legalMoves, player);

		final int r = ThreadLocalRandom.current().nextInt(legalMoves.size());
		final Move move = legalMoves.get(r);
		lastReturnedMove = move;
		return move;
	}
	
	/**
	 * @return The last move we returned
	 */
	public Move lastReturnedMove()
	{
		return lastReturnedMove;
	}
	
	@Override
	public void initAI(final Game game, final int playerID)
	{
		this.player = playerID;
		lastReturnedMove = null;
	}
	
	//-------------------------------------------------------------------------

}
