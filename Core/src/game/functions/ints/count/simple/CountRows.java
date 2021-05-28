package game.functions.ints.count.simple;

import java.util.BitSet;

import annotations.Hide;
import annotations.Opt;
import game.Game;
import game.functions.ints.BaseIntFunction;
import game.types.board.SiteType;
import other.context.Context;

/**
 * Returns the number of rows of the corresponding graph element.
 * 
 * @author Eric.Piette
 */
@Hide
public final class CountRows extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** If we can, we'll precompute once and cache. */
	private Integer preComputedInteger = null;

	//-------------------------------------------------------------------------

	/** Cell/Edge/Vertex. */
	private SiteType type;

	/**
	 * @param type The graph element type.
	 */
	public CountRows
	(
		@Opt final SiteType type
	)
	{
		this.type = type;
	}

	//-------------------------------------------------------------------------

	@Override
	public int eval(final Context context)
	{
		if (preComputedInteger != null)
			return preComputedInteger.intValue();

		final SiteType realSiteType = (type != null) ? type
				: context.board().defaultSite();

		return context.topology().rows(realSiteType).size();
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
		return "Columns()";
	}

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = 0L;

		gameFlags |= SiteType.gameFlags(type);

		return gameFlags;
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
		preComputedInteger = Integer.valueOf(eval(new Context(game, null)));
	}
}
