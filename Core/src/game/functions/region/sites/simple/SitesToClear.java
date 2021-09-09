package game.functions.region.sites.simple;

import java.util.BitSet;

import annotations.Hide;
import game.Game;
import game.functions.region.BaseRegionFunction;
import game.types.state.GameType;
import game.util.equipment.Region;
import other.context.Context;

/**
 * Returns the sites to remove when a capture sequence is currently in progress.
 * 
 * @author Eric.Piette
 */
@Hide
public final class SitesToClear extends BaseRegionFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * 
	 */
	public SitesToClear()
	{
		// Nothing to do.
	}

	//-------------------------------------------------------------------------

	@Override
	public Region eval(final Context context)
	{
		return context.state().regionToRemove();
	}
	
	@Override
	public boolean contains(final Context context, final int location)
	{
		return context.state().piecesToRemove().contains(location);
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
		return "ToClear()";
	}

	@Override
	public long gameFlags(final Game game)
	{
		return GameType.SequenceCapture;
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
