package game.functions.ints.count;

import annotations.Name;
import annotations.Opt;
import annotations.Or;
import annotations.Or2;
import game.Game;
import game.functions.booleans.BooleanFunction;
import game.functions.intArray.IntArrayFunction;
import game.functions.ints.BaseIntFunction;
import game.functions.ints.IntFunction;
import game.functions.ints.count.component.CountPieces;
import game.functions.ints.count.component.CountPips;
import game.functions.ints.count.groups.CountGroups;
import game.functions.ints.count.sizeBiggestGroup.CountSizeBiggestGroup;
import game.functions.ints.count.sizeBiggestLine.CountSizeBiggestLine;
import game.functions.ints.count.sitesPlatformBelow.CountSitesPlatformBelow;
import game.functions.ints.count.liberties.CountLiberties;
import game.functions.ints.count.simple.CountActive;
import game.functions.ints.count.simple.CountCells;
import game.functions.ints.count.simple.CountColumns;
import game.functions.ints.count.simple.CountEdges;
import game.functions.ints.count.simple.CountMoves;
import game.functions.ints.count.simple.CountMovesThisTurn;
import game.functions.ints.count.simple.CountLegalMoves;
import game.functions.ints.count.simple.CountPhases;
import game.functions.ints.count.simple.CountPlayers;
import game.functions.ints.count.simple.CountRows;
import game.functions.ints.count.simple.CountTrials;
import game.functions.ints.count.simple.CountTurns;
import game.functions.ints.count.simple.CountVertices;
import game.functions.ints.count.site.CountAdjacent;
import game.functions.ints.count.site.CountDiagonal;
import game.functions.ints.count.site.CountNeighbours;
import game.functions.ints.count.site.CountNumber;
import game.functions.ints.count.site.CountOff;
import game.functions.ints.count.site.CountOrthogonal;
import game.functions.ints.count.site.CountSites;
import game.functions.ints.count.stack.CountStack;
import game.functions.ints.count.steps.CountSteps;
import game.functions.ints.count.stepsOnTrack.CountStepsOnTrack;
import game.functions.ints.count.value.CountValue;
import game.functions.region.RegionFunction;
import game.rules.play.moves.nonDecision.effect.Step;
import game.types.board.RelationType;
import game.types.board.SiteType;
import game.types.play.RoleType;
import game.util.directions.Direction;
import game.util.directions.AbsoluteDirection;
import game.util.directions.StackDirection;
import other.IntArrayFromRegion;
import other.context.Context;

/**
 * Returns the count of the specified property.
 * 
 * @author Eric Piette
 */
