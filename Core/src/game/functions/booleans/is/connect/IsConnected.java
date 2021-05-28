package game.functions.booleans.is.connect;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import annotations.Or;
import game.Game;
import game.equipment.other.Regions;
import game.functions.booleans.BaseBooleanFunction;
import game.functions.directions.Directions;
import game.functions.directions.DirectionsFunction;
import game.functions.ints.IntFunction;
import game.functions.ints.last.LastTo;
import game.functions.region.RegionFunction;
import game.types.board.RegionTypeStatic;
import game.types.board.SiteType;
import game.types.play.RoleType;
import game.util.directions.AbsoluteDirection;
import game.util.directions.Direction;
import gnu.trove.list.array.TIntArrayList;
import main.Constants;
import main.collections.ChunkSet;
import other.concept.Concept;
import other.context.Context;
import other.location.FullLocation;
import other.location.Location;
import other.state.container.ContainerState;
import other.topology.Topology;
import other.topology.TopologyElement;
import other.trial.Trial;

/**
 * Is used to detect if regions are connected by a group of pieces.
 *
 * @author Eric.Piette and tahmina (for UF version)
 */
@Hide
public final class IsConnected extends BaseBooleanFunction
{
	private static final long serialVersionUID = 1L;
	
	//-------------------------------------------------------------------------

	/** The regions to connect. */
	private final RegionFunction[] regionsToConnectFn;
	
	/** The owner of the regions. */
	private final IntFunction roleFunc;
	
	/** The different regions of the regionType. */
	private final Regions staticRegions;
	
	/** The minimum number of regions to connect */
	private final IntFunction number;

	/** The SiteType of the sites to look. */
	private SiteType type;

	/** The starting location of the group of pieces. */
	private final IntFunction startLocationFn;

	/** Direction chosen. */
	private final DirectionsFunction dirnChoice;

	//-------------------------Pre-computation------------------------------------------

	/** The pre-computed regions to connect. */
	private List<ChunkSet> precomputedSitesRegions;

	/** The pre-computed owned regions. */
	private List<List<ChunkSet>> precomputedOwnedRegions;

	//-------------------------------------------------------------------------

