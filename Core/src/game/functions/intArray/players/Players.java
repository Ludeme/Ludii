package game.functions.intArray.players;

import annotations.Name;
import annotations.Opt;
import game.Game;
import game.functions.booleans.BooleanConstant;
import game.functions.booleans.BooleanFunction;
import game.functions.intArray.BaseIntArrayFunction;
import game.functions.intArray.IntArrayFunction;
import game.functions.intArray.players.many.PlayersMany;
import game.functions.intArray.players.team.PlayersTeam;
import game.functions.ints.IntFunction;
import other.context.Context;

/**
 * Returns an array of players indices.
 * 
 * @author Eric.Piette
 */
@SuppressWarnings("javadoc")
public final class Players extends BaseIntArrayFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * For returning the indices of the players in a team.
	 * 
	 * @param playerType The player type to return.
	 * @param If         The condition to keep the players [True].
	 * 
	 * @example (players Team1)
	 */
	public static IntArrayFunction construct
	(
		            final PlayersTeamType playerType,
		 @Opt @Name final BooleanFunction If
	)
	{
		return new PlayersTeam(playerType,If != null ? If : new BooleanConstant(true));
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * For returning the indices of players related to others.
	 * 
	 * @param playerType The player type to return.
	 * @param of         The index of the related player.
	 * @param If         The condition to keep the players.
	 * 
	 * @example (players Team1)
	 */
	public static IntArrayFunction construct
	(
		            final PlayersManyType playerType,
		 @Opt @Name final IntFunction     of,
		 @Opt @Name final BooleanFunction If
	)
	{
		return new PlayersMany(playerType,of,If != null ? If : new BooleanConstant(true));
	}

	private Players()
	{
		// Make grammar pick up construct() and not default constructor
	}

	//-------------------------------------------------------------------------

	@Override
	public int[] eval(final Context context)
	{
		// Should not be called, should only be called on subclasses
		throw new UnsupportedOperationException("Players.eval(): Should never be called directly.");
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		// Should never be there
		return false;
	}

	@Override
	public long gameFlags(final Game game)
	{
		// Should never be there
		return 0L;
	}
	
	@Override
	public void preprocess(final Game game)
	{
		// Nothing to do.
	}
}
