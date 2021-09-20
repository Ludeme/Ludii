package manualGeneration;

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
	private final String pieceName;
	private final List<Move> similarMoves;
	private final String englishDescription;
	private String endingDescription = "Not Found";
	
	// Locations of generated gif/images
	private String gifLocation = "";
	private String screenshotA = "";
	private String screenshotB = "";
	
	//-------------------------------------------------------------------------
	
	MoveCompleteInformation(final Game game, final Trial trial, final RandomProviderDefaultState rng, final Move move, final int moveIndex, 
							final String pieceName, final List<Move> similarMoves)
	{
		this.trial = trial == null ? null : new Trial(trial);
		this.rng = rng == null ? null : new RandomProviderDefaultState(rng.getState());
		this.move = move == null ? null : new Move(move);
		this.moveIndex = moveIndex;
		this.pieceName = pieceName;
		this.similarMoves = similarMoves;
		englishDescription = move.movesLudeme() == null ? "Not Found" : move.movesLudeme().toEnglish(game);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toString()
	{
		return move().toString().replaceAll("[^a-zA-Z0-9]", "") + "_piece_" + pieceName() + "_mover_" + move().mover();
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

	String pieceName()
	{
		return pieceName;
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
