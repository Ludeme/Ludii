package game.rules.play.moves.decision;

import annotations.And;
import annotations.Name;
import annotations.Opt;
import annotations.Or;
import annotations.Or2;
import game.Game;
import game.functions.booleans.BooleanFunction;
import game.functions.intArray.IntArrayFunction;
import game.functions.intArray.math.Difference;
import game.functions.ints.IntFunction;
import game.functions.range.RangeFunction;
import game.functions.region.RegionFunction;
import game.rules.play.moves.Moves;
import game.rules.play.moves.nonDecision.effect.Add;
import game.rules.play.moves.nonDecision.effect.Bet;
import game.rules.play.moves.nonDecision.effect.Claim;
import game.rules.play.moves.nonDecision.effect.FromTo;
import game.rules.play.moves.nonDecision.effect.Hop;
import game.rules.play.moves.nonDecision.effect.Leap;
import game.rules.play.moves.nonDecision.effect.Pass;
import game.rules.play.moves.nonDecision.effect.PlayCard;
import game.rules.play.moves.nonDecision.effect.Promote;
import game.rules.play.moves.nonDecision.effect.Propose;
import game.rules.play.moves.nonDecision.effect.Remove;
import game.rules.play.moves.nonDecision.effect.Select;
import game.rules.play.moves.nonDecision.effect.Shoot;
import game.rules.play.moves.nonDecision.effect.Slide;
import game.rules.play.moves.nonDecision.effect.Step;
import game.rules.play.moves.nonDecision.effect.Then;
import game.rules.play.moves.nonDecision.effect.Vote;
import game.rules.play.moves.nonDecision.effect.set.SetNextPlayerType;
import game.rules.play.moves.nonDecision.effect.set.SetRotationType;
import game.rules.play.moves.nonDecision.effect.set.SetTrumpType;
import game.rules.play.moves.nonDecision.effect.set.direction.SetRotation;
import game.rules.play.moves.nonDecision.effect.set.nextPlayer.SetNextPlayer;
import game.rules.play.moves.nonDecision.effect.set.suit.SetTrumpSuit;
import game.rules.play.moves.nonDecision.effect.state.swap.SwapPlayersType;
import game.rules.play.moves.nonDecision.effect.state.swap.SwapSitesType;
import game.rules.play.moves.nonDecision.effect.state.swap.players.SwapPlayers;
import game.rules.play.moves.nonDecision.effect.state.swap.sites.SwapPieces;
import game.types.board.SiteType;
import game.types.board.StepType;
import game.types.play.RoleType;
import game.types.play.WhenType;
import game.util.directions.AbsoluteDirection;
import game.util.moves.From;
import game.util.moves.Piece;
import game.util.moves.Player;
import game.util.moves.To;
import other.context.Context;

/**
 * Defines a decision move.
 * 
 * @author Eric.Piette
 */
