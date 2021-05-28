package game.functions.ints;

import annotations.Hide;
import game.Game;
import other.context.Context;

/**
 * Constant int value.
 * 
 * @author cambolbro
 */
@Hide
public final class IntConstant extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Constant value. */
	private final int a;

	//-------------------------------------------------------------------------

	/**
	 * @param a The int value.
	 */
	public IntConstant(final int a)
	{
		this.a = a;
	}

	//-------------------------------------------------------------------------

	@Override
	public int eval(final Context context)
	{
		return a;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		final String str = "" + a;
		return str;
	}

	@Override
	public boolean isStatic()
	{
		return true;
	}

	@Override
	public long gameFlags(final Game game)
	{
		return 0;
	}
	
	@Override
	public void preprocess(final Game game)
	{
		// nothing to do
	}
}
