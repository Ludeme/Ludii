package game.functions.booleans.is.simple;

import java.util.BitSet;

import annotations.Hide;
import game.Game;
import game.functions.booleans.BaseBooleanFunction;
import other.context.Context;

/**
 * Checks if the current state is a pending state.
 * 
 * @author Eric.Piette
 * @remarks A state is in pending if any value is on the pending list.
 */
@Hide
public final class IsPending extends BaseBooleanFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**  
	 *
	 */
	public IsPending()
	{
		// Nothing to do.
	}
	
	//-------------------------------------------------------------------------

	@Override
	public boolean eval(final Context context)
	{
		return context.state().isPending();
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
		return 0L;
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
		// Do nothing.
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{
		return "a state is pending";
	}
	
	//-------------------------------------------------------------------------
}
