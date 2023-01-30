package game.functions.booleans.is.site;

import java.util.BitSet;

import annotations.Hide;
import annotations.Opt;
import game.Game;
import game.functions.booleans.BaseBooleanFunction;
import game.functions.ints.IntFunction;
import game.types.board.SiteType;
import other.context.Context;
import other.state.container.ContainerState;

/**
 * Checks if a site is occupied.
 * 
 * @author Eric.Piette
 */
@Hide
public final class IsOccupied extends BaseBooleanFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The graph Element type to check. */
	private SiteType type;

	/** The index of the site. */
	private final IntFunction siteFn;

	//-------------------------------------------------------------------------

	/**
	 * @param type The graph element type [default SiteType of the board].
	 * @param site The index of the site.
	 */
	public IsOccupied
	(
		@Opt final SiteType    type, 
		     final IntFunction site
    )
	{
		this.type = type;
		siteFn = site;
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean eval(final Context context)
	{
		final int site = siteFn.eval(context);

		if (site < 0 || site >= context.containerId().length)
			return false;

		final ContainerState cs = context.containerState((context.containerId()[site]));
		return cs.what(site, type) != 0;
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
		long gameFlags = siteFn.gameFlags(game);

		gameFlags |= SiteType.gameFlags(type);

		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(SiteType.concepts(type));
		concepts.or(siteFn.concepts(game));
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(siteFn.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(siteFn.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		type = SiteType.use(type, game);
		siteFn.preprocess(game);
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= siteFn.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= siteFn.willCrash(game);
		return willCrash;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{
		final SiteType realSiteType = (type == null) ? game.board().defaultSite() : type;
		return realSiteType.name().toLowerCase() + " " + siteFn.toEnglish(game) + " is occupied" ;
	}
	
	//-------------------------------------------------------------------------
}
