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

/**
 * Heuristic-based playout move selector
 *
 * @author Dennis Soemers
 */
public class HeuristicMoveSelector extends PlayoutMoveSelector
{
	
	/** Big constant, probably bigger than any heuristic score */
	protected final float TERMINAL_SCORE_MULT = 100000.f;

	/** Heuristic function to use */
	protected Heuristics heuristicValueFunction = null;
	
	/**
	 * Default constructor; will have to make sure to call setHeuristics() before using this,
	 * and also make sure that the heuristics are initialised.
	 */
	public HeuristicMoveSelector()
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
	public HeuristicMoveSelector(final Heuristics heuristicValueFunction, final Game game)
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

}
