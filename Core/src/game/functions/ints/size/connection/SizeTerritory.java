package game.functions.ints.size.connection;

import java.util.BitSet;
import java.util.List;

import annotations.Hide;
import annotations.Opt;
import annotations.Or;
import game.Game;
import game.functions.ints.BaseIntFunction;
import game.functions.ints.IntFunction;
import game.types.board.SiteType;
import game.types.play.RoleType;
import game.util.directions.AbsoluteDirection;
import game.util.graph.Step;
import gnu.trove.list.array.TIntArrayList;
import other.PlayersIndices;
import other.concept.Concept;
import other.context.Context;
import other.state.container.ContainerState;
import other.topology.Edge;
import other.topology.Topology;
import other.topology.TopologyElement;

/**
 * Returns the total number of sites enclosed by a specific Player.
 *
 * @author eric.piette
 */
@Hide
public final class SizeTerritory extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The index of player. **/
	private final IntFunction who;

	/** The roleType. */
	private final RoleType role;

	/** Direction of the connection. */
	private final AbsoluteDirection dirnChoice;
	
	/** Cell/Edge/Vertex. */
	private SiteType type;

	//-------------------------------------------------------------------------

	/**
	 * @param type      The graph element type [default SiteType of the board].
	 * @param role      The roleType of the player owning the components in the
	 *                  territory.
	 * @param player    The index of the player owning the components in the
	 *                  territory.
	 * @param direction The type of directions from the site to compute the group
	 *                  [Adjacent].
	 */
	public SizeTerritory
	(
			@Opt final SiteType               type,
		@Or      final RoleType               role, 
		@Or      final game.util.moves.Player player,
		    @Opt final AbsoluteDirection      direction
	)
	{
		this.dirnChoice = (direction != null) ? direction : AbsoluteDirection.Adjacent;
		this.who = (player == null) ? RoleType.toIntFunction(role) : player.index();
		this.role = role;
		this.type = type;
	}

	//-------------------------------------------------------------------------

	@Override
	public int eval(final Context context)
	{
		int sizeTerritory = 0;
		final Topology topology = context.topology();
		final ContainerState cs = context.state().containerStates()[0];
		
		// Get all possible territory sites.
		final TIntArrayList emptySites = new TIntArrayList(cs.emptyRegion(type).sites());
		
		// Code to handle specific roleType.
		final int whoId = who.eval(context);
		final TIntArrayList idPlayers = PlayersIndices.getIdPlayers(context, role, whoId);
		
		// Look if each empty site is in the territory of a player.
		final TIntArrayList sitesExplored = new TIntArrayList();
		for(int i = 0; i < emptySites.size(); i++)
		{
			final int site = emptySites.get(i);		
			if(sitesExplored.contains(site))
				continue;
			
			// Get group of empty sites from that site.
			final TIntArrayList groupSites = new TIntArrayList();
			groupSites.add(site);
			final TIntArrayList groupSitesExplored = new TIntArrayList();
			int indexGroup = 0;
			while (groupSitesExplored.size() != groupSites.size())
			{
				final int siteGroup = groupSites.get(indexGroup);
				final TopologyElement siteElement = topology.getGraphElements(type).get(siteGroup);
					final List<game.util.graph.Step> steps = topology.trajectories().steps(type, siteElement.index(),
							type, dirnChoice);
	
					for (final game.util.graph.Step step : steps)
					{
						final int to = step.to().id();
	
						// If we already have it we continue to look the others.
						if (groupSites.contains(to))
							continue;
						
						context.setTo(to);
						if (cs.isEmpty(to, type))
							groupSites.add(to);
					}

				groupSitesExplored.add(site);
				indexGroup++;
			}
			
			sitesExplored.addAll(groupSites);
			
			// Check if that group is owned by the right players.
			if(checkTerritory(groupSites,topology,dirnChoice,type,cs,idPlayers))
				sizeTerritory += groupSites.size();
		}
			
		return sizeTerritory;
	}
			
	/**
	 * 
	 * @param sites The sites.
	 * @param graph The graph.
	 * @param dirnChoice The direction.
	 * @param type The type of graph element.
	 * @param cs The container state.
	 * @param playerTerritory The indices of the player supposed to own the territory.
	 * @return True if the sites are surrounded only by the expected players.
	 */
	static final boolean checkTerritory
	(
		final TIntArrayList sites, 
		final Topology graph, 
		final AbsoluteDirection dirnChoice, 
		final SiteType type,
		final ContainerState cs,
		final TIntArrayList playerTerritory
	)
	{
			if (type.equals(SiteType.Edge))
			{
				for (int i = 0; i < sites.size();i++)
				{
					final int site = sites.get(i);
					final Edge edge = graph.edges().get(site);
					for (final Edge edgeAdj : edge.adjacent())
					{
						final int territorySite = edgeAdj.index();
						if (!sites.contains(territorySite))
						{
							final int who = cs.who(territorySite, type);
							if(!playerTerritory.contains(who))
								return false;
						}
					}
				}
			}
			else
			{
				for (int i = 0; i < sites.size();i++)
				{
					final int site = sites.get(i);
					final List<Step> steps = graph.trajectories().steps(type, site, dirnChoice);

					for (final Step step : steps)
					{
						if (step.from().siteType() != step.to().siteType())
							continue;

						final int to = step.to().id();

						if (!sites.contains(to))
						{
							final int who = cs.who(to, type);
							if(!playerTerritory.contains(who))
								return false;
						}
					}
				}
			}
			return true;
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
		long flags = who.gameFlags(game);
		flags |= SiteType.gameFlags(type);
		return flags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(who.concepts(game));
		concepts.or(SiteType.concepts(type));
		concepts.set(Concept.Territory.id(), true);
		return concepts;
	}
	
	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(who.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(who.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= who.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= who.willCrash(game);
		return willCrash;
	}

	@Override
	public void preprocess(final Game game)
	{
		who.preprocess(game);
		type = SiteType.use(type, game);
	}
}
