package game.functions.ints.board;

import java.util.BitSet;
import java.util.List;

import annotations.Name;
import annotations.Opt;
import game.Game;
import game.functions.ints.BaseIntFunction;
import game.functions.ints.IntFunction;
import game.types.board.SiteType;
import game.types.state.GameType;
import main.Constants;
import other.context.Context;
import other.topology.TopologyElement;

/**
 * Returns the layer of a site.
 * 
 * @author Eric Piette
 * 
 * @remarks This ludeme returns the layer of a site for 3D boards. 
 *          If the board is flat (2D), then 0 is returned to indicate the board layer.
 */
public final class Layer extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Which site. */
	private final IntFunction site;
	
	/** Type of the graph element. */
	private SiteType type;

	/** The pre-computed value. */
	private int precomputedValue = Constants.OFF;

	//-------------------------------------------------------------------------

	/**
	 * @param of   The site to check.
	 * @param type The graph element type of the site.
	 * @example (layer of:(to))
	 */
	public Layer
	(
		@Name     final IntFunction of,
		     @Opt final SiteType    type
	)
	{
		this.site = of;
		this.type = type;
	}

	//-------------------------------------------------------------------------

	@Override
	public int eval(final Context context)
	{
		if (precomputedValue != Constants.OFF)
			return precomputedValue;

		final int index = site.eval(context);

		if (index < 0)
			return Constants.OFF;

		final SiteType realType = (type != null) ? type : context.game().board().defaultSite();
		final List<? extends TopologyElement> elements = context.topology().getGraphElements(realType);

		if (index >= elements.size())
			return Constants.OFF;
		
		return elements.get(index).layer();
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		return site.isStatic();
	}

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = site.gameFlags(game) | GameType.ThreeDimensions;
		gameFlags |= SiteType.gameFlags(type);
		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(SiteType.concepts(type));
		concepts.or(site.concepts(game));
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(site.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(site.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		site.preprocess(game);
		type = SiteType.use(type, game);

		if (isStatic())
			precomputedValue = eval(new Context(game, null));
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= site.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= site.willCrash(game);
		return willCrash;
	}
}
