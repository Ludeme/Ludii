package game.functions.region.sites.simple;

import java.util.BitSet;

import annotations.Hide;
import game.Game;
import game.functions.region.BaseRegionFunction;
import game.types.state.GameType;
import game.util.equipment.Region;
import other.context.Context;

/**
 * Returns the hint region stored in the context.
 * 
 * @author Eric.Piette and cambolbro
 * 
 * @remarks Rename from Hint ludeme to avoid compiler confusion.
 */
@Hide
public final class SitesHint extends BaseRegionFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * 
	 */
	public SitesHint()
	{
		// Nothing to do.
	}

	//-------------------------------------------------------------------------

	@Override
	public Region eval(final Context context)
	{
		return context.hintRegion().eval(context);
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
		return "Hint()";
	}

	@Override
	public long gameFlags(final Game game)
	{
		return GameType.DeductionPuzzle;
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
		// Do nothing
	}
}
