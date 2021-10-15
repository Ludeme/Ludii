package game.rules.play.moves.nonDecision.effect.set;

import annotations.Name;
import annotations.Opt;
import annotations.Or;
import annotations.Or2;
import game.Game;
import game.functions.booleans.BooleanFunction;
import game.functions.intArray.IntArrayFunction;
import game.functions.intArray.math.Difference;
import game.functions.ints.IntFunction;
import game.functions.region.RegionFunction;
import game.rules.play.moves.Moves;
import game.rules.play.moves.nonDecision.effect.Effect;
import game.rules.play.moves.nonDecision.effect.Then;
import game.rules.play.moves.nonDecision.effect.set.direction.SetRotation;
import game.rules.play.moves.nonDecision.effect.set.hidden.SetHidden;
import game.rules.play.moves.nonDecision.effect.set.nextPlayer.SetNextPlayer;
import game.rules.play.moves.nonDecision.effect.set.pending.SetPending;
import game.rules.play.moves.nonDecision.effect.set.player.SetScore;
import game.rules.play.moves.nonDecision.effect.set.player.SetValuePlayer;
import game.rules.play.moves.nonDecision.effect.set.site.SetCount;
import game.rules.play.moves.nonDecision.effect.set.site.SetState;
import game.rules.play.moves.nonDecision.effect.set.site.SetValue;
import game.rules.play.moves.nonDecision.effect.set.suit.SetTrumpSuit;
import game.rules.play.moves.nonDecision.effect.set.team.SetTeam;
import game.rules.play.moves.nonDecision.effect.set.value.SetCounter;
import game.rules.play.moves.nonDecision.effect.set.value.SetPot;
import game.rules.play.moves.nonDecision.effect.set.var.SetVar;
import game.types.board.HiddenData;
import game.types.board.SiteType;
import game.types.play.RoleType;
import game.util.moves.Player;
import other.IntArrayFromRegion;
import other.context.Context;

/**
 * Sets some aspect of the game state in response to a move.
 * 
 * @author Eric.Piette
 */
