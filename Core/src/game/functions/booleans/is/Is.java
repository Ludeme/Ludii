package game.functions.booleans.is;

import annotations.Name;
import annotations.Opt;
import annotations.Or;
import annotations.Or2;
import game.Game;
import game.functions.booleans.BaseBooleanFunction;
import game.functions.booleans.BooleanFunction;
import game.functions.booleans.is.Hidden.IsHidden;
import game.functions.booleans.is.Hidden.IsHiddenCount;
import game.functions.booleans.is.Hidden.IsHiddenRotation;
import game.functions.booleans.is.Hidden.IsHiddenState;
import game.functions.booleans.is.Hidden.IsHiddenValue;
import game.functions.booleans.is.Hidden.IsHiddenWhat;
import game.functions.booleans.is.Hidden.IsHiddenWho;
import game.functions.booleans.is.angle.IsAcute;
import game.functions.booleans.is.angle.IsObtuse;
import game.functions.booleans.is.angle.IsReflex;
import game.functions.booleans.is.angle.IsRight;
import game.functions.booleans.is.component.IsThreatened;
import game.functions.booleans.is.component.IsWithin;
import game.functions.booleans.is.component.IsFreedom;
import game.functions.booleans.is.connect.IsBlocked;
import game.functions.booleans.is.connect.IsConnected;
import game.functions.booleans.is.edge.IsCrossing;
import game.functions.booleans.is.graph.IsLastFrom;
import game.functions.booleans.is.graph.IsLastTo;
import game.functions.booleans.is.in.IsIn;
import game.functions.booleans.is.integer.IsAnyDie;
import game.functions.booleans.is.integer.IsEven;
import game.functions.booleans.is.integer.IsFlat;
import game.functions.booleans.is.integer.IsOdd;
import game.functions.booleans.is.integer.IsPipsMatch;
import game.functions.booleans.is.integer.IsSidesMatch;
import game.functions.booleans.is.integer.IsVisited;
import game.functions.booleans.is.line.IsLine;
import game.functions.booleans.is.loop.IsLoop;
import game.functions.booleans.is.path.IsPath;
import game.functions.booleans.is.pattern.IsPattern;
import game.functions.booleans.is.pyramidCorners.IsPyramidCorners;
import game.functions.booleans.is.player.IsActive;
import game.functions.booleans.is.player.IsEnemy;
import game.functions.booleans.is.player.IsFriend;
import game.functions.booleans.is.player.IsMover;
import game.functions.booleans.is.player.IsNext;
import game.functions.booleans.is.player.IsPrev;
import game.functions.booleans.is.regularGraph.IsRegularGraph;
import game.functions.booleans.is.related.IsRelated;
import game.functions.booleans.is.repeat.IsRepeat;
import game.functions.booleans.is.simple.IsCycle;
import game.functions.booleans.is.simple.IsFull;
import game.functions.booleans.is.simple.IsPending;
import game.functions.booleans.is.site.IsEmpty;
import game.functions.booleans.is.site.IsOccupied;
import game.functions.booleans.is.string.IsDecided;
import game.functions.booleans.is.string.IsProposed;
import game.functions.booleans.is.target.IsTarget;
import game.functions.booleans.is.tree.IsCaterpillarTree;
import game.functions.booleans.is.tree.IsSpanningTree;
import game.functions.booleans.is.tree.IsTree;
import game.functions.booleans.is.tree.IsTreeCentre;
import game.functions.booleans.is.triggered.IsTriggered;
import game.functions.intArray.IntArrayFunction;
import game.functions.ints.IntFunction;
import game.functions.range.RangeFunction;
import game.functions.region.RegionFunction;
import game.rules.play.moves.Moves;
import game.types.board.HiddenData;
import game.types.board.RegionTypeStatic;
import game.types.board.RelationType;
import game.types.board.SiteType;
import game.types.board.StepType;
import game.types.play.RepetitionType;
import game.types.play.RoleType;
import game.util.directions.AbsoluteDirection;
import game.util.directions.Direction;
import game.util.moves.Player;
import other.IntArrayFromRegion;
import other.context.Context;

