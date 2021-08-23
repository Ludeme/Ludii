package game.functions.region.sites.index;

import java.util.BitSet;

import annotations.Hide;
import annotations.Opt;
import game.Game;
import game.functions.ints.IntFunction;
import game.functions.region.BaseRegionFunction;
import game.types.board.SiteType;
import game.util.equipment.Region;
import other.context.Context;
import other.translation.LanguageUtils;

/**
 * Returns all the sites in a specific row of the board.
 * 
 * @author Eric.Piette
 */
@Hide
public final class SitesRow extends BaseRegionFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** If we can, we'll precompute once and cache. */
	private Region precomputedRegion = null;

	//-------------------------------------------------------------------------

	/**
	 * The index of the row.
	 */
	private final IntFunction index;

	/**
	 * @param elementType Type of graph elements to return [Cell (or Vertex if the
	 *                    main board uses intersections)].
	 * @param index       Index of the row.
	 */
	public SitesRow
	(
		@Opt final SiteType elementType, 
		     final IntFunction index
    )
	{
		type = elementType;
		this.index = index;
	}

	//-------------------------------------------------------------------------

	@Override
	public Region eval(final Context context)
	{
		if (precomputedRegion != null)
			return precomputedRegion;

		final SiteType realType = (type != null) ? type
				: context.board().defaultSite();

		final other.topology.Topology graph = context.topology();

		if (index == null)
			return new Region();
		final int i = index.eval(context);
		if (i < 0)
		{
			System.out.println("** Negative row index.");
			return new Region();
		}
		else if (i >= graph.rows(realType).size())
			return new Region();

		return new Region(graph.rows(realType).get(i));
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
		return "Row()";
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
	public void preprocess(final Game game)
	{
		type = SiteType.use(type, game);
		if (index != null)
			index.preprocess(game);
		if (isStatic())
			precomputedRegion = eval(new Context(game, null));
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
	public String toEnglish(final Game game) 
	{
		final String indexText = LanguageUtils.IndexAsText(index.eval(null) + 1);

		switch(type) {
		case Cell:
			return "each cell of the " + indexText + " row";
		case Edge:

		case Vertex:

		default:
			throw new RuntimeException("SiteType can't be translated! [" + type.name() + "]");
		}
	}
}
