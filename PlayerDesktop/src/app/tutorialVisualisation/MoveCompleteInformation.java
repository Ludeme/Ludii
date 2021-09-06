package app.tutorialVisualisation;

import java.util.List;

import org.apache.commons.rng.core.RandomProviderDefaultState;

import game.Game;
import other.move.Move;
import other.trial.Trial;

/**
 * All necessary information to recreate (and compare) a specific move from a specific trial.
 * 
 * @author Matthew.Stephenson
 */
public class MoveCompleteInformation
{
	final Trial trial;
	final RandomProviderDefaultState rng;
	final Move move;
	final int moveIndex;
	final int what;
	final List<Move> similarMoves;
	final String moveString;
	String endingString = "Not Found";
	
	// Locations of generated gif/images
	String gifLocation = "";
	String screenshotA = "";
	String screenshotB = "";
	
	MoveCompleteInformation(final Game game, final Trial trial, final RandomProviderDefaultState rng, final Move move, final int moveIndex, 
							final int what, final List<Move> similarMoves)
	{
		this.trial = trial == null ? null : new Trial(trial);
		this.rng = rng == null ? null : new RandomProviderDefaultState(rng.getState());
		this.move = move == null ? null : new Move(move);
		this.moveIndex = moveIndex;
		this.what = what;
		this.similarMoves = similarMoves;
		moveString = move.movesLudeme().toEnglish(game);
	}
	
	@Override
	public String toString()
	{
		return move.toString().replaceAll("[^a-zA-Z0-9]", "") + "_what_" + what + "_mover_" + move.mover();
	}
}
