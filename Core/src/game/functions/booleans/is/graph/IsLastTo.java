package game.functions.booleans.is.graph;

import java.util.BitSet;

import annotations.Hide;
import game.Game;
import game.functions.booleans.BaseBooleanFunction;
import game.types.board.SiteType;
import other.context.Context;

/**
 * Checks if the to location of the last move is a specific graph element type.
 * 
 * @author Eric.Piette
 */
@Hide
public final class IsLastTo extends BaseBooleanFunction
{
	private static final long serialVersionUID = 1L;
	
	//-------------------------------------------------------------------------

	/** The graph Element Type to check. */
	private final SiteType type;

	//-------------------------------------------------------------------------

	/**
	 * @param type The graph element type to check.
	 */
	public IsLastTo
	(
		 final SiteType type
	)
	{
		this.type = type;
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean eval(final Context context)
	{
		return context.trial().lastMove().toType() == type;
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		return false;
	}

	@Override
	public long gameFlags(final Game game)
	{
		return SiteType.gameFlags(type);
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(SiteType.concepts(type));
		return concepts;
	}
	
	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		// Nothing to do.
	}
}
