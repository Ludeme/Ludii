package game.functions.region.foreach;

import annotations.Name;
import annotations.Opt;
import game.Game;
import game.functions.booleans.BooleanFunction;
import game.functions.intArray.IntArrayFunction;
import game.functions.ints.IntFunction;
import game.functions.region.BaseRegionFunction;
import game.functions.region.RegionFunction;
import game.functions.region.foreach.level.ForEachLevel;
import game.functions.region.foreach.player.ForEachPlayer;
import game.functions.region.foreach.sites.ForEachSite;
import game.functions.region.foreach.sites.ForEachSiteInRegion;
import game.functions.region.foreach.team.ForEachTeam;
import game.rules.start.forEach.ForEachTeamType;
import game.types.board.SiteType;
import game.util.directions.StackDirection;
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

	//-------------------------------------------------------------------------

	/**
	 * For iterating through levels of a site.
	 * 
	 * @param forEachType    The type of property to iterate.
	 * @param type           The type of graph element.
	 * @param at             The site.
	 * @param stackDirection The direction to count in the stack [FromTop].
	 * @param If             The condition to satisfy.
	 * @param startAt        The level to start to look at.
	 * 
	 * @example (forEach Level at:(site))
	 */
	public static RegionFunction construct
	(
		           final ForEachLevelType forEachType, 
		     @Opt  final SiteType         type,
	         @Name final IntFunction      at,
	 	@Opt       final StackDirection   stackDirection,
		@Opt @Name final BooleanFunction  If,
        @Opt @Name final IntFunction      startAt
	)
	{
		return new ForEachLevel(type, at, stackDirection, If, startAt);
	}
	
	//-------------------------------------------------------------------------

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
	
	//-------------------------------------------------------------------------
	
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
		return new ForEachSite(region, If);
	}
	
	/**
	 * For computing a region in iterating another with (site).
	 * 
	 * @param of     The region of sites.
	 * @param region The region to compute with each site of the first region.
	 * @example (forEach of:(sites Occupied by:Mover) (sites To (slide (from (site)))))
	 */
	public static RegionFunction construct
	(
		 @Name final RegionFunction of, 
		       final RegionFunction region
	)
	{
		return new ForEachSiteInRegion(of, region);
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