	/**
	 * @param number     The minimum number of regions to connect [0].
	 * @param type       The graph element type [default SiteType of the board].
	 * @param at         The specific starting position need to connect.
	 * @param directions The directions of the connected pieces used to connect the
	 *                   region [Adjacent].
	 * @param regions    The disjoint regions set, which need to use for connection.
	 * @param role       Role of the player.
	 * @param regionType Type of the regions.
	 */
	public IsConnected
	(			
			@Opt       final IntFunction      number,
			@Opt       final SiteType         type,
			@Opt @Name final IntFunction      at,
			@Opt       final Direction        directions,
		@Or	       	   final RegionFunction[] regions,
		@Or            final RoleType         role,
		@Or            final RegionTypeStatic regionType			 
	)
	{
		int numNonNull = 0;
		
		if (regions != null)		numNonNull++;
		if (role != null)			numNonNull++;
		if (regionType != null)		numNonNull++;		
		if (numNonNull != 1)		throw new IllegalArgumentException("Exactly one Or parameter must be non-null.");

		this.startLocationFn = at == null ? new LastTo(null) : at;
		this.regionsToConnectFn = regions;
		this.roleFunc = (role == null) ? null : RoleType.toIntFunction(role);
		this.staticRegions = (regionType == null) ? null : new game.equipment.other.Regions(null, null, null, null, null, regionType, null, null);
		this.type = type;
		this.number = number;
		this.dirnChoice = (directions != null) ? directions.directionsFunctions()
				: new Directions(AbsoluteDirection.Adjacent, null);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public boolean eval(final Context context)
	{	
		final Game game = context.game();
		final int from = startLocationFn.eval(context);

		// Check if this is a site.
		if (from < 0)
			return false;

		final Topology topology = context.topology();
		final SiteType realType = (type != null) ? type : context.game().board().defaultSite();

		// Check if the site is in the board.
		final List<? extends TopologyElement> realTypeElements = topology.getGraphElements(realType);
		if (from >= realTypeElements.size())
			return false;
		final ContainerState cs = context.containerState(0);
		final int who = cs.who(from, realType);

		// Check if the site is not empty.
		if (who <= 0)
			return false;

		// We get the regions to connect.
		final int playerRegion = (roleFunc == null) ? Constants.UNDEFINED : roleFunc.eval(context);
		List<ChunkSet> sitesRegions;
		if (precomputedSitesRegions == null)
		{
			final RegionFunction[] regionsToConnect = regionsToConnectFn;
			sitesRegions = new ArrayList<ChunkSet>();
			if (regionsToConnect != null)
			{
				for (final RegionFunction regionToConnect : regionsToConnect)
					sitesRegions.add(regionToConnect.eval(context).bitSet());
			}
			else
			{
				if (staticRegions != null)
				{
					// Conversion of the static region.
					final Integer[][] regionSets = staticRegions
							.convertStaticRegionOnLocs(staticRegions.regionTypes()[0], context);
					for (final Integer[] region : regionSets)
					{
						final ChunkSet regionToAdd = new ChunkSet();
						for (final Integer site : region)
							regionToAdd.set(site.intValue());
						if (regionToAdd.size() > 0)
							sitesRegions.add(regionToAdd);
					}
				}
				else
				{
					if (precomputedOwnedRegions != null)
					{
						for (final ChunkSet preComputedRegions : precomputedOwnedRegions.get(playerRegion))
							sitesRegions.add(preComputedRegions);
					}
					else
					{
						// Get the regions to connect.
						for (final Regions region : game.equipment().regions())
						{
							if (region.owner() == playerRegion)
							{
								if (region.region() != null)
								{
									for (final RegionFunction r : region.region())
										sitesRegions.add(r.eval(context).bitSet());
								}
								else
								{
									final ChunkSet bitSet = new ChunkSet();
									for (final int site : region.sites())
										bitSet.set(site);
									sitesRegions.add(bitSet);
								}
							}
						}
					}
				}
			}
		}
		else // Already precomputed.
		{
			sitesRegions = new ArrayList<ChunkSet>(precomputedSitesRegions);
		}

		final int numRegionToConnect = (number != null) ? number.eval(context) : sitesRegions.size();
		// We get the group of sites connected from the location.
		final TIntArrayList groupSites = new TIntArrayList();

		if (cs.who(from, realType) == playerRegion || playerRegion == Constants.UNDEFINED)
			groupSites.add(from);

		// Counter of connected regions.
		int numRegionConnected = 0;

		// Check if the origin of the connected group is in one of the regions to
		// connect.
		for (int j = sitesRegions.size() - 1; j >= 0; j--)
		{
			final ChunkSet regionToConnect = sitesRegions.get(j);
			if (regionToConnect.get(from))
			{
				numRegionConnected++;

				// Region is connected we remove it.
				sitesRegions.remove(j);
			}

			if (numRegionConnected == numRegionToConnect)
				return true;
		}

		if (groupSites.size() > 0)
		{
			final boolean[] inGroupSites = new boolean[realTypeElements.size()];
			inGroupSites[from] = true;
			
			int i = 0;
			while (i < groupSites.size())
			{
				final int site = groupSites.getQuick(i);
				final TopologyElement siteElement = realTypeElements.get(site);
				final List<AbsoluteDirection> directions = dirnChoice.convertToAbsolute(realType, siteElement, null,
						null,
						null, context);

				for (final AbsoluteDirection direction : directions)
				{
					final List<game.util.graph.Step> steps = topology.trajectories().steps(realType,
							siteElement.index(), realType, direction);

					for (final game.util.graph.Step step : steps)
					{
						final int to = step.to().id();

						// If we already have it we continue to look the others.
						if (inGroupSites[to])
							continue;

						// New element in the group.
						if (who == cs.who(to, realType))
						{
							// Check if that element is in one of the regions to connect.
							for (int j = sitesRegions.size() - 1; j >= 0; j--)
							{
								final ChunkSet regionToConnect = sitesRegions.get(j);
								if (regionToConnect.get(to))
								{
									numRegionConnected++;

									// Region is connected we remove it.
									sitesRegions.remove(j);
								}

								// If enough regions connected we return true.
								if (numRegionConnected == numRegionToConnect)
									return true;
							}
							
							groupSites.add(to);
							inGroupSites[to] = true;
						}
					}
				}

				i++;
			}
		}

		return false;
	}

	//------------------------------------------------------------------------

	@Override
	public String toString()
	{
		String str = "";
		str += "IsConnected (" + regionsToConnectFn + ")";
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
		long flags = 0L;
		
		if (regionsToConnectFn != null)
		{
			for (final RegionFunction regionFunc : regionsToConnectFn)
			{
				if (regionFunc != null)
					flags |= regionFunc.gameFlags(game);
			}
		}		

		flags |= SiteType.gameFlags(type);

		if (number != null)
			flags |= number.gameFlags(game);

		return flags;
	}
	
	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(super.concepts(game));
		concepts.or(SiteType.concepts(type));
		concepts.set(Concept.Connection.id(), true);

		if (dirnChoice != null)
			concepts.or(dirnChoice.concepts(game));

		if (regionsToConnectFn != null)
		{
			for (final RegionFunction regionFunc : regionsToConnectFn)
			{
				if (regionFunc != null)
					concepts.or(regionFunc.concepts(game));
			}
		}

		if (number != null)
			concepts.or(number.concepts(game));

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();

		if (dirnChoice != null)
			writeEvalContext.or(dirnChoice.writesEvalContextRecursive());

		if (regionsToConnectFn != null)
		{
			for (final RegionFunction regionFunc : regionsToConnectFn)
			{
				if (regionFunc != null)
					writeEvalContext.or(regionFunc.writesEvalContextRecursive());
			}
		}

		if (number != null)
			writeEvalContext.or(number.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();

		if (dirnChoice != null)
			readEvalContext.or(dirnChoice.readsEvalContextRecursive());

		if (regionsToConnectFn != null)
		{
			for (final RegionFunction regionFunc : regionsToConnectFn)
			{
				if (regionFunc != null)
					readEvalContext.or(regionFunc.readsEvalContextRecursive());
			}
		}

		if (number != null)
			readEvalContext.or(number.readsEvalContextRecursive());

		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		if (regionsToConnectFn != null)
		{
			for (final RegionFunction regionFunc : regionsToConnectFn)
			{
				if (regionFunc != null)
					missingRequirement |= regionFunc.missingRequirement(game);
			}
		}

		if (number != null)
			missingRequirement |= number.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		if (regionsToConnectFn != null)
		{
			for (final RegionFunction regionFunc : regionsToConnectFn)
			{
				if (regionFunc != null)
					willCrash |= regionFunc.willCrash(game);
			}
		}

		if (number != null)
			willCrash |= number.willCrash(game);
		return willCrash;
	}

	@Override
	public void preprocess(final Game game)
	{
		type = SiteType.use(type, game);
		
		if (regionsToConnectFn != null)
		{
			for (final RegionFunction regionFunc : regionsToConnectFn)
			{
				if (regionFunc != null)
					regionFunc.preprocess(game);
			}
		}

		if (number != null)
			number.preprocess(game);

		// Precomputation of the regions to connect if they are static.
		// Look the array of regions to connect.
		if (regionsToConnectFn != null)
		{
			boolean allStatic = true;
			for (final RegionFunction regionFunc : regionsToConnectFn)
			{
				if (regionFunc != null && !regionFunc.isStatic())
				{
					allStatic = false;
					break;
				}
			}

			if (allStatic)
			{
				precomputedSitesRegions = new ArrayList<ChunkSet>();
				for (final RegionFunction regionToConnect : regionsToConnectFn)
					if (regionToConnect != null)
					{
						precomputedSitesRegions.add(
								regionToConnect.eval(new Context(game, new Trial(game))).bitSet());
					}
			}
		}
		else
		{
			// Look the static regions used in entry.
			if (staticRegions != null)
			{
				precomputedSitesRegions = new ArrayList<ChunkSet>();
				final Integer[][] regionSets = staticRegions.convertStaticRegionOnLocs(staticRegions.regionTypes()[0],
						new Context(game, new Trial(game)));
				for (final Integer[] region : regionSets)
				{
					final ChunkSet regionToAdd = new ChunkSet();
					for (final Integer site : region)
						regionToAdd.set(site.intValue());
					if (regionToAdd.size() > 0)
						precomputedSitesRegions.add(regionToAdd);
				}
			}
			// Look the regions owned.
			else
			{
				if (roleFunc != null)
				{
					boolean allStatic = true;
					for (final Regions region : game.equipment().regions())
					{
						if (!region.isStatic())
						{
							allStatic = false;
							break;
						}
					}

					if (allStatic)
					{
						precomputedOwnedRegions = new ArrayList<List<ChunkSet>>();

						for (int i = 0; i < game.players().size(); i++)
							precomputedOwnedRegions.add(new ArrayList<ChunkSet>());

						for (final Regions region : game.equipment().regions())
						{
							if (region.region() != null)
							{
								for (final RegionFunction r : region.region())
								{
									final ChunkSet sitesToConnect = 
											r.eval(new Context(game, new Trial(game))).bitSet();
									precomputedOwnedRegions.get(region.owner()).add(sitesToConnect);
								}
							}
							else
							{
								final ChunkSet sitesToConnect = new ChunkSet();
								for (final int site : region.sites())
									sitesToConnect.set(site);
								precomputedOwnedRegions.get(region.owner()).add(sitesToConnect);
							}
						}
					}
				}
			}
		}
	}

	// ----------------------Visualisation for the GUI---------------------------------------

	@Override
	public List<Location> satisfyingSites(final Context context)
	{
		if (!eval(context))
			return new ArrayList<Location>();

		final List<Location> winningSites = new ArrayList<Location>();

		final Game game = context.game();

		final int from = (startLocationFn == null) ? context.trial().lastMove().toNonDecision()
				: startLocationFn.eval(context);

		// Check if this is a site.
		if (from < 0)
			return new ArrayList<Location>();

		final Topology topology = context.topology();
		final SiteType realType = (type != null) ? type : context.game().board().defaultSite();

		// Check if the site is in the board.
		if (from >= topology.getGraphElements(realType).size())
			return new ArrayList<Location>();
		final ContainerState cs = context.containerState(0);
		final int who = cs.who(from, type);

		// Check if the site is not empty.
		if (who <= 0)
			return new ArrayList<Location>();

		// We get the regions to connect.
		final int playerRegion = (roleFunc == null) ? Constants.UNDEFINED : roleFunc.eval(context);
		List<ChunkSet> sitesRegions;
		if (precomputedSitesRegions == null)
		{
			final RegionFunction[] regionsToConnect = regionsToConnectFn;
			sitesRegions = new ArrayList<ChunkSet>();
			if (regionsToConnect != null)
			{
				for (final RegionFunction regionToConnect : regionsToConnect)
					sitesRegions.add(regionToConnect.eval(context).bitSet());
			}
			else
			{
				if (staticRegions != null)
				{
					// Conversion of the static region.
					final Integer[][] regionSets = staticRegions
							.convertStaticRegionOnLocs(staticRegions.regionTypes()[0], context);
					for (final Integer[] region : regionSets)
					{
						final ChunkSet regionToAdd = new ChunkSet();
						for (final Integer site : region)
							regionToAdd.set(site.intValue());
						if (regionToAdd.size() > 0)
							sitesRegions.add(regionToAdd);
					}
				}
				else
				{
					if (precomputedOwnedRegions != null)
					{
						for (final ChunkSet preComputedRegions : precomputedOwnedRegions.get(playerRegion))
							sitesRegions.add(preComputedRegions);
					}
					else
					{
						// Get the regions to connect.
						for (final Regions region : game.equipment().regions())
						{
							if (region.owner() == playerRegion)
							{
								if (region.region() != null)
								{
									for (final RegionFunction r : region.region())
										sitesRegions.add(r.eval(context).bitSet());
								}
								else
								{
									final ChunkSet bitSet = new ChunkSet();
									for (final int site : region.sites())
										bitSet.set(site);
									sitesRegions.add(bitSet);
								}
							}
						}
					}
				}
			}
		}
		else // Already precomputed.
		{
			sitesRegions = new ArrayList<ChunkSet>(precomputedSitesRegions);
		}

		final int numRegionToConnect = (number != null) ? number.eval(context) : sitesRegions.size();
		// We get the group of sites connected from the location.
		final TIntArrayList groupSites = new TIntArrayList();

		if (cs.who(from, realType) == playerRegion || playerRegion == Constants.UNDEFINED)
			groupSites.add(from);

		winningSites.add(new FullLocation(from, 0, realType));

		// Counter of connected regions.
		int numRegionConnected = 0;

		// Check if the origin of the connected group is in one of the regions to
		// connect.
		for (int j = sitesRegions.size() - 1; j >= 0; j--)
		{
			final ChunkSet regionToConnect = sitesRegions.get(j);
			if (regionToConnect.get(from))
			{
				numRegionConnected++;

				// Region is connected we remove it.
				sitesRegions.remove(j);
			}

			if (numRegionConnected == numRegionToConnect)
				return filterWinningSites(context, winningSites);
		}

		if (groupSites.size() > 0)
		{
			final TIntArrayList sitesExplored = new TIntArrayList();

			int i = 0;
			while (sitesExplored.size() != groupSites.size())
			{
				final int site = groupSites.get(i);
				final TopologyElement siteElement = topology.getGraphElements(realType).get(site);
				final List<AbsoluteDirection> directions = dirnChoice.convertToAbsolute(realType, siteElement, null,
						null,
						null, context);

				for (final AbsoluteDirection direction : directions)
				{
					final List<game.util.graph.Step> steps = topology.trajectories().steps(realType,
							siteElement.index(), realType, direction);

					for (final game.util.graph.Step step : steps)
					{
						final int to = step.to().id();

						// If we already have it we continue to look the others.
						if (groupSites.contains(to))
							continue;

						// New element in the group.
						if (who == cs.who(to, realType))
						{
							groupSites.add(to);
							winningSites.add(new FullLocation(to, 0, realType));
							// Check if that element is in one of the regions to connect.
							for (int j = sitesRegions.size() - 1; j >= 0; j--)
							{
								final ChunkSet regionToConnect = sitesRegions.get(j);
								if (regionToConnect.get(to))
								{
									numRegionConnected++;

									// Region is connected we remove it.
									sitesRegions.remove(j);
								}

								// If enough regions connected we return true.
								if (numRegionConnected == numRegionToConnect)
									return filterWinningSites(context, winningSites);
							}
						}
					}
				}

				sitesExplored.add(site);
				i++;
			}
		}

		return new ArrayList<Location>();
	}

	/**
	 * @param context      the context.
	 * @param winningGroup The winning group detected in satisfyingSites.
	 * @return The minimum group of sites to connect the regions.
	 */
	public List<Location> filterWinningSites(final Context context, final List<Location> winningGroup)
	{
		final Game game = context.game();
		final Topology topology = context.topology();
		final SiteType realType = (type != null) ? type : context.game().board().defaultSite();
		// We get the regions to connect.
		final int playerRegion = (roleFunc == null) ? Constants.UNDEFINED : roleFunc.eval(context);
		List<ChunkSet> sitesRegions;
		if (precomputedSitesRegions == null)
		{
			final RegionFunction[] regionsToConnect = regionsToConnectFn;
			sitesRegions = new ArrayList<ChunkSet>();
			if (regionsToConnect != null)
			{
				for (final RegionFunction regionToConnect : regionsToConnect)
					sitesRegions.add(regionToConnect.eval(context).bitSet());
			}
			else
			{
				if (staticRegions != null)
				{
					// Conversion of the static region.
					final Integer[][] regionSets = staticRegions
							.convertStaticRegionOnLocs(staticRegions.regionTypes()[0], context);
					for (final Integer[] region : regionSets)
					{
						final ChunkSet regionToAdd = new ChunkSet();
						for (final Integer site : region)
							regionToAdd.set(site.intValue());
						if (regionToAdd.size() > 0)
							sitesRegions.add(regionToAdd);
					}
				}
				else
				{
					if (precomputedOwnedRegions != null)
					{
						for (final ChunkSet preComputedRegions : precomputedOwnedRegions.get(playerRegion))
							sitesRegions.add(preComputedRegions);
					}
					else
					{
						// Get the regions to connect.
						for (final Regions region : game.equipment().regions())
						{
							if (region.owner() == playerRegion)
							{
								if (region.region() != null)
								{
									for (final RegionFunction r : region.region())
										sitesRegions.add(r.eval(context).bitSet());
								}
								else
								{
									final ChunkSet bitSet = new ChunkSet();
									for (final int site : region.sites())
										bitSet.set(site);
									sitesRegions.add(bitSet);
								}
							}
						}
					}
				}
			}
		}
		else // Already precomputed.
		{
			sitesRegions = new ArrayList<ChunkSet>(precomputedSitesRegions);
		}
		final int numRegionToConnect = (number != null) ? number.eval(context) : sitesRegions.size();
		
		// Minimum group to connect the regions.
		final List<Location> minimumGroup = new ArrayList<Location>(winningGroup);

		for (int i = minimumGroup.size() - 1; i >= 0; i--)
		{
			final TIntArrayList groupMinusI = new TIntArrayList();
			for (int j = 0; j < minimumGroup.size(); j++)
				if(j != i)
					groupMinusI.add(minimumGroup.get(j).site());

			// System.out.println("groupMinusI is" + groupMinusI);

			// Check if all the pieces are in one group.

			if (groupMinusI.isEmpty())
				continue;

			final int startGroup = groupMinusI.get(0);
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

						for (final game.util.graph.Step step : steps)
						{
							final int to = step.to().id();

							// If we already have it we continue to look the others.
							if (groupSites.contains(to))
								continue;

							// New element in the group.
							if (groupMinusI.contains(to))
								groupSites.add(to);
						}
					}

					sitesExplored.add(site);
					k++;
				}
			}
			
			final boolean oneSingleGroup = (groupSites.size() == groupMinusI.size());

			// Check if the connected regions are still connected.
			// Counter of connected regions.
			int numRegionConnected = 0;

			// Check if that element is in one of the regions to connect.
			for (int j = sitesRegions.size() - 1; j >= 0; j--)
			{
				final ChunkSet regionToConnect = sitesRegions.get(j);
				for (int siteToCheck = regionToConnect.nextSetBit(0); siteToCheck >= 0; siteToCheck = regionToConnect.nextSetBit(siteToCheck + 1))
				{
					if (groupMinusI.contains(siteToCheck))
					{
						numRegionConnected++;
						break;
					}
				}

				// If enough regions connected we return true.
				if (numRegionConnected == numRegionToConnect)
				{
					break;
				}
			}

			if (oneSingleGroup && numRegionConnected == numRegionToConnect)
				minimumGroup.remove(i);
		}

		return minimumGroup;
	}
}

