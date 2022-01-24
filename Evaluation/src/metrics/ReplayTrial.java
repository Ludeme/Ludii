package metrics;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.rng.RandomProviderState;

import game.Game;
import other.context.Context;
import other.move.Move;
import other.trial.Trial;

/**
 * Contains trial, and any other information needed to robustly replay the game.
 *
 * @author matthew.stephenson
 */
public class ReplayTrial
{
	private final Trial trial;
	private final List<Move> fullMoves;
	
	public ReplayTrial(final Game game, final Trial trial, final RandomProviderState rng)
	{
		this.trial = trial;
		final List<Move> movesToReturn = new ArrayList<>();
		final Context newContext = Utils.setupNewContext(game, rng);
		
		for (final Move m : trial.generateRealMovesList())
		{
			final Move matchingMove = game.getMatchingLegalMove(newContext, m);
			movesToReturn.add(matchingMove);
			game.applyRobust(newContext, matchingMove);
		}
			
		this.fullMoves = movesToReturn;
	}

	public List<Move> fullMoves()
	{
		return fullMoves;
	}
	
	public Move getMove(final int i)
	{
		return fullMoves.get(i);
	}

	public Trial trial()
	{
		return trial;
	}
	
	
}