@SuppressWarnings("javadoc")
public final class Move extends Decision
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * For deciding to swap two players.
	 * 
	 * @param moveType The type of move.
	 * @param swapType The type of property to take.
	 * @param player1  The index of the first player.
	 * @param role1    The role of the first player.
	 * @param player2  The index of the second player.
	 * @param role2    The role of the second player.
	 * @param then     The moves applied after that move is applied.
	 * 
	 * @example (move Swap Players P1 P2)
	 */
	public static Moves construct
	(
		               final MoveSwapType    moveType,
		               final SwapPlayersType swapType,
		@And @Or       final IntFunction     player1,
		@And @Or       final RoleType        role1,
		@And @Or2      final IntFunction     player2, 
		@And @Or2      final RoleType        role2,
		          @Opt final Then            then
	)
	{
		int numNonNull = 0;
		if (player1 != null)
			numNonNull++;
		if (role1 != null)
			numNonNull++;

		if (numNonNull != 1)
			throw new IllegalArgumentException("Move(): With MoveSwapType and SwapPlayersType exactly one player1 or role1 parameter must be non-null.");
		
		int numNonNull2 = 0;
		if (player2 != null)
			numNonNull2++;
		if (role2 != null)
			numNonNull2++;

		if (numNonNull2 != 1)
			throw new IllegalArgumentException("Move(): With MoveSwapType and SwapPlayersType exactly one player2 or role2 parameter must be non-null.");
		
		Moves moves = null;

		switch (moveType)
		{
		case Swap:
			moves = new SwapPlayers(player1, role1, player2, role2, then);
			break;
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		if (moves == null)
			throw new IllegalArgumentException("Move(): A MoveSwapType is not implemented.");

		moves.setDecision();
		return moves;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * For deciding to swap two pieces.
	 * 
	 * @param moveType The type of move.
	 * @param swapType The type of property to take.
	 * @param locA     The first location [(lastFrom)].
	 * @param locB     The second location [(lastTo)].
	 * @param then     The moves applied after that move is applied.
	 * 
	 * @example (move Swap Pieces (last To) (last From))
	 */
	public static Moves construct
	(
		     final MoveSwapType  moveType,
		     final SwapSitesType swapType,
		@Opt final IntFunction   locA,
		@Opt final IntFunction   locB,
		@Opt final Then          then
	)
	{
		Moves moves = null;

		switch (moveType)
		{
		case Swap:
			moves = new SwapPieces(locA, locB,then);
			break;
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		if (moves == null)
			throw new IllegalArgumentException("Move(): A MoveSwapType is not implemented.");

		moves.setDecision();
		return moves;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * For deciding to remove components.
	 * 
	 * @param moveType         The type of move.
	 * @param type             The graph element type of the location [Cell (or
	 *                         Vertex if the main board uses this)].
	 * @param locationFunction The location to remove a piece.
	 * @param regionFunction   The locations to remove a piece.
	 * @param level            The level to remove a piece [top level].
	 * @param at               When to perform the removal [immediately].
	 * @param count            The number of pieces to remove [1].
	 * @param then             The moves applied after that move is applied.
	 * 
	 * @example (move Remove (last To))
	 */
	public static Moves construct
	(
			           final MoveRemoveType moveType,
		@Opt           final SiteType       type,
		     @Or       final IntFunction    locationFunction, 
		     @Or       final RegionFunction regionFunction, 
		@Opt     @Name final IntFunction    level,
		@Opt     @Name final WhenType       at,
	    @Opt     @Name final IntFunction    count,
		@Opt 	       final Then           then
	)
	{
		Moves moves = null;

		switch (moveType)
		{
		case Remove:
			moves = new Remove(type, locationFunction, regionFunction, level, at, count, then);
			break;
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		if (moves == null)
			throw new IllegalArgumentException("Move(): A MoveRemoveType is not implemented.");

		moves.setDecision();
		return moves;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * For deciding the trump suit of a card game.
	 * 
	 * @param moveType The type of move.
	 * @param setType  The type of property to set.
	 * @param suit     The suit to choose.
	 * @param suits    The possible suits to choose.
	 * @param then     The moves applied after that move is applied.
	 * 
	 * @example (move Set TrumpSuit (card Suit at:(handSite Shared)))
	 * 
	 */
	public static Moves construct
	(
			     final MoveSetType  moveType,
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
			throw new IllegalArgumentException(
					"Move(): With SetSuitType only one suit or suits parameter must be non-null.");

		Moves moves = null;

		switch (setType)
		{
		case TrumpSuit:
			moves = new SetTrumpSuit(suit, suits, then);
			break;
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		if (moves == null)
			throw new IllegalArgumentException("Move(): A SetSuitType is not implemented.");

		moves.setDecision();
		return moves;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * For deciding the next player.
	 * 
	 * @param moveType    The type of move.
	 * @param setType     The type of property to set.
	 * @param who         The data of the next player.
	 * @param nextPlayers The indices of the next players.
	 * @param then        The moves applied after that move is applied.
	 * 
	 * @example (move Set NextPlayer (player (mover)))
	 * 
	 */
	public static Moves construct
	(
			     final MoveSetType       moveType,
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
			throw new IllegalArgumentException(
					"Move(): With SetPlayerType only one who or nextPlayers parameter can be non-null.");

		Moves moves = null;

		switch (setType)
		{
		case NextPlayer:
			moves = new SetNextPlayer(who, nextPlayers, then);
			break;
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		if (moves == null)
			throw new IllegalArgumentException("Move(): A SetPlayerType is not implemented.");

		moves.setDecision();
		return moves;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * For deciding to set the rotation.
	 * 
	 * @param moveType   The type of move.
	 * @param setType    The type of property to set.
	 * @param to         Description of the ``to'' location [(to (from))].
	 * @param directions The index of the possible new directions.
	 * @param direction  The index of the possible new direction.
	 * @param previous   True to allow movement to the left [True].
	 * @param next       True to allow movement to the right [True].
	 * @param then       The moves applied after that move is applied.
	 * 
	 * @example (move Set Rotation)
	 * 
	 * @example (move Set Rotation (to (last To)) next:False)
	 */
	public static Moves construct
	(
	                   final MoveSetType        moveType,
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
					"Move(): With SetRotationType zero or one directions or direction parameter must be non-null.");

		Moves moves = null;

		switch (setType)
		{
		case Rotation:
			moves = new SetRotation(to, directions, direction, previous, next, then);
			break;
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		if (moves == null)
			throw new IllegalArgumentException("Move(): A SetRotationType is not implemented.");

		moves.setDecision();
		return moves;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * For deciding to step.
	 * 
	 * @param moveType   The type of move.
	 * @param from       Description of ``from'' location [(from)].
	 * @param directions The directions of the move [Adjacent].
	 * @param to         Description of the ``to'' location.
	 * @param stack      True if the move is applied to a stack [False].
	 * @param then       Moves to apply after this one.
	 * 
	 * @example (move Step (to if:(is Empty (to))) )
	 * 
	 * @example (move Step Forward (to if:(is Empty (to))) )
	 * 
	 * @example (move Step (directions {FR FL}) (to if:(or (is Empty (to)) (is
	 *          Enemy (who at:(to)))) (apply (remove (to)))) )
	 */
	public static Moves construct
	(
		           final MoveStepType                    moveType,
		@Opt 	   final game.util.moves.From            from,
		@Opt       final game.util.directions.Direction  directions,
			  	   final game.util.moves.To              to,
		@Opt @Name final Boolean                         stack,
		@Opt 	   final Then                            then
	)
	{
		Moves moves = null;

		switch (moveType)
		{
		case Step:
			moves = new Step(from, directions, to, stack, then);
			break;
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		if (moves == null)
			throw new IllegalArgumentException("Move(): A MoveStepType is not implemented.");

		moves.setDecision();
		return moves;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * For deciding to slide.
	 * 
	 * @param moveType   The type of move.
	 * @param from       Description of the ``from'' location [(from)].
	 * @param track      The track on which to slide.
	 * @param directions The directions of the move [Adjacent].
	 * @param between    Description of the location(s) between ``from'' and ``to''.
	 * @param to         Description of the ``to'' location.
	 * @param stack      True if the move is applied to a stack [False].
	 * @param then       Moves to apply after this one.
	 * 
	 * @example (move Slide)
	 * 
	 * @example (move Slide Orthogonal)
	 * 
	 * @example (move Slide "AllTracks" (between if:(or (= (between) (from)) (is In
	 *          (between) (sites Empty)) ) ) (to if:(is Enemy (who at:(to))) (apply
	 *          (remove (to))) ) (then (set Counter)) )
	 * 
	 */
	public static Moves construct
	(
		           final MoveSlideType                  moveType,
		@Opt       final game.util.moves.From           from,
		@Opt       final String                         track,
		@Opt       final game.util.directions.Direction directions, 
		@Opt       final game.util.moves.Between        between,
		@Opt       final To 	                        to,
		@Opt @Name final Boolean                        stack,
		@Opt       final Then                           then
	)
	{
		Moves moves = null;

		switch (moveType)
		{
		case Slide:
			moves = new Slide(from, track, directions, between, to, stack, then);
			break;
		default:
			break;
		}
		// We should never reach that except if we forget some codes.
		if (moves == null)
			throw new IllegalArgumentException("Move(): A MoveSlideType is not implemented.");

		moves.setDecision();
		return moves;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * For deciding to shoot.
	 * 
	 * @param moveType The type of move.
	 * @param what     The data about the piece to shoot.
	 * @param from     The ``from'' location [(lastTo)].
	 * @param dirn     The direction to follow [Adjacent].
	 * @param between  The location(s) between ``from'' and ``to''.
	 * @param to       The condition on the ``to'' location to allow shooting [(to
	 *                 if:(in (to) (sites Empty)))].
	 * @param then     The moves applied after that move is applied.
	 * 
	 * @example (move Shoot (piece "Dot0"))
	 * 
	 */
	public static Moves construct
	(
		     final MoveShootType           moveType,
	         final Piece 		           what, 
	   @Opt  final From 		           from,
	   @Opt  final AbsoluteDirection       dirn,
	   @Opt  final game.util.moves.Between between,
	   @Opt  final To 	                   to,
	   @Opt  final Then 			       then
	)
	{
		Moves moves = null;

		switch (moveType)
		{
		case Shoot:
			moves = new Shoot(what, from, dirn, between, to, then);
			break;
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		if (moves == null)
			throw new IllegalArgumentException("Move(): A MoveShootType is not implemented.");

		moves.setDecision();
		return moves;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * For deciding to select sites.
	 * 
	 * @param moveType The type of move.
	 * @param from     Describes the ``from'' location to select [(from)].
	 * @param to       Describes the ``to'' location to select.
	 * @param mover    The mover of the move.
	 * @param then     The moves applied after that move is applied.
	 * 
	 * @example (move Select (from) (then (remove (last To))))
	 * 
	 * @example (move Select (from (sites Occupied by:Mover) if:(!= (state at:(to))
	 *          0) ) (to (sites Occupied by:Next) if:(!= (state at:(to)) 0) ) (then
	 *          (set State at:(last To) (% (+ (state at:(last From)) (state at:(last
	 *          To))) 5) ) ) )
	 * 
	 */
	public static Moves construct
	(
		     final MoveSelectType moveType,
			 final From           from,
		@Opt final To             to,
		@Opt final RoleType       mover,
		@Opt final Then           then
	)
	{
		Moves moves = null;

		switch (moveType)
		{
		case Select:
			moves = new Select(from, to, mover, then);
			break;
		default:
			break;
		}
		// We should never reach that except if we forget some codes.
		if (moves == null)
			throw new IllegalArgumentException("Move(): A MoveSelectType is not implemented.");

		moves.setDecision();
		return moves;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * For deciding to vote or propose.
	 * 
	 * @param moveType The type of move.
	 * @param message  The message.
	 * @param messages The messages.
	 * @param then     The moves applied after that move is applied.
	 * 
	 * @example (move Propose "End")
	 * 
	 * @example (move Vote "End")
	 * 
	 */
	public static Moves construct
	(
		         final MoveMessageType moveType,
		     @Or final String          message,
		     @Or final String[]        messages,
		@Opt     final Then            then
	)
	{
		Moves moves = null;

		switch (moveType)
		{
		case Propose:
			moves = new Propose(message, messages, then);
			break;
		case Vote:
			moves = new Vote(message, messages, then);
			break;
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		if (moves == null)
			throw new IllegalArgumentException("Move(): A MoveProposeType is not implemented.");

		moves.setDecision();
		return moves;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * For deciding to promote.
	 * 
	 * @param moveType   The type of move.
	 * @param type       The graph element type [default SiteType of the board].
	 * @param locationFn The location of the piece to promote [(to)].
	 * @param what       The data about the promoted pieces.
	 * @param who        Data of the owner of the promoted piece.
	 * @param role       RoleType of the owner of the promoted piece.
	 * @param then       The moves applied after that move is applied.
	 * 
	 * @example (move Promote (last To) (piece {"Queen" "Knight" "Bishop" "Rook"})
	 *          Mover)
	 * 
	 */
	public static Moves construct
	(
			     final MovePromoteType moveType,
		@Opt     final SiteType        type,
		@Opt     final IntFunction     locationFn,
	             final Piece           what,
		@Opt @Or final Player          who,
		@Opt @Or final RoleType        role, 
		@Opt     final Then            then
	)
	{
		Moves moves = null;

		switch (moveType)
		{
		case Promote:
			moves = new Promote(type, locationFn, what, who, role, then);
			break;
		default:
			break;
		}
		// We should never reach that except if we forget some codes.
		if (moves == null)
			throw new IllegalArgumentException("Move(): A MovePromotionType is not implemented.");

		moves.setDecision();
		return moves;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * For deciding to pass or play a card.
	 * 
	 * @param moveType The type of move.
	 * @param then     The moves applied after that move is applied.
	 * 
	 * @example (move Pass)
	 */
	public static Moves construct
	(
			 final MoveSimpleType moveType,
		@Opt final Then           then
	)
	{
		Moves moves = null;

		switch (moveType)
		{
		case Pass:
			moves = new Pass(then);
			break;
		case PlayCard:
			moves = new PlayCard(then);
			break;
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		if (moves == null)
			throw new IllegalArgumentException("Move(): A MoveSimpleType is not implemented.");

		moves.setDecision();
		return moves;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * For deciding to leap.
	 * 
	 * @param moveType  The type of move.
	 * @param from      The from location [(from)].
	 * @param walk      The walk to follow.
	 * @param forward   True if the move can only move forward according to the
	 *                  direction of the piece [False].
	 * @param rotations True if the move includes all the rotations of the walk
	 *                  [True].
	 * @param to        The data about the location to move.
	 * @param then      The moves applied after that move is applied.
	 * 
	 * @example (move Leap { {F F R F} {F F L F} } (to if:(or (is Empty (to)) (is
	 *          Enemy (who at:(to)))) (apply (if (is Enemy (who at:(to))) (remove
	 *          (to) ) ) ) ) )
	 * 
	 */
	public static Moves construct
	(
			       final MoveLeapType         moveType,
		@Opt       final game.util.moves.From from,
				   final StepType[][]         walk,
		@Opt @Name final BooleanFunction      forward,
		@Opt @Name final BooleanFunction      rotations,
		 		   final To                   to,
		@Opt 	   final Then                 then
	)
	{
		Moves moves = null;

		switch (moveType)
		{
		case Leap:
			moves = new Leap(from, walk, forward, rotations, to, then);
			break;
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		if (moves == null)
			throw new IllegalArgumentException("Move(): A MoveLeapType is not implemented.");

		moves.setDecision();
		return moves;
	}

	//-------------------------------------------------------------------------

	/**
	 * For deciding to hop.
	 * 
	 * @param moveType   The type of move.
	 * @param from       The data of the from location [(from)].
	 * @param directions The directions of the move [Adjacent].
	 * @param between    The information about the locations between ``from'' and
	 *                   ``to'' [(between if:True)].
	 * @param to         The condition on the location to move.
	 * @param stack      True if the move has to be applied for stack [False].
	 * @param then       The moves applied after that move is applied.
	 * 
	 * @example (move Hop (between if:(is Enemy (who at:(between))) (apply (remove
	 *          (between))) ) (to if:(is Empty (to))) )
	 * 
	 * @example (move Hop Orthogonal (between if:(is Friend (who at:(between)))
	 *          (apply (remove (between))) ) (to if:(is Empty (to))) )
	 * 
	 */
	public static Moves construct
	(
			       final MoveHopType                    moveType,
		@Opt       final game.util.moves.From           from,
		@Opt       final game.util.directions.Direction directions,
		@Opt       final game.util.moves.Between        between,
			       final To                             to,
		@Opt @Name final Boolean                        stack,
		@Opt 	   final Then                           then
	)
	{
		Moves moves = null;

		switch (moveType)
		{
		case Hop:
			moves = new Hop(from, directions, between, to, stack, then);
			break;
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		if (moves == null)
			throw new IllegalArgumentException("Move(): A MoveHopType is not implemented.");

		moves.setDecision();
		return moves;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * For deciding to move a piece.
	 * 
	 * @param from  The data of the ``from'' location [(from)].
	 * @param to    The data of the ``to'' location.
	 * @param count The number of pieces to move.
	 * @param copy  Whether to duplicate the piece rather than moving it [False].
	 * @param stack To move a complete stack [False].
	 * @param mover The mover of the move.
	 * @param then  The moves applied after that move is applied.
	 * 
	 * @example (move (from (last To)) (to (last From)))
	 *
	 * @example (move (from (handSite Mover)) (to (sites Empty)))
	 * 
	 * @example (move (from (to)) (to (sites Empty)) count:(count at:(to)))
	 * 
	 * @example (move (from (handSite Shared)) (to (sites Empty)) copy:True )
	 * 
	 */
	public static Moves construct
	(
		 			final From            from, 
		 			final To              to,
		@Opt @Name  final IntFunction     count,
		@Opt @Name  final BooleanFunction copy,
		@Opt @Name  final Boolean         stack,
		@Opt 		final RoleType        mover,
		@Opt		final Then            then
	)
	{
		final Moves moves = new FromTo(from, to, count, copy, stack, mover, then);
		moves.setDecision();
		return moves;
	}
	
	/**
	 * For deciding to bet.
	 * 
	 * @param moveType The type of move.
	 * @param who      The data about the player to bet.
	 * @param role     The RoleType of the player to bet.
	 * @param range    The range of the bet.
	 * @param then     The moves applied after that move is applied.
	 * 
	 * @example (move Bet P1 (range 0 5))
	 * 
	 */
	public static Moves construct
	(
			     final MoveBetType   moveType,
			 @Or final Player        who,
			 @Or final RoleType      role,
			     final RangeFunction range,
		@Opt     final Then          then
	)
	{
		Moves moves = null;

		switch (moveType)
		{
		case Bet:
			moves = new Bet(who, role, range, then);
			break;
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		if (moves == null)
			throw new IllegalArgumentException("Move(): A MoveBetType is not implemented.");

		moves.setDecision();
		return moves;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * For deciding to add a piece or claim a site.
	 * 
	 * @param moveType The type of move.
	 * @param what     The data about the components to add.
	 * @param to       The data on the location to add.
	 * @param count    The number of components to add [1].
	 * @param stack    True if the move has to be applied on a stack [False].
	 * @param then     The moves applied after that move is applied.
	 * 
	 * @example (move Add (to (sites Empty)))
	 * 
	 * @example (move Claim (to Cell (site)))
	 */
	public static Moves construct
	(
			       final MoveSiteType moveType,
		@Opt       final Piece        what,
			       final To           to,
		@Opt @Name final IntFunction  count,
		@Opt @Name final Boolean      stack,
		@Opt       final Then         then
	)
	{
		Moves moves = null;

		switch (moveType)
		{
		case Add:
			moves = new Add(what, to, count, stack, then);
			break;
		case Claim:
			moves = new Claim(what, to, then);
			break;
		default:
			break;
		}
		// We should never reach that except if we forget some codes.
		if (moves == null)
			throw new IllegalArgumentException("Move(): A MoveAddType is not implemented.");

		moves.setDecision();
		return moves;
	}
	
	private Move()
	{
		super(null);
		// Ensure that compiler does pick up default constructor
	}

	@Override
	public Moves eval(final Context context)
	{
		// Should not be called, should only be called on subclasses
		throw new UnsupportedOperationException("Move.eval(): Should never be called directly.");
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
		throw new UnsupportedOperationException("Move.canMoveTo(): Should never be called directly.");
	}
	
	//-------------------------------------------------------------------------

}
