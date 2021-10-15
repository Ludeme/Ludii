package game.functions.region.last;

import game.Game;
import game.functions.region.BaseRegionFunction;
import game.functions.region.RegionFunction;
import game.util.equipment.Region;
import other.context.Context;

/**
 * Returns sites related to the last move.
 * 
 * @author Eric.Piette
 */
@SuppressWarnings("javadoc")
public final class Last extends BaseRegionFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * For getting between sites of the last move played.
	 * 
	 * @param regionType Type of sites to return.
	 * 
	 * @example (last Between)
	 */
	public static RegionFunction construct(final LastRegionType regionType)
	{
		switch (regionType)
		{
		case Between:
			return new LastBetween();
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Sites(): A LastRegionType is not implemented.");
	}

	//-------------------------------------------------------------------------

	private Last()
	{
		// Ensure that compiler does pick up default constructor
	}

	//-------------------------------------------------------------------------

	@Override
	public Region eval(final Context context)
	{
		return null;
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		// Should never be there
		return false;
	}

	@Override
	public long gameFlags(final Game game)
	{
		// Should never be there
		return 0L;
	}

	@Override
	public void preprocess(final Game game)
	{
		// Nothing to do.
	}
}
