package game.rules.play.moves.nonDecision.effect.state.forget;

import annotations.Opt;
import game.Game;
import game.functions.ints.IntFunction;
import game.rules.play.moves.Moves;
import game.rules.play.moves.nonDecision.effect.Effect;
import game.rules.play.moves.nonDecision.effect.Then;
import game.rules.play.moves.nonDecision.effect.state.forget.value.ForgetValue;
import game.rules.play.moves.nonDecision.effect.state.forget.value.ForgetValueAll;
import other.context.Context;

/**
 * Forget information about the state to be used in future state.
 * 
 * @author Eric.Piette
 */
@SuppressWarnings("javadoc")
public final class Forget extends Effect
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * For forgetting all values.
	 * 
	 * @param rememberType The type of property to forget.
	 * @param name         The name of the remembering values.
	 * @param valueType    To specify which values.
	 * @param then         The moves applied after that move is applied.
	 * 
	 * @example (forget Value All)
	 */
	public static Moves construct
	(
			 final ForgetValueType    rememberType, 
	   @Opt  final String             name,
			 final ForgetValueAllType valueType,
	   @Opt  final Then               then
	)
	{
		switch (rememberType)
		{
		case Value:
			switch (valueType)
			{
			case All:
				return new ForgetValueAll(name,then);
			default:
				break;
			}
			break;
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Forget(): A ForgetValueType is not implemented.");
	}

	// -------------------------------------------------------------------------

	/**
	 * For forgetting a value.
	 * 
	 * @param rememberType The type of property to forget.
	 * @param name         The name of the remembering values.
	 * @param value        The value to forget.
	 * @param then         The moves applied after that move is applied.
	 * 
	 * @example (forget Value (count Pips))
	 */
	public static Moves construct
	(
		      final ForgetValueType rememberType, 
		@Opt  final String          name,
		      final IntFunction     value,
		@Opt  final Then            then
	)
	{
		switch (rememberType)
		{
		case Value:
			return new ForgetValue(name, value, then);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Forget(): A ForgetValueType is not implemented.");
	}
	
	//-------------------------------------------------------------------------

	private Forget()
	{
		super(null);
		// Ensure that compiler does pick up default constructor
	}

	@Override
	public Moves eval(final Context context)
	{
		// Should not be called, should only be called on subclasses
		throw new UnsupportedOperationException("Forget.eval(): Should never be called directly.");
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
		throw new UnsupportedOperationException("Forget.canMoveTo(): Should never be called directly.");
	}
	
	//-------------------------------------------------------------------------
}