/**
 * Returns whether the specified query about the game state is true or not.
 * 
 * @author Eric.Piette and cambolbro 
 */
@SuppressWarnings("javadoc")
public class Is extends BaseBooleanFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------
	
	/**
	 * For checking angles between sites.
	 * 
	 * @param isType         The type of query to perform.
	 * @param type           The graph element type [default of the board].
	 * @param at             The site to look the angle.
	 * @param conditionSite  The condition on the left site.
	 * @param conditionSite2 The condition on the right site.
	 *
	 * @example (is Acute at:(last To) (is Enemy (who at:(site))) (is Enemy (who at:(site))))
	 */
	public static BooleanFunction construct
	(
			   final IsAngleType      isType, 
	    @Opt   final SiteType         type,
		@Name  final IntFunction      at, 
		       final BooleanFunction  conditionSite, 
		       final BooleanFunction  conditionSite2
	)
	{
		switch (isType)
		{
		case Acute:
			return new IsAcute(type, at, conditionSite, conditionSite2);
		case Obtuse:
			return new IsObtuse(type, at, conditionSite, conditionSite2);
		case Reflex:
			return new IsReflex(type, at, conditionSite, conditionSite2);
		case Right:
			return new IsRight(type, at, conditionSite, conditionSite2);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Is(): An IsAngleType is not implemented.");
	}
	
	//-------------------------------------------------------------------------

	/**
	 * For checking hidden information at a location for a specific player.
	 * 
	 * @param isType   The type of query to perform.
	 * @param dataType The type of hidden data [Invisible].
	 * @param type     The graph element type [default of the board].
	 * @param at       The site to set the hidden information.
	 * @param level    The level to set the hidden information [0].
	 * @param to       The player with these hidden information.
	 * @param To       The roleType with these hidden information.
	 *
	 * @example (is Hidden at:(to) to:Mover)
	 */
	public static BooleanFunction construct
	(
				     final IsHiddenType isType, 
		      @Opt   final HiddenData   dataType,
			  @Opt   final SiteType     type,
		@Name        final IntFunction  at, 
		@Name @Opt   final IntFunction  level,
		@Name @Or    final Player       to, 
		@Name @Or    final RoleType     To
	)
	{
		int numNonNull = 0;
		if (to != null)
			numNonNull++;
		if (To != null)
			numNonNull++;

		if (numNonNull != 1)
			throw new IllegalArgumentException("Set(): With IsHiddenType one to or To parameter must be non-null.");
		
		switch (isType)
		{
		case Hidden:
			if (dataType == null)
				return new IsHidden(type, at, level, to, To);
			else
			{
				switch (dataType)
				{
				case What:
					return new IsHiddenWhat(type, at, level, to, To);
				case Who:
					return new IsHiddenWho(type, at, level, to, To);
				case Count:
					return new IsHiddenCount(type, at, level, to, To);
				case State:
					return new IsHiddenState(type, at, level, to, To);
				case Rotation:
					return new IsHiddenRotation(type, at, level, to, To);
				case Value:
					return new IsHiddenValue(type, at, level, to, To);
				}
			}
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Is(): An IsHiddenType is not implemented.");
	}
	
	//-------------------------------------------------------------------------

	/**
	 * For detecting a specific pattern from a site.
	 * 
	 * @param isType         The type of query to perform.
	 * @param repetitionType The type of repetition [Positional].
	 *
	 * @example (is Repeat Positional)
	 */
	public static BooleanFunction construct
	(
			  final IsRepeatType   isType,
       @Opt   final RepetitionType repetitionType
	)
	{
		switch (isType)
		{
		case Repeat:
			return new IsRepeat(repetitionType);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Is(): An IsRepeatType is not implemented.");
	}
	
	//-------------------------------------------------------------------------

	/**
	 * For detecting a specific pattern from a site.
	 * 
	 * @param isType The type of query to perform.
	 * @param walk   The walk describing the pattern.
	 * @param type   The type of the site from to detect the pattern.
	 * @param from   The site from to detect the pattern [(last To)].
	 * @param froms  The sites from to detect the pattern [(last To)].
	 * @param what   The piece to check in the pattern [piece in from].
	 * @param whats  The sequence of pieces to check in the pattern [piece in from].
	 *
	 * @example (is Pattern {F R F R F})
	 */
	public static BooleanFunction construct
	(
		               	final IsPatternType isType,
		     @Opt       final StepType[]    walk,
             @Opt       final SiteType      type,
        @Or2 @Opt @Name final IntFunction   from,
	    @Or  @Opt @Name final IntFunction   what,
	    @Or  @Opt @Name final IntFunction[] whats,
	    @Or2 @Opt @Name final RegionFunction froms
	)
	{
		int numNonNull = 0;
		if (what != null)
			numNonNull++;
		if (whats != null)
			numNonNull++;

		if (numNonNull > 1)
			throw new IllegalArgumentException("Only one parameter between what and whats can be non-null.");

		switch (isType)
		{
		case Pattern:
			return new IsPattern(walk, type, from, what, whats);
		case PyramidCorners:
			return new IsPyramidCorners(type, from, froms);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Is(): An IsPatternType is not implemented.");
	}
	
	//-------------------------------------------------------------------------

	/**
	 * For testing a tree.
	 * 
	 * @param isType The type of query to perform.
	 * @param who    Data about the owner of the tree.
	 * @param role   RoleType of the owner of the tree.
	 *
	 * @example (is Tree Mover)
	 * @example (is SpanningTree Mover)
	 * @example (is CaterpillarTree Mover)
	 * @example (is TreeCentre Mover)
	 */
	public static BooleanFunction construct
	(
		     final IsTreeType isType,
		@Or  final Player     who,
		@Or  final RoleType   role
	)
	{
		switch (isType)
		{
		case Tree:
			return new IsTree(who, role);
		case SpanningTree:
			return new IsSpanningTree(who, role);
		case CaterpillarTree:
			return new IsCaterpillarTree(who, role);
		case TreeCentre:
			return new IsTreeCentre(who, role);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Is(): A IsTreeType is not implemented.");
	}
	
	//-------------------------------------------------------------------------

	/**
	 * For testing if a graph is regular.
	 * 
	 * @param isType The type of query to perform.
	 * @param who    The owner of the tree.
	 * @param role   RoleType of the owner of the tree.
	 * @param k      The parameter of k-regular graph.
	 * @param odd    Flag to recognise the k (in k-regular graph) is odd or not.
	 * @param even   Flag to recognise the k (in k-regular graph) is even or not.
	 *
	 * @example (is RegularGraph Mover)
	 */
	public static BooleanFunction construct
	(
						final IsRegularGraphType isType,
			@Or         final Player             who,
			@Or         final RoleType           role,
	   @Opt @Or2  @Name final IntFunction        k,
       @Opt @Or2  @Name final BooleanFunction    odd,
       @Opt @Or2  @Name final BooleanFunction    even
	)
	{
		
		int numNonNull1 = 0;
		if (who != null)
			numNonNull1++;
		if (role != null)
			numNonNull1++;
		
		if (numNonNull1 != 1)
			throw new IllegalArgumentException(
					"Is(): with IsRegularGraphType one of who or role has to be non-null.");
		
		numNonNull1 = 0;
		if (k != null)
			numNonNull1++;
		if (odd != null)
			numNonNull1++;
		if (even != null)
			numNonNull1++;
		
		if (numNonNull1 > 1)
			throw new IllegalArgumentException(
					"Is(): with IsRegularGraphType only one of k, odd, even has to be non-null.");

		switch (isType)
		{
		case RegularGraph:
			return new IsRegularGraph(who, role, k, odd, even);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Is(): A IsRegularGraphType is not implemented.");
	}
	
	//-------------------------------------------------------------------------

	/**
	 * For test relative to player.
	 * 
	 * @param isType The type of query to perform.
	 * @param index  Index of the player or the component.
	 * @param role   The Role type corresponding to the index.
	 *
	 * @example (is Enemy (who at:(last To)))
	 * @example (is Prev Mover)
	 */
	public static BooleanFunction construct
	(
			 final IsPlayerType isType,
		 @Or final IntFunction  index, 
		 @Or final RoleType     role 
	)
	{
		int numNonNull = 0;
		if (index != null)
			numNonNull++;
		if (role != null)
			numNonNull++;

		if (numNonNull != 1)
			throw new IllegalArgumentException(
					"Is(): with IsPlayerType only one of index, role has to be non-null.");

		switch (isType)
		{
		case Enemy:
			return new IsEnemy(index, role);
		case Friend:
			return new IsFriend(index, role);
		case Mover:
			return new IsMover(index, role);
		case Next:
			return new IsNext(index, role);
		case Prev:
			return new IsPrev(index, role);
		case Active:
			return new IsActive(index, role);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Is(): A IsPlayerType is not implemented.");
	}
	
	//-------------------------------------------------------------------------

	/**
	 * For a triggered test.
	 * 
	 * @param isType The type of query to perform.
	 * @param event  The event triggered.
	 * @param index  Index of the player or the component.
	 * @param role   The Role type corresponding to the index.
	 *
	 * @example (is Triggered "Lost" Next)
	 */
	public static BooleanFunction construct
	(
			 final IsTriggeredType isType,
			 final String          event,
		 @Or final IntFunction     index, 
		 @Or final RoleType        role 
	)
	{
		int numNonNull = 0;
		if (index != null)
			numNonNull++;
		if (role != null)
			numNonNull++;

		if (numNonNull != 1)
			throw new IllegalArgumentException(
					"Is(): with IsTriggeredType only one of index, role has to be non-null.");

		switch (isType)
		{
		case Triggered:
			return new IsTriggered(event,index, role);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Is(): A IsTriggeredType is not implemented.");
	}
	
	//-------------------------------------------------------------------------

	/**
	 * For a test with no parameter.
	 * 
	 * @param isType The type of query to perform.
	 * 
	 * @example (is Cycle)
	 * @example (is Full)
	 */
	public static BooleanFunction construct
	(
		final IsSimpleType isType
	)
	{
		switch (isType)
		{
		case Cycle:
			return new IsCycle();
		case Pending:
			return new IsPending();
		case Full:
			return new IsFull();
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Is(): A IsSimpleType is not implemented.");
	}
	
	//-------------------------------------------------------------------------

	/**
	 * For testing two edges crossing each other.
	 * 
	 * @param isType The type of query to perform.
	 * @param edge1  The index of the first edge.
	 * @param edge2  The index of the second edge.
	 * 
	 * @example (is Crossing (last To) (to))
	 */
	public static BooleanFunction construct
	(
		final IsEdgeType  isType,
		final IntFunction edge1,
		final IntFunction edge2
	)
	{
		switch (isType)
		{
		case Crossing:
			return new IsCrossing(edge1,edge2);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Is(): A IsEdgeType is not implemented.");
	}


	//-------------------------------------------------------------------------

	/**
	 * For test relative to a string.
	 * 
	 * @param isType The type of query to perform.
	 * @param string The string to check.
	 * 
	 * @example (is Decided "End")
	 * @example (is Proposed "End")
	 */
	public static BooleanFunction construct
	(
		final IsStringType isType,
		final String       string
	)
	{
		switch (isType)
		{
		case Decided:
			return new IsDecided(string);
		case Proposed:
			return new IsProposed(string);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Is(): A IsStringType is not implemented.");
	}
	
	//-------------------------------------------------------------------------

	/**
	 * For test relative to a graph element type.
	 * 
	 * @param isType The type of query to perform.
	 * @param type   The graph element type [default SiteType of the board].
	 * 
	 * @example (is LastFrom Vertex)
	 */
	public static BooleanFunction construct
	(
	  final IsGraphType isType,
	  final SiteType    type
	)
	{
		switch (isType)
		{
		case LastFrom:
			return new IsLastFrom(type);
		case LastTo:
			return new IsLastTo(type);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Is(): A IsGraphType is not implemented.");
	}
	
	//-------------------------------------------------------------------------

	/**
	 * For test about a single integer.
	 * 
	 * @param isType The type of query to perform.
	 * @param value  The value.
	 * 
	 * @example (is Even (last To))
	 * @example (is Visited (last To))
	 */
	public static BooleanFunction construct
	(
		     final IsIntegerType isType,
		@Opt final IntFunction   value
	)
	{
		switch (isType)
		{
		case Even:
			return new IsEven(value);
		case Odd:
			return new IsOdd(value);
		case Flat:
			return new IsFlat(value);
		case PipsMatch:
			return new IsPipsMatch(value);
		case SidesMatch:
			return new IsSidesMatch(value);
		case Visited:
			return new IsVisited(value);
		case AnyDie:
			return new IsAnyDie(value);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Is(): A IsValueType is not implemented.");
	}
	
	//-------------------------------------------------------------------------

	/**
	 * For tests relative to a component.
	 * 
	 * @param isType        The type of query to perform.
	 * @param what          The piece possibly under threat.
	 * @param type          The graph element type [default SiteType of the board].
	 * @param at            The location of the piece to check.
	 * @param in            The locations of the piece to check.
	 * @param specificMoves The specific moves used to threat.
	 *
	 * 
	 * @example (is Threatened (id "King" Mover) at:(to))
	 */
	public static BooleanFunction construct
	(
		               final IsComponentType isType,
		@Opt	       final IntFunction     what,
		@Opt           final SiteType        type,
		@Opt @Or @Name final IntFunction     at,
		@Opt @Or @Name final RegionFunction  in,
		@Opt           final Moves           specificMoves
	)
	{
		int numNonNull = 0;
		if (at != null)
			numNonNull++;
		if (in != null)
			numNonNull++;

		if (numNonNull > 1)
			throw new IllegalArgumentException(
					"Is(): With IsComponentType only one 'site' or 'sites' parameter must be non-null.");
		switch (isType)
		{
		case Threatened:
			return new IsThreatened(what, type, at, in, specificMoves);
		case Within:
			return new IsWithin(what, type, at, in);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Is(): A IsComponentType is not implemented.");
	}

	//-------------------------------------------------------------------------

	/**
	 * For testing the relation between two sites.
	 * 
	 * @param isType       The type of query to perform.
	 * @param relationType The type of relation to check between the graph elements.
	 * @param type         The graph element type [default SiteType of the board].
	 * @param siteA        The first site.
	 * @param siteB        The second site.
	 * @param region       The region of the second site.
	 * 
	 * @example (is Related Adjacent (from) (sites Occupied by:Next))
	 */
	public static BooleanFunction construct
	(
			 final IsRelationType isType,
		     final RelationType   relationType,
		@Opt final SiteType       type,
			 final IntFunction    siteA,
		@Or  final IntFunction    siteB,
		@Or  final RegionFunction region
	)
	{
		int numNonNull = 0;
		if (siteB != null)
			numNonNull++;
		if (region != null)
			numNonNull++;

		if (numNonNull != 1)
			throw new IllegalArgumentException(
					"Is(): With IsRelationType only one siteB or region parameter can be non-null.");

		switch (isType)
		{
		case Related:
			return new IsRelated(relationType, type, siteA, new IntArrayFromRegion(siteB, region));
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Is(): A IsRelationType is not implemented.");
	}
	
	//-------------------------------------------------------------------------

	/**
	 * For testing a region.
	 * 
	 * @param isType        The type of query to perform.
	 * @param containerIdFn The index of the container [0].
	 * @param containerName The name of the container ["Board"].
	 * @param configuration The configuration defined by the indices of each piece.
	 * @param specificSite  The specific site of the configuration.
	 * @param specificSites The specific sites of the configuration.
	 * 
	 * @example (is Target {2 2 2 2 0 0 1 1 1 1})
	 */
	public static BooleanFunction construct
	(
			     final IsTargetType isType,
		@Opt @Or final IntFunction  containerIdFn,
		@Opt @Or final String       containerName,
				 final Integer[]    configuration,
		@Opt @Or final Integer      specificSite,
		@Opt @Or final Integer[]    specificSites
	)
	{
		int numNonNull = 0;
		if (containerIdFn != null)
			numNonNull++;
		if (containerName != null)
			numNonNull++;

		if (numNonNull > 1)
			throw new IllegalArgumentException(
					"Is(): with IsTargetType zero or one containerId or containerName parameter must be non-null.");

		int numNonNullA = 0;
		if (specificSite != null)
			numNonNullA++;
		if (specificSites != null)
			numNonNullA++;

		if (numNonNullA > 1)
			throw new IllegalArgumentException(
					"Is(): with IsTargetType zero or one specificSite or specificSites parameter must be non-null.");

		switch (isType)
		{
		case Target:
			return new IsTarget(containerIdFn, containerName, configuration, specificSite, specificSites);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Is(): A IsTargetType is not implemented.");
	}
	
	//-------------------------------------------------------------------------

	/**
	 * For test relative to a connection.
	 * 
	 * @param isType     The type of query to perform.
	 * @param number     The minimum number of regions to connect [All of them].
	 * @param type       The graph element type [default SiteType of the board].
	 * @param at         The specific starting position need to connect.
	 * @param directions The directions of the connected pieces used to connect the
	 *                   region [Adjacent].
	 * @param regions    The disjointed regions set, which need to use for
	 *                   connection.
	 * @param role       The role of the player.
	 * @param regionType Type of the regions to connect.
	 * 
	 * @example (is Blocked Mover)
	 * @example (is Connected Mover)
	 * @example (is Connected { (sites Side S) (sites Side NW) (sites Side NE)})
	 */
	public static BooleanFunction construct
	(
			       final IsConnectType    isType,
		@Opt       final IntFunction      number,
		@Opt       final SiteType         type,
		@Opt @Name final IntFunction      at,
		@Opt       final Direction        directions,
		@Or	       final RegionFunction[] regions,
		@Or        final RoleType         role,
		@Or        final RegionTypeStatic regionType	
	)
	{
		int numNonNull = 0;
		if (regions != null)
			numNonNull++;
		if (role != null)
			numNonNull++;
		if (regionType != null)
			numNonNull++;
		if (numNonNull != 1)
			throw new IllegalArgumentException(
					"Is(): with IsConnectType Exactly one regions, role or regionType parameter must be non-null.");

		switch (isType)
		{
		case Blocked:
			return new IsBlocked(type, number, directions, regions, role, regionType);
		case Connected:
			return new IsConnected(number, type, at, directions, regions, role, regionType);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Is(): A IsConnectType is not implemented.");
	}
	
	//-------------------------------------------------------------------------

	/**
	 * For test relative to a line.
	 * 
	 * @param isType     		The type of query to perform.
	 * @param type       		The graph element type [default SiteType of the board].
	 * @param length     		Minimum length of lines.
	 * @param dirn      		Direction category to which potential lines must belong
	 *                   		[Adjacent].
	 * @param through    		Location through which the line must pass.
	 * @param throughAny 		The line must pass through at least one of these sites.
	 * @param who        		The owner of the pieces making a line [Mover].
	 * @param what       		The index of the component composing the line [(mover)].
	 * @param whats      		The indices of the components composing the line.
	 * @param exact      		If true, then lines cannot exceed minimum length [False].
	 * @param contiguous 		If true, the line has to be contiguous [True].
	 * @param If         		The condition on each site on the line [True].
	 * @param byLevel    		If true, then lines are detected in using the level in a
	 *                   		stack [False].
	 * @param top        		If true, then lines are detected in using only the top level 
	 *                   		in a stack [False].
	 * @param throughHowMuch    The number of minimum component types to compose the line
	 * @param isVisible			If a line has to be visible from above (and not cut by other components) (used in 3D games)
	 * @param useOpposites		Whether to use the opposite radial to try and find line 
	 *                          (i.e whether to iterate in both opposite directions of analyzed component to find line)
	 * 
	 * @example (is Line 3)
	 * @example (is Line 5 Orthogonal if:(not (is In (to) (sites Mover))))
	 */
	public static BooleanFunction construct
	(
			             final IsLineType        isType,
			 @Opt        final SiteType          type,
       	                 final IntFunction       length,
             @Opt 	     final AbsoluteDirection dirn,
        @Or2 @Opt @Name  final IntFunction       through,
        @Or2 @Opt @Name  final RegionFunction    throughAny,
        @Or  @Opt        final RoleType          who,
        @Or  @Opt @Name  final IntFunction       what,
        @Or  @Opt @Name  final IntFunction[]     whats,
        @Opt      @Name  final BooleanFunction   exact,
		@Opt 	  @Name  final BooleanFunction   contiguous,
             @Opt @Name  final BooleanFunction   If,
 			 @Opt @Name  final BooleanFunction   byLevel,
 			 @Opt @Name  final BooleanFunction   top,
 			 @Opt @Name  final IntFunction   	 throughHowMuch,
			 @Opt @Name  final BooleanFunction   isVisible,
			 @Opt @Name  final BooleanFunction   useOpposites
	)
	{
		int numNonNull = 0;
		if (what != null)
			numNonNull++;
		if (whats != null)
			numNonNull++;
		if (who != null)
			numNonNull++;

		if (numNonNull > 1)
			throw new IllegalArgumentException(
					"Is(): With IsLineType zero or one what, whats or who parameter can be non-null.");

		int numNonNull2 = 0;
		if (through != null)
			numNonNull2++;
		if (throughAny != null)
			numNonNull2++;

		if (numNonNull2 > 1)
			throw new IllegalArgumentException(
					"Is(): With IsLineType zero or one through or throughAny parameter can be non-null.");

		switch (isType)
		{
		case Line:
			return new IsLine(type, length, dirn, through, throughAny, who, what, whats, exact, contiguous, If, byLevel, top, throughHowMuch, isVisible, useOpposites);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Is(): A IsLineType is not implemented.");
	}

	//-------------------------------------------------------------------------

	/**
	 * For test relative to a loop.
	 * 
	 * @param isType       The type of query to perform.
	 * @param type         The graph element type [default SiteType of the board].
	 * @param surround     Used to define the inside condition of the loop.
	 * @param surroundList The list of items inside the loop.
	 * @param directions   The direction of the connection [Adjacent].
	 * @param colour       The owner of the looping pieces [Mover].
	 * @param start        The starting point of the loop [(last To)].
	 * @param regionStart  The region to start to detect the loop.
	 * @param path         Whether to detect loops in the paths of pieces (e.g.
	 *                     Trax).
	 * 
	 * @example (is Loop)
	 * @example (is Loop (mover) path:True)
	 */
	public static BooleanFunction construct
	(
			            final IsLoopType     isType,
			 @Opt       final SiteType       type,
		@Or	 @Opt @Name final RoleType       surround,
		@Or  @Opt	    final RoleType[]     surroundList,
			 @Opt       final Direction      directions,
			 @Opt       final IntFunction    colour,
		@Or2 @Opt       final IntFunction    start,
		@Or2 @Opt       final RegionFunction regionStart,
	         @Opt @Name final Boolean        path
	)
	{
		int numNonNull = 0;
		if (surround != null)
			numNonNull++;
		if (surroundList != null)
			numNonNull++;

		if (numNonNull > 1)
			throw new IllegalArgumentException(
					"Is(): With IsLoopType zero or one surround or surroundList parameter can be non-null.");

		int numNonNull2 = 0;
		if (start != null)
			numNonNull2++;
		if (regionStart != null)
			numNonNull2++;

		if (numNonNull2 > 1)
			throw new IllegalArgumentException(
					"Is(): With IsLoopType zero or one start or regionStart parameter can be non-null.");

		switch (isType)
		{
		case Loop:
			return new IsLoop(type, surround, surroundList, directions, colour, start, regionStart, path);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Is(): A IsLoopType is not implemented.");
	}
	
	//-------------------------------------------------------------------------

	/**
	 * For test relative a path.
	 * 
	 * @param isType The type of query to perform.
	 * @param type   The graph element type [default SiteType of the board].
	 * @param from   The site to look the path [(last To)].
	 * @param who    The owner of the pieces on the path.
	 * @param role   The role of the player owning the pieces on the path.
	 * @param length The range size of the path.
	 * @param closed Is used to detect closed components [False].
	 * 
	 * @example (is Path Edge Mover length:(exact 4))
	 */
	public static BooleanFunction construct
	(
			          final IsPathType             isType,
					  final SiteType               type,
		   @Opt @Name final IntFunction            from,
	  @Or	          final game.util.moves.Player who,
	  @Or             final RoleType               role,
                @Name final RangeFunction         length,
		   @Opt @Name final BooleanFunction       closed
	)
	{
		int numNonNull = 0;
		if (who != null)
			numNonNull++;
		if (role != null)
			numNonNull++;

		if (numNonNull != 1)
			throw new IllegalArgumentException(
					"Is(): With IsPathType Only one who or role parameter must be non-null.");

		switch (isType)
		{
		case Path:
			return new IsPath(type, from, who, role, length, closed);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Is(): A IsPathType is not implemented.");
	}
	
	//-------------------------------------------------------------------------

	/**
	 * For test relative to an empty or occupied site.
	 * 
	 * @param isType The type of query to perform.
	 * @param type   Graph element type [default SiteType of the board].
	 * @param at     The index of the site.
	 * 
	 * @example (is Empty (to))
	 * @example (is Occupied Vertex (to))
	 */
	public static BooleanFunction construct
	(
			  final IsSiteType  isType,
		@Opt  final SiteType    type,
	     	  final IntFunction at
	)
	{
		switch (isType)
		{
		case Empty:
			return new IsEmpty(type, at);
		case Occupied:
			return new IsOccupied(type, at);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Is(): A IsSiteType is not implemented.");
	}
	
	//-------------------------------------------------------------------------

	/**
	 * For testing if a site is in a region or if an integer is in an array of
	 * integers.
	 * 
	 * @param isType The type of query to perform.
	 * @param site   The site [(to)].
	 * @param sites  The sites.
	 * @param region The region.
	 * @param array  The array of integers.
	 * 
	 * @example (is In {(last To) (last From)} (sites Mover))
	 * @example (is In (last To) (sites Mover))
	 */
	public static BooleanFunction construct
	(
		          final IsInType         isType, 
		@Opt @Or  final IntFunction      site,
		@Opt @Or  final IntFunction[]    sites, 
		     @Or2 final RegionFunction   region,
		     @Or2 final IntArrayFunction array
    )
	{
		int numNonNull = 0;
		if (site != null)
			numNonNull++;
		if (sites != null)
			numNonNull++;

		if (numNonNull > 1)
			throw new IllegalArgumentException(
					"Is(): With IsInType Only one site or sites parameter can be non-null.");
		
		int numNonNull2 = 0;
		if (region != null)
			numNonNull2++;
		if (array != null)
			numNonNull2++;

		if (numNonNull2 != 1)
			throw new IllegalArgumentException(
					"Is(): With IsInType one region or array parameter must be non-null.");
		
		switch (isType)
		{
		case In:
			return IsIn.construct(site, sites, region, array);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Is(): A IsInType is not implemented.");
	}
	
	//-------------------------------------------------------------------------

		/**
		 * For tests relative to a group.
		 * 
		 * @param isType The type of query to perform.
		 * @param in  The locations of the piece to check.
		 * @param toPlace  Check if still freedom if a piece is placed at this position
		 * 
		 * @example (is Threatened (id "King" Mover) at:(to))
		 */
		public static BooleanFunction construct
		(
					final IsGroupType   	isType,
			@Opt  	final SiteType    		type,
					final RegionFunction  	in,
			@Opt    final IntFunction       toPlace
		)
		{
//			System.out.println("Here");
			int numNull = 0;
			if (in == null)
				numNull++;
			
			if (numNull >= 1)
				throw new IllegalArgumentException(
						"Is(): With IsGroup only no parameter must be non-null.");
			switch (isType)
			{
			case Freedom:
				return new IsFreedom(type, in, toPlace);
			default:
				break;
			}

			// We should never reach that except if we forget some codes.
			throw new IllegalArgumentException("Is(): A IsGroupType is not implemented.");
		}

	//-------------------------------------------------------------------------
	
	private Is()
	{
		// Ensure that compiler does not pick up default constructor
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
	public boolean eval(Context context)
	{
		// Should not be called, should only be called on subclasses
		throw new UnsupportedOperationException("Is.eval(): Should never be called directly.");

		// return false;
	}

}
