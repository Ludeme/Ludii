package game.rules.meta.no;

import annotations.Opt;
import game.Game;
import game.rules.meta.MetaRule;
import game.rules.meta.no.repeat.NoRepeat;
import game.types.play.RepetitionType;
import other.context.Context;

/**
 * Defines a decision move.
 * 
 * @author Eric.Piette
 */
@SuppressWarnings("javadoc")
public final class No extends MetaRule
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * For specifying a particular type of repetition that is forbidden in the game.
	 * 
	 * @param type Type of repetition to forbid [Positional].
	 * @param repetitionType Type of repetition to forbid [Positional].
	 * 
	 * @example (no Repeat PositionalInTurn)
	 */
	public static MetaRule construct
	(
		      final NoRepeatType    type,
		 @Opt final RepetitionType repetitionType
	)
	{
		switch (type)
		{
		case Repeat:
			return new NoRepeat(repetitionType);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("No(): A NoRepeatType is not implemented.");
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

	@Override
	public void eval(Context context)
	{
		// TODO Auto-generated method stub
	}

	//-------------------------------------------------------------------------

}
