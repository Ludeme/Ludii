package game.functions.ints.count.component;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import annotations.Or;
import game.Game;
import game.functions.ints.BaseIntFunction;
import game.functions.ints.IntFunction;
import game.functions.ints.board.Id;
import game.functions.region.RegionFunction;
import game.types.board.SiteType;
import game.types.play.RoleType;
import game.types.state.GameType;
import gnu.trove.list.array.TIntArrayList;
import other.context.Context;
import other.location.Location;
import other.state.container.ContainerState;

/**
 * Returns the number of pieces of a player.
 * 
 * @author Eric.Piette
 */
@Hide
public final class CountPieces extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Cell/Edge/Vertex. */
	private SiteType type;

	/** The index of the player. */
	private final IntFunction whoFn;

	/** The name of the item (Container or Component) to count. */
	private final String name;

	/** The RoleType of the player. */
	private final RoleType role;
	
	/** The region to count the pieces. */
	private final RegionFunction whereFn;

	/**
	 * 
	 * @param type The graph element type [default SiteType of the board].
	 * @param role The role of the player [All].
	 * @param of   The index of the player.
	 * @param name The name of the piece to count only these pieces.
	 * @param in   The region where to count the pieces.
	 */
	public CountPieces
	(
		@Opt           final SiteType        type,
		@Opt @Or       final RoleType        role,
		@Opt @Or @Name final IntFunction     of,
		@Opt           final String          name,
		@Opt     @Name final RegionFunction  in
	)
	{
		this.type = type;
		this.whoFn = (of != null) ? of : (role != null) ? RoleType.toIntFunction(role) : new Id(null, RoleType.Shared);
		this.name = name;
		this.role = role;
		this.whereFn = in;
	}

	//-------------------------------------------------------------------------

	@Override
	public int eval(final Context context)
	{
		if (name != null && name.equals("Bag"))
			return context.state().remainingDominoes().size();

		int count = 0;
		if (role == RoleType.All)
		{
			final List<Location> locsOwned = new ArrayList<Location>();

			for (int pid = 1; pid <= context.game().players().count(); pid++)
			{
				final List<? extends Location>[] owned = context.state().owned().positions(pid);
				for (int i = 0; i < owned.length; i++)
				{
					final List<? extends Location> locations = owned[i];
					for (int j = 0; j < locations.size(); j++)
						locsOwned.add(locations.get(j));
				}
			}

			for (int i = 0; i < locsOwned.size(); i++)
			{
				final int site = locsOwned.get(i).site();
				final SiteType typeSite = locsOwned.get(i).siteType();
				final ContainerState cs = context.containerState(context.containerId()[site]);
				count += cs.count(site, typeSite);
			}

			return count;
		}

		List<? extends Location>[] sitesOwned = null;
		final int whoId = whoFn.eval(context);

		if (whereFn != null)
		{
			final TIntArrayList whereSites = new TIntArrayList(whereFn.eval(context).sites());
			for (int pid = 0; pid <= context.players().size(); pid++)
				if (pid == whoId)
				{
					sitesOwned = context.state().owned().positions(pid);
					break;
				}
			
			if (sitesOwned != null)
				for (final List<? extends Location> locs : sitesOwned)
					for (int i = 0; i < locs.size(); i++)
						if(whereSites.contains(locs.get(i).site()) && locs.get(i).siteType().equals(type))
							count++;

			return count;
		}

		if (name != null)
		{
			final ContainerState cs = context.containerState(0); // board
			final int sitesFrom = context.sitesFrom()[0];
			final int sitesTo = sitesFrom + context.containers()[0].numSites();

			for (int site = sitesFrom; site < sitesTo; site++)
			{
				final int what = cs.what(site, type);
				final int who = cs.who(site, type);
				if (what != 0 && context.components()[what].name().contains(name) && who == whoId)
					count++;
			}
			return count;
		}

		for (int pid = 1; pid <= context.players().size(); pid++)
			if (whoId == context.players().size() || pid == whoId)
			{
				sitesOwned = context.state().owned().positions(pid);
				break;
			}

		if (sitesOwned != null)
		{
			for (final List<? extends Location> locs : sitesOwned)
			{
				for (int i = 0; i < locs.size(); i++)
				{
					final int site = locs.get(i).site();
					final SiteType locType = locs.get(i).siteType();
					final int level = locs.get(i).level();
					final ContainerState cs = context
							.containerState((locType.equals(SiteType.Cell) ? context.containerId()[site] : 0));
					final int who = cs.who(site, level, locType);
					if (whoId == who)
					{
						if (level == 0)
						{
							count += cs.count(site, locType);
						}
						else
							count++;
					}
				}
			}
		}
		return count;
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		return false;
	}

	@Override
	public String toString()
	{
		return "Pieces()";
	}

	@Override
	public long gameFlags(final Game game)
	{
		if (name != null && name.equals("Bag"))
			return GameType.Dominoes | GameType.LargePiece;

		long gameFlags = whoFn.gameFlags(game);

		if (whereFn != null)
			gameFlags |= whereFn.gameFlags(game);

		gameFlags |= SiteType.gameFlags(type);

		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(SiteType.concepts(type));
		concepts.or(whoFn.concepts(game));

		if (whereFn != null)
			concepts.or(whereFn.concepts(game));

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(whoFn.writesEvalContextRecursive());

		if (whereFn != null)
			writeEvalContext.or(whereFn.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(whoFn.readsEvalContextRecursive());

		if (whereFn != null)
			readEvalContext.or(whereFn.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;

		missingRequirement |= whoFn.missingRequirement(game);

		if (whereFn != null)
			missingRequirement |= whereFn.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= whoFn.willCrash(game);

		if (whereFn != null)
			willCrash |= whereFn.willCrash(game);
		return willCrash;
	}

	@Override
	public void preprocess(final Game game)
	{
		type = SiteType.use(type, game);
		whoFn.preprocess(game);
		if (whereFn != null)
			whereFn.preprocess(game);
	}

	// -------------------------------------------------------------------------

	/**
	 * @return The roletype of the owner of the pieces to count.
	 */
	public RoleType roleType()
	{
		return role;
	}
}
