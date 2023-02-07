package game.functions.ints.size.site;

import java.util.BitSet;

import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import annotations.Or2;
import game.Game;
import game.functions.ints.BaseIntFunction;
import game.functions.ints.IntFunction;
import game.functions.ints.last.LastTo;
import game.functions.region.RegionFunction;
import game.types.board.SiteType;
import game.types.state.GameType;
import other.IntArrayFromRegion;
import other.context.Context;
import other.state.stacking.BaseContainerStateStacking;

/**
 * Returns the size of a stack.
 * 
 * @author Eric.Piette
 */
@Hide
public final class SizeStack extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Which region. */
	private final IntArrayFromRegion region;

	/** Cell/Edge/Vertex. */
	private SiteType type;

	/**
	 * @param type The graph element type.
	 * @param in   The region to count the size of the stacks.
	 * @param at   The location to count the site of the stack.
	 */
	public SizeStack
	(
		@Opt            final SiteType       type,
		@Opt @Or2 @Name final RegionFunction in,
		@Opt @Or2 @Name final IntFunction    at
	)
	{
		region = new IntArrayFromRegion(
				(in == null && at != null ? at : in == null ? new LastTo(null) : null),
				(in != null) ? in : null);
		this.type   = type;
	}

	//-------------------------------------------------------------------------

	@Override
	public int eval(final Context context)
	{
		final int[] sites;
		int count = 0;

		sites = region.eval(context);
		for (final int site : sites)
		{
			final BaseContainerStateStacking state = (BaseContainerStateStacking) context.state()
					.containerStates()[context.containerId()[site]];
			count += state.sizeStack(site, type);
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
		return "Stack()";
	}

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = region.gameFlags(game) | GameType.Stacking;

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
		type = SiteType.use(type, game);
		region.preprocess(game);
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
	public String toEnglish(final Game game)
	{
		final SiteType realType = (type == null) ? game.board().defaultSite() : type;
		return "the size of the stack on " + realType.name().toLowerCase() + " " + region.toEnglish(game);
	}
	
	//-------------------------------------------------------------------------
}
