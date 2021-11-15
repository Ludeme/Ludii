package game.rules.start.set;

import annotations.Name;
import annotations.Opt;
import annotations.Or;
import annotations.Or2;
import game.Game;
import game.functions.booleans.BooleanFunction;
import game.functions.ints.IntFunction;
import game.functions.region.RegionFunction;
import game.rules.Rule;
import game.rules.start.StartRule;
import game.rules.start.set.player.SetAmount;
import game.rules.start.set.player.SetScore;
import game.rules.start.set.players.SetTeam;
import game.rules.start.set.remember.SetRememberValue;
import game.rules.start.set.sites.SetCost;
import game.rules.start.set.sites.SetCount;
import game.rules.start.set.sites.SetPhase;
import game.rules.start.set.sites.SetSite;
import game.types.board.HiddenData;
import game.types.board.SiteType;
import game.types.play.RoleType;
import other.IntArrayFromRegion;
import other.context.Context;

/**
 * Sets some aspect of the initial game state.
 * 
 * @author Eric.Piette
 */
@SuppressWarnings("javadoc")
public final class Set extends StartRule
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * For setting the remembering values.
	 * 
	 * @param setType     The type of property to set.
	 * @param name        The name of the remembering values.
	 * @param value       The value to remember.
	 * @param regionValue The values to remember.
	 * @param unique      If True we remember a value only if not already
	 *                    remembered[False].
	 * 
	 * @example (set RememberValue 5)
	 */
	public static Rule construct
	(
				          final SetRememberValueType setType, 
			@Opt          final String               name,
				 @Or      final IntFunction          value,
				 @Or      final RegionFunction       regionValue,
			@Opt    @Name final BooleanFunction      unique
	)
	{
		int numNonNull = 0;
		if (value != null)
			numNonNull++;
		if (regionValue != null)
			numNonNull++;

		if (numNonNull != 1)
			throw new IllegalArgumentException(
					"Set(): With SetRememberValueType only one value or regionValue parameter must be non-null.");
		
		switch (setType)
		{
		case RememberValue:
			return new SetRememberValue(name, value, regionValue, unique);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Set(): A SetRememberValueType is not implemented.");
	}
	
	//-------------------------------------------------------------------------

	/**
	 * For setting the hidden information.
	 * 
	 * @param setType   The type of property to set.
	 * @param dataType  The type of hidden data [Invisible].
	 * @param dataTypes The types of hidden data [Invisible].
	 * @param type      The graph element type [default of the board].
	 * @param at        The site to set the hidden information.
	 * @param region    The region to set the hidden information.
	 * @param level     The level to set the hidden information [0].
	 * @param value     The value to set [True].
	 * @param to        The player with these hidden information.
	 * 
	 * @example (set Hidden What at:5 to:P1)
	 * @example (set Hidden What at:6 to:P2)
	 * @example (set Hidden Count (sites Occupied by:Next) to:P1)
	 */
	public static Rule construct
	(
				         final SetStartHiddenType setType, 
		      @Opt @Or   final HiddenData         dataType,
		      @Opt @Or   final HiddenData[]       dataTypes, 
			  @Opt       final SiteType           type,
		@Name      @Or2  final IntFunction        at, 
			       @Or2  final RegionFunction     region,
		@Name @Opt       final IntFunction        level,
		      @Opt       final BooleanFunction    value, 
	    @Name            final RoleType           to
	)
	{
		int numNonNull = 0;
		if (dataType != null)
			numNonNull++;
		if (dataTypes != null)
			numNonNull++;

		if (numNonNull > 1)
			throw new IllegalArgumentException(
					"Set(): With SetHiddenType only one dataType or dataTypes parameter must be non-null.");

		int numNonNull2 = 0;
		if (at != null)
			numNonNull2++;
		if (region != null)
			numNonNull2++;

		if (numNonNull2 != 1)
			throw new IllegalArgumentException(
					"Set(): With SetHiddenType one at or region parameter must be non-null.");

		switch (setType)
		{
		case Hidden:
			return new game.rules.start.set.hidden.SetHidden(
					dataTypes != null ? dataTypes : dataType != null ? new HiddenData[]
					{ dataType } : null, type, new IntArrayFromRegion(at, region), level, value, to);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Set(): A SetStartHiddenType is not implemented.");
	}

	//-------------------------------------------------------------------------
	
	/**
	 * For setting a site to a player.
	 * 
	 * @param role  The owner of the site.
	 * @param type  The graph element type [default SiteType of the board].
	 * @param loc   The location to place a piece.
	 * @param coord The coordinate of the location to place a piece.
	 * 
	 * @example (set P1 Vertex 5)
	 */
	public static Rule construct
	(
                   final RoleType    role,
		@Opt 	   final SiteType    type,
		@Opt	   final IntFunction loc,
		@Opt @Name final String      coord
	)
	{
		return new SetSite(role, type, loc, coord);
	}

	/**
	 * For setting sites to a player.
	 * 
	 * @param role   The owner of the site.
	 * @param type   The graph element type [default SiteType of the board].
	 * @param locs   The sites to fill.
	 * @param region The region to fill.
	 * @param coords The coordinates of the sites to fill.
	 * 
	 * @example (set P1 Vertex (sites {0 5 6} ))
	 */
	public static Rule construct
	(
                       final RoleType       role,
			@Opt 	   final SiteType       type,
			@Opt 	   final IntFunction[] 	locs,
			@Opt 	   final RegionFunction region,
			@Opt 	   final String[] 	    coords
	)
	{
		return new SetSite(role, type, locs, region, coords);
	}
	
	//-------------------------------------------------------------------------

	/**
	 * For setting the count, the cost or the phase to sites.
	 * 
	 * @param startType The property to set.
	 * @param value     The value.
	 * @param type      The graph element type [default SiteType of the board].
	 * @param at        The site to set.
	 * @param to        The region to set.
	 * 
	 * @example (set Count 5 to:(sites Track) )
	 * 
	 * @example (set Cost 5 Vertex at:10)
	 * 
	 * @example (set Phase 1 Cell at:3)
	 */
	public static Rule construct
	(
			          final SetStartSitesType startType,
		              final IntFunction       value,
	       @Opt       final SiteType          type,
	   @Or     @Name  final IntFunction       at, 
	   @Or     @Name  final RegionFunction    to
	)
	{
		int numNonNull = 0;
		if (to != null)
			numNonNull++;
		if (at != null)
			numNonNull++;

		if (numNonNull != 1)
			throw new IllegalArgumentException(
					"Set(): With SetStartSitesType Exactly one region or site parameter must be non-null.");
		
		switch (startType)
		{
		case Count:
			return new SetCount(value, type, at, to);
		case Cost:
			return new SetCost(value, type, at, to);
		case Phase:
			return new SetPhase(value, type, at, to);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Set(): A SetStartSitesType is not implemented.");
	}
	
	//-------------------------------------------------------------------------

	/**
	 * For setting the amount or the score of a player.
	 * 
	 * @param startType The property to set.
	 * @param role      The roleType of the player.
	 * @param value     The value to set.
	 * 
	 * @example (set Amount 5000)
	 */
	public static Rule construct
	(
			 final SetStartPlayerType startType,
		@Opt final RoleType           role,
			 final IntFunction        value
	)
	{
		switch (startType)
		{
		case Amount:
			return new SetAmount(role, value);
		case Score:
			return new SetScore(role, value);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Set(): A SetStartPlayerType is not implemented.");
	}
	
	//-------------------------------------------------------------------------

	/**
	 * For setting a team.
	 * 
	 * @param startType The property to set.
	 * @param roles     The roleType of the player.
	 * @param index     The index of the team.
	 * 
	 * @example (set Team 1 {P1 P3})
	 */
	public static Rule construct
	(
	     final SetStartPlayersType startType,
	     final IntFunction         index,
	     final RoleType[]          roles
	)
	{
		switch (startType)
		{
		case Team:
			return new SetTeam(index, roles);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Set(): A SetStartPlayersType is not implemented.");
	}
	
	private Set()
	{
		// Ensure that compiler does pick up default constructor
	}

	@Override
	public void eval(final Context context)
	{
		// Should not be called, should only be called on subclasses
		throw new UnsupportedOperationException("Set.eval(): Should never be called directly.");
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