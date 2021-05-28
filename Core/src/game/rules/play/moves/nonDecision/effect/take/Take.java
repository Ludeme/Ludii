package game.rules.play.moves.nonDecision.effect.take;

import annotations.Name;
import annotations.Opt;
import annotations.Or;
import annotations.Or2;
import game.Game;
import game.functions.ints.IntFunction;
import game.functions.region.RegionFunction;
import game.rules.play.moves.Moves;
import game.rules.play.moves.nonDecision.effect.Effect;
import game.rules.play.moves.nonDecision.effect.Then;
import game.rules.play.moves.nonDecision.effect.take.control.TakeControl;
import game.rules.play.moves.nonDecision.effect.take.simple.TakeDomino;
import game.types.board.SiteType;
import game.types.play.RoleType;
import other.context.Context;

/**
 * Takes a piece or the control of pieces.
 * 
 * @author Eric.Piette
 */
@SuppressWarnings("javadoc")
public final class Take extends Effect
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * For taking a domino.
	 * 
	 * @param takeType The type of property to take.
	 * @param then     The moves applied after that move is applied.
	 * 
	 * @example (take Domino)
	 */
	public static Moves construct
	(
			 final TakeSimpleType takeType,
		@Opt final Then           then
	)
	{
		switch (takeType)
		{
		case Domino:
			return new TakeDomino(then);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Take(): A TakeSimpleType is not implemented.");
	}
	
	//-------------------------------------------------------------------------

	/**
	 * For taking the control of the pieces of another player.
	 * 
	 * @param takeType The type of property to take.
	 * @param of       The roleType of the pieces to take control of.
	 * @param Of       The player index of the pieces to take control of.
	 * @param by       The roleType taking the control.
	 * @param By       The player index of the player taking control.
	 * @param at       The site to take the control.
	 * @param to       The region to take the control.
	 * @param type     The graph element type [default SiteType of the board].
	 * @param then     The moves applied after that move is applied.
	 * 
	 * @example (take Control of:P1 by:Mover)
	 */
	public static Moves construct
	(
			                final TakeControlType takeType,
			     @Or  @Name final RoleType        of,
			     @Or  @Name final IntFunction     Of,
			     @Or2 @Name final RoleType        by,
			     @Or2 @Name final IntFunction     By,
			@Opt @Or  @Name final IntFunction     at,
			@Opt @Or  @Name final RegionFunction  to,
			@Opt       	    final SiteType        type,
			@Opt            final Then            then
	)
	{
		int numNonNull = 0;
		if (of != null)
			numNonNull++;
		if (Of != null)
			numNonNull++;

		if (numNonNull != 1)
			throw new IllegalArgumentException(
					"take(): With TakeControlType one of or Of parameter must be non-null.");

		int numNonNull2 = 0;
		if (by != null)
			numNonNull2++;
		if (By != null)
			numNonNull2++;

		if (numNonNull2 != 1)
			throw new IllegalArgumentException(
					"take(): With TakeControlType one by or By parameter must be non-null.");
		
		numNonNull = 0;
		if (at != null)
			numNonNull++;
		if (to != null)
			numNonNull++;

		if (numNonNull > 1)
			throw new IllegalArgumentException(
					"take(): With TakeControlType zero or one at or to parameter can be non-null.");

		switch (takeType)
		{
		case Control:
			return new TakeControl(of, Of, by, By, at, to, type, then);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Take(): A TakeControlType is not implemented.");
	}
	
	private Take()
	{
		super(null);
		// Ensure that compiler does pick up default constructor
	}

	@Override
	public Moves eval(final Context context)
	{
		// Should not be called, should only be called on subclasses
		throw new UnsupportedOperationException("Take.eval(): Should never be called directly.");
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
	public boolean canMoveTo(Context context, int target)
	{
		// Should never be there
		throw new UnsupportedOperationException("Take.canMoveTo(): Should never be called directly.");
	}

}