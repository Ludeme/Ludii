package game.functions.booleans.is.connect;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import annotations.Hide;
import annotations.Opt;
import annotations.Or;
import game.Game;
import game.equipment.other.Regions;
import game.functions.booleans.BaseBooleanFunction;
import game.functions.directions.Directions;
import game.functions.directions.DirectionsFunction;
import game.functions.ints.IntFunction;
import game.functions.region.RegionFunction;
import game.types.board.RegionTypeStatic;
import game.types.board.SiteType;
import game.types.play.RoleType;
import game.util.directions.AbsoluteDirection;
import game.util.directions.Direction;
import gnu.trove.list.array.TIntArrayList;
import main.Constants;
import other.context.Context;
import other.state.container.ContainerState;
import other.topology.Topology;
import other.topology.TopologyElement;
import other.trial.Trial;

/**
 * Detects whether regions cannot possibly be connected by a player.
 *
 * @author Eric.Piette and Dennis Soemers and cambolbro
 */
@Hide
public final class IsBlocked extends BaseBooleanFunction
{
	private static final long serialVersionUID = 1L;
	
	// -------------------------------------------------------------------------

	/** the regions to connect */
	private final RegionFunction[] regionsToConnectFn;
	
	/** The owner of the regions */
	private final IntFunction roleFunc;
	
	/** The different regions of the regionType. */
	private final Regions staticRegions;
	
	/** The minimum number of set need to connect */
	private final IntFunction number;

	/** Cell/Edge/Vertex. */
	private SiteType type;

	/** Direction chosen. */
	private final DirectionsFunction dirnChoice;

	// -------------------------Pre-computation------------------------------------------

	/** The pre-computed regions to connect. */
	private List<TIntArrayList> precomputedSitesRegions;

	/** The pre-computed owned regions. */
	private List<List<TIntArrayList>> precomputedOwnedRegions;

	/**
	 * Precomputed lists of BitSets (only used in case where role != null currently)
	 * One list of BitSets per player index
	 */
	// private List<List<BitSet>> precomputedRegionsBitSets = null;

	//-------------------------------------------------------------------------
	
	/**
	 * @param type       The graph element type [default SiteType of the board].
	 * @param number     The minimum number of set need to connect [number of owned
	 *                   regions if not specified].
	 * @param directions The directions of the connected pieces used to connect the
	 *                   region [Adjacent].
	 * @param regions    The disjointed regions set, which need to use for
	 *                   connection.
	 * @param role       The role of the player.
	 * @param regionType Type of the regions.
	 */
	public IsBlocked
	(			
			@Opt final SiteType         type,
			@Opt final IntFunction      number,
			@Opt final Direction        directions,
		@Or	 	 final RegionFunction[] regions,
		@Or  	 final RoleType         role,
		@Or  	 final RegionTypeStatic regionType			 
	)
	{
		this.number = number;
		this.regionsToConnectFn = regions;
		roleFunc         = (role == null) ? null : RoleType.toIntFunction(role);
		this.staticRegions = (regionType == null) 
						   ? null 
						   : new game.equipment.other.Regions(null, null, null, null, null, regionType, null, null);
		this.type = type;
		this.dirnChoice = (directions != null) ? directions.directionsFunctions()
				: new Directions(AbsoluteDirection.Adjacent, null);
	}
	
	//-------------------------------------------------------------------------

