package game.functions.ints.board;

import java.util.BitSet;

import annotations.Opt;
import game.Game;
import game.functions.ints.BaseIntFunction;
import game.types.board.SiteType;
import main.Constants;
import other.context.Context;

/**
 * Returns the index of the central board site.
 * 
 * @author Eric.Piette
 */
public final class CentrePoint extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Cell, Edge or Vertex. */
	protected SiteType type;

	/** If we can, we'll precompute once and cache */
	private int precomputedInteger = Constants.UNDEFINED;

	//-------------------------------------------------------------------------

	/**
	 * @param type The graph element type [default SiteType of the board].
	 * 
	 * @example (centrePoint)
	 */
	public CentrePoint
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
		if (precomputedInteger != Constants.UNDEFINED)
			return precomputedInteger;

		final other.topology.Topology graph = context.topology();

		final SiteType realType = (type != null) ? type : context.game().board().defaultSite();

		return graph.centre(realType).get(0).index();
	}

	@Override
	public boolean isStatic()
	{
		return true;
	}

	@Override
	public long gameFlags(final Game game)
	{
		long flags = 0;
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

		if (isStatic())
			precomputedInteger = eval(new Context(game, null));
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		return "CentrePoint()";
	}
	
	@Override
	public String toEnglish(final Game game) 
	{
		return "the centre point of the board";
	}
}
