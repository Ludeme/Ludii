package game.functions.booleans.is.loop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import annotations.Or;
import annotations.Or2;
import game.Game;
import game.equipment.component.Component;
import game.equipment.component.tile.Path;
import game.functions.booleans.BaseBooleanFunction;
import game.functions.directions.Directions;
import game.functions.directions.DirectionsFunction;
import game.functions.ints.IntFunction;
import game.functions.ints.last.LastTo;
import game.functions.ints.state.Mover;
import game.functions.region.RegionFunction;
import game.functions.region.sites.simple.SitesLastTo;
import game.types.board.RelationType;
import game.types.board.SiteType;
import game.types.play.RoleType;
import game.util.directions.AbsoluteDirection;
import game.util.directions.Direction;
import game.util.directions.RelativeDirection;
import game.util.equipment.Region;
import game.util.graph.Radial;
import gnu.trove.list.array.TIntArrayList;
import main.Constants;
import other.concept.Concept;
import other.context.Context;
import other.location.FullLocation;
import other.location.Location;
import other.state.container.ContainerState;
import other.topology.Cell;
import other.topology.Topology;
import other.topology.TopologyElement;

/**
 * Detects a loop.
 * 
 * @author Eric.Piette & cambolbro & tahmina
 * 
 * @remarks This is used for tile games or connection games.
 */
@Hide
public final class IsLoop extends BaseBooleanFunction
{
	private static final long serialVersionUID = 1L;	

	//-------------------------------------------------------------------------
				
	/** List of all the possible owners inside the loop. */
	private final IntFunction[] rolesArray;
	
	/** The starting point of the loop. */
	private final IntFunction startFn;

	/** The starting points of the loop. */
	private final RegionFunction regionStartFn;	
	
	/** The SiteType of the site. */
	private SiteType type;

	/** Direction chosen. */
	private final DirectionsFunction dirnChoice;

	//---------------------------Data for Path Tile ------------------------------
	
	/** True if we check a loop path. */
	private final boolean tilePath;	
	
	/** The colour of the path. */
	private final IntFunction colourFn;
	
	// ----------------------pre-computed-----------------------------------------

	// Indices of the outer sites of the board.
	TIntArrayList outerIndices;

	// -------------------------------------------------------------------------
	/**
	 * @param type         The graph element type [default SiteType of the board].
	 * @param surround     Used to define the inside condition of the loop.
	 * @param surroundList The list of items inside the loop.
	 * @param directions   The directions of the connected pieces used to connect
	 *                     the region [Adjacent].
	 * @param colour       The owner of the looping pieces [Mover].
	 * @param start        The starting point of the loop [(last To)].
	 * @param regionStart  The region to start to detect the loop.
	 * @param path         True if attempting to detect a loop in the path of the
	 *                     pieces (e.g. Trax).
	 */
	public IsLoop
	(
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
			throw new IllegalArgumentException("Zero or one Or parameter can be non-null.");

		int numNonNull2 = 0;
		if (start != null)
			numNonNull2++;
		if (regionStart != null)
			numNonNull2++;

		if (numNonNull2 > 1)
			throw new IllegalArgumentException("Zero or one Or2 parameter can be non-null.");
		
		this.colourFn 		= (colour == null) 	   ? new Mover() : colour;
		this.startFn 	 	= (start == null)      ? new LastTo(null) : start;
		this.regionStartFn 	= (regionStart == null)? ((start == null) ? new SitesLastTo() : null) : regionStart;
		this.tilePath 		= (path == null)       ? false : path.booleanValue();		
		
		if (surround != null) 
		{
			this.rolesArray = new IntFunction[]
			{ RoleType.toIntFunction(surround) };
		}
		else
		{
			if (surroundList != null)
			{
				this.rolesArray = new IntFunction[surroundList.length];
				for (int i = 0; i < surroundList.length; i++)
					this.rolesArray[i] = RoleType.toIntFunction(surroundList[i]);
			}
			else
				this.rolesArray = null;
		}

		this.type = type;

		this.dirnChoice = (directions != null) ? directions.directionsFunctions()
				: new Directions(AbsoluteDirection.Adjacent, null);
	}

	//------------------------------------------------------------------------------------------
	
