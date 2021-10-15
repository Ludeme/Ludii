package game.functions.region.last;

import java.util.BitSet;

import annotations.Hide;
import game.Game;
import game.functions.region.BaseRegionFunction;
import game.util.equipment.Region;
import other.context.Context;
import other.move.Move;

/**
 * Returns the ``between'' sites of a set of moves.
 * 
 * @author Eric Piette
 */
@Hide
public final class LastBetween extends BaseRegionFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * 
	 */
	public LastBetween()
	{
	}

	//-------------------------------------------------------------------------

	@Override
	public Region eval(final Context context)
	{
		final Move move = context.trial().lastMove();
		if (move == null)
			return new Region();
		return new Region(move.betweenNonDecision().toArray());
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
		return 0l;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
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
		// Nothing to do.
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		final boolean missingRequirement = false;
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		final boolean willCrash = false;
		return willCrash;
	}
}

