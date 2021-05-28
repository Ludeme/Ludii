package game.functions.ints.board.where;

import annotations.Name;
import annotations.Opt;
import annotations.Or;
import game.Game;
import game.functions.booleans.BooleanFunction;
import game.functions.ints.BaseIntFunction;
import game.functions.ints.IntFunction;
import game.types.board.SiteType;
import game.types.play.RoleType;
import other.context.Context;

/**
 * Returns the site (or level) of a piece if it is on the board/site, else OFF
 * (-1).
 * 
 * @author Eric.Piette
 * @remarks The name of the piece can be specific without the number on it
 *          because the owner is also specified in the ludeme.
 */
@SuppressWarnings("javadoc")
public final class Where extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * If a piece is on the board, return its site else Off (-1).
	 * 
	 * @param namePiece   The name of the piece (without the number at the end).
	 * @param indexPlayer The index of the owner.
	 * @param role        The roleType of the owner.
	 * @param state       The local state of the piece.
	 * @param type        The graph element type [default SiteType of the board].
	 * 
	 * @example (where "Pawn" Mover)
	 */
	public static IntFunction construct
	(
			           final String       namePiece, 
			 @Or       final IntFunction  indexPlayer,
			 @Or       final RoleType     role,
		@Opt     @Name final IntFunction  state,
		@Opt           final SiteType     type
	)
	{
		int numNonNull = 0;
		if (indexPlayer != null)
			numNonNull++;
		if (role != null)
			numNonNull++;

		if (numNonNull != 1)
			throw new IllegalArgumentException("Exactly one Or parameter must be non-null.");

		return new WhereSite(namePiece, indexPlayer, role, state, type);
	}
	
	/**
	 * If a piece is on the board, return its site else Off (-1).
	 * 
	 * @param what The index of the piece.
	 * @param type The graph element type [default SiteType of the board].
	 * 
	 * @example (where (what at:(last To)))
	 */
	public static IntFunction construct
	(
			 final IntFunction what,
		@Opt final SiteType    type
	)
	{
		return new WhereSite(what, type);
	}
	
	/**
	 * If a piece is on a site, return its level else Off (-1).
	 * 
	 * @param whereType   The type of the where location.
	 * @param namePiece   The name of the piece (without the number at the end).
	 * @param indexPlayer The index of the owner.
	 * @param role        The roleType of the owner.
	 * @param state       The local state of the piece.
	 * @param type        The graph element type [default SiteType of the board].
	 * @param at          The site to check.
	 * @param fromTop     If true, check the stack from the top [True].
	 * 
	 * @example (where Level (what at:(last To)) at:(last To))
	 */
	public static IntFunction construct
	(
				      final WhereLevelType  whereType,
			          final String          namePiece, 
			@Or       final IntFunction     indexPlayer,
			@Or       final RoleType        role,
		@Opt    @Name final IntFunction     state,
		@Opt          final SiteType        type,
		        @Name final IntFunction     at,
	    @Opt    @Name final BooleanFunction fromTop
	)
	{
		return new WhereLevel(namePiece, indexPlayer, role, state, type,at,fromTop);
	}
	
	/**
	 * If a piece is on a site, return its level else Off (-1).
	 * 
	 * @param whereType The type of the where location.
	 * @param what      The index of the piece.
	 * @param type      The graph element type [default SiteType of the board].
	 * @param at        The site to check.
	 * @param fromTop   If true, check the stack from the top [True].
	 * 
	 * @example (where Level "Pawn" Mover at:(last To))
	 */
	public static IntFunction construct
	(
				   final WhereLevelType  whereType,
				   final IntFunction     what,
	   @Opt        final SiteType        type,
		     @Name final IntFunction     at,
	   @Opt  @Name final BooleanFunction fromTop
	)
	{
		return new WhereLevel(what, type,at,fromTop);
	}

	private Where()
	{
		// Make grammar pick up construct() and not default constructor
	}

	//-------------------------------------------------------------------------

	@Override
	public int eval(final Context context)
	{
		// Should not be called, should only be called on subclasses
		throw new UnsupportedOperationException("Count.eval(): Should never be called directly.");

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
