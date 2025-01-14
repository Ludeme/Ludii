package game.functions.region.sites;

import annotations.Name;
import annotations.Opt;
import annotations.Or;
import annotations.Or2;
import game.Game;
import game.functions.booleans.BooleanFunction;
import game.functions.intArray.IntArrayFunction;
import game.functions.ints.IntFunction;
import game.functions.range.RangeFunction;
import game.functions.region.BaseRegionFunction;
import game.functions.region.RegionFunction;
import game.functions.region.sites.around.SitesAround;
import game.functions.region.sites.between.SitesBetween;
import game.functions.region.sites.context.SitesContext;
import game.functions.region.sites.coords.SitesCoords;
import game.functions.region.sites.crossing.SitesCrossing;
import game.functions.region.sites.custom.SitesCustom;
import game.functions.region.sites.direction.SitesDirection;
import game.functions.region.sites.distance.SitesDistance;
import game.functions.region.sites.edges.SitesAngled;
import game.functions.region.sites.edges.SitesAxial;
import game.functions.region.sites.edges.SitesHorizontal;
import game.functions.region.sites.edges.SitesSlash;
import game.functions.region.sites.edges.SitesSlosh;
import game.functions.region.sites.edges.SitesVertical;
import game.functions.region.sites.group.SitesGroup;
import game.functions.region.sites.hidden.SitesHidden;
import game.functions.region.sites.hidden.SitesHiddenCount;
import game.functions.region.sites.hidden.SitesHiddenRotation;
import game.functions.region.sites.hidden.SitesHiddenState;
import game.functions.region.sites.hidden.SitesHiddenValue;
import game.functions.region.sites.hidden.SitesHiddenWhat;
import game.functions.region.sites.hidden.SitesHiddenWho;
import game.functions.region.sites.incidents.SitesIncident;
import game.functions.region.sites.index.SitesCell;
import game.functions.region.sites.index.SitesColumn;
import game.functions.region.sites.index.SitesEdge;
import game.functions.region.sites.index.SitesEmpty;
import game.functions.region.sites.index.SitesSupport;
import game.functions.region.sites.index.SitesLayer;
import game.functions.region.sites.index.SitesPhase;
import game.functions.region.sites.index.SitesRow;
import game.functions.region.sites.index.SitesState;
import game.functions.region.sites.largePiece.SitesLargePiece;
import game.functions.region.sites.lineOfSight.SitesLineOfSight;
import game.functions.region.sites.loop.SitesLoop;
import game.functions.region.sites.moves.SitesFrom;
import game.functions.region.sites.moves.SitesTo;
import game.functions.region.sites.occupied.SitesOccupied;
import game.functions.region.sites.pattern.SitesPattern;
import game.functions.region.sites.piece.SitesStart;
import game.functions.region.sites.player.SitesEquipmentRegion;
import game.functions.region.sites.player.SitesHand;
import game.functions.region.sites.player.SitesWinning;
import game.functions.region.sites.random.SitesRandom;
import game.functions.region.sites.side.SitesSide;
import game.functions.region.sites.simple.SitesBoard;
import game.functions.region.sites.simple.SitesBottom;
import game.functions.region.sites.simple.SitesCentre;
import game.functions.region.sites.simple.SitesConcaveCorners;
import game.functions.region.sites.simple.SitesConvexCorners;
import game.functions.region.sites.simple.SitesCorners;
import game.functions.region.sites.simple.SitesHint;
import game.functions.region.sites.simple.SitesInner;
import game.functions.region.sites.simple.SitesLastFrom;
import game.functions.region.sites.simple.SitesLastTo;
import game.functions.region.sites.simple.SitesLeft;
import game.functions.region.sites.simple.SitesLineOfPlay;
import game.functions.region.sites.simple.SitesMajor;
import game.functions.region.sites.simple.SitesMinor;
import game.functions.region.sites.simple.SitesOuter;
import game.functions.region.sites.simple.SitesPending;
import game.functions.region.sites.simple.SitesPerimeter;
import game.functions.region.sites.simple.SitesPlayable;
import game.functions.region.sites.simple.SitesRight;
import game.functions.region.sites.simple.SitesToClear;
import game.functions.region.sites.simple.SitesTop;
import game.functions.region.sites.track.SitesTrack;
import game.functions.region.sites.walk.SitesWalk;
import game.rules.play.moves.Moves;
import game.rules.play.moves.nonDecision.NonDecision;
import game.rules.play.moves.nonDecision.effect.Step;
import game.types.board.HiddenData;
import game.types.board.RegionTypeDynamic;
import game.types.board.RelationType;
import game.types.board.SiteType;
import game.types.board.StepType;
import game.types.play.RoleType;
import game.util.directions.AbsoluteDirection;
import game.util.directions.CompassDirection;
import game.util.directions.Direction;
import game.util.equipment.Region;
import game.util.moves.Piece;
import game.util.moves.Player;
import main.StringRoutines;
import other.context.Context;

