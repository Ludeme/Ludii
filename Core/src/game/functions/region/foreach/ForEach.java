package game.functions.region.foreach;

import annotations.Name;
import game.Game;
import game.functions.booleans.BooleanFunction;
import game.functions.intArray.IntArrayFunction;
import game.functions.region.BaseRegionFunction;
import game.functions.region.RegionFunction;
import game.functions.region.foreach.player.ForEachPlayer;
import game.functions.region.foreach.sites.ForEachSite;
import game.functions.region.foreach.team.ForEachTeam;
import game.rules.start.forEach.ForEachTeamType;
import game.util.equipment.Region;
import other.context.Context;

/**
 * Returns a region filtering with a condition or build according to different player indices.
 * 
 * @author Eric.Piette
 */
@SuppressWarnings("javadoc")
public final class ForEach extends BaseRegionFunction
{
	private static final long serialVersionUID = 1L;

	// -------------------------------------------------------------------------

	/**
	 * For iterating through teams.
	 * 
	 * @param forEachType The type of property to iterate.
	 * @param region      The region.
	 * 
	 * @example (forEach Team (forEach (team) (sites Occupied by:Player)))
	 */
	public static RegionFunction construct
	(
		final ForEachTeamType forEachType, 
	    final RegionFunction  region
	)
	{
		return new ForEachTeam(region);
	}
	
	// -------------------------------------------------------------------------
	
	/**
	 * For filtering a region according to a condition.
	 * 
	 * @param region The original region.
	 * @param If     The condition to satisfy.
	 * @example (forEach (sites Occupied by:P1) if:(= (what at:(site)) (id
	 *          "Pawn1")))
	 */
	public static RegionFunction construct
	(
		      final RegionFunction region, 
		@Name final BooleanFunction If
	)
	{
		return new ForEachSite(region,If);
	}
	
	/**
	 * For iterating on players.
	 * 
	 * @param players The list of players.
	 * @param region  The region.
	 * 
	 * @example (forEach (players Ally of:(next)) (sites Occupied by:Player))
	 */
	public static RegionFunction construct
	(
	    final IntArrayFunction players,
	    final RegionFunction region 
	)
	{
		return new ForEachPlayer(players, region);
	}

	private ForEach()
	{
		// Make grammar pick up construct() and not default constructor
	}

	//-------------------------------------------------------------------------

	@Override
	public final Region eval(final Context context)
	{
		// Should not be called, should only be called on subclasses
		throw new UnsupportedOperationException("ForEach.eval(): Should never be called directly.");
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
