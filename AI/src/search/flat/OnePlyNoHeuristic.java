package search.flat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import game.Game;
import main.collections.FastArrayList;
import other.AI;
import other.RankUtils;
import other.context.Context;
import other.move.Move;

/**
 * One-ply search with no heuristics (only optimises for best ranking achievable
 * in a single move, with random tie-breaking). For stochastic games, only randomly
 * considers one outcome for every move.
 * 
 * @author Dennis Soemers
 */
public class OnePlyNoHeuristic extends AI
{
	
	//-------------------------------------------------------------------------

	/** The number of players in the game we're currently playing */
	protected int numPlayersInGame = 0;
		
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 */
	public OnePlyNoHeuristic()
	{
		this.friendlyName = "One-Ply (No Heuristic)";
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
		final FastArrayList<Move> legalMoves = game.moves(context).moves();
		final int agent = context.state().playerToAgent(context.state().mover());
		
		double bestScore = Double.NEGATIVE_INFINITY;
		final List<Move> bestMoves = new ArrayList<Move>();
		
		final double utilLowerBound = RankUtils.rankToUtil(context.computeNextLossRank(), numPlayersInGame);
		final double utilUpperBound = RankUtils.rankToUtil(context.computeNextWinRank(), numPlayersInGame);
		
		for (final Move move : legalMoves)
		{
			game.apply(context, move);
			final int player = context.state().currentPlayerOrder(agent);
			
			final double score;
			if (context.active(player))
			{
				// Still active, so just assume average between lower and upper bound
				score = (utilLowerBound + utilUpperBound) / 2.0;
			}
			else
			{
				// Not active, so take actual utility
				score = RankUtils.rankToUtil(context.trial().ranking()[player], numPlayersInGame);
			}
			
			if (score > bestScore)
				bestMoves.clear();
			
			if (score >= bestScore)
			{
				bestMoves.add(move);
				bestScore = score;
			}
			
			game.undo(context);
		}

		return bestMoves.get(ThreadLocalRandom.current().nextInt(bestMoves.size()));
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public void initAI(final Game game, final int playerID)
	{
		numPlayersInGame = game.players().count();
	}
	
	@Override
	public boolean supportsGame(final Game game)
	{
		if (game.players().count() <= 1)
			return false;
		
//		if (game.isStochasticGame())
//			return false;
		
		if (game.hiddenInformation())
			return false;
		
		return game.isAlternatingMoveGame();
	}
	
	//-------------------------------------------------------------------------

}