@SuppressWarnings("javadoc")
public final class Set extends Effect
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * For setting a team.
	 * 
	 * @param setType The type of property to set.
	 * @param team    The index of the team.
	 * @param roles   The roleType of each player on the team.
	 * @param then    The moves applied after that move is applied.
	 * 
	 * @example (set Team 1 {P1 P3})
	 */
	public static Moves construct
	(
			final SetTeamType setType,
		    final IntFunction team,
	        final RoleType[]  roles,
	   @Opt final Then        then
	)
	{
		return new SetTeam(team, roles, then);
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
	 * @param To        The roleType with these hidden information.
	 * @param then      The moves applied after that move is applied.
	 * 
	 * @example (set Hidden What at:(last To) to:Mover)
	 * @example (set Hidden What at:(last To) to:P2)
	 * @example (set Hidden Count (sites Occupied by:Next) to:Mover)
	 */
	public static Moves construct
	(
				         final SetHiddenType   setType, 
		@Opt        @Or  final HiddenData      dataType,
		@Opt        @Or  final HiddenData[]    dataTypes, 
	    @Opt             final SiteType        type,
		      @Name @Or2 final IntFunction     at, 
			        @Or2 final RegionFunction  region,
		@Opt  @Name      final IntFunction     level,
		@Opt             final BooleanFunction value, 
		      @Name @Or  final Player          to, 
	          @Name @Or  final RoleType        To,
	    @Opt             final Then            then
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

		numNonNull = 0;
		if (to != null)
			numNonNull++;
		if (To != null)
			numNonNull++;

		if (numNonNull != 1)
			throw new IllegalArgumentException("Set(): With SetHiddenType one to or To parameter must be non-null.");

		switch (setType)
		{
		case Hidden:
			return new SetHidden(dataTypes != null ? dataTypes : dataType != null ? new HiddenData[]
			{ dataType } : null, type, new IntArrayFromRegion(at, region), level, value, to, To, then);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Set(): A SetHiddenType is not implemented.");
	}

	//-------------------------------------------------------------------------

	/**
	 * For setting the trump suit.
	 * 
	 * @param setType The type of property to set.
	 * @param suit    The suit to choose.
	 * @param suits   The possible suits to choose.
	 * @param then    The moves applied after that move is applied.
	 * 
	 * @example (set TrumpSuit (card Suit at:(handSite Shared)))
	 * 
	 */
	public static Moves construct
	(
			     final SetTrumpType setType,
		     @Or final IntFunction  suit,
		     @Or final Difference   suits,
		@Opt     final Then         then
	)
	{
		int numNonNull = 0;
		if (suit != null)
			numNonNull++;
		if (suits != null)
			numNonNull++;

		if (numNonNull != 1)
			throw new IllegalArgumentException("Set(): With SetSuitType only one suit or suits parameter must be non-null.");

		switch (setType)
		{
		case TrumpSuit:
			return new SetTrumpSuit(suit, suits, then);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Set(): A SetSuitType is not implemented.");
	}
	
	//-------------------------------------------------------------------------

	/**
	 * For setting the next player.
	 * 
	 * @param setType     The type of property to set.
	 * @param who         The data of the next player.
	 * @param nextPlayers The indices of the next players.
	 * @param then        The moves applied after that move is applied.
	 * 
	 * @example (set NextPlayer (player (mover)))
	 * 
	 */
	public static Moves construct
	(
			     final SetNextPlayerType setType,
		     @Or final Player            who,
			 @Or final IntArrayFunction  nextPlayers,
		@Opt     final Then              then
	)
	{
		int numNonNull = 0;
		if (who != null)
			numNonNull++;
		if (nextPlayers != null)
			numNonNull++;

		if (numNonNull > 1)
			throw new IllegalArgumentException("Set(): With SetPlayerType only one who or nextPlayers parameter can be non-null.");

		switch (setType)
		{
		case NextPlayer:
			return new SetNextPlayer(who, nextPlayers, then);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Set(): A SetPlayerType is not implemented.");
	}
	
	//-------------------------------------------------------------------------

	/**
	 * For setting the rotations.
	 * 
	 * @param setType    The type of property to set.
	 * @param to         Description of the ``to'' location [(to (from))].
	 * @param directions The index of the possible new rotations.
	 * @param direction  The index of the possible new rotation.
	 * @param previous   True to allow movement to the left [True].
	 * @param next       True to allow movement to the right [True].
	 * @param then       The moves applied after that move is applied.
	 * 
	 * @example (set Rotation)
	 * 
	 * @example (set Rotation (to (last To)) next:False)
	 */
	public static Moves construct
	(
			           final SetRotationType    setType,
		@Opt           final game.util.moves.To to,
		@Opt @Or 	   final IntFunction[]      directions,
		@Opt @Or 	   final IntFunction        direction,
		@Opt     @Name final BooleanFunction    previous,
		@Opt     @Name final BooleanFunction    next,
		@Opt 	 	   final Then               then
	)
	{
		int numNonNull = 0;
		if (directions != null)
			numNonNull++;
		if (direction != null)
			numNonNull++;

		if (numNonNull > 1)
			throw new IllegalArgumentException(
					"Set(): With SetRotationType zero or one directions or direction parameter must be non-null.");

		switch (setType)
		{
		case Rotation:
			return new SetRotation(to, directions, direction, previous, next, then);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Set(): A SetRotationType is not implemented.");
	}
	
	//-------------------------------------------------------------------------

	/**
	 * For setting the value or the score of a player.
	 * 
	 * @param setType The type of property to set.
	 * @param player  The index of the player.
	 * @param role    The role of the player.
	 * @param value   The value of the player.
	 * @param then    The moves applied after that move is applied.
	 * 
	 * @example (set Value Mover 1)
	 * 
	 * @example (set Score P1 50)
	 */
	public static Moves construct
	(
			     final SetPlayerType          setType,
			 @Or final game.util.moves.Player player,
		     @Or final RoleType               role,
			     final IntFunction            value,
		@Opt     final Then                   then
	)
	{
		int numNonNull = 0;
		if (player != null)
			numNonNull++;
		if (role != null)
			numNonNull++;

		if (numNonNull != 1)
			throw new IllegalArgumentException("Set(): With SetType Only one player or role parameter m be non-null.");
		
		switch (setType)
		{
		case Value:
			return new SetValuePlayer(player, role, value, then);
		case Score:
			return new SetScore(player, role, value, then);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Set(): A SetType is not implemented.");
	}
	
	//-------------------------------------------------------------------------

	/**
	 * For setting the pending values.
	 * 
	 * @param setType The type of property to set.
	 * @param value   The value of the pending state [1].
	 * @param region  The set of locations to put in pending.
	 * @param then    The moves to apply afterwards.
	 * 
	 * @example (set Pending)
	 * @example (set Pending (sites From (forEach Piece)))
	 */
	public static Moves construct
	(
			     final SetPendingType setType,
		@Opt @Or final IntFunction    value, 
		@Opt @Or final RegionFunction region, 
		@Opt     final Then           then
	)
	{
		switch (setType)
		{
		case Pending:
			return new SetPending(value, region, then);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Set(): A SetPendingType is not implemented.");
	}
	
	//-------------------------------------------------------------------------

	/**
	 * For setting the counter or the variables.
	 * 
	 * @param setType  The type of property to set.
	 * @param name     The name of the var.
	 * @param newValue The new counter value [-1].
	 * @param then     The moves to apply afterwards.
	 * 
	 * @example (set Var (value Piece at:(last To)))
	 */
	public static Moves construct
	(
			 final SetVarType     setType,
		@Opt final String         name,
		@Opt final IntFunction    newValue,
		@Opt final Then           then
	)
	{
		switch (setType)
		{
		case Var:
			return new SetVar(name, newValue, then);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Set(): A SetVarType is not implemented.");
	}
	
	//-------------------------------------------------------------------------

	/**
	 * For setting the counter or the variables.
	 * 
	 * @param setType  The type of property to set.
	 * @param newValue The new counter value [-1].
	 * @param then     The moves to apply afterwards.
	 * 
	 * @example (set Counter -1)
	 */
	public static Moves construct
	(
			 final SetValueType   setType,
		@Opt final IntFunction    newValue,
		@Opt final Then           then
	)
	{
		switch (setType)
		{
		case Counter:
			return new SetCounter(newValue,then);
		case Pot:
			return new SetPot(newValue, then);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Set(): A SetValueType is not implemented.");
	}
	
	//-------------------------------------------------------------------------

	/**
	 * For setting the count or the state of a site.
	 * 
	 * @param setType The type of property to set.
	 * @param type    The graph element type [default SiteType of the board].
	 * @param at      The site to set.
	 * @param level   The level to set [0].
	 * @param value   The new value.
	 * @param then    The moves to apply afterwards.
	 * 
	 * @example (set State at:(last To) (mover))
	 * 
	 * @example (set Count at:(last To) 10)
	 * 
	 * @example (set Value at:(last To) 10)
	 * 
	 */
	public static Moves construct
	(
			       final SetSiteType setType,
		      @Opt final SiteType    type,
		@Name      final IntFunction at,
		@Name @Opt final IntFunction level,
			       final IntFunction value,
		      @Opt final Then        then
	)
	{
		switch (setType)
		{
		case Count:
			return new SetCount(type, at, value, then);
		case State:
			return new SetState(type, at, level, value, then);
		case Value:
			return new SetValue(type, at, level, value, then);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Set(): A SetSiteType is not implemented.");
	}
	
	//-------------------------------------------------------------------------
	
	private Set()
	{
		super(null);
		// Ensure that compiler does pick up default constructor
	}

	//-------------------------------------------------------------------------

	@Override
	public Moves eval(final Context context)
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

	@Override
	public boolean canMoveTo(Context context, int target)
	{
		// Should never be there
		throw new UnsupportedOperationException("Set.canMoveTo(): Should never be called directly.");
	}
	
	//-------------------------------------------------------------------------

}
