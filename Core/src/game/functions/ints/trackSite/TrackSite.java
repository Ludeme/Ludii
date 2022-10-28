package game.functions.ints.trackSite;

import annotations.Name;
import annotations.Opt;
import annotations.Or;
import game.Game;
import game.functions.booleans.BooleanFunction;
import game.functions.ints.BaseIntFunction;
import game.functions.ints.IntFunction;
import game.functions.ints.trackSite.first.TrackSiteFirstTrack;
import game.functions.ints.trackSite.move.TrackSiteMove;
import game.functions.ints.trackSite.position.TrackSiteEndTrack;
import game.types.play.RoleType;
import other.context.Context;

/**
 * Returns a site on a track.
 * 
 * @author Eric Piette 
 */
@SuppressWarnings("javadoc")
public final class TrackSite extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	/**
	 * For the first site in a track.
	 * 
	 * @param trackSiteType The type of site on the track.
	 * @param player        The index of the player.
	 * @param role          The role of the player.
	 * @param name          The name of the track ["Track"].
	 * @param from          The site from where to look [First site of the track].
	 * @param If            The condition to verify for that site [True].
	 * 
	 * @example (trackSite FirstSite if:(is Empty (to)))
	 */
	public static IntFunction construct
	(
			          final TrackSiteFirstType     trackSiteType,
			 @Or @Opt final game.util.moves.Player player,
			 @Or @Opt final RoleType	           role,
			     @Opt final String                 name,
		@Name	 @Opt final IntFunction            from,
		@Name	 @Opt final BooleanFunction        If
	)
	{
		switch (trackSiteType)
		{ 
		case FirstSite:
			return new TrackSiteFirstTrack(player, role, name, from, If);
		default:
			break;
		}
		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("TrackSite(): A TrackSiteFirstType is not implemented.");
	}
	
	//-------------------------------------------------------------------------

	/**
	 * For the last site in a track.
	 * 
	 * @param trackSiteType The type of site on the track.
	 * @param player        The index of the player.
	 * @param role          The role of the player.
	 * @param name          The name of the track ["Track"].
	 * 
	 * @example (trackSite EndSite)
	 */
	public static IntFunction construct
	(
			     final TrackSiteType          trackSiteType,
		@Or @Opt final game.util.moves.Player player,
		@Or @Opt final RoleType	              role,
			@Opt final String                 name
	)
	{
		switch (trackSiteType)
		{
		case EndSite:
			return new TrackSiteEndTrack(player, role, name);
		default:
			break;
		}
		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("TrackSite(): A TrackSiteType is not implemented.");
	}

	//-------------------------------------------------------------------------

	/**
	 * For getting the site in a track from a site after some steps.
	 * 
	 * @param trackSiteType The type of site on the track.
	 * @param from          The current location [(from)].
	 * @param player        The owner of the track [(mover)].
	 * @param role          The role of the owner of the track [Mover].
	 * @param name          The name of the track.
	 * @param steps         The distance to move on the track.
	 *
	 * @example (trackSite Move steps:(count Pips))
	 */
	public static IntFunction construct
	(
			           final TrackSiteMoveType      trackSiteType,
		@Opt     @Name final IntFunction            from,
		@Opt @Or   	   final RoleType               role,
		@Opt @Or       final game.util.moves.Player player,
		@Opt @Or       final String                 name,
				 @Name final IntFunction            steps
	)
	{
		switch (trackSiteType)
		{
		case Move:
			return new TrackSiteMove(from, role, player, name, steps);
		default:
			break;
		}
		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("TrackSite(): A TrackSiteMoveType is not implemented.");
	}

	//-------------------------------------------------------------------------

	
	private TrackSite()
	{
		// Make grammar pick up construct() and not default constructor
	}
	
	//-------------------------------------------------------------------------

	@Override
	public int eval(final Context context)
	{
		// Should not be called, should only be called on subclasses
		throw new UnsupportedOperationException("TrackSite.eval(): Should never be called directly.");

		// return new Region();
	}

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