package game.rules.play.moves.nonDecision.operators.foreach;

import annotations.Name;
import annotations.Opt;
import annotations.Or;
import annotations.Or2;
import game.Game;
import game.functions.booleans.BooleanFunction;
import game.functions.intArray.IntArrayFunction;
import game.functions.ints.IntFunction;
import game.functions.region.RegionFunction;
import game.rules.play.moves.Moves;
import game.rules.play.moves.nonDecision.effect.Effect;
import game.rules.play.moves.nonDecision.effect.Then;
import game.rules.play.moves.nonDecision.operators.foreach.die.ForEachDie;
import game.rules.play.moves.nonDecision.operators.foreach.direction.ForEachDirection;
import game.rules.play.moves.nonDecision.operators.foreach.group.ForEachGroup;
import game.rules.play.moves.nonDecision.operators.foreach.level.ForEachLevel;
import game.rules.play.moves.nonDecision.operators.foreach.piece.ForEachPiece;
import game.rules.play.moves.nonDecision.operators.foreach.player.ForEachPlayer;
import game.rules.play.moves.nonDecision.operators.foreach.site.ForEachSite;
import game.rules.play.moves.nonDecision.operators.foreach.team.ForEachTeam;
import game.rules.play.moves.nonDecision.operators.foreach.value.ForEachValue;
import game.rules.start.forEach.ForEachTeamType;
import game.types.board.SiteType;
import game.types.play.RoleType;
import game.util.directions.Direction;
import game.util.directions.StackDirection;
import game.util.moves.To;
import other.context.Context;

/**
 * Iterates over a set of items.
 * 
 * @author Eric.Piette
 * 
 * Use this ludeme to iterate over a set of items such as pieces, players, directions or regions, 
 * and apply specified actions to each item.
 */
@SuppressWarnings("javadoc")
public final class ForEach extends Effect
{
	private static final long serialVersionUID = 1L;

	// -------------------------------------------------------------------------

	/**
	 * For iterating on teams.
	 * 
	 * @param forEachType    The type of property to iterate.
	 * @param type           The type of the graph elements of the group [default SiteType of the board].
	 * @param site           The site to iterate through. 
	 * @param stackDirection The direction to count in the stack [FromBottom].
	 * @param moves          The moves.
	 * @param then           The moves applied after that move is applied.
	 * 
	 * @example (forEach Level (last To) (if (= (id "King" Mover) (what at:(last To) level:(level))) (addScore Mover 1)))
	 */
	public static Moves construct
	(
		     final ForEachLevelType forEachType, 
	    @Opt final SiteType         type,
		     final IntFunction      site,
	    @Opt final StackDirection   stackDirection,
	         final Moves            moves,
		@Opt final Then             then
	)
	{
		return new ForEachLevel(type, site, stackDirection, moves, then);
	}
	
	// -------------------------------------------------------------------------

	/**
	 * For iterating on teams.
	 * 
	 * @param forEachType The type of property to iterate.
	 * @param moves       The moves.
	 * @param then        The moves applied after that move is applied.
	 * 
	 * @example (forEach Team (forEach (team) (set Hidden What at:(site)
	 *          to:Player)))
	 */
	public static Moves construct
	(
		     final ForEachTeamType forEachType, 
	         final Moves           moves,
		@Opt final Then            then
	)
	{
		return new ForEachTeam(moves, then);
	}
	
	//-------------------------------------------------------------------------

