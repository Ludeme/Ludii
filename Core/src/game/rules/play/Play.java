package game.rules.play;

import java.io.Serializable;

import game.Game;
import game.rules.play.moves.Moves;
import other.BaseLudeme;

/**
 * Checks the playing rules of the game.
 * 
 * @author cambolbro and Eric.Piette
 */
public final class Play extends BaseLudeme implements Serializable
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The playing moves. */
	private final Moves moves;
	
	//-------------------------------------------------------------------------

	/**
	 * The playing rules of the game.
	 * 
	 * @param moves The legal moves of the playing rules.
	 * @example (play (forEach Piece))
	 */
	public Play
	(
		final Moves moves
	)
	{
		this.moves = moves;
	}

	//-------------------------------------------------------------------------

	/**
	 * @return The moves.
	 */
	public Moves moves()
	{
		return moves;
	}
		
	//-------------------------------------------------------------------------
	// Ludeme overrides
	
	@Override
	public String toEnglish(final Game game)
	{
		//return "<Play>";
		return moves.toEnglish(game);
	}

	//-------------------------------------------------------------------------

}
