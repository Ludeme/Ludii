package game.rules.play.moves.nonDecision.operators.logical;

import annotations.Opt;
import game.Game;
import game.rules.play.moves.Moves;
import game.rules.play.moves.nonDecision.effect.Then;
import game.rules.play.moves.nonDecision.operator.Operator;
import other.context.Context;

/**
 * Is used to combine lists of moves.
 * 
 * @author Eric.Piette
 */
@SuppressWarnings("javadoc")
public final class And extends Operator
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * For making a move between two sets of moves.
	 * 
	 * @param listA The first move.
	 * @param listB The second move.
	 * @param then  The moves applied after that move is applied.
	 * 
	 * @example (and (set Score P1 100) (set Score P2 100))
	 */
	public static Moves construct
	(
			 final Moves listA,
			 final Moves listB,
		@Opt final Then  then
	)
	{
		// And moves is just an Or moves code in the context of the moves.
		return new Or(listA, listB, then);
	}

	/**
	 * For making a move between many sets of moves.
	 * 
	 * @param list The list of moves.
	 * @param then The moves applied after that move is applied.
	 * 
	 * @example (and { (set Score P1 100) (set Score P2 100) (set Score P3 100) })
	 */
	public static Moves construct
	(
			 final Moves[] list, 
		@Opt final Then    then
	)
	{
		// And moves is just an Or moves code in the context of the moves.
		return new Or(list, then);
	}

	// -------------------------------------------------------------------------
	private And()
	{
		super(null);
		// Ensure that compiler does pick up default constructor
	}
	
	//-------------------------------------------------------------------------

	@Override
	public Moves eval(final Context context)
	{
		// Should not be called, should only be called on subclasses
		throw new UnsupportedOperationException("And.eval(): Should never be called directly.");
	}

	//-------------------------------------------------------------------------
	
	@Override
	public boolean canMoveTo(final Context context, final int target)
	{
		// Should never be there
		throw new UnsupportedOperationException("And.canMoveTo(): Should never be called directly.");
	}

	//-------------------------------------------------------------------------

	@Override
	public long gameFlags(final Game game)
	{
		// Should never be there
		return 0L;
	}

	@Override
	public boolean isStatic()
	{
		// Should never be there
		return false;
	}
	
	@Override
	public void preprocess(final Game game)
	{
		// Nothing to do.
	}

}
