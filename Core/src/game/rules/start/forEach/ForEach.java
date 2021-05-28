package game.rules.start.forEach;

import annotations.Name;
import annotations.Opt;
import game.Game;
import game.functions.booleans.BooleanFunction;
import game.functions.intArray.IntArrayFunction;
import game.functions.ints.IntFunction;
import game.functions.region.RegionFunction;
import game.rules.Rule;
import game.rules.play.moves.nonDecision.operators.foreach.ForEachPlayerType;
import game.rules.play.moves.nonDecision.operators.foreach.ForEachSiteType;
import game.rules.start.StartRule;
import game.rules.start.forEach.player.ForEachPlayer;
import game.rules.start.forEach.site.ForEachSite;
import game.rules.start.forEach.team.ForEachTeam;
import game.rules.start.forEach.value.ForEachValue;
import other.context.Context;

/**
 * Iterates over a set of items.
 * 
 * @author Eric.Piette
 */
@SuppressWarnings("javadoc")
public final class ForEach extends StartRule
{
	private static final long serialVersionUID = 1L;

	/**
	 * For iterating on teams.
	 * 
	 * @param forEachType  The type of property to iterate.
	 * @param regionFn     The original region.
	 * @param If           The condition to satisfy.
	 * @param startingRule The starting rule to apply.
	 * 
	 * @example (forEach Team (forEach (team) (set Hidden What at:1 to:Player)))
	 */
	public static Rule construct
	(
		final ForEachTeamType forEachType, 
		final StartRule       startingRule
	)
	{
		switch (forEachType)
		{
		case Team:
			return new ForEachTeam(startingRule);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("ForEach(): A ForEachTeam is not implemented.");
	}
	
	//-------------------------------------------------------------------------

	/**
	 * For iterating on values between two.
	 * 
	 * @param forEachType  The type of property to iterate.
	 * @param regionFn     The original region.
	 * @param If           The condition to satisfy.
	 * @param startingRule The starting rule to apply.
	 * 
	 * @example (forEach Site (sites Top) if:(is Even (site)) (place "Pawn1"
	 *          (site)))
	 */
	public static Rule construct
	(
	               final ForEachSiteType forEachType,
		           final RegionFunction regionFn,
		@Opt @Name final BooleanFunction If,
			       final StartRule startingRule
	)
	{
		switch (forEachType)
		{
		case Site:
			return new ForEachSite(regionFn, If, startingRule);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("ForEach(): A ForEachSiteType is not implemented.");
	}
	
	//-------------------------------------------------------------------------

	/**
	 * For iterating on values between two.
	 * 
	 * @param forEachType  The type of property to iterate.
	 * @param min          The minimal value.
	 * @param max          The maximal value.
	 * @param startingRule The starting rule to apply.
	 * 
	 * @example (forEach Value min:1 max:5 (set Hidden What at:10 level:(value)
	 *          to:P1))
	 */
	public static Rule construct
	(
			  final ForEachStartValueType forEachType,
	    @Name final IntFunction min,
		@Name final IntFunction max,
			  final StartRule startingRule
	)
	{
		switch (forEachType)
		{
		case Value:
			return new ForEachValue(min, max, startingRule);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("ForEach(): A ForEachStartValueType is not implemented.");
	}
	
	//-------------------------------------------------------------------------

	/**
	 * For iterating through the players.
	 * 
	 * @param forEachType  The type of property to iterate.
	 * @param startingRule The starting rule to apply.
	 * 
	 * @example (forEach Player (set Hidden What at:1 to:Player))
	 */
	public static Rule construct
	(
	   final ForEachPlayerType forEachType,
	   final StartRule startingRule
	)
	{
		switch (forEachType)
		{
		case Player:
			return new ForEachPlayer(startingRule);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("ForEach(): A ForEachPlayerType is not implemented.");
	}

	//-------------------------------------------------------------------------
	
	/**
	 * For iterating through the players in using an IntArrayFunction.
	 * 
	 * @param players      The list of players.
	 * @param startingRule The starting rule to apply.
	 * 
	 * @example (forEach (players Ally of:(next)) (set Hidden What at:1 to:Player))
	 */
	public static Rule construct
	(
	     final IntArrayFunction players,
		 final StartRule startingRule
	)
	{
		return new ForEachPlayer(players, startingRule);
	}

	//-------------------------------------------------------------------------
	
	private ForEach()
	{
		// Ensure that compiler does pick up default constructor
	}

	@Override
	public void eval(final Context context)
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

	//-------------------------------------------------------------------------

}