	/**
	 * @param context	 The current Context of the game board.
	 * @return 		     Is the last move create a ring or not?
	 */
	@Override
	public boolean eval(final Context context)
	{
		final int from = startFn.eval(context);

		// Check if this is a site.
		if (from < 0)
			return false;

		final Topology topology = context.topology();
		final SiteType realType = (type != null) ? type : context.game().board().defaultSite();

		// Check if the site is in the board.
		if (from >= topology.getGraphElements(realType).size())
			return false;
		final ContainerState cs = context.containerState(0);
		final int what = cs.what(from, realType);
		final int colourLoop = colourFn.eval(context);

		// Check if the site is not empty.
		if (what <= 0)
			return false;

		// If the piece is a tile we run the corresponding code.
		if (context.components()[what].isTile() && tilePath)
			return evalTilePath(context);
		
		final TIntArrayList ownersOfEnclosedSite = (rolesArray == null) ? null : new TIntArrayList();
		
		if (rolesArray != null)
			for (int i = 0; i < rolesArray.length; i++)
				ownersOfEnclosedSite.add(rolesArray[i].eval(context));

		// We get all the sites around the starting position with not the same piece on
		// them.
		final TIntArrayList aroundSites = new TIntArrayList();
		final TopologyElement startElement = topology.getGraphElements(realType).get(from);
		final List<AbsoluteDirection> directionsFromStart = new Directions(AbsoluteDirection.Adjacent, null)
				.convertToAbsolute(realType, startElement, null,
				null, null,
				context);
		for (final AbsoluteDirection direction : directionsFromStart)
		{
			final List<game.util.graph.Step> steps = topology.trajectories().steps(realType, startElement.index(),
					realType, direction);

			for (final game.util.graph.Step step : steps)
			{
				final int to = step.to().id();
				if (ownersOfEnclosedSite != null)
				{
					final int whoTo = cs.who(to, realType);
					if (ownersOfEnclosedSite.contains(whoTo) && !outerIndices.contains(to))
						aroundSites.add(to);
				}
				else if (!outerIndices.contains(to))
					aroundSites.add(to);
			}
		}

		// We look for a group starting from each site around which not touch the outer
		// sites.
		for (int indexSite = aroundSites.size() - 1; indexSite >= 0; indexSite--)
		{
			final int origin = aroundSites.get(indexSite);
			// If site already checked we do not look at it again.
//			if (sitesToCheckInPreviousGroup.contains(origin))
//				continue;

			final TIntArrayList groupSites = new TIntArrayList();
			groupSites.add(origin);
			boolean continueSearch = true;
			
			final TIntArrayList sitesExplored = new TIntArrayList();
			int i = 0;
			while (sitesExplored.size() != groupSites.size())
			{
				final int site = groupSites.get(i);
				final TopologyElement siteElement = topology.getGraphElements(realType).get(site);
				final List<AbsoluteDirection> directions = new Directions(AbsoluteDirection.Orthogonal, null)
						.convertToAbsolute(realType, siteElement, null,
						null, null, context);

				for (final AbsoluteDirection direction : directions)
				{
					final List<Radial> radials = topology.trajectories().radials(type, siteElement.index(), direction);
					for (final Radial radial : radials)
					{
						for (int toIdx = 1; toIdx < radial.steps().length; toIdx++)
						{
							final int to = radial.steps()[toIdx].id();

							// If we already have it we continue to look the others.
							if (groupSites.contains(to))
								continue;

							// Not the border of the loop.
							if (what != cs.what(to, realType))
							{
								if (ownersOfEnclosedSite != null)
								{
									final int whoTo = cs.who(to, realType);
									if (ownersOfEnclosedSite.contains(whoTo) && !outerIndices.contains(to))
									{
										groupSites.add(to);
									}
								}
								else
								{
									groupSites.add(to);
								}
								// If outer site, no loop.
								if (outerIndices.contains(to))
								{
									continueSearch = false;
									break;
								}
							}
							else // We stop this radial because this is the loop.
								break;
						}
						if (!continueSearch)
							break;
					}
					if (!continueSearch)
						break;
				}

				if (!continueSearch)
					break;

				sitesExplored.add(site);
				i++;
			}

			// If a potential loop is detected we need to check if a path using the
			// direction selected exist in it and if all the pieces looping are owned by the
			// correct player.
			if (continueSearch)
			{
				// Get the loop
				final TIntArrayList loop = new TIntArrayList();
				for (int indexGroup = 0; indexGroup < groupSites.size(); indexGroup++)
				{
					final int siteGroup = groupSites.get(indexGroup);
					final TopologyElement element = topology.getGraphElements(realType).get(siteGroup);
					final List<AbsoluteDirection> directionsElement = new Directions(AbsoluteDirection.Orthogonal, null)
							.convertToAbsolute(realType, element,
							null, null, null, context);
					for (final AbsoluteDirection direction : directionsElement)
					{
						final List<game.util.graph.Step> steps = topology.trajectories().steps(realType,
								element.index(), realType, direction);

						for (final game.util.graph.Step step : steps)
						{
							final int to = step.to().id();
							if (!groupSites.contains(to) && !loop.contains(to))
								loop.add(to);
						}
					}
				}

				// If all the pieces looping are owned by the correct player.
				boolean ownedPiecesLooping = true;
				for (int indexLoop = 0; indexLoop < loop.size(); indexLoop++)
				{
					final int siteLoop = loop.get(indexLoop);
					if (cs.who(siteLoop, realType) != colourLoop)
					{
						ownedPiecesLooping = false;
						break;
					}
				}
				if (!ownedPiecesLooping)
					continue;

				boolean loopFound = false;
				int previousIndice = 0;
				int indexSiteLoop = 0;
				final TIntArrayList exploredLoop = new TIntArrayList();
				while (!loopFound)
				{
					if (loop.size() == 0)
						break;

					final int siteLoop = loop.get(indexSiteLoop);
					final int whatElement = cs.what(siteLoop, realType);
					if (whatElement != what)
					{
						loop.remove(siteLoop);
						indexSiteLoop = previousIndice;
						continue;
					}

					final TopologyElement element = topology.getGraphElements(realType).get(siteLoop);
					final List<AbsoluteDirection> directionsElement = dirnChoice.convertToAbsolute(realType, element,
							null, null, null, context);

					int newSite = Constants.UNDEFINED;
					for (final AbsoluteDirection direction : directionsElement)
					{
						final List<game.util.graph.Step> steps = topology.trajectories().steps(realType,
								element.index(), realType, direction);

						for (final game.util.graph.Step step : steps)
						{
							final int to = step.to().id();
							final int whatTo = cs.what(to, realType);
							if (loop.contains(to) && whatTo == what)
							{
								newSite = to;
								break;
							}
						}
						if (newSite != Constants.UNDEFINED)
							break;
					}
					if (newSite == Constants.UNDEFINED)
					{
						loop.remove(siteLoop);
						exploredLoop.remove(siteLoop);
						indexSiteLoop = previousIndice;
						continue;
					}
					else
					{
						exploredLoop.add(siteLoop);
						if (exploredLoop.size() == loop.size())
						{
							loopFound = true;
							break;
						}
						previousIndice = indexSiteLoop;
						indexSiteLoop = loop.indexOf(newSite);
					}
				}

				if (loopFound)
					return true;
			}
		}

		return false;
	}

