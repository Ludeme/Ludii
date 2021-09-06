package app.tutorialVisualisation;

import java.util.List;

import org.apache.commons.rng.core.RandomProviderDefaultState;

import game.Game;
import other.move.Move;
import other.trial.Trial;

/**
 * Object containing all necessary information to recreate (and compare) a specific move from a specific trial.
 * 
 * @author Matthew.Stephenson
 */
public class MoveCompleteInformation
{
	
	//-------------------------------------------------------------------------
	
	private final Trial trial;
	private final RandomProviderDefaultState rng;
	private final Move move;
	private final int moveIndex;
	private final int what;
	private final List<Move> similarMoves;
	private final String englishDescription;
	private String endingDescription = "Not Found";
	
	// Locations of generated gif/images
	private String gifLocation = "";
	private String screenshotA = "";
	private String screenshotB = "";
	
	//-------------------------------------------------------------------------
	
	MoveCompleteInformation(final Game game, final Trial trial, final RandomProviderDefaultState rng, final Move move, final int moveIndex, 
							final int what, final List<Move> similarMoves)
	{
		this.trial = trial == null ? null : new Trial(trial);
		this.rng = rng == null ? null : new RandomProviderDefaultState(rng.getState());
		this.move = move == null ? null : new Move(move);
		this.moveIndex = moveIndex;
		this.what = what;
		this.similarMoves = similarMoves;
		englishDescription = move.movesLudeme().toEnglish(game);
		
//		String combinedActionString = "";
//		for (final Action a : move.actions())
//			combinedActionString += new Move(a).movesLudeme().toEnglish(game) + ", ";
//		combinedActionString = combinedActionString.substring(0, combinedActionString.length()-2);
//		moveString = combinedActionString;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toString()
	{
		return move().toString().replaceAll("[^a-zA-Z0-9]", "") + "_what_" + what() + "_mover_" + move().mover();
	}

	Trial trial()
	{
		return trial;
	}

	RandomProviderDefaultState rng()
	{
		return rng;
	}

	Move move()
	{
		return move;
	}

	int moveIndex()
	{
		return moveIndex;
	}

	int what()
	{
		return what;
	}

	List<Move> similarMoves()
	{
		return similarMoves;
	}

	String englishDescription()
	{
		return englishDescription;
	}

	String endingDescription()
	{
		return endingDescription;
	}

	void setEndingDescription(String endingDescription)
	{
		this.endingDescription = endingDescription;
	}

	String gifLocation()
	{
		return gifLocation;
	}

	void setGifLocation(String gifLocation)
	{
		this.gifLocation = gifLocation;
	}

	String screenshotA()
	{
		return screenshotA;
	}

	void setScreenshotA(String screenshotA)
	{
		this.screenshotA = screenshotA;
	}

	String screenshotB()
	{
		return screenshotB;
	}

	void setScreenshotB(String screenshotB)
	{
		this.screenshotB = screenshotB;
	}
	
	//-------------------------------------------------------------------------
	
}
