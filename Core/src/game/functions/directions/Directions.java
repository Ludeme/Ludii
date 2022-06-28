package game.functions.directions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import annotations.Name;
import annotations.Opt;
import annotations.Or;
import game.Game;
import game.equipment.component.Component;
import game.functions.ints.IntFunction;
import game.functions.ints.last.LastFrom;
import game.functions.ints.last.LastTo;
import game.types.board.RelationType;
import game.types.board.SiteType;
import game.types.state.GameType;
import game.util.directions.AbsoluteDirection;
import game.util.directions.CompassDirection;
import game.util.directions.Direction;
import game.util.directions.DirectionFacing;
import game.util.directions.RelativeDirection;
import game.util.graph.Radial;
import main.collections.ArrayUtils;
import other.concept.Concept;
import other.context.Context;
import other.state.container.ContainerState;
import other.topology.Topology;
import other.topology.TopologyElement;
import other.translation.LanguageUtils;

/**
 * Converts the directions with absolute directions or relative directions
 * according to the direction of the piece/player to a list of integers.
 * 
 * @author Eric.Piette
 */
public class Directions extends DirectionsFunction implements Serializable
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Relative direction. */
	private final RelativeDirection[] relativeDirections;
	
	/** The type of the relative direction type. */
	private final RelationType relativeDirectionType;

	/**
	 * If true, the directions to return are computed according to the supported
	 * directions of the site and if not according to all the directions supported
	 * by the board.
	 */
	private final boolean bySite;
	
	/**
	 * Map of cached lists of absolute directions for facing directions.
	 * One map per thread, for thread-safety.
	 * Only used for relative directions without SameDirection and OppositeDirection, 
	 * and only if bySite is false.
	 */
	private final ThreadLocal<Map<DirectionFacing, List<AbsoluteDirection>>> cachedAbsDirs;

	//-------------------------------------------------------------------------
	
	/** In static cases, we precomputed the outcome of convertToAbsolute() once and can immediately return. */
	private List<AbsoluteDirection> precomputedDirectionsToReturn = null;

	/** Absolute directions. */
	private final AbsoluteDirection[] absoluteDirections;

	//-------------------------------------------------------------------------

	/** The type of the sites. */
	private final SiteType siteType;

	/** The from site. */
	private final IntFunction fromFn;

	/** The to site. */
	private final IntFunction toFn;

	//-------------------------------------------------------------------------

	/** The set of directions. */
	private final Direction randomDirections;

	/** The number of directions to return if possible. */
	private final IntFunction numDirection;

	//-------------------------------------------------------------------------

	/**
	 * For defining directions with absolute directions.
	 * 
	 * @param absoluteDirection  The absolute direction.
	 * @param absoluteDirections The absolute directions.
	 * 
	 * @example (directions Orthogonal)
	 */
	public Directions
	(
		@Or final AbsoluteDirection   absoluteDirection,
		@Or final AbsoluteDirection[] absoluteDirections
	)
	{
		int numNonNull = 0;
		if (absoluteDirection != null)
			numNonNull++;
		if (absoluteDirections != null)
			numNonNull++;

		if (numNonNull > 1)
			throw new IllegalArgumentException(
					"Only zero or one absoluteDirection, absoluteDirections parameter can be non-null.");

		relativeDirections = null;
		this.absoluteDirections = (absoluteDirections != null) ? absoluteDirections : new AbsoluteDirection[]
		{ absoluteDirection };
		relativeDirectionType = RelationType.Adjacent;
		bySite = false;
		cachedAbsDirs = null;
		siteType = null;
		fromFn = null;
		toFn = null;
		randomDirections = null;
		numDirection = null;
	}

	/**
	 * For defining directions with relative directions.
	 * 
	 * @param relativeDirection  The relative direction.
	 * @param relativeDirections The relative directions.
	 * @param of                 The type of directions to return [Adjacent].
	 * @param bySite             If true, the directions to return are computed
	 *                           according to the supported directions of the site
	 *                           and if not according to all the directions
	 *                           supported by the board [False].
	 * 
	 * @example (directions Forwards)
	 */
	public Directions
	(
	  	@Opt @Or       final RelativeDirection   relativeDirection,
		@Opt @Or       final RelativeDirection[] relativeDirections,
		@Opt     @Name final RelationType        of,
		@Opt     @Name final Boolean             bySite
	)
	{
		int numNonNull = 0;
		if (relativeDirection != null)
			numNonNull++;
		if (relativeDirections != null)
			numNonNull++;

		if (numNonNull > 1)
			throw new IllegalArgumentException(
					"Only zero or one relativeDirection, relativeDirections parameter can be non-null.");

		absoluteDirections = null;
		if (relativeDirections != null)
			this.relativeDirections = relativeDirections;
		else
			this.relativeDirections = (relativeDirection == null) ? new RelativeDirection[]
			{ RelativeDirection.Forward } : new RelativeDirection[]
			{ relativeDirection };
		relativeDirectionType = (of != null) ? of : RelationType.Adjacent;
		this.bySite = (bySite == null) ? false : bySite.booleanValue();
		
		if 
		(
			!this.bySite && 
			!ArrayUtils.contains(this.relativeDirections, RelativeDirection.SameDirection) &&
			!ArrayUtils.contains(this.relativeDirections, RelativeDirection.OppositeDirection)
		)
		{
			cachedAbsDirs = ThreadLocal.withInitial(() -> new HashMap<DirectionFacing, List<AbsoluteDirection>>());
		}
		else
		{
			cachedAbsDirs = null;
		}

		siteType = null;
		fromFn = null;
		toFn = null;
		randomDirections = null;
		numDirection = null;
	}
	
	/**
	 * For returning a direction between two sites of the same type.
	 * 
	 * @param type The type of the graph element.
	 * @param from The 'from' site.
	 * @param to   The 'to' site.
	 * 
	 * @example (directions Cell from:(last To) to:(last From)))
	 */
	public Directions
	(
			  final SiteType    type, 
		@Name final IntFunction from, 
		@Name final IntFunction to
	)
	{

		siteType = type;
		fromFn = from;
		toFn = to;
		bySite = false;
		relativeDirections = null;
		relativeDirectionType = null;
		absoluteDirections = null;
		cachedAbsDirs = null;
		randomDirections = null;
		numDirection = null;
	}
	
	/**
	 * For returning a direction between two sites of the same type.
	 * 
	 * @param type       The type of random direction.
	 * @param directions The direction function.
	 * @param num        The number of directions to return (if possible).
	 * 
	 * @example (directions Random Orthogonal num:2)
	 */
	public Directions
	(
			  final RandomDirectionType type, 
			  final Direction           directions, 
		@Name final IntFunction         num
	)
	{
		siteType = null;
		fromFn = null;
		toFn = null;
		bySite = false;
		relativeDirections = null;
		relativeDirectionType = null;
		absoluteDirections = null;
		cachedAbsDirs = null;
		randomDirections = directions;
		numDirection = num;
	}

	//-------------------------------------------------------------------------

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = 0l;

		if (siteType != null)
			gameFlags |= SiteType.gameFlags(siteType);

		if (fromFn != null)
			gameFlags = gameFlags | fromFn.gameFlags(game);

		if (toFn != null)
			gameFlags = gameFlags | toFn.gameFlags(game);

		if (numDirection != null)
			gameFlags = gameFlags | numDirection.gameFlags(game);

		if (randomDirections != null)
			gameFlags = gameFlags | GameType.Stochastic;

		return gameFlags;
	}
	

	@Override
	public boolean isStatic()
	{
		return (absoluteDirections != null);
	}

	@Override
	public void preprocess(final Game game)
	{
		if (fromFn != null)
			fromFn.preprocess(game);

		if (toFn != null)
			toFn.preprocess(game);

		if (numDirection != null)
			numDirection.preprocess(game);

		if (isStatic())
			precomputedDirectionsToReturn = convertToAbsolute(null, null, null, null, null, null);
	}

	//-------------------------------------------------------------------------
	
	/**
	 * @return If the ludeme corresponds to all the directions.
	 */
	public boolean isAll()
	{
		return (absoluteDirections != null && absoluteDirections[0].equals(AbsoluteDirection.All));
	}

	@Override
	public RelativeDirection[] getRelativeDirections()
	{
		return relativeDirections;
	}

	/**
	 * @return The absolute direction.
	 */
	public AbsoluteDirection absoluteDirection()
	{
		if (absoluteDirections == null)
			return null;

		return absoluteDirections[0];
	}

	@Override
	public List<AbsoluteDirection> convertToAbsolute
	(
		final SiteType graphType, 
		final TopologyElement element,
		final Component newComponent, 
		final DirectionFacing newFacing,
		final Integer newRotation,
		final Context context
	)
	{
		if (precomputedDirectionsToReturn != null)
			return precomputedDirectionsToReturn;

		if(randomDirections != null)
		{
			int num = numDirection.eval(context);
			final List<AbsoluteDirection> directions = randomDirections.directionsFunctions()
					.convertToAbsolute(graphType, element, newComponent, newFacing, newRotation, context);
			final List<AbsoluteDirection> realDirectionToCheck = new ArrayList<AbsoluteDirection>();

			for (int i = directions.size() - 1; i >= 0; i--)
			{
				final AbsoluteDirection absoluteDirection = directions.get(i);
				if (absoluteDirection.equals(AbsoluteDirection.Adjacent))
				{
					final List<DirectionFacing> supportedDirection = element.supportedAdjacentDirections();
					for (final DirectionFacing facingDirection : supportedDirection)
						if (!directions.contains(facingDirection.toAbsolute()))
							realDirectionToCheck.add(facingDirection.toAbsolute());
				}
				else if (absoluteDirection.equals(AbsoluteDirection.Orthogonal))
				{
					final List<DirectionFacing> supportedDirection = element.supportedOrthogonalDirections();
					for (final DirectionFacing facingDirection : supportedDirection)
						if (!directions.contains(facingDirection.toAbsolute()))
							realDirectionToCheck.add(facingDirection.toAbsolute());
				}
				else if (absoluteDirection.equals(AbsoluteDirection.Diagonal))
				{
					final List<DirectionFacing> supportedDirection = element.supportedDiagonalDirections();
					for (final DirectionFacing facingDirection : supportedDirection)
						if (!directions.contains(facingDirection.toAbsolute()))
							realDirectionToCheck.add(facingDirection.toAbsolute());
				}
				else if (absoluteDirection.equals(AbsoluteDirection.OffDiagonal))
				{
					final List<DirectionFacing> supportedDirection = element.supportedOffDirections();
					for (final DirectionFacing facingDirection : supportedDirection)
						if (!directions.contains(facingDirection.toAbsolute()))
							realDirectionToCheck.add(facingDirection.toAbsolute());
				}
				else if (absoluteDirection.equals(AbsoluteDirection.All))
				{
					final List<DirectionFacing> supportedDirection = element.supportedDirections();
					for (final DirectionFacing facingDirection : supportedDirection)
						if (!directions.contains(facingDirection.toAbsolute()))
							realDirectionToCheck.add(facingDirection.toAbsolute());
				}
				else
					realDirectionToCheck.add(absoluteDirection);
			}

			if (num > realDirectionToCheck.size())
				num = realDirectionToCheck.size();
			
			final List<AbsoluteDirection> directionsToReturn = new ArrayList<AbsoluteDirection>();
			
			if (num == 0)
				return directionsToReturn;

			while (directionsToReturn.size() != num)
			{
				final AbsoluteDirection absoluteDirection = realDirectionToCheck
						.get(context.rng().nextInt(realDirectionToCheck.size()));
				if (!directionsToReturn.contains(absoluteDirection))
					directionsToReturn.add(absoluteDirection);
			}
			
			return directionsToReturn;
		}
		
		if (siteType != null)
		{
			final Topology topology = context.topology();
			final int from = fromFn.eval(context);
			final int to = toFn.eval(context);
			final int maxSize = topology.getGraphElements(siteType).size();

			if (from < 0 || from >= maxSize || to < 0 || to >= maxSize)
				return new ArrayList<AbsoluteDirection>();

			final TopologyElement fromV = topology.getGraphElements(siteType).get(from);
			
			final List<DirectionFacing> directionsSupported = topology.supportedDirections(RelationType.All, siteType);
			final List<AbsoluteDirection> directionList = new ArrayList<AbsoluteDirection>();
			
			for (final DirectionFacing facingDirection : directionsSupported)
			{
				final AbsoluteDirection absDirection = facingDirection.toAbsolute();
				final List<Radial> radials = topology.trajectories().radials(siteType, fromV.index(), absDirection);

				for (final Radial radial : radials)
				{
					for (int toIdx = 1; toIdx < radial.steps().length; toIdx++)
					{
						final int toRadial = radial.steps()[toIdx].id();
						if (toRadial == to)
						{
							directionList.add(absDirection);
							break;
						}
					}
				}
			}

			return directionList;
		}

		if (absoluteDirections != null)
		{
			return Arrays.asList(absoluteDirections);
		}
		else if (element != null)
		{
			final Topology topology = context.topology();
			final int site = element.index();
			final int containerId = (graphType != SiteType.Cell ? 0 : context.containerId()[site]);
			final ContainerState cs = context.containerState(containerId);
			final int what = cs.what(site, graphType);
			
//			if ( &newComponent == null) // we need a component to return a relative direction according to it.
//				return new ArrayList<AbsoluteDirection>();
			
			final Component component = (newComponent != null) ? newComponent : (what >= 1) ? context.components()[what] : null;

			final List<DirectionFacing> directionsSupported = (bySite) ? element.supportedDirections(relativeDirectionType)
					: topology.supportedDirections(relativeDirectionType, graphType);

			// If no facing direction, we consider the piece to face to the north.
			DirectionFacing facingDirection = (newFacing != null) ? newFacing
					: (component != null) ? ((component.getDirn() != null) ? component.getDirn() : CompassDirection.N) : CompassDirection.N;
			
			// We apply the rotation here.
			int rotation = (newRotation == null) ? cs.rotation(site, graphType) : newRotation.intValue();
			while (rotation != 0)
			{
				facingDirection = RelativeDirection.FR.directions(facingDirection, directionsSupported).get(0);
				rotation--;
			}
			
			final Map<DirectionFacing, List<AbsoluteDirection>> cachedDirs;
			if (cachedAbsDirs == null)
			{
				cachedDirs = null;
			}
			else
			{
				cachedDirs = cachedAbsDirs.get();
				final List<AbsoluteDirection> cachedList = cachedDirs.get(facingDirection);
				if (cachedList != null)
					return cachedList;
			}
			
			final List<AbsoluteDirection> directionsToReturn = new ArrayList<AbsoluteDirection>();

			for (final RelativeDirection relativeDirection : relativeDirections)
			{
				// Special case for the same direction.
				if (relativeDirection.equals(RelativeDirection.SameDirection))
				{
					final int lastFrom = new LastFrom(null).eval(context);
					final int lastTo = new LastTo(null).eval(context);

					boolean found = false;
					for (final DirectionFacing direction : directionsSupported)
					{
						final AbsoluteDirection absDirection = direction.toAbsolute();
						final List<Radial> radials = topology.trajectories().radials(graphType, lastFrom, absDirection);

						for (final Radial radial : radials)
						{
							for (int toIdx = 1; toIdx < radial.steps().length; toIdx++)
							{
								final int toSite = radial.steps()[toIdx].id();
								if (toSite == lastTo)
								{
									directionsToReturn.add(absDirection);
									found = true;
									break;
								}
							}
						}
						if (found)
							break;
					}
				}
				else if (relativeDirection.equals(RelativeDirection.OppositeDirection))
				{
					final int lastFrom = new LastFrom(null).eval(context);
					final int lastTo = new LastTo(null).eval(context);

					boolean found = false;
					for (final DirectionFacing direction : directionsSupported)
					{
						final AbsoluteDirection absDirection = direction.toAbsolute();
						final List<Radial> radials = topology.trajectories().radials(graphType, lastTo, absDirection);

						for (final Radial radial : radials)
						{
							for (int toIdx = 1; toIdx < radial.steps().length; toIdx++)
							{
								final int toSite = radial.steps()[toIdx].id();
								if (toSite == lastFrom)
								{
									directionsToReturn.add(absDirection);
									found = true;
									break;
								}
							}
						}
						if (found)
							break;
					}
				}
				else
				{
					// All the other relative directions are converted here.
					final List<DirectionFacing> supportedDirections = (bySite)
							? element.supportedDirections(relativeDirectionType)
							: topology.supportedDirections(relativeDirectionType, graphType);

					final List<DirectionFacing> directions = relativeDirection.directions(facingDirection,
							supportedDirections);
					
					for (final DirectionFacing direction : directions)
						directionsToReturn.add(direction.toAbsolute());
				}
			}
			
			// Cache result if allowed
			if (cachedDirs != null)
				cachedDirs.put(facingDirection, directionsToReturn);
			
			return directionsToReturn;
		}
		else
		{
			return new ArrayList<AbsoluteDirection>();
		}
	}
	
	@Override
	public String toString()
	{
		String str = "DirectionChoice(";
		if (absoluteDirections != null)
			for (final AbsoluteDirection absoluteDirection : absoluteDirections)
				str += absoluteDirection.name() + ", ";
		else if (relativeDirections != null)
			for (final RelativeDirection relativeDirection : relativeDirections)
				str += relativeDirection.name() + ", ";
		str += ")";
		return str;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();

		if (relativeDirections != null)
			for (final RelativeDirection relativeDirection : relativeDirections)
				concepts.or(RelativeDirection.concepts(relativeDirection));

		if (absoluteDirections != null)
			for (final AbsoluteDirection absoluteDirection : absoluteDirections)
				concepts.or(AbsoluteDirection.concepts(absoluteDirection));

		if (siteType != null)
			concepts.or(SiteType.concepts(siteType));

		if (fromFn != null)
			concepts.or(fromFn.concepts(game));

		if (toFn != null)
			concepts.or(toFn.concepts(game));

		if (numDirection != null)
			concepts.or(numDirection.concepts(game));

		if (randomDirections != null)
			concepts.set(Concept.Stochastic.id(), true);

		return concepts;
	}
	
	@Override
	public String toEnglish(final Game game)
	{
		String text = "";
		int count=0;
		
		if (absoluteDirections != null) 
		{
			for (final AbsoluteDirection absoluteDirection : absoluteDirections) 
			{
				text += LanguageUtils.GetDirection(absoluteDirection.name());
				count++;
				
	            if(count == absoluteDirections.length-1)
	                text+=" or ";
	            else if(count < absoluteDirections.length)
	                text+=", ";
			}
		}

		else if (relativeDirections != null)
		{
			for (final RelativeDirection relativeDirection : relativeDirections) 
			{
				text += LanguageUtils.GetDirection(relativeDirection.name());
				count++;
				
	            if(count == relativeDirections.length-1)
	                text+=" or ";
	            else if(count < relativeDirections.length)
	                text+=", ";
			}	
		}
		
		return text;
	}
}
