package game.functions.ints.value;

import annotations.Name;
import annotations.Opt;
import annotations.Or;
import game.Game;
import game.functions.ints.BaseIntFunction;
import game.functions.ints.IntFunction;
import game.functions.ints.value.iterated.ValueIterated;
import game.functions.ints.value.piece.ValuePiece;
import game.functions.ints.value.player.ValuePlayer;
import game.functions.ints.value.random.ValueRandom;
import game.functions.ints.value.simple.ValuePending;
import game.functions.range.RangeFunction;
import game.types.board.SiteType;
import game.types.play.RoleType;
import other.context.Context;

/**
 * Returns the value of the specified property.
 * 
 * @author Eric Piette
 */
@SuppressWarnings("javadoc")
public final class Value extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * For returning a random value within a range.
	 * 
	 * @param valueType The property to return the value.
	 * @param range     The range.
	 * 
	 * @example (value Random (range 3 5))
	 */
	public static IntFunction construct
	(
		final ValueRandomType valueType, 
		final RangeFunction   range
	)
	{
		switch (valueType)
		{
		case Random:
			return new ValueRandom(range);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Value(): A ValueRandomType is not implemented.");
	}

	//-------------------------------------------------------------------------

	/**
	 * For returning the pending value.
	 * 
	 * @param valueType The property to return the value.
	 * 
	 * @example (value Pending)
	 */
	public static IntFunction construct
	(
		final ValueSimpleType valueType
	)
	{
		switch (valueType)
		{
		case Pending:
			return new ValuePending();
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Value(): A ValueSimpleType is not implemented.");
	}
	
	//-------------------------------------------------------------------------

	/**
	 * For returning the player value.
	 * 
	 * @param valueType   The property to return the value.
	 * @param indexPlayer The index of the player.
	 * @param role        The roleType of the player.
	 * 
	 * @example (value Player (who at:(to)))
	 */
	public static IntFunction construct
	(
		    final ValuePlayerType valueType,
		@Or final IntFunction     indexPlayer,
		@Or final RoleType        role
	)
	{
		int numNonNull = 0;
		if (indexPlayer != null)
			numNonNull++;
		if (role != null)
			numNonNull++;

		if (numNonNull != 1)
			throw new IllegalArgumentException(
					"Value(): With ValuePlayerType exactly one indexPlayer or role parameter must be non-null.");
		
		switch (valueType)
		{
		case Player:
			return new ValuePlayer(indexPlayer, role);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Value(): A ValuePlayerType is not implemented.");
	}
	
	//-------------------------------------------------------------------------

	/**
	 * For returning the piece value.
	 * 
	 * @param valueType The property to return the value.
	 * @param type      The graph element type [default SiteType of the board].
	 * @param at        The location to check.
	 * @param level     The level to check.
	 * 
	 * @example (value Piece at:(to))
	 */
	public static IntFunction construct
	(
		               final ValueComponentType valueType,
			@Opt       final SiteType           type,
			     @Name final IntFunction        at,
			@Opt @Name final IntFunction        level
	)
	{
		switch (valueType)
		{
		case Piece:
			return new ValuePiece(type,at,level);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Value(): A ValueComponentType is not implemented.");
	}
	
	//-------------------------------------------------------------------------

	/**
	 * For returning the value iterated in (forEach Value ...).
	 * 
	 * @example (value)
	 */
	public static IntFunction construct()
	{
		return new ValueIterated();
	}

	//-------------------------------------------------------------------------

	private Value()
	{
		// Make grammar pick up construct() and not default constructor
	}
	
	//-------------------------------------------------------------------------

	@Override
	public int eval(final Context context)
	{
		// Should not be called, should only be called on subclasses
		throw new UnsupportedOperationException("Value.eval(): Should never be called directly.");

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