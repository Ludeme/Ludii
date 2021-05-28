package game.functions.region.sites.piece;

import java.util.BitSet;

import annotations.Hide;
import game.Game;
import game.functions.ints.IntFunction;
import game.functions.region.BaseRegionFunction;
import game.util.equipment.Region;
import game.util.moves.Piece;
import other.context.Context;

/**
 * Returns the sites that a specified component starts on.
 * 
 * @author Eric.Piette
 */
@Hide
public final class SitesStart extends BaseRegionFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** If we can, we'll precompute once and cache */
	private Region precomputedRegion = null;

	//-------------------------------------------------------------------------

	/** Index of the component. */
	private final IntFunction indexFn;

	/**
	 * @param piece The index of the component.
	 */
	public SitesStart
	(
	    final Piece piece
	)
	{
		this.indexFn = piece.component();
	}

	//-------------------------------------------------------------------------

	@Override
	public Region eval(final Context context)
	{
		if (precomputedRegion != null)
			return precomputedRegion;

		if (indexFn == null)
			return new Region();

		final int index = indexFn.eval(context);

		if (index < 1 || index >= context.components().length)
			return new Region();

		return context.trial().startingPos().get(index);
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		if (indexFn != null)
			return indexFn.isStatic();
		return false;
	}

	@Override
	public long gameFlags(final Game game)
	{
		if (indexFn != null)
			return indexFn.gameFlags(game);
		return 0L;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		if (indexFn != null)
			concepts.or(indexFn.concepts(game));
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		if (indexFn != null)
			writeEvalContext.or(indexFn.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		if (indexFn != null)
			readEvalContext.or(indexFn.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		if (isStatic())
			precomputedRegion = eval(new Context(game, null));
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		if (indexFn != null)
			missingRequirement |= indexFn.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		if (indexFn != null)
			willCrash |= indexFn.willCrash(game);
		return willCrash;
	}
}
