package game.functions.ints.board;

import java.util.BitSet;
import java.util.List;

import annotations.Name;
import annotations.Opt;
import game.Game;
import game.functions.ints.BaseIntFunction;
import game.functions.ints.IntFunction;
import game.types.board.SiteType;
import main.Constants;
import other.context.Context;
import other.topology.TopologyElement;
import other.translation.LanguageUtils;

/**
 * Returns the column number in which a given site lies.
 * 
 * @author Eric Piette
 * 
 * @remarks Returns OFF (-1) if the site does not belong to any column.
 */
public final class Column extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Which site. */
	private final IntFunction site;

	/** Cell/Edge/Vertex. */
	private SiteType type;

	/** The precomputed value. */
	private int precomputedValue = Constants.OFF;

	//-------------------------------------------------------------------------

	/**
	 * @param type The graph element type [default SiteType of the board].
	 * @param of   The site to check.
	 * 
	 * @example (column of:(to))
	 */
	public Column
	(
		      @Opt  final SiteType    type,
		@Name       final IntFunction of
	)
	{
		site = of;
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

		return elements.get(index).col();
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
		long gameFlags = site.gameFlags(game);
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
		type = SiteType.use(type, game);

		if (site != null)
			site.preprocess(game);
		
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
	
	@Override
	public String toEnglish(final Game game) 
	{
		return "the column within which site " + LanguageUtils.getLocationName(site.toEnglish(game), type) + " lies";
	}
	
}
