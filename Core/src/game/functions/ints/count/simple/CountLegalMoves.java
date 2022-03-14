package game.functions.ints.count.simple;

import java.util.BitSet;

import annotations.Hide;
import game.Game;
import game.functions.ints.BaseIntFunction;
import other.context.Context;

/**
 * Returns the number of edges.
 * 
 * @author Eric.Piette
 */
@Hide
public final class CountLegalMoves extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * 
	 */
	public CountLegalMoves()
	{
	}

	//-------------------------------------------------------------------------

	@Override
	public int eval(final Context context)
	{
		return context.game().moves(context).moves().size();
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		return true;
	}

	@Override
	public String toString()
	{
		return "LegalMoves()";
	}

	@Override
	public long gameFlags(final Game game)
	{
		return 0l;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
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
