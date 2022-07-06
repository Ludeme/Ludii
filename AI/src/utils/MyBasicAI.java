package utils;

import game.Game;
import main.collections.FastArrayList;
import other.AI;
import other.context.Context;
import other.move.Move;

/**
 * AI player which always select the last proposed action
 * 
 * @author cyprien
 */
public class MyBasicAI extends AI
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
	public MyBasicAI()
	{
		friendlyName = "Bob the Basic AI";
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

		final Move move = legalMoves.get(legalMoves.size()-1);
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