	//----------------------------------------------------------------------------------
	
	/**
	 * @param context
	 * @return if a loop is realised with paths on pieces.
	 */
	public boolean evalTilePath(final Context context)
	{
		final int[] regionToCheck;
		if (regionStartFn != null)
		{
			final Region region = regionStartFn.eval(context);
			regionToCheck = region.sites();
		}
		else
		{
			regionToCheck = new int[1];
			regionToCheck[0] = startFn.eval(context);
		}

		final DirectionsFunction directionFunction = new Directions(RelativeDirection.Forward, null,
				RelationType.Orthogonal, null);

		for (int p = 0; p < regionToCheck.length; p++)
		{
			final int from = regionToCheck[p];
			final int colourLoop = colourFn.eval(context);
			final int cid = context.containerId()[from];
			final Topology graph = context.topology();
			final ContainerState cs = context.containerState(cid);

			final int ratioAdjOrtho = context.topology().numEdges();

			final TIntArrayList tileConnected = new TIntArrayList();
			final TIntArrayList originTileConnected = new TIntArrayList();
			tileConnected.add(from);
			originTileConnected.add(from);

			for (int index = 0; index < tileConnected.size(); index++)
			{
				final int site = tileConnected.getQuick(index);
				final Cell cell = graph.cells().get(site);
				final int what = cs.what(site, SiteType.Cell);
				final Component component = context.components()[what];
				final int rotation = (cs.rotation(site, SiteType.Cell) * 2) / ratioAdjOrtho;
				final Path[] paths = Arrays.copyOf(component.paths(), component.paths().length);

				for (int i = 0; i < paths.length; i++)
				{
					final Path path = paths[i];

					if (path.colour().intValue() == colourLoop)
					{
						// Side1
						final List<AbsoluteDirection> directionsStep1 = directionFunction.convertToAbsolute(SiteType.Cell,
								cell, null, null, Integer.valueOf(path.side1(rotation,
										graph.numEdges())),
								context);
						final AbsoluteDirection directionSide1 = directionsStep1.get(0);
						final List<game.util.graph.Step> stepsSide1 = graph.trajectories().steps(SiteType.Cell, cell.index(),
								SiteType.Cell, directionSide1);
						if (stepsSide1.size() != 0)
						{
							final int site1Connected = stepsSide1.get(0).to().id();
							if (originTileConnected.getQuick(index) != site1Connected && site1Connected == from)
								return true;

							final int whatSide1 = cs.whatCell(site1Connected);
							if (originTileConnected.getQuick(index) != site1Connected && whatSide1 != 0
									&& context.components()[whatSide1].isTile())
							{
								tileConnected.add(site1Connected);
								originTileConnected.add(site);
							}
						}

						// Side 2
						final List<AbsoluteDirection> directionsSide2 = directionFunction.convertToAbsolute(
								SiteType.Cell, cell, null, null,
								Integer.valueOf(path.side2(rotation, graph.numEdges())), context);
						final AbsoluteDirection directionSide2 = directionsSide2.get(0);
						final List<game.util.graph.Step> stepsSide2 = graph.trajectories().steps(SiteType.Cell,
								cell.index(), SiteType.Cell, directionSide2);

						if (stepsSide2.size() != 0)
						{
							final int site2Connected = stepsSide2.get(0).to().id();
							if (originTileConnected.getQuick(index) != site2Connected && site2Connected == from)
								return true;

							final int whatSide2 = cs.whatCell(site2Connected);
							if (originTileConnected.getQuick(index) != site2Connected && whatSide2 != 0
									&& context.components()[whatSide2].isTile())
							{
								tileConnected.add(site2Connected);
								originTileConnected.add(site);
							}
						}
					}
				}
			}
		}
		return false;
	}
		
