package game.rules.play.moves.nonDecision.effect.state.swap;

import annotations.And;
import annotations.Opt;
import annotations.Or;
import annotations.Or2;
import game.Game;
import game.functions.ints.IntFunction;
import game.rules.play.moves.Moves;
import game.rules.play.moves.nonDecision.effect.Effect;
import game.rules.play.moves.nonDecision.effect.Then;
import game.rules.play.moves.nonDecision.effect.state.swap.players.SwapPlayers;
import game.rules.play.moves.nonDecision.effect.state.swap.sites.SwapPieces;
import game.types.play.RoleType;
import other.context.Context;

/**
 * Swaps two players or two pieces.
 * 
 * @author Eric.Piette and cambolbro
 */
@SuppressWarnings("javadoc")
public final class Swap extends Effect
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * For swapping two pieces.
	 * 
	 * @param swapType The type of property to take.
	 * @param locA     The first location [(lastFrom)].
	 * @param locB     The second location [(lastTo)].
	 * @param then     The moves applied after that move is applied.
	 * 
	 * @example (swap Pieces)
	 */
	public static Moves construct
	(
			      final SwapSitesType swapType,
		@And @Opt final IntFunction   locA,
		@And @Opt final IntFunction   locB,
		     @Opt final Then          then
	)
	{
		switch (swapType)
		{
		case Pieces:
			return new SwapPieces(locA, locB, then);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Swap(): A SwapSitesType is not implemented.");
	}

	//-------------------------------------------------------------------------

	/**
	 * For swapping two players.
	 * 
	 * @param takeType The type of property to take.
	 * @param player1  The index of the first player.
	 * @param role1    The role of the first player.
	 * @param player2  The index of the second player.
	 * @param role2    The role of the second player.
	 * @param then     The moves applied after that move is applied.
	 * 
	 * @example (swap Players P1 P2)
	 */
	public static Moves construct
	(
		         final SwapPlayersType takeType, 
		    @Or  final IntFunction     player1,
		    @Or  final RoleType        role1,
		    @Or2 final IntFunction     player2, 
		    @Or2 final RoleType        role2,
	   @Opt      final Then            then
	)
	{
		int numNonNull = 0;
		if (player1 != null)
			numNonNull++;
		if (role1 != null)
			numNonNull++;

		if (numNonNull != 1)
			throw new IllegalArgumentException("Swap(): Exactly one player1 or role1 parameter must be non-null.");
		
		int numNonNull2 = 0;
		if (player2 != null)
			numNonNull2++;
		if (role2 != null)
			numNonNull2++;

		if (numNonNull2 != 1)
			throw new IllegalArgumentException("Swap(): Exactly one player2 or role2 parameter must be non-null.");
		
		switch (takeType)
		{
		case Players:
			return new SwapPlayers(player1, role1, player2, role2, then);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Swap(): A SwapSitesType is not implemented.");
	}

	//-------------------------------------------------------------------------

	private Swap()
	{
		super(null);
		// Ensure that compiler does pick up default constructor
	}

	@Override
	public Moves eval(final Context context)
	{
		// Should not be called, should only be called on subclasses
		throw new UnsupportedOperationException("Swap.eval(): Should never be called directly.");
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
		throw new UnsupportedOperationException("Swap.canMoveTo(): Should never be called directly.");
	}
	
	//-------------------------------------------------------------------------

}
