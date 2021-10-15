package game.functions.ints.iterator;

import java.util.BitSet;

import game.Game;
import game.functions.ints.BaseIntFunction;
import other.context.Context;
import other.context.EvalContextData;

/**
 * Returns the ``track'' value of the context.
 * 
 * @author Eric.Piette
 * @remarks Used in a (forEach Track ...) ludeme to set the value to the index
 *          of each track.
 */
public final class Track extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * @example (track)
	 */
	public Track()
	{
		// Nothing to do.
	}

	//-------------------------------------------------------------------------

	@Override
	public int eval(final Context context)
	{
		return context.track();
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
		return 0;
	}

	@Override
	public void preprocess(final Game game)
	{
		// nothing to do
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
		return readsEvalContextFlat();
	}
	
	@Override
	public BitSet readsEvalContextFlat()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.set(EvalContextData.Track.id(), true);
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		if (!game.hasTrack())
		{
			game.addRequirementToReport("The ludeme (track) is used but the board has no tracks.");
			missingRequirement = true;
		}
		return missingRequirement;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		return "(track)";
	}
}
