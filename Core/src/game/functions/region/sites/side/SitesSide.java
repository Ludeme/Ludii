package game.functions.region.sites.side;

import java.util.BitSet;
import java.util.List;

import annotations.Hide;
import annotations.Opt;
import annotations.Or;
import game.Game;
import game.functions.ints.IntFunction;
import game.functions.region.BaseRegionFunction;
import game.players.Player;
import game.types.board.SiteType;
import game.types.play.RoleType;
import game.util.directions.CompassDirection;
import game.util.directions.DirectionFacing;
import game.util.equipment.Region;
import gnu.trove.list.array.TIntArrayList;
import other.context.Context;
import other.topology.TopologyElement;

/**
 * Returns all the sites in a specific side of the board.
 * 
 * @author Eric.Piette
 */
@Hide
public final class SitesSide extends BaseRegionFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** If we can, we'll precompute once and cache. */
	private Region precomputedRegion = null;

	//-------------------------------------------------------------------------

	/** The index of the side. */
	private final IntFunction index;

	/** The role. */
	private final RoleType role;

	//-------------------------------------------------------------------------

	/**
	 * The direction of the side.
	 */
	private final DirectionFacing direction;

	/**
	 * @param elementType Type of graph elements to return [Cell (or Vertex if the
	 *                    main board uses intersections)].
	 * @param player      Index of the side.
	 * @param role        The Role type corresponding to the index.
	 * @param direction   Direction of the side to return.
	 */
	public SitesSide
	(
		    @Opt final SiteType               elementType, 
		@Or @Opt final game.util.moves.Player player,
		@Or @Opt final RoleType	              role,
		@Or @Opt final CompassDirection       direction
	)
	{
		type = elementType;
		this.direction = direction;
		index = (role != null) ? RoleType.toIntFunction(role) : (player != null) ? player.index() : null;
		this.role = role;
	}

	//-------------------------------------------------------------------------

	@Override
	public Region eval(final Context context)
	{
		if (precomputedRegion != null)
			return precomputedRegion;

		final boolean useCells = type != null && type.equals(SiteType.Cell)
				|| type == null && context.game().board().defaultSite() != SiteType.Vertex;

		final other.topology.Topology graph = context.topology();

		if (role != null && role == RoleType.Shared)
			return new Region(useCells ? graph.outer(SiteType.Cell) : graph.outer(SiteType.Vertex));

		DirectionFacing dirn = direction;
		if (dirn == null && index != null)
		{
			// Get direction from (possibly dynamic) player index
			final int pid = index.eval(context);
			if (pid < 1 || pid > context.game().players().count())
			{
				System.out.println("** Bad player index.");
				return new Region();
			}
			final Player player = context.game().players().players().get(pid);
			dirn = player.direction();
		}

		if (dirn == null)
			return new Region();

		final TIntArrayList sites = new TIntArrayList();
		if (useCells)
		{
			final List<TopologyElement> side = graph.sides(SiteType.Cell).get(dirn);
			for (final TopologyElement v : side)
				sites.add(v.index());
		}
		else
		{
			final List<TopologyElement> side = graph.sides(SiteType.Vertex).get(dirn);
			for (final other.topology.TopologyElement v : side)
				sites.add(v.index());
		}
		return new Region(sites.toArray());
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		if (index != null)
			return index.isStatic();
		return true;
	}

	@Override
	public String toString()
	{
		return "Side()";
	}

	@Override
	public long gameFlags(final Game game)
	{
		long flags = 0L;

		if (index != null)
			flags = index.gameFlags(game);

		flags |= SiteType.gameFlags(type);

		return flags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(SiteType.concepts(type));
		if (index != null)
			concepts.or(index.concepts(game));
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		if (index != null)
			writeEvalContext.or(index.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		if (index != null)
			readEvalContext.or(index.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		if (index != null)
			missingRequirement |= index.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		if (index != null)
			willCrash |= index.willCrash(game);
		return willCrash;
	}

	@Override
	public void preprocess(final Game game)
	{
		type = SiteType.use(type, game);
		if (index != null)
			index.preprocess(game);

		if (isStatic())
		{
			if (type.equals(SiteType.Cell))
				precomputedRegion = new Region(
						game.equipment().containers()[0].topology().sides(SiteType.Cell)
								.get(direction));
			else
				precomputedRegion = new Region(
						game.equipment().containers()[0].topology().sides(SiteType.Vertex)
								.get(direction));
		}
	}
	
	//-------------------------------------------------------------------------

	@Override
	public String toEnglish(Game game) 
	{
		return "the " + direction.toEnglish(game) + " side";
	}
	
	//-------------------------------------------------------------------------
		
}
