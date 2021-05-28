package game.functions.booleans.is.simple;

import java.util.BitSet;

import annotations.Hide;
import game.Game;
import game.functions.booleans.BaseBooleanFunction;
import other.context.Context;

/**
 * Checks if there are no empty cells in the board.
 * 
 * @author Eric.Piette and cambolbro
 */
@Hide
public final class IsFull extends BaseBooleanFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * 
	 */
	public IsFull()
	{
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean eval(final Context context)
	{
		return context.state().containerStates()[0].emptyRegion(context.board().defaultSite()).sites().length == 0;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		String str = "";
		str += "Full()";
		return str;
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
		return 0;
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
		// Do Nothing
	}
}