	@Override
	public boolean eval(final Context context)
	{
		final Game game = context.game();
		final Topology topology = context.topology();
		final SiteType realType = (type != null) ? type : context.game().board().defaultSite();

		// Check if the site is in the board.
		final ContainerState cs = context.containerState(0);
		final int who = (roleFunc != null) ? roleFunc.eval(context) : context.state().mover();

		// We get the regions to connect.
		final int playerRegion = (roleFunc == null) ? Constants.UNDEFINED : roleFunc.eval(context);
		List<TIntArrayList> sitesRegions;
		if (precomputedSitesRegions == null)
		{
			final RegionFunction[] regionsToConnect = regionsToConnectFn;
			sitesRegions = new ArrayList<TIntArrayList>();
			if (regionsToConnect != null)
			{
				for (final RegionFunction regionToConnect : regionsToConnect)
					sitesRegions.add(new TIntArrayList(regionToConnect.eval(context).sites()));
			}
			else
			{
				if (staticRegions != null)
				{
					// Conversion of the static region.
					final Integer[][] regionSets = staticRegions.convertStaticRegionOnLocs(staticRegions.regionTypes()[0], context);
					for (final Integer[] region : regionSets)
					{
						final TIntArrayList regionToAdd = new TIntArrayList();
						for (final Integer site : region)
							regionToAdd.add(site.intValue());
						if (regionToAdd.size() > 0)
							sitesRegions.add(regionToAdd);
					}
				}
				else
				{
					if (precomputedOwnedRegions != null)
					{
						for (final TIntArrayList preComputedRegions : precomputedOwnedRegions.get(playerRegion))
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
										sitesRegions.add(new TIntArrayList(r.eval(context).sites()));
								}
								else
								{
									final TIntArrayList bitSet = new TIntArrayList();
									for (final int site : region.sites())
										bitSet.add(site);
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
			sitesRegions = new ArrayList<TIntArrayList>(precomputedSitesRegions);
		}
		
		final int numRegionToConnect = (number != null) ? number.eval(context) : sitesRegions.size();
		final TIntArrayList originalRegion = sitesRegions.get(0);

		for (int i = 0; i < originalRegion.size(); i++)
		{
			final ArrayList<TIntArrayList> othersRegionToConnect = new ArrayList<TIntArrayList>(sitesRegions);

			// We remove the region from when we start to look
			othersRegionToConnect.remove(0);

			final int from = originalRegion.get(i);

			// We get the group of sites connected from the location owned by the owner or
			// empty.
			final TIntArrayList groupSites = new TIntArrayList();

			if (cs.who(from, realType) == who || cs.what(from, realType) == 0)
				groupSites.add(from);

			// Counter of connected regions.
			int numRegionConnected = 0;

			// Already one region connected.
			numRegionConnected++;

			if (numRegionConnected == numRegionToConnect)
				return false;

			if (groupSites.size() > 0)
			{
				final TIntArrayList sitesExplored = new TIntArrayList();

				int j = 0;
				while (sitesExplored.size() != groupSites.size())
				{
					final int site = groupSites.get(j);
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
							if (who == cs.who(to, realType) || cs.what(to, realType) == 0)
							{
								groupSites.add(to);
								// Check if that element is in one of the regions to connect.
								for (int k = othersRegionToConnect.size() - 1; k >= 0; k--)
								{
									final TIntArrayList regionToConnect = othersRegionToConnect.get(k);
									if (regionToConnect.contains(to))
									{
										numRegionConnected++;

										// Region is connected we remove it.
										othersRegionToConnect.remove(k);
									}

									// If enough regions connected we return false.
									if (numRegionConnected == numRegionToConnect)
										return false;
								}
							}
						}
					}

					sitesExplored.add(site);
					j++;
				}
			}
		}
		
		return true;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		String str = "";
		str += "IsBlocked";
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
		long flags = 0l;

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
		concepts.or(SiteType.concepts(type));

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
				precomputedSitesRegions = new ArrayList<TIntArrayList>();
				for (final RegionFunction regionToConnect : regionsToConnectFn)
					if (regionToConnect != null)
					{
						precomputedSitesRegions.add(
								new TIntArrayList(regionToConnect.eval(new Context(game, new Trial(game))).sites()));
					}
			}
		}
		else
		{
			// Look the static regions used in entry.
			if (staticRegions != null)
			{
				precomputedSitesRegions = new ArrayList<TIntArrayList>();
				final Integer[][] regionSets = staticRegions.convertStaticRegionOnLocs(staticRegions.regionTypes()[0],
						new Context(game, new Trial(game)));
				for (final Integer[] region : regionSets)
				{
					final TIntArrayList regionToAdd = new TIntArrayList();
					for (final Integer site : region)
						regionToAdd.add(site.intValue());
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
						precomputedOwnedRegions = new ArrayList<List<TIntArrayList>>();

						for (int i = 0; i < game.players().size(); i++)
							precomputedOwnedRegions.add(new ArrayList<TIntArrayList>());

						for (final Regions region : game.equipment().regions())
						{
							if (region.region() != null)
							{
								for (final RegionFunction r : region.region())
								{
									final TIntArrayList sitesToConnect = new TIntArrayList(
											r.eval(new Context(game, new Trial(game))).sites());
									precomputedOwnedRegions.get(region.owner()).add(sitesToConnect);
								}
							}
							else
							{
								final TIntArrayList sitesToConnect = new TIntArrayList();
								for (final int site : region.sites())
									sitesToConnect.add(site);
								precomputedOwnedRegions.get(region.owner()).add(sitesToConnect);
							}
						}
					}
				}
			}
		}
	}	
}