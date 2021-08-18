package game.functions.ints.board;

import java.util.BitSet;

import annotations.Name;
import annotations.Opt;
import annotations.Or;
import game.Game;
import game.functions.ints.BaseIntFunction;
import game.functions.ints.IntFunction;
import game.functions.region.RegionFunction;
import game.types.board.SiteType;
import main.Constants;
import other.IntArrayFromRegion;
import other.context.Context;

/**
 * Returns the cost of graph element(s).
 * 
 * @author Eric.Piette
 */
public final class Cost extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** If we can, we'll precompute once and cache */
	private int precomputedInteger = Constants.UNDEFINED;

	//-------------------------------------------------------------------------

	/** The region. */
	private final IntArrayFromRegion region;

	/** The type of the graph element. */
	private final SiteType type;

	/**
	 * @param type The type of the graph element [Cell].
	 * @param at   The index of the graph element.
	 * @param in   The region of the graph elements.
	 * 
	 * @example (cost at:(to))
	 */
	public Cost
	(
		          @Opt final SiteType       type,
		@Or @Name      final IntFunction    at, 
		@Or @Name      final RegionFunction in
	)
	{
		this.type = (type == null) ? SiteType.Cell : type;
		this.region = new IntArrayFromRegion(at, in);
	}

	//-------------------------------------------------------------------------

	@Override
	public int eval(final Context context)
	{
		if (precomputedInteger != Constants.UNDEFINED)
			return precomputedInteger;

		final int[] sites = region.eval(context);
		final other.topology.Topology graph = context.topology();
		int sum = 0;

		for (final int site : sites)
		{
			if (type.equals(SiteType.Vertex))
				sum += graph.vertices().get(site).cost();
			else if (type.equals(SiteType.Cell))
				sum += graph.cells().get(site).cost();
			else if (type.equals(SiteType.Edge))
				sum += graph.edges().get(site).cost();
		}
		
		return sum;
	}

	@Override
	public boolean isStatic()
	{
		return false;
	}

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = region.gameFlags(game);
		gameFlags |= SiteType.gameFlags(type);
		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(SiteType.concepts(type));
		concepts.or(region.concepts(game));
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(region.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(region.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		region.preprocess(game);
		if (isStatic())
			precomputedInteger = eval(new Context(game, null));
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= region.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= region.willCrash(game);
		return willCrash;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		return "Cost()";
	}
}