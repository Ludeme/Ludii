package game.rules.play.moves.nonDecision.effect.state.remember;

import annotations.Name;
import annotations.Opt;
import game.Game;
import game.functions.booleans.BooleanFunction;
import game.functions.ints.IntFunction;
import game.rules.play.moves.Moves;
import game.rules.play.moves.nonDecision.effect.Effect;
import game.rules.play.moves.nonDecision.effect.Then;
import game.rules.play.moves.nonDecision.effect.state.remember.state.RememberState;
import game.rules.play.moves.nonDecision.effect.state.remember.value.RememberValue;
import other.context.Context;

/**
 * Remember information about the state to be used in future state.
 * 
 * @author Eric.Piette
 */
@SuppressWarnings("javadoc")
public final class Remember extends Effect
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * For remembering a value.
	 * 
	 * @param rememberType The type of property to remember.
	 * @param name 		   The name of the remembering values.
	 * @param value        The value to remember.
	 * @param unique       True if each remembered value has to be unique [False].
	 * @param then         The moves applied after that move is applied.
	 * 
	 * @example (remember Value (count Pips))
	 */
	public static Moves construct
	(
		           final RememberValueType rememberType, 
		@Opt       final String            name,
		           final IntFunction       value,
		@Opt @Name final BooleanFunction   unique,
		@Opt       final Then              then
	)
	{
		switch (rememberType)
		{
		case Value:
			return new RememberValue(name,value,unique,then);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Remember(): A RememberValueType is not implemented.");
	}
	
	/**
	 * For remembering the current state.
	 * 
	 * @param rememberType The type of property to remember.
	 * @param then         The moves applied after that move is applied.
	 * 
	 * @example (remember State)
	 */
	public static Moves construct
	(
		      final RememberStateType rememberType, 
		@Opt  final Then              then
	)
	{
		switch (rememberType)
		{
		case State:
			return new RememberState(then);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Remember(): A RememberStateType is not implemented.");
	}

	//-------------------------------------------------------------------------

	private Remember()
	{
		super(null);
		// Ensure that compiler does pick up default constructor
	}

	@Override
	public Moves eval(final Context context)
	{
		// Should not be called, should only be called on subclasses
		throw new UnsupportedOperationException("Remember.eval(): Should never be called directly.");
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
		return super.gameFlags(game);
	}

	@Override
	public void preprocess(final Game game)
	{
		// Nothing to do.
	}

	@Override
	public boolean canMoveTo(Context context, int target)
	{
		// Should never be there
		throw new UnsupportedOperationException("Remember.canMoveTo(): Should never be called directly.");
	}
	
	//-------------------------------------------------------------------------
}