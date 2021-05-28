package game.functions.floats;

import annotations.Hide;
import game.Game;
import other.context.Context;

/**
 * Sets a constant int value.
 * 
 * @author cambolbro
 */
@Hide
public final class FloatConstant extends BaseFloatFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Constant value. */
	private final float a;

	//-------------------------------------------------------------------------

	/**
	 * Constant int value.
	 * 
	 * @param a The int value.
	 */
	public FloatConstant
	(
		final float a
	)
	{
		this.a = a;
	}

	//-------------------------------------------------------------------------

	@Override
	public float eval(final Context context)
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
