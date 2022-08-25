package other.playout;

import java.util.concurrent.ThreadLocalRandom;

import game.Game;
import main.collections.FastArrayList;
import metadata.ai.heuristics.Heuristics;
import other.context.Context;
import other.move.Move;
import other.move.MoveScore;

/**
 * Heuristic-based playout move selector using Heuristic Sampling.
 *
 * @author Eric.Piette (based on code of Dennis Soemers and Cameron Browne)
 */
public class HeuristicSamplingMoveSelector extends PlayoutMoveSelector
{
	
	/** Big constant, probably bigger than any heuristic score */
	protected final float TERMINAL_SCORE_MULT = 100000.f;

	/** Score we give to winning opponents in paranoid searches in states where game is still going (> 2 players) */
	private static final float PARANOID_OPP_WIN_SCORE = 10000.f;
	private static final float WIN_SCORE = 10000.f;
	
	/** We skip computing heuristics with absolute weight value lower than this */
	public static final float ABS_HEURISTIC_WEIGHT_THRESHOLD = 0.01f;
	
	/** Denominator of heuristic threshold fraction, i.e. 1/2, 1/4, 1/8, etc. */
	private int fraction = 8;
	
	/** Whether to apply same-turn continuation. */
	private boolean continuation = true;
	
	/** Our heuristic value function estimator */
	private Heuristics heuristicValueFunction = null;

	/**
	 * Default constructor; will have to make sure to call setHeuristics() before using this,
	 * and also make sure that the heuristics are initialised.
	 */
	public HeuristicSamplingMoveSelector()
	{
	}
	
	/**
	 * Constructor with heuristic value function to use already passed in at construction time.
	 * This constructor will also ensure that the heuristic is initialised for the given game.
	 * 
	 * @param heuristicValueFunction The heuristics to use.
	 * @param game The game.
	 */
	public HeuristicSamplingMoveSelector(final Heuristics heuristicValueFunction, final Game game)
	{
		this.heuristicValueFunction = heuristicValueFunction;
		heuristicValueFunction.init(game);
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
		final MoveScore moveScore = evaluateMoves(context.game(), context);
		final Move move = moveScore.move();
		if (move == null)
			System.out.println("** No best move.");
		return move;
	}

	//-------------------------------------------------------------------------
	
	/**
	 * @param player The player.
	 * @param context The context.
	 * 
	 * @return Opponents of given player
	 */
	@SuppressWarnings("static-method")
	public int[] opponents(final int player, final Context context)
	{
		final int numPlayersInGame = context.game().players().count();
		final int[] opponents = new int[numPlayersInGame - 1];

		int idx = 0;
		
		if (context.game().requiresTeams())
		{
			final int tid = context.state().getTeam(player);
			for (int p = 1; p <= numPlayersInGame; p++)
				if (context.state().getTeam(p) != tid)
					opponents[idx++] = p;
		}
		else
		{
			for (int p = 1; p <= numPlayersInGame; ++p)
			{
				if (p != player)
					opponents[idx++] = p;
			}
		}
		
		return opponents;
	}

	//-------------------------------------------------------------------------
	
	/**
	 * @param game    Current game.
	 * @param context Current context.
	 * @param fraction  Number of moves to select.
	 * @return Randomly chosen subset of moves.
	 */
	public static FastArrayList<Move> selectMoves(final Game game, final Context context, final int fraction)
	{
		FastArrayList<Move> playerMoves   = game.moves(context).moves();
		FastArrayList<Move> selectedMoves = new FastArrayList<Move>();
	
		final int target = Math.max(2, (playerMoves.size() + 1) / fraction);
		
		if (target >= playerMoves.size()) 
			return playerMoves;
		
		while (selectedMoves.size() < target)
		{
			final int r = ThreadLocalRandom.current().nextInt(playerMoves.size());
			selectedMoves.add(playerMoves.get(r));
			playerMoves.remove(r);
		}
		
		return selectedMoves;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @param game The game.
	 * @param context The context.
	 * @return The score and the move.
	 */
	public MoveScore evaluateMoves(final Game game, final Context context)
	{
		FastArrayList<Move> moves = selectMoves(game, context, fraction);
		
		float bestScore = Float.NEGATIVE_INFINITY;
		Move bestMove = moves.get(0);
		
		final int mover = context.state().mover();
		
		//Context contextCurrent = context;
		
		for (Move move: moves) 
		{
			final Context contextCopy = new Context(context);
			game.apply(contextCopy, move);
			
			if (contextCopy.trial().status() != null) 
			{
				// Check if move is a winner
				final int winner = contextCopy.state().playerToAgent(contextCopy.trial().status().winner());
					
				if (winner == mover)
					return new MoveScore(move, WIN_SCORE);  // return winning move immediately
					
				if (winner != 0)
					continue;  // skip losing move
			}
			
			float score = 0;
			if (continuation && contextCopy.state().mover() == mover)
			{
				//System.out.println("Recursing...");
				return new MoveScore(move, evaluateMoves(game, contextCopy).score());
			}
			else
			{
				score = heuristicValueFunction.computeValue
						(
							contextCopy, mover, ABS_HEURISTIC_WEIGHT_THRESHOLD
						);
				for (final int opp : opponents(mover, context))
				{
					if (context.active(opp))
						score -= heuristicValueFunction.computeValue(contextCopy, opp, ABS_HEURISTIC_WEIGHT_THRESHOLD);
					else if (context.winners().contains(opp))
						score -= PARANOID_OPP_WIN_SCORE;
				}
				score += (float)(ThreadLocalRandom.current().nextInt(1000) / 1000000.0);
			}
		
			if (score > bestScore)
			{
				bestScore = score;
				bestMove  = move;
			}
		}
		
		return new MoveScore(bestMove, bestScore);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Heuristic function to use
	 */
	public Heuristics heuristicValueFunction()
	{
		return heuristicValueFunction;
	}

	/**
	 * Set the heuristics to use
	 * @param heuristics
	 */
	public void setHeuristics(final Heuristics heuristics)
	{
		this.heuristicValueFunction = heuristics;
	}

}
