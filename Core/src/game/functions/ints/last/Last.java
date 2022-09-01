package game.functions.ints.last;

import annotations.Name;
import annotations.Opt;
import game.Game;
import game.functions.booleans.BooleanFunction;
import game.functions.ints.BaseIntFunction;
import game.functions.ints.IntFunction;
import other.context.Context;

/**
 * Returns a site related to the last move.
 * 
 * @author Eric Piette
 */
@SuppressWarnings("javadoc")
public final class Last extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * @param lastType         The site to return.
	 * @param afterConsequence True, to check the location related to the last decision; False, to check the to location related to the last consequence. [False].
	 * 
	 * @example (last To)
	 * @example (last From)
	 * @example (last LevelFrom)
	 * @example (last LevelTo)
	 */
	public static IntFunction construct
	(
		           final LastType        lastType, 
		@Opt @Name final BooleanFunction afterConsequence
	)
	{
		switch (lastType)
		{
		case From:
			return new LastFrom(afterConsequence);
		case LevelFrom:
			return new LastLevelFrom(afterConsequence);
		case To:
			return new LastTo(afterConsequence);
		case LevelTo:
			return new LastLevelTo(afterConsequence);
		default:
			break;
		}
		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Last(): A LastType is not implemented.");
	}

	private Last()
	{
		// Make grammar pick up construct() and not default constructor
	}

	//-------------------------------------------------------------------------

	@Override
	public int eval(final Context context)
	{
		// Should not be called, should only be called on subclasses
		throw new UnsupportedOperationException("Last.eval(): Should never be called directly.");

		// return new Region();
	}

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