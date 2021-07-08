package other.playout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import game.Game;
import main.collections.FastArrayList;
import metadata.ai.heuristics.Heuristics;
import other.RankUtils;
import other.context.Context;
import other.context.TempContext;
import other.move.Move;
import other.move.MoveScore;

/**
 * Heuristic-based playout move selector
 *
 * @author Dennis Soemers
 */
public class HeuristicSamplingMoveSelector extends PlayoutMoveSelector
{
	
	/** Big constant, probably bigger than any heuristic score */
	protected final float TERMINAL_SCORE_MULT = 100000.f;

	/** 
	 * Auto-end playouts in a draw if they take more turns than this, Negative value means
	 * no limit.
	 * 
	 * TODO if we have heuristics anyway, might make sense to use them for a non-draw eval..
	 */	
	protected int playoutTurnLimit = -1;
	
	/** Heuristic-based PlayoutMoveSelector */
	protected HeuristicSamplingMoveSelector moveSelector = new HeuristicSamplingMoveSelector();
	
	/** Score we give to winning opponents in paranoid searches in states where game is still going (> 2 players) */
	private static final float PARANOID_OPP_WIN_SCORE = 10000.f;
	private static final float WIN_SCORE = 10000.f;
	
	/** We skip computing heuristics with absolute weight value lower than this */
	public static final float ABS_HEURISTIC_WEIGHT_THRESHOLD = 0.01f;
	
	/** The number of players in the game we're currently playing */
	protected int numPlayersInGame = 0;
	
	/** Denominator of heuristic threshold fraction, i.e. 1/2, 1/4, 1/8, etc. */
	private int fraction = 2;
	
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
		// Do nothing
	}
	
	/**
	 * Constructor with heuristic value function to use already passed in at construction time.
	 * This constructor will also ensure that the heuristic is initialised for the given game.
	 * 
	 * @param heuristicValueFunction
	 * @param game
	 */
	public HeuristicSamplingMoveSelector(final Heuristics heuristicValueFunction, final Game game)
	{
		this.heuristicValueFunction = heuristicValueFunction;
		heuristicValueFunction.init(game);
	}

	@Override
	public Move selectMove
	(
		final Context context,
		final FastArrayList<Move> maybeLegalMoves,
		final int p,
		final IsMoveReallyLegal isMoveReallyLegal
	)
	{
		final Game game = context.game();
		final List<Move> bestMoves = new ArrayList<Move>();
		float bestValue = Float.NEGATIVE_INFINITY;

		// boolean foundLegalMove = false;
		for (final Move move : maybeLegalMoves)
		{
			if (isMoveReallyLegal.checkMove(move))
			{
				// foundLegalMove = true;
				final TempContext copyContext = new TempContext(context);
				game.apply(copyContext, move);

				float heuristicScore = 0.f;

				if (copyContext.trial().over() || !copyContext.active(p))
				{
					// terminal node (at least for maximising player)
					heuristicScore = (float) RankUtils.agentUtilities(copyContext)[p] * TERMINAL_SCORE_MULT;
				}
				else
				{
					for (int player = 1; player <= game.players().count(); ++player)
					{
						if (copyContext.active(player))
						{
							final float playerScore = heuristicValueFunction.computeValue(copyContext, player, 0.f);

							if (player == p)
							{
								// Need to add this score
								heuristicScore += playerScore;
							}
							else
							{
								// Need to subtract
								heuristicScore -= playerScore;
							}
						}

					}
				}

				if (heuristicScore > bestValue)
				{
					bestValue = heuristicScore;
					bestMoves.clear();
					bestMoves.add(move);
				}
				else if (heuristicScore == bestValue)
				{
					bestMoves.add(move);
				}
			}
		}

		if (bestMoves.size() > 0)
			return bestMoves.get(ThreadLocalRandom.current().nextInt(bestMoves.size()));
		else
			return null;
	}

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
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param player
	 * @return Opponents of given player
	 */
	public int[] opponents(final int player)
	{
		final int[] opponents = new int[numPlayersInGame - 1];
		int idx = 0;
		
		for (int p = 1; p <= numPlayersInGame; ++p)
		{
			if (p != player)
				opponents[idx++] = p;
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

	MoveScore evaluateMoves(final Game game, final Context context)
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
				for (final int opp : opponents(mover))
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

}
