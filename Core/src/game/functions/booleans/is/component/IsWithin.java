package game.functions.booleans.is.component;

import java.util.BitSet;

import annotations.Hide;
import annotations.Opt;
import annotations.Or;
import game.Game;
import game.functions.booleans.BaseBooleanFunction;
import game.functions.ints.IntFunction;
import game.functions.ints.last.LastTo;
import game.functions.region.RegionFunction;
import game.types.board.SiteType;
import gnu.trove.list.array.TIntArrayList;
import other.IntArrayFromRegion;
import other.context.Context;

/**
 * Tests if a specific piece is on the designed region.
 * 
 * @author Eric.Piette
 */
@Hide
public final class IsWithin extends BaseBooleanFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The item identity. */
	private final IntFunction pieceId;

	/** Which region. */
	protected final IntArrayFromRegion region;

	/** Cell/Edge/Vertex. */
	private SiteType type;

	//-------------------------------------------------------------------------

	/**
	 * @param pieceId The index of the item.
	 * @param type    The graph element type.
	 * @param locn    The location to check [(lastTo)].
	 * @param region  The region to check.
	 */
	public IsWithin
	(
			     final IntFunction    pieceId,
			@Opt final SiteType       type,
		@Or      final IntFunction    locn,
		@Or      final RegionFunction region
	)
	{
		this.pieceId = pieceId;
		
		this.region = new IntArrayFromRegion
			(
				(region == null && locn != null ? locn : region == null ? new LastTo(null) : null),
				(region != null) ? region : null
			);
		
		this.type = type;
	}
	
	@Override
	public final boolean eval(Context context)
	{
		final int    pid   = pieceId.eval(context);
		final int    owner = context.components()[pid].owner();
		final TIntArrayList sites = new TIntArrayList(region.eval(context));

		final TIntArrayList owned = context.state().owned().sites(owner, pid);
		for (int i = 0; i < owned.size(); i++)
		{
			final int location = owned.getQuick(i);
			if (sites.contains(location))
				return true;
		}

		return false;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		return "IsWithin(" + pieceId + "," + region + ")";
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
		long gameFlags = pieceId.gameFlags(game) | region.gameFlags(game);

		gameFlags |= SiteType.gameFlags(type);

		return gameFlags;
	}
	
	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(pieceId.concepts(game));
		concepts.or(region.concepts(game));
		concepts.or(SiteType.concepts(type));
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(pieceId.writesEvalContextRecursive());
		writeEvalContext.or(region.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(pieceId.readsEvalContextRecursive());
		readEvalContext.or(region.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		type = SiteType.use(type, game);
		pieceId.preprocess(game);
		region.preprocess(game);
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= pieceId.missingRequirement(game);
		missingRequirement |= region.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= pieceId.willCrash(game);
		willCrash |= region.willCrash(game);
		return willCrash;
	}
}