	/**
	 * For iterating through the groups.
	 * 
	 * @param forEachType The type of property to iterate.
	 * @param type        The type of the graph elements of the group.
	 * @param directions  The directions of the connection between elements in the
	 *                    group [Adjacent].
	 * @param If          The condition on the pieces to include in the group.
	 * @param moves       The moves to apply.
	 * @param then        The moves applied after that move is applied.
	 * 
	 * @example (forEach Group (addScore Mover (count Sites in:(sites))))
	 */
	public static Moves construct
	(
			       final ForEachGroupType forEachType,
		@Opt	   final SiteType         type,
		@Opt       final Direction        directions,
		@Opt @Name final BooleanFunction  If,
    	           final Moves            moves,
		@Opt 	   final Then             then
	)
	{
		switch (forEachType)
		{
		case Group:
			return new ForEachGroup(type,directions, If, moves, then);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("ForEach(): A ForEachGroupType is not implemented.");
	}
	
	//-------------------------------------------------------------------------

	/**
	 * For iterating through the dice.
	 * 
	 * @param forEachType   The type of property to iterate.
	 * @param handDiceIndex The index of the dice container [0].
	 * @param combined      True if the combination is allowed [False].
	 * @param replayDouble  True if double allows a second move [False].
	 * @param If            The condition to satisfy to move [True].
	 * @param moves         The moves to apply.
	 * @param then          The moves applied after that move is applied.
	 * 
	 * @example (forEach Die (if (= (pips) 5) (or (forEach Piece "Pawn") (forEach
	 *          Piece "King_noCross")) (if (= (pips) 4) (forEach Piece "Elephant")
	 *          (if (= (pips) 3) (forEach Piece "Knight") (if (= (pips) 2) (forEach
	 *          Piece "Boat") ) ) ) ) )
	 */
	public static Moves construct
	(
			         final ForEachDieType  forEachType,
		@Opt 	     final IntFunction     handDiceIndex,
		@Opt @Name 	 final BooleanFunction combined,
		@Opt @Name 	 final BooleanFunction replayDouble,
		@Opt @Name 	 final BooleanFunction If,
			     	 final Moves           moves,
		@Opt 		 final Then            then
	)
	{
		switch (forEachType)
		{
		case Die:
			return new ForEachDie(handDiceIndex, combined, replayDouble, If, moves, then);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("ForEach(): A ForEachDieType is not implemented.");
	}
	
	//-------------------------------------------------------------------------

	/**
	 * For iterating through the directions.
	 * 
	 * @param forEachType The type of property to iterate.
	 * @param from        The origin of the movement [(from)].
	 * @param directions  The directions of the move [Adjacent].
	 * @param between     The data on the locations between the from location and
	 *                    the to location [(between (exact 1))].
	 * @param to          The data on the location to move.
	 * @param moves       The moves to applied on these directions.
	 * @param then        The moves applied after that move is applied.
	 * 
	 * @example (forEach Direction (from (to)) (directions {FR FL}) (to if:(or (is
	 *          In (to) (sites Empty)) (is Enemy (who at:(to)))) (apply (fromTo
	 *          (from) (to if:(or (is Empty (to)) (is Enemy (who at:(to)))) (apply
	 *          (remove (to))) ) ) ) ) )
	 */
	public static Moves construct
	(
			     final ForEachDirectionType    forEachType,
			@Opt final game.util.moves.From    from,
			@Opt final Direction               directions,
			@Opt final game.util.moves.Between between,
		@Or      final To 	                   to,
		@Or      final Moves                   moves,
			@Opt final Then                    then
	)
	{
		int numNonNull = 0;
		if (to != null)
			numNonNull++;
		if (moves != null)
			numNonNull++;

		if (numNonNull != 1)
			throw new IllegalArgumentException(
					"ForEach(): With ForEachDirectionType one to, moves parameter must be non-null.");

		switch (forEachType)
		{
		case Direction:
			return new ForEachDirection(from, directions, between, to, moves,then);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("ForEach(): A ForEachDirectionType is not implemented.");
	}
	
	//-------------------------------------------------------------------------

	/**
	 * For iterating through the sites of a region.
	 * 
	 * @param forEachType The type of property to iterate.
	 * @param regionFn    The region used.
	 * @param generator   The move to apply.
	 * @param noMoveYet   The moves to apply if the list of moves resulting from the
	 *                    generator is empty.
	 * @param then        The moves applied after that move is applied.
	 * 
	 * @example (forEach Site (intersection (sites Around (last To)) (sites Occupied
	 *          by:Next) ) (and (remove (site)) (add (piece (id "Ball" Mover)) (to
	 *          (site))) ) )
	 */
	public static Moves construct
	(
			       final ForEachSiteType forEachType,
			 	   final RegionFunction  regionFn,
			 	   final Moves           generator,
		@Opt @Name final Moves           noMoveYet,
		@Opt 	   final Then            then
	)
	{
		switch (forEachType)
		{
		case Site:
			return new ForEachSite(regionFn, generator, noMoveYet, then);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("ForEach(): A ForEachSiteType is not implemented.");
	}
	
	//-------------------------------------------------------------------------


	/**
	 * For iterating through values from an IntArrayFunction.
	 * 
	 * @param forEachType The type of property to iterate.
	 * @param values      The IntArrayFunction.
	 * @param generator   The move to apply.
	 * @param then        The moves applied after that move is applied.
	 * 
	 * @example (forEach Value (values Remembered) (move (from) (to (trackSite Move
	 *          steps:(value)))))
	 */
	public static Moves construct
	(
			       final ForEachValueType forEachType,
			       final IntArrayFunction values,
			 	   final Moves            generator,
		@Opt 	   final Then             then
	)
	{
		switch (forEachType)
		{
		case Value:
			return new ForEachValue(values, generator, then);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("ForEach(): A ForEachValueType is not implemented.");
	}

	//-------------------------------------------------------------------------
	
	/**
	 * For iterating through values between two.
	 * 
	 * @param forEachType The type of property to iterate.
	 * @param min         The minimal value.
	 * @param max         The maximal value.
	 * @param generator   The move to apply.
	 * @param then        The moves applied after that move is applied.
	 * 
	 * @example (forEach Value min:1 max:5 (move (from) (to (trackSite Move
	 *          steps:(value)))))
	 */
	public static Moves construct
	(
			       final ForEachValueType forEachType,
			 @Name final IntFunction      min,
			 @Name final IntFunction      max,
			 	   final Moves            generator,
		@Opt 	   final Then             then
	)
	{
		switch (forEachType)
		{
		case Value:
			return new ForEachValue(min, max, generator, then);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("ForEach(): A ForEachValueType is not implemented.");
	}
	
	//-------------------------------------------------------------------------

	/**
	 * For iterating through the pieces.
	 * 
	 * @param forEachType   The type of property to iterate.
	 * @param on            Type of graph element.
	 * @param item          The name of the piece.
	 * @param items         The names of the pieces.
	 * @param container     The index of the container.
	 * @param containerName The name of the container.
	 * @param specificMoves The specific moves to apply to the pieces.
	 * @param player        The owner of the piece [(mover)].
	 * @param role          The role type of the owner of the piece [Mover].
	 * @param top           To apply the move only to the top piece in case of a
	 *                      stack [False].
	 * @param then          The moves applied after that move is applied.
	 * 
	 * @example (forEach Piece)
	 * 
	 * @example (forEach Piece "Bear" (step (to if:(= (what at:(to)) (id "Seal1")))
	 *          ) )
	 */
	public static Moves construct
	(
			             final ForEachPieceType       forEachType,
	    @Opt @Name       final SiteType               on,
		@Opt       @Or	 final String                 item, 
		@Opt       @Or	 final String[]               items, 
		@Opt @Name @Or2  final IntFunction            container, 
		@Opt       @Or2  final String                 containerName,
		@Opt 			 final Moves                  specificMoves, 
		@Opt       @Or2  final game.util.moves.Player player, 
		@Opt       @Or2  final RoleType               role, 
		@Opt @Name 		 final BooleanFunction        top, 
		@Opt 			 final Then                   then
	)
	{
		int numNonNull = 0;
		if (item != null)
			numNonNull++;
		if (items != null)
			numNonNull++;

		if (numNonNull > 1)
			throw new IllegalArgumentException(
					"ForEach(): With ForEachPieceType zero or one item, items parameter must be non-null.");

		int numNonNull2 = 0;
		if (container != null)
			numNonNull2++;
		if (containerName != null)
			numNonNull2++;

		if (numNonNull2 > 1)
			throw new IllegalArgumentException(
					"ForEach(): With ForEachPieceType zero or one container, containerName parameter must be non-null.");

		int numNonNull3 = 0;
		if (player != null)
			numNonNull3++;
		if (role != null)
			numNonNull3++;

		if (numNonNull3 > 1)
			throw new IllegalArgumentException(
					"ForEach(): With ForEachPieceType zero or one player, role parameter must be non-null.");

		switch (forEachType)
		{
		case Piece:
			return new ForEachPiece(on, item, items, container, containerName, specificMoves, player, role, top,
					then);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("ForEach(): A ForEachPieceType is not implemented.");
	}
	
	//-------------------------------------------------------------------------

	/**
	 * For iterating through the players.
	 * 
	 * @param moves The moves.
	 * @param then  The moves applied after that move is applied.
	 * 
	 * @example (forEach Player (addScore (player (player)) 1))
	 */
	public static Moves construct
	(
			 final ForEachPlayerType forEachType,
			 final Moves             moves, 
		@Opt final Then              then
	)
	{
		switch (forEachType)
		{
		case Player:
			return new ForEachPlayer(moves, then);
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
	 * @param players The list of players.
	 * @param moves   The moves.
	 * @param then    The moves applied after that move is applied.
	 * 
	 * @example (forEach (players Ally of:(next)) (addScore (player (player)) 1))
	 */
	public static Moves construct
	(
			 final IntArrayFunction players,
			 final Moves            moves, 
		@Opt final Then             then
	)
	{
		return new ForEachPlayer(players, moves, then);
	}
	
	private ForEach()
	{
		super(null);
		// Ensure that compiler does pick up default constructor
	}

	@Override
	public Moves eval(final Context context)
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

	@Override
	public boolean canMoveTo(Context context, int target)
	{
		// Should never be there
		throw new UnsupportedOperationException("ForEach.canMoveTo(): Should never be called directly.");
	}

	//-------------------------------------------------------------------------

}
