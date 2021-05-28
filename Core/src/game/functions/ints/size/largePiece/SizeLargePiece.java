package game.functions.ints.size.largePiece;

import java.util.BitSet;

import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import annotations.Or;
import game.Game;
import game.equipment.component.Component;
import game.functions.ints.BaseIntFunction;
import game.functions.ints.IntFunction;
import game.functions.region.RegionFunction;
import game.types.board.SiteType;
import gnu.trove.list.array.TIntArrayList;
import other.IntArrayFromRegion;
import other.context.Context;
import other.state.container.ContainerState;

/**
 * Returns the size of large pieces currently placed..
 *
 * @author Eric.Piette
 */
@Hide
public final class SizeLargePiece extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;
	
	//-------------------------------------------------------------------------
	
	/** the type of player. */
	private final IntArrayFromRegion region;
	
	/** Cell/Edge/Vertex. */
	private SiteType type;

	//-------------------------------------------------------------------------

	/**
	 * @param type     The graph element type [default site type of the board].
	 * @param in       The region to look for large pieces.
	 * @param at       The site to look for large piece.
	 */
	public SizeLargePiece
	(
		@Opt           final SiteType       type,
			 @Or @Name final RegionFunction in, 
			 @Or @Name final IntFunction    at
	)
	{
		this.type   = type;
		this.region = new IntArrayFromRegion(at, in);
	}

	//-------------------------------------------------------------------------
	@Override
	public int eval(final Context context)
	{
		int count = 0;
		final SiteType realType = (type != null) ? type : context.game().board().defaultSite();
		final int[] sites = region.eval(context);
		
		for (int i = 0; i < sites.length; i++)
		{
			final int site = sites[i];
			final int cid = (realType.equals(SiteType.Cell) ? context.containerId()[site] : 0);
			final ContainerState cs = context.containerState(cid);
			final int what = cs.what(site, realType);
			if (what != 0)
			{
				final Component component = context.components()[what];
				if (component.isLargePiece())
				{
					final TIntArrayList locs = component.locs(context,
							context.topology().centre(realType).get(0).index(), 0, context.topology());
					count += locs.size();
				}
				else
					count++;
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
	public long gameFlags(final Game game)
	{
		long flags = region.gameFlags(game);

		flags |= SiteType.gameFlags(type);

		return flags;
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
		if (!game.hasLargePiece())
		{
			game.addRequirementToReport(
					"The ludeme (size LargePiece ...) is used but the equipment has no large pieces.");
			missingRequirement = true;
		}
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
}