/**
 * Returns the specified set of sites.
 * 
 * @author Eric.Piette
 */
@SuppressWarnings("javadoc")
public final class Sites extends BaseRegionFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------
	
	/**
	 * For getting the sites iterated in ForEach Moves.
	 * 
	 * @example (sites)
	 */
	public static RegionFunction construct()
	{
		return new SitesContext();
	}

	//-------------------------------------------------------------------------

	/**
	 * For getting the sites in a loop or making the loop.
	 * 
	 * @param regionType   Type of sites to return.
	 * @param inside       True to return the sites inside the loop [False].
	 * @param type         The graph element type [default SiteType of the board].
	 * @param surround     Used to define the inside condition of the loop.
	 * @param surroundList The list of items inside the loop.
	 * @param directions   The direction of the connection [Adjacent].
	 * @param colour       The owner of the looping pieces [Mover].
	 * @param start        The starting point of the loop [(last To)].
	 * @param regionStart  The region to start to detect the loop.
	 * 
	 * @example (sites Loop)
	 */
	public static RegionFunction construct
	(		
					    final SitesLoopType   regionType,
		 	 @Opt @Name final BooleanFunction inside,
		     @Opt       final SiteType        type,
		@Or	 @Opt @Name final RoleType        surround,
		@Or  @Opt	    final RoleType[]      surroundList,
		     @Opt       final Direction       directions,
			 @Opt       final IntFunction     colour,
		@Or2 @Opt       final IntFunction     start,
		@Or2 @Opt       final RegionFunction  regionStart
	)
	{
		int numNonNull = 0;
		if (surround != null)
			numNonNull++;
		if (surroundList != null)
			numNonNull++;

		if (numNonNull > 1)
			throw new IllegalArgumentException(
					"Is(): With SitesLoopType zero or one surround or surroundList parameter can be non-null.");

		int numNonNull2 = 0;
		if (start != null)
			numNonNull2++;
		if (regionStart != null)
			numNonNull2++;

		if (numNonNull2 > 1)
			throw new IllegalArgumentException(
					"Is(): With SitesLoopType zero or one start or regionStart parameter can be non-null.");

		switch (regionType)
		{
		case Loop:
			return new SitesLoop(inside, type, surround, surroundList, directions, colour, start, regionStart);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Sites(): A SitesLoopType is not implemented.");
	}
	
	//-------------------------------------------------------------------------

	/**
	 * For getting the sites in a pattern.
	 * 
	 * @param regionType Type of sites to return.
	 * @param isType     The type of query to perform.
	 * @param walk       The walk describing the pattern.
	 * @param type       The type of the site from to detect the pattern.
	 * @param from       The site from to detect the pattern [(last To)].
	 * @param what       The piece to check in the pattern [piece in from].
	 * @param whats      The sequence of pieces to check in the pattern [piece in
	 *                   from].
	 * 
	 * @example (sites Pattern {F R F R F})
	 */
	public static RegionFunction construct
	(		
					   final SitesPatternType regionType,
				   	   final StepType[]       walk,
		    @Opt       final SiteType         type,
			@Opt @Name final IntFunction      from,
		@Or @Opt @Name final IntFunction      what,
		@Or @Opt @Name final IntFunction[]    whats
	)
	{
		int numNonNull = 0;
		if (what != null)
			numNonNull++;
		if (whats != null)
			numNonNull++;

		if (numNonNull > 1)
			throw new IllegalArgumentException("Only one parameter between what and whats can be non-null.");

		switch (regionType)
		{
		case Pattern:
			return new SitesPattern(walk, type, from, what, whats);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Is(): An SitesPatternType is not implemented.");
	}
	
	//-------------------------------------------------------------------------

	/**
	 * For getting the sites with specific hidden information for a player.
	 * 
	 * @param regionType Type of sites to return.
	 * @param dataType   The type of hidden data [Invisible].
	 * @param type       The graph element type [default of the board].
	 * @param to         The player with these hidden information.
	 * @param To         The roleType with these hidden information.
	 * 
	 * @example (sites Hidden to:Mover)
	 * @example (sites Hidden What to:(player (next)))
	 * @example (sites Hidden Rotation Vertex to:Next)
	 */
	public static RegionFunction construct
	(		
			         final SitesHiddenType regionType,
			    @Opt final HiddenData      dataType,
			    @Opt final SiteType        type,
	  @Name @Or      final Player          to, 
	  @Name @Or      final RoleType        To
	)
	{
		int numNonNull = 0;
		if (to != null)
			numNonNull++;
		if (To != null)
			numNonNull++;

		if (numNonNull != 1)
			throw new IllegalArgumentException("Set(): With SitesHiddenType one to or To parameter must be non-null.");
		
		switch (regionType)
		{
		case Hidden:
			if (dataType == null)
				return new SitesHidden(type, to, To);
			else
			{
				switch (dataType)
				{
				case What:
					return new SitesHiddenWhat(type, to, To);
				case Who:
					return new SitesHiddenWho(type, to, To);
				case Count:
					return new SitesHiddenCount(type, to, To);
				case State:
					return new SitesHiddenState(type, to, To);
				case Rotation:
					return new SitesHiddenRotation(type, to, To);
				case Value:
					return new SitesHiddenValue(type, to, To);
				}
			}
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Sites(): A SitesHiddenType is not implemented.");
	}
	
	//-------------------------------------------------------------------------

	/**
	 * For getting the sites (in the same radial) between two others sites.
	 * 
	 * @param regionType   Type of sites to return.
	 * @param directions   The directions of the move [Adjacent].
	 * @param type         The type of the graph element [Default SiteType].
	 * @param from         The 'from' site.
	 * @param fromIncluded True if the 'from' site is included in the result
	 *                     [False].
	 * @param to           The 'to' site.
	 * @param toIncluded   True if the 'to' site is included in the result [False].
	 * @param cond         The condition to include the site in between [True].
	 * 
	 * @example (sites Between from:(last From) to:(last To))
	 */
	public static RegionFunction construct
	(		
			       final SitesBetweenType               regionType,
       @Opt        final game.util.directions.Direction directions,
	   @Opt        final SiteType                       type,
		     @Name final IntFunction                    from,
	   @Opt  @Name final BooleanFunction                fromIncluded,
		     @Name final IntFunction                    to,
	   @Opt  @Name final BooleanFunction                toIncluded,
	   @Opt  @Name final BooleanFunction                cond
	)
	{
		switch (regionType)
		{
		case Between:
			return new SitesBetween(directions, type, from, fromIncluded, to, toIncluded, cond);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Sites(): A SitesBetweenType is not implemented.");
	}
	
	//-------------------------------------------------------------------------

	/**
	 * For getting the sites occupied by a large piece from the root of the large
	 * piece.
	 * 
	 * @param regionType Type of sites to return.
	 * @param type       The type of the graph element [Default SiteType].
	 * @param at         The site to look (the root).
	 * 
	 * @example (sites LargePiece at:(last To))
	 */
	public static RegionFunction construct
	(		
			       final SitesLargePieceType regionType,
		@Opt       final SiteType            type,
		     @Name final IntFunction         at
	)
	{
		switch (regionType)
		{
		case LargePiece:
			return new SitesLargePiece(type, at);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Sites(): A SitesLargePiece is not implemented.");
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * For getting a random site in a region.
	 * 
	 * @param regionType Type of sites to return.
	 * @param region     The region to get [(sites Empty Cell)].
	 * @param num        The number of sites to return [1].
	 * 
	 * @example (sites Random)
	 */
	public static RegionFunction construct
	(		
			       final SitesRandomType regionType,
		@Opt       final RegionFunction  region,
		@Opt @Name final IntFunction     num
	)
	{
		switch (regionType)
		{
		case Random:
			return new SitesRandom(region,num);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Sites(): A SitesRandomType is not implemented.");
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * For getting the sites crossing another site.
	 * 
	 * @param regionType Type of sites to return.
	 * @param at         The specific starting position needs to crossing check.
	 * @param who        The returned crossing items player type.
	 * @param role       The returned crossing items player role type.
	 * 
	 * @example (sites Crossing at:(last To) All)
	 */
	public static RegionFunction construct
	(		
			          final SitesCrossingType regionType,
	            @Name final IntFunction       at,	
	   @Opt @Or       final Player            who,
	   @Opt @Or       final RoleType          role
	)
	{
		int numNonNull = 0;
		if (who != null)
			numNonNull++;
		if (role != null)
			numNonNull++;

		if (numNonNull > 1)
			throw new IllegalArgumentException(
					"Sites(): With SitesCrossingType only one who or role parameter must be non-null.");

		switch (regionType)
		{
		case Crossing:
			return new SitesCrossing(at,who,role);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Sites(): A SitesCrossingType is not implemented.");
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * For getting the site of a group.
	 * 
	 * @param regionType Type of sites to return.
	 * @param type       The type of the graph elements of the group.
	 * @param at         The specific starting position of the group.
	 * @param From       The specific starting positions of the groups.
	 * @param directions The directions of the connection between elements in the
	 *                   group [Adjacent].
	 * @param If         The condition on the pieces to include in the group.
	 * @param IsVisible  Whether pieces of group have to be visibly connected
	 * 
	 * @example (sites Group Vertex at:(site))
	 */
	public static RegionFunction construct
	(		
			          final SitesGroupType  regionType, 
		  @Opt		  final SiteType        type,
	  @Or      @Name  final IntFunction     at, 
	  @Or      @Name  final RegionFunction  From,
	      @Opt        final Direction       directions,
	      @Opt @Name  final BooleanFunction If,
	      @Opt @Name  final BooleanFunction isVisible
	)
	{
		int numNonNull = 0;
		if (at != null)
			numNonNull++;
		if (From != null)
			numNonNull++;

		if (numNonNull != 1)
			throw new IllegalArgumentException(
					"Sites(): With SitesCrossingType only one at or From parameter must be non-null.");
		
		switch (regionType)
		{
		case Group:
			return new SitesGroup(type, at, From, directions, If, isVisible);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Sites(): A SitesGroupType is not implemented.");
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * For the sites relative to edges.
	 * 
	 * @param regionType Type of sites to return.
	 * 
	 * @example (sites Axial)
	 */
	public static RegionFunction construct
	(
		final SitesEdgeType regionType
	)
	{
		switch (regionType)
		{
		case Axial:
			return new SitesAxial();
		case Horizontal:
			return new SitesHorizontal();
		case Vertical:
			return new SitesVertical();
		case Angled:
			return new SitesAngled();
		case Slash:
			return new SitesSlash();
		case Slosh:
			return new SitesSlosh();
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Sites(): A SitesEdgeType is not implemented.");
	}

	//-------------------------------------------------------------------------

	/**
	 * For getting sites without any parameter or only the graph element type.
	 * 
	 * @param regionType  Type of sites to return.
	 * @param elementType The graph element type [default SiteType of the board].
	 * 
	 * @example (sites Top)
	 * @example (sites Playable)
	 * @example (sites Right Vertex)
	 */
	public static RegionFunction construct
	(
		      final SitesSimpleType regionType,
		@Opt  final SiteType        elementType
	)
	{
		switch (regionType)
		{
		case Board:
			return new SitesBoard(elementType);
		case Bottom:
			return new SitesBottom(elementType);
		case Corners:
			return new SitesCorners(elementType);
		case ConcaveCorners:
			return new SitesConcaveCorners(elementType);
		case ConvexCorners:
			return new SitesConvexCorners(elementType);
		case Hint:
			return new SitesHint();
		case Inner:
			return new SitesInner(elementType);
		case Left:
			return new SitesLeft(elementType);
		case LineOfPlay:
			return new SitesLineOfPlay();
		case Major:
			return new SitesMajor(elementType);
		case Minor:
			return new SitesMinor(elementType);
		case Outer:
			return new SitesOuter(elementType);
		case Right:
			return new SitesRight(elementType);
		case ToClear:
			return new SitesToClear();
		case Top:
			return new SitesTop(elementType);
		case Pending:
			return new SitesPending();
		case Playable:
			return new SitesPlayable();
		case LastTo:
			return new SitesLastTo();
		case LastFrom:
			return new SitesLastFrom();
		case Centre:
			return new SitesCentre(elementType);
		case Perimeter:
			return new SitesPerimeter(elementType);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Sites(): A SitesSimpleType is not implemented.");
	}

	//-------------------------------------------------------------------------
	
	/**
	 * For getting sites according to their coordinates.
	 * 
	 * @param elementType The graph element type [default SiteType of the board].
	 * @param coords      The sites corresponding to these coordinates.
	 *
	 * @example (sites {"A1" "B1" "A2" "B2"})
	 */
	public static RegionFunction construct
	(
		@Opt final SiteType elementType,
		     final String[] coords
	)
	{
		return new SitesCoords(elementType, coords);
	}

	//-------------------------------------------------------------------------
	
	/**
	 * For getting sites based on the ``from'' or ``to'' locations of all the moves
	 * in a collection of moves.
	 * 
	 * @param moveType	Type of sites to return.
	 * @param moves		The moves for which to collect the positions.
	 * 
	 * @example (sites From (forEach Piece))
	 * @example (sites To (forEach Piece))
	 * 
	 * @return Sites based on from-positions or to-positions of moves.
	 */
	public static RegionFunction construct
	(
		final SitesMoveType moveType,
		final Moves         moves
	)
	{
		switch (moveType)
		{
		case From:
			return new SitesFrom(moves);
		case To:
			return new SitesTo(moves);
		case Between:
			return new game.functions.region.sites.moves.SitesBetween(moves);
		default:
			break;
		}
		
		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Sites(): A SitesMoveType is not implemented.");
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * For creating a region from a list of site indices or an IntArrayFunction.
	 * 
	 * @param sites The list of the sites.
	 * @param array The IntArrayFunction.
	 *
	 * @example (sites {1..10})
	 * 
	 * @example (sites {1 5 10})
	 */
	public static RegionFunction construct
	(
		@Or final IntFunction[]    sites,
		@Or final IntArrayFunction array
	)
	{
		int numNonNull = 0;
		if (sites != null)
			numNonNull++;
		if (array != null)
			numNonNull++;

		if (numNonNull != 1)
			throw new IllegalArgumentException(
					"Sites(): For custom (sites ...) one of sites or array must be non-null.");
		
		if (sites != null)
			return new SitesCustom(sites);
		else
			return new SitesCustom(array);
	}

	//-------------------------------------------------------------------------
	
	/**
	 * For getting sites of a walk.
	 * 
	 * @param elementType   The graph element type [default SiteType of the board].
	 * @param index         The location from which to compute the walk [(from)].
	 * @param possibleSteps The different turtle steps defining a graphic turtle
	 *                      walk.
	 * @param rotations     True if the move includes all the rotations of the walk
	 *                      [True].
	 *
	 * @example (sites { {F F R F} {F F L F} })
	 */
	public static RegionFunction construct
	(
		@Opt       final SiteType        elementType,
		@Opt       final IntFunction     index,	
		 	       final StepType[][]    possibleSteps,
		@Opt @Name final BooleanFunction rotations
	)
	{
		return new SitesWalk(elementType, index, possibleSteps,rotations);
	}

	//-------------------------------------------------------------------------

	/**
	 * For getting sites belonging to a part of the board.
	 * 
	 * @param regionType  Type of sites to return.
	 * @param elementType The graph element type [default SiteType of the board].
	 * @param index       Index of the row, column or phase to return. This can also
	 *                    be the value of the local state for the State
	 *                    SitesIndexType or the container to search for the Empty
	 *                    SitesIndexType.
	 *
	 * @example (sites Row 1)
	 */
	public static RegionFunction construct
	(
		      final SitesIndexType regionType,
		@Opt  final SiteType       elementType,
		@Opt  final IntFunction    index	
	)
	{
		switch (regionType)
		{
		case Cell:
			return new SitesCell(elementType, index);
		case Column:
			return new SitesColumn(elementType, index);
		case Layer:
			return new SitesLayer(elementType, index);
		case Edge:
			return new SitesEdge(elementType, index);
		case Phase:
			return new SitesPhase(elementType, index);
		case Row:
			return new SitesRow(elementType, index);
		case State:
			return new SitesState(elementType, index);
		case Empty:
			return SitesEmpty.construct(elementType, index);
		case Support:
			return new SitesSupport(elementType, index);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Sites(): A SitesIndexType is not implemented.");
	}

	//-------------------------------------------------------------------------

	/**
	 * For getting sites of a side of the board.
	 * 
	 * @param regionType  Type of sites to return.
	 * @param elementType The graph element type [default SiteType of the board].
	 * @param player      Index of the player or the component.
	 * @param role        The Role type corresponding to the index.
	 * @param direction   Direction of the side to return.
	 *
	 * @example (sites Side NE)
	 */
	public static RegionFunction construct
	(
		         final SitesSideType    regionType, 
		@Opt     final SiteType         elementType,
		@Opt @Or final Player           player, 
		@Opt @Or final RoleType         role, 
		@Opt @Or final CompassDirection direction 
	)
	{
		int numNonNull = 0;
		if (player != null)
			numNonNull++;
		if (role != null)
			numNonNull++;
		if (direction != null)
			numNonNull++;

		if (numNonNull > 1)
			throw new IllegalArgumentException(
					"Sites(): A SitesSideType only one of index, role, direction can be non-null.");

		switch (regionType)
		{
		case Side:
			return new SitesSide(elementType, player, role, direction);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Sites(): A SitesPlayerType is not implemented.");
	}
	
	/**
	 * For getting sites at a specific distance of another.
	 * 
	 * @param regionType  Type of sites to return.
	 * @param elementType The graph element type [default site type of the board].
	 * @param relation    The relation type of the steps [Adjacent].
	 * @param stepMove    Define a particular step move to step.
	 * @param newRotation Define a new rotation at each step move in using the (value) iterator for the rotation.
	 * @param from        Index of the site.
	 * @param distance    Distance from the site.
	 *
	 * @example (sites Distance from:(last To) (exact 5))
	 * @example (sites Distance (step Forward (to if:(is Empty (to)))) newRotation:(+ (value) 1) from:(from)
                 (range 1 Infinity))
	 */
	public static RegionFunction construct
	(
		              final SitesDistanceType    regionType, 
		      @Opt    final SiteType             elementType,
		      @Opt    final RelationType         relation,
	          @Opt    final Step                 stepMove,
	 	@Name @Opt 	  final IntFunction          newRotation,
		@Name         final IntFunction          from, 
		              final RangeFunction        distance
	)
	{
		switch (regionType)
		{
		case Distance:
			return new SitesDistance(elementType, relation, stepMove, newRotation, from, distance);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Sites(): A SitesDistanceType is not implemented.");
	}

	//-------------------------------------------------------------------------

	/**
	 * For getting sites of a region defined in the equipment or of a single
	 * coordinate.
	 * 
	 * @param player   Index of the player or the component.
	 * @param role     The Role type corresponding to the index.
	 * @param siteType The graph element type of the coordinate is specified.
	 * @param name     The name of the region to return or of a single coordinate.
	 *
	 * @example (sites P1)
	 * @example (sites "E5")
	 */
	public static RegionFunction construct
	(
		@Opt @Or final Player     player, 
		@Opt @Or final RoleType   role, 
		@Opt     final SiteType   siteType, 
		@Opt     final String     name
	)
	{
		// If the name is a coordinate.
		if (StringRoutines.isCoordinate(name))
		{
			int numNonNull = 0;
			if (player != null)
				numNonNull++;
			if (role != null)
				numNonNull++;

			if (numNonNull != 0)
				throw new IllegalArgumentException(
						"Sites(): index and role has to be null to specify a region of a single coordinate.");
			
			return new SitesCoords(siteType, new String[]
			{ name });
		}

		int numNonNull = 0;
		if (player != null)
			numNonNull++;
		if (role != null)
			numNonNull++;

		if (numNonNull > 1)
			throw new IllegalArgumentException(
					"Sites(): only one of index, role can be non-null.");

		return new SitesEquipmentRegion(player, role, name);
	}
	
	//-------------------------------------------------------------------------

	/**
	 * For getting sites relative to a track.
	 * 
	 * @param regionType Type of sites to return.
	 * @param pid        Index of the player owned the track.
	 * @param role       The Role type corresponding to the index.
	 * @param name       The name of the track.
	 * @param from       Only the sites in the track from that site (included).
	 * @param to         Only the sites in the track until to reach that site
	 *                   (included).
	 *
	 * @example (sites Track)
	 */
	public static RegionFunction construct
	(
		              final SitesTrackType  regionType,
		@Opt @Or      final Player          pid, 
		@Opt @Or      final RoleType        role, 
		@Opt          final String          name,
		@Opt 	@Name final IntFunction     from,
		@Opt 	@Name final IntFunction     to
	)
	{
		int numNonNull = 0;
		if (pid != null)
			numNonNull++;
		if (role != null)
			numNonNull++;

		if (numNonNull > 1)
			throw new IllegalArgumentException(
					"Sites(): A SitesTrackType only one of pid, role can be non-null.");

		switch (regionType)
		{
		case Track:
			return new SitesTrack(pid, role, name,from,to);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Sites(): A SitesTrackType is not implemented.");
	}
	
	//-------------------------------------------------------------------------

	/**
	 * For getting sites relative to a player.
	 * 
	 * @param regionType  Type of sites to return.
	 * @param elementType The graph element type [default SiteType].
	 * @param pid         Index of the player or the component.
	 * @param role        The Role type corresponding to the index.
	 * @param moves       Rules used to generate moves for finding winning sites.
	 * @param name        The name of the board or region to return.
	 *
	 * @example (sites Hand Mover)
	 * @example (sites Winning Next (add (to (sites Empty))))
	 */
	public static RegionFunction construct
	(
		         final SitesPlayerType regionType,
		@Opt     final SiteType        elementType,
		@Opt @Or final Player          pid, 
		@Opt @Or final RoleType        role, 
		@Opt	 final NonDecision	   moves,
		@Opt     final String          name
	)
	{
		int numNonNull = 0;
		if (pid != null)
			numNonNull++;
		if (role != null)
			numNonNull++;

		if (numNonNull > 1)
			throw new IllegalArgumentException(
					"Sites(): A SitesPlayerType only one of pid, role can be non-null.");

		switch (regionType)
		{
		case Hand:
			return new SitesHand(pid, role);
		case Winning:
			return new SitesWinning(pid, role, moves);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Sites(): A SitesPlayerType is not implemented.");
	}
	
	//-------------------------------------------------------------------------

	/**
	 * For getting sites relative to a player.
	 * 
	 * @param regionType  Type of sites to return.
	 * @param elementType The graph element type [default SiteType of the board].
	 * @param pid         Index of the player or the component.
	 * @param role        The Role type corresponding to the index.
	 * @param moves       Rules used to generate moves for finding winning sites.
	 * @param name        The name of the board or region to return.
	 *
	 * @example (sites Start (piece (what at:(from))))
	 */
	public static RegionFunction construct
	(
	   final SitesPieceType  regionType,
	   final Piece           pid 
	)
	{
		switch (regionType)
		{
		case Start:
			return new SitesStart(pid);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Sites(): A SitesPlayerType is not implemented.");
	}
	
	/**
	 * For getting sites occupied by player(s).
	 * 
	 * @param regionType Type of sites to return.
	 * @param by         The index of the owner.
	 * @param By         The roleType of the owner.
	 * @param container  The index of the container.
	 * @param Container  The name of the container.
	 * @param component  The index of the component.
	 * @param Component  The name of the component.
	 * @param components The names of the component.
	 * @param top        True to look only the top of the stack [True].
	 * @param on         The type of the graph element.
	 *
	 * @example (sites Occupied by:Mover)
	 */
	public static RegionFunction construct
	(
		                final SitesOccupiedType regionType,
		     @Or  @Name final Player            by,
		     @Or  @Name final RoleType          By,
		@Opt @Or2 @Name	final IntFunction       container, 
		@Opt @Or2 @Name final String            Container,
		@Opt @Or  @Name final IntFunction       component,
		@Opt @Or  @Name final String            Component,
		@Opt @Or  @Name final String[]          components,
		@Opt      @Name final Boolean           top,
		@Opt 	  @Name final SiteType          on 
	)
	{
		int numNonNull = 0;
		if (by != null)
			numNonNull++;
		if (By != null)
			numNonNull++;
		
		if (numNonNull != 1)
			throw new IllegalArgumentException(
					"Sites(): With SitesOccupiedType exactly one who or role parameter must be non-null.");
		
		int numNonNull2 = 0;
		if (container != null)
			numNonNull2++;
		if (Container != null)
			numNonNull2++;

		if (numNonNull2 > 1)
			throw new IllegalArgumentException(
					"Sites(): With SitesOccupiedType zero or one container or Container parameter must be non-null.");

		int numNonNull3 = 0;
		if (Component != null)
			numNonNull3++;
		if (component != null)
			numNonNull3++;
		if (components != null)
			numNonNull3++;

		if (numNonNull3 > 1)
			throw new IllegalArgumentException(
					"Sites(): With SitesOccupiedType zero or one Component or component or components parameter must be non-null.");

		switch (regionType)
		{
		case Occupied:
			return new SitesOccupied(by, By, container, Container, component, Component, components, top, on);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Sites(): A SitesOccupiedType is not implemented.");
	}

	//-------------------------------------------------------------------------

	/**
	 * For getting sites incident to another.
	 * 
	 * @param regionType Type of sites to return.
	 * @param resultType The graph type of the result.
	 * @param of         The graph type of the index.
	 * @param at         Index of the element to check.
	 * @param owner      The owner of the site to return.
	 * @param roleOwner  The role of the owner of the site to return.
	 *
	 * @example (sites Incident Edge of:Vertex at:(last To))
	 * 
	 * @example (sites Incident Cell of:Edge at:(last To) Mover)
	 */
	public static RegionFunction construct
	(
		               final SitesIncidentType regionType,
					   final SiteType          resultType,
				 @Name final SiteType          of,
			     @Name final IntFunction       at,
		@Opt @Or @Name final Player            owner,
		@Opt @Or       final RoleType          roleOwner
	)
	{
		int numNonNull = 0;
		if (owner != null)
			numNonNull++;
		if (roleOwner != null)
			numNonNull++;

		if (numNonNull > 1)
			throw new IllegalArgumentException(
					"Sites(): With SitesIncidentType Zero or one owner or roleOwner parameter can be non-null.");

		switch (regionType)
		{
		case Incident:
			return new SitesIncident(resultType, of, at, owner, roleOwner);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Sites(): A SitesIncidentType is not implemented.");
	}
	
	//-------------------------------------------------------------------------

	/**
	 * For getting sites around another.
	 * 
	 * @param regionType  Type of sites to return.
	 * @param typeLoc     The graph element type [default SiteType of the board].
	 * @param where       The location to check.
	 * @param regionWhere The region to check.
	 * @param type        The type of the dynamic region.
	 * @param distance    The distance around which to check [1].
	 * @param directions  The directions to use [Adjacent].
	 * @param If          The condition to satisfy around the site to be included in
	 *                    the result.
	 * @param includeSelf True if the origin site/region is included in the result
	 *                    [False].
	 * 
	 * @example (sites Around (last To))
	 * @example (sites Around (to) Orthogonal if:(is Empty (to)))
	 */
	public static RegionFunction construct
	(
		               final SitesAroundType   regionType,
		@Opt           final SiteType          typeLoc,
		     @Or       final IntFunction       where,
			 @Or  	   final RegionFunction    regionWhere,
		@Opt 	       final RegionTypeDynamic type,
		@Opt     @Name final IntFunction       distance,
	    @Opt 	       final AbsoluteDirection directions,
		@Opt     @Name final BooleanFunction   If,
		@Opt     @Name final BooleanFunction   includeSelf
	)
	{
		int numNonNull = 0;
		if (where != null)
			numNonNull++;
		if (regionWhere != null)
			numNonNull++;

		if (numNonNull != 1)
			throw new IllegalArgumentException(
					"Sites(): With SitesAroundType one where or regionWhere parameter must be non-null.");

		switch (regionType)
		{
		case Around:
			return new SitesAround(typeLoc, where, regionWhere, type, distance, directions, If, includeSelf);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Sites(): A SitesAroundType is not implemented.");
	}
	
	//-------------------------------------------------------------------------

	/**
	 * For getting sites in a direction from another.
	 * 
	 * @param regionType   Type of sites to return.
	 * @param from         The origin location.
	 * @param From         The origin region location.
	 * @param absoluteDirn The directions of the move [Adjacent].
	 * @param relativeDirn The directions of the move.
	 * @param directions   The directions of the move [Adjacent].
	 * @param included     True if the origin is included in the result [False].
	 * @param stop         When the condition is true in one specific direction,
	 *                     sites are no longer added to the result [False].
	 * @param stopIncluded True if the site stopping the radial in each direction is
	 *                     included in the result [False].
	 * @param distance     The distance around which to check [Infinite].
	 * @param type         The graph element type [default SiteType of the board].
	 * 
	 * @example (sites Direction from:(last To) Diagonal)
	 */
	public static RegionFunction construct
	(
		            final SitesDirectionType             regionType,
		@Or  @Name  final IntFunction                    from,
		@Or  @Name  final RegionFunction                 From,
		@Opt        final game.util.directions.Direction directions,
		@Opt @Name  final BooleanFunction                included,
		@Opt @Name  final BooleanFunction                stop, 
		@Opt @Name  final BooleanFunction                stopIncluded, 
		@Opt @Name  final IntFunction                    distance,
		@Opt 	    final SiteType                       type
	)
	{
		int numNonNull = 0;
		if (from != null)
			numNonNull++;
		if (From != null)
			numNonNull++;

		if (numNonNull != 1)
			throw new IllegalArgumentException(
					"Sites(): With SitesDirectionType one from or From parameter must be non-null.");

		switch (regionType)
		{
		case Direction:
			return new SitesDirection(from, From, directions, included, stop, stopIncluded, distance,
					type);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Sites(): A SitesDirectionType is not implemented.");
	}
	
	//-------------------------------------------------------------------------

	/**
	 * For getting sites in the line of sight.
	 * 
	 * @param regionType Type of sites to return.
	 * @param typeLoS    The line-of-sight test to apply [Piece].
	 * @param typeLoc    The graph element type [default SiteType of the board].
	 * @param at         The location [(last To)].
	 * @param directions The directions of the move [Adjacent].
	 * 
	 * @example (sites LineOfSight Orthogonal)
	 */
	public static RegionFunction construct
	(
		           final SitesLineOfSightType           regionType,
   		@Opt	   final LineOfSightType                typeLoS,
		@Opt       final SiteType                       typeLoc, 
		@Opt @Name final IntFunction                    at,
		@Opt       final game.util.directions.Direction directions
	)
	{
		switch (regionType)
		{
		case LineOfSight:
			return new SitesLineOfSight(typeLoS, typeLoc, at, directions);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Sites(): A SitesAroundType is not implemented.");
	}
	
	//-------------------------------------------------------------------------
	
	private Sites()
	{
		// Ensure that compiler does pick up default constructor
	}
	
	//-------------------------------------------------------------------------

	@Override
	public Region eval(final Context context)
	{
		return null;
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