	//------------------------------------------------------------------------------
	
	@Override
	public String toString()
	{
		String str = "";
		str += "IsLoop()";
		return str;
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		return false;
	}

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = 0L;
		
		gameFlags |= SiteType.gameFlags(type);

		if (colourFn != null)
			gameFlags |= colourFn.gameFlags(game);
		
		if (regionStartFn != null)
			gameFlags |= regionStartFn.gameFlags(game);

		if (startFn != null)
			gameFlags |= startFn.gameFlags(game);

		return gameFlags;
	}
	
	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(super.concepts(game));
		concepts.set(Concept.Loop.id(), true);

		if (colourFn != null)
			concepts.or(colourFn.concepts(game));

		if (regionStartFn != null)
			concepts.or(regionStartFn.concepts(game));

		if (startFn != null)
			concepts.or(startFn.concepts(game));
		concepts.or(SiteType.concepts(type));

		if (dirnChoice != null)
			concepts.or(dirnChoice.concepts(game));

		return concepts;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= super.missingRequirement(game);

		if (colourFn != null)
			missingRequirement |= colourFn.missingRequirement(game);

		if (regionStartFn != null)
			missingRequirement |= regionStartFn.missingRequirement(game);

		if (startFn != null)
			missingRequirement |= startFn.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= super.willCrash(game);

		if (colourFn != null)
			willCrash |= colourFn.willCrash(game);

		if (regionStartFn != null)
			willCrash |= regionStartFn.willCrash(game);

		if (startFn != null)
			willCrash |= startFn.willCrash(game);
		return willCrash;
	}

	@Override
	public void preprocess(final Game game)
	{		
		type = SiteType.use(type, game);

		// We get the outer sites.
		final List<TopologyElement> outerElements = game.board().topology().outer(type);
		outerIndices = new TIntArrayList();
		for (final TopologyElement element : outerElements)
			outerIndices.add(element.index());

		if (colourFn != null)
			colourFn.preprocess(game);

		if (rolesArray != null)
			for (final IntFunction role : rolesArray)
				role.preprocess(game);

		if (startFn != null)
			startFn.preprocess(game);

		if (regionStartFn != null)
			regionStartFn.preprocess(game);
	}

	// ------------------------------for GUI-------------------------------------

	@Override
	public List<Location> satisfyingSites(final Context context)
	{
		if (!eval(context))
			return new ArrayList<Location>();

		final List<Location> winningSites = new ArrayList<Location>();

		final int from = startFn.eval(context);

		// Check if this is a site.
		if (from < 0)
			return new ArrayList<Location>();

		final Topology topology = context.topology();
		final SiteType realType = (type != null) ? type : context.game().board().defaultSite();

		// Check if the site is in the board.
		if (from >= topology.getGraphElements(realType).size())
			return new ArrayList<Location>();

		final ContainerState cs = context.containerState(0);
		final int what = cs.what(from, realType);

		// Check if the site is not empty.
		if (what <= 0)
			return new ArrayList<Location>();

		// If the piece is a tile we run the corresponding code.
		if (context.components()[what].isTile() && tilePath)
			return new ArrayList<Location>();

		final TIntArrayList ownersOfEnclosedSite = (rolesArray == null) ? null : new TIntArrayList();

		if (rolesArray != null)
			for (int i = 0; i < rolesArray.length; i++)
				ownersOfEnclosedSite.add(rolesArray[i].eval(context));

		// We get all the sites around the starting position with not the same piece on
		// them.
		final TIntArrayList aroundSites = new TIntArrayList();
		final TopologyElement startElement = topology.getGraphElements(realType).get(from);
		final List<AbsoluteDirection> directionsFromStart = new Directions(AbsoluteDirection.Adjacent, null)
				.convertToAbsolute(realType, startElement, null, null, null, context);
		for (final AbsoluteDirection direction : directionsFromStart)
		{
			final List<game.util.graph.Step> steps = topology.trajectories().steps(realType, startElement.index(),
					realType, direction);

			for (final game.util.graph.Step step : steps)
			{
				final int to = step.to().id();
				if (ownersOfEnclosedSite != null)
				{
					final int whoTo = cs.who(to, realType);
					if (ownersOfEnclosedSite.contains(whoTo) && !outerIndices.contains(to))
						aroundSites.add(to);
				}
				else if (!outerIndices.contains(to))
					aroundSites.add(to);
			}
		}

		// We look for a group starting from each site around which not touch the outer
		// sites.
		for (int indexSite = aroundSites.size() - 1; indexSite >= 0; indexSite--)
		{
			final int origin = aroundSites.get(indexSite);

			final TIntArrayList groupSites = new TIntArrayList();
			groupSites.add(origin);
			boolean continueSearch = true;

			final TIntArrayList sitesExplored = new TIntArrayList();
			int i = 0;
			while (sitesExplored.size() != groupSites.size())
			{
				final int site = groupSites.get(i);
				final TopologyElement siteElement = topology.getGraphElements(realType).get(site);
				final List<AbsoluteDirection> directions = new Directions(AbsoluteDirection.Orthogonal, null)
						.convertToAbsolute(realType, siteElement, null, null, null, context);

				for (final AbsoluteDirection direction : directions)
				{
					final List<Radial> radials = topology.trajectories().radials(type, siteElement.index(), direction);
					for (final Radial radial : radials)
					{
						for (int toIdx = 1; toIdx < radial.steps().length; toIdx++)
						{
							final int to = radial.steps()[toIdx].id();

							// If we already have it we continue to look the others.
							if (groupSites.contains(to))
								continue;

							// Not the border of the loop.
							if (what != cs.what(to, realType))
							{
								if (ownersOfEnclosedSite != null)
								{
									final int whoTo = cs.who(to, realType);
									if (ownersOfEnclosedSite.contains(whoTo) && !outerIndices.contains(to))
									{
										groupSites.add(to);
									}
								}
								else
								{
									groupSites.add(to);
								}
								// If outer site, no loop.
								if (outerIndices.contains(to))
								{
									continueSearch = false;
									break;
								}
							}
							else // We stop this radial because this is the loop.
								break;
						}
						if (!continueSearch)
							break;
					}
					if (!continueSearch)
						break;
				}

				if (!continueSearch)
					break;

				sitesExplored.add(site);
				i++;
			}

			// If a group is found without to touch the outer sites, a loop is detecting.
			if (continueSearch)
			{
				final TIntArrayList loop = new TIntArrayList();
				for (int indexGroup = 0; indexGroup < groupSites.size(); indexGroup++)
				{
					final int siteGroup = groupSites.get(indexGroup);
					final TopologyElement element = topology.getGraphElements(realType).get(siteGroup);
					final List<AbsoluteDirection> directionsElement = new Directions(AbsoluteDirection.Orthogonal, null)
							.convertToAbsolute(realType, element, null, null, null, context);
					for (final AbsoluteDirection direction : directionsElement)
					{
						final List<game.util.graph.Step> steps = topology.trajectories().steps(realType,
								element.index(), realType, direction);

						for (final game.util.graph.Step step : steps)
						{
							final int to = step.to().id();
							if (!groupSites.contains(to) && !loop.contains(to))
								loop.add(to);
						}
					}

				}

				for (int indexWin = 0; indexWin < loop.size(); indexWin++)
					winningSites.add(new FullLocation(loop.get(indexWin), 0, realType));
				return filterWinningSites(context, winningSites);
			}
		}

		return new ArrayList<Location>();
	}

	/**
	 * @param context      the context.
	 * @param winningGroup The winning group detected in satisfyingSites.
	 * @return The minimum group of sites making the loop.
	 */
	public List<Location> filterWinningSites(final Context context, final List<Location> winningGroup)
	{
		final Topology topology = context.topology();
		final SiteType realType = (type != null) ? type : context.game().board().defaultSite();

		// Minimum group to connect the regions.
		final List<Location> minimumGroup = new ArrayList<Location>(winningGroup);

		for (int i = minimumGroup.size() - 1; i >= 0; i--)
		{
			final TIntArrayList groupMinusI = new TIntArrayList();
			for (int j = 0; j < minimumGroup.size(); j++)
				if (j != i)
					groupMinusI.add(minimumGroup.get(j).site());

			// System.out.println("groupMinusI is" + groupMinusI);

			// Check if all the pieces are in one group.
			final int startGroup = groupMinusI.get(0);
			int lastExploredSite = startGroup;
			final TIntArrayList groupSites = new TIntArrayList();
			groupSites.add(startGroup);
			if (groupSites.size() > 0)
			{
				final TIntArrayList sitesExplored = new TIntArrayList();

				int k = 0;
				while (sitesExplored.size() != groupSites.size())
				{
					final int site = groupSites.get(k);
					final TopologyElement siteElement = topology.getGraphElements(realType).get(site);
					final List<AbsoluteDirection> directions = dirnChoice.convertToAbsolute(realType, siteElement, null,
							null, null, context);

					for (final AbsoluteDirection direction : directions)
					{
						final List<game.util.graph.Step> steps = topology.trajectories().steps(realType,
								siteElement.index(), realType, direction);

						boolean foundNextElement = false;
						for (final game.util.graph.Step step : steps)
						{
							final int to = step.to().id();

							// If we already have it we continue to look the others.
							if (groupSites.contains(to))
								continue;

							// New element in the group.
							if (groupMinusI.contains(to))
							{
								groupSites.add(to);
								lastExploredSite = to;
								foundNextElement = true;
								break;
							}
						}
						if (foundNextElement)
							break;
					}

					sitesExplored.add(site);
					k++;
				}
			}

			final boolean oneSingleGroup = (groupSites.size() == groupMinusI.size());

			if (oneSingleGroup)
			{
				boolean isALoop = false;
				final TopologyElement siteElement = topology.getGraphElements(realType).get(lastExploredSite);
				final List<AbsoluteDirection> directions = dirnChoice.convertToAbsolute(realType, siteElement, null,
						null, null, context);

				for (final AbsoluteDirection direction : directions)
				{
					final List<game.util.graph.Step> steps = topology.trajectories().steps(realType,
							siteElement.index(), realType, direction);

					for (final game.util.graph.Step step : steps)
					{
						final int to = step.to().id();

						if (to == startGroup)
						{
							isALoop = true;
							break;
						}
					}
					if (isALoop)
						break;
				}

				if (isALoop)
					minimumGroup.remove(i);
			}
		}

		return minimumGroup;
	}
}