@SuppressWarnings("javadoc")
public final class Count extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * For counting the number of identical values in an array.
	 * 
	 * @param countType  The property to count.
	 * @param of         The value to count.
	 * @param in         The array.
	 * 
	 * @example (count Value 1 in:(values Remembered))
	 */
	public static IntFunction construct
	(
			  final CountValueType   countType, 
	          final IntFunction 	 of, 
	    @Name final IntArrayFunction in 
	)
	{
		switch (countType)
		{
		case Value:
			return new CountValue(of,in);
		default:
			break;
		}
		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Count(): A CountValueType is not implemented.");
	}
	
	/**
	 * For counting according to no parameters or only a graph element type.
	 * 
	 * @param countType      The property to count.
	 * @param stackDirection The direction to count in the stack [FromBottom].
	 * @param type           The graph element type [default SiteType of the board].
	 * @param at             The site where is the stack.
	 * @param to             The region where are the stacks.
	 * @param If             The condition to count in the stack [True].
	 * @param stop           The condition to stop to count in the stack [False].
	 * 
	 * 
	 * @example (count Stack FromTop at:(last To) if:(= (what at:(to) level:(level))
	 *          (id "Disc" P1)) stop:(= (what at:(to) level:(level)) (id "Disc"
	 *          P2)))
	 */
	public static IntFunction construct
	(
			           final CountStackType  countType, 
		    @Opt       final StackDirection  stackDirection,
		    @Opt       final SiteType 		 type,
		    @Or  @Name final IntFunction 	 at,
		    @Or  @Name final RegionFunction  to,
		    @Opt @Name final BooleanFunction If,
		    @Opt @Name final BooleanFunction stop
 		
	)
	{
		int numNonNull = 0;
		if (at != null)
			numNonNull++;
		if (to != null)
			numNonNull++;

		if (numNonNull != 1)
			throw new IllegalArgumentException(
					"Count(): With CountStackType one 'at', 'to' parameters must be non-null.");
		
		switch (countType)
		{
		case Stack:
			return new CountStack(stackDirection, type, new IntArrayFromRegion(at, to), If, stop);
		default:
			break;
		}
		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Count(): A CountSimpleType is not implemented.");
	}

	//-------------------------------------------------------------------------

	/**
	 * For counting according to no parameters or only a graph element type.
	 * 
	 * @param countType The property to count.
	 * @param type      The graph element type [default SiteType of the board].
	 * 
	 * @example (count Players)
	 * 
	 * @example (count Vertices)
	 * 
	 * @example (count Moves)
	 */
	public static IntFunction construct
	(
		     final CountSimpleType countType,
		@Opt final SiteType 	   type
	)
	{
		switch (countType)
		{
		case Active:
			return new CountActive();
		case Cells:
			return new CountCells();
		case Columns:
			return new CountColumns(type);
		case Edges:
			return new CountEdges();
		case Moves:
			return new CountMoves();
		case MovesThisTurn:
			return new CountMovesThisTurn();
		case Phases:
			return new CountPhases();
		case Players:
			return new CountPlayers();
		case Rows:
			return new CountRows(type);
		case Trials:
			return new CountTrials();
		case Turns:
			return new CountTurns();
		case Vertices:
			return new CountVertices();
		case LegalMoves:
			return new CountLegalMoves();
		default:
			break;
		}
		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Count(): A CountSimpleType is not implemented.");
	}

	//-------------------------------------------------------------------------

	/**
	 * For counting according to a site or a region.
	 * 
	 * @param countType The property to count.
	 * @param type      The graph element type [default SiteType of the board].
	 * @param in        The region to count.
	 * @param at        The site from which to compute the count [(last To)].
	 * @param name      The name of the container from which to count the number of
	 *                  sites or the name of the piece to count only pieces of that
	 *                  type.
	 * @param who  		Player id the counted items belong to
	 * 
	 * @example (count at:(last To))
	 * 
	 * @example (count Sites in:(sites Empty))
	 */
	public static IntFunction construct
	(
		@Opt            final CountSiteType  countType, 
		@Opt            final SiteType 		type,
		@Opt @Or @Name  final RegionFunction in, 
		@Opt @Or @Name  final IntFunction 	at, 
		@Opt @Or        final String 		name,
		@Opt	  @Name final RoleType 		who,
		@Opt @Or2 @Name final IntFunction 	what,
		@Opt @Or2 @Name final IntFunction[] whats
		
	)
	{
		int numNonNull = 0;
		if (in != null)
			numNonNull++;
		if (at != null)
			numNonNull++;
		if (name != null)
			numNonNull++;

		if (numNonNull > 1)
			throw new IllegalArgumentException(
					"Count(): With CountSiteType zero or one 'in', 'at' or 'name' parameters must be non-null.");

		if (countType == null)
			return new CountNumber(type, in, at);
		switch (countType)
		{
		case Adjacent:
			return new CountAdjacent(type, in, at);
		case Diagonal:
			return new CountDiagonal(type, in, at);
		case Neighbours:
			return new CountNeighbours(type, in, at);
		case Off:
			return new CountOff(type, in, at);
		case Orthogonal:
			return new CountOrthogonal(type, in, at);
		case Sites:
			return new CountSites(in, at, name);
		case SitesPlatformBelow:
			return new CountSitesPlatformBelow(type, at, who, what, whats);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Count(): A CountSiteType is not implemented.");
	}

	//-------------------------------------------------------------------------

	/**
	 * For counting according to a component.
	 * 
	 * @param countType The property to count.
	 * @param type      The graph element type [default SiteType of the board].
	 * @param role      The role of the player [All].
	 * @param of        The index of the player.
	 * @param name      The name of the container from which to count the number of
	 *                  sites or the name of the piece to count only pieces of that
	 *                  type.
	 * @param in        The region where to count the pieces.
	 * @param If        The condition to check for each site where is the piece [True].
	 * 
	 * @example (count Pieces Mover)
	 * 
	 * @example (count Pips)
	 */
	public static IntFunction construct
	(
		               final CountComponentType countType, 
		@Opt           final SiteType 		    type,
		@Opt @Or       final RoleType 			role, 
		@Opt @Or @Name final IntFunction 		of, 
		@Opt           final String 			name,
		@Opt @Name     final RegionFunction  	in,
		@Opt @Name     final BooleanFunction  	If
	)
	{
		int numNonNull = 0;
		if (role != null)
			numNonNull++;
		if (of != null)
			numNonNull++;

		if (numNonNull > 1)
			throw new IllegalArgumentException(
					"Count(): With CountComponentType zero or one 'role' or 'of' parameters must be non-null.");

		switch (countType)
		{
		case Pieces:
			return new CountPieces(type, role, of, name, in, If);
		case Pips:
			return new CountPips(role, of);
		default:
			break;
		}
		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Count(): A CountComponentType is not implemented.");
	}

	//-------------------------------------------------------------------------

	/**
	 * For counting elements in a group.
	 * 
	 * @param countType  The property to count.
	 * @param type       The graph element type [default SiteType of the board].
	 * @param directions The directions of the connection between elements in the
	 *                   group [Adjacent].
	 * @param If         The condition on the pieces to include in the group [(is Occupied (to))].
	 * @param min        Minimum size of each group [0].
	 * @param isVisible  Whether the group has to be visibly connected or not (a connection can not be hidden by another component)
	 * 
	 * @example (count Groups Orthogonal)
	 */
	public static IntFunction construct
	(
			       final CountGroupsType   countType,
		@Opt 	   final SiteType          type,
		@Opt       final Direction         directions,
		@Opt	   final RegionFunction  throughAny,
		@Opt @Name final BooleanFunction   If,
		@Opt @Name final IntFunction       min,
		@Opt @Name final BooleanFunction   isVisible
	)
	{
		switch (countType)
		{
		case Groups:
			return new CountGroups(type, directions, If, min);
		case SizeBiggestGroup:
			return new CountSizeBiggestGroup(type, directions, throughAny, If, isVisible);
		default:
			break;
		}
		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Count(): A CountGroupsType is not implemented.");
	}
	
	//-------------------------------------------------------------------------

		/**
		 * For counting elements in a line.
		 * 
		 * @param countType  The property to count.
		 * @param type       The graph element type [default SiteType of the board].
		 * @param directions The directions of the connection between elements in the
		 *                   line [Adjacent].
		 * @param If         The condition on the pieces to include in the line [(is Occupied (to))].
		 * 
		 * @example (count SizeBiggestLine Orthogonal)
		 */
		public static IntFunction construct
		(
				       final CountLinesType    countType,
			@Opt 	   final SiteType          type,
			@Opt       final AbsoluteDirection directions,
			@Opt @Name final BooleanFunction   If
		)
		{
			switch (countType)
			{
			case SizeBiggestLine:
				return new CountSizeBiggestLine(type, directions, If);
			default:
				break;
			}
			// We should never reach that except if we forget some codes.
			throw new IllegalArgumentException("Count(): A CountLineType is not implemented.");
		}
	
	//-------------------------------------------------------------------------

	/**
	 * For counting elements in a region of liberties.
	 * 
	 * @param countType  The property to count.
	 * @param type       The graph element type [default SiteType of the board].
	 * @param at         The site to compute the group [(last To)].
	 * @param directions The type of directions from the site to compute the group
	 *                   [Adjacent].
	 * @param If         The condition of the members of the group [(= (mover) (who
	 *                   at:(to)))].
	 * 
	 * @example (count Liberties Orthogonal)
	 */
	public static IntFunction construct
	(
			           final CountLibertiesType countType,
			@Opt	   final SiteType 			type,
			@Opt @Name final IntFunction 		at,
			@Opt       final Direction    		directions,
			@Opt @Name final BooleanFunction 	If
	)
	{
		switch (countType)
		{
		case Liberties:
			return new CountLiberties(type, at, directions, If);
		default:
			break;
		}
		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Count(): A CountLibertiesType is not implemented.");
	}

	//-------------------------------------------------------------------------
	
	/**
	 * For counting the number of steps between two sites.
	 * 
	 * @param countType   The property to count.
	 * @param type        Graph element type [default SiteType of the board].
	 * @param relation    The relation type of the steps [Adjacent].
	 * @param stepMove    Define a particular step move to step.
	 * @param newRotation Define a new rotation at each step move in using the (value) iterator for the rotation.
	 * @param site1       The first site.
	 * @param site2       The second site.
	 * @param region2     The second region.
	 * 
	 * @example (count Steps (where (id "King")) (where (id "Queen")))
	 */
	public static IntFunction construct
	(
			          final CountStepsType    countType,
		@Opt          final SiteType          type,
		@Opt          final RelationType      relation,
	    @Opt          final Step              stepMove,
		@Opt @Name	  final IntFunction       newRotation,
		              final IntFunction       site1,
		          @Or final IntFunction       site2, 
		          @Or final RegionFunction    region2
	)
	{
		switch (countType)
		{
		case Steps:
			return new CountSteps(type, relation, stepMove, newRotation, site1, new IntArrayFromRegion(site2, region2));
		default:
			break;
		}
		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Count(): A CountStepsType is not implemented.");
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * For counting the number of steps between two sites.
	 * 
	 * @param countType The property to count.
	 * @param player    The owner of the track [(mover)].
	 * @param role      The role of the owner of the track [Mover].
	 * @param name      The name of the track.
	 * @param site1     The first site.
	 * @param site2     The second site.
	 * 
	 * @example (count StepsOnTrack (last From) (last To))
	 */
	public static IntFunction construct
	(
			      final CountStepsOnTrackType  countType,
		@Opt @Or  final RoleType 			   role,
		@Opt @Or  final game.util.moves.Player player,
		@Opt @Or  final String      		   name,
		@Opt      final IntFunction 		   site1,
		@Opt      final IntFunction 		   site2
	)
	{
		switch (countType)
		{
		case StepsOnTrack:
			return new CountStepsOnTrack(role,player,name, site1, site2);
		default:
			break;
		}
		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Count(): A CountStepsOnTrackType is not implemented.");
	}
	
	private Count()
	{
		// Make grammar pick up construct() and not default constructor
	}
	
	//-------------------------------------------------------------------------

	@Override
	public int eval(final Context context)
	{
		// Should not be called, should only be called on subclasses
		throw new UnsupportedOperationException("Count.eval(): Should never be called directly.");

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