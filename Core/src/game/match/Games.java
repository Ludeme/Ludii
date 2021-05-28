package game.match;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import annotations.Or;
import other.BaseLudeme;

/**
 * Defines the games used in a match.
 * 
 * @author Eric.Piette
 */
public class Games extends BaseLudeme implements Serializable
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * The games used in the match.
	 */
	final List<Subgame> games;

	//-------------------------------------------------------------------------
	
	/**
	 * @param game  The game that makes up the subgames of the match.
	 * @param games The games that make up the subgames of the match.
	 * 
	 * @example (games { (subgame "Tic-Tac-Toe" next:1) (subgame "Yavalath" next:2)
	 *          (subgame "Breakthrough" next:0) })
	 */
	public Games
	(
		@Or final Subgame   game,
		@Or final Subgame[] games
	)
	{
		int numNonNull = 0;
		if (game != null)
			numNonNull++;
		if (games != null)
			numNonNull++;

		if (numNonNull != 1)
			throw new IllegalArgumentException("Exactly one Or parameter must be non-null.");

		if (game != null)
		{
			this.games = new ArrayList<Subgame>();
			this.games.add(game);
		}
		else
		{	
			if (games.length < 1)
				throw new IllegalArgumentException("A match needs at least one game.");

			this.games = new ArrayList<Subgame>();
			for (final Subgame subGame : games)
				this.games.add(subGame);
		}
	}

	//-------------------------------------------------------------------------

	/**
	 * @return the games.
	 */
	public List<Subgame> games()
	{
		return this.games;
	}

	//-------------------------------------------------------------------------

}
