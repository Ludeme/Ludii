package game.functions.region.sites.coords;

import java.util.Arrays;
import java.util.BitSet;

import annotations.Hide;
import annotations.Opt;
import game.Game;
import game.functions.region.BaseRegionFunction;
import game.types.board.SiteType;
import game.util.equipment.Region;
import gnu.trove.list.array.TIntArrayList;
import main.StringRoutines;
import other.context.Context;
import other.topology.SiteFinder;
import other.topology.TopologyElement;

/**
 * Returns all the sites with the correct coordinates.
 * 
 * @author Eric.Piette
 */
@Hide
public final class SitesCoords extends BaseRegionFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** If we can, we'll precompute once and cache. */
	private Region precomputedRegion = null;

	//-------------------------------------------------------------------------

	/**
	 * The coordinates.
	 */
	private final String[] coords;

	/**
	 * @param elementType Type of graph elements to return [Cell (or Vertex if the
	 *                    main board uses intersections)].
	 * @param coords      The sites corresponding to these coordinates.
	 */
	public SitesCoords
	(
		@Opt final SiteType elementType, 
		     final String[] coords
	)
	{
		type = elementType;
		this.coords = coords;
	}

	//-------------------------------------------------------------------------

	@Override
	public Region eval(final Context context)
	{
		if (precomputedRegion != null)
			return precomputedRegion;

		final TIntArrayList sites = new TIntArrayList();

		for (final String coord : coords)
		{
			final TopologyElement element = SiteFinder.find(context.board(), coord, type);
			if (element != null)
				sites.add(element.index());
		}

		return new Region(sites.toArray());
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		return true;
	}

	@Override
	public String toString()
	{
		return "Column()";
	}

	@Override
	public long gameFlags(final Game game)
	{
		long flags = 0L;

		flags |= SiteType.gameFlags(type);

		return flags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(SiteType.concepts(type));
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		type = SiteType.use(type, game);
		precomputedRegion = eval(new Context(game, null));
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{
		final SiteType realType = (type != null) ? type : game.board().defaultSite();
		return "the " + realType.name().toLowerCase() + StringRoutines.getPlural(realType.name()) + " with coordinates " + Arrays.toString(coords);
	}
	
	//-------------------------------------------------------------------------
		
}
